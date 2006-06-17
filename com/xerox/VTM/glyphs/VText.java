/*   FILE: VText.java
 *   DATE OF CREATION:   Nov 23 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
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
 * $Id: VText.java,v 1.11 2005/12/07 15:29:34 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;
import net.claribole.zvtm.lens.Lens;

/**
 * Standalone Text  (font properties are set in the view, but can be changed for each VText using setSpecialFont())
 * vx and vy are coordinates of lower-left corner of String because it would be too time-consuming to compute the String's center (needs to be computed at each repaint: it requires access to Graphics2D) (besides it makes the VTM unstable)
 * @author Emmanuel Pietriga
 */

public class VText extends Glyph implements Cloneable {

    /**text alignment (for text anchor) used to align a VText relative to its (vx,vy coordinates coincides with start of String)*/
    public static final short TEXT_ANCHOR_START=0;
    /**text alignment (for text anchor) used to align a VText relative to its (vx,vy coordinates coincides with middle of String)*/
    public static final short TEXT_ANCHOR_MIDDLE=1;
    /**text alignment (for text anchor) used to align a VText relative to its (vx,vy coordinates coincides with end of String)*/
    public static final short TEXT_ANCHOR_END=2;

    short text_anchor = TEXT_ANCHOR_START;

    AffineTransform at;

    ProjText[] pc;

    boolean zoomSensitive=true;

    /**font size in pixels*/
    public static float fontSize=VirtualSpaceManager.getMainFont().getSize2D();
    /**special font used in this object only (null if default font)*/
    Font font;
    /**do not rely on this - use for optimization and not guaranteed to be consistent*/
    Rectangle2D bounds;
    /**text that should be drawn with glyph*/
    String text;

    public VText(String t){
	vx=0;
	vy=0;
	vz=0;
	sensit=false;
	text=t;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     */
    public VText(long x,long y,float z,Color c,String t){
	vx=x;
	vy=y;
	vz=z;
	sensit=false;
	text=t;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public VText(long x,long y,float z,Color c,String t,short ta){
	vx=x;
	vy=y;
	vz=z;
	sensit=false;
	text=t;
	setColor(c);
	setBorderColor(Color.black);
	text_anchor=ta;
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjText[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjText();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjText[] ta=pc;
		pc=new ProjText[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjText();
	    }
	    else {System.err.println("VText:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjText[1];
		pc[0]=new ProjText();
	    }
	    else {System.err.println("VText:Error while adding camera "+verifIndex);}
	}
    }

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public void removeCamera(int index){
	pc[index]=null;
    }

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
    }

    /**set size (absolute) - has no effect*/
    public void sizeTo(float factor){}

    /**set size (relative) - has no effect*/
    public void reSize(float factor){}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (always =1)*/
    public float getSize(){return 1.0f;}

    /**get orientation*/
    public float getOrient(){return orient;}

    /**if false, text size is not sensitive to zoom*/
    public void setZoomSensitive(boolean b){
	if (zoomSensitive!=b){
	    zoomSensitive=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
	}
    }

    /**if false, text size is not sensitive to zoom*/
    public boolean isZoomSensitive(){
	return zoomSensitive;
    }

    /**used to find out if it is necessary to project and draw the glyph in the current view or through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){ //if glyph hotspot is in the region, it is obviously visible
	    return true;
	}
	else {
	    if (text_anchor==TEXT_ANCHOR_START){
		if ((vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
		else return false;   //otherwise the glyph is not visible
	    }
	    else if (text_anchor==TEXT_ANCHOR_MIDDLE){
		if ((vx-pc[i].cw/2<=eb) && ((vx+pc[i].cw/2)>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
		else return false;   //otherwise the glyph is not visible
	    }
	    else {//TEXT_ANCHOR_END
		if ((vx-pc[i].cw<=eb) && (vx>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
		else return false;   //otherwise the glyph is not visible
	    }
	}
    }

    /**used to find out if it is necessary to project and draw the glyph through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region.
	       There is a good chance the glyph is contained in the region, but this is not sufficient. */
	    if (text_anchor==TEXT_ANCHOR_START){
		if ((vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	    else if (text_anchor==TEXT_ANCHOR_MIDDLE){
		if ((vx+pc[i].cw/2<=eb) && ((vx-pc[i].cw/2)>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	    else {//TEXT_ANCHOR_END
		if ((vx+pc[i].cw<=eb) && (vx>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	}
	return false;
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    /**returns 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)*/
    public int mouseInOut(int x,int y,int camIndex){
	return 0;
    }

    /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].cw = (int)bounds.getWidth();
		    pc[i].ch = (int)bounds.getHeight();
		    pc[i].valid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(pc[i].cx,pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(pc[i].cx-pc[i].cw*coef/2.0f,pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(pc[i].cx-pc[i].cw*coef,pc[i].cy);}
		at.preConcatenate(stdT);
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		try {g.drawString(text,0.0f,0.0f);}
		catch (NullPointerException ex){/*text could be null*/}
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].cw = (int)bounds.getWidth();
		    pc[i].ch = (int)bounds.getHeight();
		    pc[i].valid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(pc[i].cx,pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(pc[i].cx-pc[i].cw*coef/2.0f,pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(pc[i].cx-pc[i].cw*coef,pc[i].cy);}
		at.preConcatenate(stdT);
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		try {g.drawString(text,0.0f,0.0f);}
		catch(NullPointerException ex){/*text could be null*/}
	    }
	    g.setTransform(stdT);
	}
	else {
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].lcw = (int)bounds.getWidth();
		    pc[i].lch = (int)bounds.getHeight();
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(pc[i].lcx,pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(pc[i].lcx-pc[i].lcw*coef/2.0f,pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(pc[i].lcx-pc[i].lcw*coef,pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		try {g.drawString(text,0.0f,0.0f);}
		catch (NullPointerException ex){/*text could be null*/}
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].lcw = (int)bounds.getWidth();
		    pc[i].lch = (int)bounds.getHeight();
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(pc[i].lcx,pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(pc[i].lcx-pc[i].lcw*coef/2.0f,pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(pc[i].lcx-pc[i].lcw*coef,pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		try {g.drawString(text,0.0f,0.0f);}
		catch(NullPointerException ex){/*text could be null*/}
	    }
	    g.setTransform(stdT);
	}
	else {
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	}
    }

    /**set text that should be painted with this glyph - override Glyph method to call invalidate*/
    public void setText(String t){
	text=t;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
	invalidate();
    }

    /**force computing of text's bounding box at next paint call*/
    public void invalidate(){
	try {
	    for (int i=0;i<pc.length;i++){
		pc[i].valid=false;
		pc[i].lvalid=false;
	    }
	}
	catch (NullPointerException ex){}
    }

    /**returns width and height of the bounding box as a Point (so this is not a real Point)
     *@param i index of camera (Camera.getIndex())
     */
    public LongPoint getBounds(int i){
	return new LongPoint(pc[i].cw,pc[i].ch);
    }

    /**tells whether the bounds of the text are valid at this time or not (can be invalid if the thread in charge of painting has not dealt with this glyph since invalidate() was called on it) - might be useful to test this before calling getBounds()*/
    public boolean validBounds(int i){
	return pc[i].valid;
    }

    /**change font for this specific text object - f=null to get back to default font*/
    public void setSpecialFont(Font f){
	if (f!=null){font=f;fontSize=font.getSize2D();}else{font=null;fontSize=VirtualSpaceManager.getMainFont().getSize2D();}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
	invalidate();
    }

    /**returns the font used for this glyph's text*/
    public Font getFont(){
	if (font!=null){return font;}
	else return VirtualSpaceManager.getMainFont();
    }

    /**tells whether this glyph is using a special font or not (note: using a special font does not necessarily means that this font is different from the default font (although it should, but this is at programer's prerogative))*/
    public boolean usesSpecialFont(){
	if (font==null){return false;}
	else {return true;}
    }

    /**get text associated with this glyph*/
    public String getText(){return text;}

    /** Set the text anchor
     *@param ta one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END
     */
    public void setTextAnchor(short ta){
	text_anchor=ta;
    }

    /**
     *get text anchor (one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END)
     */
    public short getTextAnchor(){
	return text_anchor;
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	VText res=new VText(vx,vy,0,color,(new StringBuffer(text)).toString(),text_anchor);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
