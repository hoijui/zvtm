/*   FILE: VRectangleOr.java
 *   DATE OF CREATION:   Jul 24 2000
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
 * Rectangle - can be reoriented
 * @author Emmanuel Pietriga
 **/

public class VRectangleOr extends VRectangle implements Cloneable {

    public VRectangleOr(){
	super();
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
    public VRectangleOr(long x,long y,float z,long w,long h,Color c,float or){
	super(x,y,z,w,h,c);
	orient=or;
    }

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
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
	if (orient==0){
	    if ((w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	    else {return false;}
	}
	else {
	    if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	    else {return false;}
	}
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if (orient==0){
	    if ((x>=(pc[camIndex].cx-pc[camIndex].cw)) && (x<=(pc[camIndex].cx+pc[camIndex].cw)) && (y>=(pc[camIndex].cy-pc[camIndex].ch)) && (y<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
	    else {return false;}
	}
	else {
	    if (pc[camIndex].p.contains(x,y)){return true;}
	    else {return false;}
	}
    }

    /**project shape in camera coord sys prior to actual painting*/
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
	    int[] xcoords={(int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx),(int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx),(int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx),(int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx)};
	    int[] ycoords={(int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy),(int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy),(int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy),(int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy)};	
	    pc[i].p=new Polygon(xcoords,ycoords,4);
	}
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
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
	    int[] xcoords={(int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx),
			   (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx),
			   (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx),
			   (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx)};
	    int[] ycoords={(int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy),
			   (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy),
			   (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy),
			   (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy)};	
	    pc[i].lp=new Polygon(xcoords,ycoords,4);
	}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	if ((pc[i].cw>1) && (pc[i].ch>1)){//repaint only if object is visible
	    g.setColor(this.color);
	    if (orient==0) {
		if (filled){g.fillRect(pc[i].cx-pc[i].cw,pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);}
		g.setColor(borderColor);
		if (paintBorder){
		    if (stroke!=null) {
			if (((pc[i].cx-pc[i].cw)>0) || ((pc[i].cy-pc[i].ch)>0) || ((2*pc[i].cw-1)<vW) || ((2*pc[i].ch-1)<vH)){
			    g.setStroke(stroke);
			    g.drawRect(pc[i].cx-pc[i].cw,pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
			    g.setStroke(stdS);
			}
		    }
		    else {
			g.drawRect(pc[i].cx-pc[i].cw,pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
		    }
		}
	    }
	    else {
		if (filled){g.fillPolygon(pc[i].p);}
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
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	if ((pc[i].lcw>1) && (pc[i].lch>1)){//repaint only if object is visible
	    g.setColor(this.color);
	    if (orient==0) {
		if (filled){g.fillRect(pc[i].lcx-pc[i].lcw,pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);}
		g.setColor(borderColor);
		if (paintBorder){
		    if (stroke!=null) {
			if (((pc[i].lcx-pc[i].lcw)>0) || ((pc[i].lcy-pc[i].lch)>0) || ((2*pc[i].lcw-1)<vW) || ((2*pc[i].lch-1)<vH)){
			    g.setStroke(stroke);
			    g.drawRect(pc[i].lcx-pc[i].lcw,pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
			    g.setStroke(stdS);
			}
		    }
		    else {
			g.drawRect(pc[i].lcx-pc[i].lcw,pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
		    }
		}
	    }
	    else {
		if (filled){g.fillPolygon(pc[i].lp);}
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
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VRectangleOr res=new VRectangleOr(vx,vy,0,vw,vh,color,orient);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
