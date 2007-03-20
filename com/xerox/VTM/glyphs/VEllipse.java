/*   FILE: VEllipse.java
 *   DATE OF CREATION:   Oct 14 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
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
 * $Id: VEllipse.java,v 1.8 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

  /**
   * Ellipse - cannot be reoriented
   * @author Emmanuel Pietriga
   */

public class VEllipse extends Glyph implements RectangularShape,Cloneable {

    /**half width and height in virtual space*/
    long vw,vh;
    /**aspect ratio (width divided by height)*/
    float ar;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
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
     *@param z altitude in virtual space
     *@param sx horizontal axis radius in virtual space
     *@param sy vertical axis radius in virtual space
     *@param c main shape's color
     */
    public VEllipse(long x,long y,float z,long sx,long sy,Color c){
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

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjEllipse[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjEllipse();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
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

    /**orientation is disabled*/
    public float getOrient(){return 0;}

    /**orientation is disabled*/
    public void orientTo(float angle){}

    /**size is bounding circle radius*/
    void computeSize(){
	size=Math.max(vw,vh);
	ar=(float)vw/(float)vh;
    }

    /**size is disabled*/
    public float getSize(){return size;}

    /**size is disabled*/
    public void sizeTo(float radius){
	size=radius;
	if (vw>=vh){vw=(long)size;vh=(long)(vw/ar);}
	else {vh=(long)size;vw=(long)(vh*ar);}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**set absolute half width*/
    public void setWidth(long w){ 
	vw=w;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**set absolute half height*/
    public void setHeight(long h){
	vh=h;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get width*/
    public long getWidth(){return vw;}

    /**get height*/
    public long getHeight(){return vh;}

    /**size is disabled*/
    public void reSize(float factor){
	size*=factor;
	if (vw>=vh){vw=(long)size;vh=(long)(vw/ar);}
	else {vh=(long)size;vw=(long)(vh*ar);}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if (pc[camIndex].ellipse.contains(x,y)){return true;}
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

    /**project shape in camera coord sys prior to actual painting*/
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

    /**project shape in camera coord sys prior to actual painting through the lens*/
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

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].ellipse.getBounds().width>2) && (pc[i].ellipse.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.translate(dx, dy);
		g.fill(pc[i].ellipse);
		g.translate(-dx, -dy);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
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
	    g.setColor(borderColor);
	    if (paintBorder){
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

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VEllipse res=new VEllipse(vx,vy,0,vw,vh,color);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
