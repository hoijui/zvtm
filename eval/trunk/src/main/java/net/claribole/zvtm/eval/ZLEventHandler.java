/*   FILE: ZLEventHandler.java
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

class ZLEventHandler extends WorldTaskEventHandler {

    boolean cameraStickedToMouse = false;

    ZLEventHandler(ZLWorldTask appli){
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
	application.ewmm.updateVisibleMaps(application.demoView.getVisibleRegion(application.demoCamera), false, (short)0);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
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

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
// 	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
// 	v.setDrawDrag(true);
	cameraStickedToMouse = true;
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
// 	application.vsm.animator.Xspeed = 0;
// 	application.vsm.animator.Yspeed = 0;
// 	application.vsm.animator.Aspeed = 0;
// 	application.vsm.activeView.mouse.setSensitivity(true);
// 	v.setDrawDrag(false);
	cameraStickedToMouse = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
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
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
	if ((jpx-ZLWorldTask.LENS_R1) < 0){
	    jpx = ZLWorldTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+ZLWorldTask.LENS_R1) > application.panelWidth){
	    jpx = application.panelWidth - ZLWorldTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-ZLWorldTask.LENS_R1) < 0){
	    jpy = ZLWorldTask.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+ZLWorldTask.LENS_R1) > application.panelHeight){
	    jpy = application.panelHeight - ZLWorldTask.LENS_R1;
	    cursorNearBorder = true;
	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, true, e.getWhen());
	}
	//application.vsm.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
 	if (buttonNumber != 2){
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
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, false, e.getWhen());
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (!application.logm.trialStarted && !application.gds.trainingData){return;}
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == WHEEL_UP){
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	    else {
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.demoCamera);
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
