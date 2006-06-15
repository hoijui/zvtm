/*   FILE: VTriangleOr.java
 *   DATE OF CREATION:   Jul 25 2000
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
 * $Id: VTriangleOr.java,v 1.9 2006/03/17 17:45:23 epietrig Exp $
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
 * Triangle - can be reoriented
 * @author Emmanuel Pietriga
 **/

public class VTriangleOr extends VTriangle implements Cloneable {

    /**vertex x coords*/
    int[] xcoords=new int[3];
    /**vertex y coords*/
    int[] ycoords=new int[3];

    public VTriangleOr(){super();}

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param h height in virtual space
     *@param c fill color
     *@param or orientation
     */
    public VTriangleOr(long x,long y,float z,long h,Color c,float or){
	super(x,y,z,h,c);
	orient=or;
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
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vh*coef);
	pc[i].halfEdge=Math.round(halfEdgeFactor*pc[i].cr);
	pc[i].thirdHeight=Math.round(thirdHeightFactor*pc[i].cr);
	xcoords[0]=(int)Math.round(pc[i].cx-pc[i].cr*Math.sin(orient));
	xcoords[1]=(int)Math.round(pc[i].cx-pc[i].halfEdge*Math.cos(orient)+pc[i].thirdHeight*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].cx+pc[i].halfEdge*Math.cos(orient)+pc[i].thirdHeight*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].cy-pc[i].cr*Math.cos(orient));
	ycoords[1]=(int)Math.round(pc[i].cy+pc[i].thirdHeight*Math.cos(orient)+pc[i].halfEdge*Math.sin(orient));
	ycoords[2]=(int)Math.round(pc[i].cy+pc[i].thirdHeight*Math.cos(orient)-pc[i].halfEdge*Math.sin(orient));
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 3);
	}
	else {
	    pc[i].p.xpoints = xcoords;
	    pc[i].p.ypoints = ycoords;
	    pc[i].p.invalidate();
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
	//project height and construct polygon
	pc[i].lcr=Math.round(vh*coef);
	pc[i].lhalfEdge=Math.round(halfEdgeFactor*pc[i].lcr);
	pc[i].lthirdHeight=Math.round(thirdHeightFactor*pc[i].lcr);
	xcoords[0]=(int)Math.round(pc[i].lcx-pc[i].lcr*Math.sin(orient));
	xcoords[1]=(int)Math.round(pc[i].lcx-pc[i].lhalfEdge*Math.cos(orient)+pc[i].lthirdHeight*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].lcx+pc[i].lhalfEdge*Math.cos(orient)+pc[i].lthirdHeight*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.cos(orient));
	ycoords[1]=(int)Math.round(pc[i].lcy+pc[i].lthirdHeight*Math.cos(orient)+pc[i].lhalfEdge*Math.sin(orient));
	ycoords[2]=(int)Math.round(pc[i].lcy+pc[i].lthirdHeight*Math.cos(orient)-pc[i].lhalfEdge*Math.sin(orient));
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 3);
	}
	else {
	    pc[i].lp.xpoints = xcoords;
	    pc[i].lp.ypoints = ycoords;
	    pc[i].lp.invalidate();
	}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	if (pc[i].cr >1){
	    if (filled){
		g.setColor(this.color);
		g.fillPolygon(pc[i].p);
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
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	if (pc[i].lcr >1){
	    if (filled){
		g.setColor(this.color);
		g.fillPolygon(pc[i].lp);
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
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VTriangleOr res=new VTriangleOr(vx,vy,0,vh,color,orient);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
