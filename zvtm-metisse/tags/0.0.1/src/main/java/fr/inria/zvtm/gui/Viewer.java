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
	public VirtualSpace cursorSpace;
	protected VirtualSpaceManager vsm;
	/**
	 * The virtual space displayed on the wall and on client views. It is shared by all clients
	 */
	public VirtualSpace wallSpace;
	/**
	 * The space where the user operates it is not visible on the wall
	 */
	private VirtualSpace localSpace;
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
	private boolean mobileDesktop = true;
	private Camera localCamera;
	private Camera cursorCamera;
	

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
			boolean antialiased, VirtualSpace vs, VirtualSpace cursorSpace) {
		vsm = VirtualSpaceManager.INSTANCE;
		preInitHook();
		windowLayout();
		nm = new Navigation(this);
		if(!isClient){
			this.wallSpace = vsm.addVirtualSpace(Messages.WallSpaceName);
			this.cursorSpace = vsm.addVirtualSpace(Messages.CursorSpaceName);
		}
		else{
			this.wallSpace = vs;
			this.localSpace = vsm.addVirtualSpace(Messages.LocalSpaceName);
			this.cursorSpace = cursorSpace;
			//	vsm.addVirtualSpace(vs.getName());//meme machine virtuelle donc vsm partagé ! il faudra gérer ça ...
		}
		
		mCamera = wallSpace.addCamera();
		cursorCamera = this.cursorSpace.addCamera();
		mCamera.stick(cursorCamera);
		
		if (isClient){
			mCamera.addListener(new HorizonUpdater(this));
			
			localCamera = localSpace.addCamera();
			localCamera.setZoomFloor(-99);
			if(mobileDesktop)mCamera.stick(localCamera);
		}
		mCamera.setZoomFloor(-99);
		cursorCamera.setZoomFloor(-99);
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(mCamera);
		if (isClient)cameras.add(localCamera);
		cameras.add(cursorCamera);
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
		GraphicsEnvironment e =GraphicsEnvironment.getLocalGraphicsEnvironment();
		if(isClient){
			mView.getFrame().setLocation((int) (e.getCenterPoint().x-mView.getFrame().getWidth()*1./2), (int)(e.getCenterPoint().y-mView.getFrame().getHeight()*1./2));
			setHorizon();
			cursor = new PCursor(this.cursorSpace, clientId,mCamera,localCamera,eh);
		}
		cursorHandler = new CursorHandler(cursor, this);
		backgroundHook();
	}


	protected void backgroundHook() {
		if(!isClient)addBackground();
	}

	protected void addBackground() {
		ImageIcon img = (new ImageIcon("src/main/java/fr/inria/zvtm/resources/bg.jpg"));
		wallSpace.addGlyph(new VImage(img.getImage()));
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
		wallSpace.addGlyph(horizon);
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
		if(isClient){
			if(f.isOnWall())wallSpace.addGlyph(f);
			else localSpace.addGlyph(f);
		}
		refresh();
	}

	public void remFrame(fr.inria.zvtm.compositor.MetisseWindow f){
		if(isClient){
			if(f.isOnWall())wallSpace.removeGlyph(f);
			else localSpace.removeGlyph(f);
		}
		refresh();
	}
	
	/**
	 * transfers the given MetisseWindow from the localSpace to the wallSpace
	 * @param metisseWindow
	 */
	public void teleport(fr.inria.zvtm.compositor.MetisseWindow metisseWindow){
		if(!isClient)return;
		if(!localSpace.getAllGlyphs().contains(metisseWindow)){
			return;
		}
		localSpace.removeGlyph(metisseWindow);
		wallSpace.addGlyph(metisseWindow);
		metisseWindow.setOnWall(true);
	}
	
	/**
	 * transfers the given MetisseWindow from the wallSpace to the localSpace
	 * @param metisseWindow
	 */
	public void callBack(fr.inria.zvtm.compositor.MetisseWindow metisseWindow){
		if(!isClient)return;
		if(!wallSpace.getAllGlyphs().contains(metisseWindow)){
			return;
		}
		wallSpace.removeGlyph(metisseWindow);
		localSpace.addGlyph(metisseWindow);
		metisseWindow.setOnWall(false);
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
	
	public VirtualSpace getLocalSpace(){
		return localSpace;
	}
}
