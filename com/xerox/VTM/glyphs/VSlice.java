/*   FILE: VSlice.java
 *   DATE OF CREATION:  Mon Aug 29 15:27:03 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
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
import java.awt.geom.AffineTransform;

import net.claribole.zvtm.glyphs.projection.ProjSlice;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;

/**
 * Slice. This version is the most efficient, but it cannot be made translucent (see VSliceST).<br>
 * Slices are useful e.g. to draw pie menus.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VSliceST
 */

public class VSlice extends ClosedShape {

    /*vertex x coords*/
    int[] xpcoords = new int[3];
    /*vertex y coords*/
    int[] ypcoords = new int[3];

    public static final double RAD2DEG_FACTOR = 360 / Utilities.TWO_PI;
    public static final double DEG2RAD_FACTOR = Utilities.TWO_PI / 360.0;

    /*2nd point (arc end point)*/
    LongPoint p1 = new LongPoint(0,0);
    /*3rd point (arc end point)*/
    LongPoint p2 = new LongPoint(0,0);
    /*1st point corresponding to the outer triangle (near 2nd point)*/
    LongPoint p3 = new LongPoint(0,0);
    /*2nd point corresponding to the outer triangle (near 3rd point)*/
    LongPoint p4 = new LongPoint(0,0);

    /*radius in virtual space (equal to bounding circle radius since this is a circle)*/
    long vr;

    double angle;
    double orient;
    int angleDeg;
    int orientDeg;
    ProjSlice[] pc;
    
    /** Construct a slice by giving its 3 vertices
     *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc
     *@param c fill color
     *@param bc border color
     */
    public VSlice(LongPoint[] v, Color c, Color bc){
	vx = v[0].x;
	vy = v[0].y;
	vz = 0;
	p1 = v[1];
	p2 = v[2];
	computeSize();
	computeOrient();
	computeAngle();
	computePolygonEdges();
	setColor(c);
	setBorderColor(bc);
    }

    /** Construct a slice by giving its size, angle and orientation
     *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
     *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
     *@param vs arc radius in virtual space (in rad)
     *@param ag arc angle in virtual space (in rad)
     *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
     *@param c fill color
     *@param bc border color
     */
    public VSlice(long x, long y, long vs, double ag, double or, Color c, Color bc){
	vx = x;
	vy = y;
	vz = 0;
	size = (float)vs;
	vr = vs;
	orient = or;
	orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
	angle = ag;
	angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
	computeSliceEdges();
	computePolygonEdges();
	setColor(c);
	setBorderColor(bc);
    }

    /** Construct a slice by giving its size, angle and orientation
     *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
     *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
     *@param vs arc radius in virtual space (in degrees)
     *@param ag arc angle in virtual space (in degrees)
     *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
     *@param c fill color
     *@param bc border color
     */
    public VSlice(long x, long y, long vs, int ag, int or, Color c, Color bc){
	vx = x;
	vy = y;
	vz = 0;
	size = (float)vs;
	vr = vs;
	orient = or * DEG2RAD_FACTOR;
	orientDeg = or;
	angle = ag * DEG2RAD_FACTOR;
	angleDeg = ag;
	computeSliceEdges();
	computePolygonEdges();
	setColor(c);
	setBorderColor(bc);
    }

    void computeSize(){
	size = (float)Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	vr = Math.round(size);
    }

    void computeOrient(){
	double c = Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	double a1 = (p1.y-vy >= 0) ? Math.acos((p1.x-vx)/c) : Utilities.TWO_PI - Math.acos((p1.x-vx)/c);
	double a2 = (p2.y-vy >= 0) ? Math.acos((p2.x-vx)/c) : Utilities.TWO_PI - Math.acos((p2.x-vx)/c);
	// was initially (360/(4*Math.PI)) * (a1 + a2) / 2.0
	orient = (a1 + a2) / 2.0;
	orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
    }

    void computeAngle(){
	double c = Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	double a1 = (p1.y-vy >= 0) ? Math.acos((p1.x-vx)/c) : Utilities.TWO_PI - Math.acos((p1.x-vx)/c);
	double a2 = (p2.y-vy >= 0) ? Math.acos((p2.x-vx)/c) : Utilities.TWO_PI - Math.acos((p2.x-vx)/c);
	angle = a2 - a1;
	angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
    }

    void computeSliceEdges(){
	p1.x = Math.round(Math.cos(orient-angle/2.0)*size) + vx;
	p1.y = Math.round(Math.sin(orient-angle/2.0)*size) + vy;
	p2.x = Math.round(Math.cos(orient+angle/2.0)*size) + vx;
	p2.y = Math.round(Math.sin(orient+angle/2.0)*size) + vy;
    }

    void computePolygonEdges(){
	if (angle < Math.PI){
	    p3.x = vx + Math.round((p1.x-vx)/Math.cos(angle/2.0));
	    p3.y = vy + Math.round((p1.y-vy)/Math.cos(angle/2.0));
	    p4.x = vx + Math.round((p2.x-vx)/Math.cos(angle/2.0));
	    p4.y = vy + Math.round((p2.y-vy)/Math.cos(angle/2.0));
	}
	else if (angle > Math.PI){// if angle >= PI a triangle cannot be used to model the bounding polygon
	    p3.x = vx - Math.round((p1.x-vx)/Math.cos(angle/2.0)); // compute coordInside by checking that 
	    p3.y = vy - Math.round((p1.y-vy)/Math.cos(angle/2.0)); // point is in circle and *not* inside triangle
	    p4.x = vx - Math.round((p2.x-vx)/Math.cos(angle/2.0)); // (triangle modeling the part not covered by
	    p4.y = vy - Math.round((p2.y-vy)/Math.cos(angle/2.0)); // the slice)
	}
	else {// angle == Math.PI  - case of zero division
	    p3.x = p1.x;
	    p3.y = p1.y;
	    p4.x = p2.x;
	    p4.y = p2.y;
	}
    }
    
    public void initCams(int nbCam){
	pc = new ProjSlice[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new ProjSlice();
	}
    }

    public void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		ProjSlice[] ta = pc;
		pc = new ProjSlice[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i] = ta[i];
		}
		pc[pc.length-1] = new ProjSlice();
	    }
	    else {System.err.println("VSlice:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new ProjSlice[1];
		pc[0] = new ProjSlice();
	    }
	    else {System.err.println("VSlice:Error while adding camera "+verifIndex);}
	}
    }

    public void removeCamera(int index){
	pc[index] = null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i] != null){pc[i].prevMouseIn = false;}
	borderColor = bColor;
    }

    public void sizeTo(float sz){
	size = sz;
	vr = Math.round(size);
	computeSliceEdges();
	computePolygonEdges();
    }

    public void reSize(float factor){
	size *= factor;
	vr = Math.round(size);
	computeSliceEdges();
	computePolygonEdges();
    }

    /** Set the slice's orientation.
     *@param ag slice orientation in virtual space, interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc (bisector of the main angle). In [0:2Pi[
     */
    public void orientTo(float ag){
	orient = (ag > Utilities.TWO_PI) ? (ag % Utilities.TWO_PI) : ag;
	orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
	computeSliceEdges();
	computePolygonEdges();
    }

    /** Set the arc angle.
     *@param ag in [0:2Pi[
     */
    public void setAngle(double ag){
	angle = (ag > Utilities.TWO_PI) ? (ag % Utilities.TWO_PI) : ag;
	angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
	computeSliceEdges();
	computePolygonEdges();
    }

    /** Get the arc angle.
     *@return the angle in [0:2Pi[
     */
    public double getAngle(){
	return angle;
    }

    public float getSize(){
	return size;
    }

    /** Get the slice's orientation.
     *@return slice's orientation in virtual space, interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc (bisector of the main angle). In [0:2Pi[
     */
    public float getOrient(){return (float)orient;}

    public boolean fillsView(long w,long h,int camIndex){
	//XXX: TBW (call coordInside() for the four view corners)
	return false;
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (Math.sqrt(Math.pow(x-pc[camIndex].cx, 2)+Math.pow(y-pc[camIndex].cy, 2)) <= pc[camIndex].innerCircleRadius){
	    // see computePolygonEdges() for an explanation of the following tests
	    if (angle < Math.PI && pc[camIndex].boundingPolygon.contains(x, y) ||
		angle > Math.PI && !pc[camIndex].boundingPolygon.contains(x, y) ||
		angle == Math.PI && coordInsideHemisphere(x, y, camIndex)){
		return true;
	    }
	}
	return false;
    }
    
    boolean coordInsideHemisphere(int x, int y, int camIndex){
	if (orient == 0){
	    return (x >= pc[camIndex].cx) ? true : false;
	}
	else if (orient == Math.PI){
	    return (x <= pc[camIndex].cx) ? true : false;
	}
	else {
	    double a = (pc[camIndex].p2y-pc[camIndex].p1y) / (pc[camIndex].p2x-pc[camIndex].p1x);
	    double b = (pc[camIndex].p1y*pc[camIndex].p2x - pc[camIndex].p2y*pc[camIndex].p1x) / (pc[camIndex].p2x-pc[camIndex].p1x);
	    if (orient < Math.PI && y <= a*x+b ||
		orient > Math.PI && y >= a*x+b){
		return true;
	    }
	    else {
		return false;
	    }
	}
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

    //XXX: visibleInRegion() could be slightly optimized for VSlice (what about containedInRegion() ?)

    public void project(Camera c, Dimension d){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal + c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	int hw = d.width/2;
	int hh = d.height/2;
	pc[i].cx = hw + Math.round((vx-c.posx) * coef);
	pc[i].cy = hh - Math.round((vy-c.posy) * coef);
	// do the same for other points
	pc[i].p1x = hw + Math.round((p1.x-c.posx) * coef);
	pc[i].p1y = hh - Math.round((p1.y-c.posy) * coef);
	pc[i].p2x = hw + Math.round((p2.x-c.posx) * coef);
	pc[i].p2y = hh - Math.round((p2.y-c.posy) * coef);
	xpcoords[0] = pc[i].cx;
	ypcoords[0] = pc[i].cy;
	xpcoords[1] = hw + Math.round((p3.x-c.posx) * coef);
	ypcoords[1] = hh - Math.round((p3.y-c.posy) * coef);
	xpcoords[2] = hw + Math.round((p4.x-c.posx) * coef);
	ypcoords[2] = hh - Math.round((p4.y-c.posy) * coef);
	if (pc[i].boundingPolygon == null){
	    pc[i].boundingPolygon = new Polygon(xpcoords, ypcoords, 3);
	}
	else {
	    for (int j=0;j<xpcoords.length;j++){
		pc[i].boundingPolygon.xpoints[j] = xpcoords[j];
		pc[i].boundingPolygon.ypoints[j] = ypcoords[j];
	    }
	    pc[i].boundingPolygon.invalidate();
	}
	pc[i].innerCircleRadius = Math.round(size * coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal + c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	int hw = lensWidth/2;
	int hh = lensHeight/2;
	pc[i].lcx = hw + Math.round((vx-lensx) * coef);
	pc[i].lcy = hh - Math.round((vy-lensy) * coef);
	// do the same for other points
	pc[i].lp1x = hw + Math.round((p1.x-lensx) * coef);
	pc[i].lp1y = hh - Math.round((p1.y-lensy) * coef);
	pc[i].lp2x = hw + Math.round((p2.x-lensx) * coef);
	pc[i].lp2y = hh - Math.round((p2.y-lensy) * coef);
	xpcoords[0] = pc[i].lcx;
	ypcoords[0] = pc[i].lcy;
	xpcoords[1] = hw + Math.round((p3.x-lensx) * coef);
	ypcoords[1] = hh - Math.round((p3.y-lensy) * coef);
	xpcoords[2] = hw + Math.round((p4.x-lensx) * coef);
	ypcoords[2] = hh - Math.round((p4.y-lensy) * coef);
	if (pc[i].lboundingPolygon == null){
	    pc[i].lboundingPolygon = new Polygon(xpcoords, ypcoords, 3);
	}
	else {
	    for (int j=0;j<xpcoords.length;j++){
		pc[i].lboundingPolygon.xpoints[j] = xpcoords[j];
		pc[i].lboundingPolygon.ypoints[j] = ypcoords[j];
	    }
	    pc[i].lboundingPolygon.invalidate();
	}
	pc[i].linnerCircleRadius = Math.round(size * coef);
    }

    public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy){
 	if (pc[i].innerCircleRadius > 2){//paint a dot if too small
	    if (filled){
		g.setColor(this.color);    
		g.fillArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
			  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke != null){
		    g.setStroke(stroke);
		    g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
		    g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
		    g.setStroke(stdS);
		}
		else {
		    g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
		    g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
 	if (pc[i].linnerCircleRadius > 2){//paint a dot if too small
	    if (filled){
		g.setColor(this.color);    
		g.fillArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
			  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke != null){
		    g.setStroke(stroke);
		    g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
		    g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		    g.setStroke(stdS);
		}
		else {
		    g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
		    g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    /** Not implement yet. */
    public Object clone(){
	//XXX: TBW
	return null;
    }
    
}
