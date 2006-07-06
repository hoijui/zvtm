/*   FILE: AbstractTaskPZEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskPZEventHandler.java,v 1.13 2006/06/06 09:00:33 epietrig Exp $
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

class AbstractTaskPZEventHandler extends AbstractTaskEventHandler implements PortalEventHandler {

    boolean cameraStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

    AbstractTaskPZEventHandler(ZLAbstractTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
	if (inPortal){
	    regionStickedToMouse = true;
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
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
	if (inPortal){
	    regionStickedToMouse = true;
	}
	else {
	    cameraStickedToMouse = true;
	}
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	if (cameraStickedToMouse){
	    cameraStickedToMouse = false;
	    application.centerOverview();
	}
	if (regionStickedToMouse){
	    regionStickedToMouse = false;
	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
 	if (buttonNumber != 2){
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

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.logm.startSession();}
	else if (code==KeyEvent.VK_SPACE){application.logm.nextStep(v.getMouse().vx, v.getMouse().vy);}
	else if (code==KeyEvent.VK_G){application.gc();}
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
