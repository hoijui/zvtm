/*   FILE: TransitionManager.java
 *   DATE OF CREATION:  Sun Mar 04 10:59:11 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 

package net.claribole.zvtm.engine;

import java.awt.Color;

import java.util.Vector;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangleST;

/** Creation and management of transition animations such as fade in/fade out for views.
 * @author Emmanuel Pietriga
 */

public class TransitionManager {
    
    static Float F0 = new Float(0);
    static Float Fp1 = new Float(1);
    static Float Fm1 = new Float(-1);

    static Vector fadeOutData;
    static {
	fadeOutData = new Vector();
	fadeOutData.add(F0);
	fadeOutData.add(F0);
	fadeOutData.add(F0);
	fadeOutData.add(F0);
	fadeOutData.add(F0);
	fadeOutData.add(F0);
	fadeOutData.add(Fp1);
    };
    static Vector fadeInData;
    static {
	fadeInData = new Vector();
	fadeInData.add(F0);
	fadeInData.add(F0);
	fadeInData.add(F0);
	fadeInData.add(F0);
	fadeInData.add(F0);
	fadeInData.add(F0);
	fadeInData.add(Fm1);
    };

    /** Make a view fade out, and eventually be painted blank.
     * The view must not be blank for the fade out to work.
     *@param v the view whose content will fade out
     *@param duration duration of the fade out transition in milliseconds
     *@param fadeColor target color for the fade out
     *@param vsm hook to virtual space manager
     */
    public static void fadeOut(View v, int duration, Color fadeColor, VirtualSpaceManager vsm){
	// do not fade out is view is already blank
	if (v.isBlank() != null){return;}
	// get the region of virtual space seen through the
	// camera belonging to the top layer in this view
	Camera c = v.getCameraNumber(v.getLayerCount() - 1);
	long[] wnes = v.getVisibleRegion(c);
	// position the fade rectangle so that it covers this region
	VRectangleST fadeRect = new VRectangleST((wnes[0]+wnes[2])/2, (wnes[1]+wnes[3])/2, 0,
						 (wnes[2]-wnes[0])/2, (wnes[1]-wnes[3])/2,
						 fadeColor, 0);
	fadeRect.setPaintBorder(false);
	vsm.addGlyph(fadeRect, c.getOwningSpace());
	vsm.animator.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, fadeOutData,
					  fadeRect.getID(), new FadeOut(v, fadeColor, c.getOwningSpace()));
    }

    /** Make a view (originally blank) fade in.
     * The view must be blank for the fade in to work.
     *@param v the view whose content will fade in
     *@param duration duration of the fade in transition in milliseconds
     *@param vsm hook to virtual space manager
     */
    public static void fadeIn(View v, int duration, VirtualSpaceManager vsm){
	// do not fade in if view is not blank
	Color fadeColor = v.isBlank();
	if (fadeColor == null){return;}
	// get the region of virtual space seen through the
	// camera belonging to the top layer in this view
	Camera c = v.getCameraNumber(v.getLayerCount() - 1);
	long[] wnes = v.getVisibleRegion(c);
	// position the fade rectangle so that it covers this region
	VRectangleST fadeRect = new VRectangleST((wnes[0]+wnes[2])/2, (wnes[1]+wnes[3])/2, 0,
						 (wnes[2]-wnes[0])/2, (wnes[1]-wnes[3])/2,
						 fadeColor, 1);
	fadeRect.setPaintBorder(false);
	vsm.addGlyph(fadeRect, c.getOwningSpace());
	v.setBlank(null);
	vsm.repaintNow();
	vsm.animator.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, fadeInData,
					  fadeRect.getID(), new FadeIn(v, c.getOwningSpace()));
    }

}

class FadeOut implements PostAnimationAction {
    
    View view;
    Color blankColor;
    VirtualSpace spaceOwningFadeRect;
    
    FadeOut(View v, Color c, VirtualSpace vs){
	this.view = v;
	this.blankColor = c;
	this.spaceOwningFadeRect = vs;
    }

    public void animationEnded(Object target, short type, String dimension){
	view.setBlank(blankColor);
	spaceOwningFadeRect.destroyGlyph((Glyph)target);
    }

}

class FadeIn implements PostAnimationAction {
    
    View view;
    VirtualSpace spaceOwningFadeRect;
    
    FadeIn(View v, VirtualSpace vs){
	this.view = v;
	this.spaceOwningFadeRect = vs;
    }

    public void animationEnded(Object target, short type, String dimension){
	spaceOwningFadeRect.destroyGlyph((Glyph)target);
    }

}
