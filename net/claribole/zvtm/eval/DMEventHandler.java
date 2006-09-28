/*   FILE: WorldTaskEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: DMEventHandler.java,v 1.13 2006/05/26 14:51:48 epietrig Exp $
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
import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.CameraPortal;


class DMEventHandler extends WorldTaskEventHandler implements PortalEventHandler {

    boolean dcamStickedToMouse = false;
    boolean pcamStickedToMouse = false;
    boolean portalStickedToMouse = false;
    boolean dmRegionStickedToMouse = false;
    boolean inPortal = false;
    boolean cursorHasNotMovedYet = false;

    DMEventHandler(ZLWorldTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	if (application.dmPortal != null){
	    if (inPortal){
		if ((application.dmPortal).coordInsideBar(jpx, jpy)){
		    portalStickedToMouse = true;
		}
		else {
		    pcamStickedToMouse = true;
		}
	    }
	    else {
		if (inDMRegion(v.getGlyphsUnderMouseList())){
		    dmRegionStickedToMouse = true;
		    application.vsm.stickToMouse(application.dmRegion);
		}
		else {
		    dcamStickedToMouse = true;
		}
	    }
	}
	else {
	    v.setDrawDrag(true);
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (application.dmPortal != null){
	    portalStickedToMouse = false;
	    dcamStickedToMouse = false;
	    pcamStickedToMouse = false;
	    application.vsm.unstickFromMouse();
	    dmRegionStickedToMouse = false;
	}
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
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
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (mod == SHIFT_MOD){
	    cursorHasNotMovedYet = true;
	    application.triggerDM(jpx, jpy);
	}
	else {
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
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	cursorHasNotMovedYet = false;
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
	    application.moveLens(jpx, jpy, true);
	}
	//application.vsm.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (application.dmPortal != null){
	    if (inPortal || portalStickedToMouse){
		if (buttonNumber == 1){
		    if (portalStickedToMouse){
			application.dmPortal.move(jpx-lastJPX, jpy-lastJPY);
			lastJPX = jpx;
			lastJPY = jpy;
			application.vsm.repaintNow();
		    }
		    else if (pcamStickedToMouse){
			float a = (application.portalCamera.focal+Math.abs(application.portalCamera.altitude))/application.portalCamera.focal;
			synchronized(application.portalCamera){
			    application.portalCamera.move(Math.round(a*(lastJPX-jpx)),
							  Math.round(a*(jpy-lastJPY)));
			    lastJPX = jpx;
			    lastJPY = jpy;
			    cameraMoved();
			}
		    }
		}
	    }
	    else {
		if (buttonNumber == 1){
		    if (dcamStickedToMouse){
			float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
			synchronized(application.demoCamera){
			    application.demoCamera.move(Math.round(a*(lastJPX-jpx)),
							Math.round(a*(jpy-lastJPY)));
			    lastJPX = jpx;
			    lastJPY = jpy;
			    cameraMoved();
			}
		    }
		    else if (dmRegionStickedToMouse){
			application.updateDMWindow();
		    }
		}
	    }
	}
	else {
	    if (buttonNumber != 2){
		float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
		application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
		application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
		application.vsm.animator.Aspeed = 0;
	    }
	    if (lensType != 0 && application.lens != null){
		application.moveLens(jpx, jpy, false);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == WHEEL_UP){// increasing lens focus mag factor
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	    else {// decreasing lens focus mag factor
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	}
	else if (application.dmPortal != null){
	    if (inPortal){
		Camera c = application.portalCamera;
		float a = (c.focal+Math.abs(c.altitude))/c.focal;
		if (wheelDirection  == WHEEL_UP){// zooming in
		    c.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
		}
		else {//wheelDirection == WHEEL_DOWN, zooming out
		    c.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
		}
	    }
	    else {
		Camera c1 = application.demoCamera;
		Camera c2 = application.portalCamera;
		float a1 = (c1.focal+Math.abs(c1.altitude))/c1.focal;
		float a2 = (c2.focal+Math.abs(c2.altitude))/c2.focal;
		if (wheelDirection  == WHEEL_UP){// zooming in
		    c1.altitudeOffset(-a1*WHEEL_ZOOMIN_FACTOR);
		    c2.altitudeOffset(-a2*WHEEL_ZOOMIN_FACTOR);
		}
		else {//wheelDirection == WHEEL_DOWN, zooming out
		    c1.altitudeOffset(a1*WHEEL_ZOOMOUT_FACTOR);
		    c2.altitudeOffset(a2*WHEEL_ZOOMOUT_FACTOR);
		}
	    }
	    cameraMoved();
	    application.vsm.repaintNow();
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

    boolean inDMRegion(Glyph[] guml){
	for (int i=0;i<guml.length;i++){
	    if (guml[i] == application.dmRegion){return true;}
	}
	return false;
    }

}
