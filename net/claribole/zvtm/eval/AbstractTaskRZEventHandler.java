/*   FILE: AbstractTaskRZEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskRZEventHandler.java,v 1.5 2006/06/06 09:00:33 epietrig Exp $
 */

package net.claribole.zvtm.eval;

import java.awt.Dimension;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import java.util.Hashtable;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class AbstractTaskRZEventHandler extends AbstractTaskEventHandler {

    long[] rz_wnes = new long[4];
    boolean cameraStickedToMouse = false;

    AbstractTaskRZEventHandler(ZLAbstractTask appli){
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

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	application.vsm.activeView.mouse.setSensitivity(false);
	rz_wnes[0] = v.getMouse().vx;
	rz_wnes[1] = v.getMouse().vy;
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawRect(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	rz_wnes[2] = v.getMouse().vx;
	rz_wnes[3] = v.getMouse().vy;
	if (rz_wnes[0] > rz_wnes[2]){
	    long tmpL = rz_wnes[0];
	    rz_wnes[0] = rz_wnes[2];
	    rz_wnes[2] = tmpL;
	}
	if (rz_wnes[3] > rz_wnes[1]){
	    long tmpL = rz_wnes[3];
	    rz_wnes[3] = rz_wnes[1];
	    rz_wnes[1] = tmpL;
	}
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawRect(false);
	// zoom in only if a region was drawn (not if user clicked)
 	if (Math.abs(lastJPX-jpx) > 6 &&  Math.abs(lastJPY-jpy) > 6){application.zoomInRegion(rz_wnes);}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	Float alt = new Float(application.demoCamera.getAltitude()+application.demoCamera.getFocal());
	application.vsm.animator.createCameraAnimation(200,AnimManager.CA_ALT_SIG,alt,application.demoCamera.getID());
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
 	if (buttonNumber == 1){
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
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.logm.startSession();}
	else if (code==KeyEvent.VK_SPACE){application.logm.nextStep();}
	else if (code==KeyEvent.VK_PLUS){application.showGridLevel(application.currentLevel+1);}
	else if (code==KeyEvent.VK_MINUS){application.showGridLevel(application.currentLevel-1);}
	else if (code==KeyEvent.VK_G){application.gc();}
    }

}
