/*   FILE: VQdCurve.java
 *   DATE OF CREATION:   Oct 02 2001
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
 * $Id: VQdCurve.java,v 1.8 2006/03/17 17:45:23 epietrig Exp $
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

/**
 * Quadratic Curve -  can be reoriented -  CANNOT DETECT ENTRY/EXIT in curves, even when filled (they look as, but are not, closed shapes) <br> a quadratic curve is a curved segment that has two endpoints and one control point. The control point determines the shape of the curve by controlling both of the endpoint tangent vectors <br> for this particular glyph, vx and vy correspond to the center of the imaginary segment linking the curve's start and end points <br> the coordinates of the control point are expressed w.r.t this point in polar coordinates (orient=0 on segment linking start and end points, meaning that if orient=0, start control and end points are aligned) 
 * @author Emmanuel Pietriga
 **/

public class VQdCurve extends Glyph implements Cloneable {

    /**size (distance between start and end point)*/
    long vs;

    /**control point, polar coordinates - origin is vx,vy - orient=0 on segment linking start and end points*/
    long vrad;
    float ang;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjQdCurve[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param or orientation
     *@param ctrlDist1 distance of control point (polar coords origin=(x,y) provided in this constructor)
     *@param or1 orientation of control point (polar coords origin=(x,y) provided in this constructor)
     */
    public VQdCurve(long x,long y,float z,long s,Color c,float or,long ctrlDist1,float or1){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	sensit=false;
	orient=or;
	vrad=ctrlDist1;
	ang=or1;
	computeSize();
	filled=false;
	setColor(c);
	setBorderColor(bColor);
    }

    /**set position of control point (polar coords w.r.t center of segment linking start and end points)*/
    public void setCtrlPoint(long d,float o){
	vrad=d;
	ang=o;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get distance from center of segment linking start and end points to control point (polar coords)*/
    public long getCtrlPointRadius(){return vrad;}

    /**get orientation of control point (polar coords)*/
    public float getCtrlPointAngle(){return ang;}

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjQdCurve[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjQdCurve();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjQdCurve[] ta=pc;
		pc=new ProjQdCurve[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjQdCurve();
	    }
	    else {System.err.println("VQdCurve:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjQdCurve[1];
		pc[0]=new ProjQdCurve();
	    }
	    else {System.err.println("VQdCurve:Error while adding camera "+verifIndex);}
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
	vrad=Math.round(vrad*radius/size);
	size=radius;
	vs=Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	vrad=Math.round(vrad*factor);
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

    /**returns 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)*/
    public int mouseInOut(int x,int y,int camIndex){
	return 0;
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
	    pc[i].ctrl.setLocation(pc[i].cx+(int)Math.round(coef*vrad*Math.cos(orient-ang)),pc[i].cy+(int)Math.round(coef*vrad*Math.sin(orient-ang)));
	    pc[i].quad.setCurve(pc[i].start,pc[i].ctrl,pc[i].end);
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
	    pc[i].lctrl.setLocation(pc[i].lcx+(int)Math.round(coef*vrad*Math.cos(orient-ang)),pc[i].lcy+(int)Math.round(coef*vrad*Math.sin(orient-ang)));
	    pc[i].lquad.setCurve(pc[i].lstart,pc[i].lctrl,pc[i].lend);
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
		g.fill(pc[i].quad);
		g.setColor(borderColor);
		g.drawLine((int)pc[i].start.x,(int)pc[i].start.y,(int)pc[i].end.x,(int)pc[i].end.y);
		g.translate(-dx, -dy);
	    }
	    else {//if not filled (common case), paint curve with main color, not border color
		g.setColor(this.color);
		g.translate(dx, dy);
		g.draw(pc[i].quad);
		g.translate(-dx, -dy);
	    }
	    if (stroke!=null) {
		g.setStroke(stroke);
		g.translate(dx, dy);
		g.draw(pc[i].quad);
		g.translate(-dx, -dy);
		g.setStroke(stdS);
	    }
	    else {
		g.translate(dx, dy);
		g.draw(pc[i].quad);
		g.translate(-dx, -dy);
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
		g.fill(pc[i].lquad);
		g.setColor(borderColor);
		g.drawLine((int)pc[i].lstart.x,(int)pc[i].lstart.y,(int)pc[i].lend.x,(int)pc[i].lend.y);
		g.translate(-dx, -dy);
	    }
	    else {//if not filled (common case), paint curve with main color, not border color
		g.setColor(this.color);
		g.draw(pc[i].lquad);
	    }
	    if (stroke!=null) {
		g.setStroke(stroke);
		g.translate(dx, dy);
		g.draw(pc[i].lquad);
		g.translate(-dx, -dy);
		g.setStroke(stdS);
	    }
	    else {
		g.translate(dx, dy);
		g.draw(pc[i].lquad);
		g.translate(-dx, -dy);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VQdCurve res=new VQdCurve(vx,vy,0,vs,color,orient,vrad,ang);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}

