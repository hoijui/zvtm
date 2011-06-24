package fr.inria.zvtm.common.gui;

import java.awt.Color;
import java.awt.Dimension;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.PPicker;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.SICircle;
import fr.inria.zvtm.glyphs.SIRectangle;

public class PCursor {

	public static double[] wallBounds= {-512,300,512,-300};
	protected double[] bounds;
	private SIRectangle cursorX;
	private SIRectangle cursorY;
	private SIRectangle phcursorX;
	private SIRectangle phcursorY;
	private SICircle circle;
	private double vx;
	private double vy;
	private PPicker picker;
	private PPicker picker2;
	private Camera mCamera;
	private Camera mCamera2;
	private ViewListener viewListener;
	private VirtualSpace ownerSpace;
	private VirtualSpace pickingSpace;
	private boolean phantomMode = false;
	private double wallX =0;
	private double wallY =0;
	private boolean mirrorMode = true;
	private CursorHandler parent;

	public PCursor(VirtualSpace cursorSpace,VirtualSpace pickingSpace,VirtualSpace menuSpace,Camera locCam,Camera menuCam ,ViewListener eh,double thickness,double size){
		Color c = getColor();
		cursorX = new SIRectangle(0, 0, 2, size, thickness, c);
		cursorY = new SIRectangle(0, 0, 2, thickness, size, c);
		cursorX.setBorderColor(c);
		cursorX.setVisible(true);
		this.ownerSpace = cursorSpace;
		this.pickingSpace = pickingSpace;
		ownerSpace.addGlyph(cursorX);
		cursorX.setSensitivity(false);
		cursorY.setBorderColor(c);
		cursorY.setVisible(true);
		ownerSpace.addGlyph(cursorY);
		cursorY.setSensitivity(false);

		c = getPhantomColor();
		phcursorX = new SIRectangle(0, 0, 2, size, thickness, c);
		phcursorY = new SIRectangle(0, 0, 2, thickness, size, c);
		circle = new SICircle(0, 0, 2, size, c);
		ownerSpace.addGlyph(circle);
		ownerSpace.addGlyph(phcursorX);
		ownerSpace.addGlyph(phcursorY);

		circle.setSensitivity(false);
		phcursorX.setSensitivity(false);
		phcursorY.setSensitivity(false);
		
		circle.setBorderColor(c);
		phcursorX.setBorderColor(Color.WHITE);
		phcursorY.setBorderColor(Color.WHITE);
		phcursorX.setColor(Color.WHITE);
		phcursorY.setColor(Color.WHITE);
		circle.setFilled(true);
		phcursorX.setFilled(true);
		phcursorY.setFilled(true);
		
		phcursorX.setVisible(false);
		phcursorY.setVisible(false);
		circle.setVisible(false);

		picker = new PPicker();
		picker2 = new PPicker();
		pickingSpace.registerPicker(picker);
		menuSpace.registerPicker(picker2);
		this.mCamera = locCam;
		this.mCamera2 = menuCam;
		this.viewListener = eh;

	}

	public PCursor() {
	
	}

	public void moveCursorTo(double x, double y, int jpx, int jpy){
		this.vx = x;
		this.vy = y;
		cursorX.moveTo(x, y);
		cursorY.moveTo(x, y);
		movePhantom(x,y,jpx,jpy);
		picker.setVSCoordinates(x, y);
		picker.setJPanelCoordinates(jpx, jpy);
		
		Camera c = ownerSpace.getCamera(0);
		double a = (c.focal+c.altitude)/c.focal;
		picker2.setVSCoordinates((x-c.vx)/a, (y-c.vy)/a);
		picker2.setJPanelCoordinates(jpx, jpy);
		refreshPicker();
	}

	private void movePhantom(double x, double y, int jpx, int jpy) {
		if(!phantomMode)return;

		bounds = pickingSpace.getCamera(0).getOwningView().getVisibleRegion(pickingSpace.getCamera(0));
		double w =(bounds[2]-bounds[0]);
		double h =(bounds[1]-bounds[3]);
		double W =(wallBounds[2]-wallBounds[0]);
		double H =(wallBounds[1]-wallBounds[3]);

		double xx = (x-wallBounds[0])/W*w+bounds[0];
		double yy = (y-bounds[1])/H*h+bounds[3];

		wallX = x;
		wallY = y-bounds[1]+wallBounds[3];

		if(mirrorMode){
			yy = bounds[1]+bounds[3]-yy;
		}

		phcursorX.moveTo(xx,yy);
		phcursorY.moveTo(xx,yy);
		circle.moveTo(xx,yy);
	}

	public PPicker getPicker() {
		return picker;
	}

	public PPicker getPicker2() {
		return picker2;
	}

	public void setColor(Color clientId){
		cursorX.setBorderColor(clientId);
		cursorX.setColor(clientId);
		cursorY.setBorderColor(clientId);
		cursorY.setColor(clientId);
	}

	public double getVSXCoordinate(){
		return vx;
	}
	public double getVSYCoordinate(){
		return vy;
	}

	public void refreshPicker() {
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
				picker.computePickedGlyphList(viewListener, mCamera);	
				picker2.computePickedGlyphList(viewListener, mCamera2);	
//			}
//		});
	}

	private static Color getColor() {
		return new Color(200,0,0);
	}

	private static Color getPhantomColor() {
		return new Color(000,150,150);
	}

	public void enablePhantomMode(){
		if(phantomMode)return;
		phantomMode = true;
		
		//jump to ensure visual continuity
		bounds = pickingSpace.getCamera(0).getOwningView().getVisibleRegion(pickingSpace.getCamera(0));
		double w =(bounds[2]-bounds[0]);
		double W =(wallBounds[2]-wallBounds[0]);
		Camera c = ownerSpace.getCamera(0);
		Dimension d = parent.viewer.getView().getPanelSize();
		double coef = c.focal / (c.focal+c.altitude);
		double vxx = (vx-bounds[0])*W/w+wallBounds[0];
		int cx = (int)Math.round((d.width/2)+(vxx-c.vx)*coef);
		int cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
		parent.jumpTo(vxx, vy,cx,cy);


		phcursorX.setVisible(true);
		phcursorY.setVisible(true);
		circle.setVisible(true);
	}

	public void disablePhantomMode(){
		if(!phantomMode)return;
		phantomMode = false;
		
		//jump to ensure visual continuity
		bounds = pickingSpace.getCamera(0).getOwningView().getVisibleRegion(pickingSpace.getCamera(0));
		double w =(bounds[2]-bounds[0]);
		double W =(wallBounds[2]-wallBounds[0]);
		Camera c = ownerSpace.getCamera(0);
		Dimension d = parent.viewer.getView().getPanelSize();
		double coef = c.focal / (c.focal+c.altitude);
		double vxx = (vx-wallBounds[0])*w/W+bounds[0];
		int cx = (int)Math.round((d.width/2)+(vxx-c.vx)*coef);
		int cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
		parent.jumpTo(vxx, vy,cx,cy);
		
		phcursorX.setVisible(false);
		phcursorY.setVisible(false);
		circle.setVisible(false);
	}

	public void togglePhantomMode(){
		if(phantomMode)disablePhantomMode();
		else enablePhantomMode();
	}

	public double getWallX(){
		return wallX;
	}

	public double getWallY(){
		return wallY;
	}

	public void setParent(CursorHandler cursorHandler) {
		parent = cursorHandler;
	}

	public void setVisible(boolean b) {
		cursorX.setVisible(b);
		cursorY.setVisible(b);
		if(!phantomMode)return;
		phcursorX.setVisible(b);
		phcursorY.setVisible(b);
		circle.setVisible(b);
	}

	public void end() {
		ownerSpace.removeGlyph(circle);
		ownerSpace.removeGlyph(cursorX);
		ownerSpace.removeGlyph(cursorY);
		ownerSpace.removeGlyph(phcursorX);
		ownerSpace.removeGlyph(phcursorY);
	}

}
