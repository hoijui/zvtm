/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.net.URL;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/* Delta for ClusteredImage creation */
class ImageCreateDelta implements CreateDelta {
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
		slaveUpdater.addGlyph(id, create());
	}

	public Glyph create(){
		ClusteredImage glyph = new ClusteredImage(center.x, center.y,
				zIndex, location, scale);
		return glyph;
	}

	@Override public String toString(){
		return "ImageCreateDelta, Glyph id " + id;
	}

}

