package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;

class RectangleCreateDelta implements Delta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint center; //should be moved to Glyph attr??
	private final int zIndex; //should be moved to Glyph attr??
	private final long halfWidth; 
	private final long halfHeight;
	private final Color mainColor; //should be moved to Glyph attr??

	RectangleCreateDelta(ObjId id, LongPoint center, int zIndex,
			long halfWidth, long halfHeight, Color mainColor){
		this.id = id;
		this.center = center;
		this.zIndex = zIndex;
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		this.mainColor = mainColor;
	}

	public void apply(SlaveUpdater slaveUpdater){
		VRectangle rect = new VRectangle(center.x, center.y,
				zIndex, halfWidth, halfHeight, mainColor);
		slaveUpdater.addGlyph(id, rect);
	}

	@Override public String toString(){
		return "RectangleCreateDelta, Glyph id " + id;
	}

}

