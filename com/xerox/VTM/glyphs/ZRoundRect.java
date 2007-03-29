/*   FILE: ZRoundRect.java
 *   DATE OF CREATION:   Aug 22 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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
 * Do not use.
 * @author Emmanuel Pietriga
 **/

public class ZRoundRect extends VRoundRect  {

    public static final int ROUND_CORNER_THRESHOLD = 8;

    boolean renderRound = false;

    public ZRoundRect(){
	super();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param aw arc width in virtual space
     *@param ah arc height in virtual space
     */
    public ZRoundRect(long x,long y,float z,long w,long h,Color c,int aw,int ah, boolean rr){
	super(x, y, z, w, h, c, aw, ah);
	this.renderRound = rr;
    }

    public void renderRound(boolean b){
	renderRound = b;
	vsm.repaintNow();
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (renderRound){
	    if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
		if (filled) {
		    g.setColor(this.color);
		    if (pc[i].aw > ROUND_CORNER_THRESHOLD || pc[i].ah > ROUND_CORNER_THRESHOLD){
			g.fillRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch,pc[i].aw,pc[i].ah);
		    }
		    else {
			g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
		    }
		}
		if (paintBorder && (pc[i].aw > ROUND_CORNER_THRESHOLD || pc[i].ah > ROUND_CORNER_THRESHOLD)){
		    g.setColor(borderColor);
		    g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
		}
	    }
	    else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
		g.setColor(this.color);
		if (pc[i].cw<=1){
		    g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
		}
		else if (pc[i].ch<=1){
		    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
		}
	    }
	    else {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    }
	}
	else {
	    if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
		if (filled) {
		    g.setColor(this.color);
		    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
		}
		if (paintBorder && (pc[i].aw > ROUND_CORNER_THRESHOLD || pc[i].ah > ROUND_CORNER_THRESHOLD)){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		}
	    }
	    else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
		g.setColor(this.color);
		if (pc[i].cw<=1){
		    g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
		}
		else if (pc[i].ch<=1){
		    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
		}
	    }
	    else {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (renderRound){
	    if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
		if (filled) {
		    g.setColor(this.color);
		    g.fillRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch,pc[i].law,pc[i].lah);		
		}
		if (paintBorder && (pc[i].law > ROUND_CORNER_THRESHOLD || pc[i].lah > ROUND_CORNER_THRESHOLD)){
		    g.setColor(borderColor);
		    g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
		}
	    }
	    else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
		g.setColor(this.color);
		if (pc[i].lcw<=1){
		    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
		}
		else if (pc[i].lch<=1){
		    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
		}
	    }
	    else {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    }
	}
	else {
	    if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
		if (filled) {
		    g.setColor(this.color);
		    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
		}
		if (paintBorder && (pc[i].law > ROUND_CORNER_THRESHOLD || pc[i].lah > ROUND_CORNER_THRESHOLD)){
		    g.setColor(borderColor);
		    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
		}
	    }
	    else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
		g.setColor(this.color);
		if (pc[i].lcw<=1){
		    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
		}
		else if (pc[i].lch<=1){
		    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
		}
	    }
	    else {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
	    }
	}
    }

    /** Indicates whether round corners are visible or not (as seen through camera c). */
    public boolean cornersVisible(Camera c){
	return (pc[c.getIndex()].aw > ROUND_CORNER_THRESHOLD || pc[c.getIndex()].ah > ROUND_CORNER_THRESHOLD);
    }

    /** Indicates whether round corners are visible or not (as seen through a lens with camera c). */
    public boolean cornersVisibleInLens(Camera c){
	return (pc[c.getIndex()].law > ROUND_CORNER_THRESHOLD || pc[c.getIndex()].lah > ROUND_CORNER_THRESHOLD);	
    }

    public Object clone(){
	ZRoundRect res=new ZRoundRect(vx,vy,0,vw,vh,color,arcWidth,arcHeight,renderRound);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
