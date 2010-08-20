/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

class GlyphVisibilityDelta implements Delta {
	private final ObjId id;
	private final boolean visible;

	GlyphVisibilityDelta(ObjId id, boolean visible){
		this.id = id;
		this.visible = visible;
	}

	public void apply(SlaveUpdater slaveUpdater){
		Glyph toAlter = slaveUpdater.getGlyphById(id);

		if(null == toAlter){
			System.err.println("Could not retrieve glyph id "  + id);
			return;
		}

		toAlter.setVisible(visible);
	}

	@Override public String toString(){
		return "GlyphVisibilityDelta, target " + id +
			", new state=" + (visible ? "visible" : "invisible");
	}
}

