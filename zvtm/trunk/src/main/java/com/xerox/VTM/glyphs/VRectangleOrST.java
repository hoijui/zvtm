/*   FILE: VRectangleOrST.java
 *   DATE OF CREATION:   Jul 24 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
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

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.VirtualSpaceManager;

/**
 * Reorient-able, translucent Rectangle. This version is less efficient than all others, but it can be reoriented and made translucent.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VRectangle
 *@see com.xerox.VTM.glyphs.VRectangleOr
 *@see com.xerox.VTM.glyphs.VRectangleST
 */

public class VRectangleOrST extends VRectangleOr implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    public VRectangleOrST(){
	super();
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param or orientation
     */
    public VRectangleOrST(long x, long y, int z, long w, long h, Color c, float or){
	super(x, y, z, w, h, c, or);
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param bc border color
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     *@param or orientation
     */
    public VRectangleOrST(long x, long y, int z, long w, long h, Color c, Color bc, float a, float or){
	super(x, y, z, w, h, c, bc, or);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    public void setTranslucencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public float getTranslucencyValue(){return alpha;}

    public boolean fillsView(long w,long h,int camIndex){
	if (orient==0){
	    if ((alpha==1.0) && (w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	    else {return false;}
	}
	else {
	    if ((alpha==1.0) && (pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	    else {return false;}
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if (alpha == 0){return;}
	if ((pc[i].cw>1) && (pc[i].ch>1)){//repaint only if object is visible
	    if (orient==0) {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    if (filled){
			g.setColor(this.color);
			g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
				((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
				// [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
				// the rectangle, in which case the border would not be visible;
				// the fact that the rectangle intersects the viewport has already been tested by the main
				// clipping algorithm
				g.setStroke(stroke);
				g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);   //outline rectangle
				g.setStroke(stdS);
			    }
			}
			else {
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);   //outline rectangle
			}
		    }
		    g.setComposite(acO);
		}
		else {
		    if (filled){
			g.setColor(this.color);
			g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
				((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
				// [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
				// the rectangle, in which case the border would not be visible;
				// the fact that the rectangle intersects the viewport has already been tested by the main
				// clipping algorithm
				g.setStroke(stroke);
				g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);   //outline rectangle
				g.setStroke(stdS);
			    }
			}
			else {
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);   //outline rectangle
			}
		    }
		}
	    }
	    else {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    if (filled){
			g.setColor(this.color);
			g.fillPolygon(pc[i].p);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    g.setStroke(stroke);
			    g.drawPolygon(pc[i].p);
			    g.setStroke(stdS);
			}
			else {
			    g.drawPolygon(pc[i].p);
			}
		    }
		    g.setComposite(acO);
		}
		else {
		    if (filled){
			g.setColor(this.color);
			g.fillPolygon(pc[i].p);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    g.setStroke(stroke);
			    g.drawPolygon(pc[i].p);
			    g.setStroke(stdS);
			}
			else {
			    g.drawPolygon(pc[i].p);
			}
		    }
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if (alpha == 0){return;}
	if ((pc[i].lcw>1) && (pc[i].lch>1)){//repaint only if object is visible
	    if (orient==0) {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    if (filled){
			g.setColor(this.color);
			g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
				((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
				// see [C1] above for explanations about this test
				g.setStroke(stroke);
				g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);   //outline rectangle
				g.setStroke(stdS);
			    }
			}
			else {
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);   //outline rectangle
			}
		    }
		    g.setComposite(acO);
		}
		else {
		    if (filled){
			g.setColor(this.color);
			g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
				((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
				// see [C1] above for explanations about this test
				g.setStroke(stroke);
				g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);   //outline rectangle
				g.setStroke(stdS);
			    }
			}
			else {
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);   //outline rectangle
			}
		    }
		}
	    }
	    else {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    if (filled){
			g.setColor(this.color);
			g.fillPolygon(pc[i].lp);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    g.setStroke(stroke);
			    g.drawPolygon(pc[i].lp);
			    g.setStroke(stdS);
			}
			else {
			    g.drawPolygon(pc[i].lp);
			}
		    }
		    g.setComposite(acO);
		}
		else {
		    if (filled){
			g.setColor(this.color);
			g.fillPolygon(pc[i].lp);
		    }
		    if (paintBorder){
			g.setColor(borderColor);
			if (stroke!=null) {
			    g.setStroke(stroke);
			    g.drawPolygon(pc[i].lp);
			    g.setStroke(stdS);
			}
			else {
			    g.drawPolygon(pc[i].lp);
			}
		    }
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    }
	}
    }

    public Object clone(){
	VRectangleOrST res=new VRectangleOrST(vx, vy, 0, vw, vh, color, borderColor, alpha, orient);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
