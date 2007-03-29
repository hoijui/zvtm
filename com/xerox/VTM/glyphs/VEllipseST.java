/*   FILE: VEllipseST.java
 *   DATE OF CREATION:   Dec 24 2001
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
 * Translucent Ellipse. This version is less efficient than VEllipse, but it can be made translucent.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VEllipse
 *@see com.xerox.VTM.glyphs.VCircle
 *@see com.xerox.VTM.glyphs.VCircleST
 */


public class VEllipseST extends VEllipse implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    public VEllipseST(){
	super();
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public VEllipseST(long x, long y, float z, long w, long h, Color c){
	super(x, y, z, w, h, c);
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param bc border color
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VEllipseST(long x, long y, float z, long w, long h, Color c, Color bc, float a){
	super(x, y, z, w, h, c, bc);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    public void setTranslucencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public float getTranslucencyValue(){return alpha;}

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].ellipse.getBounds().width>2) && (pc[i].ellipse.getBounds().height>2)){
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fill(pc[i].ellipse);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null){
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.draw(pc[i].ellipse);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.draw(pc[i].ellipse);
			g.translate(-dx, -dy);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fill(pc[i].ellipse);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null){
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.draw(pc[i].ellipse);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.draw(pc[i].ellipse);
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
	if ((pc[i].lellipse.getBounds().width>2) && (pc[i].lellipse.getBounds().height>2)){
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fill(pc[i].lellipse);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null){
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.draw(pc[i].lellipse);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.draw(pc[i].lellipse);
			g.translate(-dx, -dy);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.translate(dx, dy);
		    g.fill(pc[i].lellipse);
		    g.translate(-dx, -dy);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke!=null){
			g.setStroke(stroke);
			g.translate(dx, dy);
			g.draw(pc[i].lellipse);
			g.translate(-dx, -dy);
			g.setStroke(stdS);
		    }
		    else {
			g.translate(dx, dy);
			g.draw(pc[i].lellipse);
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
	VEllipseST res=new VEllipseST(vx, vy, 0, vw, vh, color, borderColor, alpha);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
