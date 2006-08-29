/*   FILE: AbstractTaskDMEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskDMEventHandler.java,v 1.13 2006/06/06 09:00:33 epietrig Exp $
 */

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Hashtable;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

class AbstractTaskDMEventHandler extends AbstractTaskEventHandler implements PortalEventHandler, AnimationListener {

    boolean dcamStickedToMouse = false;
    boolean pcamStickedToMouse = false;
    boolean portalStickedToMouse = false;
    boolean dmRegionStickedToMouse = false;
    boolean inPortal = false;
    boolean cursorHasNotMovedYet = false;

    AbstractTaskDMEventHandler(ZLAbstractTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
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

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	portalStickedToMouse = false;
	dcamStickedToMouse = false;
	pcamStickedToMouse = false;
	application.vsm.unstickFromMouse();
	dmRegionStickedToMouse = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	if (inPortal || inDMRegion(v.getGlyphsUnderMouseList())){
	    application.meetDM();
	    dmRegionStickedToMouse = false;
	}
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	cursorHasNotMovedYet = true;
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
// 	application.triggerDM(jpx+ZLAbstractTask.DM_PORTAL_INITIAL_X_OFFSET, jpy+ZLAbstractTask.DM_PORTAL_INITIAL_Y_OFFSET);
	application.triggerDM(jpx, jpy);
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	cursorHasNotMovedYet = false;
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!application.logm.trialStarted){return;}
	if (inPortal || portalStickedToMouse){
	    if (buttonNumber == 1){
		if (portalStickedToMouse){
		    application.dmPortal.move(jpx-lastJPX, jpy-lastJPY);
		    lastJPX = jpx;
		    lastJPY = jpy;
		    dragMagMoved();
		    application.vsm.repaintNow();
		}
		else if (pcamStickedToMouse){
		    float a = (application.portalCamera.focal+Math.abs(application.portalCamera.altitude))/application.portalCamera.focal;
		    synchronized(application.portalCamera){
			application.portalCamera.move(Math.round(a*(lastJPX-jpx)),
						      Math.round(a*(jpy-lastJPY)));
			lastJPX = jpx;
			lastJPY = jpy;
			dragMagMoved();
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

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (!application.logm.trialStarted){return;}
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

    public void enterGlyph(Glyph g){// only highlight drag mag region glyph
	if (g != application.dmRegion){return;}
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
    }

    public void exitGlyph(Glyph g){// only highlight drag mag region glyph
	if (g != application.dmRegion){return;}
	if (g.mouseInsideFColor != null){g.color = g.fColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
    }

    long[] dragmagBoundaries = new long[4];

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.logm.startSession();}
	else if (code==KeyEvent.VK_SPACE){
	    application.logm.nextStep(v.getMouse().vx, v.getMouse().vy,
				      (application.dmPortal != null) ?
				      application.dmPortal.getVisibleRegion(dragmagBoundaries):
				      application.demoView.getVisibleRegion(application.demoCamera, dragmagBoundaries));}
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
