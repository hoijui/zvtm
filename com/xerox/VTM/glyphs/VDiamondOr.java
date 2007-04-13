/*   FILE: VDiamondOr.java
 *   DATE OF CREATION:   Jul 27 2000
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;

/**
 * Reorient-able Diamond (losange with height equal to width). This version is less efficient than VDiamond, but it can be reoriented. It cannot be made translucent (see VDiamond*ST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VDiamond
 *@see com.xerox.VTM.glyphs.VDiamondOrST
 *@see com.xerox.VTM.glyphs.VDiamondST
 */

public class VDiamondOr extends VDiamond {

    public VDiamondOr(){super();}

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param or orientation
     */
    public VDiamondOr(long x,long y,float z,long s,Color c,float or){
	super(x,y,z,s,c);
	orient=or;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param bc border color
     *@param or orientation
     */
    public VDiamondOr(long x, long y, float z, long s, Color c, Color bc, float or){
	super(x, y, z, s, c, bc);
	orient=or;
    }

    public float getOrient(){return orient;}

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public boolean fillsView(long w,long h,int camIndex){
	if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	xcoords[0]=(int)Math.round(pc[i].cx+pc[i].cr*Math.cos(orient));
	xcoords[1]=(int)Math.round(pc[i].cx-pc[i].cr*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].cx-pc[i].cr*Math.cos(orient));
	xcoords[3]=(int)Math.round(pc[i].cx+pc[i].cr*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].cy-pc[i].cr*Math.sin(orient));
	ycoords[1]=(int)Math.round(pc[i].cy-pc[i].cr*Math.cos(orient));
	ycoords[2]=(int)Math.round(pc[i].cy+pc[i].cr*Math.sin(orient));
	ycoords[3]=(int)Math.round(pc[i].cy+pc[i].cr*Math.cos(orient));
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = xcoords[j];
		pc[i].p.ypoints[j] = ycoords[j];
	    }
	    pc[i].p.invalidate();
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vs*coef);
	xcoords[0]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.cos(orient));
	xcoords[1]=(int)Math.round(pc[i].lcx-pc[i].lcr*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].lcx-pc[i].lcr*Math.cos(orient));
	xcoords[3]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.sin(orient));
	ycoords[1]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.cos(orient));
	ycoords[2]=(int)Math.round(pc[i].lcy+pc[i].lcr*Math.sin(orient));
	ycoords[3]=(int)Math.round(pc[i].lcy+pc[i].lcr*Math.cos(orient));
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = xcoords[j];
		pc[i].lp.ypoints[j] = ycoords[j];
	    }
	    pc[i].lp.invalidate();
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr > 1){//repaint only if object is visible
	    if (filled) {
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
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].lcr >1){//repaint only if object is visible
	    if (filled) {
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
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	VDiamondOr res=new VDiamondOr(vx,vy,0,vs,color,orient);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
