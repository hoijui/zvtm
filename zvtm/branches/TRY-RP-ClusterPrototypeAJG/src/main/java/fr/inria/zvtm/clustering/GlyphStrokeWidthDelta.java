package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

class GlyphStrokeWidthDelta implements Delta {
	private final ObjId id;
	private final float strokeWidth;

	GlyphStrokeWidthDelta(ObjId id, float strokeWidth){
		this.id = id;
		this.strokeWidth = strokeWidth;
	}

	public void apply(SlaveUpdater slaveUpdater){
		Glyph toAlter = slaveUpdater.getGlyphById(id);

		if(null == toAlter){
			System.err.println("Could not retrieve glyph id "  + id);
			return;
		}

		toAlter.setStrokeWidth(strokeWidth);
	}

	@Override public String toString(){
		return "GlyphStrokeWidthDelta, target " + id +
			", new stroke width=" + strokeWidth;
	}
}

