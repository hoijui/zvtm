/*   FILE: VSliceST.java
 *   DATE OF CREATION:  Wed Aug 31 16:05:40 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VSliceST.java,v 1.3 2006/03/17 17:45:23 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.LongPoint;

/**
 * VSlice - transparency
 * @author Emmanuel Pietriga
 **/

public class VSliceST extends VSlice implements Transparent,Cloneable {

    /**semi transparency (default is 0.5)*/
    AlphaComposite acST;
    /**alpha channel*/
    float alpha=0.5f;

    /** Construct a slice by giving its 3 vertices
     *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc
     *@param c fill color
     */
    public VSliceST(LongPoint[] v, Color c){
	super(v, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /** Construct a slice by giving its size, angle and orientation
     *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
     *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
     *@param vs arc radius in virtual space (in rad)
     *@param ag arc angle in virtual space (in rad)
     *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
     *@param c fill color
     */
    public VSliceST(long x, long y, long vs, double ag, double or, Color c){
	super(x, y, vs, ag, or, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /** Construct a slice by giving its size, angle and orientation
     *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
     *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
     *@param vs arc radius in virtual space (in degrees)
     *@param ag arc angle in virtual space (in degrees)
     *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
     *@param c fill color
     */
    public VSliceST(long x, long y, long vs, int ag, int or, Color c){
	super(x, y, vs, ag, or, c);
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //transparency set to 0.5
    }

    /**
     *set alpha channel value (transparency)
     *@param a [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public void setTransparencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);  //transparency set to alpha
	try{vsm.repaintNow();}
	catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**get alpha value (transparency) for this glyph*/
    public float getTransparencyValue(){return alpha;}

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude) - always return false for now*/
    public boolean fillsView(long w,long h,int camIndex){
	//XXX: TBW (call coordInside() for the four view corners)
	return false;
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
 	if (pc[i].innerCircleRadius > 2){//paint a dot if too small
	    if (filled){
		g.setColor(this.color);
		g.setComposite(acST);
		g.fillArc(pc[i].cx - pc[i].innerCircleRadius, pc[i].cy - pc[i].innerCircleRadius,
			  2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		g.setComposite(acO);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke != null){
		    g.setStroke(stroke);
		    g.drawArc(pc[i].cx - pc[i].innerCircleRadius, pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(pc[i].cx, pc[i].cy, pc[i].p1x, pc[i].p1y);
		    g.drawLine(pc[i].cx, pc[i].cy, pc[i].p2x, pc[i].p2y);
		    g.setStroke(stdS);
		}
		else {
		    g.drawArc(pc[i].cx - pc[i].innerCircleRadius, pc[i].cy - pc[i].innerCircleRadius,
			      2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(pc[i].cx, pc[i].cy, pc[i].p1x, pc[i].p1y);
		    g.drawLine(pc[i].cx, pc[i].cy, pc[i].p2x, pc[i].p2y);
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(pc[i].cx,pc[i].cy,1,1);
	    g.setComposite(acO);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT){
 	if (pc[i].linnerCircleRadius > 2){//paint a dot if too small
	    if (filled){
		g.setColor(this.color);
		g.setComposite(acST);
		g.fillArc(pc[i].lcx - pc[i].linnerCircleRadius, pc[i].lcy - pc[i].linnerCircleRadius,
			  2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			  (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		g.setComposite(acO);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke != null){
		    g.setStroke(stroke);
		    g.drawArc(pc[i].lcx - pc[i].linnerCircleRadius, pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(pc[i].lcx, pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
		    g.drawLine(pc[i].lcx, pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		    g.setStroke(stdS);
		}
		else {
		    g.drawArc(pc[i].lcx - pc[i].linnerCircleRadius, pc[i].lcy - pc[i].linnerCircleRadius,
			      2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
			      (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
		    g.drawLine(pc[i].lcx, pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
		    g.drawLine(pc[i].lcx, pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
		}
	    }
 	}
	else {
	    g.setColor(this.color);
	    g.setComposite(acST);
	    g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
	    g.setComposite(acO);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	//XXX: TBW
	return null;
    }

}
