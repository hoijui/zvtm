/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Viewer.java 2771 2010-01-15 10:27:47Z epietrig $
 */

package fr.inria.zvtm.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Vector;

import javax.swing.JFrame;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.kernel.Main;
import fr.inria.zvtm.kernel.Temporizer;

public class Viewer implements ComponentListener{

	/* screen dimensions, actual dimensions of windows */
	private int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
	private int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
	private int VIEW_MAX_W = 1024;  // 1400
	private int VIEW_MAX_H = 768;   // 1050
	private int VIEW_W, VIEW_H;
	@SuppressWarnings("unused")
		private int VIEW_X, VIEW_Y;
	/* dimensions of zoomable panel */
	protected int panelWidth, panelHeight;
	private long lastRepaintTime ;
	protected VirtualSpaceManager vsm;
	public VirtualSpace mSpace;
	protected EView mView;
	public boolean isClient;
	private Color clientId;

	protected MainEventHandler eh;
	protected Navigation nm;

	protected Camera mCamera;
	public boolean dragging = false;
	private VRectangle cursorX;
	private VRectangle cursorY;
	private VRectangle horizon;


	public Viewer(boolean isClient){
		this.isClient = isClient; 
		clientId = getRandomColor();
	}

	protected void preInitHook(){}

	protected void viewCreatedHook(Vector<Camera> cameras){}

	/**
	 * @param vs VirtualSpace to provide if this Viewer is a client.
	 */
	public void init(boolean fullscreen, boolean opengl,
			boolean antialiased, VirtualSpace vs) {
		vsm = VirtualSpaceManager.INSTANCE;
		preInitHook();
		windowLayout();
		nm = new Navigation(this);
		if(!isClient)mSpace = vsm.addVirtualSpace(Messages.mSpaceName);
		else{
			mSpace = vs;
			//	vsm.addVirtualSpace(vs.getName());//meme machine virtuelle donc vsm partagé ! il faudra gérer ça ...
		}
		mCamera = mSpace.addCamera();
		if (isClient){
			mCamera.addListener(new HorizonUpdater(this));
		}
		mCamera.setZoomFloor(-99);
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(mCamera);
		nm.setCamera(mCamera);
		mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H,
				false, false, !fullscreen, null);
		if (fullscreen){
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
		}
		else {
			mView.setVisible(true);
		}
		updatePanelSize();
		eh = new MainEventHandler(this);
		mView.setListener(eh, 0);
		((JFrame)mView.getFrame()).addKeyListener(eh);
		mView.setAntialiasing(antialiased);
		mView.setBackgroundColor(Config.BACKGROUND_COLOR);
		mView.getPanel().getComponent().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);

		viewCreatedHook(cameras);

		if(isClient){
			setHorizon();
			addCursor();
		}
	}

	private void addCursor(){
		cursorX = new VRectangle(0, 0, 1, 20, 1, this.clientId);
		cursorX.setBorderColor(this.clientId);
		cursorX.setVisible(true);
		mSpace.addGlyph(cursorX);
		cursorX.setSensitivity(false);
		cursorY = new VRectangle(0, 0, 1, 1, 20, this.clientId);
		cursorY.setBorderColor(this.clientId);
		cursorY.setVisible(true);
		mSpace.addGlyph(cursorY);
		cursorY.setSensitivity(false);

		refresh();
	}

	public boolean hasCursor() {
		return (cursorX!=null && cursorY!=null);
	}
	private void moveCursor(double x, double y){
		cursorX.moveTo(x, y);
		cursorY.moveTo(x, y);
	}
	
	public void updateCursor() {
		moveCursor(mView.getPanel().getVCursor().getVSXCoordinate(), mView.getPanel().getVCursor().getVSYCoordinate());
	}
	private void setHorizon() {
		horizon = new VRectangle(mCamera.vx,mCamera.vy,1,200,200,this.clientId);
		horizon.setBorderColor(this.clientId);
		horizon.setVisible(true);
		horizon.setFilled(false);
		horizon.setSensitivity(false);
		mSpace.addGlyph(horizon);
		mView.getFrame().addComponentListener(this);
		updateHorizon();
	}
	
	public void updateHorizon(){
		if(!isClient) System.err.println("error trying to update horizon of the a non-client camera");
		double a = (Math.abs(mCamera.focal+mCamera.altitude))/100;
		horizon.setWidth(a*this.panelWidth);
		horizon.setHeight(a*this.panelHeight);
		horizon.moveTo(mCamera.vx, mCamera.vy);
		
	}
	public void addFrame(fr.inria.zvtm.compositor.MetisseWindow f){
		mSpace.addGlyph(f);
		refresh();
	}

	public void remFrame(fr.inria.zvtm.compositor.MetisseWindow metisseWindow){
		mSpace.removeGlyph(metisseWindow);
		refresh();
	}


	void windowLayout(){
		if (Utils.osIsWindows()){
			VIEW_X = VIEW_Y = 0;
		}
		else if (Utils.osIsMacOS()){
			VIEW_X = 80;
			SCREEN_WIDTH -= 80;
		}
		VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
		VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
	}
	
	void updatePanelSize(){
		Dimension d = mView.getPanel().getComponent().getSize();
		panelWidth = d.width;
		panelHeight = d.height;
	}

	void exit(){
		System.exit(0);
	}

	public void refresh() {
		if(System.currentTimeMillis()-lastRepaintTime>Temporizer.repaintMinDelay){
			mView.repaint();
			VirtualSpaceManager.INSTANCE.repaint();
			lastRepaintTime = System.currentTimeMillis();
		}
	}
	private static Color getRandomColor() {
		return new Color((int)(Math.random()*255),(int)(Math.random()*255) ,(int) (Math.random()*255));
	}
	public void changeColor(){
		if(!isClient)return;
		clientId = getRandomColor();
		cursorX.setBorderColor(clientId);
		cursorX.setColor(clientId);
		cursorY.setBorderColor(clientId);
		cursorY.setColor(clientId);
		horizon.setBorderColor(clientId);
	}
	@Override
	public void componentHidden(ComponentEvent arg0) {}
	@Override
	public void componentMoved(ComponentEvent arg0) {}
	@Override
	public void componentResized(ComponentEvent arg0) {
		updatePanelSize();
		updateHorizon();
	}
	@Override
	public void componentShown(ComponentEvent arg0) {}
}
