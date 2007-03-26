/*   FILE: VTextOr.java
 *   DATE OF CREATION:   Jan 11 2001
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
 * $Id: VTextOr.java,v 1.7 2005/12/08 09:08:21 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.VirtualSpaceManager;

/**
 * Re-orientable Standalone Text.  This version is less efficient than VText, but it can be reoriented.<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).<br>
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VText
 *@see com.xerox.VTM.glyphs.LText
 *@see com.xerox.VTM.glyphs.LBText
 *@see net.claribole.zvtm.glyphs.VTextST
 *@see net.claribole.zvtm.glyphs.VTextOrST
 */

public class VTextOr extends VText {

    /*half width and height in virtual space (of String when horizontal)*/
    float vw,vh;

    public VTextOr(String t,float or){
	super(t);
	orient=or;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param or orientation
     */
    public VTextOr(long x,long y,float z,Color c,String t,float or){
	super(x,y,z,c,t);
	orient=or;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param or orientation
     *@param ta text-anchor (for alignment: one of VText.TEXT_ANCHOR_*)
     */
    public VTextOr(long x,long y,float z,Color c,String t,float or,short ta){
	super(x, y, z, c, t, ta);
	orient = or;
    }

    public void orientTo(float angle){
	orient = angle;
	invalidate();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font != null){
		g.setFont(font);
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text, g);
		    pc[i].cw = (int)Math.abs(Math.round(bounds.getWidth()));
		    pc[i].ch = (int)Math.abs(Math.round(bounds.getHeight()));
		    pc[i].valid=true;
		}
		if (text_anchor == TEXT_ANCHOR_START){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].cw/2.0f, 0));
		}
		else {
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].cw, 0));
		}
		g.setTransform(at);
		g.drawString(text, 0.0f, 0.0f);
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text, g);
		    pc[i].cw = (int)Math.abs(Math.round(bounds.getWidth()));
		    pc[i].ch = (int)Math.abs(Math.round(bounds.getHeight()));
		    pc[i].valid=true;
		}
		if (text_anchor == TEXT_ANCHOR_START){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].cw/2.0f, 0));
		}
		else {
		    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].cw, 0));
		}
		g.setTransform(at);
		g.drawString(text, 0.0f, 0.0f);
	    }
	    g.setTransform(stdT);
	}
	else {g.fillRect(dx+pc[i].cx,pc[i].cy,1,1);}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font != null){
		g.setFont(font);
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text, g);
		    pc[i].lcw = (int)Math.abs(Math.round(bounds.getWidth()));
		    pc[i].lch = (int)Math.abs(Math.round(bounds.getHeight()));
		    pc[i].lvalid=true;
		}
		if (text_anchor == TEXT_ANCHOR_START){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, dy+pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].lcw/2.0f, 0));
		}
		else {
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, dy+pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].lcw, 0));
		}
		g.setTransform(at);
		g.drawString(text, 0.0f, 0.0f);
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text, g);
		    pc[i].lcw = (int)Math.abs(Math.round(bounds.getWidth()));
		    pc[i].lch = (int)Math.abs(Math.round(bounds.getHeight()));
		    pc[i].lvalid=true;
		}
		if (text_anchor == TEXT_ANCHOR_START){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE){
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, dy+pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].lcw/2.0f, 0));
		}
		else {
		    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, dy+pc[i].lcy);
		    if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef, coef));}
		    if (orient!=0){at.concatenate(AffineTransform.getRotateInstance(-orient));}
		    at.concatenate(AffineTransform.getTranslateInstance(-pc[i].lcw, 0));
		}
		g.setTransform(at);
		g.drawString(text, 0.0f, 0.0f);
	    }
	    g.setTransform(stdT);
	}
	else {g.fillRect(dx+pc[i].lcx, pc[i].lcy, 1, 1);}
    }

    public Object clone(){
	VTextOr res=new VTextOr(vx,vy,0,color,(new StringBuffer(text)).toString(),orient, text_anchor);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

}
