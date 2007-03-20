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
 * $Id: VRectangleOrST.java,v 1.7 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Rectangle - can be reoriented - transparency
 * @author Emmanuel Pietriga
 **/

public class VRectangleOrST extends VRectangleOr implements Transparent,Cloneable {

    /**semi transparency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    public VRectangleOrST(){
	super();
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param or orientation
     */
    public VRectangleOrST(long x,long y,float z,long w,long h,Color c,float or){
	super(x,y,z,w,h,c,or);
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /**
     *set alpha channel value (transparency)
     *@param a [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public void setTransparencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get alpha value (transparency) for this glyph*/
    public float getTransparencyValue(){return alpha;}

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
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

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)){//repaint only if object is visible
	    if (orient==0) {
		if (filled){
		    g.setColor(this.color);
		    g.setComposite(acST);
		    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
		    g.setComposite(acO);
		}
		g.setColor(borderColor);
		if (paintBorder){
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
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.setComposite(acST);
		    g.fillPolygon(pc[i].p);
		    g.setComposite(acO);
		}
		g.setColor(borderColor);
		if (paintBorder){
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    g.setComposite(acO);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)){//repaint only if object is visible
	    if (orient==0) {
		if (filled){
		    g.setColor(this.color);
		    g.setComposite(acST);
		    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
		    g.setComposite(acO);
		}
		g.setColor(borderColor);
		if (paintBorder){
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
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.setComposite(acST);
		    g.fillPolygon(pc[i].lp);
		    g.setComposite(acO);
		}
		g.setColor(borderColor);
		if (paintBorder){
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    g.setComposite(acO);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VRectangleOrST res=new VRectangleOrST(vx,vy,0,vw,vh,color,orient);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setTransparencyValue(alpha);
	return res;
    }

}
