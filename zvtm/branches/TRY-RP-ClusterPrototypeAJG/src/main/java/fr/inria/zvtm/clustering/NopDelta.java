package fr.inria.zvtm.clustering;

/**
 * No-op Delta, to be used as a 
 * default implementation while "patching holes"
 * in the Delta coverage. Will be used later to
 * remind library developers that Deltas need implementing
 * for their Glyph or Camera derived types.
 */
class NopDelta implements Delta {
	public void apply(SlaveUpdater slaveUpdater){
		System.err.println("Default delta executed. This probably means that a Glyph or Camera derived class needs updates to its serialization code");
	}

	@Override public String toString(){
		return "NopDelta";
	}
}

