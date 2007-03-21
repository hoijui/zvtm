/*   FILE: ZSegmentST.java
 *   DATE OF CREATION:   Jan 19 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZSegmentST.java,v 1.1 2006/01/19 15:09:15 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.AlphaComposite;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * Rectangle - behaves correctly for a very wide range of altitudes - must be either horizontal or vertical
 * can be made translucent
 * @author Emmanuel Pietriga
 **/

public class ZSegmentST extends ZSegment implements Translucent {

    /**semi translucency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    public ZSegmentST(){
	super();
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //translucency set to 0.5
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public ZSegmentST(long x,long y,float z,long w,long h,Color c){
	super(x, y, z, w, h, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //translucency set to 0.5
    }


    /**
     * Set alpha channel value (translucency).
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //translucency set to alpha
    }

    /** Get alpha channel value (translucency).
     *@return a value in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public float getTranslucencyValue(){return alpha;}

    /**draw glyph 
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, pc[i].ch);
		g.setComposite(acO);
	    }
	    else {
		g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, pc[i].ch);
	    }
	}
	else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].cw<=1){
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawRect(dx+pc[i].cx, dy+pc[i].cy, 0, pc[i].ch);
		    g.setComposite(acO);
		}
		else {
		    g.drawRect(dx+pc[i].cx, dy+pc[i].cy, 0, pc[i].ch);
		}
	    }
	    else if (pc[i].ch<=1){
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, 0);
		    g.setComposite(acO);
		}
		else {
		    g.drawRect(dx+pc[i].cx, dy+pc[i].cy, pc[i].cw, 0);
		}
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, pc[i].lch);
		g.setComposite(acO);
	    }
	    else {
		g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, pc[i].lch);
	    }
	}
	else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].lcw<=1){
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, 0, pc[i].lch);
		    g.setComposite(acO);
		}
		else {
		    g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, 0, pc[i].lch);
		}
	    }
	    else if (pc[i].lch<=1){
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, 0);
		    g.setComposite(acO);
		}
		else {
		    g.drawRect(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lcw, 0);
		}
	    }
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	ZSegmentST res = new ZSegmentST(vx,vy,0,vw,vh,color);
	res.borderColor = this.borderColor;
	res.mouseInsideColor = this.mouseInsideColor;
	res.bColor = this.bColor;
	res.setTranslucencyValue(alpha);
	return res;
    }

}
