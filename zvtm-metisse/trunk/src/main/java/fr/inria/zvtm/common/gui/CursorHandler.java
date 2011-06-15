package fr.inria.zvtm.common.gui;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;

public class CursorHandler {

	private PCursor pcursor;
	private int jpx;
	private int jpy;
	private double vx;
	private double vy;
	private Robot rbt;
	private int refPosX;
	private int refPosY;
	Viewer viewer;
	private int frameLocOnScreenX;
	private int frameLocOnScreenY;
	private boolean exited;
	private boolean beware;
	private boolean active = true;
	private boolean recalib = false;
	private boolean virtualMode;


	public CursorHandler(PCursor out, Viewer v) {
		this.pcursor = out;
		this.viewer = v;
		try {
			this.rbt = new Robot();

		} catch (AWTException e) {
			e.printStackTrace();
		}
		updateRefPos();
		resetCursorPos();
		pcursor.setParent(this);
	}



	public PCursor getCursor() {
		return pcursor;
	}



	public int getX() {
		return jpx;
	}



	public int getY() {
		return jpy;
	}



	public void move(int jpx, int jpy, MouseEvent e) {
		if(!active)return;
		if((jpx+frameLocOnScreenX+1)==refPosX && (jpy+frameLocOnScreenY+27)==refPosY)return;
		if(pcursor==null)return;
		updateRefPos();		
		if(exited||recalib){
			this.jpx = e.getLocationOnScreen().x-frameLocOnScreenX;
			this.jpy = e.getLocationOnScreen().y-frameLocOnScreenY;
			Point.Double p = viewer.getView().getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
			vx = p.x;
			vy = p.y;
			pcursor.getPicker().setJPanelCoordinates(this.jpx, this.jpy);
			pcursor.moveCursorTo(vx, vy, this.jpy, this.jpy);
			exited = false;
			beware = true;
			recalib = false;
			return;
		}

		if(beware){
			this.jpx = e.getLocationOnScreen().x-frameLocOnScreenX;
			this.jpy = e.getLocationOnScreen().y-frameLocOnScreenY;
			beware = false;
		}
		else{
			Point.Double p = viewer.getView().getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx+(e.getLocationOnScreen().x-refPosX), this.jpy+(e.getLocationOnScreen().y-refPosY));
			if((virtualMode&&(p.x>PCursor.wallBounds[0])&&(p.x<PCursor.wallBounds[2]) )||
					(!virtualMode&&(this.jpx+e.getLocationOnScreen().x-refPosX)>=0 && (this.jpx+e.getLocationOnScreen().x-refPosX)<viewer.getView().getFrame().getWidth()))

				this.jpx += (e.getLocationOnScreen().x-refPosX);


			if((virtualMode&&(p.y-pcursor.bounds[1]+PCursor.wallBounds[3]<PCursor.wallBounds[1])) ||
					(!virtualMode&&(this.jpy+e.getLocationOnScreen().y-refPosY)<(viewer.getView().getFrame().getHeight()-27))){

				if((this.jpy+e.getLocationOnScreen().y-refPosY)<0){
					virtualMode = true;
					pcursor.enablePhantomMode();
				}
				else {
					if((this.jpx+e.getLocationOnScreen().x-refPosX)<0)this.jpx=0;
					if((this.jpx+e.getLocationOnScreen().x-refPosX)>=viewer.getView().getFrame().getWidth())this.jpx=viewer.getView().getFrame().getWidth()-1;
					virtualMode = false;
					pcursor.disablePhantomMode();
				}
				this.jpy += (e.getLocationOnScreen().y-refPosY);
			}
		}
		Point.Double p = viewer.getView().getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
		vx = p.x;
		vy = p.y;
		pcursor.moveCursorTo(vx, vy,this.jpx, this.jpy);

		rbt.mouseMove(refPosX, refPosY);
		if(virtualMode)dealWithVirtualMode(pcursor.getWallX(), pcursor.getWallY(),this.jpx, this.jpy);
	}



	private void dealWithVirtualMode(double vx2, double vy2, int jpx2, int jpy2) {
	}



	private void updateRefPos() {
		Point locOnScreen = viewer.getView().getFrame().getLocationOnScreen();
		frameLocOnScreenX = locOnScreen.x;
		frameLocOnScreenY = locOnScreen.y;

		refPosX = frameLocOnScreenX + (viewer.getView().getFrame().getWidth() / 2);
		refPosY = frameLocOnScreenY + (viewer.getView().getFrame().getHeight() / 2);
	}

	public boolean isRefPos(int jpx, int jpy) {
		boolean res =  (((jpx+frameLocOnScreenX+1)==refPosX)&&((27+jpy+frameLocOnScreenY)==refPosY));
		return res;
	}

	public void resetCursorPos(){
		this.jpx = refPosX-frameLocOnScreenX;
		this.jpy = refPosY-frameLocOnScreenY;
		Point.Double p = viewer.getView().getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
		vx = p.x;
		vy = p.y;
		pcursor.moveCursorTo(vx, vy,this.jpx,this.jpy);
		rbt.mouseMove(refPosX, refPosY);
	}



	public void deactivate() {
		recalib = true;
		active = false;
	}



	public void activate() {
		rbt.mouseMove(this.jpx, this.jpy);
		active = true;
	}


	public double getVX() {
		return vx;
	}



	public double getVY() {
		return vy;
	}

	public void jumpTo(double vx, double vy,int jpx,int jpy){
		this.vx = vx;
		this.vy = vy;
		this.jpx = jpx;
		this.jpy = jpy;
		rbt.mouseMove(jpx, jpy);
		rbt.mouseMove(refPosX, refPosY);
	}



	public void refresh() {
		pcursor.moveCursorTo(vx, vy,this.jpx,this.jpy);
	}



	public boolean VirtualMode() {
		return virtualMode;
	}


}
