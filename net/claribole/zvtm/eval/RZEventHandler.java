/*   FILE: RZEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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

class RZEventHandler extends WorldTaskEventHandler {

    long[] rz_wnes = new long[4];
    boolean cameraStickedToMouse = false;

    RZEventHandler(ZLWorldTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
// 	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
// 	v.setDrawDrag(true);
	cameraStickedToMouse = true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
// 	application.vsm.animator.Xspeed = 0;
// 	application.vsm.animator.Yspeed = 0;
// 	application.vsm.animator.Aspeed = 0;
// 	application.vsm.activeView.mouse.setSensitivity(true);
// 	v.setDrawDrag(false);
	cameraStickedToMouse = false;
	//XXX: why ?
	application.ewmm.updateVisibleMaps(application.demoView.getVisibleRegion(application.demoCamera), false, (short)0);
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
	application.vsm.activeView.mouse.setSensitivity(false);
	rz_wnes[0] = v.getMouse().vx;
	rz_wnes[1] = v.getMouse().vy;
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawRect(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
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
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
	Float alt = new Float(application.demoCamera.getAltitude()+application.demoCamera.getFocal());
	application.vsm.animator.createCameraAnimation(200,AnimManager.CA_ALT_SIG,alt,application.demoCamera.getID());
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
 	if (buttonNumber == 1){
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
// 	    application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
// 	    application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
// 	    application.vsm.animator.Aspeed = 0;
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
	else if (code==KeyEvent.VK_J){application.ewmm.switchAdaptMaps();}
	else if (code==KeyEvent.VK_K){
	    application.SHOW_MEMORY_USAGE = !application.SHOW_MEMORY_USAGE;
	    application.vsm.repaintNow();
	}
	else if (code==KeyEvent.VK_G){application.gc();}
    }

}
