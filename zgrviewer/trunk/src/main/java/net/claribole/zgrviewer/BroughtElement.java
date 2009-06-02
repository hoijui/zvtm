/*   FILE: LNode.java
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import com.xerox.VTM.engine.VirtualSpaceManager;
import net.claribole.zvtm.animation.AnimationManager;
import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.glyphs.DPath;

abstract class BroughtElement {
	
	static BroughtElement rememberPreviousState(LElem el){
		if (el instanceof LNode){return new BroughtNode((LNode)el);}
		else if (el instanceof LEdge){return new BroughtEdge((LEdge)el);}
		else {return null;}
	}

	Glyph[] glyphs;
	LongPoint[] previousLocations;
	
	abstract LongPoint restorePreviousState(int duration, Glyph g);
		
}

class BroughtNode extends BroughtElement {
	
	float[] previousSize;
	
	BroughtNode(LNode n){
		glyphs = n.getGlyphs();
		previousLocations = new LongPoint[glyphs.length];
		previousSize = new float[glyphs.length];
		for (int i=0;i<glyphs.length;i++){
			previousLocations[i] = glyphs[i].getLocation();
            previousSize[i] = (glyphs[i] instanceof VText) ? ((VText)glyphs[i]).getScale() : glyphs[i].getSize();
		}
	}

	LongPoint restorePreviousState(int duration, Glyph g){
		for (int i=0;i<glyphs.length;i++){
		    if (glyphs[i] instanceof VText){
		        final VText t = (VText)glyphs[i];
		        final float sz = previousSize[i];
    		    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createGlyphTranslation(
    		        duration, glyphs[i], previousLocations[i], false, SlowInSlowOutInterpolator.getInstance(),
    		        new EndAction(){
    		            public void execute(Object subject, Animation.Dimension dimension){
                            t.setScale(sz);
    		            }
    		        }
    		    );
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
		    }
		    else {
		        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createGlyphTranslation(
    		        duration, glyphs[i], previousLocations[i], false, SlowInSlowOutInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
                if (previousSize[i] != glyphs[i].getSize()){
                    a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createGlyphSizeAnim(
                        duration, glyphs[i], previousSize[i], false, SlowInSlowOutInterpolator.getInstance(), null);
                    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
                }		        
		    }
        }
		int i = Utilities.indexOfGlyph(glyphs, g);
        return (i != -1) ? previousLocations[i] : null;
	}
	
}

class BroughtEdge extends BroughtElement {

	DPath spline;
	float splineAlpha;
	LongPoint[] splineCoords;
	
	BroughtEdge(LEdge e){
		glyphs = e.getGlyphs();
		spline = e.getSpline();
		if (spline != null){
			splineCoords = spline.getAllPointsCoordinates();
			splineAlpha = spline.getTranslucencyValue();
		}
		previousLocations = new LongPoint[glyphs.length];
		for (int i=0;i<glyphs.length;i++){
			if (glyphs[i] == spline){
				previousLocations[i] = null;
			}
			else if (glyphs[i] instanceof VText){
				previousLocations[i] = glyphs[i].getLocation();
			}
			else {
				// probably a tail or head decoration, we've just hidden the glyph, don't do anything
				previousLocations[i] = null;
			}
		}
	}
	
	LongPoint restorePreviousState(int duration, Glyph g){
	    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createPathAnim(
	        duration, spline, splineCoords,
		    false, SlowInSlowOutInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
		spline.setTranslucencyValue(splineAlpha);
		for (int i=0;i<glyphs.length;i++){
			if (!glyphs[i].isVisible()){
				glyphs[i].setVisible(true);
			}
			if (previousLocations[i] != null){
			    a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createGlyphTranslation(
    		        duration, glyphs[i], previousLocations[i], false, SlowInSlowOutInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
			}
		}
		return null;
	}
	
}
