package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;

/**
 * SlaveUpdater maintains a set of object that may be identified
 * by their ObjectId, and applies Deltas to those objects.
 * Examples of such objects are Glyphs and Cameras.
 */
public class SlaveUpdater {
	//a few design choices: should we present a simple interface to Delta
	//objects?

	private final VirtualSpace virtualSpace;
	public SlaveUpdater(VirtualSpace virtualSpace){
		this.virtualSpace = virtualSpace;
	}
}

