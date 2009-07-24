
package fr.inria.zvtm.clustering;

import java.net.URL;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;

/* Delta for ClusteredImage creation */
class ImageCreateDelta implements Delta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint center; //should be moved to Glyph attr??
	private final int zIndex; //should be moved to Glyph attr??
	private final URL location; 
	private final double scale; 

	ImageCreateDelta(ObjId id, LongPoint center, int zIndex,
			URL location, double scale){
		this.id = id;
		this.center = center;
		this.zIndex = zIndex;
		this.location = location;
		this.scale = scale;
	}

	public void apply(SlaveUpdater slaveUpdater){
		ClusteredImage glyph = new ClusteredImage(center.x, center.y,
				zIndex, location, scale);
		slaveUpdater.addGlyph(id, glyph);
	}

	@Override public String toString(){
		return "ImageCreateDelta, Glyph id " + id;
	}

}

