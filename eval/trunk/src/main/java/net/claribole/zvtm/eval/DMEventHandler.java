/*   FILE: WorldTaskEventHandler.java
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

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;


class DMEventHandler extends WorldTaskEventHandler implements PortalEventHandler {

    boolean dcamStickedToMouse = false;
    boolean pcamStickedToMouse = false;
    boolean portalStickedToMouse = false;
    boolean dmRegionStickedToMouse = false;
    boolean inDMZoomWindow = false;
    boolean cursorHasNotMovedYet = false;

    boolean inOvPortal = false;
    boolean ovCameraStickedToMouse = false;
    boolean regionStickedToMouse = false;

    boolean draggingMainView = false;

    DMEventHandler(ZLWorldTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	if (mode == MODE_DM){
	    if (inDMZoomWindow){
		if ((application.dmPortal).coordInsideBar(jpx, jpy)){
		    portalStickedToMouse = true;
		}
		else {
		    pcamStickedToMouse = true;
		}
	    }
	    else if (inOvPortal){
		if (application.ovPortal.coordInsideObservedRegion(jpx, jpy)){
		    regionStickedToMouse = true;
		}
		else {
		    ovCameraStickedToMouse = true;
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
	    if (inOvPortal){
		if (application.ovPortal.coordInsideObservedRegion(jpx, jpy)){
		    regionStickedToMouse = true;
		}
		else {
		    ovCameraStickedToMouse = true;
		}
	    }
	    else {
		v.setDrawDrag(true);
		draggingMainView = true;
	    }
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (mode == MODE_DM){
	    portalStickedToMouse = false;
	    dcamStickedToMouse = false;
	    pcamStickedToMouse = false;
	    application.vsm.unstickFromMouse();
	    dmRegionStickedToMouse = false;
	}
	v.setDrawDrag(false);
	if (application.ovPortal != null){
	    if (regionStickedToMouse){
		regionStickedToMouse = false;
	    }
	    if (ovCameraStickedToMouse){
		ovCameraStickedToMouse = false;
	    }
	    if (draggingMainView){
		application.centerOverview();
	    }
	}
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	draggingMainView = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (mode == MODE_PZL){
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
	else if (mode == MODE_DM){
	    if (inDMZoomWindow || inDMRegion(v.getGlyphsUnderMouseList())){
		application.meetDM();
		dmRegionStickedToMouse = false;
	    }
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
	if (mode == MODE_DM){
	    cursorHasNotMovedYet = true;
	    application.triggerDM(jpx, jpy);
	}
	else if (mode == MODE_PZL){
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
	    application.moveLens(jpx, jpy, true, e.getWhen());
	}
	//application.vsm.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (mode == MODE_DM){
	    if (inDMZoomWindow || portalStickedToMouse){
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
		    else if (regionStickedToMouse){
			float a = (application.overviewCamera.focal+Math.abs(application.overviewCamera.altitude))/application.overviewCamera.focal;
			application.demoCamera.move(Math.round(a*(jpx-lastJPX)),
						    Math.round(a*(lastJPY-jpy)));
			lastJPX = jpx;
			lastJPY = jpy;
		    }
		    else if (ovCameraStickedToMouse){
			float a = (application.overviewCamera.focal+Math.abs(application.overviewCamera.altitude))/application.overviewCamera.focal;
			application.overviewCamera.move(Math.round(a*(lastJPX-jpx)),
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
	else {
	    if (regionStickedToMouse){
		float a = (application.overviewCamera.focal+Math.abs(application.overviewCamera.altitude))/application.overviewCamera.focal;
		application.demoCamera.move(Math.round(a*(jpx-lastJPX)),
					    Math.round(a*(lastJPY-jpy)));
		lastJPX = jpx;
		lastJPY = jpy;
	    }
	    else if (ovCameraStickedToMouse){
		float a = (application.overviewCamera.focal+Math.abs(application.overviewCamera.altitude))/application.overviewCamera.focal;
		application.overviewCamera.move(Math.round(a*(lastJPX-jpx)),
						Math.round(a*(jpy-lastJPY)));
		application.demoCamera.move(Math.round(a*(lastJPX-jpx)),
					    Math.round(a*(jpy-lastJPY)));
		lastJPX = jpx;
		lastJPY = jpy;
		cameraMoved();
	    }
	    else {
		if (buttonNumber != 2){
		    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
		    application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
		    application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
		    application.vsm.animator.Aspeed = 0;
		}
		if (mode == MODE_PZL && lensType != 0 && application.lens != null){
		    application.moveLens(jpx, jpy, false, e.getWhen());
		}
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (mode == MODE_PZL){
	    if (lensType != 0 && application.lens != null){
		if (wheelDirection  == WHEEL_UP){// increasing lens focus mag factor
		    application.magnifyFocus(ZLWorldTask.WHEEL_MM_STEP, lensType, application.demoCamera);
		}
		else {// decreasing lens focus mag factor
		    application.magnifyFocus(-ZLWorldTask.WHEEL_MM_STEP, lensType, application.demoCamera);
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
	else if (mode == MODE_DM){
	    if (inDMZoomWindow){
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
	else if (code == KeyEvent.VK_F1){switchMode(MODE_PZ);}
	else if (code == KeyEvent.VK_F2){switchMode(MODE_PZL);}
	else if (code == KeyEvent.VK_F3){switchMode(MODE_DM);}
	else if (code == KeyEvent.VK_F4){application.createOverview();}
	else if (code==KeyEvent.VK_SPACE){application.logm.nextStep();}
	else if (code==KeyEvent.VK_PLUS){application.showGridLevel(application.currentLevel+1);}
	else if (code==KeyEvent.VK_MINUS){application.showGridLevel(application.currentLevel-1);}
	else if (code==KeyEvent.VK_J){application.ewmm.switchAdaptMaps();}
	else if (code==KeyEvent.VK_K){
	    ZLWorldTask.SHOW_MEMORY_USAGE = !ZLWorldTask.SHOW_MEMORY_USAGE;
	    application.vsm.repaintNow();
	}
	else if (code==KeyEvent.VK_G){application.gc();}
    }

    void switchMode(short m){
	mode = m;
    }

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	inDMZoomWindow = true;
	((CameraPortal)p).setBorder(Color.WHITE);
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	inDMZoomWindow = false;
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
