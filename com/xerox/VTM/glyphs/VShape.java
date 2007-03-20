/*   FILE: VShape.java
 *   DATE OF CREATION:   Aug 01 2001
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
 * $Id: VShape.java,v 1.10 2006/03/17 17:45:23 epietrig Exp $
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
 * Custom shape implementing Jean-Yves Vion-Dury's model - defined by its N vertices (every vertex is between 0 (distance from shape's center=0) and 1.0 (distance from shape's center equals bounding circle radius)) - angle between each vertices is 2*Pi/N - can be reoriented
 * @author Emmanuel Pietriga
 **/

public class VShape extends Glyph implements Cloneable {

    /**height=width in virtual space*/
    long vs;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    BProjectedCoordsP[] pc;

    /**list of vertex distance to the shape's center in the 0-1.0 range (relative to bounding circle) --vertices are layed out counter clockwise, with the first vertex placed at the same Y coord as the shape's center (provided orient=0)*/
    float[] vertices;

    int[] xcoords;
    int[] ycoords;
    int[] lxcoords;
    int[] lycoords;

    /**
     *@param v list of vertex distance to the shape's center in the 0-1.0 range (relative to bounding circle)
     */
    public VShape(float[] v){
	vx=0;
	vy=0;
	vz=0;
	vs=10;
	vertices=v;
	xcoords=new int[vertices.length];
	ycoords=new int[vertices.length];
	lxcoords=new int[vertices.length];
	lycoords=new int[vertices.length];
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
     *@param v list of vertex distance to the shape's center in the 0-1.0 range (relative to bounding circle) --vertices are layed out counter clockwise, with the first vertex placed at the same Y coord as the shape's center (provided orient=0)
     *@param c fill color
     */
    public VShape(long x,long y,float z,long s,float[] v,Color c,float or){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	vertices=v;
	xcoords=new int[vertices.length];
	ycoords=new int[vertices.length];
	lxcoords=new int[vertices.length];
	lycoords=new int[vertices.length];
	computeSize();
	orient=or;
	setColor(c);
	setBorderColor(bColor);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new BProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new BProjectedCoordsP();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		BProjectedCoordsP[] ta=pc;
		pc=new BProjectedCoordsP[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new BProjectedCoordsP();
	    }
	    else {System.err.println("VShape:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new BProjectedCoordsP[1];
		pc[0]=new BProjectedCoordsP();
	    }
	    else {System.err.println("VShape:Error while adding camera "+verifIndex);}
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

    /**compute size (bounding circle radius)*/
    void computeSize(){
	size=(float)vs;
    }

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){
	size=radius;
	vs=Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
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

    /**list of vertex distance to the shape's center in the 0-1.0 range (relative to bounding circle) --vertices are layed out counter clockwise, with the first vertex placed at the same Y coord as the shape's center (provided orient=0)*/
    public float[] getVertices(){
	return vertices;
    }

    /**
     *returns a comma-separated string representation of the vertex distance to the shape's center
     */
    public String getVerticesAsText(){
	StringBuffer res=new StringBuffer();
	for (int i=0;i<vertices.length-1;i++){
	    res.append(vertices[i]+",");
	}
	res.append(vertices[vertices.length-1]);
	return res.toString();
    }

    /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx = (d.width/2) + Math.round((vx-c.posx)*coef);
	pc[i].cy = (d.height/2) - Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	float vertexAngle=orient;
	for (int j=0;j<vertices.length-1;j++){
	    xcoords[j]=(int)Math.round(pc[i].cx+pc[i].cr*Math.cos(vertexAngle)*vertices[j]);
	    ycoords[j]=(int)Math.round(pc[i].cy-pc[i].cr*Math.sin(vertexAngle)*vertices[j]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertexAngle computation too many
	xcoords[vertices.length-1]=(int)Math.round(pc[i].cx+pc[i].cr*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	ycoords[vertices.length-1]=(int)Math.round(pc[i].cy-pc[i].cr*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, vertices.length);
	}
	else {
	    pc[i].p.npoints = xcoords.length;
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
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vs*coef);
	float vertexAngle=orient;
	for (int j=0;j<vertices.length-1;j++){
	    lxcoords[j]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.cos(vertexAngle)*vertices[j]);
	    lycoords[j]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.sin(vertexAngle)*vertices[j]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertexAngle computation too many
	lxcoords[vertices.length-1]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	lycoords[vertices.length-1]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(lxcoords, lycoords, vertices.length);
	}
	else {
	    pc[i].lp.npoints = xcoords.length;
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

    /**
     * returns a given VShape's area
     */
    public double getArea(){
	long[] xcoordsForArea=new long[vertices.length];
	long[] ycoordsForArea=new long[vertices.length];
	float vertexAngle=orient;
	for (int i=0;i<vertices.length-1;i++){
	    xcoordsForArea[i]=Math.round(vx+vs*Math.cos(vertexAngle)*vertices[i]);
	    ycoordsForArea[i]=Math.round(vy+vs*Math.sin(vertexAngle)*vertices[i]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertexAngle computation too many
	xcoordsForArea[vertices.length-1]=Math.round(vx+vs*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	ycoordsForArea[vertices.length-1]=Math.round(vy+vs*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	int j,k;
	double res=0;
	for (j=0;j<vertices.length;j++){
	    k=(j+1) % vertices.length;
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
	long[] xcoordsForArea=new long[vertices.length];
	long[] ycoordsForArea=new long[vertices.length];
	float vertexAngle=orient;
	for (int i=0;i<vertices.length-1;i++){
	    xcoordsForArea[i]=Math.round(vx+vs*Math.cos(vertexAngle)*vertices[i]);
	    ycoordsForArea[i]=Math.round(vy+vs*Math.sin(vertexAngle)*vertices[i]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertexAngle computation too many
	xcoordsForArea[vertices.length-1]=Math.round(vx+vs*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	ycoordsForArea[vertices.length-1]=Math.round(vy+vs*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	//compute polygon area
	int j,k;
	double area=0;
	for (j=0;j<vertices.length;j++){
	    k=(j+1) % vertices.length;
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
	for (j=0;j<vertices.length;j++){
	    k=(j+1) % vertices.length;
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
	VShape res=new VShape(vx,vy,0,vs,(float[])vertices.clone(),color,orient);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
