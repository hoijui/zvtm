/*   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.*;


class BehaviorEventHandler implements ViewEventHandler, PortalEventHandler, ComponentListener {

    static final int PORTAL_EXPANSION_TIME = 200;

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    BehaviorEval application;

    boolean mouseInsideOverview = false;
    boolean mouseActuallyInsideOverview = false;

    VCursor cursor;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;
    int currentJPX, currentJPY;

    float projCoef, alt, oldCameraAltitude; // for efficiency

    /* main camera is being dragged */
    boolean mCameraStickedToMouse = false;
    /* overview camera is being dragged */
    boolean oCameraStickedToMouse = false;
    /* observed region in overview is being dragged */
    boolean orStickedToMouse = false;

    boolean delayedTOWExit = false;

    BehaviorEventHandler(BehaviorEval app){
	this.application = app;
	cursor = application.mView.mouse;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (!application.blm.trialStarted){
	    if (application.blm.sessionStarted && application.blm.im.clickOnStartButton(jpx, jpy)){
		application.blm.startTrial();
		return;
	    }
	    else {
		return;
	    }
	}
	if (mouseInsideOverview){
	    if (application.to.coordInsideObservedRegion(jpx, jpy)){
		orStickedToMouse = true;
	    }
	}
	else {
	    mCameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (!application.blm.trialStarted){return;}
	if (delayedTOWExit){
	    portalExitActions();
	    delayedTOWExit = false;
	}
	if (mCameraStickedToMouse){
	    mCameraStickedToMouse = false;
	}
	orStickedToMouse = false;
    }

    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
	if (!mouseInsideOverview && application.to != null){
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(jpx, jpy);
	}
	currentJPX = jpx;
	currentJPY = jpy;
	if (application.blm.trialStarted){
	    application.blm.writeCinematic(jpx, jpy, application.to.x, application.to.y);
	}
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
	if (!application.blm.trialStarted){return;}
	if (!mouseInsideOverview && application.to != null){
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(jpx, jpy);
	}
	currentJPX = jpx;
	currentJPY = jpy;
	if (buttonNumber == 1){
	    if (mCameraStickedToMouse){
		synchronized(application.mCamera){
		    projCoef = (application.mCamera.focal+Math.abs(application.mCamera.altitude))/application.mCamera.focal;
		    application.mCamera.move(Math.round(projCoef*(lastJPX-jpx)),
					     Math.round(projCoef*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		}
	    }
	    else if (orStickedToMouse && mouseActuallyInsideOverview){
		synchronized(application.oCamera){
		    projCoef = (application.oCamera.focal+Math.abs(application.oCamera.altitude))/application.oCamera.focal;
		    application.mCamera.move(Math.round(projCoef*(jpx-lastJPX)),
					     Math.round(projCoef*(lastJPY-jpy)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		}
	    }
 	}
	application.blm.writeCinematic(jpx, jpy, application.to.x, application.to.y);
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){
	if (!application.blm.trialStarted){return;}
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
    }

    public void exitGlyph(Glyph g){
	if (!application.blm.trialStarted){return;}
	if (g.isSelected()){
	    g.borderColor = (g.selectedColor != null) ? g.selectedColor : g.bColor;
	}
	else {
	    if (g.mouseInsideFColor != null){g.color = g.fColor;}
	    if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
	}
    }

    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (code == KeyEvent.VK_F1){
	    application.switchPortal(currentJPX, currentJPY);
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(currentJPX, currentJPY);
	}
    }
           
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (code == KeyEvent.VK_S){application.blm.startSession();}
	else if (mod == CTRL_MOD && code == KeyEvent.VK_Q){application.exit();}
	else if (code == KeyEvent.VK_F12){application.blm.nextTarget();}
	else if (code == KeyEvent.VK_V){application.blm.im.toggleIndications();}
    }
           
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}
    
    public void viewActivated(View v){}
    
    public void viewClosing(View v){}
    
    public void viewDeactivated(View v){}
    
    public void viewDeiconified(View v){}
    
    public void viewIconified(View v){}

    /* ComponentListener */
    public void componentHidden(ComponentEvent e){}

    public void componentMoved(ComponentEvent e){}

    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }

    public void componentShown(ComponentEvent e){}

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	if (!application.blm.trialStarted){return;}
	if (mCameraStickedToMouse){// do not exec actions associated with entering portal when the user is
	    return;                // panning the main viewport (most likely entered portal by accident)
	}
	mouseInsideOverview = true;
	mouseActuallyInsideOverview = true;
	if (delayedTOWExit){
	    delayedTOWExit = false;
	    return;
	}
 	stickPortal();
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	if (!application.blm.trialStarted){return;}
	mouseActuallyInsideOverview = false;
	if (!mouseInsideOverview){// do not exec exit actions if enter actions
	    return;          // were not executed at entry time
	}
	if (orStickedToMouse){
	    delayedTOWExit = true;
	}
	else {
	    portalExitActions();
	}
    }

    void portalExitActions(){
	mouseInsideOverview = false;
	delayedTOWExit = false;
 	unstickPortal();
	application.vsm.repaintNow();
    }

    void stickPortal(){
	application.blm.acquiredTOW(System.currentTimeMillis());
	application.to.setNoUpdateWhenMouseStill(true); // prevent tow from moving
	application.to.resize(Eval.TOW_HORIZONTAL_EXPANSION_OFFSET, Eval.TOW_VERTICAL_EXPANSION_OFFSET);
	application.to.move(-Eval.TOW_HORIZONTAL_EXPANSION_OFFSET/2, -Eval.TOW_VERTICAL_EXPANSION_OFFSET/2);
	application.to.setTransparencyValue(1.0f);
    }

    void unstickPortal(){
	application.to.setNoUpdateWhenMouseStill(false);
	application.to.resize(-Eval.TOW_HORIZONTAL_EXPANSION_OFFSET, -Eval.TOW_VERTICAL_EXPANSION_OFFSET);
    }
   
}