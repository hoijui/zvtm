package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;

class RectangleCreateDelta implements Delta {
	private final ObjId id;
	private final LongPoint center;
	private final int zIndex;	
	private final long halfWidth; 
	private final long halfHeight;

	RectangleCreateDelta(ObjId id, LongPoint center, int zIndex,
			long halfWidth, long halfHeight){
		this.id = id;
		this.center = center;
		this.zIndex = zIndex;
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
	}

	public void apply(SlaveUpdater slaveUpdater){
		VRectangle rect = new VRectangle(center.x, center.y,
				zIndex, halfWidth, halfHeight, Color.BLUE);
		slaveUpdater.addGlyph(id, rect);

	}

	@Override public String toString(){
		return "RectangleCreateDelta, Glyph id " + id;
	}

}

