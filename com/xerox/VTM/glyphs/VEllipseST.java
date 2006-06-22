/*   FILE: VEllipseST.java
 *   DATE OF CREATION:   Dec 24 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jul 10 16:44:46 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 * $Id: VEllipseST.java,v 1.7 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

  /**
   * Ellipse - cannot be reoriented - transparency
   * @author Emmanuel Pietriga
   */

public class VEllipseST extends VEllipse implements Transparent,Cloneable {

    /**semi transparency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    public VEllipseST(){
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
     */
    public VEllipseST(long x,long y,float z,long w,long h,Color c){
	super(x,y,z,w,h,c);
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

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].ellipse.getBounds().width>2) && (pc[i].ellipse.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.setComposite(acST);
		g.translate(dx, dy);
		g.fill(pc[i].ellipse);
		g.translate(-dx, -dy);
		g.setComposite(acO);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    g.setComposite(acO);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lellipse.getBounds().width>2) && (pc[i].lellipse.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.setComposite(acST);
		g.translate(dx, dy);
		g.fill(pc[i].lellipse);
		g.translate(-dx, -dy);
		g.setComposite(acO);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    g.setComposite(acO);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VEllipseST res=new VEllipseST(vx,vy,0,vw,vh,color);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setTransparencyValue(alpha);
	return res;
    }

}
