/*   FILE: GNBEventHandler.java
 *   DATE OF CREATION:  Mon Oct 23 08:51:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.gnb;

import java.awt.Dimension;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Hashtable;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.ViewEventHandler;

import com.hp.hpl.jena.rdf.model.Resource;

class GNBEventHandler implements ViewEventHandler, AnimationListener, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 8.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 9.0f;

    float oldCameraAltitude = GeonamesBrowser.START_ALTITUDE;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    int currentJPX, currentJPY;
    long lastVX, lastVY;

    static final double DRAG_FACTOR = 50.0;

    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;

    static final short ZOOMING_IN = -1;
    static final short ZOOMING_OUT = 1;
    static final short NOT_ZOOMING = 0;
    short zoomDirection = NOT_ZOOMING;

    long[] wnes; //region seen through camera

    boolean cursorNearBorder = false;

    GeonamesBrowser application;

    GNBEventHandler(GeonamesBrowser appli){
	application = appli;
	application.vsm.animator.setAnimationListener(this);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
	application.mm.updateVisibleMaps(application.mView.getVisibleRegion(application.mCamera), false, (short)0);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (lensType != NO_LENS){
	    application.zoomInPhase2(lastVX, lastVY);
	}
	else {
	    if (cursorNearBorder){// do not activate the lens when cursor is near the border
		return;
	    }
	    application.zoomInPhase1(jpx, jpy);
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (lensType != NO_LENS){
	    application.zoomOutPhase2();
	}
	else {
	    if (cursorNearBorder){// do not activate the lens when cursor is near the border
		return;
	    }
	    application.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
	}
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	currentJPX = jpx;
	currentJPY = jpy;
	if ((jpx-GeonamesBrowser.LENS_R1) < 0){
	    jpx = GeonamesBrowser.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+GeonamesBrowser.LENS_R1) > application.panelWidth){
	    jpx = application.panelWidth - GeonamesBrowser.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-GeonamesBrowser.LENS_R1) < 0){
	    jpy = GeonamesBrowser.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+GeonamesBrowser.LENS_R1) > application.panelHeight){
	    jpy = application.panelHeight - GeonamesBrowser.LENS_R1;
	    cursorNearBorder = true;
	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, e.getWhen());
	}	
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	currentJPX = jpx;
	currentJPY = jpy;
 	if (buttonNumber != 2){
	    float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude))/application.mCamera.focal;
	    application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
	    application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
	    application.vsm.animator.Aspeed = 0;
 	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, e.getWhen());
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == ViewEventHandler.WHEEL_UP){
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.mCamera);
	    }
	    else {
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.mCamera);
	    }
	}
	else {
	    float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude))/application.mCamera.focal;
	    if (wheelDirection  == WHEEL_UP){// zooming in
		application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	    else {//wheelDirection == WHEEL_DOWN, zooming out
		application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	}
    }

    public void enterGlyph(Glyph g){
	if (g instanceof BRectangle){
	    application.fm.showInformationAbout((Resource)g.getOwner(), currentJPX, currentJPY);
	}
    }

    public void exitGlyph(Glyph g){
	if (g instanceof BRectangle){
	    application.fm.hideInformationAbout();
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_J){application.mm.switchAdaptMaps();}
	else if (code==KeyEvent.VK_K){
	    application.SHOW_MEMORY_USAGE = !application.SHOW_MEMORY_USAGE;
	    application.vsm.repaintNow();
	}
	else if (code==KeyEvent.VK_G){application.gc();}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
 	System.exit(0);
    }


    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}
    
    public void cameraMoved(){
	// region seen through camera
	wnes = application.mView.getVisibleRegion(application.mCamera);
	// update grid depth
	float alt = application.mCamera.getAltitude();
	if (alt != oldCameraAltitude){
	    if (lensType == NO_LENS){
		application.updateLabels(alt);
		application.mm.updateMapLevel(alt);
	    }
	    oldCameraAltitude = alt;
	}
	else {// camera movement was a simple translation
	    // update detailed map(s) depending on what region is currently observed
	    // only if camera is not being moved as a result of lens activation/deactivation
	    application.mm.updateVisibleMaps(wnes, true, application.mm.getMapLevel(alt));
	}
	// stop camera if going too far away from map
	if (cameraFarFromMapH(wnes, application.vsm.animator.Xspeed < 0)){
	    application.vsm.animator.Xspeed = 0;
	}
	if (cameraFarFromMapV(wnes, application.vsm.animator.Yspeed < 0)){
	    application.vsm.animator.Yspeed = 0;
	}
    }

    boolean cameraFarFromMapH(long[] wnes, boolean goingEast){// false if going west
	if (wnes[0] > GeonamesBrowser.HALF_MAP_WIDTH && goingEast ||
	    wnes[2] < -GeonamesBrowser.HALF_MAP_WIDTH && !goingEast){
	    return true;
	}
	else {
	    return false;
	}
    }

    boolean cameraFarFromMapV(long[] wnes, boolean goingNorth){// false if going south
	if (wnes[1] < -GeonamesBrowser.HALF_MAP_HEIGHT && !goingNorth ||
	    wnes[3] > GeonamesBrowser.HALF_MAP_HEIGHT && goingNorth){
	    return true;
	}
	else {
	    return false;
	}
    }

}
