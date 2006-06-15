/*   FILE: ZgrAppletEvtHdlr.java
 *   DATE OF CREATION:   Fri May 09 09:54:03 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri May 09 10:09:49 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zgrviewer;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VSegment;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ZgrAppletEvtHdlr implements ViewEventHandler {

    ZGRApplet application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;

    boolean zoomingInRegion=false;
    boolean manualLeftButtonMove=false;
    boolean manualRightButtonMove=false;

    ZgrAppletEvtHdlr(ZGRApplet app){
	this.application=app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.rememberLocation(v.cams[0].getLocation());
	if (mod==NO_MODIFIER || mod==SHIFT_MOD){
	    manualLeftButtonMove=true;
	    lastJPX=jpx;
	    lastJPY=jpy;
	    //application.vsm.setActiveCamera(v.cams[0]);
	    v.setDrawDrag(true);
	    application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	    activeCam=application.vsm.getActiveCamera();
	}
	else if (mod==CTRL_MOD){
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
		application.vsm.centerOnRegion(application.vsm.getActiveCamera(),ConfigManager.ANIM_MOVE_LENGTH,x1,y1,x2,y2);
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

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	Glyph g=v.lastGlyphEntered();
	if (g!=null){
	    application.vsm.centerOnGlyph(g,v.cams[0],ConfigManager.ANIM_MOVE_LENGTH);
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.rememberLocation(v.cams[0].getLocation());
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
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

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	Glyph g=v.lastGlyphEntered();
	if (g!=null){
	    application.vsm.centerOnGlyph(g,v.cams[0],ConfigManager.ANIM_MOVE_LENGTH);
	}
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber==3 || (buttonNumber==1 && (mod==NO_MODIFIER || mod==SHIFT_MOD))){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod==SHIFT_MOD) {
		application.vsm.animator.Xspeed=0;
		application.vsm.animator.Yspeed=0;
 		application.vsm.animator.Aspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.animator.Xspeed=(activeCam.altitude>0) ? (long)((jpx-lastJPX)*(tfactor/cfactor)) : (long)((jpx-lastJPX)/(tfactor*cfactor));
		application.vsm.animator.Yspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));
		application.vsm.animator.Aspeed=0;
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

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

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

}
