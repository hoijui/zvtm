/*   FILE: TOWApplication.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import java.awt.event.MouseEvent;

import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.PortalEventHandler;

import com.xerox.VTM.engine.ViewPanel;

class AcquireOVEventHandler extends AcquireBaseEventHandler implements PortalEventHandler {

    boolean delayedOverviewExit = false;

    AcquireOVEventHandler(AcquireEval app){
	this.application = app;
	cursor = application.mView.mouse;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (!application.alm.trialStarted){
	    if (application.alm.sessionStarted && application.alm.im.clickOnStartButton(jpx, jpy)){
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
		application.alm.acquiredObservedRegion(System.currentTimeMillis());
	    }
	    // do not allow grabbing the overview's content, it creates irrelevant noise in the experiment
// 	    else {
// 		oCameraStickedToMouse = true;
// 	    }
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
// 	    application.centerOverview(true);
	    mCameraStickedToMouse = false;
	}
// 	oCameraStickedToMouse = false;
	orStickedToMouse = false;
    }

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
	if (application.alm.trialStarted){
	    application.alm.writeCinematic(jpx, jpy, application.op.x, application.op.y);
	}
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
	    else if (orStickedToMouse && mouseActuallyInsideOverview){
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
	mouseActuallyInsideOverview = true;
	if (delayedOverviewExit){
	    delayedOverviewExit = false;
	    return;
	}
// 	((CameraPortal)p).setBorder(Eval.INSIDE_PORTAL_BORDER_COLOR);
	application.alm.acquiredOverview(System.currentTimeMillis());
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	if (!application.alm.trialStarted){return;}
	mouseActuallyInsideOverview = false;
	if (orStickedToMouse){
	    delayedOverviewExit = true;
	}
	else {
	    portalExitActions();
	}
    }

    void portalExitActions(){
	application.alm.coarselyCentered(System.currentTimeMillis());
	mouseInsideOverview = false;
	delayedOverviewExit = false;
	application.vsm.repaintNow();
    }
    
}