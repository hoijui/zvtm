/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Viewer.java 2771 2010-01-15 10:27:47Z epietrig $
 */

package fr.inria.zvtm.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.kernel.Temporizer;

public class Viewer implements ComponentListener, MouseListener{

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
	private VRectangle horizon;
	private CursorHandler cursorHandler;
	private PCursor cursor;

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
		mView.getCursor().setVisibility(false);
		
		if (fullscreen){
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
		}
		else {
			mView.setVisible(true);
		}
		mView.mouse.setSensitivity(false);
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
			cursor = new PCursor(mSpace, clientId,mCamera,eh);
		}
		
		cursorHandler = new CursorHandler(cursor, this);
		backgroundHook();
	}


	protected void backgroundHook() {
		if(!isClient)addBackground();
	}

	protected void addBackground() {
		ImageIcon img = (new ImageIcon("src/main/java/fr/inria/zvtm/resources/bg.jpg"));
		mSpace.addGlyph(new VImage(img.getImage()));
	}

	public boolean hasCursor() {
		return (cursor!=null);
	}

	private void setHorizon() {
		horizon = new VRectangle(mCamera.vx,mCamera.vy,1,200,200,this.clientId);
		horizon.setBorderColor(this.clientId);
		horizon.setStroke(new BasicStroke(10f));
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
	
	public CursorHandler getCursorHandler(){
		return cursorHandler;
	}
	
	public void addFrame(fr.inria.zvtm.compositor.MetisseWindow f){
		mSpace.addGlyph(f);
		refresh();
	}

	public void remFrame(fr.inria.zvtm.compositor.MetisseWindow metisseWindow){
		mSpace.removeGlyph(metisseWindow);
		refresh();
	}

	public void moveViewTo(double x,double y){
		mCamera.moveTo(x, y);
		updateHorizon();
	}
	
	public void moveViewOf(double x,double y){
		mCamera.move(x, y);
		updateHorizon();
	}
	
	public void zoomOf(double d){
		mCamera.setAltitude(mCamera.getAltitude()+d);
		updateHorizon();
	}
	
	public void zoomViewTo(double z){
		mCamera.setAltitude(z);
		updateHorizon();
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
		cursor.setColor(clientId);
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

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	//	cursorHandler.hasEntered();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	//	cursorHandler.hasExited();
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
	
	public PCursor getCursor() {
		return cursor;
	}

	public void resetCursorPosition() {
		cursorHandler.resetCursorPos();
	}
}
