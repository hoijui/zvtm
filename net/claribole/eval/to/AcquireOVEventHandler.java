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
import java.awt.event.MouseEvent;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

class AcquireOVEventHandler extends AcquireBaseEventHandler implements PortalEventHandler {

    boolean mouseInsideOverview = false;
    boolean delayedOverviewExit = false;

    AcquireOVEventHandler(AcquireEval app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (!application.alm.trialStarted){
	    if (application.alm.sessionStarted && AcquireInstructionsManager.clickOnStartButton(jpx, jpy)){
		application.alm.startTrial();
		return;
	    }
	    else {
		return;
	    }
	}
	if (mouseInsideOverview){
	    if (application.op.coordInsideObservedRegion(jpx, jpy)){
		orStickedToMouse = true;
	    }
	    // do not allow grabbing the overview's content, it creates irrelevant noise in the experiment
// 	    else {
// 		oCameraStickedToMouse = true;
// 	    }
	}
	else if (v.lastGlyphEntered() == application.target){// user is clicking on target
	    application.alm.nextTarget();
	}
	else {
	    mCameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (!application.alm.trialStarted){return;}
	if (delayedOverviewExit){
	    portalExitActions();
	    delayedOverviewExit = false;
	}
	if (mCameraStickedToMouse){
	    application.centerOverview(true);
	    mCameraStickedToMouse = false;
	}
// 	oCameraStickedToMouse = false;
	orStickedToMouse = false;
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
	if (!application.alm.trialStarted){return;}
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
// 	    else if (oCameraStickedToMouse){
// 		synchronized(application.mCamera){
// 		synchronized(application.oCamera){
// 		    projCoef = (application.oCamera.focal+Math.abs(application.oCamera.altitude))/application.oCamera.focal;
// 		    application.oCamera.move(Math.round(projCoef*(lastJPX-jpx)),
// 					     Math.round(projCoef*(jpy-lastJPY)));
// 		    application.mCamera.move(Math.round(projCoef*(lastJPX-jpx)),
// 					     Math.round(projCoef*(jpy-lastJPY)));
// 		    lastJPX = jpx;
// 		    lastJPY = jpy;
// 		    cameraMoved();
// 		}
// 		}
// 	    }
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
    
    /**cursor enters portal*/
    public void enterPortal(Portal p){
	if (!application.alm.trialStarted){return;}
	mouseInsideOverview = true;
	if (delayedOverviewExit){
	    delayedOverviewExit = false;
	    return;
	}
	((CameraPortal)p).setBorder(Eval.INSIDE_PORTAL_BORDER_COLOR);
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	if (!application.alm.trialStarted){return;}
	if (orStickedToMouse){
	    delayedOverviewExit = true;
	}
	else {
	    portalExitActions();
	}
    }

    void portalExitActions(){
	mouseInsideOverview = false;
	delayedOverviewExit = false;
	application.centerOverview(true);
	application.op.setBorder(Eval.DEFAULT_PORTAL_BORDER_COLOR);
	application.vsm.repaintNow();
    }
    
}