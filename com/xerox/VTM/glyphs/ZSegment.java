/*   FILE: ZSegment.java
 *   DATE OF CREATION:   Jan 19 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZSegment.java,v 1.3 2006/01/20 10:32:10 epietrig Exp $
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
 * Alternative to VSegment for very large widths and heights in virtual space (that go beyond 32-bit integers). Can only handle horizontal or vertical segments. In most cases VSegment will be the best solution. This version can be useful e.g. when a virtual space contains a very large grid.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.ZSegmentST
 *@see com.xerox.VTM.glyphs.VSegmentST
 *@see com.xerox.VTM.glyphs.VSegment
 */

public class ZSegment extends VRectangle {

    public ZSegment(){
	vx = 0;
	vy = 0;
	vz = 0;
	vw = 10;
	vh = 10;
	computeSize();
	ar = (float)vw/(float)vh;
	orient = 0;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public ZSegment(long x,long y,float z,long w,long h,Color c){
	vx = x;
	vy = y;
	vz = z;
	vw = w;
	vh = h;
	computeSize();
	if (vw == 0 && vh==0){ar = 1.0f;}
	else {ar = (float)vw/(float)vh;}
	orient = 0;
	setColor(c);
	setBorderColor(Color.black);
    }

    public boolean fillsView(long w,long h,int camIndex){//width and height of view - pc[i].c? are JPanel coords
	return false;
    }

    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region. The glyph is obviously visible */
	    return true;
	}
	else if ((vx-vw <= eb) && (vx+vw >= wb) && (vy-vh <= nb) && (vy+vh >= sb)){
		/* Glyph is at least partially in region.
		   We approximate using the glyph bounding circle, meaning that some glyphs not
		   actually visible can be projected and drawn (but they won't be displayed)) */
	    return true;
	}
	return false;
    }

    public void project(Camera c, Dimension d){
	int i = c.getIndex();
	coef = (float)(c.focal/(c.focal+c.altitude));
	/* more complex than usual projection that takes into account potential overflow problems when
	   casting 64-bit long as a 32-bit int (required by Graphics2D)*/
	/* THE SEGMENT IS CONSIDERED AND DRAWN AS A RECTANGLE WITH EITHER WIDTH OR HEIGHT EQUAL TO 1 */
	/* cx, cy, cw and ch do not contain the usual values:
	   - cx and cy hold the rectangle's top left corner ON SCREEN
	   - cw and ch hold the rectangle's total width and height ON SCREEN
	   This means that if the rectangle's top left corner was supposed to be outside
	   the canvas, it is brought back into the canvas so as to keep the same appearance.
	   The same thing applies to the bottom right corner.*/
	/* keep in mind that the case where the rectangle is not visible at all
	   never occurs as only (at least partially) visible glyphs go through
           the projection process*/
	if (d.width/2 + (vx-c.posx-vw)*coef < 0){
	    // if top left corner's X is outside the canvas, bring it back at 0
	    pc[i].cx = 0;
	    // compute the apparent width accordingly (subtracting the negative X coordinate)
	    pc[i].cw = Math.round(d.width/2 + (vw+vx-c.posx)*coef);
	    // if width is negative there was probably a 32-bit overflow
	    // or if the width is just hudge, bring it back to the east edge
	    // (does not serve any purpose to draw beyond,
	    // and Graphics2D instructions do not like it
	    if (pc[i].cw < 0 || pc[i].cw > d.width-pc[i].cx){
		pc[i].cw = d.width-pc[i].cx;
	    }
	}
	else {
	    // top left corner's X is inside the canvas
	    pc[i].cx = Math.round(d.width/2 + (vx-c.posx-vw)*coef);
	    pc[i].cw = Math.round(2*vw*coef);
	    // if width is negative there was probably a 32-bit overflow
	    // or if the width is just hudge, bring it back to the east edge
	    // (does not serve any purpose to draw beyond,
	    // and Graphics2D instructions do not like it
	    if (pc[i].cw < 0 || pc[i].cw > d.width-pc[i].cx){
		pc[i].cw = d.width-pc[i].cx;
	    }
	}
	/* same comments apply to the vertical projection*/
	if (d.height/2 - (vy-c.posy+vh)*coef < 0){
	    pc[i].cy = 0;
	    pc[i].ch = Math.round(d.height/2 - (vy-c.posy-vh)*coef);
	    if (pc[i].ch < 0 || pc[i].ch > d.height-pc[i].cy){
		pc[i].ch = d.height-pc[i].cy;
	    }
	}
	else {
	    pc[i].cy = Math.round(d.height/2 - (vy-c.posy+vh)*coef);
	    pc[i].ch = Math.round(2*vh*coef);
	    if (pc[i].ch < 0 || pc[i].ch > d.height-pc[i].cy){
		pc[i].ch = d.height-pc[i].cy;
	    }
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal/(c.focal+c.altitude)) * lensMag;
	/* see comments made for project() */
	if (lensWidth/2 + (vx-lensx-vw)*coef < 0){
	    pc[i].lcx = 0;
	    pc[i].lcw = Math.round(lensWidth/2 + (vw+vx-lensx)*coef);
	    if (pc[i].lcw < 0 || pc[i].lcw > lensWidth-pc[i].lcx){
		pc[i].lcw = lensWidth-pc[i].lcx;
	    }
	}
	else {
	    pc[i].lcx = Math.round(lensWidth/2 + (vx-lensx-vw)*coef);
	    pc[i].lcw = Math.round(2*vw*coef);
	    if (pc[i].lcw < 0 || pc[i].lcw > lensWidth-pc[i].lcx){
		pc[i].lcw = lensWidth-pc[i].lcx;
	    }
	}
	if (lensHeight/2 - (vy-lensy+vh)*coef < 0){
	    pc[i].lcy = 0;
	    pc[i].lch = Math.round(lensHeight/2 - (vy-lensy-vh)*coef);
	    if (pc[i].lch < 0 || pc[i].lch > lensHeight-pc[i].lcy){
		pc[i].lch = lensHeight-pc[i].lcy;
	    }
	}
	else {
	    pc[i].lcy = Math.round(lensHeight/2 - (vy-lensy+vh)*coef);
	    pc[i].lch = Math.round(2*vh*coef);
	    if (pc[i].lch < 0 || pc[i].lch > lensHeight-pc[i].lcy){
		pc[i].lch = lensHeight-pc[i].lcy;
	    }
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
	    g.setColor(this.color);
	    g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, pc[i].ch);
	}
	else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].cw<=1){
		g.drawRect(dx+pc[i].cx, dy+pc[i].cy, 0, pc[i].ch);
	    }
	    else if (pc[i].ch<=1){
		g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, 0);
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    g.setColor(this.color);
	    g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, pc[i].lch);
	}
	else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].lcw<=1){
		g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, 0, pc[i].lch);
	    }
	    else if (pc[i].lch<=1){
		g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, 0);
	    }
	}
    }

    public Object clone(){
	ZSegment res = new ZSegment(vx,vy,0,vw,vh,color);
	res.borderColor = this.borderColor;
	res.mouseInsideColor = this.mouseInsideColor;
	res.bColor = this.bColor;
	return res;
    }

}
