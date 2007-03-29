/*   FILE: SSEventHandler.java
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Hashtable;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.AnimationListener;

class SSEventHandler extends WorldTaskEventHandler {

    long ovx,ovy;
    long nvx,nvy;
    VText vt;

    SSEventHandler(ZLWorldTask appli){
	super(appli);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	java.util.Vector v1 = v.getMouse().getIntersectingTexts(application.demoCamera);
	if (v1 != null && v1.size() > 0){
	    LText t = (LText)v1.firstElement();
	    vt = t;
	    ovx = t.vx;
	    ovy = t.vy;
	    application.vsm.stickToMouse(t);
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.unstickFromMouse();
	if (vt != null){
	    nvx = vt.vx;
	    nvy = vt.vy;
	    if (ZLWorldTask.SHOW_CONSOLE){application.console.append(ovy+" "+ovx+"       "+nvy+" "+nvx+"\n");}
	    vt = null;
	}
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
	if (application.SHOW_COORDS){
// 	    latitude = doubleFormatter(v.getMouse().vy * ZLWorldTaskMapManager.COORDS_CONV);
// 	    longitude = doubleFormatter(v.getMouse().vx * ZLWorldTaskMapManager.COORDS_CONV);
	    latitude = String.valueOf(v.getMouse().vy);
	    longitude = String.valueOf(v.getMouse().vx);
	}
	//application.vsm.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
 	if (buttonNumber == 3){
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	    if (mod == SHIFT_MOD || mod == META_SHIFT_MOD){
		application.vsm.animator.Xspeed = 0;
		application.vsm.animator.Yspeed = 0;
		application.vsm.animator.Aspeed = (lastJPY-jpy)*(a/50.0f);  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
		application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
		application.vsm.animator.Aspeed = 0;
	    }
 	}
	if (lensType != 0 && application.lens != null){
	    application.lens.setAbsolutePosition(jpx, jpy);
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == WHEEL_UP){
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	    else {
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	}
	else {
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	    if (wheelDirection  == WHEEL_UP){
		application.demoCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	    else {//wheelDirection == WHEEL_DOWN
		application.demoCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ZLWorldTask.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ZLWorldTask.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ZLWorldTask.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ZLWorldTask.MOVE_RIGHT);}
	else if (code==KeyEvent.VK_C){application.vsm.getGlobalView(application.vsm.getActiveCamera(), 200);}
	else if (code==KeyEvent.VK_PLUS){application.showGridLevel(application.currentLevel+1);}
	else if (code==KeyEvent.VK_MINUS){application.showGridLevel(application.currentLevel-1);}
	else if (code==KeyEvent.VK_J){application.ewmm.switchAdaptMaps();}
	else if (code==KeyEvent.VK_K){
	    application.SHOW_MEMORY_USAGE = !application.SHOW_MEMORY_USAGE;
	    application.vsm.repaintNow();
	}
	else if (code==KeyEvent.VK_L){
	    application.SHOW_COORDS = !application.SHOW_COORDS;
	    application.vsm.repaintNow();
	}
	else if (code==KeyEvent.VK_G){application.gc();}
	else if (code==KeyEvent.VK_SPACE){
	    if (application.screenSaver != null){application.screenSaver.switchSS();}
	}
	else if (code == KeyEvent.VK_Q && mod == CTRL_MOD){System.exit(0);}
	else if (code == KeyEvent.VK_D){
	    Glyph g = v.lastGlyphEntered();
	    if (g != null){
		int i = application.gds.getCityIndex(g);
		if (i != -1){
		    application.gds.deleteCity(i);
		}
		else {
		    if (ZLWorldTask.SHOW_CONSOLE){application.console.append("City not found\n");}
		}
	    }
	}
	else if (code == KeyEvent.VK_N){
	    application.gds.countCities();
	}
	else if (code == KeyEvent.VK_S){
	    application.gds.saveCities();
	}
    }

}
