/*   FILE: TOWApplication.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

class AcquireTOWEventHandler extends AcquireBaseEventHandler implements PortalEventHandler {

    static final int PORTAL_EXPANSION_TIME = 200;

    int currentJPX, currentJPY;

    boolean mouseInsideTOW = false;
    boolean delayedTOWExit = false;

    AcquireTOWEventHandler(AcquireEval app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (!application.alm.trialStarted){
	    if (application.alm.sessionStarted && AcquireInstructionsManager.clickOnStartButton(jpx, jpy)){
		application.alm.startTrial();
	    }
	    else {
		return;
	    }
	}
	lastJPX = jpx;
	lastJPY = jpy;
	if (mouseInsideTOW){
	    if (application.to.coordInsideObservedRegion(jpx, jpy)){
		orStickedToMouse = true;
	    }
	    else {
		oCameraStickedToMouse = true;
	    }
	}
	else {
	    mCameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (!application.alm.trialStarted){return;}
	if (delayedTOWExit){
	    portalExitActions();
	    delayedTOWExit = false;
	}
	if (mCameraStickedToMouse){
	    application.centerOverview(false);
	    mCameraStickedToMouse = false;
	}
	oCameraStickedToMouse = false;
	orStickedToMouse = false;
    }

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
	if (!mouseInsideTOW && application.to != null){
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(jpx, jpy);
	}
	currentJPX = jpx;
	currentJPY = jpy;
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
	if (!application.alm.trialStarted){return;}
	if (!mouseInsideTOW && application.to != null){
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
		    cameraMoved();
		}
	    }
	    else if (oCameraStickedToMouse){
		synchronized(application.mCamera){
		synchronized(application.oCamera){
		    projCoef = (application.oCamera.focal+Math.abs(application.oCamera.altitude))/application.oCamera.focal;
		    application.oCamera.move(Math.round(projCoef*(lastJPX-jpx)),
					     Math.round(projCoef*(jpy-lastJPY)));
		    application.mCamera.move(Math.round(projCoef*(lastJPX-jpx)),
					     Math.round(projCoef*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
		}
	    }
	    else if (orStickedToMouse){
		synchronized(application.oCamera){
		    projCoef = (application.oCamera.focal+Math.abs(application.oCamera.altitude))/application.oCamera.focal;
		    application.mCamera.move(Math.round(projCoef*(jpx-lastJPX)),
					     Math.round(projCoef*(lastJPY-jpy)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
	    }
 	}
    }

    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (code == KeyEvent.VK_SPACE){
	    application.switchPortal(currentJPX, currentJPY);
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(currentJPX, currentJPY);
	}
    }

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	if (!application.alm.trialStarted){return;}
	if (mCameraStickedToMouse){// do not exec actions associated with entering portal when the user is
	    return;                // panning the main viewport (most likely entered portal by accident)
	}
	if (delayedTOWExit){
	    delayedTOWExit = false;
	    return;
	}
	mouseInsideTOW = true;
 	stickPortal();
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	if (!application.alm.trialStarted){return;}
	if (!mouseInsideTOW){// do not exec exit actions if enter actions
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
	mouseInsideTOW = false;
	delayedTOWExit = false;
 	unstickPortal();
	application.centerOverview(false);
	application.vsm.repaintNow();
    }

    void stickPortal(){
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