/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.glyphs.Glyph;

/**
 * No-op Delta, to be used as a 
 * default implementation while "patching holes"
 * in the Delta coverage. Will be used later to
 * remind library developers that Deltas need implementing
 * for their Glyph or Camera derived types.
 */
class NopDelta implements CreateDelta {
	public void apply(SlaveUpdater slaveUpdater){
		System.err.println("Default delta executed. This probably means that a Glyph or Camera derived class needs updates to its serialization code");
	}

	public Glyph create(){
		throw new Error("not implemented");
	}

	@Override public String toString(){
		return "NopDelta";
	}
}

