package fr.inria.zvtm.common.gui;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.VirtualSpace;

/**
 * This class emulates the behavior of a real cursor, but all in zvtm. It traps the system's cursor to impair it from going out the application's frame and loose focus, and listen to it's moves. The emulated cursor (the {@link PCursor}) is a zvtm glyph set and can go anywhere in the {@link VirtualSpace}, even outside the laptop's screen.
 * The main purpose is to make it possible for the cursor to go "on the wall", by exiting by the top of the screen.
 * @author Julien Altieri
 *
 */
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

	/**
	 * 
	 * @param out the {@link PCursor} who will be used for the display.
	 * @param v The viewer to which it is related.
	 */
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


/**
 * 
 * @return the zvtm {@link PCursor}
 */
	public PCursor getCursor() {
		return pcursor;
	}



	public int getX() {
		return jpx;
	}



	public int getY() {
		return jpy;
	}


/**
 * Makes the virtual cursor move in the virtual space.
 * @param jpx The position of the system's cursor
 * @param jpy The position of the system's cursor
 * @param e Event of the system's cursor
 */
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

	/**
	 * RefPos is the position where the system's cursor is replaced at each event. Here we test if the cursor is in his reference position.
	 * @param jpx
	 * @param jpy
	 */
	public boolean isRefPos(int jpx, int jpy) {
		boolean res =  (((jpx+frameLocOnScreenX+1)==refPosX)&&((27+jpy+frameLocOnScreenY)==refPosY));
		return res;
	}

	/**
	 * Move the virtual cursor at the center of the client view.
	 */
	public void resetCursorPos(){
		this.jpx = refPosX-frameLocOnScreenX;
		this.jpy = refPosY-frameLocOnScreenY;
		Point.Double p = viewer.getView().getPanel().viewToSpaceCoords(viewer.mCamera, this.jpx, this.jpy);
		vx = p.x;
		vy = p.y;
		pcursor.moveCursorTo(vx, vy,this.jpx,this.jpy);
		rbt.mouseMove(refPosX, refPosY);
	}


	/**
	 * Disable the robot, and the handling of the virtual cursor.
	 */
	public void deactivate() {
		recalib = true;
		active = false;
	}


	/**
	 * Enables the robot, and the handling of the virtual cursor.
	 */
	public void activate() {
		rbt.mouseMove(this.jpx, this.jpy);
		active = true;
	}

	/**
	 * 
	 * @return the virtual x position of the emulated cursor.
	 */
	public double getVX() {
		return vx;
	}


	/**
	 * 
	 * @return the virtual y position of the emulated cursor.
	 */
	public double getVY() {
		return vy;
	}

	/**
	 * Make the virtual cursor move to (vx+jpx-refPosX,vy+jpy-refPosY). Used for the mapping of the top of the laptop on the bottom of the wall.
	 * @param vx
	 * @param vy
	 * @param jpx
	 * @param jpy
	 */
	public void jumpTo(double vx, double vy,int jpx,int jpy){
		this.vx = vx;
		this.vy = vy;
		this.jpx = jpx;
		this.jpy = jpy;
		rbt.mouseMove(jpx, jpy);
		rbt.mouseMove(refPosX, refPosY);
	}

	/**
	 * Virtual mode is when the cursor is on the wall.
	 * @return true if the cursor is on the wall
	 */
	public boolean VirtualMode() {
		return virtualMode;
	}


}
