/*
 *   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VirtualSpaceIntroduction.aj 5178 2014-06-27 19:13:49Z epietrig $
 */

package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.GlyphCreation;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.VirtualSpace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
aspect VirtualSpaceIntroduction {
    private boolean VirtualSpace.isZuistOwned = false;
    void VirtualSpace.setZuistOwned(boolean value){ this.isZuistOwned = value; }
    boolean VirtualSpace.isZuistOwned(){ return isZuistOwned; }

    after(Glyph glyph, VirtualSpace virtualSpace) :
        GlyphCreation.glyphAdd(glyph, virtualSpace) &&
        !cflowbelow(GlyphCreation.glyphAdd(Glyph, VirtualSpace)){
        if(virtualSpace.isZuistOwned()){
            glyph.setReplicated(false);
        }
    }
}

