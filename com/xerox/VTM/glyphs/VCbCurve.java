/*   FILE: VCbCurve.java
 *   DATE OF CREATION:   Oct 03 2001
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
 * $Id: VCbCurve.java,v 1.8 2006/03/17 17:45:22 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;
import net.claribole.zvtm.glyphs.projection.ProjCbCurve;


/**
 * Cubic Curve -  can be reoriented -  CANNOT DETECT ENTRY/EXIT in curves, even when filled (they look as, but are not, closed shapes) <br> a cubic curve is a curved segment that has two endpoints and two control points. Each control point determines the shape of the curve by controlling one of the endpoint tangent vectors. <br> for this particular glyph, vx and vy correspond to the center of the imaginary segment linking the curve's start and end points <br> the coordinates of the control points are expressed respectively w.r.t start and end points in polar coordinates (orient=0 on segment linking start and end points, meaning that if orient=0 for both control points, start control1 control2 and end points are aligned) 
 * @author Emmanuel Pietriga
 **/

public class VCbCurve extends Glyph {

    /**size (distance between start and end point)*/
    long vs;

    /**control points, polar coordinates - origin is vx,vy - orient=0 on segment linking start and end points*/
    long vrad1;
    float ang1;
    long vrad2;
    float ang2;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjCbCurve[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (distance between start and end points) in virtual space
     *@param c color
     *@param or orientation
     *@param ctrlDist1 distance of control point (polar coords origin=start point)
     *@param or1 orientation of control point (polar coords origin=start point)
     *@param ctrlDist2 distance of control point (polar coords origin=end point)
     *@param or2 orientation of control point (polar coords origin=end point)
     */
    public VCbCurve(long x,long y,float z,long s,Color c,float or,long ctrlDist1,float or1,long ctrlDist2,float or2){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	sensit=false;
	orient=or;
	vrad1=ctrlDist1;
	ang1=or1;
	vrad2=ctrlDist2;
	ang2=or2;
	computeSize();
	setColor(c);
    }

    /**set position of control point 1 (polar coords w.r.t start point)*/
    public void setCtrlPoint1(long d,float o){
	vrad1=d;
	ang1=o;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**set position of control point 2 (polar coords w.r.t end point)*/
    public void setCtrlPoint2(long d,float o){
	vrad2=d;
	ang2=o;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get distance from start point to control point 1 (polar coords)*/
    public long getCtrlPointRadius1(){return vrad1;}

    /**get orientation of control point 1 (polar coords)*/
    public float getCtrlPointAngle1(){return ang1;}

    /**get distance from start point to control point 2 (polar coords)*/
    public long getCtrlPointRadius2(){return vrad2;}

    /**get orientation of control point 2 (polar coords)*/
    public float getCtrlPointAngle2(){return ang2;}

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjCbCurve[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjCbCurve();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjCbCurve[] ta=pc;
		pc=new ProjCbCurve[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjCbCurve();
	    }
	    else {System.err.println("VCbCurve:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjCbCurve[1];
		pc[0]=new ProjCbCurve();
	    }
	    else {System.err.println("VCbCurve:Error while adding camera "+verifIndex);}
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
    }

    /**get orientation*/
    public float getOrient(){return orient;}

    /**set orientation (absolute)*/
    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**compute curve from VTM object model*/
    void computeSize(){
	size=(float)vs;
    }

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){
	vrad1=Math.round(vrad1*radius/size);
	vrad2=Math.round(vrad2*radius/size);
	size=radius;
	vs=Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	vrad1=Math.round(vrad1*factor);
	vrad2=Math.round(vrad2*factor);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    /** Method used internally for firing picking-related events.
     *@return VCurcor.ENTERED_GLYPH if cursor has entered the glyph, VCurcor.EXITED_GLYPH if it has exited the glyph, VCursor.NO_CURSOR_EVENT if nothing has changed (meaning the cursor was already inside or outside it)
     */
    public short mouseInOut(int x,int y,int camIndex){
	return Glyph.NO_CURSOR_EVENT;
    }

    /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct curve
	pc[i].cr=Math.round(vs*coef);
	if (pc[i].cr>1){
	    pc[i].start.setLocation(pc[i].cx+pc[i].cr*Math.cos(orient),pc[i].cy+pc[i].cr*Math.sin(orient));
	    pc[i].end.setLocation(pc[i].cx-pc[i].cr*Math.cos(orient),pc[i].cy-pc[i].cr*Math.sin(orient));
	    
	    pc[i].ctrlStart.setLocation(pc[i].start.x+(int)Math.round(coef*vrad1*Math.cos(orient-ang1)),pc[i].start.y+(int)Math.round(coef*vrad1*Math.sin(orient-ang1)));
	    pc[i].ctrlEnd.setLocation(pc[i].end.x+(int)Math.round(coef*vrad2*Math.cos(orient-ang2)),pc[i].end.y+(int)Math.round(coef*vrad2*Math.sin(orient-ang2)));
	    pc[i].quad.setCurve(pc[i].start,pc[i].ctrlStart,pc[i].ctrlEnd,pc[i].end);
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
	//project height and construct curve
	pc[i].lcr=Math.round(vs*coef);
	if (pc[i].lcr>1){
	    pc[i].lstart.setLocation(pc[i].lcx+pc[i].lcr*Math.cos(orient),pc[i].lcy+pc[i].lcr*Math.sin(orient));
	    pc[i].lend.setLocation(pc[i].lcx-pc[i].lcr*Math.cos(orient),pc[i].lcy-pc[i].lcr*Math.sin(orient));
	    
	    pc[i].lctrlStart.setLocation(pc[i].lstart.x+(int)Math.round(coef*vrad1*Math.cos(orient-ang1)),pc[i].lstart.y+(int)Math.round(coef*vrad1*Math.sin(orient-ang1)));
	    pc[i].lctrlEnd.setLocation(pc[i].lend.x+(int)Math.round(coef*vrad2*Math.cos(orient-ang2)),pc[i].lend.y+(int)Math.round(coef*vrad2*Math.sin(orient-ang2)));
	    pc[i].lquad.setCurve(pc[i].lstart,pc[i].lctrlStart,pc[i].lctrlEnd,pc[i].lend);
	}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr >1){//repaint only if object is visible
	    g.setColor(this.color);
	    if (stroke!=null) {
		g.setStroke(stroke);
		g.translate(dx,dy);
		g.draw(pc[i].quad);
		g.translate(-dx,-dy);
		g.setStroke(stdS);
	    }
	    else {
		g.translate(dx,dy);
		g.draw(pc[i].quad);
		g.translate(-dx,-dy);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].lcr >1){//repaint only if object is visible
	    g.setColor(this.color);
	    if (stroke!=null) {
		g.setStroke(stroke);
		g.translate(dx,dy);
		g.draw(pc[i].lquad);
		g.translate(-dx,-dy);
		g.setStroke(stdS);
	    }
	    else {
		g.translate(dx,dy);
		g.draw(pc[i].lquad);
		g.translate(-dx,-dy);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VCbCurve res = new VCbCurve(vx,vy,0,vs,color,orient,vrad1,ang1,vrad2,ang2);
	res.mouseInsideColor = this.mouseInsideColor;
	return res;
    }

    /** Highlight this glyph to give visual feedback when the cursor is inside it. */
    public void highlight(boolean b, Color selectedColor){}

}

