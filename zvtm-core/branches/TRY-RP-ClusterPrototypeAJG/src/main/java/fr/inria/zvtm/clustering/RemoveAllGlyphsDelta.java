/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

class RemoveAllGlyphsDelta implements Delta {

	RemoveAllGlyphsDelta(){}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.removeAllGlyphs();
	}

	@Override public String toString(){
		return "RemoveAllGlyphsDelta";
	}
}
