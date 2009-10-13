/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.ClosedShape;

class ClosedShapeFillDelta implements Delta {
	private final ObjId id;
	private final boolean isfilled;

	ClosedShapeFillDelta(ObjId id, boolean isfilled){
		this.id = id;
		this.isfilled = isfilled;
	}

	public void apply(SlaveUpdater slaveUpdater){
		ClosedShape toAlter = (ClosedShape)(slaveUpdater.getGlyphById(id));

		if(null == toAlter){
			System.err.println("Could not retrieve glyph id "  + id);
			return;
		}

		toAlter.setFilled(isfilled);
	}

	@Override public String toString(){
		return "ClosedShapeFillDelta, target " + id +
			", new fill status=" + isfilled;
	}
}

