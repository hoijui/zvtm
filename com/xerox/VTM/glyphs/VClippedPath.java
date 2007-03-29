/*   FILE: VClippedPath.java
 *   DATE OF CREATION:   Wed Feb 05 10:50:50 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import com.xerox.VTM.engine.LongPoint;

/**
 * Experimental variation on VPath. This version implements an experimental clipping algorithm that should enhance performances (quicker rendering) when only part of the path is actually seen (draw only curves/segments that are seen).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VPath
 **/

public class VClippedPath extends VPath {

    private ClippedPathSeg[] segs;
    LongPoint firstPoint;
    LongPoint lastPoint;

    private void addSeg(LongPoint p,short segType){
	if (firstPoint==null){
	    firstPoint=p;
	}
	else if (segs==null){
	    segs=new ClippedPathSeg[1];
	    segs[0]=new ClippedPathSeg((firstPoint.x+p.x)/2,(firstPoint.y+p.y)/2,(p.x-firstPoint.x)/2,-(p.y-firstPoint.y)/2,segType,p.x,-p.y);
	    lastPoint=p;
	}
	else {
	    ClippedPathSeg[] tmpArray=new ClippedPathSeg[segs.length+1];
	    System.arraycopy(segs,0,tmpArray,0,segs.length);
	    segs=tmpArray;
	    segs[segs.length-1]=new ClippedPathSeg((lastPoint.x+p.x)/2,(lastPoint.y+p.y)/2,(p.x-lastPoint.x)/2,-(p.y-lastPoint.y)/2,segType,p.x,-p.y);
	    lastPoint=p;
	}
    }

    public VClippedPath(){
	super();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c color
     */
    public VClippedPath(long x,long y,float z,Color c){
	super(x,y,z,c);
    }

    /**
     *@param z altitude
     *@param c color
     *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
     */
    public VClippedPath(float z,Color c,String svg){
	super(z,c,svg);
    }

    /**
     * new path, begins at (vx,vy)
     */
    public void resetPath(){
	super.resetPath();
	firstPoint=null;
	segs=null;
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
 	addSeg(new LongPoint(lp.x,lp.y),ClippedPathSeg.SEG_TYPE_SEG);
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
	    addSeg(new LongPoint(x1,y1),ClippedPathSeg.SEG_TYPE_QD1);
	    addSeg(new LongPoint(x,y),ClippedPathSeg.SEG_TYPE_QD2);
	    lp.setLocation(x,y);
	}
	else {
	    path.quadTo(lp.x+x1,-(lp.y+y1),lp.x+x,-(lp.y+y));
	    addSeg(new LongPoint(lp.x+x1,lp.y+y1),ClippedPathSeg.SEG_TYPE_QD1);
	    addSeg(new LongPoint(lp.x+x,lp.y+y),ClippedPathSeg.SEG_TYPE_QD2);
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
	    addSeg(new LongPoint(x1,y1),ClippedPathSeg.SEG_TYPE_CB1);
	    addSeg(new LongPoint(x2,y2),ClippedPathSeg.SEG_TYPE_CB2);
	    addSeg(new LongPoint(x,y),ClippedPathSeg.SEG_TYPE_CB3);
	    lp.setLocation(x,y);
	}
	else {
	    path.curveTo(lp.x+x1,-(lp.y+y1),lp.x+x2,-(lp.y+y2),lp.x+x,-(lp.y+y));
	    addSeg(new LongPoint(lp.x+x1,lp.y+y1),ClippedPathSeg.SEG_TYPE_CB1);
	    addSeg(new LongPoint(lp.x+x2,lp.y+y2),ClippedPathSeg.SEG_TYPE_CB2);
	    addSeg(new LongPoint(lp.x+x,lp.y+y),ClippedPathSeg.SEG_TYPE_CB3);
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
 	addSeg(new LongPoint(lp.x,lp.y),ClippedPathSeg.SEG_TYPE_JMP);
	if (getPathLength()==1){vx=lp.x;vy=lp.y;}
	realHotSpot.setLocation((vx+lp.x)/2,(vy+lp.y)/2);
	computeSize();
    }


    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (visibilityHasChanged()){constructVisiblePath();}
	g.setColor(this.color);
// 	if (true){//replace by something using projected size (so that we do not paint it if too small)
 	    at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
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
	if (visibilityHasChanged()){constructVisiblePath();}
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
	boolean res=false;
	if (forcedDrawing){return true;}
	else {
	    if (((realHotSpot.x>=wb) && (realHotSpot.x<=eb) && (realHotSpot.y>=sb) && (realHotSpot.y<=nb))
		|| (((realHotSpot.x-drawingRadius)<=eb) && ((realHotSpot.x+drawingRadius)>=wb)
		    && ((realHotSpot.y-drawingRadius)<=nb) && ((realHotSpot.y+drawingRadius)>=sb))){
		try {
		    for (int j=0;j<segs.length;j++){
			if (((segs[j].x>=wb) && (segs[j].x<=eb) && (segs[j].y>=sb) && (segs[j].y<=nb)) || (((segs[j].x-segs[j].w)<=eb) && ((segs[j].x+segs[j].w)>=wb) && ((segs[j].y-segs[j].h)<=nb) && ((segs[j].y+segs[j].h)>=sb))){
			    segs[j].setVisible(true);
			    res=true;
			}
			else {segs[j].setVisible(false);}
		    }
		}
		catch (NullPointerException ex){return false;}
	    }
	}
	return res;
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	boolean res=false;
	if (forcedDrawing){return true;}
	else {
	    if (((realHotSpot.x>=wb) && (realHotSpot.x<=eb) && (realHotSpot.y>=sb) && (realHotSpot.y<=nb))
		|| (((realHotSpot.x+drawingRadius)<=eb) && ((realHotSpot.x-drawingRadius)>=wb)
		    && ((realHotSpot.y+drawingRadius)<=nb) && ((realHotSpot.y-drawingRadius)>=sb))){
		try {
		    for (int j=0;j<segs.length;j++){
			if (((segs[j].x>=wb) && (segs[j].x<=eb) && (segs[j].y>=sb) && (segs[j].y<=nb)) || (((segs[j].x+segs[j].w)<=eb) && ((segs[j].x-segs[j].w)>=wb) && ((segs[j].y+segs[j].h)<=nb) && ((segs[j].y-segs[j].h)>=sb))){
			    segs[j].setVisible(true);
			    res=true;
			}
			else {segs[j].setVisible(false);}
		    }
		}
		catch (NullPointerException ex){return false;}
	    }
	}
	return res;
    }

    void printVis(){
	System.err.print("[");
	for (int i=0;i<segs.length;i++){
	    System.err.print(segs[i].visible+",");
	}
	System.err.print("]");
	System.err.println(visibilityHasChanged());
    }

    private boolean visibilityHasChanged(){
	for (int i=0;i<segs.length;i++){
	    if (segs[i].wasVisible!=segs[i].visible){return true;}
	}
	return false;
    }

    void constructVisiblePath(){
	path=new GeneralPath();
	path.moveTo(firstPoint.x,-firstPoint.y);
	for (int i=0;i<segs.length;){
	    if (segs[i].type==ClippedPathSeg.SEG_TYPE_CB1){
		if (segs[i].visible || segs[i+1].visible || segs[i+2].visible){
		    path.curveTo(segs[i].java2Dx,segs[i].java2Dy,segs[i+1].java2Dx,segs[i+1].java2Dy,segs[i+2].java2Dx,segs[i+2].java2Dy);
		}
		i+=3;
	    }
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_QD1){
		if (segs[i].visible || segs[i+1].visible){
		    path.quadTo(segs[i].java2Dx,segs[i].java2Dy,segs[i+1].java2Dx,segs[i+1].java2Dy);
		}
		i+=2;
	    }
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_SEG){
		if (segs[i].visible){
		    path.lineTo(segs[i].java2Dx,segs[i].java2Dy);
		}
		i++;
	    }
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_JMP){
		if (segs[i].visible){
		    path.moveTo(segs[i].java2Dx,segs[i].java2Dy);
		}
		i++;
	    }
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_CB2){i+=2;}
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_QD2){i++;}
	    else if (segs[i].type==ClippedPathSeg.SEG_TYPE_CB3){i++;}
	}	
    }

    public Object clone(){
	VClippedPath res=new VClippedPath();
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }
    
}

