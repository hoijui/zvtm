/*   FILE: LSegment.java
 *   DATE OF CREATION:  Wed Dec  7 15:32:35 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Segment specifically made for being seen through a lens without disappearing too much.
 * Doubles the line when rendering the segment in the lens buffer.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.LSegmentST
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.glyphs.VSegmentST
 **/

public class LSegmentST extends LSegment implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    public LSegmentST(){
	super();
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to 0.5
    }

    /**
     *give the centre of segment and half its projected length on X & Y axis
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space (can be negative)
     *@param h half height in virtual space (can be negative)
     *@param c color
     */
    public LSegmentST(long x, long y, int z, long w, long h, Color c){
	super(x, y, z, w, h, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to 0.5
    }

    /**
     *give the end points of segment
     *@param x1 coordinate of endpoint 1 in virtual space
     *@param y1 coordinate of endpoint 1 in virtual space
     *@param x2 coordinate of endpoint 2 in virtual space
     *@param y2 coordinate of endpoint 2 in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c color
     */
    public LSegmentST(long x1, long y1, int z, Color c, long x2, long y2){
	super(x1, y1, z, c, x2, y2);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to 0.5
    }

    /**
     *give the centre of segment and half its length & orient
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param lgth half length in virtual space
     *@param angle orientation
     *@param c color
     */
    public LSegmentST(long x,long y, int z,float lgth,float angle,Color c){
	super(x, y, z, lgth, angle, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to 0.5
    }

    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public float getTranslucencyValue(){return alpha;}

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
	g.setColor(this.color);
	if (alpha < 1.0f){
	    g.setComposite(acST);
	    g.drawLine(pc[i].cx-pc[i].cw,pc[i].cy-pc[i].ch,pc[i].cx+pc[i].cw,pc[i].cy+pc[i].ch);
	    g.setComposite(acO);
	}
	else {
	    g.drawLine(pc[i].cx-pc[i].cw,pc[i].cy-pc[i].ch,pc[i].cx+pc[i].cw,pc[i].cy+pc[i].ch);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (alpha < 1.0f){
	    g.setComposite(acST);
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,
		       dy+pc[i].lcy-pc[i].lch,
		       dx+pc[i].lcx+pc[i].lcw,
		       dy+pc[i].lcy+pc[i].lch);
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw+1,
		       dy+pc[i].lcy-pc[i].lch+1,
		       dx+pc[i].lcx+pc[i].lcw+1,
		       dy+pc[i].lcy+pc[i].lch+1);
	    g.setComposite(acO);
	}
	else {
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw,
		       dy+pc[i].lcy-pc[i].lch,
		       dx+pc[i].lcx+pc[i].lcw,
		       dy+pc[i].lcy+pc[i].lch);
	    g.drawLine(dx+pc[i].lcx-pc[i].lcw+1,
		       dy+pc[i].lcy-pc[i].lch+1,
		       dx+pc[i].lcx+pc[i].lcw+1,
		       dy+pc[i].lcy+pc[i].lch+1);
	}
    }

    public Object clone(){
	LSegmentST res = new LSegmentST(vx, vy, 0, vw, vh, color);
	res.mouseInsideColor = this.mouseInsideColor;
	return res;
    }

}
