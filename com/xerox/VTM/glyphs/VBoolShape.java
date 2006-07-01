/*   FILE: VBoolShape.java
 *   DATE OF CREATION:   Oct 03 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 * $Id: VBoolShape.java,v 1.8 2006/03/17 17:45:22 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

  /**
   * Boolean shape - defined by a main glyph and a list of boolean operations (applied according to their order in the constructor's array)
   * -right now we only support RectangularShape derivatives (Ellipse, Rectangle)
   * @author Emmanuel Pietriga
   */

public class VBoolShape extends Glyph implements Cloneable {

    /**list of boolean operations (applied in the order given by the array)*/
    BooleanOps[] booleanShapes;
    /**main shape size in virtual space*/
    long szx,szy;
    /**1=ellipse 2=rectangle*/
    int shapeType;

    /**array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjBoolean[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude in virtual space
     *@param sx horizontal size in virtual space
     *@param sy vertical size in virtual space
     *@param st shape type //1=ellipse 2=rectangle
     *@param b array of boolean operations
     *@param c main shape's color
     */
    public VBoolShape(long x,long y,float z,long sx,long sy,int st,BooleanOps[] b,Color c){
	vx=x;
	vy=y;
	vz=z;
	szx=sx;
	szy=sy;
	shapeType=st;
	booleanShapes=b;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjBoolean[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjBoolean();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjBoolean[] ta=pc;
		pc=new ProjBoolean[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjBoolean();
	    }
	    else {System.err.println("VBoolShape:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjBoolean[1];
		pc[0]=new ProjBoolean();
	    }
	    else {System.err.println("VBoolShape:Error while adding camera "+verifIndex);}
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

    /**orientation is disabled*/
    public void orientToNS(float angle){}

    /**size is disabled*/
    public float getSize(){return 0;}

    /**size is disabled*/
    public void sizeTo(float radius){}

    /**size is disabled*/
    public void sizeToNS(float radius){}

    /**get full width*/
    public long getWidth(){return szx;}

    /**get full height*/
    public long getHeight(){return szy;}

    /**size is disabled*/
    public void reSize(float factor){}

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if (pc[camIndex].mainArea.contains(x,y)){return true;}
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
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	for (int j=0;j<booleanShapes.length;j++){
	    booleanShapes[j].project(coef,pc[i].cx,pc[i].cy);
	}
	pc[i].cszx=szx*coef;
	pc[i].cszy=szy*coef;
	switch (shapeType) {
	case 1:{//ellipse
	    pc[i].mainArea=new Area(new Ellipse2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	    break;
	}
	case 2:{//rectangle
	    pc[i].mainArea=new Area(new Rectangle2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	    break;
	}
	default:{//ellipse as default
	    pc[i].mainArea=new Area(new Ellipse2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	}
	}
	for (int j=0;j<booleanShapes.length;j++){
	    switch (booleanShapes[j].opType) {
	    case 1:{
		pc[i].mainArea.add(booleanShapes[j].ar);
		break;
	    }
	    case 2:{
		pc[i].mainArea.subtract(booleanShapes[j].ar);
		break;
	    }
	    case 3:{
		pc[i].mainArea.intersect(booleanShapes[j].ar);
		break;
	    }
	    case 4:{
		pc[i].mainArea.exclusiveOr(booleanShapes[j].ar);
		break;
	    }
	    default:{
		System.err.println("Error: VBoolShape: boolean operation not defined");
	    }
	    }
	}
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(lensWidth/2) + Math.round((vx-lensx)*coef);
	pc[i].cy=(lensHeight/2) - Math.round((vy-lensy)*coef);
	for (int j=0;j<booleanShapes.length;j++){
	    booleanShapes[j].projectForLens(coef,pc[i].lcx,pc[i].lcy);
	}
	pc[i].lcszx=szx*coef;
	pc[i].lcszy=szy*coef;
	switch (shapeType) {
	case 1:{//ellipse
	    pc[i].lmainArea=new Area(new Ellipse2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	    break;
	}
	case 2:{//rectangle
	    pc[i].lmainArea=new Area(new Rectangle2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	    break;
	}
	default:{//ellipse as default
	    pc[i].lmainArea=new Area(new Ellipse2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	}
	}
	for (int j=0;j<booleanShapes.length;j++){
	    switch (booleanShapes[j].opType) {
	    case 1:{
		pc[i].lmainArea.add(booleanShapes[j].lar);
		break;
	    }
	    case 2:{
		pc[i].lmainArea.subtract(booleanShapes[j].lar);
		break;
	    }
	    case 3:{
		pc[i].lmainArea.intersect(booleanShapes[j].lar);
		break;
	    }
	    case 4:{
		pc[i].lmainArea.exclusiveOr(booleanShapes[j].lar);
		break;
	    }
	    default:{
		System.err.println("Error: VBoolShape: boolean operation not defined");
	    }
	    }
	}
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].mainArea.getBounds().width>2) && (pc[i].mainArea.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.translate(dx,dy);
		g.fill(pc[i].mainArea);
		g.translate(-dx,-dy);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null){
		    g.setStroke(stroke);
		    g.translate(dx,dy);
		    g.draw(pc[i].mainArea);
		    g.translate(-dx,-dy);
		    g.setStroke(stdS);
		}
		else {
		    g.translate(dx,dy);
		    g.draw(pc[i].mainArea);
		    g.translate(-dx,-dy);
		}		   
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lmainArea.getBounds().width>2) && (pc[i].lmainArea.getBounds().height>2)){
	    if (filled){
		g.setColor(this.color);
		g.translate(dx,dy);
		g.fill(pc[i].lmainArea);
		g.translate(-dx,-dy);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null){
		    g.setStroke(stroke);
		    g.translate(dx,dy);
		    g.draw(pc[i].lmainArea);
		    g.translate(-dx,-dy);
		    g.setStroke(stdS);
		}
		else {
		    g.translate(dx,dy);
		    g.draw(pc[i].lmainArea);
		    g.translate(-dx,-dy);
		}		   
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    /**public only because accessed by svg export module*/
    public int getMainShapeType(){
	return shapeType;
    }

    /**public only because accessed by svg export module*/
    public BooleanOps[] getOperations(){
	return booleanShapes;
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	return new VBoolShape(vx,vy,0,szx,szy,shapeType,booleanShapes,color);
    }
}
