/*   FILE: VPoint.java
 *   DATE OF CREATION:   Jul 20 2000
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

import net.claribole.zvtm.glyphs.projection.ProjectedCoords;

import com.xerox.VTM.engine.Camera;

/**
 * Point. Actually, a rectangle with constant size of 1.
 * @author Emmanuel Pietriga
 **/

public class VPoint extends Glyph {

    ProjectedCoords[] pc;

    public VPoint(){
	vx=0;
	vy=0;	
	size=1;   //radius of the bounding circle
	setColor(Color.white);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param c color
        */
    public VPoint(long x,long y, Color c){
        vx=x;
        vy=y;
        vz = 0;
        size=1;   //radius of the bounding circle
        setColor(c);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index
        *@param c color
        */
    public VPoint(long x,long y, int z, Color c){
        vx=x;
        vy=y;
        vz = z;
        size=1;   //radius of the bounding circle
        setColor(c);
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
	    else {System.err.println("VPoint:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjectedCoords[1];
		pc[0]=new ProjectedCoords();
	    }
	    else {System.err.println("VPoint:Error while adding camera "+verifIndex);}
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

    /** Cannot be resized (it makes on sense). */
    public void sizeTo(float factor){}

    /** Cannot be resized (it makes on sense). */
    public void reSize(float factor){}

    /** Cannot be reoriented (it makes on sense). */
    public void orientTo(float angle){}

    public float getSize(){return 1.0f;}

    public float getOrient(){return 0;}

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (x==pc[camIndex].cx && y==pc[camIndex].cy){return true;}
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

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);  
	g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);  
	g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
    }

    public Object clone(){
	VPoint res=new VPoint(vx,vy,color);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

    public void highlight(boolean b, Color selectedColor){}

}
