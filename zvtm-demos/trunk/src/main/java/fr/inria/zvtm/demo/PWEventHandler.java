/*   FILE: PWEventHandler.java
 *   DATE OF CREATION:  Sat Jun 17 13:58:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.demo;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.engine.ObservedRegionListener;
import fr.inria.zvtm.engine.Portal;
import fr.inria.zvtm.engine.PortalEventHandler;
import fr.inria.zvtm.engine.ViewEventHandler;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

class PWEventHandler implements ViewEventHandler, PortalEventHandler, CameraListener, ObservedRegionListener {

    PortalWorldDemo application;

    int lastJPX,lastJPY;
    int jpxD, jpyD;
    int lastpJPX,lastpJPY;

    int prevJPX,prevJPY;

    boolean inPortal = false;
    boolean regionStickedToMouse = false;
    boolean delayedPortalExit = false;

    long[] rstc = new long[4]; //region seen through camera

    Camera handledCamera;
    static final int PORTAL_MARGIN = 20;
    int[] dfb = new int[4];

    float zfactor = 40.0f;
    float pfactor = 15.0f;

    PWEventHandler(PortalWorldDemo appli){
	application = appli;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastpJPX = jpx;
	lastpJPY = jpy;
	if (inPortal){
	    if (application.portal.coordInsideObservedRegion(jpx, jpy)){
		regionStickedToMouse = true;
	    }
	    else {
		application.getTo(jpx, jpy);
	    }
	}
	else {
	    v.setDrawDrag(true);
	    application.vsm.activeView.mouse.setSensitivity(false);
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	application.vsm.getAnimationManager().setXspeed(0);
	application.vsm.getAnimationManager().setYspeed(0);
	application.vsm.getAnimationManager().setZspeed(0);
 	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
	regionStickedToMouse = false;
	if (delayedPortalExit){portalExitActions();}
// 	application.portal.resetInsideBorders();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.switchPortal(jpx, jpy);
	application.portal.updateFrequency(e.getWhen());
	application.portal.updateWidgetLocation(jpx, jpy);
	prevJPX = jpx;
	prevJPY = jpy;
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	if (!inPortal && application.portal != null){
	    application.portal.updateFrequency(e.getWhen());
	    application.portal.updateWidgetLocation(jpx, jpy);
	}
	prevJPX = jpx;
	prevJPY = jpy;
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (!inPortal && application.portal != null){
	    application.portal.updateFrequency(e.getWhen());
	    application.portal.updateWidgetLocation(jpx, jpy);
	}
	prevJPX = jpx;
	prevJPY = jpy;
	if (buttonNumber == 1){
	    if (inPortal){
		if (regionStickedToMouse){
		    handledCamera = application.demoCamera;
		    float a = (application.portalCamera.focal+Math.abs(application.portalCamera.altitude)) / application.portalCamera.focal;
		    handledCamera.move(Math.round(a*(jpx-lastJPX)),
				       Math.round(a*(lastJPY-jpy)));
		}
// 		else {
// 		    application.portal.getDistanceFromBorders(jpx, jpy, dfb);
// 		    if (dfb[0] > 0 && dfb[0] < PORTAL_MARGIN){// inside west margin
// 			application.portal.insideWestBorder((PORTAL_MARGIN-dfb[0])/2);
// 			application.portal.insideEastBorder(0);
// 		    }
// 		    else if (dfb[2] > 0 && dfb[2] < PORTAL_MARGIN){// inside east margin 
// 			application.portal.insideWestBorder(0);
// 			application.portal.insideEastBorder((PORTAL_MARGIN-dfb[2])/2);
// 		    }
// 		    else {
// 			application.portal.insideWestBorder(0);
// 			application.portal.insideEastBorder(0);
// 		    }
// 		    if (dfb[1] > 0 && dfb[1] < PORTAL_MARGIN){// inside north margin
// 			application.portal.insideNorthBorder((PORTAL_MARGIN-dfb[1])/2);
// 			application.portal.insideSouthBorder(0);			
// 		    }
// 		    else if (dfb[3] > 0 && dfb[3] < PORTAL_MARGIN){// inside south margin
// 			application.portal.insideNorthBorder(0);
// 			application.portal.insideSouthBorder((PORTAL_MARGIN-dfb[3])/2);
// 		    }
// 		    else {
// 			application.portal.insideNorthBorder(0);
// 			application.portal.insideSouthBorder(0);
// 		    }
// 		}
		lastJPX = jpx;
		lastJPY = jpy;
	    }
	    else {
		handledCamera = application.demoCamera;
		float a = (handledCamera.focal+Math.abs(handledCamera.altitude)) / handledCamera.focal;
		if (mod == SHIFT_MOD){// zoom
		    application.vsm.getAnimationManager().setZspeed((handledCamera.altitude>0) ? (long)((lastpJPY-jpy)*(a/zfactor)) : (long)((lastpJPY-jpy)/(a*zfactor)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		}
		else {// pan
		    // 0-order control
// 		    handledCamera.move(Math.round(a*(lastJPX-jpx)),
// 				       Math.round(a*(jpy-lastJPY)));
// 		    lastJPX = jpx;
// 		    lastJPY = jpy;
		    // 1st-order control
		    jpxD = jpx-lastJPX;
		    jpyD = lastJPY-jpy;
		    application.vsm.getAnimationManager().setXspeed((handledCamera.altitude>0) ? (long)(jpxD*(a/pfactor)) : (long)(jpxD/(a*pfactor)));
		    application.vsm.getAnimationManager().setYspeed((handledCamera.altitude>0) ? (long)(jpyD*(a/pfactor)) : (long)(jpyD/(a*pfactor)));
		    application.vsm.getAnimationManager().setZspeed(0);
		}
	    }

	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	handledCamera = application.demoCamera;
	float a = (handledCamera.focal+Math.abs(handledCamera.altitude)) / handledCamera.focal;
	if (wheelDirection == WHEEL_UP){
	    handledCamera.altitudeOffset(-a*5);
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN
	    handledCamera.altitudeOffset(a*5);
	    application.vsm.repaintNow();
	}
    }

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_PAGE_UP){application.getHigherView(mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_PAGE_DOWN){application.getLowerView(mod == CTRL_MOD);}
	// next two are called through tablet touch strips
	else if (code == KeyEvent.VK_OPEN_BRACKET){application.getFastHigherView();}
	else if (code == KeyEvent.VK_CLOSE_BRACKET){application.getFastLowerView();}
	else if (code == KeyEvent.VK_A){application.mm.switchAdaptMaps();}
	else if (code == KeyEvent.VK_HOME){application.getGlobalView(mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_UP){application.translateView(PortalWorldDemo.MOVE_UP, mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_DOWN){application.translateView(PortalWorldDemo.MOVE_DOWN, mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_LEFT){application.translateView(PortalWorldDemo.MOVE_LEFT, mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_RIGHT){application.translateView(PortalWorldDemo.MOVE_RIGHT, mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_SPACE){
	    application.switchPortal(prevJPX, prevJPY);
	    application.portal.updateFrequency(e.getWhen());
	    application.portal.updateWidgetLocation(prevJPX, prevJPY);
	}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	if (delayedPortalExit){delayedPortalExit = false;return;}
	inPortal = true;
	stickPortal();
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	if (regionStickedToMouse){
	    delayedPortalExit = true;
	}
	else {
	    portalExitActions();
	}
    }

    void portalExitActions(){
	inPortal = false;
	delayedPortalExit = false;
	unstickPortal();
	application.vsm.repaintNow();
    }

    void updateOverviewAltitude(float alt){
	float palt = alt * 200;
	if (palt > PortalWorldDemo.PORTAL_CEILING_ALTITUDE){
	    application.portalCamera.setAltitude(PortalWorldDemo.PORTAL_CEILING_ALTITUDE);
	    if (application.portalCamera.posx != 0 || application.portalCamera.posy != 0){
		application.portalCamera.moveTo(0, 0);
	    }
	}
	else {
	    application.portalCamera.setAltitude(palt);
	}
    }

    void stickPortal(){
	application.portal.setNoUpdateWhenMouseStill(true);
	updateOverviewAltitude(application.demoCamera.getAltitude());
	application.portal.resize(PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET, PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET);
	application.portal.move(-PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET/2, -PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET/2);
	application.portal.setTransparencyValue(1.0f);
    }

    void unstickPortal(){
	application.portal.setNoUpdateWhenMouseStill(false);
	application.portalCamera.moveTo(0, 0);
	application.portalCamera.setAltitude(PortalWorldDemo.CONTRACTED_PORTAL_CEILING_ALTITUDE);
	application.portal.resize(-PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET, -PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET);
    }

    float oldDemoCameraAltitude = 0;

    public void cameraMoved(Camera cam, LongPoint coord, float newAlt){
	float alt = application.demoCamera.getAltitude();
	if (alt != oldDemoCameraAltitude){
	    float palt = alt * 200;
	    if (inPortal){// only update overview's altitude if inside portal
		updateOverviewAltitude(alt);
	    }
	    application.altitudeChanged();
	    oldDemoCameraAltitude = alt;
	}
	else {
	    application.demoView.getVisibleRegion(application.demoCamera, rstc);
	    application.mm.updateVisibleMaps(rstc, true, application.mm.getMapLevel(alt));
	}
	if (application.portal != null){
	    long[] wnes = application.portal.getVisibleRegion();
	    if (application.demoCamera.posx < wnes[0] ||
		application.demoCamera.posx > wnes[2] ||
		application.demoCamera.posy < wnes[3] ||
		application.demoCamera.posy > wnes[1]){
		application.portalCamera.moveTo(application.demoCamera.posx, application.demoCamera.posy);
	    }
	}
    }

    public void intersectsParentRegion(long[] wnes){
	if (regionStickedToMouse && application.portalCamera.getAltitude() < PortalWorldDemo.PORTAL_CEILING_ALTITUDE){
	    long disp = Math.round((application.portalCamera.altitude+application.portalCamera.focal)/application.portalCamera.focal);
	    if (wnes[0] < 0 && wnes[2] < 0){// intersection west border
		application.portalCamera.move(-disp, 0);
 		application.demoCamera.move(-disp, 0);
	    }
	    else if (wnes[0] > 0 && wnes[2] > 0){// intersection east border
		application.portalCamera.move(disp, 0);
		application.demoCamera.move(disp, 0);
	    }
	    if (wnes[1] > 0 && wnes[3] > 0){// intersection north border
		application.portalCamera.move(0, disp);
		application.demoCamera.move(0, disp);
	    }
	    else if (wnes[1] < 0 && wnes[3] < 0){// intersection south border
		application.portalCamera.move(0, -disp);
		application.demoCamera.move(0, -disp);
	    }
	}
    }

}
