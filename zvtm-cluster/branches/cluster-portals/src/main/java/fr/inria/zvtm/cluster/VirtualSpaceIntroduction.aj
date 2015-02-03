/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

public aspect VirtualSpaceIntroduction {
    //introduce parent space attribute to Glyph
    private VirtualSpace Glyph.parentSpace = null;

    VirtualSpace Glyph.getParentSpace(){ return parentSpace; }

    void Glyph.setParentSpace(VirtualSpace parentSpace){
        this.parentSpace = parentSpace;
    }
}

