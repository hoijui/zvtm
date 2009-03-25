/*   FILE: VRectangleOr.java
 *   DATE OF CREATION:   Jul 24 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;

/**
 * Rectangle. This version is less efficient than VRectangle, but it can be reoriented. It cannot be made translucent (see VRectangle*ST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VRectangle
 *@see com.xerox.VTM.glyphs.VRectangleOrST
 *@see com.xerox.VTM.glyphs.VRectangleST
 */

public class VRectangleOr extends VRectangle {

    /*vertex x coords*/
    int[] xcoords = new int[4];
    /*vertex y coords*/
    int[] ycoords = new int[4];

    public VRectangleOr(){
	super();
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
    public VRectangleOr(long x,long y, int z,long w,long h,Color c,float or){
	super(x,y,z,w,h,c);
	orient=or;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param bc border color
     *@param or orientation
     */
    public VRectangleOr(long x, long y, int z, long w, long h, Color c, Color bc, float or){
	super(x, y, z, w, h, c);
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
	if (orient==0){
	    if ((w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	    else {return false;}
	}
	else {
	    if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	    else {return false;}
	}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		if (orient == 0){
    		return dvs.intersects(vx-vw, vy-vh, 2*vw, 2*vh);
		}
		else {
    		return pc[camIndex].p.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);		    
		}
	}

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (orient==0){
            if ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
                (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
            else {return false;}
        }
        else {
            if (pc[camIndex].p.contains(jpx, jpy)){return true;}
            else {return false;}
        }
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project width and height
	pc[i].cw=Math.round(vw*coef);
	pc[i].ch=Math.round(vh*coef);
	if (orient!=0){
	    float x1=-pc[i].cw;
	    float y1=-pc[i].ch;
	    float x2=pc[i].cw;
	    float y2=pc[i].ch;
	    xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	    ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
	    xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	    ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	    xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	    ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	    xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	    ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
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
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project width and height
	pc[i].lcw=Math.round(vw*coef);
	pc[i].lch=Math.round(vh*coef);
	if (orient!=0){
	    float x1=-pc[i].lcw;
	    float y1=-pc[i].lch;
	    float x2=pc[i].lcw;
	    float y2=pc[i].lch;
	    xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	    ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
	    xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	    ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	    xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	    ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	    xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	    ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
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
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)){//repaint only if object is visible
	    if (orient==0) {
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
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
			    g.setStroke(stdS);
 			}
		    }
		    else {
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
		    }
		}
	    }
	    else {
		g.translate(dx, dy);
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
		g.translate(-dx, -dy);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)){//repaint only if object is visible
	    if (orient==0) {
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
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
			    g.setStroke(stdS);
			}
		    }
		    else {
			g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
		    }
		}
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
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	VRectangleOr res=new VRectangleOr(vx, vy, 0, vw, vh, color, borderColor, orient);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
