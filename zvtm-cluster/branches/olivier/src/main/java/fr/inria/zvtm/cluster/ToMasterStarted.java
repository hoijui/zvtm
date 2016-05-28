package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterStarted extends ToMasterGeneric {

	public ToMasterStarted(int cvid, int bn){
		super(cvid, bn);
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveStarted(blockNumber);
		}
	}
}