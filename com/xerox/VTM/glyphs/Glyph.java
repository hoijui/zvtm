/*   FILE: Glyph.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
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
 * $Id: Glyph.java,v 1.19 2006/04/05 06:16:14 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import net.claribole.zvtm.glyphs.CGlyph;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;

/** Glyph - parent class of all graphical objects.
 *@author Emmanuel Pietriga
 */

public abstract class Glyph implements Cloneable {

    /*------------Misc. Info-------------------------------------*/

    /**Glyph ID*/
    Long ID;

    /**ref to the object this glyph represents*/
    Object owner;

    /**type of object (can be any string)*/
    String type = "";

    /**get glyph ID*/
    public Long getID(){
	return ID;
    }

    /**set glyph ID (make sure there is no conflict)*/
    public void setID(Long ident){
	ID = ident;
    }

    /**get object this glyph represents*/
    public Object getOwner(){
	return owner;
    }
   
    /**associate an application object with this glyph*/
    public void setOwner(Object o){
	this.owner = o;
    }

    /**get glyph type*/
    public String getType(){
	return type;
    }
   
    /**
     *set glyph type
     *@param t any string
     */
    public void setType(String t){
	this.type = t;
    }

    /**
     * returns a String with ID, position, and altitude
     */
    public String toString(){
	return new String(super.toString()+" Glyph ID "+ID+" pos ("+vx+","+vy+","+vz+") "+type);
    }


    /*------------Geometry---------------------------------------*/

    /**coordinate in virtual space (geometric center of object)*/
    public long vx,vy;

    /**altitude (in virtual space)*/
    public float vz;

    /**radius of bounding circle*/
    float size;

    /**object orientation [0:2Pi[ */
    float orient=0.0f;

    /**relative translation (offset)*/
    public void move(long x,long y){
	vx+=x;
	vy+=y;
	propagateMove(x,y);  //take care of sticked glyphs
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**absolute translation*/
    public void moveTo(long x,long y){
	propagateMove(x-vx,y-vy);  //take care of sticked glyphs
	vx=x;
	vy=y;
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**returns coordinates of the glyph's geom center as a LongPoint*/
    public LongPoint getLocation(){return new LongPoint(vx,vy);}

    /**get size of object (radius of bounding circle)*/
    public abstract float getSize();    

    /**set size of object by setting its bounding circle's radius*/
    public abstract void sizeTo(float radius);

    /**multiply bounding circle radius by factor*/
    public abstract void reSize(float factor);

    /**get orientation*/
    public abstract float getOrient();

    /**set absolute orientation*/
    public abstract void orientTo(float angle);


    /*---Visibility and sensitivity------------------------------*/

    /**tells whether this glyph is visible or not (default is true)<br>does not affect sensitivity*/
    boolean visible=true;

    /**tells whether we should detect entry/exit in this glyph*/
    boolean sensit=true;

    /**set sensitivity of this glyph*/
    public void setSensitivity(boolean b){
	sensit=b;
    }

    /**tells whether mouse sends events related to entry/exit in this glyph or not*/
    public boolean isSensitive(){return sensit;}

    /**make this glyph (in)visible (the glyph remains sensitive to cursor in/out events)<br>
     * use methods VirtualSpace.show(Glyph g) and VirtualSpace.hide(Glyph g) to make a glyph both (in)visible and (in)sensitive
     *@param b true to make glyph visible, false to make it invisible
     */
    public void setVisible(boolean b){
	if (b!=visible){
	    visible=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
	}
    }

    /**get this glyph's visibility state (returns true if visible)*/
    public boolean isVisible(){
	return visible;
    }

    /**get this glyph's visibility state when seen through the lens (returns true if visible)<br>This is equivalent to isVisible() for most glyphs, except LText*/
    public boolean isVisibleThroughLens(){
	return visible;
    }


    /*------------Color------------------------------------------*/

    /**current fill color*/
    public Color color;
    /**current border color*/
    public Color borderColor;
    /**HSV coordinates of fill color in range 0.0-1.0*/
    protected float[] HSV=new float[3];
    /**HSV coordinates of border color in range 0.0-1.0*/
    protected float[] HSVb=new float[3];

    /** Main fill color. */
    public Color fColor = Color.white;
    /** Main border color. */
    public Color bColor = Color.black;
    /**color of border when cursor is inside glyph*/
    public Color mouseInsideColor;
    /**color of interior when cursor is inside glyph*/
    public Color mouseInsideFColor;

    boolean filled=true;

    /** Set whether the glyph should be filled with the fill color or not.
     *@param b false -&gt; do not paint interior of glyph
     */
    public void setFilled(boolean b){
	if (b!=filled){
	    filled=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){}
	}
    }

    /** Indicates whether this glyph is filled or not. */
    public boolean isFilled(){return filled;}

    /** Set whether the glyph's border should be painted with the border color or not.
     *@param b false -&gt; do not paint border of glyph
     */
    public void setDrawBorder(boolean b){
	if (b!=paintBorder){
	    paintBorder=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){}
	}
    }

    /** Indicates whether the glyph's border is painted with the border color or not. */
    public boolean isBorderDrawn(){return paintBorder;}

    /**set border color when cursor is inside glyph (null to keep the original color)
     */
    public void setMouseInsideBorderColor(Color c){
	this.mouseInsideColor = c;
    }

    /**set fill color when cursor is inside glyph (null to keep the original color)
     */
    public void setMouseInsideFillColor(Color c){
	this.mouseInsideFColor = c;
    }

    /** Set the glyph's main color. This is the fill color for closed shapes, or stroke color for other glyphs (text, paths, segments, etc.). */
    public void setColor(Color c){
	color = c;
	fColor = color;
	HSV = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),(new float[3]));
	if (vsm != null){vsm.repaintNow();}
    }

    /** Set the glyph's border color (use setColor for text, paths, segments, etc.).
     *@see #setColor(Color c)
     */
    public void setBorderColor(Color c){
	borderColor = c;
	bColor = borderColor;
	HSVb = Color.RGBtoHSB(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), (new float[3]));
	if (vsm != null){vsm.repaintNow();}
    }

    /** Set the glyph's main color (absolute value, HSV color space).
     *@param h hue in [0.0, 1.0]
     *@param s saturation in [0.0, 1.0]
     *@param v value (brightness) in [0.0, 1.0]
     *@see #addHSVColor(float h,float s,float v)
     */
    public void setHSVColor(float h,float s,float v){
	HSV[0]=h;
	if (HSV[0]>1) {HSV[0]=1.0f;} else {if (HSV[0]<0) {HSV[0]=0;}}
	HSV[1]=s;
	if (HSV[1]>1) {HSV[1]=1.0f;} else {if (HSV[1]<0) {HSV[1]=0;}}
	HSV[2]=v;
	if (HSV[2]>1) {HSV[2]=1.0f;} else {if (HSV[2]<0) {HSV[2]=0;}}
	color = Color.getHSBColor(HSV[0],HSV[1],HSV[2]);
	fColor = color;
	if (vsm != null){vsm.repaintNow();}
    }

    /** Set the glyph's main color (absolute value, HSV color space).
     *@param h hue so that the final hue is in [0.0, 1.0]
     *@param s saturation so that the final saturation is in [0.0, 1.0]
     *@param v value so that the final value (brightness) is in [0.0, 1.0]\
     *@see #setHSVColor(float h,float s,float v)
     */
    public void addHSVColor(float h,float s,float v){ //color  [-1.0,1.0]
	HSV[0]=HSV[0]+h;
	if (HSV[0]>1) {HSV[0]=1.0f;} else {if (HSV[0]<0) {HSV[0]=0;}}
	HSV[1]=HSV[1]+s;
	if (HSV[1]>1) {HSV[1]=1.0f;} else {if (HSV[1]<0) {HSV[1]=0;}}
	HSV[2]=HSV[2]+v;
	if (HSV[2]>1) {HSV[2]=1.0f;} else {if (HSV[2]<0) {HSV[2]=0;}}
	this.color=Color.getHSBColor(HSV[0],HSV[1],HSV[2]);
	fColor = color;
	if (vsm != null){vsm.repaintNow();}
    }

    /** Set the glyph's border color (absolute value, HSV color space).
     * Use setColor for text, paths, segments, etc.
     *@param h hue in [0.0, 1.0]
     *@param s saturation in [0.0, 1.0]
     *@param v value (brightness) in [0.0, 1.0]
     *@see #addHSVbColor(float h,float s,float v)
     */
    public void setHSVbColor(float h,float s,float v){//color  [0.0,1.0]
	HSVb[0]=h;
	if (HSVb[0]>1) {HSVb[0]=1.0f;} else {if (HSVb[0]<0) {HSVb[0]=0;}}
	HSVb[1]=s;
	if (HSVb[1]>1) {HSVb[1]=1.0f;} else {if (HSVb[1]<0) {HSVb[1]=0;}}
	HSVb[2]=v;
	if (HSVb[2]>1) {HSVb[2]=1.0f;} else {if (HSVb[2]<0) {HSVb[2]=0;}}
	borderColor=Color.getHSBColor(HSVb[0],HSVb[1],HSVb[2]);
	bColor = borderColor;
	if (vsm != null){vsm.repaintNow();}
    }
    
    /** Set the glyph's border color (absolute value, HSV color space).
     * Use setColor for text, paths, segments, etc.
     *@param h hue so that the final hue is in [0.0, 1.0]
     *@param s saturation so that the final saturation is in [0.0, 1.0]
     *@param v value so that the final value (brightness) is in [0.0, 1.0]
     *@see #setHSVbColor(float h,float s,float v)
     */
    public void addHSVbColor(float h,float s,float v){//color  [-1.0,1.0]
	HSVb[0]=HSVb[0]+h;
	if (HSVb[0]>1) {HSVb[0]=1.0f;} else {if (HSVb[0]<0) {HSVb[0]=0;}}
	HSVb[1]=HSVb[1]+s;
	if (HSVb[1]>1) {HSVb[1]=1.0f;} else {if (HSVb[1]<0) {HSVb[1]=0;}}
	HSVb[2]=HSVb[2]+v;
	if (HSVb[2]>1) {HSVb[2]=1.0f;} else {if (HSVb[2]<0) {HSVb[2]=0;}}
	this.borderColor = Color.getHSBColor(HSVb[0],HSVb[1],HSVb[2]);
	bColor = borderColor;
	if (vsm != null){vsm.repaintNow();}
    }

    /** Get main color's HSV components. */
    public float[] getHSVColor(){
	return this.HSV;
    }

    /** Get border color's HSV components. */
    public float[] getHSVbColor(){
	return this.HSVb;
    }

    /** Get the glyph's main color. This is the fill color for closed shapes, or stroke color for other glyphs (text, paths, segments, etc.). */
    public Color getColor(){
	return this.color;
    }

    /** Get the glyph's border color (use getColor for text, paths, segments, etc.).
     *@see #getColor()
     */
    public Color getBorderColor(){
	return this.borderColor;
    }

    /*------------Selection--------------------------------------*/

    /**tells whether this glyph is selected or not (default is false)*/
    boolean selected = false;

    /**select this glyph
     *@param b true to select glyph, false to unselect it
     */
    public void select(boolean b){
	selected=b;
    }

    /**get this glyph's selection state (returns true if selected)*/
    public boolean isSelected(){
	return selected;
    }


    /*------------Stroke-----------------------------------------*/

    public static final float DEFAULT_STROKE_WIDTH = 1.0f;
    BasicStroke stroke = null;  
    boolean dashedContour = false;
    float strokeWidth = DEFAULT_STROKE_WIDTH;
    boolean paintBorder = true;

    /** Convenience method for painting the glyph's border with an predefined dashed stroke.
     *@param b true -&gt; draw a discontinuous contour for this glyph
     */
    public void setDashed(boolean b){
	dashedContour=b;
	strokeWidth=(stroke!=null) ? stroke.getLineWidth() : DEFAULT_STROKE_WIDTH;
	int cap=(stroke!=null) ? stroke.getEndCap() : BasicStroke.CAP_BUTT;
	int join=(stroke!=null) ? stroke.getLineJoin() : BasicStroke.JOIN_MITER;
	float miterlimit=(stroke!=null) ? stroke.getMiterLimit() : 4.0f;
	if (dashedContour){
	    float[] dasharray={10.0f};
	    float dashphase=(stroke!=null) ? stroke.getDashPhase() : 0.0f;
	    stroke=new BasicStroke(strokeWidth,cap,join,miterlimit,dasharray,dashphase);
	}
	else {
	    stroke=new BasicStroke(strokeWidth,cap,join,miterlimit);
	}
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /** Set the width of the stroke used to paint ther glyph's border.
     * Does not change the stroke dash settings.
     *@param w stroke width
     */
    public void setStrokeWidth(float w){
	strokeWidth=w;
	int cap=(stroke!=null) ? stroke.getEndCap() : BasicStroke.CAP_BUTT;
	int join=(stroke!=null) ? stroke.getLineJoin() : BasicStroke.JOIN_MITER;
	float miterlimit=(stroke!=null) ? stroke.getMiterLimit() : 4.0f;
	if (dashedContour){
	    float[] dasharray={10.0f};
	    float dashphase=(stroke!=null) ? stroke.getDashPhase() : 0.0f;
	    stroke=new BasicStroke(strokeWidth,cap,join,miterlimit,dasharray,dashphase);
	}
	else {
	    stroke=new BasicStroke(strokeWidth,cap,join,miterlimit);
	}
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /** Set a custom stroke to paint glyph's border.
     *@param b basic stroke (null to set standard 1px-thick stroke)
     */
    public void setStroke(BasicStroke b){
	if (b!=null){stroke=b;strokeWidth=stroke.getLineWidth();}
	else {stroke=null;strokeWidth=1.0f;}
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /** Get the stroke used to paint glyph's border.
     *@return null if default 1px-thick solid stroke
     */
    public BasicStroke getStroke(){
	return stroke;
    }

    /** Get the width of the stroke used to paint ther glyph's border.
     *@return the stroke (default is 1.0)
     */
    public float getStrokeWidth(){
	if (stroke!=null){return stroke.getLineWidth();}
	else return strokeWidth;
    }


    /*---------Composite glyphs----------------------------------*/

    /** Composite glyph associated with this glyph.
     * Means that this glyph is either a primary or secondary glyph inside a CGlyph.
     */
    CGlyph cGlyph=null;

    /** Set the composite glyph associated with this glyph.
     * Means that this glyph is either a primary or secondary glyph inside a CGlyph. Do not call this method manually ; called automatically when adding the glyph in a CGlyph. */
    public void setCGlyph(CGlyph c){cGlyph=c;}


    /** Get the composite glyph associated with this glyph.
     *@return null if this glyph is not part of a composite glyph.
     */
    public CGlyph getCGlyph(){return cGlyph;}


    /*---------Sticked glyphs----------------------------------*/

    /**glyphs sticked to this one*/
    Glyph[] stickedGlyphs;

    /**object to which this glyph is sticked to (could be a VCursor, a Camera or a Glyph)*/
    public Object stickedTo;

    /**propagate this glyph's movement to all glyphs constrained by this one (pos)*/
    public void propagateMove(long x,long y){
	if (stickedGlyphs != null){
	    for (int i=0;i<stickedGlyphs.length;i++){
		stickedGlyphs[i].move(x,y);
	    }
	}
    }

    /**
     *attach glyph to this one
     *@param g glyph to be attached to this one
     */
    public void stick(Glyph g){
	if (stickedGlyphs == null){
	    stickedGlyphs = new Glyph[1];
	    stickedGlyphs[0] = g;
	    g.stickedTo = this;
	}
	else {
	    boolean alreadySticked = false;
	    for (int i=0;i<stickedGlyphs.length;i++){
		if (stickedGlyphs[i] == g){
		    alreadySticked = true;
		    break;
		}
	    }
	    if (!alreadySticked){
		Glyph[] newStickList = new Glyph[stickedGlyphs.length + 1];
		System.arraycopy(stickedGlyphs, 0, newStickList, 0, stickedGlyphs.length);
		newStickList[stickedGlyphs.length] = g;
		stickedGlyphs = newStickList;
		g.stickedTo = this;
	    }
	    else {
		if (this.vsm.debugModeON()){System.err.println("Warning: trying to stick Glyph "+g+" to Glyph "+this+" while they are already sticked.");}
	    }
	}
    }

    /**
     *detach glyph from this one
     *@param g glyph to be detached
     */
    public void unstick(Glyph g){
	if (stickedGlyphs != null){
	    for (int i=0;i<stickedGlyphs.length;i++){
		if (stickedGlyphs[i] == g){
		    g.stickedTo = null;
		    Glyph[] newStickList = new Glyph[stickedGlyphs.length - 1];
		    System.arraycopy(stickedGlyphs, 0, newStickList, 0, i);
		    System.arraycopy(stickedGlyphs, i+1, newStickList, i, stickedGlyphs.length-i-1);
		    stickedGlyphs = newStickList;
		    break;
		}
	    }
	    if (stickedGlyphs.length == 0){stickedGlyphs = null;}
	    g.stickedTo = null;
	}
    }

   /**
    *detach all glyphs attached to this one
    */
    public void unstickAllGlyphs(){
	if (stickedGlyphs != null){
	    for (int i=0;i<stickedGlyphs.length;i++){
		stickedGlyphs[i].stickedTo = null;
		stickedGlyphs[i] = null;
	    }
	    stickedGlyphs = null;
	}
    }

    /**return the list of glyphs sticked to this one (null if none)*/
    public Glyph[] getStickedGlyphArray(){
	return stickedGlyphs;
    }

    /*----Projecting and Drawing--------------------------------*/

    /**projection coef*/
    public float coef=1.0f;

    /**project shape in camera coord sys prior to actual painting*/
    public abstract void project(Camera c,Dimension d);

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public abstract void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy);

    /**draw this glyph
     *@param g graphic context in which the glyph should be drawn 
     *@param vW associated view width (used to determine if border should be drawn)
     *@param vH associated view height (used to determine if border should be drawn)
     * right now only VRectangle and VRectangleOr(/Or=0) use this
     *@param i camera index in the virtual space
     */
    public abstract void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy);

    /**draw this glyph through the lens
     *@param g graphic context in which the glyph should be drawn 
     *@param vW associated view width (used to determine if border should be drawn)
     *@param vH associated view height (used to determine if border should be drawn)
     * right now only VRectangle and VRectangleOr(/Or=0) use this
     *@param i camera index in the virtual space
     */
    public abstract void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy);

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public abstract void initCams(int nbCam);

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public abstract void addCamera(int verifIndex);

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public abstract void removeCamera(int index);

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public abstract boolean coordInside(int x,int y,int camIndex);

    /**reset prevMouseIn for projected coordinates nb i*/
    public abstract void resetMouseIn();

    /**reset prevMouseIn for projected coordinates nb i*/
    public abstract void resetMouseIn(int i);
    
    /**used to find out if it is necessary to project and draw the glyph in the current view or through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    /**used to find out if it is necessary to project and draw the glyph in the current view*/
    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region. The glyph is obviously visible */
	    return true;
	}
	else {
	    if (((vx-size)<=eb) && ((vx+size)>=wb) && ((vy-size)<=nb) && ((vy+size)>=sb)){
		/* Glyph is at least partially in region.
		   We approximate using the glyph bounding circle, meaning that some glyphs not
		   actually visible can be projected and drawn (but they won't be displayed)) */
		return true;  
	    }
	}
	return false;
    }

    /** Find out if it is necessary to project and draw the glyph through the lens in the current view.
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     *@return true if the glyph intersects the region delimited by wb, nb, eb, sb
     */
    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region.
	       There is a good chance the glyph is
	       contained in the region, but this is not sufficient. */
	    if (((vx+size)<=eb) && ((vx-size)>=wb) && ((vy+size)<=nb) && ((vy-size)>=sb)){
		return true;
	    }
	    else return false;   //otherwise the glyph is not visible
	}
	return false;
    }

    /** Find out if glyph completely fills the view. (In which case it
       is not necessary to repaint objects below it in the drawing stack). */
    public abstract boolean fillsView(long w,long h,int camIndex);

    /** Method used internally for picking.
     *@return 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)
     */
    public abstract int mouseInOut(int x,int y,int camIndex);


    /*-------Internal use only----------------------------------*/

    //XXX: it would probably be better to have a hook to the owning
    //     virtual space as this information could be more useful
    //     and we can get the VSM from it

    /** Reference to the owvning VSM. */
    public VirtualSpaceManager vsm;

    /** Set a reference to the virtual space manager (called internally). */
    public void setVSM(VirtualSpaceManager v){this.vsm=v;}


    /*-------------Cloning--------------------------------------*/

    public abstract Object clone();

}
