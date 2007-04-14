/*   FILE: DPathST.java
 *   DATE OF CREATION:   Sat Apr 14 10:16 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.glyphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;

import net.claribole.zvtm.glyphs.projection.ProjectedCoords;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.Translucent;

/**
 * Dynamic Path, made of an arbitrary number of segments, quadratic curves, cubic curves, and gaps. All of these can be dynamically modified and animated through AnimManager's createPathAnimation method. This version is less efficient than DPath, but it can be made translucent.
 *@author Emmanuel Pietriga
 *@see net.claribole.zvtm.glyphs.DPath
 *@see com.xerox.VTM.glyphs.VPath
 *@see net.claribole.zvtm.glyphs.VPathST
 *@see com.xerox.VTM.glyphs.VQdCurve
 *@see com.xerox.VTM.glyphs.VCbCurve
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.glyphs.VSegmentST
 *@see com.xerox.VTM.engine.VCursor#intersectsVPath(VPath p)
 *@see com.xerox.VTM.engine.AnimManager#createPathAnimation(long duration, short type, LongPoint[] data, Long gID, PostAnimationAction paa)
 */

public class DPathST extends DPath implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    /**
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public DPathST(float a){
	super();
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x start coordinate in virtual space
     *@param y start coordinate in virtual space
     *@param z altitude
     *@param c color
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public DPathST(long x, long y, float z, Color c, float a){
	super(x, y, z, c);
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
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.translate(dx,dy);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getShape(i));		
		}
		g.setComposite(acO);
	    }
	    else {
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getShape(i));		
		}
	    }
	    g.translate(-dx,-dy);
	    g.setStroke(stdS);
	}
	else {
	    g.translate(dx,dy);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getShape(i));
		}
		g.setComposite(acO);
	    }
	    else {
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getShape(i));
		}
	    }
	    g.translate(-dx,-dy);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (stroke!=null) {
	    g.setStroke(stroke);
	    g.translate(dx,dy);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getlShape(i));
		}
		g.setComposite(acO);
	    }
	    else {
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getlShape(i));
		}
	    }
	    g.translate(-dx,-dy);
	    g.setStroke(stdS);
	}
	else {
	    g.translate(dx,dy);
	    if (alpha < 1.0f){
		g.setComposite(acST);
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getlShape(i));
		}
		g.setComposite(acO);
	    }
	    else {
		for (int j=0;j<elements.length;j++){
		    if (elements[j].type == DPath.MOV){continue;}
		    g.draw(elements[j].getlShape(i));
		}
	    }
	    g.translate(-dx,-dy);
	}
    }

    /** Not implemented yet. */
    public Object clone(){
	return new DPathST(alpha);
    }

}
