/*   FILE: VCircleST.java
 *   DATE OF CREATION:   Nov 22 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
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
 * $Id: VCircleST.java,v 1.7 2006/01/16 08:37:14 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Circle - cannot be reoriented - transparency
 * @author Emmanuel Pietriga
 **/

public class VCircleST extends VCircle implements Transparent,Cloneable {

    /**semi transparency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    public VCircleST(){
	super();
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param r radius in virtual space
     *@param c fill color
     */
    public VCircleST(long x,long y,float z,long r,Color c){
	super(x,y,z,r,c);
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /**
     *set alpha channel value (transparency)
     *@param a [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public void setTransparencyValue(float a){
	alpha=a;
	acST=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to alpha
    }

    /**get alpha value (transparency) for this glyph*/
    public float getTransparencyValue(){return alpha;}

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	if ((alpha==1.0) && (Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cr)){return true;}
	else {return false;}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr>1){
	    g.setColor(this.color);  
	    g.setComposite(acST);
	    if (filled){g.fillOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);}
	    g.setComposite(acO);
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
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
	if (pc[i].lcr>1){
	    g.setColor(this.color);  
	    g.setComposite(acST);
	    if (filled){g.fillOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);}
	    g.setComposite(acO);
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
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
	VCircleST res=new VCircleST(vx,vy,0,vr,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setTransparencyValue(alpha);
	return res;
    }
    
}
