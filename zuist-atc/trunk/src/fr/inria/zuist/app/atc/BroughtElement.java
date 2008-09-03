/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.atc;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.glyphs.DPathST;

abstract class BroughtElement {
	
	Object owner;
	
	static BroughtElement rememberPreviousState(LNode el){
		return new BroughtNode(el);
	}

	static BroughtElement rememberPreviousState(LEdge el){
		return new BroughtEdge((LEdge)el);
	}

	Glyph[] glyphs;
	LongPoint[] previousLocations;
	
	abstract void restorePreviousState(AnimManager animator, int duration);
		
}

class BroughtNode extends BroughtElement {
	
	BroughtNode(LNode n){
		owner = n;
		glyphs = new Glyph[2];
		glyphs[0] = n.getShape();
		glyphs[1] = n.getLabel();
		previousLocations = new LongPoint[glyphs.length];
		for (int i=0;i<glyphs.length;i++){
			previousLocations[i] = glyphs[i].getLocation();
		}
	}

	void restorePreviousState(AnimManager animator, int duration){
		for (int i=0;i<glyphs.length;i++){
			animator.createGlyphAnimation(duration, AnimManager.GL_TRANS_LIN,
			                              new LongPoint(previousLocations[i].x-glyphs[i].vx, previousLocations[i].y-glyphs[i].vy),
			                              glyphs[i].getID());
		}
	}
	
}

class BroughtEdge extends BroughtElement {

	DPathST spline;
	float splineAlpha;
	LongPoint[] splineCoords;
	
	BroughtEdge(LEdge e){
		owner = e;
		spline = e.getSpline();
		if (spline != null){
			splineCoords = spline.getAllPointsCoordinates();
			splineAlpha = spline.getTranslucencyValue();
		}
	}
	
	void restorePreviousState(AnimManager animator, int duration){
		animator.createPathAnimation(duration, AnimManager.DP_TRANS_SIG_ABS, splineCoords, spline.getID(), null);
		spline.setTranslucencyValue(splineAlpha);
	}
	
}
