package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

class TextCreateDelta implements CreateDelta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint pos; //should be moved to Glyph attr??
	private final int zIndex; //should be moved to Glyph attr??
	private final Color mainColor; //should be moved to Glyph attr??
	private final short anchor;
	private final String text;
	private final float scale;

	TextCreateDelta(ObjId id, LongPoint pos, 
			int zIndex,
			Color mainColor,
			String text,
			short anchor,
			float scale){
		this.id = id;
		this.pos = pos;
		this.zIndex = zIndex;
		this.mainColor = mainColor;
		this.text = text;
		this.anchor = anchor;
		this.scale = scale;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.addGlyph(id, create());
	}

	public Glyph create(){
		VText glyph = new VText(pos.x, pos.y,
				zIndex, mainColor,
				text, anchor, scale);
		return glyph;
	}

	@Override public String toString(){
		return "TextCreateDelta, Glyph id " + id;
	}

}

