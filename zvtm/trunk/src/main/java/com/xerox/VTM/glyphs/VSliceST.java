/*   FILE: VSliceST.java
 *   DATE OF CREATION:  Wed Aug 31 16:05:40 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
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

import com.xerox.VTM.engine.LongPoint;

/**
 * Translucent Slice. This version is less efficient than VSlice, but it can be made translucent.<br>
 * Slices are useful e.g. to draw pie menus.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VSlice
 */

public class VSliceST extends VSlice implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    /** Construct a slice by giving its 3 vertices
        *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc   
        *@param z z-index
        *@param c fill color
        *@param bc border color
        *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSliceST(LongPoint[] v, int z, Color c, Color bc, float a){
        super(v, z, c, bc);
        alpha = a;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
        *@param z z-index
        *@param vs arc radius in virtual space (in rad)
        *@param ag arc angle in virtual space (in rad)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSliceST(long x, long y, int z, long vs, double ag, double or, Color c, Color bc, float a){
        super(x, y, z, vs, ag, or, c, bc);
        alpha = a;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
        *@param z z-index
        *@param vs arc radius in virtual space (in degrees)
        *@param ag arc angle in virtual space (in degrees)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSliceST(long x, long y, int z, long vs, int ag, int or, Color c, Color bc, float a){
        super(x, y, z, vs, ag, or, c, bc);
        alpha = a;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    public void setTranslucencyValue(float a){
        alpha = a;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //translucency set to alpha
        try{vsm.repaintNow();}
        catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    public float getTranslucencyValue(){return alpha;}

    public boolean fillsView(long w,long h,int camIndex){
	//XXX: TBW (call coordInside() for the four view corners)
	return false;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
 	if (pc[i].innerCircleRadius > 2){//paint a dot if too small
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.fillArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke != null){
			g.setStroke(stroke);
			g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
				  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
			g.setStroke(stdS);
		    }
		    else {
			g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
				  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.fillArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke != null){
			g.setStroke(stroke);
			g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
				  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
			g.setStroke(stdS);
		    }
		    else {
			g.drawArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
				  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
			g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
		    }
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
 	if (pc[i].linnerCircleRadius > 2){//paint a dot if too small
	    if (alpha < 1.0f){
		g.setComposite(acST);
		if (filled){
		    g.setColor(this.color);
		    g.fillArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke != null){
			g.setStroke(stroke);
			g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
				  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
			g.setStroke(stdS);
		    }
		    else {
			g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
				  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		    }
		}
		g.setComposite(acO);
	    }
	    else {
		if (filled){
		    g.setColor(this.color);
		    g.fillArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		}
		if (paintBorder){
		    g.setColor(borderColor);
		    if (stroke != null){
			g.setStroke(stroke);
			g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
				  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
			g.setStroke(stdS);
		    }
		    else {
			g.drawArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
				  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
				  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
			g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		    }
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    }
	}
    }

    /** Not implement yet. */
    public Object clone(){
	//XXX: TBW
	return null;
    }

}
