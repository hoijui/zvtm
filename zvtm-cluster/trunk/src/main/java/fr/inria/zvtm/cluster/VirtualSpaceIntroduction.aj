package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

public aspect VirtualSpaceIntroduction {
	//introduce parent space attribute to Glyph
	private VirtualSpace Glyph.parentSpace = null;
	VirtualSpace Glyph.getParentSpace(){ return parentSpace; }
	
	//introduce attribute slave to VirtualSpace
	private boolean VirtualSpace.isSlave = false;
	boolean VirtualSpace.isSlave(){ return isSlave; }
	void VirtualSpace.setSlave(boolean b){ isSlave = b; }
}

