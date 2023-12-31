/*   FILE: AbstractTaskPZLEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import com.xerox.VTM.engine.ViewPanel;

class AbstractTaskPZLEventHandler extends AbstractTaskEventHandler {

    boolean cameraStickedToMouse = false;

    AbstractTaskPZLEventHandler(ZLAbstractTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
	cameraStickedToMouse = true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	cameraStickedToMouse = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (application.logm.trialStarted){
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
	else {// subject is in between two trials (provided session started)
	    application.logm.start(jpx, jpy);
	}
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
	cameraStickedToMouse = true;
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	cameraStickedToMouse = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
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
	if (!application.logm.trialStarted){return;}
	if ((jpx-ZLAbstractTask.LENS_R1) < 0){
	    jpx = ZLAbstractTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+ZLAbstractTask.LENS_R1) > application.panelWidth){
	    jpx = application.panelWidth - ZLAbstractTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-ZLAbstractTask.LENS_R1) < 0){
	    jpy = ZLAbstractTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+ZLAbstractTask.LENS_R1) > application.panelHeight){
	    jpy = application.panelHeight - ZLAbstractTask.LENS_R1;
	    cursorNearBorder = true;
	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, true);
	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
 	if (buttonNumber != 2){
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	    synchronized(application.demoCamera){
		if (cameraStickedToMouse){
		    application.demoCamera.move(Math.round(a*(lastJPX-jpx)),
						Math.round(a*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    cameraMoved();
		}
	    }
 	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, false);
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (!application.logm.trialStarted){return;}
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == WHEEL_UP){
		application.magnifyFocus(ZLAbstractTask.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	    else {
		application.magnifyFocus(-ZLAbstractTask.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	}
	else {
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
    }

    long[] lensRegionBoundaries = new long[4];

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.logm.startSession();}
	else if (code==KeyEvent.VK_SPACE){application.logm.unveil((application.lens != null) ?
								  application.lens.getVisibleRegion(application.demoCamera, lensRegionBoundaries) :
								  application.demoView.getVisibleRegion(application.demoCamera, lensRegionBoundaries));}
	else if (code==KeyEvent.VK_F1){application.logm.validateTarget((application.lens != null) ?
									  application.lens.getVisibleRegion(application.demoCamera, lensRegionBoundaries) :
									  application.demoView.getVisibleRegion(application.demoCamera, lensRegionBoundaries));}
    }

}
