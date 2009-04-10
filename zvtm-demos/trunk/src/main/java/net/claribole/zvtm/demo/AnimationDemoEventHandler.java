/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 22 17:58:34 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zvtm.demo;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Vector;

import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VCursor;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;

class AnimationDemoEventHandler implements ViewEventHandler {

    AnimationDemo application;

    long lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    Glyph underCursor;

    AnimationDemoEventHandler(AnimationDemo app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	underCursor = getGlyph(v.getMouse());
	if (underCursor != null){
	    application.mSpace.onTop((underCursor.getCGlyph() != null) ? underCursor.getCGlyph() : underCursor);
	    application.vsm.stickToMouse((underCursor.getCGlyph() != null) ? underCursor.getCGlyph() : underCursor);
	}
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	application.vsm.unstickFromMouse();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (underCursor != null){
	    application.animate(underCursor);
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getAnimationManager().setXspeed(0);
	application.vsm.getAnimationManager().setYspeed(0);
	application.vsm.getAnimationManager().setZspeed(0);
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    /* Mapping from drag segment value to speed */
    static final float SPEED_FACTOR = 50.0f;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    Camera c = application.vsm.getActiveCamera();
	    float a = (c.focal+Math.abs(c.altitude))/c.focal;
	    if (mod == META_SHIFT_MOD) {
		application.vsm.getAnimationManager().setXspeed(0);
		application.vsm.getAnimationManager().setYspeed(0);
		application.vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy) * (a/SPEED_FACTOR)) : (long)((lastJPY-jpy) / (a*SPEED_FACTOR)));
	    }
	    else {
		application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX) * (a/SPEED_FACTOR)) : (long)((jpx-lastJPX) / (a*SPEED_FACTOR)));
		application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy) * (a/SPEED_FACTOR)) : (long)((lastJPY-jpy) / (a*SPEED_FACTOR)));
		application.vsm.getAnimationManager().setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
	float a = (c.focal+Math.abs(c.altitude))/c.focal;
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

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

    Glyph getGlyph(VCursor c){
	Glyph res = c.lastGlyphEntered;
	if (res != null){return res;}
	else {
	    Vector v = c.getIntersectingTexts(application.mCam);
	    if (v != null){
		res = (Glyph)v.firstElement();
	    }
	    else {
		v = c.getIntersectingSegments(application.mCam, 4);
		if (v != null){
		    res = (Glyph)v.firstElement();
		}
	    }
	}
	return res;
    }
    
}