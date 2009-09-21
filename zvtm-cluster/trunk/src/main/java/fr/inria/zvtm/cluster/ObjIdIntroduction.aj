package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;

public aspect ObjIdIntroduction {
	private final ObjId Glyph.id = ObjIdFactory.next();
	public final ObjId Glyph.getObjId(){ return id; }
}

