/*   FILE: VRectangle.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import net.claribole.zvtm.glyphs.projection.RProjectedCoordsP;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;


/**
 * Rectangle. This version is the most efficient, but it can neither be reoriented (see VRectangleOr*) nor made translucent (see VRectangle*ST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VRectangleOr
 *@see com.xerox.VTM.glyphs.VRectangleOrST
 *@see com.xerox.VTM.glyphs.VRectangleST
 */

public class VRectangle extends ClosedShape implements RectangularShape {

    /** For internal use. Made public for easier outside package subclassing. Half width in virtual space.*/
    public long vw;
    /** For internal use. Made public for easier outside package subclassing. Half height in virtual space.*/
    public long vh;
    /* For internal use. Made public for easier outside package subclassing. Aspect ratio (width divided by height). */
    public float ar;

    /** For internal use. Made public for easier outside package subclassing. */
    public RProjectedCoordsP[] pc;

    public VRectangle(){
	vx=0;
	vy=0;
	vz=0;
	vw=10;
	vh=10;
	computeSize();
	ar=(float)vw/(float)vh;
	orient=0;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public VRectangle(long x,long y, int z,long w,long h,Color c){
	vx=x;
	vy=y;
	vz=z;
	vw=w;
	vh=h;
	computeSize();
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	orient=0;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param bc border color
     */
    public VRectangle(long x, long y, int z, long w, long h, Color c, Color bc){
	vx=x;
	vy=y;
	vz=z;
	vw=w;
	vh=h;
	computeSize();
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	orient=0;
	setColor(c);
	setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new RProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoordsP();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		RProjectedCoordsP[] ta=pc;
		pc=new RProjectedCoordsP[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new RProjectedCoordsP();
	    }
	    else {System.err.println("VRectangle:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoordsP[1];
		pc[0]=new RProjectedCoordsP();
	    }
	    else {System.err.println("VRectangle:Error while adding camera "+verifIndex);}
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

    public long getWidth(){return vw;}

    public long getHeight(){return vh;}

    void computeSize(){
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    public void sizeTo(float radius){  //new bounding circle radius
	size=radius;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setWidth(long w){ 
	vw=w;
	ar=(float)vw/(float)vh;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setHeight(long h){
	vh=h;
	ar=(float)vw/(float)vh;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){ //resizing factor
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public boolean fillsView(long w,long h,int camIndex){//width and height of view - pc[i].c? are JPanel coords
	if ((w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	else {return false;}
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
            (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
        else {return false;}
    }

	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return dvs.intersects(vx-vw, vy-vh, 2*vw, 2*vh);
	}

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
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

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project and translate in JPanel coords
	//translate in JPanel coords
	pc[i].cx = (d.width/2) + Math.round((vx-c.posx)*coef);
	pc[i].cy = (d.height/2) - Math.round((vy-c.posy)*coef);
	//project width and height
	pc[i].cw=Math.round(vw*coef);
	pc[i].ch=Math.round(vh*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project and translate in JPanel coords
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project width and height
	pc[i].lcw = Math.round(vw*coef);
	pc[i].lch = Math.round(vh*coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {	
		    if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
			((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
			// [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
			// the rectangle, in which case the border would not be visible;
			// the fact that the rectangle intersects the viewport has already been tested by the main
			// clipping algorithm
			g.setStroke(stroke);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
			g.setStroke(stdS);
		    }
		}
		else {
		    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);   //outline rectangle
		}
	    }
	}
	else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].cw<=1){
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
	    }
	    else if (pc[i].ch<=1){
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
			((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
			// see [C1] above for explanations about this test
			g.setStroke(stroke);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
			g.setStroke(stdS);
		    }
		}
		else {
		    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);   //outline rectangle
		}
	    }
	}
	else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].lcw<=1){
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
	    }
	    else if (pc[i].lch<=1){
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
	}
    }

    public Object clone(){
	VRectangle res=new VRectangle(vx, vy, 0, vw, vh, color, borderColor);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
