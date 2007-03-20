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

    /** Glyph ID. */
    Long ID;

    /** Object this glyph represents in the client application.
     * The owner can be any arbitrary Java object set by the client application, that the programmer wants to be easily accessible through the Glyph (typically the owner will be an object that models a logical concept of which a visual depiction is given by the glyph). Multiple glyphs can have the same owner. A glyph can only have one owner.
     * Set by client application. Null if not set.
     */
    Object owner;

    /** Type of object.
     * Arbitrary String, set by client application. Null if not set.
     */
    String type;

    /** Get this glyph's ID. */
    public Long getID(){
	return ID;
    }

    /** Set this glyph's ID.
     * Set internally by ZVTM. If tampering, make sure there is no conflict.
     */
    public void setID(Long ident){
	ID = ident;
    }

    /** Get the object this glyph represents in the client application.
     * The owner can be any arbitrary Java object set by the client application, that the programmer wants to be easily accessible through the Glyph (typically the owner will be an object that models a logical concept of which a visual depiction is given by the glyph). Multiple glyphs can have the same owner. A glyph can only have one owner.
     *@return null if not associated with anything.
     */
    public Object getOwner(){
	return owner;
    }
   
     /** Set the object this glyph represents in the client application.
     * The owner can be any arbitrary Java object set by the client application, that the programmer wants to be easily accessible through the Glyph (typically the owner will be an object that models a logical concept of which a visual depiction is given by the glyph). Multiple glyphs can have the same owner. A glyph can only have one owner.
     *@param o provided by client application, null by default.
     */
    public void setOwner(Object o){
	this.owner = o;
    }

    /** Get the type of this glyph.
     * Arbitrary String, set by client application. Null if not set. This is somewhat equivalent to tagging an object, but it only one type can be associated with a glyph.
     *@return null if not set
     */
    public String getType(){
	return type;
    }
   
    /** Set the type of this glyph.
     * This is somewhat equivalent to tagging an object, but it only one type can be associated with a glyph.
     *@param t arbitrary string, set by client application. Null if not set. 
     */
    public void setType(String t){
	this.type = t;
    }

    /** Get a string representation of this glyph.
     */
    public String toString(){
	return new String(super.toString()+" Glyph ID "+ID+" pos ("+vx+","+vy+","+vz+") type="+type);
    }


    /*------------Geometry---------------------------------------*/

    /** Coordinates in virtual space (geometric center of object). */
    public long vx,vy;

    /** Altitude (in virtual space). Not used yet. */
    public float vz;

    /** Radius of bounding circle. */
    float size;

    /** Glyph's orientation in [0:2Pi[. */
    float orient=0.0f;

    /** Translate the glyph by (x,y) - relative translation.
     *@see #moveTo(long x, long y)
     */
    public void move(long x, long y){
	vx+=x;
	vy+=y;
	propagateMove(x,y);  //take care of sticked glyphs
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /** Translate the glyph to (x,y) - absolute translation.
     *@see #move(long x, long y)
     */
    public void moveTo(long x, long y){
	propagateMove(x-vx,y-vy);  //take care of sticked glyphs
	vx=x;
	vy=y;
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /** Get the coordinates of the glyph's geometrical center. */
    public LongPoint getLocation(){return new LongPoint(vx,vy);}

    /** Get glyph's size (radius of bounding circle). */
    public abstract float getSize();    

    /** Set glyph's size by setting its bounding circle's radius.
     *@see #reSize(float factor)
     */
    public abstract void sizeTo(float radius);

    /** Set glyph's size by multiplying its bounding circle radius by a factor. 
     *@see #sizeTo(float radius)
     */
    public abstract void reSize(float factor);

    /** Get the glyph's orientation. */
    public abstract float getOrient();

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public abstract void orientTo(float angle);


    /*---Visibility and sensitivity------------------------------*/

    /** Indicates whether this glyph is visible or not (default is true)<br>Does not affect its sensitivity.*/
    boolean visible=true;

    /** Indicates whether we should detect entry/exit in this glyph (i.e., is it sensitive). */
    boolean sensit=true;

    /** Make this glyph sensitive (or not). */
    public void setSensitivity(boolean b){
	sensit=b;
    }

    /** Indicates whether ZVTM sends events related to cursor entry/exit in/from this glyph or not. */
    public boolean isSensitive(){return sensit;}

    /** Make this glyph (in)visible (the glyph remains sensitive to cursor in/out events).<br>
     * Use methods VirtualSpace.show(Glyph g) and VirtualSpace.hide(Glyph g) to make a glyph both (in)visible and (in)sensitive.
     *@param b true to make glyph visible, false to make it invisible
     *@see com.xerox.VTM.engine.VirtualSpace#show(Glyph g)
     *@see com.xerox.VTM.engine.VirtualSpace#hide(Glyph g)
     */
    public void setVisible(boolean b){
	if (b!=visible){
	    visible=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
	}
    }

    /** Get this glyph's visibility status.
     *@return true if visible
     */
    public boolean isVisible(){
	return visible;
    }

    /** Get this glyph's visibility status when seen through a lens.
     * This is equivalent to isVisible() for most glyphs, except LText.
     *@return true if visible
     */
    public boolean isVisibleThroughLens(){
	return visible;
    }


    /*------------Color------------------------------------------*/

    /** Current main color (read only, use access methods for all modification purposes).
     * Fill color for closed shapes, stroke color for glyphs which use just one color, such as text, paths, segments.
     */
    public Color color;
    
    /** Current border color (read only, use access methods for all modification purposes).
     * Border color for closed shapes.
     */
    public Color borderColor;

    /** Coordinates of main color in HSV color space. */
    protected float[] HSV=new float[3];

    /** Coordinates of border color in HSV color space. */
    protected float[] HSVb=new float[3];

    /** Fill color of this glyph when it is in its default state. */
    public Color fColor = Color.white;

    /** Border color of this glyph when it is in its default state. */
    public Color bColor = Color.black;

    /** Border color of this glyph when cursor is inside it. Null if same as default border color. */
    public Color mouseInsideColor;

    /** Fill color of this glyph when cursor is inside it. Null if same as default fill color. */
    public Color mouseInsideFColor;

    /** Indicates whether this glyph's interior is filled or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    boolean filled=true;

    /** Indicates whether this glyph's border is drawn or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    boolean paintBorder = true;

    /** Set whether this glyph's interior should be filled or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    public void setFilled(boolean b){
	if (b!=filled){
	    filled=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){}
	}
    }

    /** Indicates whether this glyph's interior is filled or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    public boolean isFilled(){return filled;}

    /** Set whether the glyph's border should be drawn or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    public void setDrawBorder(boolean b){
	if (b!=paintBorder){
	    paintBorder=b;
	    try{vsm.repaintNow();}catch(NullPointerException e){}
	}
    }

    /** Indicates whether the glyph's border is drawn or not.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     */
    public boolean isBorderDrawn(){return paintBorder;}

    /** Set the glyph's border color when cursor is inside it.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     *@param c set to null to keep the original color.
     */
    public void setMouseInsideBorderColor(Color c){
	this.mouseInsideColor = c;
    }

    /** Set the glyph's fill color when cursor is inside it.
     * Relevant for closed shapes only. Does not make sense for glyphs such as text, paths and segments.
     *@param c set to null to keep the original color.
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

    /** Indicates whether this glyph is selected or not (default is false)*/
    boolean selected = false;

    /** Select this glyph. This just flags the glyph as selected.
     *@param b true to select glyph, false to unselect it
     */
    public void select(boolean b){
	selected=b;
    }

    /** Get this glyph's selection status.
     *@return true if selected. 
     */
    public boolean isSelected(){
	return selected;
    }


    /*------------Stroke-----------------------------------------*/

    public static final float DEFAULT_STROKE_WIDTH = 1.0f;
    BasicStroke stroke = null;  
    boolean dashedContour = false;
    float strokeWidth = DEFAULT_STROKE_WIDTH;

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

    /** Glyphs sticked to this one. */
    Glyph[] stickedGlyphs;

    /** Object to which this glyph is sticked (could be a VCursor, a Camera or a Glyph). */
    public Object stickedTo;

    /** Propagate this glyph's movement to all glyphs constrained by this one.
     * Called automatically by ZVTM when translating this glyph. 
     */
    public void propagateMove(long x,long y){
	if (stickedGlyphs != null){
	    for (int i=0;i<stickedGlyphs.length;i++){
		stickedGlyphs[i].move(x,y);
	    }
	}
    }

    /** Attach a glyph to this one. Translations of this glyph will be propagated to g.
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

    /** Unattach a glyph from this one. Translations of this glyph will no longer be propagated to g.
     *@param g glyph to be unattached from this one
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

   /** Unattach all glyphs attached to this one. Translations of this glyph will no longer be propagated to them.
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

    /** Get the list of glyphs sticked to this one.
     *@return null if no glyph is attached to this one
     */
    public Glyph[] getStickedGlyphArray(){
	return stickedGlyphs;
    }

    /*----Projecting and Drawing--------------------------------*/

    /** Value sent to cursor when it enters this glyph. For internal use. */
    public static final short ENTERED_GLYPH = 1;
    /** Value sent to cursor when it exits this glyph. For internal use. */
    public static final short EXITED_GLYPH = -1;
    /** Value sent to cursor when it neither enters nor exit this glyph. For internal use. */
    public static final short NO_CURSOR_EVENT = 0;

    /** Projection coefficient. Computed internally. Do not tamper with. */
    public float coef=1.0f;

    /** Project glyph w.r.t a given camera's coordinate system, prior to actual painting. Called internally.
     *@param c camera
     *@param d dimension of View using camera c
     */
    public abstract void project(Camera c,Dimension d);

    /** Project glyph w.r.t a given camera's coordinate system, prior to actual painting through a lens. Called internally.
     *@param c camera
     *@param lensWidth width of lens activated in View using this camera
     *@param lensHeight height of lens activated in View using this camera
     *@param lensMag magnification factor of lens activated in View using this camera
     *@param lensx horizontal coordinateof lens activated in View using this camera
     *@param lensy vertical coordinate of lens activated in View using this camera
     */
    public abstract void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy);

    /** Draw this glyph.
     *@param g graphics context in which the glyph should be drawn 
     *@param vW associated View width (used by some closed shapes to determine if it is worth painting the glyph's border)
     *@param vH associated View height (used by some closed shapes to determine if it is worth painting the glyph's border)
     *@param i camera index in the virtual space containing the glyph
     *@param stdS default stroke
     *@param stdT identity transform
     *@param dx horizontal offset
     *@param dy vertical offset
     */
    public abstract void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy);

    /** Draw this glyph through a lens.
     *@param g graphics context in which the glyph should be drawn 
     *@param vW associated View width (used by some closed shapes to determine if it is worth painting the glyph's border)
     *@param vH associated View height (used by some closed shapes to determine if it is worth painting the glyph's border)
     *@param i camera index in the virtual space containing the glyph
     *@param stdS default stroke
     *@param stdT identity transform
     *@param dx horizontal offset
     *@param dy vertical offset
     */
    public abstract void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy);

    /** Initialize projected coordinates.
	Called internally when glyph is created in order to create the initial set of projected coordinates w.r.t the number of cameras in the virtual space.
     *@param nbCam current number of cameras in the virtual space
     *@see #addCamera(int verifIndex)
     *@see #removeCamera(int index)
     */
    public abstract void initCams(int nbCam);

    /** Create new projected coordinates.
     * Called internally to create new projected coordinates to use with the new camera.
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras.
     *@see #initCams(int nbCam)
     *@see #removeCamera(int index)
     */
    public abstract void addCamera(int verifIndex);

    /** Dispose of projected coordinates.
     * If a camera is removed from the virtual space to which this glyphs belongs, the corresponding projected coordinates should be deleted. the array of projected coordinates should not be modified however, because other cameras' index remain the same. The corresponding index in the array should just be set to null.
     *@see #initCams(int nbCam)
     *@see #addCamera(int verifIndex)
     */
    public abstract void removeCamera(int index);

    /** Detect whether the given point is inside this glyph or not. 
     *@param x provide projected JPanel coordinates of the associated view, not virtual space coordinates
     *@param y provide projected JPanel coordinates of the associated view, not virtual space coordinates
     */
    public abstract boolean coordInside(int x,int y,int camIndex);

    /** Reset memory of cursor being inside the glyph. */
    public abstract void resetMouseIn();

    /** Reset memory of cursor being inside the glyph for projected coordinates associated with camera at index i. */
    public abstract void resetMouseIn(int i);
    
    /** Method used internally for firing picking-related events.
     *@return VCurcor.ENTERED_GLYPH if cursor has entered the glyph, VCurcor.EXITED_GLYPH if it has exited the glyph, VCursor.NO_CURSOR_EVENT if nothing has changed (meaning the cursor was already inside or outside it)
     */
    public abstract short mouseInOut(int x,int y,int camIndex);

    /** Method used internally to find out if it is necessary to project and draw this glyph for a given camera.
     *@return true if the glyph is currently visible in the region delimited by wb, nb, eb, sb, symbolising the region seen through a camera
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
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

    /** Method used internally to find out if it is necessary to project and draw the glyph through a lens for a given camera.
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

    /** Find out if this glyph completely fills a view. (In which case it is not necessary to repaint objects below it in the drawing stack).
     * If implemented, this method should be very efficient, as it is used by an optional top-down clipping algorithm.
     * Otherwise it might cost more time than it can potentially save.
     * Until now it has only been implemented for non-reorientable rectangles and was activated only for 
     * treemap-like representations in which a lot of rectangles can potentially overlap each other.
     */
    public abstract boolean fillsView(long w,long h,int camIndex);


    /*-------Internal use only----------------------------------*/

    //XXX: it would probably be better to have a hook to the owning
    //     virtual space as this information could be more useful
    //     and we can get the VSM from it

    /** Reference to owning VSM. */
    public VirtualSpaceManager vsm;

    /** Set a reference to the virtual space manager. Called internally. */
    public void setVSM(VirtualSpaceManager v){this.vsm=v;}

    /*-------------Cloning--------------------------------------*/

    public abstract Object clone();

}
