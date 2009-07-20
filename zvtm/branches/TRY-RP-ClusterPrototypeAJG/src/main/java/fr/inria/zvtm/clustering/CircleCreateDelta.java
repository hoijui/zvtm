package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VCircle;

class CircleCreateDelta implements Delta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint center; //should be moved to Glyph attr??
	private final int zIndex; //should be moved to Glyph attr??
	private final long radius; 
	private final Color mainColor; //should be moved to Glyph attr??

	CircleCreateDelta(ObjId id, LongPoint center, int zIndex,
			long radius, Color mainColor){
		this.id = id;
		this.center = center;
		this.zIndex = zIndex;
		this.radius = radius;
		this.mainColor = mainColor;
	}

	public void apply(SlaveUpdater slaveUpdater){
		VCircle circle = new VCircle(center.x, center.y,
				zIndex, radius, mainColor);
		slaveUpdater.addGlyph(id, circle);
	}

	@Override public String toString(){
		return "CircleCreateDelta, Glyph id " + id;
	}

}

