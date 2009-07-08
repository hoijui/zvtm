package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;

/*
 * Note: we assume that every VirtualSpace
 * owns one and only one CameraGroup (this should be 
 * sufficient for our prototype). Therefore, we do
 * not bother with CameraGroup creation or removal, and
 * we do not need a GroupId to move a CameraGroup.
 * Also, the only operation that a CameraGroup supports 
 * is a location change.
 */
class GroupLocDelta implements Delta {
	private final Location location;

	/**
	 * @param location new CameraGroup location
	 */
	GroupLocDelta(Location location){
		this.location = location;
	}

	public void apply(SlaveUpdater slaveUpdater){
		CameraGroup toMove = slaveUpdater.getCameraGroup();

		if(null == toMove){
			System.err.println("Could not retrieve current camera group");
			return;
		}

		toMove.setLocation(location);
	}

	@Override public String toString(){
		return "GroupLocDelta, new location " + location;
	}
}

