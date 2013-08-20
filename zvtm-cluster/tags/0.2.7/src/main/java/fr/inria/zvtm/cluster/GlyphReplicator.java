package fr.inria.zvtm.cluster;

import java.io.Serializable;

import fr.inria.zvtm.glyphs.Glyph;

public interface GlyphReplicator extends Serializable {
   public Glyph createGlyph();
}

