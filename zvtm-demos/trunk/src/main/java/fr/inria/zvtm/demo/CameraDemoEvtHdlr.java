/*   FILE: CameraDemoEvtHdlr.java
 *   DATE OF CREATION:   Dec 07 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Wed Oct 20 12:06:14 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.demo;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VSegment;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class CameraDemoEvtHdlr implements ViewEventHandler {

    Introduction application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    float tfactor;
    float cfactor=40.0f;

    Camera activeCam;

    VSegment navSeg;

    CameraDemoEvtHdlr(Introduction appli){
	application=appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	try {
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		if (g.getCGlyph()!=null){
		    application.vsm.stickToMouse(g.getCGlyph());
		}
		else {
		    application.vsm.stickToMouse(g);
		    application.vsm.getVirtualSpace("vs1").onTop(g);
		}
	    }
	}
	catch (NullPointerException ex){}
	application.vsm.activeView.mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(true);
	application.vsm.unstickFromMouse();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
	v.setDrawSegment(true);
	application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=v.cams[0];
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getAnimationManager().setXspeed(0);
	application.vsm.getAnimationManager().setYspeed(0);
	application.vsm.getAnimationManager().setZspeed(0);
	v.setDrawSegment(false);
	application.vsm.activeView.mouse.setSensitivity(true);
	if (autoZoomed){
	    Animation anim = application.vsm.getAnimationManager().getAnimationFactory()
		.createCameraAltAnim(300, v.cams[0], -2*v.cams[0].getAltitude()/3.0f, true,
				     IdentityInterpolator.getInstance(), null);
	    application.vsm.getAnimationManager().startAnimation(anim, true);
	    
	    autoZoomed = false;
	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    double drag;
    boolean autoZoomed = false;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == META_SHIFT_MOD) {
		application.vsm.getAnimationManager().setXspeed(0);
		application.vsm.getAnimationManager().setYspeed(0);
 		application.vsm.getAnimationManager().setZspeed((activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor)));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.getAnimationManager().setXspeed((activeCam.altitude>0) ? (long)((jpx-lastJPX)*(tfactor/cfactor)) : (long)((jpx-lastJPX)/(tfactor*cfactor)));
		application.vsm.getAnimationManager().setYspeed((activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor)));
		application.vsm.getAnimationManager().setZspeed(0);
		if (application.isAutoZoomEnabled()){
		    drag = Math.sqrt(Math.pow(jpx-lastJPX, 2) + Math.pow(jpy-lastJPY, 2));
		    if (!autoZoomed && drag > 300.0f){
			autoZoomed = true;
		
			Animation anim = application.vsm.getAnimationManager().getAnimationFactory()
			    .createCameraAltAnim(300, v.cams[0], 2*v.cams[0].getAltitude(), true,
						 IdentityInterpolator.getInstance(), null);
			application.vsm.getAnimationManager().startAnimation(anim, true);
		    }
		}
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
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

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
	switch(c){
	case 'c':{
	    application.vsm.getGlobalView(application.vsm.getActiveCamera(),200);
	    break;
	}
	case 'f':{
	    fr.inria.zvtm.glyphs.GlyphFactory.getGlyphFactoryDialog((java.awt.Frame)v.parent.getFrame());
	    break;
	}
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public String toString(){return "CameraDemoEvtHdlr";}

}
