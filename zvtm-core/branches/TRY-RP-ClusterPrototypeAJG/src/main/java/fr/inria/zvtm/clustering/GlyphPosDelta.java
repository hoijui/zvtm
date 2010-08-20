/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

class GlyphPosDelta implements Delta {
	private final ObjId id;
	private final long xPos;
	private final long yPos;

	GlyphPosDelta(ObjId id, long xPos, long yPos){
		this.id = id;
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public void apply(SlaveUpdater slaveUpdater){
		Glyph toAlter = slaveUpdater.getGlyphById(id);

		if(null == toAlter){
			System.err.println("Could not retrieve glyph id "  + id);
			return;
		}

		toAlter.moveTo(xPos, yPos);
	}

	@Override public String toString(){
		return "GlyphPosDelta, target " + id +
			", x=" + xPos + ", y=" + yPos;
	}
}

