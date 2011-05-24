package fr.inria.zvtm.gui;

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
	private int refPosX = 400;
	private int refPosY = 400;
	private Viewer viewer;
	private int frameLocOnScreenX;
	private int frameLocOnScreenY;
	private boolean exited;
	private boolean beware;


	public CursorHandler(PCursor out, Viewer v) {
		this.pcursor = out;
		this.viewer = v;
		
		try {
			this.rbt = new Robot();

		} catch (AWTException e) {
			e.printStackTrace();
		}
	}



	public void move(int jpx, int jpy, MouseEvent e) {
		if(pcursor==null)return;
		updateRefPos();		
		if(exited){
			this.jpx = e.getLocationOnScreen().x-frameLocOnScreenX;
			this.jpy = e.getLocationOnScreen().y-frameLocOnScreenY;
			Point.Double p = viewer.mView.getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
			vx = p.x;
			vy = p.y;
			pcursor.getPicker().setJPanelCoordinates(this.jpx, this.jpy);
			pcursor.moveCursorTo(vx, vy, this.jpy, this.jpy);
			exited = false;
			beware = true;
			return;
		}

		if(beware){
			this.jpx = e.getLocationOnScreen().x-frameLocOnScreenX;
			this.jpy = e.getLocationOnScreen().y-frameLocOnScreenY;
			beware = false;
		}
		else{
			this.jpx += (e.getLocationOnScreen().x-refPosX);
			this.jpy += (e.getLocationOnScreen().y-refPosY);
		}
		Point.Double p = viewer.mView.getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
		vx = p.x;
		vy = p.y;		
		pcursor.moveCursorTo(vx, vy,this.jpx, this.jpy);

		if(!exiting(e)&&!exited)rbt.mouseMove(refPosX, refPosY);
		else {
			rbt.mouseMove(frameLocOnScreenX, frameLocOnScreenY);	
			exited = true;
		}
	}

	private boolean exiting(MouseEvent e) {
		return ((jpx*jpx+jpy*jpy)<300);
	}

	private void updateRefPos() {
		Point locOnScreen = viewer.mView.getFrame().getLocationOnScreen();
		frameLocOnScreenX = locOnScreen.x;
		frameLocOnScreenY = locOnScreen.y;

		refPosX = frameLocOnScreenX + (viewer.mView.getFrame().getWidth() / 2);
		refPosY = frameLocOnScreenY + (viewer.mView.getFrame().getHeight() / 2);
	}

	public int getX() {
		return jpx;
	}

	public int getY() {
		return jpy;
	}

	public boolean isRefPos(int jpx, int jpy) {
		boolean res =  (((jpx+frameLocOnScreenX+1)==refPosX)&&((27+jpy+frameLocOnScreenY)==refPosY));
		return res;
	}

	public void resetCursorPos(){
		this.jpx = refPosX-frameLocOnScreenX;
		this.jpy = refPosY-frameLocOnScreenY;
		Point.Double p = viewer.mView.getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
		vx = p.x;
		vy = p.y;
		pcursor.moveCursorTo(vx, vy,this.jpx,this.jpy);
		rbt.mouseMove(refPosX, refPosY);
	}


}
