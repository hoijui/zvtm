/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 29 14:30:34 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VPath;

/**
 * Translucent General path: made of an arbitrary number of segments, quadratic curves, cubic curves, and gaps. This version is less efficient than VPath, but it can be made translucent. Can neither be resized nor reoriented (for now). This glyph does not follow the standard object model: (vx,vy) are the coordinates of the path's first point. VPaths do not fire cursor entry/exit events, but it is possible to detect that a cursor is overlapping a VPath by explicitely calling VCursor.interesctsPath(VPath p) and related methods.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VPath
 *@see net.claribole.zvtm.glyphs.DPath
 *@see net.claribole.zvtm.glyphs.DPathST
 *@see com.xerox.VTM.glyphs.VQdCurve
 *@see com.xerox.VTM.glyphs.VCbCurve
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.glyphs.VSegmentST
 *@see com.xerox.VTM.engine.VCursor#intersectsVPath(VPath p)
 */

public class VPathST extends VPath implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    /**
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VPathST(float a){
	super();
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x start coordinate in virtual space
     *@param y start coordinate in virtual space
     *@param z z-index
     *@param c color
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VPathST(long x, long y, int z, Color c, float a){
	super(x, y, z, c);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
        *@param z z-index
        *@param c color
        *@param svg valid <i>d</i> attribute of an SVG <i>path</i> element. m as first coords are taken into account, so any coord list beginning with one of these instructions will make the path begin elsewhere than at (x,y). Absolute commands (uppercase letters) as first coords have the side effect of assigning first point with these values instead of x,y (overriden)
        *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
        */
    public VPathST(int z, Color c, String svg, float a){
        super(z, c, svg);
        alpha = a;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public float getTranslucencyValue(){
	return alpha;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
// 	if (true){//replace by something using projected size (so that we do not paint it if too small)
 	    at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy);
	    at.preConcatenate(stdT);
 	    at.concatenate(AffineTransform.getScaleInstance(coef, coef));
	    g.setTransform(at);
	    if (stroke != null){
		g.setStroke(stroke);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.draw(path);
		    g.setComposite(acO);
		}
		else {
		    g.draw(path);
		}
		g.setStroke(stdS);
	    }
	    else {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.draw(path);
		    g.setComposite(acO);
		}
		else {
		    g.draw(path);
		}
	    }
	    g.setTransform(stdT);
// 	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
// 	if (true){//replace by something using projected size (so that we do not paint it if too small)
 	    at = AffineTransform.getTranslateInstance(dx+pc[i].lcx, dy+pc[i].lcy);
 	    at.concatenate(AffineTransform.getScaleInstance(coef, coef));
	    g.setTransform(at);
	    if (stroke != null){
		g.setStroke(stroke);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.draw(path);
		    g.setComposite(acO);
		}
		else {
		    g.draw(path);
		}
		g.setStroke(stdS);
	    }
	    else {
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.draw(path);
		    g.setComposite(acO);
		}
		else {
		    g.draw(path);
		}
	    }
	    g.setTransform(stdT);
// 	}
    }

    /** Not implemented yet. */
    public Object clone(){
	VPathST res = new VPathST(alpha);
	res.mouseInsideColor = this.mouseInsideColor;
	return res;
    }

}

