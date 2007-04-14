/*   FILE: DPath.java
 *   DATE OF CREATION:   Thu Mar 29 19:33 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;

import net.claribole.zvtm.glyphs.projection.ProjectedCoords;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.Translucent;

/**
 * Dynamic Path, made of an arbitrary number of segments, quadratic curves, cubic curves, and gaps. All of these can be dynamically modified and animated through AnimManager's createPathAnimation method.
 *@author Emmanuel Pietriga, Boris Trofimov
 *@see net.claribole.zvtm.glyphs.DPathST
 *@see com.xerox.VTM.glyphs.VPath
 *@see net.claribole.zvtm.glyphs.VPathST
 *@see com.xerox.VTM.glyphs.VQdCurve
 *@see com.xerox.VTM.glyphs.VCbCurve
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.glyphs.VSegmentST
 *@see com.xerox.VTM.engine.VCursor#intersectsVPath(VPath p)
 *@see com.xerox.VTM.engine.AnimManager#createPathAnimation(long duration, short type, LongPoint[] data, Long gID, PostAnimationAction paa)
 */

public class DPath extends Glyph {
    
    static final short MOV = 0;
    static final short SEG = 1;
    static final short QDC = 2;
    static final short CBC = 3;
    
    /** For internal use. Dot not tamper with. Made public for outside package subclassing. Stores projected start point only. */
    public ProjectedCoords[] pc;

    PathElement[] elements;

    /* vx,vy are the coordinates of the path's start point */
    /* endPoint contains the coordinates of the last element's endpoint */
    LongPoint endPoint;
    /* centerPoint contains the coordinates of the point half way on 
       the virtual line linking the path's start point and end point. */
    LongPoint centerPoint;
    
    /* Variables used to determine and control the path clipping/drawing */
    float drawingRadius;
    float drawingFactor = 1.2f;
    boolean forcedDrawing = false;

    public DPath(){
	vx = 0;
	vy = 0;
	vz = 0;
	endPoint = new LongPoint(vx, vy);
	centerPoint = new LongPoint(vx, vy);
	elements = new PathElement[0];
	sensit = false;
	setColor(Color.BLACK);
    }

    /**
     *@param x start coordinate in virtual space
     *@param y start coordinate in virtual space
     *@param z altitude
     *@param c color
     */
    public DPath(long x, long y, float z, Color c){
	vx = x;
	vy = y;
	vz = z;
	endPoint = new LongPoint(vx, vy);
	centerPoint = new LongPoint(vx, vy);
	elements = new PathElement[0];
	sensit = false;
	setColor(c);
    }

    /** Add a new cubic curve to the path, from current point to point (x,y), controlled by (x1,y1)
     *@param x x coordinate of end point in virtual space
     *@param y y coordinate of end point in virtual space
     *@param x1 x coordinate of 1st control point in virtual space
     *@param y1 y coordinate of 1st control point in virtual space
     *@param x2 x coordinate of 2nd control point in virtual space
     *@param y2 y coordinate of 2nd control point in virtual space
     *@param abs true if coordinates should be interpreted as absolute coordinates, false if coordinates should be interpreted as relative coordinates (w.r.t last point)
     */
    public void addCbCurve(long x, long y, long x1, long y1, long x2, long y2, boolean abs){
	CBCElement e;
	if (abs){
	    e = new CBCElement(x, y, x1, y1, x2, y2);
	    endPoint.setLocation(x, y);
	}
	else {
	    e = new CBCElement(endPoint.x+x, endPoint.y+y, endPoint.x+x1, endPoint.y+y1, endPoint.x+x2, endPoint.y+y2);
	    endPoint.translate(x, y);
	}
	centerPoint.setLocation((vx+endPoint.x)/2, (vy+endPoint.y)/2);
	PathElement[] tmp = new PathElement[elements.length+1];
	System.arraycopy(elements, 0, tmp, 0, elements.length);
	tmp[elements.length] = e;
	Arrays.fill(elements, null);
	elements = tmp;
	computeSize();
    }

    /** Add a new quadratic curve to the path, from current point to point (x,y), controlled by (x1,y1)
     *@param x x coordinate of end point in virtual space
     *@param y y coordinate of end point in virtual space
     *@param x1 x coordinate of control point in virtual space
     *@param y1 y coordinate of control point in virtual space
     *@param abs true if coordinates should be interpreted as absolute coordinates, false if coordinates should be interpreted as relative coordinates (w.r.t last point)
     */
    public void addQdCurve(long x, long y, long x1, long y1, boolean abs){
	QDCElement e;
	if (abs){
	    e = new QDCElement(x, y, x1, y1);
	    endPoint.setLocation(x, y);
	}
	else {
	    e = new QDCElement(endPoint.x+x, endPoint.y+y, endPoint.x+x1, endPoint.y+y1);
	    endPoint.translate(x, y);
	}
	centerPoint.setLocation((vx+endPoint.x)/2, (vy+endPoint.y)/2);
	PathElement[] tmp = new PathElement[elements.length+1];
	System.arraycopy(elements, 0, tmp, 0, elements.length);
	tmp[elements.length] = e;
	Arrays.fill(elements, null);
	elements = tmp;
	computeSize();
    }

    /** Add a new segment to the path, from current point to point (x,y).
     *@param x x coordinate of end point in virtual space
     *@param y y coordinate of end point in virtual space
     *@param abs true if coordinates should be interpreted as absolute coordinates, false if coordinates should be interpreted as relative coordinates (w.r.t last point)
     */
    public void addSegment(long x, long y, boolean abs){
	if (abs){endPoint.setLocation(x, y);}
	else {endPoint.translate(x, y);}
	centerPoint.setLocation((vx+endPoint.x)/2, (vy+endPoint.y)/2);
	PathElement[] tmp = new PathElement[elements.length+1];
	System.arraycopy(elements, 0, tmp, 0, elements.length);
	tmp[elements.length] = new SEGElement(endPoint.x, endPoint.y);
	Arrays.fill(elements, null);
	elements = tmp;
	computeSize();
    }

    /** Add a new 'gap' to the path (move without drawing anything), from current point to point (x,y).
     *@param x x coordinate of end point in virtual space
     *@param y y coordinate of end point in virtual space
     *@param abs true if coordinates should be interpreted as absolute coordinates, false if coordinates should be interpreted as relative coordinates (w.r.t last point)
     */
    public void jump(long x, long y, boolean abs){
	if (abs){endPoint.setLocation(x, y);}
	else {endPoint.translate(x, y);}
	centerPoint.setLocation((vx+endPoint.x)/2, (vy+endPoint.y)/2);
	PathElement[] tmp = new PathElement[elements.length+1];
	System.arraycopy(elements, 0, tmp, 0, elements.length);
	tmp[elements.length] = new MOVElement(endPoint.x, endPoint.y);
	Arrays.fill(elements, null);
	elements = tmp;
	computeSize();
    }

    public void initCams(int nbCam){
	pc = new ProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new ProjectedCoords();
	}
	for (int i=0;i<elements.length;i++){
	    elements[i].initCams(nbCam);
	}
    }

    public void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		ProjectedCoords[] ta = pc;
		pc = new ProjectedCoords[ta.length+1];
		System.arraycopy(ta, 0, pc, 0, ta.length);
		pc[pc.length-1] = new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new ProjectedCoords[1];
		pc[0] = new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
	}
	for (int i=0;i<elements.length;i++){
	    elements[i].addCamera(verifIndex);
	}
    }

    public void removeCamera(int index){
 	pc[index] = null;
	for (int i=0;i<elements.length;i++){
	    elements[i].removeCamera(index);
	}
    }

    public void resetMouseIn(){}

    public void resetMouseIn(int i){}
    
    public void sizeTo(float factor){}

    public void reSize(float factor){}

    public void orientTo(float angle){}

    public float getSize(){
	return size;
    }

    void computeSize(){
	size = (float)Math.sqrt(Math.pow((endPoint.x-vx)/2, 2) + Math.pow((endPoint.y-vy)/2, 2));
	drawingRadius = size * drawingFactor;
    }

    public float getOrient(){return orient;}

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    public short mouseInOut(int x,int y,int camIndex){
	return Glyph.NO_CURSOR_EVENT;
    }
    
    int hw, hh, lhw, lhh;

    public void project(Camera c, Dimension d){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal+c.altitude));
	hw = d.width/2;
	hh = d.height/2;
	pc[i].cx = hw + Math.round((vx-c.posx)*coef);
	pc[i].cy = hh - Math.round((vy-c.posy)*coef);
	if (elements.length == 0){return;}
	elements[0].project(i, hw, hh, c, coef, pc[i].cx, pc[i].cy);
	for (int j=1;j<elements.length;j++){
	    elements[j].project(i, hw, hh, c, coef, elements[j-1].getX(i), elements[j-1].getY(i));
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal+c.altitude)) * lensMag;
	lhw = lensWidth/2;
	lhh = lensHeight/2;
	pc[i].lcx = lhw + Math.round((vx-(lensx))*coef);
	pc[i].lcy = lhh - Math.round((vy-(lensy))*coef);
	if (elements.length == 0){return;}
	elements[0].projectForLens(i, lhw, lhh, lensx, lensy, coef, pc[i].lcx, pc[i].lcy);
	for (int j=1;j<elements.length;j++){
	    elements[j].projectForLens(i, lhw, lhh, lensx, lensy, coef, elements[j-1].getlX(i), elements[j-1].getlY(i));
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.translate(dx,dy);
	    for (int j=0;j<elements.length;j++){
		if (elements[j].type == DPath.MOV){continue;}
		g.draw(elements[j].getShape(i));		
	    }
	    g.translate(-dx,-dy);
	    g.setStroke(stdS);
	}
	else {
	    g.translate(dx,dy);
	    for (int j=0;j<elements.length;j++){
		if (elements[j].type == DPath.MOV){continue;}
		g.draw(elements[j].getShape(i));
	    }
	    g.translate(-dx,-dy);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.translate(dx,dy);
	    for (int j=0;j<elements.length;j++){
		if (elements[j].type == DPath.MOV){continue;}
		g.draw(elements[j].getlShape(i));
	    }
	    g.translate(-dx,-dy);
	    g.setStroke(stdS);
	}
	else {
	    g.translate(dx,dy);
	    for (int j=0;j<elements.length;j++){
		if (elements[j].type == DPath.MOV){continue;}
		g.draw(elements[j].getlShape(i));
	    }
	    g.translate(-dx,-dy);
	}
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if (forcedDrawing){return true;}
	else {
	    if ((centerPoint.x >= wb) && (centerPoint.x <= eb)
		&& (centerPoint.y >= sb) && (centerPoint.y <= nb)){
		// if glyph hotspot is in the region, we consider it is visible
		return true;
	    }
	    else {// if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning 
		//   that some glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		return (((centerPoint.x-drawingRadius) <= eb) && ((centerPoint.x+drawingRadius) >= wb)
			&& ((centerPoint.y-drawingRadius) <= nb) && ((centerPoint.y+drawingRadius) >= sb));
	    }
	}
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if (forcedDrawing){return true;}
	else {
	    return ((centerPoint.x >= wb) && (centerPoint.x <= eb) &&
		    (centerPoint.y >= sb) && (centerPoint.y <= nb)
		    && ((centerPoint.x+drawingRadius) <= eb) && ((centerPoint.x-drawingRadius) >= wb)
		    && ((centerPoint.y+drawingRadius) <= nb) && ((centerPoint.y-drawingRadius) >= sb));
	}
    }

    /** Sets a threshold below which the pass should be drawn. Default is 1.5.
     *@see #setForcedDrawing(boolean b)
     */
    public void setDrawingFactor(float f){
	drawingFactor = f;
    }

    /** Force drawing of path, even if not visible. The algorithm in charge of detecting whether a glyph should be drawn or not is not completely reliable for weird paths.<br>Default is false. Use only if absolutely sure that your path is not displayed whereas it should be. Also try increasing drawing factor before resorting to forced drawing.
     *@see #setDrawingFactor(float f)
     */
    public void setForcedDrawing(boolean b){
	forcedDrawing = b;
    }

    /** Not implemented yet. */
    public Object clone(){
	return new DPath();
    }

    public void highlight(boolean b, Color selectedColor){}
    
    /**
     * Edit coordinates of start, end and control points of the element in DPath
     * @param index index of the element in the DPath
     * @param sx x coordinate of the element's start point
     * @param sy y coordinate of the element's start point
     * @param ex x coordinate of the element's end point
     * @param ey y coordinate of the element's end point
     * @param ctrlPoints list of the LongPoints that contain coordinates of the control point(s) (in case of QD/CB curve)
     * @param abs indicates whether to use absolute coordinates or relative
     */
    public void editElement(int index, long sx, long sy, long ex, long ey, LongPoint[] ctrlPoints, boolean abs){
	if (index > -1 && index < elements.length && elements[index] != null){
	    if (index > 0){
		if (abs){
		    elements[index-1].x = sx;
		    elements[index-1].y = sy;	            
		}
		else {
		    elements[index-1].x += sx;
		    elements[index-1].y += sy;
		}		    
	    }
            else{
        	if (abs){
        	    this.vx = sx;
        	    this.vy = sy;
        	}
        	else {
        	    this.vx += sx;
        	    this.vy += sy;
        	}
            }
	    PathElement el = elements[index];
	    switch(el.type){
	    case DPath.QDC:{
		if (ctrlPoints != null && ctrlPoints.length > 0 && ctrlPoints[0] != null){
		    if (abs){
			((QDCElement)el).ctrlx = ctrlPoints[0].x;
			((QDCElement)el).ctrly = ctrlPoints[0].y;
		    }
		    else {
			((QDCElement)el).ctrlx += ctrlPoints[0].x;
			((QDCElement)el).ctrly += ctrlPoints[0].y;
		    }
		}
		break;
	    }
	    case DPath.CBC:{
		if (ctrlPoints != null && ctrlPoints.length > 1 && ctrlPoints[0] != null && ctrlPoints[1] != null){
		    if (abs){
			((CBCElement)el).ctrlx1 = ctrlPoints[0].x;
			((CBCElement)el).ctrly1 = ctrlPoints[0].y;
			((CBCElement)el).ctrlx2 = ctrlPoints[1].x;
			((CBCElement)el).ctrly2 = ctrlPoints[1].y;
		    }
		    else {
			((CBCElement)el).ctrlx1 += ctrlPoints[0].x;
			((CBCElement)el).ctrly1 += ctrlPoints[0].y;
			((CBCElement)el).ctrlx2 += ctrlPoints[1].x;
			((CBCElement)el).ctrly2 += ctrlPoints[1].y;
		    }
		}
		break;
	    }
	    }
	    if (abs){
                el.x = ex;
                el.y = ey;
            }
            else {
                el.x += ex;
                el.y += ey;
            }
	}	    
    }
    
    /**
     * Transform DPath by translating each of the points
     * @param points List of new coordinates for each point. Example order could be: startPoint, controlPoint1, controlPoint2, endPoint, controlPoint1, endPoint, endPoint ...
     * @param abs  whether to use absolute coordinates or relative
     */
    public void edit(LongPoint[] points, boolean abs){
	// check consistensy
	int totalPointsCount = 1;
	for (int i=0; i < elements.length; i++){
	    totalPointsCount += 1; // for SEG and MOV
	    switch (elements[i].type){
	    case DPath.CBC:{totalPointsCount += 2; break;} // Two additional points
	    case DPath.QDC:{totalPointsCount += 1; break;} // One additional point
	    }
	}
	if (points != null && points.length == totalPointsCount){
	    this.vx = points[0].x;
	    this.vy = points[0].y;
	    int offset = 0;
	    for (int i=0; i < elements.length; i++) {
		switch (elements[i].type){
		case DPath.CBC:{
		    if (abs){
			((CBCElement)elements[i]).ctrlx1 = points[i+1+offset].x;
		    	((CBCElement)elements[i]).ctrly1 = points[i+1+offset].y;
		    	((CBCElement)elements[i]).ctrlx2 = points[i+2+offset].x;
		    	((CBCElement)elements[i]).ctrly2 = points[i+2+offset].y;
		    	elements[i].x = points[i+3+offset].x;
			elements[i].y = points[i+3+offset].y;
		    }
		    else {
			((CBCElement)elements[i]).ctrlx1 += points[i+1+offset].x;
		    	((CBCElement)elements[i]).ctrly1 += points[i+1+offset].y;
		    	((CBCElement)elements[i]).ctrlx2 += points[i+2+offset].x;
		    	((CBCElement)elements[i]).ctrly2 += points[i+2+offset].y;
		    	elements[i].x += points[i+3+offset].x;
			elements[i].y += points[i+3+offset].y;
		    }
		    offset += 2;
		    break;
		}
		case DPath.QDC:{
		    if (abs){
			((QDCElement)elements[i]).ctrlx = points[i+1+offset].x;
		    	((QDCElement)elements[i]).ctrly = points[i+1+offset].y;
		    	elements[i].x = points[i+2+offset].x;
			elements[i].y = points[i+2+offset].y;
		    }
		    else{
			((QDCElement)elements[i]).ctrlx += points[i+1+offset].x;
		    	((QDCElement)elements[i]).ctrly += points[i+1+offset].y;
		    	elements[i].x += points[i+2+offset].x;
			elements[i].y += points[i+2+offset].y;
		    }
		    offset += 1;
		    break;
		}
		default:{
		    if (abs){
                        elements[i].x = points[i+1+offset].x;
                        elements[i].y = points[i+1+offset].y;
                    }
                    else {
                        elements[i].x += points[i+1+offset].x;
                        elements[i].y += points[i+1+offset].y;
                    }
		}
		}
	    }
	}
    }
    
    /**
     * Get total number of elements in the path
     */
    public int getElementsCount(){
	if (elements != null)
	    return elements.length;
	else
	    return 0;
    }

    /**
     * Get element's type
     * @param index index of the element in the DPath
     * @return -1 in case of incorrect parameters, otherwise returns one of the constants DPath.CBC, DPath.QDC, DPath.SEG, DPath.MOV 
     */
    public int getElementType(int index){
	if (elements != null && index > -1 && index < elements.length && elements[index] != null){
	    return elements[index].type;
	}
	else {
	    return -1;
	}
    }
    
    /**
     * Get coordinates of the start, end and control points of the element
     * @param index index of the element in the DPath
     * @return List of element's points ordered as startPoint, controlPoint1, controlPoint2, endPoint
     */    
    public LongPoint[] getElementPointsCoordinates(int index){
	LongPoint[] result = null;
	if (elements != null && index > -1 && index < elements.length && elements[index] != null){
	    switch (elements[index].type){
	    case DPath.CBC:{
		result = new LongPoint[4];
		if (index == 0){
		    result[0] = new LongPoint(this.vx, this.vy);
		}
		else{
		    result[0] = new LongPoint(elements[index - 1].x, elements[index - 1].y);
		}
		result[3] = new LongPoint(elements[index].x, elements[index].y);
		result[1] = new LongPoint(((CBCElement)elements[index]).ctrlx1, ((CBCElement)elements[index]).ctrly1);
		result[2] = new LongPoint(((CBCElement)elements[index]).ctrlx2, ((CBCElement)elements[index]).ctrly2);
		break;
	    }
	    case DPath.QDC:{
		result = new LongPoint[3];
		if (index == 0){
		    result[0] = new LongPoint(this.vx, this.vy);
		}
		else{
		    result[0] = new LongPoint(elements[index - 1].x, elements[index - 1].y);
		}
		result[2] = new LongPoint(elements[index].x, elements[index].y);
		result[1] = new LongPoint(((QDCElement)elements[index]).ctrlx, ((QDCElement)elements[index]).ctrly);
		break;
	    }
	    default:{
		result = new LongPoint[2];
		if (index == 0){
		    result[0] = new LongPoint(this.vx, this.vy);
		}
		else{
		    result[0] = new LongPoint(elements[index - 1].x, elements[index - 1].y);
		}
		result[1] = new LongPoint(elements[index].x, elements[index].y);
		break;
	    }
	    }
	}
	return result;
    }
    
    /**
     * Get coordinates of each point in the path including control points
     * @return Rerurns list of points in following format: startPoint, controlPoint1, controlPoint2, endPoint ...
     */
    public LongPoint[] getAllPointsCoordinates(){
	int totalNumberOfPoints = 1;
	for (int i=0; i < elements.length; i++){
	    totalNumberOfPoints += 1;
	    short type = elements[i].type;
	    switch (type){
	    case DPath.CBC:{totalNumberOfPoints += 2; break;}
	    case DPath.QDC:{totalNumberOfPoints += 1; break;}
	    }
	}
	LongPoint[] result = new LongPoint[totalNumberOfPoints];
	int offset = 0;
	result[0] = new LongPoint(this.vx, this.vy);
	for (int i=0; i < elements.length; i++){
	    switch(elements[i].type){
	    case DPath.CBC:{
		CBCElement el = (CBCElement)elements[i];
		result[i+1+offset] = new LongPoint(el.ctrlx1, el.ctrly1);
		result[i+2+offset] = new LongPoint(el.ctrlx2, el.ctrly2);
		result[i+3+offset] = new LongPoint(el.x, el.y);
		offset += 2;
		break;
	    }
	    case DPath.QDC:{
		QDCElement el = (QDCElement)elements[i];
		result[i+1+offset] = new LongPoint(el.ctrlx, el.ctrly);
		result[i+2+offset] = new LongPoint(el.x, el.y);
		offset += 1;
		break;
	    }
	    default:{
		result[i+1+offset] = new LongPoint(elements[i].x, elements[i].y);
	    }
	    }
	}
	return result;
    }
    
    /**
     * Calculates coordinates of all DPath's points (including control points) to display the DPAth as a line.
     * @param path DPath to be flatten
     * @param startPoint Start point of desired line
     * @param endPoint End point of desired line
     * @param abs whether to use absolute values
     * @return List of LongPoint absolute coordinates that can be passed to the edit(LongPoint[], boolean) method or to the AnimManager
     */
    public static LongPoint[] getFlattenedCoordinates(DPath path, LongPoint startPoint, LongPoint endPoint, boolean abs){
	LongPoint[] result = path.getAllPointsCoordinates();
	if (!abs){
	    startPoint = new LongPoint(result[0].x + startPoint.x, result[0].y + startPoint.y);
	    endPoint = new LongPoint(result[result.length-1].x + endPoint.x, result[result.length-1].y + endPoint.y);            
	}
	long dx = Math.round((double)(endPoint.x - startPoint.x) / (double)result.length);
	long dy = Math.round((double)(endPoint.y - startPoint.y) / (double)result.length);
	
	for (int i = 0; i < result.length - 1; i++){
            result[i].x = startPoint.x + i * dx;
            result[i].y = startPoint.y + i * dy;                
        }
	result[result.length - 1].x = endPoint.x;
	result[result.length - 1].y = endPoint.y;
	return result;
    }
    
    /**
     * Convert given VPath instance to the DPath
     * @param vp VPath to be converted
     * @return new instance of DPath which has the same structure and location as given VPath
     */
    public static DPath fromVPath(VPath vp){
	DPath res = null;
	if (vp != null){
	    res = (vp instanceof VPathST) ? new DPathST(vp.vx, vp.vy, vp.vz, vp.getColor(), ((Translucent)vp).getTranslucencyValue()) : new DPath(vp.vx, vp.vy, vp.vz, vp.getColor());
	    BasicStroke s = vp.getStroke();
	    if (s != null){
		res.setStroke(s);
	    }
	    else {
		res.setStrokeWidth(vp.getStrokeWidth());
	    }
	    res.setDrawingFactor(vp.getDrawingFactor());
	    res.setForcedDrawing(vp.getForcedDrawing());
	    PathIterator pi = vp.getJava2DPathIterator();
	    pi.next(); 
	    double[] cds=new double[6];
	    int type;
	    while (!pi.isDone()){
		type=pi.currentSegment(cds);
		switch (type){
		case PathIterator.SEG_CUBICTO:{
		    res.addCbCurve((long)cds[4],(long)-cds[5],(long)cds[0],(long)-cds[1],(long)cds[2],(long)-cds[3],true);
		    break;
		}
		case PathIterator.SEG_QUADTO:{
		    res.addQdCurve((long)cds[2],(long)-cds[3],(long)cds[0],(long)-cds[1],true);
		    break;
		}
		case PathIterator.SEG_LINETO:{
		    res.addSegment((long)cds[0],(long)-cds[1],true);
		    break;
		}
		case PathIterator.SEG_MOVETO:{
		    res.jump((long)cds[0],(long)-cds[1],true);
		    break;
		}
		}
		pi.next();
	    }
	}
	return res;
    }
    
    /**
     * Convert given DPath instance to the VPath
     * @param dp DPath to be converted
     * @return new instance of VPath which has the same structure and location as given DPath
     */
    public static VPath toVPath(DPath dp){
	VPath res = null;
	if (dp != null){
	    res = (dp instanceof DPathST) ? new VPathST(dp.vx, dp.vy, dp.vz, dp.getColor(), ((Translucent)dp).getTranslucencyValue()) : new VPath(dp.vx, dp.vy, dp.vz, dp.getColor());
	    BasicStroke s = dp.getStroke();
	    if (s != null)
		res.setStroke(s);
	    else
		res.setStrokeWidth(dp.getStrokeWidth());
	    res.setDrawingFactor(dp.drawingFactor);
	    res.setForcedDrawing(dp.forcedDrawing);
	    for (int i = 0; i < dp.getElementsCount(); i++){
		int elType = dp.getElementType(i);
		LongPoint[] pts = dp.getElementPointsCoordinates(i);
		switch(elType){
		case DPath.CBC:{
		    res.addCbCurve(pts[3].x, pts[3].y, pts[1].x, pts[1].y, pts[2].x, pts[2].y, true);
		    break;
		}
		case DPath.QDC:{
		    res.addQdCurve(pts[2].x, pts[2].y, pts[1].x, pts[1].y, true);
		    break;
		}
		case DPath.SEG:{
		    res.addSegment(pts[1].x, pts[1].y, true);
		    break;
		}
		case DPath.MOV:{
		    res.jump(pts[1].x, pts[1].y, true);
		    break;
		}
		}
	    }
	}
	return res;
    }
    
    /**
     * Get orientation of the tangent to the start of the path.
     * @return radians between 0..2*Pi
     */
    public float getStartTangentOrientation(){
	float res = 0;
	if (elements.length > 0){
	    PathElement el = elements[0];
	    long sx = 0;
	    long sy = 0;
	    switch(el.type){
	    case DPath.CBC:{
		sx = ((CBCElement)el).ctrlx1;
		sy = ((CBCElement)el).ctrly1;
		break;
	    }
	    case DPath.QDC:{
		sx = ((QDCElement)el).ctrlx;
		sy = ((QDCElement)el).ctrly;
		break;
	    }
	    default:{
		sx = el.x;
		sy = el.y;
		break;
	    }
	    }
	    if (vx == sx){ // x = 0, y = +-1
		if (vy > sy) // y > 0
		    res = (float)(Math.PI / 2);
		else // y < 0
		    res = (float)(Math.PI * 1.5);
	    }
	    else {
		double tan = (double)(vy - sy) / (double)(vx - sx);
		res = (float)Math.atan(tan);
		if (vx < sx) { // x < 0 
		    res += Math.PI;
		}
		if (vx > sx && vy < sy){ // x > 0; y < 0
		    res += 2*Math.PI;
		}
	    }
	}
	return res;
    }
    
    /**
     * Get orientation of the tangent to the end of the path.
     * @return radians between 0..2*Pi
     */
    public float getEndTangentOrientation(){
	float res = 0;
	if (elements.length > 0){
	    PathElement el = elements[elements.length-1];
	    long sx = 0;
	    long sy = 0;
	    switch(el.type){
	    case DPath.CBC:{
		sx = ((CBCElement)el).ctrlx2;
		sy = ((CBCElement)el).ctrly2;
		break;
	    }
	    case DPath.QDC:{
		sx = ((QDCElement)el).ctrlx;
		sy = ((QDCElement)el).ctrly;
		break;
	    }
	    default:{
		if (elements.length > 1){
		    sx = elements[elements.length - 2].x;
		    sy = elements[elements.length - 2].y;
		}
		else {
		    sx = vx;
		    sy = vy;
		}
		break;
	    }
	    }
	    if (el.x == sx){ // x = 0, y = +-1
		if (el.y > sy) // y > 0
		    res = (float)(Math.PI / 2);
		else // y < 0
		    res = (float)(Math.PI * 1.5);
	    }
	    else {
		double tan = (double)(el.y - sy) / (double)(el.x - sx);
		res = (float)Math.atan(tan);
		if (el.x < sx) { // x < 0 
		    res += Math.PI;
		}
		if (el.x > sx && el.y < sy){ // x > 0; y < 0
		    res += 2*Math.PI;
		}
	    }
	}
	return res;
    }
}

abstract class PathElement {

    short type;

    long x;
    long y;

    abstract void initCams(int nbCam);

    abstract void addCamera(int verifIndex);

    abstract void removeCamera(int index);
    
    abstract void project(int i, int hw, int hh, Camera c, float coef, double px, double py);

    abstract void projectForLens(int i, int hw, int hh, long lx, long ly, float coef, double px, double py);

    abstract double getX(int i);

    abstract double getY(int i);

    abstract double getlX(int i);

    abstract double getlY(int i);

    abstract Shape getShape(int i);

    abstract Shape getlShape(int i);

}

class MOVElement extends PathElement {

    /* Move from previous point to (x,y) in virtual space
       without drawing anything */

    Point2D[] pc;
    Point2D[] lpc;

    MOVElement(long x, long y){
	type = DPath.MOV;
	this.x = x;
	this.y = y;
    }
    
    void initCams(int nbCam){
	pc = new Point2D[nbCam];
	lpc = new Point2D[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new Point2D.Double();
	    lpc[i] = new Point2D.Double();
	}
    }

    void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		Point2D[] ta = pc;
		pc = new Point2D[ta.length+1];
		System.arraycopy(ta, 0, pc, 0, ta.length);
		pc[pc.length-1] = new Point2D.Double();
		ta = lpc;
		lpc = new Point2D[ta.length+1];
		System.arraycopy(ta, 0, lpc, 0, ta.length);
		lpc[lpc.length-1] = new Point2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new Point2D[1];
		pc[0] = new Point2D.Double();
		lpc = new Point2D[1];
		lpc[0] = new Point2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
    }

    void removeCamera(int index){
 	pc[index] = null;
 	lpc[index] = null;
    }

    void project(int i, int hw, int hh, Camera c, float coef, double px, double py){
	pc[i].setLocation(hw+(x-c.posx)*coef, hh-(y-c.posy)*coef);
    }

    void projectForLens(int i, int hw, int hh, long lx, long ly, float coef, double px, double py){
	lpc[i].setLocation(hw+(x-lx)*coef, hh-(y-ly)*coef);
    }

    double getX(int i){
	return pc[i].getX();
    }

    double getY(int i){
	return pc[i].getY();
    }

    double getlX(int i){
	return lpc[i].getX();
    }

    double getlY(int i){
	return lpc[i].getY();
    }

    Shape getShape(int i){
	return null;
    }

    Shape getlShape(int i){
	return null;
    }

}

class SEGElement extends PathElement {
    
    /* Draw a segment from previous point to (x,y) in virtual space */

    Line2D[] pc;
    Line2D[] lpc;
    
    SEGElement(long x, long y){
	type = DPath.SEG;
	this.x = x;
	this.y = y;
    }

    void initCams(int nbCam){
	pc = new Line2D[nbCam];
	lpc = new Line2D[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new Line2D.Double();
	    lpc[i] = new Line2D.Double();
	}
    }

    void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		Line2D[] ta = pc;
		pc = new Line2D[ta.length+1];
		System.arraycopy(ta, 0, pc, 0, ta.length);
		pc[pc.length-1] = new Line2D.Double();
		ta = lpc;
		lpc = new Line2D[ta.length+1];
		System.arraycopy(ta, 0, lpc, 0, ta.length);
		lpc[lpc.length-1] = new Line2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new Line2D[1];
		pc[0] = new Line2D.Double();
		lpc = new Line2D[1];
		lpc[0] = new Line2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
    }

    void removeCamera(int index){
 	pc[index] = null;
 	lpc[index] = null;
    }

    void project(int i, int hw, int hh, Camera c, float coef, double px, double py){
	pc[i].setLine(px, py, hw+(x-c.posx)*coef, hh-(y-c.posy)*coef);
    }

    void projectForLens(int i, int hw, int hh, long lx, long ly, float coef, double px, double py){
	lpc[i].setLine(px, py, hw+(x-lx)*coef, hh-(y-ly)*coef);
    }

    double getX(int i){
	return pc[i].getX2();
    }

    double getY(int i){
	return pc[i].getY2();
    }

    double getlX(int i){
	return lpc[i].getX2();
    }

    double getlY(int i){
	return lpc[i].getY2();
    }

    Shape getShape(int i){
	return pc[i];
    }

    Shape getlShape(int i){
	return lpc[i];
    }

}

class QDCElement extends PathElement {

    /* Draw a quadratic curve from previous point to (x,y) in virtual space,
       controlled by point (ctrlx, ctrly) */

    long ctrlx;
    long ctrly;

    QuadCurve2D[] pc;
    QuadCurve2D[] lpc;

    QDCElement(long x, long y, long ctrlx, long ctrly){
	type = DPath.QDC;
	this.x = x;
	this.y = y;
	this.ctrlx = ctrlx;
	this.ctrly = ctrly;
    }

    void initCams(int nbCam){
	pc = new QuadCurve2D[nbCam];
	lpc = new QuadCurve2D[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new QuadCurve2D.Double();
	    lpc[i] = new QuadCurve2D.Double();
	}
    }

    void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		QuadCurve2D[] ta = pc;
		pc = new QuadCurve2D[ta.length+1];
		System.arraycopy(ta, 0, pc, 0, ta.length);
		pc[pc.length-1] = new QuadCurve2D.Double();
		ta = lpc;
		lpc = new QuadCurve2D[ta.length+1];
		System.arraycopy(ta, 0, lpc, 0, ta.length);
		lpc[lpc.length-1] = new QuadCurve2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new QuadCurve2D[1];
		pc[0] = new QuadCurve2D.Double();
		lpc = new QuadCurve2D[1];
		lpc[0] = new QuadCurve2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
    }

    void removeCamera(int index){
 	pc[index] = null;
 	lpc[index] = null;
    }
    
    void project(int i, int hw, int hh, Camera c, float coef, double px, double py){
	pc[i].setCurve(px, py, hw+(ctrlx-c.posx)*coef, hh-(ctrly-c.posy)*coef, hw+(x-c.posx)*coef, hh-(y-c.posy)*coef);
    }

    void projectForLens(int i, int hw, int hh, long lx, long ly, float coef, double px, double py){
	lpc[i].setCurve(px, py, hw+(ctrlx-lx)*coef, hh-(ctrly-ly)*coef, hw+(x-lx)*coef, hh-(y-ly)*coef);
    }

    double getX(int i){
	return pc[i].getX2();
    }

    double getY(int i){
	return pc[i].getY2();
    }

    double getlX(int i){
	return lpc[i].getX2();
    }

    double getlY(int i){
	return lpc[i].getY2();
    }

    Shape getShape(int i){
	return pc[i];
    }

    Shape getlShape(int i){
	return lpc[i];
    }

}

class CBCElement extends PathElement {

    /* Draw a cubic curve from previous point to (x,y) in virtual space,
       controlled by points (ctrlx1, ctrly1) and (ctrlx2, ctrly2) */

    long ctrlx1;
    long ctrly1;
    long ctrlx2;
    long ctrly2;

    CubicCurve2D[] pc;
    CubicCurve2D[] lpc;

    CBCElement(long x, long y, long ctrlx1, long ctrly1, long ctrlx2, long ctrly2){
	type = DPath.CBC;
	this.x = x;
	this.y = y;
	this.ctrlx1 = ctrlx1;
	this.ctrly1 = ctrly1;
	this.ctrlx2 = ctrlx2;
	this.ctrly2 = ctrly2;
    }

    void initCams(int nbCam){
	pc = new CubicCurve2D[nbCam];
	lpc = new CubicCurve2D[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new CubicCurve2D.Double();
	    lpc[i] = new CubicCurve2D.Double();
	}
    }

    void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		CubicCurve2D[] ta = pc;
		pc = new CubicCurve2D[ta.length+1];
		System.arraycopy(ta, 0, pc, 0, ta.length);
		pc[pc.length-1] = new CubicCurve2D.Double();
		ta = lpc;
		lpc = new CubicCurve2D[ta.length+1];
		System.arraycopy(ta, 0, lpc, 0, ta.length);
		lpc[lpc.length-1] = new CubicCurve2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new CubicCurve2D[1];
		pc[0] = new CubicCurve2D.Double();
		lpc = new CubicCurve2D[1];
		lpc[0] = new CubicCurve2D.Double();
	    }
	    else {System.err.println("DPath:Error while adding camera "+verifIndex);}
	}
    }

    void removeCamera(int index){
 	pc[index] = null;
 	lpc[index] = null;
    }

    void project(int i, int hw, int hh, Camera c, float coef, double px, double py){
	pc[i].setCurve(px, py,
		       hw+(ctrlx1-c.posx)*coef, hh-(ctrly1-c.posy)*coef,
		       hw+(ctrlx2-c.posx)*coef, hh-(ctrly2-c.posy)*coef,
		       hw+(x-c.posx)*coef, hh-(y-c.posy)*coef);
    }

    void projectForLens(int i, int hw, int hh, long lx, long ly, float coef, double px, double py){
	lpc[i].setCurve(px, py,
		       hw+(ctrlx1-lx)*coef, hh-(ctrly1-ly)*coef,
		       hw+(ctrlx2-lx)*coef, hh-(ctrly2-ly)*coef,
		       hw+(x-lx)*coef, hh-(y-ly)*coef);
    }

    double getX(int i){
	return pc[i].getX2();
    }

    double getY(int i){
	return pc[i].getY2();
    }

    double getlX(int i){
	return lpc[i].getX2();
    }

    double getlY(int i){
	return lpc[i].getY2();
    }

    Shape getShape(int i){
	return pc[i];
    }

    Shape getlShape(int i){
	return lpc[i];
    }

}
