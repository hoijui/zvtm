/*   FILE: VImageOr.java
 *   DATE OF CREATION:   Jan 09 2001
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
 */

package com.xerox.VTM.glyphs;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;

/**
 * Re-orientable Bitmap Image. This version is less efficient than VImage, but it can be reoriented. It cannot be made translucent (see VImage*ST).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VImage
 *@see net.claribole.zvtm.glyphs.VImageOrST
 *@see net.claribole.zvtm.glyphs.VImageST
 */

public class VImageOr extends VImage {

    /*vertex x coords*/
    int[] xcoords = new int[4];
    /*vertex y coords*/
    int[] ycoords = new int[4];

    /**
     *@param img image to be displayed
     *@param or orientation
     */
    public VImageOr(Image img,float or){
	super(img);
	orient=or;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param img image to be displayed
     *@param or orientation
     */
    public VImageOr(long x,long y, int z,Image img,float or){
	super(x,y,z,img);
	orient=or;
    }

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public void orientTo(float angle){
	orient=angle;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public boolean coordInside(int x,int y,int camIndex){
	if (pc[camIndex].p.contains(x,y)){return true;}
	else {return false;}
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project width and height
	if (zoomSensitive){pc[i].cw=Math.round(vw*coef);pc[i].ch=Math.round(vh*coef);}else{pc[i].cw=(int)vw;pc[i].ch=(int)vh;}
	float x1=-pc[i].cw;
	float y1=-pc[i].ch;
	float x2=pc[i].cw;
	float y2=pc[i].ch;
	xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
	xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = xcoords[j];
		pc[i].p.ypoints[j] = ycoords[j];
	    }
	    pc[i].p.invalidate();
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	if (zoomSensitive){
	    pc[i].lcw=Math.round(vw*coef);
	    pc[i].lch=Math.round(vh*coef);
	}
	else {
	    pc[i].lcw=(int)vw;
	    pc[i].lch=(int)vh;
	}
	float x1=-pc[i].lcw;
	float y1=-pc[i].lch;
	float x2=pc[i].lcw;
	float y2=pc[i].lch;
	xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
	xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = xcoords[j];
		pc[i].lp.ypoints[j] = ycoords[j];
	    }
	    pc[i].lp.invalidate();
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)){
	    if (zoomSensitive){
		trueCoef = scaleFactor*coef;
	    }
	    else{
		trueCoef = scaleFactor;
	    }
	    // a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
	    if (trueCoef!=1.0f){
		// translate
		at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		if (orient != 0){
		    // rotate
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
		}
		// rescale
		at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
		// draw
		g.drawImage(image,at,null);
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawPolygon(pc[i].p);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawPolygon(pc[i].p);
		}
	    }
	    else {
		if (orient==0){// no rotating, no rescaling, just draw after implicit translation
		    g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
		}
		else {// translate
		    at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		    // rotate
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
		    // draw
		    g.drawImage(image,at,null);
		}
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawPolygon(pc[i].p);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawPolygon(pc[i].p);
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)){
	    if (zoomSensitive){
		trueCoef=scaleFactor*coef;
	    }
	    else{
		trueCoef=scaleFactor;
	    }
	    //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} 
	    if (trueCoef!=1.0f){
		// translate
		at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
		if (orient!=0){
		    // rotate
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
		}
		// rescale
		at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
		// draw
		g.drawImage(image,at,null);
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawPolygon(pc[i].lp);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawPolygon(pc[i].lp);
		}
	    }
	    else {
		if (orient==0){// no rotating, no rescaling, just draw after implicit translation
		    g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,null);
		}
		else {// translate
		    at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
		    // rotate
		    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
		    // draw
		    g.drawImage(image,at,null);
		}
		if (drawBorder==1){
		    if (pc[i].prevMouseIn){
			g.setColor(borderColor);
			g.drawPolygon(pc[i].lp);
		    }
		}
		else if (drawBorder==2){
		    g.setColor(borderColor);
		    g.drawPolygon(pc[i].lp);
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(dx+dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	VImageOr res=new VImageOr(vx,vy,0,image,orient);
	res.setWidth(vw);
	res.setHeight(vh);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setDrawBorderPolicy(drawBorder);
	res.setZoomSensitive(zoomSensitive);
	return res;
    }

}
