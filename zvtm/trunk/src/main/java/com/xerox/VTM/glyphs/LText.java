/*   FILE: LText.java
 *   DATE OF CREATION:  Tue Mar 21 19:30:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import net.claribole.zvtm.glyphs.LensRendering;

import com.xerox.VTM.engine.VirtualSpaceManager;

/**
 * Standalone text whose visibility and color can be different depending on whether it is seen through a lens or not. Cannot be reoriented.<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VText
 *@see com.xerox.VTM.glyphs.VTextOr
 *@see com.xerox.VTM.glyphs.LBText
 *@see net.claribole.zvtm.glyphs.VTextST
 *@see net.claribole.zvtm.glyphs.VTextOrST
 */

public class LText extends VText implements LensRendering {

    Color fillColorThroughLens;
    boolean visibleThroughLens = visible;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param c fill color
     *@param t text string
     */
    public LText(long x,long y, int z,Color c,String t){
	super(x,y,z,c,t);
	fillColorThroughLens = color;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public LText(long x,long y, int z,Color c,String t,short ta){
	super(x,y,z,c,t,ta);
	fillColorThroughLens = color;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
     */
    public LText(long x, long y, int z, Color c, String t, short ta, float scale){
	super(x,y,z,c,t,ta);
	fillColorThroughLens = color;
	scaleFactor = scale;
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

    public Color getFillColorThroughLens(){
	return fillColorThroughLens;
    }

    public void setBorderColorThroughLens(Color c){}

    public Color getBorderColorThroughLens(){
	return fillColorThroughLens;
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.fillColorThroughLens);
	trueCoef = scaleFactor * coef;
	if (trueCoef*fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){
	    //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    // lcw and lch actually hold width and height of text *in virtual space*
		    pc[i].lcw = (int)Math.round(bounds.getWidth() * scaleFactor);
		    pc[i].lch = (int)Math.round(bounds.getHeight() * scaleFactor);
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
		g.setTransform(at);
		g.drawString(text,0.0f,0.0f);
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    // lcw and lch actually hold width and height of text *in virtual space*
		    pc[i].lcw = (int)Math.round(bounds.getWidth() * scaleFactor);
		    pc[i].lch = (int)Math.round(bounds.getHeight() * scaleFactor);
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
		g.setTransform(at);
		g.drawString(text,0.0f,0.0f);
	    }
	    g.setTransform(stdT);
	}
	else {
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	LText res=new LText(vx,vy,0,color,(new StringBuffer(text)).toString(),text_anchor);
	res.fillColorThroughLens = this.fillColorThroughLens;
	res.mouseInsideColor=this.mouseInsideColor;
	res.setVisibleThroughLens(visibleThroughLens);
	return res;
    }

}
