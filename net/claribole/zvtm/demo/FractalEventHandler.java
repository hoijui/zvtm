/*   FILE: FractalEventHandler.java
 *   DATE OF CREATION:  Thu Dec 30 13:28:46 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.Glyph;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class FractalEventHandler implements ViewEventHandler {

    FractalDemo application;

    boolean manualLeftButtonMove = false;
    boolean manualRightButtonMove=false;
    boolean zoomingInRegion=false;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long jpxD, jpyD;
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                    //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;

    FractalEventHandler(FractalDemo app){
	this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (mod == NO_MODIFIER || mod == SHIFT_MOD || mod == META_MOD || mod == META_SHIFT_MOD){
	    manualLeftButtonMove=true;
	    lastJPX=jpx;
	    lastJPY=jpy;
	    v.setDrawDrag(true);
	    application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	    activeCam=application.vsm.getActiveCamera();
	}
	else if (mod == ALT_MOD){
	    zoomingInRegion=true;
	    x1=v.getMouse().vx;
	    y1=v.getMouse().vy;
	    v.setDrawRect(true);
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (zoomingInRegion){
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
		application.vsm.centerOnRegion(application.vsm.getActiveCamera(),FractalDemo.ANIM_MOVE_LENGTH,x1,y1,x2,y2);
	    }
	    zoomingInRegion=false;
	}
	else if (manualLeftButtonMove){
	    application.vsm.animator.Xspeed=0;
	    application.vsm.animator.Yspeed=0;
	    application.vsm.animator.Aspeed=0;
	    v.setDrawDrag(false);
	    application.vsm.activeView.mouse.setSensitivity(true);
	    manualLeftButtonMove=false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy, int clickNumber, MouseEvent e){
	LongPoint lp = new LongPoint(v.getMouse().vx - v.cams[0].posx, v.getMouse().vy - v.cams[0].posy);
	application.vsm.animator.createCameraAnimation(FractalDemo.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, lp, v.cams[0].getID());
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=application.vsm.getActiveCamera();
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed=0;
	application.vsm.animator.Yspeed=0;
	application.vsm.animator.Aspeed=0;
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy, int clickNumber, MouseEvent e){
	LongPoint lp = new LongPoint(v.getMouse().vx - v.cams[0].posx, v.getMouse().vy - v.cams[0].posy);
	application.vsm.animator.createCameraAnimation(FractalDemo.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, lp, v.cams[0].getID());
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (mod != ALT_MOD && (buttonNumber == 1 || buttonNumber == 3)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == SHIFT_MOD || mod == META_SHIFT_MOD){
		application.vsm.animator.Xspeed=0;
		application.vsm.animator.Yspeed=0;
		application.vsm.animator.Aspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));
		//50 is just a speed factor (too fast otherwise)
	    }
	    else {
		jpxD = jpx-lastJPX;
		jpyD = lastJPY-jpy;
		application.vsm.animator.Xspeed=(activeCam.altitude>0) ? (long)(jpxD*(tfactor/cfactor)) : (long)(jpxD/(tfactor*cfactor));
		application.vsm.animator.Yspeed=(activeCam.altitude>0) ? (long)(jpyD*(tfactor/cfactor)) : (long)(jpyD/(tfactor*cfactor));
		application.vsm.animator.Aspeed=0;
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=application.vsm.getActiveCamera();
	float a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(a*10);
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(-a*10);
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(FractalDemo.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(FractalDemo.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(FractalDemo.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(FractalDemo.MOVE_RIGHT);}
// 	else if (code == KeyEvent.VK_L){application.toggleLens();}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

}
