/*   FILE: VCirShape.java
 *   DATE OF CREATION:   Mar 28 2002
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

import net.claribole.zvtm.glyphs.projection.BProjectedCoordsP;

import com.xerox.VTM.engine.Camera;

/**
 * Same as VShape, surrounded by a circle.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VShape
 *@see com.xerox.VTM.glyphs.VShapeST
 **/

public class VCirShape extends ClosedShape {

    /*inside shape color (main color is assigned to the circle)*/
    Color shapeColor;

    /*inside shape border color (main border color is assigned to the circle)*/
    Color shapebColor;

    boolean paintShapeBorder=true;
    boolean shapeFilled=true;

    /*height=width in virtual space*/
    long vs;

    BProjectedCoordsP[] pc;

    /** Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0). */
    float[] vertices;

    /**
     *@param v Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0).
     */
    public VCirShape(float[] v){
	vx=0;
	vy=0;
	vz=0;
	vs=10;
	vertices=v;
	computeSize();
	orient=0;
	setColor(Color.white);
	setBorderColor(Color.black);
	setShapeColor(Color.red);
	setShapeBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param s size (width=height) in virtual space
     *@param v Vertex distances to the shape's center in the [0-1.0] range (relative to bounding circle). Vertices are laid out counter clockwise, with the first vertex placed at the same X coordinate as the shape's center (provided orient=0).
     *@param cc fill color for circle
     *@param sc fill color for shape
     *@param cbc border color for circle
     *@param sbc border color for shape
     */
    public VCirShape(long x, long y, int z, long s, float[] v, Color cc, Color sc, Color cbc, Color sbc, float or){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	vertices=v;
	computeSize();
	orient=or;
	setColor(cc);
	setBorderColor(cbc);
	setShapeColor(sc);
	setShapeBorderColor(sbc);
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
	    else {System.err.println("VCirShape:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new BProjectedCoordsP[1];
		pc[0]=new BProjectedCoordsP();
	    }
	    else {System.err.println("VCirShape:Error while adding camera "+verifIndex);}
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
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public float getSize(){return size;}

    void computeSize(){
	size=(float)vs;
    }

    public void sizeTo(float radius){
	size=radius;
	vs=Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public boolean fillsView(long w,long h,int camIndex){
	if ((Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cr) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cr)){return true;}
	else {return false;}
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (Math.sqrt(Math.pow(x-pc[camIndex].cx,2)+Math.pow(y-pc[camIndex].cy,2))<=pc[camIndex].cr){return true;}
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

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	int[] xcoords=new int[vertices.length];
	int[] ycoords=new int[vertices.length];
	float vertexAngle=orient;
	for (int j=0;j<vertices.length-1;j++){
	    xcoords[j]=(int)Math.round(pc[i].cx+pc[i].cr*Math.cos(vertexAngle)*vertices[j]);
	    ycoords[j]=(int)Math.round(pc[i].cy-pc[i].cr*Math.sin(vertexAngle)*vertices[j]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertxAngle computation too many
	xcoords[vertices.length-1]=(int)Math.round(pc[i].cx+pc[i].cr*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	ycoords[vertices.length-1]=(int)Math.round(pc[i].cy-pc[i].cr*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, vertices.length);
	}
	else {
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
	int[] xcoords=new int[vertices.length];
	int[] ycoords=new int[vertices.length];
	float vertexAngle=orient;
	for (int j=0;j<vertices.length-1;j++){
	    xcoords[j]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.cos(vertexAngle)*vertices[j]);
	    ycoords[j]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.sin(vertexAngle)*vertices[j]);
	    vertexAngle+=2*Math.PI/vertices.length;
	}//last iteration outside to loop to avoid one vertxAngle computation too many
	xcoords[vertices.length-1]=(int)Math.round(pc[i].lcx+pc[i].lcr*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	ycoords[vertices.length-1]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, vertices.length);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = xcoords[j];
		pc[i].lp.ypoints[j] = ycoords[j];
	    }
	    pc[i].lp.invalidate();
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr>1){//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
		}
	    }
	    if (shapeFilled){
		g.setColor(this.shapeColor);
		g.fillPolygon(pc[i].p);
	    }
	    if (paintShapeBorder){
		g.setColor(shapebColor);
		g.drawPolygon(pc[i].p);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].lcr>1){//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
		}
	    }
	    if (shapeFilled){
		g.setColor(this.shapeColor);
		g.fillPolygon(pc[i].lp);
	    }
	    if (paintShapeBorder){
		g.setColor(shapebColor);
		g.drawPolygon(pc[i].lp);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }


    public Color getShapeColor(){return shapeColor;}
    public Color getShapeBorderColor(){return shapebColor;}
    public boolean isShapeFilled(){return shapeFilled;}

    /** Set inside shape fill color. */
    public void setShapeColor(Color c){
	shapeColor=c;
    }
    
    /** Set inside shape border color. */
    public void setShapeBorderColor(Color c){
	shapebColor=c;
    }

    /** Set whether the glyph's interior should be painted with the fill color or not.
     *@param b false -&gt; do not paint interior of inside shape (only paint contour)
     */
    public void setShapeFilled(boolean b){
	shapeFilled=b;
    }

    /** Set whether the glyph's border should be painted with the border color or not.
     *@param b false -&gt; do not paint border of glyph
     */
    public void setDrawBorder(boolean b){
	paintShapeBorder=b;
    }

    public Object clone(){
	VCirShape res = new VCirShape(vx,vy,0,vs,vertices,color,shapeColor,bColor,shapebColor,orient);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
