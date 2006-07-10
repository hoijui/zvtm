/*   FILE: PWEventHandler.java
 *   DATE OF CREATION:  Sat Jun 17 13:58:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;

import net.claribole.zvtm.engine.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;

class PWEventHandler implements ViewEventHandler, PortalEventHandler {

    PortalWorldDemo application;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    int prevJPX,prevJPY;

    boolean inPortal = false;
    boolean regionStickedToMouse = false;

    PWEventHandler(PortalWorldDemo appli){
	application = appli;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (inPortal && application.portal.coordInsideObservedRegion(jpx, jpy)){
	    regionStickedToMouse = true;
	}
	else {
	    application.vsm.activeView.mouse.setSensitivity(false);
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
 	application.vsm.activeView.mouse.setSensitivity(true);
	regionStickedToMouse = false;
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

    Camera handledCamera;

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
	    }
	    else {
		handledCamera = application.demoCamera;
		float a = (handledCamera.focal+Math.abs(handledCamera.altitude)) / handledCamera.focal;
		handledCamera.move(Math.round(a*(lastJPX-jpx)),
				   Math.round(a*(jpy-lastJPY)));
	    }
	    lastJPX = jpx;
	    lastJPY = jpy;
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	handledCamera = (inPortal) ? application.portalCamera : application.demoCamera;
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

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){

    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_PAGE_UP){application.getHigherView(mod == CTRL_MOD);}
	else if (code == KeyEvent.VK_PAGE_DOWN){application.getLowerView(mod == CTRL_MOD);}
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
	inPortal = true;
	stickPortal();
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	inPortal = false;
	unstickPortal();
	application.vsm.repaintNow();
    }

    static final int PORTAL_EXPANSION_TIME = 200;

    void stickPortal(){
	application.portal.setNoUpdateWhenMouseStill(true);
	application.portal.resize(PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET, PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET);
	application.portal.move(-PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET/2, -PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET/2);
	application.portal.setTransparencyValue(1.0f);
    }

    void unstickPortal(){
	application.portal.setNoUpdateWhenMouseStill(false);
	application.portal.resize(-PortalWorldDemo.PORTAL_WIDTH_EXPANSION_OFFSET, -PortalWorldDemo.PORTAL_HEIGHT_EXPANSION_OFFSET);
    }

}
