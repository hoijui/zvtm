package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * A VirtualSpace that can be replicated over a cluster
 */
public aspect MasterVirtualSpace {
	public Glyph VirtualSpace.getGlyphById(ObjId id){
		//non-functional implementation, obviously
		System.out.println("calling aspect-woven getGlyphById");
		return null;
	}
}

