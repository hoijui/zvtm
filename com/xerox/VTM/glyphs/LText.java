/*   FILE: LText.java
 *   DATE OF CREATION:  Tue Mar 21 19:30:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LText.java,v 1.3 2006/05/29 07:28:40 epietrig Exp $
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
 * Standalone Text whose visibility and color can be different depending on whether it is seen through a lens or not
 * @author Emmanuel Pietriga
 */

public class LText extends VText implements LensRendering, Cloneable {

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
    public LText(long x,long y,float z,Color c,String t){
	super(x,y,z,c,t);
	fillColorThroughLens = color;
	borderColorThroughLens = borderColor;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public LText(long x,long y,float z,Color c,String t,short ta){
	super(x,y,z,c,t,ta);
	fillColorThroughLens = color;
	borderColorThroughLens = borderColor;
    }

    /**make this glyph (in)visible when seen through a lens (the glyph remains sensitive to cursor in/out events)<br>
     *@param b true to make glyph visible, false to make it invisible
     */
    public void setVisibleThroughLens(boolean b){
	visibleThroughLens = b;
    }

    /**get this glyph's visibility state when seen through the lens (returns true if visible)*/
    public boolean isVisibleThroughLens(){
	return visibleThroughLens;
    }

    /**set the color used to paint the glyph's interior*/
    public void setFillColorThroughLens(Color c){
	fillColorThroughLens = c;
    }

    /**set the color used to paint the glyph's border*/
    public void setBorderColorThroughLens(Color c){
	borderColorThroughLens = c;
    }

    /**get the color used to paint the glyph's interior*/
    public Color getFillColorThroughLens(){
	return fillColorThroughLens;
    }
    
    /**get the color used to paint the glyph's border*/
    public Color getBorderColorThroughLens(){
	return borderColorThroughLens;
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	g.setColor(this.fillColorThroughLens);
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

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	LText res=new LText(vx,vy,0,color,(new StringBuffer(text)).toString(),text_anchor);
	res.borderColor=this.borderColor;
	res.fillColorThroughLens = this.fillColorThroughLens;
	res.borderColorThroughLens = this.borderColorThroughLens;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setVisibleThroughLens(visibleThroughLens);
	return res;
    }

}
