/*   FILE: VOctagon.java
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
 * $Id: VOctagon.java,v 1.9 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * Octagon (eight "almost" regular edges) cannot be reoriented
 * @author Emmanuel Pietriga
 **/

public class VOctagon extends Glyph implements Cloneable {

    /**height=width in virtual space*/
    long vs;

    ProjOctagon[] pc;

    public VOctagon(){
	vx=0;
	vy=0;
	vz=0;
	vs=10;
	computeSize();
	orient=0;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param c fill color
     */
    public VOctagon(long x,long y,float z,long s,Color c){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	computeSize();
	orient=0;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjOctagon[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjOctagon();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjOctagon[] ta=pc;
		pc=new ProjOctagon[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjOctagon();
	    }
	    else {System.err.println("VOctagon:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjOctagon[1];
		pc[0]=new ProjOctagon();
	    }
	    else {System.err.println("VOctagon:Error while adding camera "+verifIndex);}
	}
    }

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public void removeCamera(int index){
	pc[index]=null;
    }

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
    }

    /**get orientation*/
    public float getOrient(){return orient;}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**compute size (bounding circle radius)*/
    void computeSize(){
	size=(float)vs*1.118f;
    }

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){
	size=radius;
	vs=Math.round(size/1.118f);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size/1.118f);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if (pc[camIndex].p.contains(x,y)){return true;}
	else {return false;}
    }

    /**returns 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)*/
    public int mouseInOut(int x,int y,int camIndex){
	if (coordInside(x,y,camIndex)){//if the mouse is inside the glyph
	    if (!pc[camIndex].prevMouseIn){//if it was not inside it last time, mouse has entered the glyph
		pc[camIndex].prevMouseIn=true;
		return 1;
	    }
	    else {return 0;}  //if it was inside last time, nothing has changed
	}
	else{//if the mouse is not inside the glyph
	    if (pc[camIndex].prevMouseIn){//if it was inside it last time, mouse has exited the glyph
		pc[camIndex].prevMouseIn=false;
		return -1;
	    }
	    else {return 0;}  //if it was not inside last time, nothing has changed
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
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	pc[i].halfcr=pc[i].cr/2;
	int[] xcoords={pc[i].cx+pc[i].cr,pc[i].cx+pc[i].halfcr,pc[i].cx-pc[i].halfcr,pc[i].cx-pc[i].cr,pc[i].cx-pc[i].cr,pc[i].cx-pc[i].halfcr,pc[i].cx+pc[i].halfcr,pc[i].cx+pc[i].cr};
	int[] ycoords={pc[i].cy-pc[i].halfcr,pc[i].cy-pc[i].cr,pc[i].cy-pc[i].cr,pc[i].cy-pc[i].halfcr,pc[i].cy+pc[i].halfcr,pc[i].cy+pc[i].cr,pc[i].cy+pc[i].cr,pc[i].cy+pc[i].halfcr};
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 8);
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
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vs*coef);
	pc[i].lhalfcr=pc[i].cr/2;
	int[] xcoords={pc[i].lcx+pc[i].lcr,
		       pc[i].lcx+pc[i].lhalfcr,
		       pc[i].lcx-pc[i].lhalfcr,
		       pc[i].lcx-pc[i].lcr,
		       pc[i].lcx-pc[i].lcr,
		       pc[i].lcx-pc[i].lhalfcr,
		       pc[i].lcx+pc[i].lhalfcr,
		       pc[i].lcx+pc[i].lcr};
	int[] ycoords={pc[i].lcy-pc[i].lhalfcr,
		       pc[i].lcy-pc[i].lcr,
		       pc[i].lcy-pc[i].lcr,
		       pc[i].lcy-pc[i].lhalfcr,
		       pc[i].lcy+pc[i].lhalfcr,
		       pc[i].lcy+pc[i].lcr,
		       pc[i].lcy+pc[i].lcr,
		       pc[i].lcy+pc[i].lhalfcr};
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 8);
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
	if (pc[i].lcr >1){//repaint only if object is visible
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
	VOctagon res=new VOctagon(vx,vy,0,vs,color);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}

