/*   FILE: VOctagonOr.java
 *   DATE OF CREATION:   Jul 28 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
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
 * $Id: VOctagonOr.java,v 1.8 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * Octagon (eight "almost" regular edges) - can be reoriented
 * @author Emmanuel Pietriga
 **/

public class VOctagonOr extends VOctagon implements Cloneable {

    public VOctagonOr(){super();}

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param or orientation
     */
    public VOctagonOr(long x,long y,float z,long s,Color c,float or){
	super(x,y,z,s,c);
	orient=or;
	//if (orient!=0){computeOrientCoords();}
    }

    /**get orientation*/
    public float getOrient(){return orient;}

    /**set orientation (absolute)*/
    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

   /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){//project glyph wrt camera info and change origin -> JPanel coords
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	pc[i].halfcr=pc[i].cr/2;
	xcoords[0]=(int)Math.round((pc[i].cr*Math.cos(orient)-pc[i].halfcr*Math.sin(orient))+pc[i].cx);
	xcoords[1]=(int)Math.round((pc[i].halfcr*Math.cos(orient)-pc[i].cr*Math.sin(orient))+pc[i].cx);
	xcoords[2]=(int)Math.round((-pc[i].halfcr*Math.cos(orient)-pc[i].cr*Math.sin(orient))+pc[i].cx);
	xcoords[3]=(int)Math.round((-pc[i].cr*Math.cos(orient)-pc[i].halfcr*Math.sin(orient))+pc[i].cx);
	xcoords[4]=(int)Math.round((-pc[i].cr*Math.cos(orient)+pc[i].halfcr*Math.sin(orient))+pc[i].cx);
	xcoords[5]=(int)Math.round((-pc[i].halfcr*Math.cos(orient)+pc[i].cr*Math.sin(orient))+pc[i].cx);
	xcoords[6]=(int)Math.round((pc[i].halfcr*Math.cos(orient)+pc[i].cr*Math.sin(orient))+pc[i].cx);
	xcoords[7]=(int)Math.round((pc[i].cr*Math.cos(orient)+pc[i].halfcr*Math.sin(orient))+pc[i].cx);
	ycoords[0]=(int)Math.round((-pc[i].halfcr*Math.cos(orient)-pc[i].cr*Math.sin(orient))+pc[i].cy);
	ycoords[1]=(int)Math.round((-pc[i].cr*Math.cos(orient)-pc[i].halfcr*Math.sin(orient))+pc[i].cy);
	ycoords[2]=(int)Math.round((-pc[i].cr*Math.cos(orient)+pc[i].halfcr*Math.sin(orient))+pc[i].cy);
	ycoords[3]=(int)Math.round((-pc[i].halfcr*Math.cos(orient)+pc[i].cr*Math.sin(orient))+pc[i].cy);
	ycoords[4]=(int)Math.round((pc[i].halfcr*Math.cos(orient)+pc[i].cr*Math.sin(orient))+pc[i].cy);
	ycoords[5]=(int)Math.round((pc[i].cr*Math.cos(orient)+pc[i].halfcr*Math.sin(orient))+pc[i].cy);
	ycoords[6]=(int)Math.round((pc[i].cr*Math.cos(orient)-pc[i].halfcr*Math.sin(orient))+pc[i].cy);
	ycoords[7]=(int)Math.round((pc[i].halfcr*Math.cos(orient)-pc[i].cr*Math.sin(orient))+pc[i].cy);
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 8);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = xcoords[j];
		pc[i].p.ypoints[j] = ycoords[j];
	    }
	    pc[i].p.invalidate();
	}
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vs*coef);
	pc[i].lhalfcr=pc[i].lcr/2;
	xcoords[0]=(int)Math.round((pc[i].lcr*Math.cos(orient)-pc[i].lhalfcr*Math.sin(orient))+pc[i].lcx);
	xcoords[1]=(int)Math.round((pc[i].lhalfcr*Math.cos(orient)-pc[i].lcr*Math.sin(orient))+pc[i].lcx);
	xcoords[2]=(int)Math.round((-pc[i].lhalfcr*Math.cos(orient)-pc[i].lcr*Math.sin(orient))+pc[i].lcx);
	xcoords[3]=(int)Math.round((-pc[i].lcr*Math.cos(orient)-pc[i].lhalfcr*Math.sin(orient))+pc[i].lcx);
	xcoords[4]=(int)Math.round((-pc[i].lcr*Math.cos(orient)+pc[i].lhalfcr*Math.sin(orient))+pc[i].lcx);
	xcoords[5]=(int)Math.round((-pc[i].lhalfcr*Math.cos(orient)+pc[i].lcr*Math.sin(orient))+pc[i].lcx);
	xcoords[6]=(int)Math.round((pc[i].lhalfcr*Math.cos(orient)+pc[i].lcr*Math.sin(orient))+pc[i].lcx);
	xcoords[7]=(int)Math.round((pc[i].lcr*Math.cos(orient)+pc[i].lhalfcr*Math.sin(orient))+pc[i].lcx);
	ycoords[0]=(int)Math.round((-pc[i].lhalfcr*Math.cos(orient)-pc[i].lcr*Math.sin(orient))+pc[i].lcy);
	ycoords[1]=(int)Math.round((-pc[i].lcr*Math.cos(orient)-pc[i].lhalfcr*Math.sin(orient))+pc[i].lcy);
	ycoords[2]=(int)Math.round((-pc[i].lcr*Math.cos(orient)+pc[i].lhalfcr*Math.sin(orient))+pc[i].lcy);
	ycoords[3]=(int)Math.round((-pc[i].lhalfcr*Math.cos(orient)+pc[i].lcr*Math.sin(orient))+pc[i].lcy);
	ycoords[4]=(int)Math.round((pc[i].lhalfcr*Math.cos(orient)+pc[i].lcr*Math.sin(orient))+pc[i].lcy);
	ycoords[5]=(int)Math.round((pc[i].lcr*Math.cos(orient)+pc[i].lhalfcr*Math.sin(orient))+pc[i].lcy);
	ycoords[6]=(int)Math.round((pc[i].lcr*Math.cos(orient)-pc[i].lhalfcr*Math.sin(orient))+pc[i].lcy);
	ycoords[7]=(int)Math.round((pc[i].lhalfcr*Math.cos(orient)-pc[i].lcr*Math.sin(orient))+pc[i].lcy);
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 8);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = xcoords[j];
		pc[i].lp.ypoints[j] = ycoords[j];
	    }
	    pc[i].lp.invalidate();
	}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr >1){//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fillPolygon(pc[i].p);
		g.translate(-dx, -dy);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
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
	if (pc[i].lcr>1){//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fillPolygon(pc[i].lp);	
		g.translate(-dx, -dy);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
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

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VOctagonOr res=new VOctagonOr(vx,vy,0,vs,color,orient);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
