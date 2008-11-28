/*   FILE: GlyphKillAction.java
 *   DATE OF CREATION:  Thu Dec 22 17:25:28 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;

public class GlyphKillAction implements PostAnimationAction {

    VirtualSpaceManager vsm;
    
    public GlyphKillAction(VirtualSpaceManager vsm){
	this.vsm = vsm;
    }
    
	public void animationEnded(Object target, short type, String dimension){
		if (type == PostAnimationAction.GLYPH){
			Glyph g = (Glyph)target;
			vsm.getOwningSpace(g).removeGlyph(g);
		}
	}
    
}