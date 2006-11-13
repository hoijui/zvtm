/*   FILE: OVEventHandler.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id:  $
 */ 

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

class OVEventHandler extends BaseEventHandler implements PortalEventHandler {

    boolean mouseInsideOverview = false;

    OVEventHandler(Eval app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (mouseInsideOverview){
	    oCameraStickedToMouse = true;
	}
	else{
	    mCameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	mCameraStickedToMouse = false;
	oCameraStickedToMouse = false;
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
	if (buttonNumber == 1){
	    if (mCameraStickedToMouse){
		synchronized(application.mCamera){
		    float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude))/application.mCamera.focal;
		    application.mCamera.move(Math.round(a*(lastJPX-jpx)),
					     Math.round(a*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
	    }
	    else if (oCameraStickedToMouse){
		synchronized(application.oCamera){
		    float a = (application.oCamera.focal+Math.abs(application.oCamera.altitude))/application.oCamera.focal;
		    application.oCamera.move(Math.round(a*(lastJPX-jpx)),
					     Math.round(a*(jpy-lastJPY)));
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
	((CameraPortal)p).setBorder(Eval.INSIDE_PORTAL_BORDER_COLOR);
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	mouseInsideOverview = false;
	((CameraPortal)p).setBorder(Eval.DEFAULT_PORTAL_BORDER_COLOR);
	application.vsm.repaintNow();
    }
    
}