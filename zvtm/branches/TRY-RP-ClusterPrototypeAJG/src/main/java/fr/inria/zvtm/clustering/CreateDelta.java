package fr.inria.zvtm.clustering;

import fr.inria.zvtm.glyphs.Glyph;

//XXX test: use create deltas to implement save/restore in 
//GraffitiWall
public interface CreateDelta extends Delta {
	public Glyph create();
}

