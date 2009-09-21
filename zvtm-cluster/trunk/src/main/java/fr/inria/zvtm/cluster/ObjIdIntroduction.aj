package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;

/** 
 * Introduces an object ID into Glyph instances.
 * This object ID will be used for instance to maintain 
 * a shared VirtualSpace state across different address spaces
 */
public aspect ObjIdIntroduction {
	private final ObjId Glyph.id = ObjIdFactory.next();
	public final ObjId Glyph.getObjId(){ return id; }
}

