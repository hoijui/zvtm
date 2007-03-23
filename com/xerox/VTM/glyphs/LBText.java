/*   FILE: LBText.java
 *   DATE OF CREATION:  Tue Mar 21 19:30:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LBText.java,v 1.1 2006/05/29 07:56:55 epietrig Exp $
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
import net.claribole.zvtm.glyphs.LensRendering;

/**
 * Standalone text whose visibility and color can be different depending on whether it is seen through a lens or not.
 * The border color is used to fill the text's background. Set to null if no background should be painted.<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VText
 *@see com.xerox.VTM.glyphs.LText
 *@see com.xerox.VTM.glyphs.VTextOr
 *@see net.claribole.zvtm.glyphs.VTextST
 */

public class LBText extends LText {

    Color bColor;
    Color borderColor;
    /* Coordinates of border (background) color in HSV color space. */
    protected float[] HSVb=new float[3];

    Color fillColorThroughLens;
    Color borderColorThroughLens;
    boolean visibleThroughLens = visible;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     */
    public LBText(long x,long y,float z,Color c,String t){
	super(x,y,z,c,t);
	fillColorThroughLens = color;
	borderColor = null;
	borderColorThroughLens = null;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public LBText(long x,long y,float z,Color c,String t,short ta){
	super(x,y,z,c,t,ta);
	fillColorThroughLens = color;
	borderColor = null;
	borderColorThroughLens = null;
    }

    public void setVisibleThroughLens(boolean b){
	visibleThroughLens = b;
    }

    public boolean isVisibleThroughLens(){
	return visibleThroughLens;
    }

    public void setFillColorThroughLens(Color c){
	fillColorThroughLens = c;
    }

    public void setBorderColorThroughLens(Color c){
	borderColorThroughLens = c;
    }

    public Color getFillColorThroughLens(){
	return fillColorThroughLens;
    }
    
    public Color getBorderColorThroughLens(){
	return borderColorThroughLens;
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

    /** Set the glyph's border color (absolute value, HSV color space).
     * Use setColor for text, paths, segments, etc.
     *@param h hue in [0.0, 1.0]
     *@param s saturation in [0.0, 1.0]
     *@param v value (brightness) in [0.0, 1.0]
     *@see #addHSVbColor(float h,float s,float v)
     */
    public void setHSVbColor(float h, float s, float v){
	HSVb[0] = h;
	if (HSVb[0]>1) {HSVb[0] = 1.0f;} else {if (HSVb[0]<0) {HSVb[0] = 0;}}
	HSVb[1] = s;
	if (HSVb[1]>1) {HSVb[1] = 1.0f;} else {if (HSVb[1]<0) {HSVb[1] = 0;}}
	HSVb[2] = v;
	if (HSVb[2]>1) {HSVb[2] = 1.0f;} else {if (HSVb[2]<0) {HSVb[2] = 0;}}
	borderColor = Color.getHSBColor(HSVb[0],HSVb[1],HSVb[2]);
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
    public void addHSVbColor(float h, float s, float v){
	HSVb[0] = HSVb[0]+h;
	if (HSVb[0]>1) {HSVb[0] = 1.0f;} else {if (HSVb[0]<0) {HSVb[0] = 0;}}
	HSVb[1] = HSVb[1]+s;
	if (HSVb[1]>1) {HSVb[1] = 1.0f;} else {if (HSVb[1]<0) {HSVb[1] = 0;}}
	HSVb[2] = HSVb[2]+v;
	if (HSVb[2]>1) {HSVb[2] = 1.0f;} else {if (HSVb[2]<0) {HSVb[2] = 0;}}
	this.borderColor = Color.getHSBColor(HSVb[0], HSVb[1], HSVb[2]);
	bColor = borderColor;
	if (vsm != null){vsm.repaintNow();}
    }

    /** Get border color's HSV components. */
    public float[] getHSVbColor(){
	return this.HSVb;
    }

    /** Get the glyph's border color (use getColor for text, paths, segments, etc.).
     *@see #getColor()
     */
    public Color getBorderColor(){
	return this.borderColor;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].cw = (int)bounds.getWidth();
		    pc[i].ch = (int)bounds.getHeight();
		    pc[i].valid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (borderColor != null){
		    g.setColor(borderColor);
		    g.fillRect(dx-2, dy-pc[i].lch+1, pc[i].lcw+4, pc[i].lch+1);
		}
		g.setColor(this.color);
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
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (borderColor != null){
		    g.setColor(borderColor);
		    g.fillRect(dx-2, dy-pc[i].ch+1, pc[i].cw+4, pc[i].ch+1);
		}
		g.setColor(this.color);
		try {g.drawString(text,0.0f,0.0f);}
		catch(NullPointerException ex){/*text could be null*/}
	    }
	    g.setTransform(stdT);
	}
	else {
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].lcw = (int)bounds.getWidth();
		    pc[i].lch = (int)bounds.getHeight();
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (borderColor != null){
		    g.setColor(borderColor);
		    g.fillRect(dx-2, dy-pc[i].lch + 1, pc[i].lcw+4, pc[i].lch+1);
		}
		g.setColor(this.fillColorThroughLens);
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
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (borderColor != null){
		    g.setColor(borderColor);
		    g.fillRect(dx-2, dy-pc[i].lch + 1, pc[i].lcw+4, pc[i].lch+1);
		}
		g.setColor(this.fillColorThroughLens);
		try {g.drawString(text,0.0f,0.0f);}
		catch(NullPointerException ex){/*text could be null*/}
	    }
	    g.setTransform(stdT);
	}
	else {
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	LBText res=new LBText(vx,vy,0,color,(new StringBuffer(text)).toString(),text_anchor);
	res.borderColor=this.borderColor;
	res.fillColorThroughLens = this.fillColorThroughLens;
	res.borderColorThroughLens = this.borderColorThroughLens;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setVisibleThroughLens(visibleThroughLens);
	return res;
    }

}
