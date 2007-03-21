/*   FILE: VPath.java
 *   DATE OF CREATION:   Oct 12 2001
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
 * $Id: VPath.java,v 1.6 2005/12/08 09:08:21 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.lens.Lens;

/**
 * General path - cannot be resized nor reoriented (for now) - does not follow the standard object model - (vx,vy) are the coordinates of the path's first point - can detect mouse overlapping path by explicitely calling VCursor.interesctsPath(VPath p), but no event is automatically fired by the event handler when the mouse is above a VPath (if you really want to do that, you can call VCursor.getIntersectingPaths() in mouseMoved in your event handler, but this probably will be time consuming)
 * @author Emmanuel Pietriga
 **/

public class VPath extends Glyph {

    //creates a copy of a VPath, with offset (x,y)
    public static VPath duplicateVPath(VPath p,long x,long y){
	if (p!=null){
	    VPath res=new VPath(0,0,0,p.getColor());
	    PathIterator pi=p.getJava2DPathIterator();
	    double[] cds=new double[6];
	    int type;
	    while (!pi.isDone()){
		type=pi.currentSegment(cds);
		switch (type){
		case PathIterator.SEG_CUBICTO:{
		    res.addCbCurve((long)cds[4]+x,(long)-cds[5]+y,(long)cds[0]+x,(long)-cds[1]+y,(long)cds[2]+x,(long)-cds[3]+y,true);
		    break;
		}
		case PathIterator.SEG_QUADTO:{
		    res.addQdCurve((long)cds[2]+x,(long)-cds[3]+y,(long)cds[0]+x,(long)-cds[1]+y,true);
		    break;
		}
		case PathIterator.SEG_LINETO:{
		    res.addSegment((long)cds[0]+x,(long)-cds[1]+y,true);
		    break;
		}
		case PathIterator.SEG_MOVETO:{
		    res.jump((long)cds[0]+x,(long)-cds[1]+y,true);
		    break;
		}
		}
		pi.next();
	    }
	    return res;
	}
	else return null;
    }
    
    AffineTransform at;
    
    ProjectedCoords[] pc;
    
    GeneralPath path;
    
    LongPoint lp;  //last point
    
    public LongPoint realHotSpot;
    float drawingRadius;
    float drawingFactor=1.2f;
    boolean forcedDrawing=false;

    public VPath(){
	vx=0;
	vy=0;
	vz=0;
	setColor(Color.black);
	sensit=false;
	resetPath();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c color
     */
    public VPath(long x,long y,float z,Color c){
	vx=x;
	vy=y;
	vz=z;
	sensit=false;
	setColor(c);
	path=new GeneralPath();
	lp=new LongPoint(vx,vy);
	realHotSpot=new LongPoint(vx,vy);
	path.moveTo(vx,-vy);
	computeSize();
    }

    /**
     *@param z altitude
     *@param c color
     *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
     */
    public VPath(float z,Color c,String svg){
	vx=0;
	vy=0;
	vz=z;
	sensit=false;
	setColor(c);
	this.setSVGPath(svg);
    }

    /**
     *reset path and assign it new coordinatea ccording to what is specified in string svg
     *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
     */
    public void setSVGPath(String svg){
	resetPath();
	SVGReader.createPath(svg,this);
    }

    /**
     * new path, begins at (vx,vy)
     */
    public void resetPath(){
	path=new GeneralPath();
	lp=new LongPoint(0,0);
	realHotSpot=new LongPoint(0,0);
	path.moveTo(0,0);
	computeSize();
    }
    
    /**
     * add a new segment to the path, from current point to point (x,y)
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param abs true if absolute coordinates, false if relative coordinates (w.r.t last point)
     */
    public void addSegment(long x,long y,boolean abs){
	if (abs){lp.setLocation(x,y);}
	else {lp.translate(x,y);}
	path.lineTo(lp.x,-lp.y);
	realHotSpot.setLocation((vx+lp.x)/2,(vy+lp.y)/2);
	computeSize();
    }

    /**
     * add a new quadratic curve to the path, from current point to point (x,y), with control point (x1,y1)
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param x1 coordinate in virtual space
     *@param y1 coordinate in virtual space
     *@param abs true if absolute coordinates, false if relative coordinates (w.r.t last point)
     */
    public void addQdCurve(long x,long y,long x1,long y1,boolean abs){
	if (abs){
	    path.quadTo(x1,-y1,x,-y);
	    lp.setLocation(x,y);


	}
	else {
	    path.quadTo(lp.x+x1,-(lp.y+y1),lp.x+x,-(lp.y+y));
	    lp.translate(x,y);
	}
	realHotSpot.setLocation((vx+lp.x)/2,(vy+lp.y)/2);
	computeSize();

    }

    /**
     * add a new cubic curve to the path, from current point to point (x,y), with control points (x1,y1) and (x2,y2)
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param x1 coordinate in virtual space
     *@param y1 coordinate in virtual space
     *@param x2 coordinate in virtual space
     *@param y2 coordinate in virtual space
     *@param abs true if absolute coordinates, false if relative coordinates (w.r.t last point)
     */
    public void addCbCurve(long x,long y,long x1,long y1,long x2,long y2,boolean abs){
	if (abs){
	    path.curveTo(x1,-y1,x2,-y2,x,-y);
	    lp.setLocation(x,y);


	}
	else {
	    path.curveTo(lp.x+x1,-(lp.y+y1),lp.x+x2,-(lp.y+y2),lp.x+x,-(lp.y+y));
	    lp.translate(x,y);
	}
	realHotSpot.setLocation((vx+lp.x)/2,(vy+lp.y)/2);
	computeSize();
    }

    /**
     * "jump" to point (x,y) without drawing anything
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param abs true if absolute coordinates, false if relative coordinates (w.r.t last point)
     */
    public void jump(long x,long y, boolean abs){
	if (abs){lp.setLocation(x,y);}
	else {lp.translate(x,y);}
	path.moveTo(lp.x,-lp.y);
	if (getPathLength()==1){vx=lp.x;vy=lp.y;}
	realHotSpot.setLocation((vx+lp.x)/2,(vy+lp.y)/2);
	computeSize();
    }

    int getPathLength(){
	int res=0;
	PathIterator pi=path.getPathIterator(null);
	while (!pi.isDone()){res++;pi.next();}
	return res;
    }

    void computeSize(){
	size=(float)Math.sqrt(Math.pow((lp.x-vx)/2,2)+Math.pow((lp.y-vy)/2,2));
	drawingRadius=size*drawingFactor;
    }
    
    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjectedCoords();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjectedCoords[] ta=pc;
		pc=new ProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjectedCoords[1];
		pc[0]=new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
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

    /**set size (absolute) - has no effect*/
    public void sizeTo(float factor){}

    /**set size (relative) - has no effect*/
    public void reSize(float factor){}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (returns distance between first and last point)*/
    public float getSize(){
	return size;
    }

    /**get orientation*/
    public float getOrient(){return orient;}

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
     *@return Glyph.ENTERED_GLYPH if cursor has entered the glyph, Glyph.EXITED_GLYPH if it has exited the glyph, Glyph.NO_EVENT if nothing has changed (meaning the cursor was already inside or outside it)
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
	pc[i].cx=(d.width/2)+Math.round((-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((-c.posy)*coef);
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx=(lensWidth/2)+Math.round((-lensx)*coef);
	pc[i].lcy=(lensHeight/2)-Math.round((-lensy)*coef);
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
// 	if (true){//replace by something using projected size (so that we do not paint it if too small)
 	    at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
	    at.preConcatenate(stdT);
 	    at.concatenate(AffineTransform.getScaleInstance(coef,coef));
	    g.setTransform(at);
	    if (stroke!=null){
		g.setStroke(stroke);
		g.draw(path);
		g.setStroke(stdS);
	    }
	    else {
		g.draw(path);
	    }
	    g.setTransform(stdT);
// 	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
// 	if (true){//replace by something using projected size (so that we do not paint it if too small)
 	    at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);
 	    at.concatenate(AffineTransform.getScaleInstance(coef,coef));
	    g.setTransform(at);
	    if (stroke!=null){
		g.setStroke(stroke);
		g.draw(path);
		g.setStroke(stdS);
	    }
	    else {
		g.draw(path);
	    }
	    g.setTransform(stdT);
// 	}
    }

    /**used to find out if it is necessary to project and draw the glyph in the current view or through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	boolean res;
	if (forcedDrawing){res=true;}
	else {
	    if ((realHotSpot.x>=wb) && (realHotSpot.x<=eb) && (realHotSpot.y>=sb) && (realHotSpot.y<=nb)){
		// if glyph hotspot is in the region, it is obviously visible
		res=true;
	    }
	    else {
		if (((realHotSpot.x-drawingRadius)<=eb) && ((realHotSpot.x+drawingRadius)>=wb)
		    && ((realHotSpot.y-drawingRadius)<=nb) && ((realHotSpot.y+drawingRadius)>=sb)){
		    // if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    res=true; // glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
		else res=false; // otherwise the glyph is not visible
	    }
	}
	return res;
    }

    /**used to find out if it is necessary to project and draw the glyph through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	boolean res;
	if (forcedDrawing){return true;}
	else if ((realHotSpot.x>=wb) && (realHotSpot.x<=eb) && (realHotSpot.y>=sb) && (realHotSpot.y<=nb)
		 && ((realHotSpot.x+drawingRadius)<=eb) && ((realHotSpot.x-drawingRadius)>=wb)
		 && ((realHotSpot.y+drawingRadius)<=nb) && ((realHotSpot.y-drawingRadius)>=sb)){
	    return true;
	}
	return false;
    }


    /**default is 1.5 (see setForcedDrawing())*/
    public void setDrawingFactor(float f){drawingFactor=f;}

    /**forces drawing of path, even if not visible (the algorithm in charge of detecting whether a glyph should be drawn or not is not completely reliable for weird paths) - default is false - use only if absolutely sure that your path is not displayed whereas it should - also try increasing drawing factor before resorting to force drawing*/
    public void setForcedDrawing(boolean b){forcedDrawing=b;}

    public PathIterator getJava2DPathIterator(){return path.getPathIterator(null);}

    public GeneralPath getJava2DGeneralPath(){return path;}

    /**returns a clone of this object - not yet implemented for VPath*/
    public Object clone(){
	VPath res=new VPath();
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

    /** Highlight this glyph to give visual feedback when the cursor is inside it. */
    public void highlight(boolean b, Color selectedColor){}
    
}

