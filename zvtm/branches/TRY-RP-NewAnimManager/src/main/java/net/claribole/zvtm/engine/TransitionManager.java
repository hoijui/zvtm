/*   FILE: TransitionManager.java
 *   DATE OF CREATION:  Sun Mar 04 10:59:11 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import java.awt.Color;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VRectangleST;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.EndAction;

/** Creation and management of transition animations such as fade in/fade out for views.
 * @author Emmanuel Pietriga
 */

public class TransitionManager {

    /** Make a view fade out, and eventually be painted blank.
     * The view must not be blank for the fade out to work.
     *@param v the view whose content will fade out
     *@param duration duration of the fade out transition in milliseconds
     *@param fadeColor target color for the fade out
     *@param vsm hook to virtual space manager
     *@see #fadeOut(View v, int duration, Color fadeColor, VirtualSpaceManager vsm, EndAction action)
     */
    public static void fadeOut(View v, int duration, Color fadeColor, VirtualSpaceManager vsm){
	TransitionManager.fadeOut(v, duration, fadeColor, vsm, null);
    }

    /** Make a view fade out, and eventually be painted blank.
     * The view must not be blank for the fade out to work.
     *@param v the view whose content will fade out
     *@param duration duration of the fade out transition in milliseconds
     *@param fadeColor target color for the fade out
     *@param vsm hook to virtual space manager
     *@param action fade out action. Specify a custom subclass of FadeOut if you need to execute specific actions after the fade out itself. See FadeOut to learn how to properly subclass it.
     *@see #fadeOut(View v, int duration, Color fadeColor, VirtualSpaceManager vsm)
     *@see net.claribole.zvtm.engine.FadeOut
     */
    public static void fadeOut(View v, int duration, final Color fadeColor, 
			       VirtualSpaceManager vsm, final EndAction action){
	// do not fade out is view is already blank
	if (v.isBlank() != null){return;}
	// get the region of virtual space seen through the
	// camera belonging to the top layer in this view
	Camera c = v.getCameraNumber(v.getLayerCount() - 1);
	long[] wnes = v.getVisibleRegion(c);
	// position the fade rectangle so that it covers this region
	final VRectangleST fadeRect = new VRectangleST((wnes[0]+wnes[2])/2, (wnes[1]+wnes[3])/2, 0,
						       (wnes[2]-wnes[0])/2, (wnes[1]-wnes[3])/2,
						       fadeColor, fadeColor, 0);
	fadeRect.setDrawBorder(false);
	vsm.addGlyph(fadeRect, c.getOwningSpace());

	Animation trans = vsm.getAnimationManager().getAnimationFactory()
	    .createTranslucencyAnim(duration, fadeRect,
				    1f, false, IdentityInterpolator.getInstance(),
				    new EndAction(){
					public void execute(Object subject, Animation.Dimension dimension){
					    view.setBlank(fadeColor);
					    vsm.removeGlyph(fadeRect);

					    if(null != action){
						action.execute(subject, dimension);
					    }
					}
				    });

	vsm.getAnimationManager().startAnimation(trans, false);
    }

    /** Make a view (originally blank) fade in.
     * The view must be blank for the fade in to work.
     *@param v the view whose content will fade in
     *@param duration duration of the fade in transition in milliseconds
     *@param vsm hook to virtual space manager
     *@see #fadeIn(View v, int duration, VirtualSpaceManager vsm, FadeIn actions)
     */
    public static void fadeIn(View v, int duration, VirtualSpaceManager vsm){
	TransitionManager.fadeIn(v, duration, vsm, null);
    }    

    /** Make a view (originally blank) fade in.
     * The view must be blank for the fade in to work.
     *@param v the view whose content will fade in
     *@param duration duration of the fade in transition in milliseconds
     *@param vsm hook to virtual space manager
     *@param actions fade in action. Executed after the view fade-in.
     *@see #fadeIn(View v, int duration, VirtualSpaceManager vsm)
     *@see net.claribole.zvtm.engine.FadeIn
     */
    public static void fadeIn(View v, int duration, VirtualSpaceManager vsm, EndAction action){
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
						 fadeColor, fadeColor, 1);
	fadeRect.setDrawBorder(false);
	vsm.addGlyph(fadeRect, c.getOwningSpace());
	v.setBlank(null);
	vsm.repaintNow();

	Animation trans = vsm.getAnimationManager().getAnimationFactory()
	    .createTranslucencyAnim(duration, fadeRect,
				    0f, false, IdentityInterpolator.getInstance(),
				    new EndAction(){
					public void execute(Object subject, Animation.Dimension dimension){
					    vsm.removeGlyph(fadeRect);

					    if(null != action){
						action.execute(subject, dimension);
					    }
					}
				    });

	vsm.getAnimationManager().startAnimation(trans, false);
    }

}
