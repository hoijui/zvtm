package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.GlyphCreation;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.VirtualSpace;

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

