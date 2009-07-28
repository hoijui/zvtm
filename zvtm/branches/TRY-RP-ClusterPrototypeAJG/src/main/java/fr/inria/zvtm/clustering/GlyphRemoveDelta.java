/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

class GlyphRemoveDelta implements Delta {
	private final ObjId id;

	GlyphRemoveDelta(ObjId id){
		this.id = id;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.removeGlyph(id);
	}

	@Override public String toString(){
		return "GlyphRemoveDelta, Glyph id " + id;
	}

}

