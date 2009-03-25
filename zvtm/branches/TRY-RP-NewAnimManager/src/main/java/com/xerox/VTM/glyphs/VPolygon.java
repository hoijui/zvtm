/*   FILE: VPolygon.java
 *   DATE OF CREATION:   Mon Jan 13 13:34:44 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.claribole.zvtm.glyphs.projection.ProjPolygon;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;

/**
 * Polygon. Can be resized. Cannot be reoriented. This new implementation of VPolygon models vertices as doubles internally to allow resizing without loss of precision (and thus resizing to small sizes does not tamper with the shape's aspect). It might be more memory consuming, and less efficient than the original implementation, so the latter is still provided (class FPolygon), for people who do not care about resizing polygons.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VPolygonST
 *@see com.xerox.VTM.glyphs.FPolygon
 *@see com.xerox.VTM.glyphs.FPolygonST
 **/

public class VPolygon extends ClosedShape {

    /*height=width in virtual space*/
    long vs;

    /*array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjPolygon[] pc;

    /*store x,y vertex coords as relative coordinates w.r.t polygon's centroid*/
    double[] xcoords;
    double[] ycoords;
    double[] lxcoords;
    double[] lycoords;

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates in virtual space
     *@param c fill color
     */
    public VPolygon(LongPoint[] v,Color c){
	vx=0;  //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
	vy=0;  //several lines below
	vz=0;
	xcoords=new double[v.length];
	ycoords=new double[v.length];
	lxcoords=new double[v.length];
	lycoords=new double[v.length];
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

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates i virtual space
     *@param c fill color
     *@param bc border color
     */
    public VPolygon(LongPoint[] v, Color c, Color bc){
	vx=0;  //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
	vy=0;  //several lines below
	vz=0;
	xcoords=new double[v.length];
	ycoords=new double[v.length];
	lxcoords=new double[v.length];
	lycoords=new double[v.length];
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
	setBorderColor(bc);
    }

    /**
        *@param v list of x,y vertices ABSOLUTE coordinates in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        */
    public VPolygon(LongPoint[] v, int z, Color c){
        vx=0;  //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
        vy=0;  //several lines below
        vz = z;
        xcoords=new double[v.length];
        ycoords=new double[v.length];
        lxcoords=new double[v.length];
        lycoords=new double[v.length];
        for (int i=0;i<v.length;i++){
            xcoords[i]=v[i].x;
            ycoords[i]=v[i].y;
        }
        orient=0;
        LongPoint ct=getCentroid();
        vx=ct.x;
        vy=ct.y;
        for (int i=0;i<xcoords.length;i++){
            //translate to get relative coords w.r.t centroid
            xcoords[i]-=vx;
            ycoords[i]-=vy;
        }
        computeSize();
        setColor(c);
        setBorderColor(Color.black);
    }

    /**
        *@param v list of x,y vertices ABSOLUTE coordinates i virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        */
    public VPolygon(LongPoint[] v, int z, Color c, Color bc){
        vx=0;  //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
        vy=0;  //several lines below
        vz = z;
        xcoords=new double[v.length];
        ycoords=new double[v.length];
        lxcoords=new double[v.length];
        lycoords=new double[v.length];
        for (int i=0;i<v.length;i++){
            xcoords[i]=v[i].x;
            ycoords[i]=v[i].y;
        }
        orient=0;
        LongPoint ct=getCentroid();
        vx=ct.x;
        vy=ct.y;
        for (int i=0;i<xcoords.length;i++){
            //translate to get relative coords w.r.t centroid
            xcoords[i]-=vx;
            ycoords[i]-=vy;
        }
        computeSize();
        setColor(c);
        setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new ProjPolygon[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjPolygon(xcoords.length);
	}
    }

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
	    else {System.err.println("VPolygon:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjPolygon[1];
		pc[0]=new ProjPolygon(xcoords.length);
	    }
	    else {System.err.println("VPolygon:Error while adding camera "+verifIndex);}
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

    /** Cannot be reoriented. */
    public void orientTo(float angle){}

    public float getSize(){return size;}

    synchronized void computeSize(){
 	size=0;
	double f;
	for (int i=0;i<xcoords.length;i++){//at this point, the xcoords,ycoords should contain relative vertices coordinates (w.r.t vx/vy=centroid)
	    f=Math.sqrt(Math.pow(xcoords[i],2)+Math.pow(ycoords[i],2));
	    if (f>size){size=(float)f;}
	}
	vs=Math.round(size);
    }

    public synchronized void sizeTo(float radius){
	double ratio=radius/((double)size);
 	size=0;
	double f;
	for (int i=0;i<xcoords.length;i++){
	    xcoords[i]=xcoords[i]*ratio;
	    ycoords[i]=ycoords[i]*ratio;
	    f=Math.sqrt(Math.pow(xcoords[i],2)+Math.pow(ycoords[i],2));
	    if (f>size){size=(float)f;}
	}
	vs=Math.round(size);
	try {vsm.repaintNow();}catch (NullPointerException ex){}
    }

    public synchronized void reSize(float factor){
 	size=0;
	double f;
	for (int i=0;i<xcoords.length;i++){
	    xcoords[i]=xcoords[i]*factor;
	    ycoords[i]=ycoords[i]*factor;
	    f=Math.sqrt(Math.pow(xcoords[i],2)+Math.pow(ycoords[i],2));
	    if (f>size){size=(float)f;}
	}
	vs=Math.round(size);
	try {vsm.repaintNow();}catch (NullPointerException ex){}
    }

    public boolean fillsView(long w,long h,int camIndex){
	if ((pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
	else {return false;}
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (pc[camIndex].p.contains(jpx, jpy)){return true;}
        else {return false;}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].p.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
	}

    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (coordInside(jpx, jpy, camIndex, cvx, cvy)){
            //if the mouse is inside the glyph
            if (!pc[camIndex].prevMouseIn){
                //if it was not inside it last time, mouse has entered the glyph
                pc[camIndex].prevMouseIn=true;
                return Glyph.ENTERED_GLYPH;
            }
            //if it was inside last time, nothing has changed
            else {return Glyph.NO_CURSOR_EVENT;}  
        }
        else{
            //if the mouse is not inside the glyph
            if (pc[camIndex].prevMouseIn){
                //if it was inside it last time, mouse has exited the glyph
                pc[camIndex].prevMouseIn=false;
                return Glyph.EXITED_GLYPH;
            }//if it was not inside last time, nothing has changed
            else {return Glyph.NO_CURSOR_EVENT;}
        }
    }

    /** Get this polygon's list of vertices (relative coordinates).
     *@return relative coordinates (w.r.t polygon's centroid)
     */
    public LongPoint[] getVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]),Math.round(ycoords[i]));
	}
	return res;
    }

    /** Get this polygon's list of vertices (absolute coordinates).
     *@return absolute coordinates
     */
    public LongPoint[] getAbsoluteVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]+vx),Math.round(ycoords[i]+vy));
	}
	return res;
    }

    /** Get a serialization of this polygon's list of vertices.
     *@return a semicolon-separated string representation of all vertex absolute coordinates (x and y coordinates seperated by commas, e.g. x1,y1;x2,y2;x3,y3 etc.)
     */
    public String getVerticesAsText(){
	StringBuffer res=new StringBuffer();
	for (int i=0;i<xcoords.length-1;i++){
	    res.append(Math.round(xcoords[i]+vx)+","+Math.round(ycoords[i]+vy)+";");
	}
	res.append(Math.round(xcoords[xcoords.length-1]+vx)+","+Math.round(ycoords[ycoords.length-1]+vy));
	return res.toString();
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
	for (int j=0;j<xcoords.length;j++){
	    pc[i].xpcoords[j]=(int)Math.round(pc[i].cx+xcoords[j]*coef);
	    pc[i].ypcoords[j]=(int)Math.round(pc[i].cy-ycoords[j]*coef);
	}
	if (pc[i].p == null){
	    pc[i].p = new Polygon(pc[i].xpcoords,pc[i].ypcoords,xcoords.length);
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
	    pc[i].lp = new Polygon(pc[i].lxpcoords,pc[i].lypcoords,xcoords.length);
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

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr>1){//repaint only if object is visible
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
	if (pc[i].lcr > 1){//repaint only if object is visible
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

    /** Get the polygon's area. */
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

    /** Get the double precision coordinates of this polygon's centroid.
     *@see #getCentroid()
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

    /** Get the coordinates of this polygon's centroid in virtual space.
     *@see #getPreciseCentroid()
     */
    public LongPoint getCentroid(){
	Point2D.Double p2dd=this.getPreciseCentroid();
	return new LongPoint(Math.round(p2dd.getX()),Math.round(p2dd.getY()));
    }

    public Object clone(){
	LongPoint[] lps=new LongPoint[xcoords.length];
	for (int i=0;i<lps.length;i++){
	    lps[i]=new LongPoint(Math.round(xcoords[i]+vx),Math.round(ycoords[i]+vy));
	}
	VPolygon res=new VPolygon(lps,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
