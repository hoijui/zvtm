/*   FILE: VSegmentST.java
 *   DATE OF CREATION:  Tue May 16 18:36:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
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
 * Translucent Segment (straight line). This version is less efficient than VSegment, but it can be made translucent.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VSegment
 */

public class VSegmentST extends VSegment implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    public VSegmentST(){
	super();
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *give the centre of segment and half its projected length on X and Y axis
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space (can be negative)
     *@param h half height in virtual space (can be negative)
     *@param c fill color
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegmentST(long x, long y, float z, long w, long h, Color c, float a){
	super(x, y, z, w, h, c);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /**
     *give the end points of segment
     *@param x1 coordinate of endpoint 1 in virtual space
     *@param y1 coordinate of endpoint 1 in virtual space
     *@param z altitude
     *@param c fill color
     *@param x2 coordinate of endpoint 2 in virtual space
     *@param y2 coordinate of endpoint 2 in virtual space
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegmentST(long x1, long y1, float z, Color c, long x2, long y2, float a){
	super(x1, y1, z, c, x2, y2);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /**
     *give the centre of segment and half its length & orient
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param lgth half length in virtual space
     *@param angle orientation
     *@param c fill color
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegmentST(long x, long y, float z, float lgth, float angle, Color c, float a){
	super(x, y, z, lgth, angle, c);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    public void setTranslucencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public float getTranslucencyValue(){return alpha;}

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.setStroke(stroke);
		g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
		g.setStroke(stdS);
		g.setComposite(acO);
	    }
	    else {
		g.setStroke(stroke);
		g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
		g.setStroke(stdS);
	    }
	}
	else if (alpha < 1.0f){
	    g.setComposite(acST);
	    g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
	    g.setComposite(acO);
	}
	else {
	    g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);	    
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.setStroke(stroke);
		g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
		g.setStroke(stdS);
		g.setComposite(acO);
	    }
	    else {
		g.setStroke(stroke);
		g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
		g.setStroke(stdS);
	    }
	}
	else if (alpha < 1.0f){
	    g.setComposite(acST);
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
	    g.setComposite(acO);
	}
	else {
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
	}
    }

    public Object clone(){
	VSegmentST res=new VSegmentST(vx, vy, 0, vw, vh, color, alpha);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
