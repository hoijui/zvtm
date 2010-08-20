/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;

class CircleCreateDelta implements CreateDelta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint center; //should be moved to Glyph attr??
	private final int zIndex; //should be moved to Glyph attr??
	private final long radius; 
	private final Color mainColor; //should be moved to Glyph attr??
	private final Color borderColor; //should be moved to Glyph attr??

	CircleCreateDelta(ObjId id, LongPoint center, int zIndex,
			long radius, Color mainColor, Color borderColor){
		this.id = id;
		this.center = center;
		this.zIndex = zIndex;
		this.radius = radius;
		this.mainColor = mainColor;
		this.borderColor = borderColor;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.addGlyph(id, create());
	}

	public Glyph create(){
		VCircle circle = new VCircle(center.x, center.y,
				zIndex, radius, mainColor);
		circle.setBorderColor(borderColor);
		return circle;
	}

	@Override public String toString(){
		return "CircleCreateDelta, Glyph id " + id;
	}

}

