/*   FILE: AnimationEvtHdlr.java
 *   DATE OF CREATION:   Dec 08 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Wed Oct 20 12:06:56 2004 by Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
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
 * $Id: AnimationEvtHdlr.java,v 1.5 2006/05/26 14:51:48 epietrig Exp $
 */

package com.xerox.VTM.demo;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VSegment;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class AnimationEvtHdlr implements ViewEventHandler {

    Introduction application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    float tfactor;
    float cfactor=40.0f;

    VSegment navSeg;

    Camera activeCam;

    Glyph orientation;Glyph size;Glyph color;Glyph translation; //type of animation
    Glyph linear;Glyph exponential;Glyph sigmoid; //temporal scheme

    AnimationEvtHdlr(Introduction appli,Glyph o,Glyph s,Glyph c,Glyph t,Glyph l,Glyph e,Glyph sg){
	application=appli;
	orientation=o;size=s;color=c;translation=t;
	linear=l;exponential=e;sigmoid=sg;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	try {
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null && g.getType().equals("an")){
		if (g.getCGlyph()!=null){
		    application.vsm.stickToMouse(g.getCGlyph());
		}
		else {
		    application.vsm.stickToMouse(g);
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

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	Glyph a;
	if ((a=v.lastGlyphEntered())!=null){
	    String t=a.getType();
	    if (t.equals("an")){
		if (a.getCGlyph()!=null){application.animate(a.getCGlyph());}
		else {application.animate(a);}
	    }
	    else if (t.equals("orient")){
		if (application.animType.equals("orient")){
		    application.animType="";
		    orientation.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animType="orient";
		    orientation.setHSVColor(0.0f,1.0f,0.7f);
		    color.setHSVColor(0.36f,1.0f,0.6f);
		    size.setHSVColor(0.36f,1.0f,0.6f);
		    translation.setHSVColor(0.36f,1.0f,0.6f);
		}
	    }
	    else if (t.equals("size")){
		if (application.animType.equals("size")){
		    application.animType="";
		    size.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animType="size";
		    orientation.setHSVColor(0.36f,1.0f,0.6f);
		    color.setHSVColor(0.36f,1.0f,0.6f);
		    size.setHSVColor(0.0f,1.0f,0.7f);
		    translation.setHSVColor(0.36f,1.0f,0.6f);
		}
	    }
	    else if (t.equals("col")){
		if (application.animType.equals("col")){
		    application.animType="";
		    a.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animType="col";
		    orientation.setHSVColor(0.36f,1.0f,0.6f);
		    color.setHSVColor(0.0f,1.0f,0.7f);
		    size.setHSVColor(0.36f,1.0f,0.6f);
		    translation.setHSVColor(0.36f,1.0f,0.6f);
		}
	    }
	    else if (t.equals("pos")){
		if (application.animType.equals("trans")){
		    application.animType="";
		    a.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animType="trans";
		    orientation.setHSVColor(0.36f,1.0f,0.6f);
		    color.setHSVColor(0.36f,1.0f,0.6f);
		    size.setHSVColor(0.36f,1.0f,0.6f);
		    translation.setHSVColor(0.0f,1.0f,0.7f);
		}
	    }
	    else if (t.equals("lin")){
		if (application.animScheme.equals("lin")){
		    application.animScheme="";
		    linear.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animScheme="lin";
		    exponential.setHSVColor(0.36f,1.0f,0.6f);
		    sigmoid.setHSVColor(0.36f,1.0f,0.6f);
		    linear.setHSVColor(0.0f,1.0f,0.7f);
		}
	    }
	    else if (t.equals("exp")){
		if (application.animScheme.equals("exp")){
		    application.animScheme="";
		    exponential.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animScheme="exp";
		    linear.setHSVColor(0.36f,1.0f,0.6f);
		    sigmoid.setHSVColor(0.36f,1.0f,0.6f);
		    exponential.setHSVColor(0.0f,1.0f,0.7f);
		}
	    }
	    else if (t.equals("sig")){
		if (application.animScheme.equals("sig")){
		    application.animScheme="";
		    sigmoid.setHSVColor(0.36f,1.0f,0.6f);
		}
		else{
		    application.animScheme="sig";
		    exponential.setHSVColor(0.36f,1.0f,0.6f);
		    linear.setHSVColor(0.36f,1.0f,0.6f);
		    sigmoid.setHSVColor(0.0f,1.0f,0.7f);
		}
	    }
	    application.vsm.repaintNow();
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=v.cams[0];
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed=0;
	application.vsm.animator.Yspeed=0;
	application.vsm.animator.Aspeed=0;
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == META_SHIFT_MOD) {
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

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){}

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

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
	switch(c){
	case 'c':{
	    application.vsm.getGlobalView(application.vsm.getActiveCamera(),200);
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
