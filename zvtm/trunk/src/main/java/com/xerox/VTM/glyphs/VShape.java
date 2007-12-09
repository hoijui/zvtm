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
 * $Id$
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.claribole.zvtm.glyphs.projection.BProjectedCoordsP;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;

/**
 * Custom shape implementing Jean-Yves Vion-Dury's graphical object model. Defined by its N vertices (every vertex is between 0 (distance from shape's center=0) and 1.0 (distance from shape's center equals bounding circle radius)). Angle between each vertices is 2*Pi/N - can be reoriented.<br>
 * This version is the most efficient, but it can not be made translucent (see VShapeST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VShapeST
 *@see com.xerox.VTM.glyphs.VCirShape
 **/

public class VShape extends ClosedShape {

    /*height=width in virtual space*/
    long vs;

    BProjectedCoordsP[] pc;

    /** Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0). */
    float[] vertices;

    int[] xcoords;
    int[] ycoords;
    int[] lxcoords;
    int[] lycoords;

    /**
     *@param v Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0).
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
     *@param z z-index
     *@param s size (width=height) in virtual space
     *@param v Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0).
     *@param c fill color
     *@param or shape's orientation in [0, 2Pi[
     */
    public VShape(long x,long y, int z,long s,float[] v,Color c,float or){
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

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param s size (width=height) in virtual space
     *@param v Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0).
     *@param c fill color
     *@param bc border color
     *@param or shape's orientation in [0, 2Pi[
     */
    public VShape(long x, long y, int z, long s, float[] v, Color c, Color bc, float or){
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
	setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new BProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new BProjectedCoordsP();
	}
    }

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

    public void removeCamera(int index){
	pc[index]=null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
	borderColor = bColor;
    }

    public float getOrient(){return orient;}

    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public float getSize(){return size;}

    void computeSize(){
	size=(float)vs;
    }

    public void sizeTo(float radius){
	size=radius;
	vs=Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public boolean fillsView(long w,long h,int camIndex){
	if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (pc[camIndex].p.contains(x,y)){return true;}
	else {return false;}
    }

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

    /** Get the list of vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0). */
    public float[] getVertices(){
	return vertices;
    }

    /** Get a serialization of the list of vertex distances to the shape's center in the [0-1.0] range.
     *@return a comma-separated string representation of the vertex distance to the shape's center.
     */
    public String getVerticesAsText(){
	StringBuffer res=new StringBuffer();
	for (int i=0;i<vertices.length-1;i++){
	    res.append(vertices[i]+",");
	}
	res.append(vertices[vertices.length-1]);
	return res.toString();
    }

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
		pc[i].lp.xpoints[j] = lxcoords[j];
		pc[i].lp.ypoints[j] = lycoords[j];
	    }
	    pc[i].lp.invalidate();
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr >1){//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fillPolygon(pc[i].p);
		g.translate(-dx, -dy);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
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
	    if (paintBorder){
		g.setColor(borderColor);
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

    /** Get the shape's area. */
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

    /** Get the double precision coordinates of this shape's centroid.
     *@see #getCentroid()
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

    /** Get the coordinates of this shape's centroid in virtual space.
     *@see #getPreciseCentroid()
     */
    public LongPoint getCentroid(){
	Point2D.Double p2dd=this.getPreciseCentroid();
	return new LongPoint(Math.round(p2dd.getX()),Math.round(p2dd.getY()));
    }

    public Object clone(){
	VShape res=new VShape(vx, vy, 0, vs, (float[])vertices.clone(), color, borderColor, orient);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
