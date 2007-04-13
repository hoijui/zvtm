/*   FILE: OVEventHandler.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id$
 */ 

package net.claribole.eval.to;

import java.awt.event.MouseEvent;

import net.claribole.zvtm.engine.CameraPortal;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.PortalEventHandler;

import com.xerox.VTM.engine.ViewPanel;

class OVEventHandler extends BaseEventHandler implements PortalEventHandler {

    boolean mouseInsideOverview = false;
    boolean delayedOverviewExit = false;

    OVEventHandler(Eval app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (mouseInsideOverview){
	    if (application.op.coordInsideObservedRegion(jpx, jpy)){
		orStickedToMouse = true;
	    }
	    else {
		oCameraStickedToMouse = true;
	    }
	}
	else{
	    mCameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (delayedOverviewExit){
	    portalExitActions();
	    delayedOverviewExit = false;
	}
	if (mCameraStickedToMouse){
	    application.centerOverview(true);
	    mCameraStickedToMouse = false;
	}
	oCameraStickedToMouse = false;
	orStickedToMouse = false;
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
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
    
    /**cursor enters portal*/
    public void enterPortal(Portal p){
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