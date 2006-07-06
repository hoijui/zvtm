/*   FILE: AbstractTaskEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.eval;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Hashtable;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

import net.claribole.zvtm.engine.ViewEventHandler;


class AbstractTaskEventHandler implements ViewEventHandler, AnimationListener, ComponentListener {

    static final String DOT = ".";
    static final int NB_DEC = 3;

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    float oldCameraAltitude = 0;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
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

    static final short LOWER_ALTITUDE = -1;
    static final short SAME_ALTITUDE = 0;
    static final short HIGHER_ALTITUDE = 1;

    long[] wnes; //region seen through camera

    boolean cursorNearBorder = false;

    /* coordinates of mouse cursor */
    String latitude = "0.0";
    String longitude = "0.0";

    ZLAbstractTask application;

    AbstractTaskEventHandler(ZLAbstractTask appli){
	application = appli;
	application.vsm.animator.setAnimationListener(this);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}
    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
// 	System.exit(0);
    }
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_Q && mod == CTRL_MOD){System.exit(0);}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}
    
    public void cameraMoved(){
	// region seen through camera
	wnes = application.demoView.getVisibleRegion(application.demoCamera);
	// update grid depth
	float alt = application.demoCamera.getAltitude();
	if (alt != oldCameraAltitude){
//   	    System.err.println(alt);
	    if (lensType == NO_LENS){
		// grid
		application.updateGridLevel(Math.max(wnes[2]-wnes[0], wnes[1]-wnes[3]));
	    }
	    if (application.logm.trialStarted){
		if (alt < oldCameraAltitude){
		    application.logm.updateWorld(wnes, LOWER_ALTITUDE);
		    if (zoomDirection != ZOOMING_IN){
			application.logm.switchedZoomDirection();
		    }
		    zoomDirection = ZOOMING_IN;
		}
		else {// alt > oldCameraAltitude
		    application.logm.updateWorld(wnes, HIGHER_ALTITUDE);
		    if (zoomDirection != ZOOMING_OUT){
			application.logm.switchedZoomDirection();
		    }
		    zoomDirection = ZOOMING_OUT;
		}
	    }
	    oldCameraAltitude = alt;
	    application.cameraIsOnFloor(alt == 0.0);
	}
	else {
	    application.logm.updateWorld(wnes, SAME_ALTITUDE);
	}
	application.updateOverview();
	if (application.logm.trialStarted){
	    application.logm.writeCinematic();
	}
	application.updateDMRegion();
    }

    void dragMagMoved(){
	if (application.logm.trialStarted){
	    application.logm.portalPositionChanged(true);
	}
    }

}
