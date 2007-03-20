/*   FILE: FPolygon.java
 *   DATE OF CREATION:   Mon Jan 13 13:34:44 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FPolygon.java,v 1.8 2006/03/17 17:45:22 epietrig Exp $
 */ 

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;

import net.claribole.zvtm.lens.Lens;

/**
 * Custom polygon - CANNOT be resized nor reoriented. This is the old implementation of VPolygon, as found in zvtm 0.8.2. The new version of VPolygon can be resized, but at some cost from the efficiency point of view, so the old version is still provided here and can be used by people who do not intend to resize their Polygon instances (this implementation uses longs instead of doubles for its internal representation of the vertices, see VPolygon for other details) 
 * @author Emmanuel Pietriga
 **/

public class FPolygon extends Glyph implements Cloneable {

    /**height=width in virtual space*/
    long vs;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjPolygon[] pc;

    /*store x,y vertex coords as relative coordinates w.r.t polygon's centroid*/
    long[] xcoords;
    long[] ycoords;

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates
     *@param c fill color
     */
    public FPolygon(LongPoint[] v,Color c){
	vx=0;  //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
	vy=0;  //several lines below
	vz=0;
	xcoords=new long[v.length];
	ycoords=new long[v.length];
	for (int i=0;i<v.length;i++){
	    xcoords[i]=v[i].x;
	    ycoords[i]=v[i].y;
	}
	orient=0;
	LongPoint ct=getCentroid();
	vx=ct.x;
	vy=ct.y;
	for (int i=0;i<xcoords.length;i++){//translate to get relative coords w.r.t centroid
	    xcoords[i]-=vx;
	    ycoords[i]-=vy;
	}
	computeSize();
	setColor(c);
	setBorderColor(Color.black);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjPolygon[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjPolygon(xcoords.length);
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjPolygon[] ta=pc;
		pc=new ProjPolygon[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjPolygon(xcoords.length);
	    }
	    else {System.err.println("FPolygon:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjPolygon[1];
		pc[0]=new ProjPolygon(xcoords.length);
	    }
	    else {System.err.println("FPolygon:Error while adding camera "+verifIndex);}
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

    /**set orientation (absolute) - no effect*/
    public void orientTo(float angle){}

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**compute size (bounding circle radius)*/
    synchronized void computeSize(){
 	size=0;
	double f;
	for (int i=0;i<xcoords.length;i++){//at this point, the xcoords,ycoords should contain relative vertices coordinates (w.r.t vx/vy=centroid)
	    f=Math.sqrt(Math.pow(xcoords[i],2)+Math.pow(ycoords[i],2));
	    if (f>size){size=(float)f;}
	}
	vs=Math.round(size);
    }

    /**set absolute size by setting bounding circle radius (no effect)*/
    public synchronized void sizeTo(float radius){}

    /**multiply bounding circle radius by factor (no effect)*/
    public synchronized void reSize(float factor){}

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

    /**list of vertices in relative coordinates (w.r.t polygon centroid)*/
    public LongPoint[] getVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]),Math.round(ycoords[i]));
	}
	return res;
    }

    /**list of vertices in relative coordinates (absolute coordinates)*/
    public LongPoint[] getAbsoluteVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]+vx),Math.round(ycoords[i]+vy));
	}
	return res;
    }

    /**
     *returns a semicolon-separated string representation of the vertex absolute coordinates for this polygon (x and y coordinates seperated by commas, e.g. x1,y1;x2,y2;x3,y3 etc.)
     */
    public String getVerticesAsText(){
	StringBuffer res=new StringBuffer();
	for (int i=0;i<xcoords.length-1;i++){
	    res.append(Math.round(xcoords[i]+vx)+","+Math.round(ycoords[i]+vy)+";");
	}
	res.append(Math.round(xcoords[xcoords.length-1]+vx)+","+Math.round(ycoords[ycoords.length-1]+vy));
	return res.toString();
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
	for (int j=0;j<xcoords.length;j++){
	    pc[i].xpcoords[j]=(int)Math.round(pc[i].cx+xcoords[j]*coef);
	    pc[i].ypcoords[j]=(int)Math.round(pc[i].cy-ycoords[j]*coef);
	}
	if (pc[i].p == null){
	    pc[i].p = new Polygon(pc[i].xpcoords, pc[i].ypcoords, xcoords.length);
	}
	else {
	    pc[i].p.npoints = xcoords.length;
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = pc[i].xpcoords[j];
		pc[i].p.ypoints[j] = pc[i].ypcoords[j];
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
	for (int j=0;j<xcoords.length;j++){
	    pc[i].lxpcoords[j]=(int)Math.round(pc[i].lcx+xcoords[j]*coef);
	    pc[i].lypcoords[j]=(int)Math.round(pc[i].lcy-ycoords[j]*coef);
	}
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(pc[i].lxpcoords, pc[i].lypcoords, xcoords.length);
	}
	else {
	    pc[i].lp.npoints = xcoords.length;
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = pc[i].lxpcoords[j];
		pc[i].lp.ypoints[j] = pc[i].lypcoords[j];
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
	    g.translate(dx, dy);
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	    g.translate(-dx, -dy);
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
	    g.translate(dx, dy);
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	    g.translate(-dx, -dy);
	}
    }

    /**
     * returns a given FPolygon's area
     */
    public double getArea(){
	double[] xcoordsForArea=new double[xcoords.length];
	double[] ycoordsForArea=new double[ycoords.length];
	for (int i=0;i<xcoords.length;i++){
	    xcoordsForArea[i]=vx+xcoords[i];
	    ycoordsForArea[i]=vy+ycoords[i];
	}
	int j,k;
	double res=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    res+=(xcoordsForArea[j]*ycoordsForArea[k]-ycoordsForArea[j]*xcoordsForArea[k]);
	}
	res=res/2.0;
	return ((res<0) ? -res : res);
    }

    /**
     *return the double precision coordinates of this VShape's centroid
     */
    public Point2D.Double getPreciseCentroid(){
	//compute polygon vertices
	double[] xcoordsForArea=new double[xcoords.length];
	double[] ycoordsForArea=new double[ycoords.length];
	for (int i=0;i<xcoords.length;i++){
	    xcoordsForArea[i]=vx+xcoords[i];
	    ycoordsForArea[i]=vy+ycoords[i];
	}
	//compute polygon area
	int j,k;
	double area=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    area+=(xcoordsForArea[j]*ycoordsForArea[k]-ycoordsForArea[j]*xcoordsForArea[k]);
	}
	area=area/2.0;
	//area=((area<0) ? -area : area);  //do not do that!!! it can change the centroid's coordinates
	                                   //(-x,-y instead of x,y) depending on the order in which the
	                                   //sequence of vertex coords
	//compute centroid
	double factor=0;
	double cx=0;
	double cy=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    factor=xcoordsForArea[j]*ycoordsForArea[k]-xcoordsForArea[k]*ycoordsForArea[j];
	    cx+=(xcoordsForArea[j]+xcoordsForArea[k])*factor;
	    cy+=(ycoordsForArea[j]+ycoordsForArea[k])*factor;
	}
	area*=6.0;
	factor=1/area;
	cx*=factor;
	cy*=factor;
	Point2D.Double res=new Point2D.Double(cx,cy);
	return res;
    }

    /**
     *return the coordinates of this VShape's centroid in virtual space
     */
    public LongPoint getCentroid(){
	Point2D.Double p2dd=this.getPreciseCentroid();
	return new LongPoint(Math.round(p2dd.getX()),Math.round(p2dd.getY()));
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	LongPoint[] lps=new LongPoint[xcoords.length];
	for (int i=0;i<lps.length;i++){
	    lps[i]=new LongPoint(xcoords[i]+vx,ycoords[i]+vy);
	}
	FPolygon res=new FPolygon(lps,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
