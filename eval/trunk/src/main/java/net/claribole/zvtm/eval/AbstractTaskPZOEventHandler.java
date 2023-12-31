/*   FILE: AbstractTaskPZOEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import net.claribole.zvtm.engine.CameraPortal;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.PortalEventHandler;

import com.xerox.VTM.engine.ViewPanel;

class AbstractTaskPZOEventHandler extends AbstractTaskEventHandler implements PortalEventHandler {

    boolean cameraStickedToMouse = false;
    boolean pcameraStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

    AbstractTaskPZOEventHandler(ZLAbstractTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
	if (inPortal){
	    if (application.ovPortal.coordInsideObservedRegion(jpx, jpy)){
		regionStickedToMouse = true;
	    }
	    else {
		pcameraStickedToMouse = true;
	    }
	}
	else {
	    cameraStickedToMouse = true;
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	if (cameraStickedToMouse){
	    cameraStickedToMouse = false;
	    application.centerOverview();
	}
	if (regionStickedToMouse){
	    regionStickedToMouse = false;
	}
	if (pcameraStickedToMouse){
	    pcameraStickedToMouse = false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (application.logm.trialStarted){
	    lastJPX = jpx;
	    lastJPY = jpy;
	}
	else {// subject is in between two trials (provided session started)
	    application.logm.start(jpx, jpy);
	}
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
 	if (buttonNumber == 1){
	    synchronized(application.demoCamera){
		if (cameraStickedToMouse){
		    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
		    application.demoCamera.move(Math.round(a*(lastJPX-jpx)),
						Math.round(a*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
		else if (regionStickedToMouse){
		    float a = (application.portalCamera.focal+Math.abs(application.portalCamera.altitude))/application.portalCamera.focal;
		    application.demoCamera.move(Math.round(a*(jpx-lastJPX)),
						Math.round(a*(lastJPY-jpy)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		}
		else if (pcameraStickedToMouse){
		    float a = (application.portalCamera.focal+Math.abs(application.portalCamera.altitude))/application.portalCamera.focal;
		    application.portalCamera.move(Math.round(a*(lastJPX-jpx)),
						  Math.round(a*(jpy-lastJPY)));
		    application.demoCamera.move(Math.round(a*(lastJPX-jpx)),
						Math.round(a*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
	    }
 	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (!application.logm.trialStarted){return;}
	float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	if (wheelDirection  == WHEEL_UP){// zooming in
	    application.demoCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
	    cameraMoved();
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN, zooming out
	    application.demoCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
	    cameraMoved();
	    application.vsm.repaintNow();
	}
    }

    long[] viewportBoundaries = new long[4];

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.logm.startSession();}
	else if (code==KeyEvent.VK_SPACE){application.logm.unveil(application.demoView.getVisibleRegion(application.demoCamera,
													viewportBoundaries));}
	else if (code==KeyEvent.VK_F1){application.logm.validateTarget(application.demoView.getVisibleRegion(application.demoCamera,
														viewportBoundaries));}
    }

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	inPortal = true;
	((CameraPortal)p).setBorder(Color.WHITE);
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	inPortal = false;
	((CameraPortal)p).setBorder(Color.RED);
	application.vsm.repaintNow();
    }

}
