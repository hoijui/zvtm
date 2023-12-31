/*   FILE: VEllipse.java
 *   DATE OF CREATION:   Oct 14 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
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

import net.claribole.zvtm.glyphs.projection.ProjEllipse;

import com.xerox.VTM.engine.Camera;

/**
 * Ellipse. This version is the most efficient, but it can not be made translucent (see VEllipseST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VEllipseST
 *@see com.xerox.VTM.glyphs.VCircle
 *@see com.xerox.VTM.glyphs.VCircleST
 */

public class VEllipse extends ClosedShape implements RectangularShape {

    /*half width and height in virtual space*/
    long vw,vh;
    /*aspect ratio (width divided by height)*/
    float ar;

    /*array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjEllipse[] pc;

    /**
     *creates a new default white ellipse
     */
    public VEllipse(){
	vx=0;
	vy=0;
	vz=0;
	vw=10;
	vh=10;
	setColor(Color.white);
	setBorderColor(Color.black);
	computeSize();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering) in virtual space
     *@param sx horizontal axis radius in virtual space
     *@param sy vertical axis radius in virtual space
     *@param c fill color
     */
    public VEllipse(long x,long y, int z,long sx,long sy,Color c){
	vx=x;
	vy=y;
	vz=z;
	vw=sx;
	vh=sy;
	orient=0;
	setColor(c);
	setBorderColor(Color.black);
	computeSize();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering) in virtual space
     *@param sx horizontal axis radius in virtual space
     *@param sy vertical axis radius in virtual space
     *@param c fill color
     *@param bc border color
     */
    public VEllipse(long x, long y, int z, long sx, long sy, Color c, Color bc){
	vx=x;
	vy=y;
	vz=z;
	vw=sx;
	vh=sy;
	orient=0;
	setColor(c);
	setBorderColor(bc);
	computeSize();
    }

    public void initCams(int nbCam){
	pc=new ProjEllipse[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjEllipse();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjEllipse[] ta=pc;
		pc=new ProjEllipse[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjEllipse();
	    }
	    else {System.err.println("VEllipse:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjEllipse[1];
		pc[0]=new ProjEllipse();
	    }
	    else {System.err.println("VEllipse:Error while adding camera "+verifIndex);}
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

    /** Cannot be reoriented. */
    public float getOrient(){return 0;}

    public void orientTo(float angle){}

    void computeSize(){
	size=Math.max(vw,vh);
	ar=(float)vw/(float)vh;
    }

    public float getSize(){return size;}

    public void sizeTo(float radius){
	size=radius;
	if (vw>=vh){vw=(long)size;vh=(long)(vw/ar);}
	else {vh=(long)size;vw=(long)(vh*ar);}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void setWidth(long w){ 
	vw=w;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void setHeight(long h){
	vh=h;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public long getWidth(){return vw;}

    public long getHeight(){return vh;}

    public void reSize(float factor){
	size*=factor;
	if (vw>=vh){vw=(long)size;vh=(long)(vw/ar);}
	else {vh=(long)size;vw=(long)(vh*ar);}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
	}

    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (pc[camIndex].ellipse.contains(jpx, jpy)){return true;}
        else {return false;}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].ellipse.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
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
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	pc[i].cvw=vw*coef;
	pc[i].cvh=vh*coef;
	pc[i].ellipse.setFrame(pc[i].cx-vw*coef,pc[i].cy-vh*coef,2*pc[i].cvw,2*pc[i].cvh);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	pc[i].lcvw=vw*coef;
	pc[i].lcvh=vh*coef;
	pc[i].lellipse.setFrame(pc[i].lcx-vw*coef,pc[i].lcy-vh*coef,2*pc[i].lcvw,2*pc[i].lcvh);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].ellipse.getBounds().width>2) && (pc[i].ellipse.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fill(pc[i].ellipse);
		g.translate(-dx, -dy);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null){
		    g.setStroke(stroke);
		    g.translate(dx, dy);
		    g.draw(pc[i].ellipse);
		    g.translate(-dx, -dy);
		    g.setStroke(stdS);
		}
		else {
		    g.translate(dx, dy);
		    g.draw(pc[i].ellipse);
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
	if ((pc[i].lellipse.getBounds().width>2) && (pc[i].lellipse.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fill(pc[i].lellipse);
		g.translate(-dx, -dy);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null){
		    g.setStroke(stroke);
		    g.translate(dx, dy);
		    g.draw(pc[i].lellipse);
		    g.translate(-dx, -dy);
		    g.setStroke(stdS);
		}
		else {
		    g.translate(dx, dy);
		    g.draw(pc[i].lellipse);
		    g.translate(-dx, -dy);
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	VEllipse res=new VEllipse(vx,vy,0,vw,vh,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
