/*   FILE: VCirImage.java
 *   DATE OF CREATION:   Apr 02 2002
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;
import net.claribole.zvtm.glyphs.projection.ProjCirImage;

/**
 * Circle containing a bitmap.
 * @author Emmanuel Pietriga
 **/

public class VCirImage extends ClosedShape {

    long vw,vh;
    long vs;
    float ar;

    AffineTransform at;

    ProjCirImage[] pc;

    Image image;

    float scaleFactor=1.0f;

    float trueCoef=1.0f;

    float relCoef=1.0f;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param img image to be displayed
     *@param c fill color of bounding circle
     *@param or orientation
     */
    public VCirImage(long x,long y,float z,long s,Image img,Color c,float or){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	image=img;
	ar=((float)image.getWidth(null))/((float)image.getHeight(null));
	size=(float)vs;
	computeSize();
	orient=or;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param s size (width=height) in virtual space
     *@param img image to be displayed
     *@param c fill color of bounding circle
     *@param bc border color of bounding circle
     *@param or orientation
     */
    public VCirImage(long x, long y, float z, long s, Image img, Color c, Color bc, float or){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	image=img;
	ar=((float)image.getWidth(null))/((float)image.getHeight(null));
	size=(float)vs;
	computeSize();
	orient=or;
	setColor(c);
	setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new ProjCirImage[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjCirImage();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjCirImage[] ta=pc;
		pc=new ProjCirImage[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjCirImage();
	    }
	    else {System.err.println("VCirImage:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjCirImage[1];
		pc[0]=new ProjCirImage();
	    }
	    else {System.err.println("VCirImage:Error while adding camera "+verifIndex);}
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

    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void orientToNS(float angle){orient=angle;}

    public float getSize(){return size;}

    void computeSize(){
	//size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
	//vs=(long)Math.round(size);
 	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
 	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
    }

    public void sizeTo(float radius){
	size=radius;
	vs=(long)Math.round(size);
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /** Set the bitmap image to be displayed inside the circle. */
    public void setImage(Image i){
	image=i;
	ar=((float)image.getWidth(null))/((float)image.getHeight(null));
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /** Get the bitmap image displayed inside the circle. */
    public Image getImage(){
	return image;
    }

    /** Set the relative size of the image w.r.t the circle.
     *@param f multiplication factor (positive float &gt; 0) default=1.0
     */
    public void setRelativeImageSize(float f){
	relCoef=f;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /** Get the relative size of the image w.r.t the circle. */
    public float getRelativeImageSize(){
	return relCoef;
    }

    public boolean fillsView(long w,long h,int camIndex){
	if ((Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cs) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(h-pc[camIndex].cy,2))<=pc[camIndex].cs) 
	    && (Math.sqrt(Math.pow(w-pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cs) 
	    && (Math.sqrt(Math.pow(pc[camIndex].cx,2)+Math.pow(pc[camIndex].cy,2))<=pc[camIndex].cs)){return true;}
	else {return false;}
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (Math.sqrt(Math.pow(x-pc[camIndex].cx,2)+Math.pow(y-pc[camIndex].cy,2))<=pc[camIndex].cs){return true;}
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
	//project width and height
	pc[i].cs=Math.round(vs*coef);
	pc[i].cw=Math.round(vw*coef*relCoef);
	pc[i].ch=Math.round(vh*coef*relCoef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	pc[i].lcs=Math.round(vs*coef);
	pc[i].lcw=Math.round(vw*coef*relCoef);
	pc[i].lch=Math.round(vh*coef*relCoef);

    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cs>1){
	    if (filled) {
		g.setColor(this.color);
		g.fillOval(dx+pc[i].cx-pc[i].cs,dy+pc[i].cy-pc[i].cs,2*pc[i].cs,2*pc[i].cs);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].cx-pc[i].cs,dy+pc[i].cy-pc[i].cs,2*pc[i].cs,2*pc[i].cs);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].cx-pc[i].cs,dy+pc[i].cy-pc[i].cs,2*pc[i].cs,2*pc[i].cs);
		}
	    }
	    trueCoef=scaleFactor*coef*relCoef;
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (trueCoef!=1.0f){
		at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));}
		at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
		g.drawImage(image,at,null);
	    }
	    else {
		if (orient==0){g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);}
		else {
		    at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
		    if (trueCoef!=1.0f){at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));}
		    g.drawImage(image,at,null);
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].lcs>1){
	    if (filled) {
		g.setColor(this.color);
		g.fillOval(dx+pc[i].lcx-pc[i].lcs,dy+pc[i].cy-pc[i].lcs,2*pc[i].lcs,2*pc[i].lcs);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].lcx-pc[i].lcs,dy+pc[i].cy-pc[i].lcs,2*pc[i].lcs,2*pc[i].lcs);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].lcx-pc[i].lcs,dy+pc[i].cy-pc[i].lcs,2*pc[i].lcs,2*pc[i].lcs);
		}
	    }
	    trueCoef=scaleFactor*coef*relCoef;
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (trueCoef!=1.0f){
		at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].cy-pc[i].lch);
		if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));}
		at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
		g.drawImage(image,at,null);
	    }
	    else {
		if (orient==0){g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].cy-pc[i].lch,null);}
		else {
		    at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].cy-pc[i].lch);
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
		    if (trueCoef!=1.0f){at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));}
		    g.drawImage(image,at,null);
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].cy,1,1);
	}
    }

    public Object clone(){
	VCirImage res=new VCirImage(vx,vy,0,vs,image,color,orient);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
