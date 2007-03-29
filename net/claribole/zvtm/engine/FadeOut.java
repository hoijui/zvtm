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

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.Glyph;

/** Fade Out transition for Views.
 * Subclasses must call super(v, c, vs); in their constructor, and super.animationEnded(target, type, dimension); at the beginning of their implementation of animationEnded(Object target, short type, String dimension).
 * @author Emmanuel Pietriga
 */

public class FadeOut implements PostAnimationAction {
    
    View view;
    Color blankColor;
    VirtualSpace spaceOwningFadeRect;

    public FadeOut(View v, Color c, VirtualSpace vs){
	this.view = v;
	this.blankColor = c;
	this.spaceOwningFadeRect = vs;
    }

    public void animationEnded(Object target, short type, String dimension){
	view.setBlank(blankColor);
	spaceOwningFadeRect.destroyGlyph((Glyph)target);
    }

}
