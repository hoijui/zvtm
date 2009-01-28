/*   FILE: ZRectangle.java
 *   DATE OF CREATION:   Aug 22 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZRectangle.java 1032 2007-12-10 10:15:08Z epietrig $
 */

package net.claribole.eval.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.glyphs.VRectangle;

/**
 * Do not use.
 * @author Emmanuel Pietriga
 **/

public class ZRectangle extends VRectangle {

    public ZRectangle(){
	vx = 0;
	vy = 0;
	vz = 0;
	vw = 10;
	vh = 10;
	ar = (float)vw/(float)vh;
	orient = 0;
	setColor(Color.white);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public ZRectangle(long x,long y, int z,long w,long h,Color c){
	vx = x;
	vy = y;
	vz = z;
	vw = w;
	vh = h;
	if (vw == 0 && vh==0){ar = 1.0f;}
	else {ar = (float)vw/(float)vh;}
	orient = 0;
	setColor(c);
	setBorderColor(Color.black);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
	    if (isFilled()) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
	    }
	    if (isBorderDrawn()){
		g.setColor(borderColor);
		g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    if (isFilled()) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
	    }
	    if (isBorderDrawn()){
		g.setColor(borderColor);
		g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
	}
    }

    public Object clone(){
	ZRectangle res = new ZRectangle(vx,vy,0,vw,vh,color);
	res.borderColor = this.borderColor;
	res.mouseInsideColor = this.mouseInsideColor;
	res.bColor = this.bColor;
	return res;
    }

}
