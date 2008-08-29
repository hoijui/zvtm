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
 * $Id$
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import net.claribole.zvtm.glyphs.projection.ProjectedCoords;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.svg.SVGReader;


/**
 * <strong>As of 0.9.7-SNAPSHOT, DPathST should be prefered to VPathST.</strong>
 * General path: made of an arbitrary number of segments, quadratic curves, cubic curves, and gaps. Can neither be resized nor reoriented (for now). This glyph does not follow the standard object model: (vx,vy) are the coordinates of the path's first point. VPaths do not fire cursor entry/exit events, but it is possible to detect that a cursor is overlapping a VPath by explicitely calling VCursor.interesctsPath(VPath p) and related methods.
 * @author Emmanuel Pietriga
 *@see net.claribole.zvtm.glyphs.VPathST
 *@see net.claribole.zvtm.glyphs.DPath
 *@see net.claribole.zvtm.glyphs.DPathST
 *@see com.xerox.VTM.glyphs.VQdCurve
 *@see com.xerox.VTM.glyphs.VCbCurve
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.engine.VCursor#intersectsVPath(VPath p)
 */

public class VPath extends Glyph {

    /** Create a copy of a VPath, offset by (x,y). */
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
    
    /** For internal use. Dot not tamper with. Made public for outside package subclassing. */
    public AffineTransform at;
    
    /** For internal use. Dot not tamper with. Made public for outside package subclassing. */
    public ProjectedCoords[] pc;
    
    /** For internal use. Dot not tamper with. Made public for outside package subclassing. */
    public GeneralPath path;
    
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
        *@param x start coordinate in virtual space
        *@param y start coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c color
        */
    public VPath(long x, long y, int z, Color c){
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
	 *@param pi PathIterator describing this path (virtual space coordinates)
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c color
     */
    public VPath(PathIterator pi, int z, Color c){
		vz = z;
		double[] cds = new double[6];
		// if first instruction is a jump, make it the start point
		if (pi.currentSegment(cds) == PathIterator.SEG_MOVETO){
			vx = (long)cds[0];
			vy = (long)cds[1];
			pi.next();
		}
		else {
			vx = 0;
			vy = 0;
		}
		path = new GeneralPath();
        lp = new LongPoint(vx,vy);
        realHotSpot = new LongPoint(vx,vy);
        path.moveTo(vx,-vy);        
		int type;
	    while (!pi.isDone()){
			type = pi.currentSegment(cds);
			switch (type){
			case PathIterator.SEG_CUBICTO:{
				addCbCurve((long)cds[4],(long)cds[5],(long)cds[0],(long)cds[1],(long)cds[2],(long)cds[3],true);
				break;
			}
			case PathIterator.SEG_QUADTO:{
				addQdCurve((long)cds[2],(long)cds[3],(long)cds[0],(long)cds[1],true);
				break;
			}
			case PathIterator.SEG_LINETO:{
				addSegment((long)cds[0],(long)cds[1],true);
				break;
			}
			case PathIterator.SEG_MOVETO:{
				jump((long)cds[0],(long)cds[1],true);
				break;
			}
			}
			pi.next();
	    }
		sensit = false;
		setColor(c);
	}

//    /**
//        *@param z z-index (pass 0 if you do not use z-ordering)
//        *@param c color
//        *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
//        */
//    public VPath(int z,Color c,String svg){
//        vx=0;
//        vy=0;
//        vz=z;
//        sensit=false;
//        setColor(c);
//        this.setSVGPath(svg);
//    }

//    /** Reset path and assign it new coordinates according to what is specified in the SVG expression.
//     *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
//     */
//    public void setSVGPath(String svg){
//	resetPath();
//	SVGReader.createPath(svg,this);
//    }

    /** New path, will begin at (vx,vy)
     */
    public void resetPath(){
	path=new GeneralPath();
	lp=new LongPoint(0,0);
	realHotSpot=new LongPoint(0,0);
	path.moveTo(0,0);
	computeSize();
    }
    
    /** Add a new segment to the path, from current point to point (x,y)
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

    /** Add a new quadratic curve to the path, from current point to point (x,y), with control point (x1,y1)
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

    /** Add a new cubic curve to the path, from current point to point (x,y), with control points (x1,y1) and (x2,y2)
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

    /** "jump" to point (x,y) without drawing anything
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
    
    public void initCams(int nbCam){
	pc=new ProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjectedCoords();
	}
    }

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
    }
    
    /** Cannot be resized. */
    public void sizeTo(float factor){}

    /** Cannot be resized. */
    public void reSize(float factor){}

    /** Cannot be reoriented. */
    public void orientTo(float angle){}

    /** Get this path's size (distance between first and last point).
     *@return the distance between first and last point
     */
    public float getSize(){
	return size;
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
    
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((-c.posy)*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx=(lensWidth/2)+Math.round((-lensx)*coef);
	pc[i].lcy=(lensHeight/2)-Math.round((-lensy)*coef);
    }

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

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if (forcedDrawing){return true;}
	else {
	    if ((realHotSpot.x>=wb) && (realHotSpot.x<=eb) && (realHotSpot.y>=sb) && (realHotSpot.y<=nb)){
		// if glyph hotspot is in the region, it is obviously visible
		return true;
	    }
	    else {// if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning 
		//   that some glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		return (((realHotSpot.x-drawingRadius)<=eb) && ((realHotSpot.x+drawingRadius)>=wb)
			&& ((realHotSpot.y-drawingRadius)<=nb) && ((realHotSpot.y+drawingRadius)>=sb));
	    }
	}
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if (forcedDrawing){return true;}
	else {
	    return ((realHotSpot.x >= wb) && (realHotSpot.x <= eb) &&
		    (realHotSpot.y >= sb) && (realHotSpot.y <= nb)
		    && ((realHotSpot.x+drawingRadius) <= eb) && ((realHotSpot.x-drawingRadius) >= wb)
		    && ((realHotSpot.y+drawingRadius) <= nb) && ((realHotSpot.y-drawingRadius) >= sb));
	}
    }


    /** Set the threshold below which the path should be drawn. Default is 1.2.
     *@see #setForcedDrawing(boolean b)
     *@see #getDrawingFactor()
     */
    public void setDrawingFactor(float f){drawingFactor=f;}

    /** Get the threshold below which the path should be drawn. Default is 1.2.
     *@see #setDrawingFactor(float f)
     *@see #setForcedDrawing(boolean b)
     */
    public float getDrawingFactor(){
	return drawingFactor;
    }

    /** Force drawing of path, even if not visible. The algorithm in charge of detecting whether a glyph should be drawn or not is not completely reliable for weird paths.<br>Default is false. Use only if absolutely sure that your path is not displayed whereas it should be. Also try increasing drawing factor before resorting to forced drawing.
     *@see #setDrawingFactor(float f)
     */
    public void setForcedDrawing(boolean b){forcedDrawing=b;}
    
    /** Indicate whether forced drawing is enabled
     *@see #setForcedDrawing(boolean b)
     */
    public boolean getForcedDrawing(){return forcedDrawing;}

    /** Get a Java2D path iterator for this VPath. */
    public PathIterator getJava2DPathIterator(){return path.getPathIterator(null);}

    /** Get the underlying Java2D General Path used for actual drawing. */
    public GeneralPath getJava2DGeneralPath(){return path;}

    /** Not implemented yet. */
    public Object clone(){
	VPath res=new VPath();
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

    public void highlight(boolean b, Color selectedColor){}
    
}

