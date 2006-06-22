/*   FILE: SZRectangle.java
 *   DATE OF CREATION:  Mon May 29 08:34:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: SZRectangle.java,v 1.2 2006/05/29 07:28:00 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * Rectangle implementing semantic zooming (in the sense that it has minSize and maxSize attributes controlling its visibility) - cannot be reoriented
 * @author Emmanuel Pietriga
 **/

public class SZRectangle extends VRectangle {

    int minSize, maxSize;

    public SZRectangle(int mns, int mxs){
	super();
	minSize = mns;
	maxSize = mxs;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param mns minimum width and height in View below which the glyph is no longer displayed  (in pixels)
     *@param mxs maximum width and height in View above which the glyph is no longer displayed  (in pixels)
     */
    public SZRectangle(long x, long y, float z, long w, long h, Color c, int mns, int mxs){
	super(x, y, z, w, h, c);
	minSize = mns;
	maxSize = mxs;
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw >= minSize) && (pc[i].ch >= minSize)
	    && (pc[i].cw <= maxSize) && (pc[i].ch <= maxSize)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    if (((pc[i].cx-pc[i].cw)>0) || ((pc[i].cy-pc[i].ch)>0) || ((2*pc[i].cw-1)<vW) || ((2*pc[i].ch-1)<vH)){
			g.setStroke(stroke);  //change stroke there
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
			g.setStroke(stdS);  //original stroke restored here
		    }
		}
		else {
		    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		}
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw >= minSize) && (pc[i].lch >= minSize)
	    && (pc[i].lcw <= maxSize) && (pc[i].lch <= maxSize)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    if (((pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) || ((2*pc[i].lcw-1)<vW) || ((2*pc[i].lch-1)<vH)){
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
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	SZRectangle res = new SZRectangle(vx, vy, 0, vw, vh, color, minSize, maxSize);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
