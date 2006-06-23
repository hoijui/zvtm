/*   FILE: DMDEventHandler.java
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class DMDEventHandler implements ViewEventHandler, PortalEventHandler, AnimationListener {

    DragMagDemo application;

    boolean dcamStickedToMouse = false;
    boolean pcamStickedToMouse = false;
    boolean portalStickedToMouse = false;
    boolean dmRegionStickedToMouse = false;

    static final int PORTAL_INITIAL_X_OFFSET = 200;
    static final int PORTAL_INITIAL_Y_OFFSET = 200;
    boolean inPortal = false;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    DMDEventHandler(DragMagDemo appli){
	application = appli;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (inPortal){
	    if (application.portal.coordInsideBar(jpx, jpy)){
		portalStickedToMouse = true;
	    }
	    else {
		pcamStickedToMouse = true;
	    }
	}
	else {
	    if (v.lastGlyphEntered() == application.dmRegion){
		dmRegionStickedToMouse = true;
		application.vsm.stickToMouse(application.dmRegion);
	    }
	    else {
		dcamStickedToMouse = true;
	    }
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	portalStickedToMouse = false;
	dcamStickedToMouse = false;
	pcamStickedToMouse = false;
	if (dmRegionStickedToMouse){
	    application.vsm.unstickFromMouse();
	    dmRegionStickedToMouse = false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }


    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	application.switchPortal(jpx+PORTAL_INITIAL_X_OFFSET, jpy+PORTAL_INITIAL_Y_OFFSET);
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (inPortal){
	    if (buttonNumber == 1){
		if (portalStickedToMouse){
		    application.portal.move(jpx-lastJPX, jpy-lastJPY);
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
		    }
		}
		else if (dmRegionStickedToMouse){
		    application.updateDMWindow();
		}
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c = (inPortal) ? application.portalCamera : application.demoCamera;
	float a = (c.focal+Math.abs(c.altitude)) / c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	}
	application.updateDMRegion();
	application.vsm.repaintNow();
    }

    public void enterGlyph(Glyph g){
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
    }

    public void exitGlyph(Glyph g){
	if (g.isSelected()){
	    g.borderColor = (g.selectedColor != null) ? g.selectedColor : g.bColor;
	}
	else {
	    if (g.mouseInsideFColor != null){g.color = g.fColor;}
	    if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
	}
    }

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
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

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

    public void cameraMoved(){
	application.updateDMRegion();
    }

}
