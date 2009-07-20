package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VSegment;

class SegmentCreateDelta implements Delta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint p1; //first endpoint 
	private final LongPoint p2; //second endpoint
	private final int zIndex; //should be moved to Glyph attr??
	private final Color mainColor; //should be moved to Glyph attr??

	SegmentCreateDelta(ObjId id, LongPoint p1, 
			LongPoint p2,
			int zIndex,
		    Color mainColor){
		this.id = id;
		this.p1 = p1;
		this.p2 = p2;
		this.zIndex = zIndex;
		this.mainColor = mainColor;
	}

	public void apply(SlaveUpdater slaveUpdater){
		VSegment segment = new VSegment(p1.x, p1.y,
				zIndex, mainColor, p2.x, p2.y);
		slaveUpdater.addGlyph(id, segment);
	}

	@Override public String toString(){
		return "SegmentCreateDelta, Glyph id " + id;
	}

}

