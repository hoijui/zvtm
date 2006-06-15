/*   FILE: FPolygonST.java
 *   DATE OF CREATION:   Mon Jan 13 16:24:15 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 16:45:04 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Emmanuel Pietriga, 2003. All Rights Reserved
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
 * $Id: FPolygonST.java,v 1.7 2006/03/17 17:45:22 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.LongPoint;


/**
 * Custom  polygon - cannot be reoriented for now - transparency
 * @author Emmanuel Pietriga
 **/

public class FPolygonST extends FPolygon implements Transparent,Cloneable {

    /**semi transparency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates
     *@param c fill color
     */
    public FPolygonST(LongPoint[] v,Color c){
	super(v,c);
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
	if ((alpha==1.0) && (pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
 	if (pc[i].cr>1){//repaint only if object is visible
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	    g.setComposite(acO);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
 	if (pc[i].lcr>1){//repaint only if object is visible
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
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	    g.setComposite(acO);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	LongPoint[] lps=new LongPoint[xcoords.length];
	for (int i=0;i<lps.length;i++){
	    lps[i]=new LongPoint(xcoords[i]+vx,ycoords[i]+vy);
	}
	FPolygonST res=new FPolygonST(lps,color);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setTransparencyValue(alpha);
	return res;
    }

}
