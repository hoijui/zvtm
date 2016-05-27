package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterRelease extends ToMasterPress { //implements ToMasterMsg {

	public ToMasterRelease(int cvid, int bn, int button, int mod, int jpx, int jpy){
		super(cvid, bn, button, mod, jpx, jpy);
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveRelease(blockNumber, button, mod, jpx, jpy);
		}
	}
}