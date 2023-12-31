/*   FILE: VTriangleOrST.java
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

/**
 * Reorient-able, translucent Equilateral Triangle. This version is less efficient than all others, but it can be reoriented and made translucent.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VTriangle
 *@see com.xerox.VTM.glyphs.VTriangleOr
 *@see com.xerox.VTM.glyphs.VTriangleST
 */

public class VTriangleOrST extends VTriangleOr implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    public VTriangleOrST(){
	super();
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param h height in virtual space
     *@param c fill color
     *@param bc border color
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     *@param or orientation
     */
    public VTriangleOrST(long x, long y, int z, long h, Color c, Color bc, float a, float or){
	super(x, y, z, h, c, bc, or);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    public void setTranslucencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public float getTranslucencyValue(){return alpha;}

    public boolean fillsView(long w,long h,int camIndex){
	if ((alpha==1.0) && (pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if (alpha == 0){return;}
	if (pc[i].cr>1){//repaint only if object is visible
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fillPolygon(pc[i].p);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null) {
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.drawPolygon(pc[i].p);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.drawPolygon(pc[i].p);
			g.translate(-dx, -dy);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fillPolygon(pc[i].p);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null) {
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.drawPolygon(pc[i].p);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.drawPolygon(pc[i].p);
			g.translate(-dx, -dy);
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
	if (pc[i].lcr>1){//repaint only if object is visible
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fillPolygon(pc[i].lp);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null) {
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.drawPolygon(pc[i].lp);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.drawPolygon(pc[i].lp);
			g.translate(-dx, -dy);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fillPolygon(pc[i].lp);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null) {
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.drawPolygon(pc[i].lp);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.drawPolygon(pc[i].lp);
			g.translate(-dx, -dy);
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
	VTriangleOrST res = new VTriangleOrST(vx, vy, 0, vh, color, borderColor, alpha, orient);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
