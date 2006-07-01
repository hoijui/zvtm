/*   FILE: VImage.java
 *   DATE OF CREATION:   Jan 09 2001
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
 * $Id: VImage.java,v 1.11 2006/03/17 17:45:23 epietrig Exp $
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

/**
 * Image (rectangular) - cannot be reoriented
 * @author Emmanuel Pietriga
 **/

public class VImage extends Glyph implements RectangularShape,Cloneable {

    public static short DRAW_BORDER_NEVER=0;
    public static short DRAW_BORDER_MOUSE_INSIDE=1;
    public static short DRAW_BORDER_ALWAYS=2;

    /**half width and height in virtual space*/
    long vw,vh;
    /**aspect ratio (width divided by height)*/
    float ar;

    AffineTransform at;

    /**draw border policy 0=never draw border 1=draw border if cursor inside 2=always draw border*/
    short drawBorder=DRAW_BORDER_NEVER;

    RProjectedCoordsP[] pc;

    Image image;

    boolean zoomSensitive=true;

    float scaleFactor=1.0f;

    float trueCoef=1.0f;

    /**
     *@param img image to be displayed
     */
    public VImage(Image img){
	vx=0;
	vy=0;
	vz=0;
	image=img;
	vw=image.getWidth(null)/2;
	vh=image.getHeight(null)/2;
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	computeSize();
	orient=0;
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     */
    public VImage(long x,long y,float z,Image img){
	vx=x;
	vy=y;
	vz=z;
	image=img;
	vw=Math.round(image.getWidth(null)/2.0);
	vh=Math.round(image.getHeight(null)/2.0);
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	computeSize();
	orient=0;
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
     */
    public VImage(long x, long y, float z, Image img, double scale){
	vx = x;
	vy = y;
	vz = z;
	image = img;
	vw = Math.round(image.getWidth(null) * scale / 2.0);
	vh = Math.round(image.getHeight(null) * scale / 2.0);
	if (vw==0 && vh==0){ar = 1.0f;}
	else {ar = (float)vw/(float)vh;}
	computeSize();
	orient = 0;
	setBorderColor(Color.black);
	scaleFactor = (float)scale;
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new RProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoordsP();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
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
	    else {System.err.println("VImage:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoordsP[1];
		pc[0]=new RProjectedCoordsP();
	    }
	    else {System.err.println("VImage:Error while adding camera "+verifIndex);}
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

    /**get orientation*/
    public float getOrient(){return orient;}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**compute size (bounding circle radius)*/
    void computeSize(){
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    /**set absolute half width - aspect ratio is automatically maintained (height modified accordingly)*/
    public void setWidth(long w){
	vw=w;
	vh=Math.round((float)vw/ar);
	computeSize();
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set absolute half height (no effect: use setImage)*/
    public void setHeight(long h){
	vh=h;
	vw=Math.round(vh*ar);
	computeSize();
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**get half width*/
    public long getWidth(){return vw;}

    /**get half height*/
    public long getHeight(){return vh;}

    /**set absolute size by setting bounding circle radius */
    public void sizeTo(float radius){
	size=radius;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set image to be displayed*/
    public void setImage(Image i){
	image=i;
	vw=Math.round(image.getWidth(null)/2.0);
	vh=Math.round(image.getHeight(null)/2.0);
	ar=(float)vw/(float)vh;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**get the bitmap image to be displayed*/
    public Image getImage(){
	return image;
    }

    /**if false, image size is not sensitive to zoom (but its size can be changed)*/
    public void setZoomSensitive(boolean b){
	if (zoomSensitive!=b){
	    zoomSensitive=b;
	    if (vsm != null){
		vsm.repaintNow();
	    }
	}
    }

    /**if false, text size is not sensitive to zoom*/
    public boolean isZoomSensitive(){
	return zoomSensitive;
    }

    /**
     *@param p one of VImage.DRAW_BORDER_*
     */
    public void setDrawBorderPolicy(short p){
	if (drawBorder!=p){
	    drawBorder=p;
	    if (vsm != null){
		vsm.repaintNow();
	    }
	}
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	return false; //can contain transparent pixel (we have no way of knowing without analysing the image data -could be done when constructing the object or setting the image)
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if ((x>=(pc[camIndex].cx-pc[camIndex].cw)) && (x<=(pc[camIndex].cx+pc[camIndex].cw)) && (y>=(pc[camIndex].cy-pc[camIndex].ch)) && (y<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
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
	//project width and height
	if (zoomSensitive){
	    pc[i].cw = Math.round(vw*coef);
	    pc[i].ch = Math.round(vh*coef);
	}
	else{
	    pc[i].cw = (int)vw;
	    pc[i].ch = (int)vh;
	}
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project width and height
	if (zoomSensitive){
	    pc[i].lcw = Math.round(vw*coef);
	    pc[i].lch = Math.round(vh*coef);
	}
	else {
	    pc[i].lcw = (int)vw;
	    pc[i].lch = (int)vh;
	}
    }

    /**draw glyph
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)){
	    if (zoomSensitive){
		trueCoef = scaleFactor*coef;
	    }
	    else{
		trueCoef = scaleFactor;
	    }
	    //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
	    if (trueCoef!=1.0f){
		at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		g.setTransform(at);
		g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		g.setTransform(stdT);
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		}
	    }
	    else {
		g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
		if (drawBorder == 1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		}
		else if (drawBorder == 2){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw > 1) && (pc[i].lch > 1)){
	    if (zoomSensitive){trueCoef=scaleFactor*coef;}
	    else {trueCoef=scaleFactor;}
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (trueCoef!=1.0f){
		g.setTransform(AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch));
		g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		g.setTransform(stdT);
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		}
	    }
	    else {
		g.drawImage(image, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
		if (drawBorder == 1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		}
		else if (drawBorder == 2){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }


    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VImage res=new VImage(vx,vy,0,image);
	res.setWidth(vw);
	res.setHeight(vh);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setDrawBorderPolicy(drawBorder);
	res.setZoomSensitive(zoomSensitive);
	return res;
    }

}
