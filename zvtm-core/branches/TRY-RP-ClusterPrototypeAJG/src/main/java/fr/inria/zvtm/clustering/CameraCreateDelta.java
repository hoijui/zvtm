/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;

class CameraCreateDelta implements Delta {
	private final ObjId id; 

	CameraCreateDelta(ObjId id){
		this.id = id;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.addCamera(id);
	}

	@Override public String toString(){
		return "CameraCreateDelta, Glyph id " + id;
	}

}

