package fr.inria.zvtm.clustering;

class GlyphRemoveDelta implements Delta {
	private final ObjId id;

	GlyphRemoveDelta(ObjId id){
		this.id = id;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.removeGlyph(id);
	}

	@Override public String toString(){
		return "GlyphRemoveDelta, Glyph id " + id;
	}

}

