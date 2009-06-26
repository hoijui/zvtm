package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.VirtualSpace;

/**
 * A class that applies incoming messages
 * to a VirtualSpace in order to keep it in sync
 * with a master reference.
 */
public class SlaveUpdater {
	private final VirtualSpace virtualSpace;

	public SlaveUpdater(VirtualSpace vs){
		this.virtualSpace = vs;
		//use slave virtual space name to search for master group

	}
}

