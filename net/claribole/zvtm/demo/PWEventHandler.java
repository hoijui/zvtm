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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class PWEventHandler implements ViewEventHandler, PortalEventHandler {

    PortalWorldDemo application;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    PWEventHandler(PortalWorldDemo appli){
	application = appli;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	application.vsm.animator.Xspeed=0;
	application.vsm.animator.Yspeed=0;
	application.vsm.animator.Aspeed=0;
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	application.switchPortal(jpx, jpy);
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 1){
	    Camera c = application.demoCamera;
	    float a = (c.focal+Math.abs(c.altitude)) / c.focal;
	    if (mod == META_SHIFT_MOD) {
		application.vsm.animator.Xspeed = 0;
		application.vsm.animator.Yspeed = 0;
		application.vsm.animator.Aspeed = (c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.animator.Xspeed = (c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50));
		application.vsm.animator.Yspeed = (c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));
		application.vsm.animator.Aspeed = 0;
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c = application.demoCamera;
	float a = (c.focal+Math.abs(c.altitude)) / c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaintNow();
	}
    }

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){

    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView(mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView(mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView(mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_UP){application.translateView(PortalWorldDemo.MOVE_UP, mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(PortalWorldDemo.MOVE_DOWN, mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(PortalWorldDemo.MOVE_LEFT, mod == CTRL_MOD);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(PortalWorldDemo.MOVE_RIGHT, mod == CTRL_MOD);}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    /**cursor enters portal*/
    public void enterPortal(Portal p){((CameraPortal)p).setBorder(Color.WHITE);application.vsm.repaintNow();}

    /**cursor exits portal*/
    public void exitPortal(Portal p){((CameraPortal)p).setBorder(Color.RED);application.vsm.repaintNow();}

}
