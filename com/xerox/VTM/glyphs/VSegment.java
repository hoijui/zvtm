/*   FILE: VSegment.java
 *   DATE OF CREATION:   Jul 20 2000
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
 * $Id: VSegment.java,v 1.9 2005/12/05 16:16:24 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Polygon;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.lens.Lens;

/**
 * Segment
 * @author Emmanuel Pietriga
 **/

public class VSegment extends Glyph implements RectangularShape,Cloneable {

    /**half width and height in virtual space*/
    long vw,vh;

    RProjectedCoords[] pc;

    public VSegment(){
	vx=0;
	vy=0;
	vz=0;
	orient=0;
	size=10;
	computeEdges();
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *give the centre of segment and half its projected length on X & Y axis
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space (can be negative)
     *@param h half height in virtual space (can be negative)
     *@param c fill color
     */
    public VSegment(long x,long y,float z,long w,long h,Color c){
	vx=x;
	vy=y;
	vz=z;
	vw=w;
	vh=h;
	computeSize();
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *give the end points of segment
     *@param x1 coordinate of endpoint 1 in virtual space
     *@param y1 coordinate of endpoint 1 in virtual space
     *@param x2 coordinate of endpoint 2 in virtual space
     *@param y2 coordinate of endpoint 2 in virtual space
     *@param z altitude
     *@param c fill color
     */
    public VSegment(long x1, long y1, float z, Color c, long x2, long y2){
	vx = (x1 + x2) / 2;
	vy = (y1 + y2) / 2;
	vz = z;
	vw = (x1 - x2) / 2;
	vh = (y2 - y1) / 2;
	computeSize();
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *give the centre of segment and half its length & orient
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param lgth half length in virtual space
     *@param angle orientation
     *@param c fill color
     */
    public VSegment(long x,long y,float z,float lgth,float angle,Color c){
	vx=x;
	vy=y;
	vz=z;
	orient=angle;
	size=lgth;
	computeEdges();
	setColor(c);
	setBorderColor(Color.black);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new RProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoords();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		RProjectedCoords[] ta=pc;
		pc=new RProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new RProjectedCoords();
	    }
	    else {System.err.println("VSegment:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoords[1];
		pc[0]=new RProjectedCoords();
	    }
	    else {System.err.println("VSegment:Error while adding camera "+verifIndex);}
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
	//System.err.println("VSegment orientation is not working properly...");
	orient=angle;
	computeEdges();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**change the segment's location, size and orientation by giving its two endpoints*/
    public void setEndPoints(long x1, long y1, long x2, long y2){
	vx = (x1 + x2) / 2;
	vy = (y1 + y2) / 2;
	vw = (x1 - x2) / 2;
	vh = (y2 - y1) / 2;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**get the segment's two endpoints*/
    public LongPoint[] getEndPoints(){
	LongPoint[] res = new LongPoint[2];
	res[0] = new LongPoint(vx+vw, vy-vh);
	res[1] = new LongPoint(vx-vw, vy+vh);
	return res;
    }
    
    /**computes size and orientation of segment*/
    void computeSize(){  
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
	if (vw!=0){orient=(float)Math.atan((vh/(float)vw));}
	else {
	    orient=(vh>0) ? (float)Math.PI/2.0f : (float)-Math.PI/2.0f ;
	}
	if (orient<0){
	    if (vh>0){orient=(float)Math.PI-orient;} 
	    else {orient=-orient;}
	}
	else if(orient>0){
	    if (vh>0){orient=2*(float)Math.PI-orient;}
	    else {orient=(float)Math.PI-orient;}
	}
	else if(orient==0 && vw<0){orient=(float)Math.PI;}	    
    }

    /**get half width*/
    public long getWidth(){return vw;}

    /**get half height*/
    public long getHeight(){return vh;}

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){
	size=radius;
	computeEdges();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	computeEdges();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**set absolute half width*/
    public void setWidth(long w){ 
	vw=w;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**set absolute half height*/
    public void setHeight(long h){
	vh=h;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void setWidthHeight(long w,long h){
	this.vw=w;
	this.vh=h;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    void computeEdges(){
	vw=(long)Math.round(size*Math.cos(orient));
	vh=(long)Math.round(size*Math.sin(orient));
    }

    /**
     *always returns false (can never be inside a segment)
     */
    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    /**detects whether the given point lies on the segment or not (default tolerance of 2 pixels)
     *@param x EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in AppEventHandler's mouse methods as jpx)
     *@param y EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in AppEventHandler's mouse methods as jpy)
     *@param camIndex camera index (obtained through Camera.getIndex())
     */
    public boolean intersects(int x, int y, int camIndex){
	return intersects(x, y, 2, camIndex);
    }

    /**detects whether the given point lies on the segment or not
     *@param x EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in AppEventHandler's mouse methods as jpx)
     *@param y EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in AppEventHandler's mouse methods as jpy)
     *@param tolerance the segment's thickness in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@param camIndex camera index (obtained through Camera.getIndex())
     */
    public boolean intersects(int x, int y, int tolerance, int camIndex){
	LongPoint[] endPoints = this.getEndPoints();
	double a = (endPoints[1].y - endPoints[0].y) / ((float)(endPoints[1].x - endPoints[0].x));
	if (Math.abs(a) > 1){
	    // slope is superior to 45 deg, expand segment thickness on X axis
	    int[] xcoords = {pc[camIndex].cx-pc[camIndex].cw+tolerance,
			     pc[camIndex].cx+pc[camIndex].cw+tolerance,
			     pc[camIndex].cx+pc[camIndex].cw-tolerance,
			     pc[camIndex].cx-pc[camIndex].cw-tolerance};
	    int[] ycoords = {pc[camIndex].cy-pc[camIndex].ch,
			     pc[camIndex].cy+pc[camIndex].ch,
			     pc[camIndex].cy+pc[camIndex].ch,
			     pc[camIndex].cy-pc[camIndex].ch};
	    Polygon p = new Polygon(xcoords, ycoords, 4);
	    return p.contains(x, y);
	}
	else {
	    // slope is inferior to 45 deg, expand segment thickness on Y axis
	    int[] xcoords = {pc[camIndex].cx-pc[camIndex].cw,
			     pc[camIndex].cx+pc[camIndex].cw,
			     pc[camIndex].cx+pc[camIndex].cw,
			     pc[camIndex].cx-pc[camIndex].cw};
	    int[] ycoords = {pc[camIndex].cy-pc[camIndex].ch+tolerance,
			     pc[camIndex].cy+pc[camIndex].ch+tolerance,
			     pc[camIndex].cy+pc[camIndex].ch-tolerance,
			     pc[camIndex].cy-pc[camIndex].ch-tolerance};
	    Polygon p = new Polygon(xcoords, ycoords, 4);
	    return p.contains(x, y);
	}
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
	//project width and height
	pc[i].cw=Math.round(vw*coef);
	pc[i].ch=Math.round(vh*coef);
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	pc[i].lcw = Math.round(vw*coef);
	pc[i].lch = Math.round(vh*coef);
    }
    
    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy+pc[i].ch);
	    g.setStroke(stdS);
	}
	else {
	    g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy+pc[i].ch);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy+pc[i].lch);
	    g.setStroke(stdS);
	}
	else {
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy+pc[i].lch);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VSegment res=new VSegment(vx,vy,0,vw,vh,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
