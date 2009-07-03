package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

class GlyphColorDelta implements Delta {
	private final ObjId id;
	private final Color color;

	GlyphColorDelta(ObjId id, Color color){
		this.id = id;
		this.color = color;
	}

	public void apply(SlaveUpdater slaveUpdater){
		Glyph toAlter = slaveUpdater.getGlyphById(id);

		if(null == toAlter){
			System.err.println("Could not retrieve glyph id "  + id);
			return;
		}

		toAlter.setColor(color);
	}

	@Override public String toString(){
		return "GlyphColorDelta, target " + id +
			", new color=" + color;
	}
}

