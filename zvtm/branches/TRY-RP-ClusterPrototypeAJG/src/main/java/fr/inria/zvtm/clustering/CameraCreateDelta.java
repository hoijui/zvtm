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

