/*   FILE: VTriangle.java
 *   DATE OF CREATION:   Jul 25 2000
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
 * $Id: VTriangle.java,v 1.10 2006/03/17 17:45:23 epietrig Exp $
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
import net.claribole.zvtm.glyphs.projection.ProjTriangle;

/**
 * Triangle - cannot be reoriented
 * @author Emmanuel Pietriga
 **/

public class VTriangle extends ClosedShape {

    /**vertex x coords*/
    int[] xcoords = new int[3];
    /**vertex y coords*/
    int[] ycoords = new int[3];

    protected static final float halfEdgeFactor=0.866f;
    protected static final float thirdHeightFactor=0.5f;

    /**height in virtual space (equal to bounding circle radius)*/
    long vh;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjTriangle[] pc;

    public VTriangle(){
	vx=0;
	vy=0;
	vz=0;
	vh=10;
	computeSize();
	orient=0;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param h height in virtual space
     *@param c fill color
     */
    public VTriangle(long x,long y,float z,long h,Color c){
	vx=x;
	vy=y;
	vz=z;
	vh=h;
	computeSize();
	orient=0;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param h height in virtual space
     *@param c fill color
     *@param bc border color
     */
    public VTriangle(long x, long y, float z, long h, Color c, Color bc){
	vx=x;
	vy=y;
	vz=z;
	vh=h;
	computeSize();
	orient=0;
	setColor(c);
	setBorderColor(bc);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjTriangle[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjTriangle();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjTriangle[] ta=pc;
		pc=new ProjTriangle[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjTriangle();
	    }
	    else {System.err.println("VTriangle:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjTriangle[1];
		pc[0]=new ProjTriangle();
	    }
	    else {System.err.println("VTriangle:Error while adding camera "+verifIndex);}
	}
    }

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public void removeCamera(int index){
	pc[index]=null;
    }

    /**reset prevMouseIn for all projected coordinates*/
    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
	borderColor = bColor;
    }

    /**get orientation*/
    public float getOrient(){return orient;}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**compute size (bounding circle radius)*/
    void computeSize(){
	size=(float)vh;
    }

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){
	size=radius;
	vh=(long)Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vh=(long)Math.round(size);
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

    /** Method used internally for firing picking-related events.
     *@return Glyph.ENTERED_GLYPH if cursor has entered the glyph, Glyph.EXITED_GLYPH if it has exited the glyph, Glyph.NO_EVENT if nothing has changed (meaning the cursor was already inside or outside it)
     */
    public short mouseInOut(int x,int y,int camIndex){
	if (coordInside(x,y,camIndex)){//if the mouse is inside the glyph
	    if (!pc[camIndex].prevMouseIn){//if it was not inside it last time, mouse has entered the glyph
		pc[camIndex].prevMouseIn=true;
		return Glyph.ENTERED_GLYPH;
	    }
	    else {return Glyph.NO_CURSOR_EVENT;}  //if it was inside last time, nothing has changed
	}
	else{//if the mouse is not inside the glyph
	    if (pc[camIndex].prevMouseIn){//if it was inside it last time, mouse has exited the glyph
		pc[camIndex].prevMouseIn=false;
		return Glyph.EXITED_GLYPH;
	    }
	    else {return Glyph.NO_CURSOR_EVENT;}  //if it was not inside last time, nothing has changed
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
	pc[i].cr=Math.round(vh*coef);
	pc[i].halfEdge=Math.round(halfEdgeFactor*pc[i].cr);
	pc[i].thirdHeight=Math.round(thirdHeightFactor*pc[i].cr);
	xcoords[0] = pc[i].cx;
	ycoords[0] = pc[i].cy-pc[i].cr;
	xcoords[1] = pc[i].cx-pc[i].halfEdge;
	ycoords[1] = pc[i].cy+pc[i].thirdHeight;
	xcoords[2] = pc[i].cx+pc[i].halfEdge;
	ycoords[2] = pc[i].cy+pc[i].thirdHeight;
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 3);
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
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vh*coef);
	pc[i].lhalfEdge=Math.round(halfEdgeFactor*pc[i].lcr);
	pc[i].lthirdHeight=Math.round(thirdHeightFactor*pc[i].lcr);
	xcoords[0] = pc[i].lcx;
	ycoords[0] = pc[i].lcy-pc[i].lcr;
	xcoords[1] = pc[i].lcx-pc[i].lhalfEdge;
	ycoords[1] = pc[i].lcy+pc[i].lthirdHeight;
	xcoords[2] = pc[i].lcx+pc[i].lhalfEdge;
	ycoords[2] = pc[i].lcy+pc[i].lthirdHeight;
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 3);
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
	if (pc[i].cr>1){//repaint only if object is visible
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
	if (pc[i].lcr > 1){//repaint only if object is visible
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
	VTriangle res=new VTriangle(vx, vy, 0, vh, color, borderColor);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
