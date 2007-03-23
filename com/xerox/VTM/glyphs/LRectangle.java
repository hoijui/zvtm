/*   FILE: LRectangle.java
 *   DATE OF CREATION:  Mon May 29 08:34:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LRectangle.java,v 1.1 2006/05/29 07:28:40 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import net.claribole.zvtm.lens.Lens;
import net.claribole.zvtm.glyphs.LensRendering;

/**
 * Rectangle whose visibility and color can be different depending on whether it is seen through a lens or not.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VRectangle
 **/

public class LRectangle extends VRectangle implements LensRendering {

    Color fillColorThroughLens;
    Color borderColorThroughLens;
    boolean visibleThroughLens = visible;

    public LRectangle(){
	super();
	fillColorThroughLens = color;
	borderColorThroughLens = borderColor;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public LRectangle(long x, long y, float z, long w, long h, Color c){
	super(x, y, z, w, h, c);
	fillColorThroughLens = color;
	borderColorThroughLens = borderColor;
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

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.fillColorThroughLens);
		g.fillRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw, 2*pc[i].lch);
	    }
	    if (paintBorder){
		g.setColor(this.borderColorThroughLens);
		if (stroke!=null) {
		    if (((pc[i].lcx-pc[i].lcw)>0) || ((pc[i].lcy-pc[i].lch)>0) || ((2*pc[i].lcw-1)<vW) || ((2*pc[i].lch-1)<vH)){
			g.setStroke(stroke);  //change stroke there
			g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
			g.setStroke(stdS);  //original stroke restored here
		    }
		}
		else {
		    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
		}
	    }
	}
	else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.fillColorThroughLens);
	    if (pc[i].lcw<=1){
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
	    }
	    else if (pc[i].lch<=1){
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
	    }
	}
	else {
	    g.setColor(this.fillColorThroughLens);
	    g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
	}
    }

    public Object clone(){
	LRectangle res = new LRectangle(vx, vy, 0, vw, vh, color);
	res.borderColor=this.borderColor;
	res.fillColorThroughLens = this.fillColorThroughLens;
	res.borderColorThroughLens = this.borderColorThroughLens;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
