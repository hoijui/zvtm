package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

public aspect VirtualSpaceIntroduction {
	//introduce parent space attribute to Glyph
	private VirtualSpace Glyph.parentSpace = null;
	VirtualSpace Glyph.getParentSpace(){ return parentSpace; }
}

