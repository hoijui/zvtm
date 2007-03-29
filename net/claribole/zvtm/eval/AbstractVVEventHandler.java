/*   FILE: TV2DEventHandler.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class AbstractVVEventHandler implements ViewEventHandler, ComponentListener {

    static final String DISPLAY_ANSWER_ON = "Display IC region";
    static final String DISPLAY_ANSWER_OFF = "Do not display IC region";
    static final String FOLLOW_CAMERA_ON = "Subjective Mode";
    static final String FOLLOW_CAMERA_OFF = "Objective Mode";

    AbstractVisitViewer application;

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 8.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 9.0f;

    float oldCameraAltitude = ZLWorldTask.START_ALTITUDE;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;

    static final double DRAG_FACTOR = 50.0;

    long[] wnes; //region seen through camera

    AbstractVVEventHandler(AbstractVisitViewer appli){
	application = appli;
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
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
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
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
 	if (buttonNumber != 2){
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	    application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
	    application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
	    application.vsm.animator.Aspeed = 0;
 	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	long vx = v.getMouse().vx;
	long vy = v.getMouse().vy;
	if (wheelDirection  == WHEEL_UP){
	    // zooming in
	    if (application.demoCamera.getAltitude() > 0){
		// prevent translation if no altitude change
		application.demoCamera.posx += Math.round((vx - application.demoCamera.posx) * WHEEL_ZOOMIN_FACTOR / application.demoCamera.focal);
		application.demoCamera.posy += Math.round((vy - application.demoCamera.posy) * WHEEL_ZOOMIN_FACTOR / application.demoCamera.focal);
		application.demoCamera.updatePrecisePosition();
	    }
	    application.demoCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN, zooming out
	    application.demoCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
	    application.demoCamera.posx -= Math.round((vx - application.demoCamera.posx) * WHEEL_ZOOMOUT_FACTOR / application.demoCamera.focal);
	    application.demoCamera.posy -= Math.round((vy - application.demoCamera.posy) * WHEEL_ZOOMOUT_FACTOR / application.demoCamera.focal);
	    application.demoCamera.updatePrecisePosition();
	    application.vsm.repaintNow();
	}
    }

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_L){
	    application.loadFile();
	}
	else if (code == KeyEvent.VK_RIGHT){
	    application.showNextVisitSequence();
	}
	else if (code == KeyEvent.VK_LEFT){
	    application.showPreviousVisitSequence();
	}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

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

}
