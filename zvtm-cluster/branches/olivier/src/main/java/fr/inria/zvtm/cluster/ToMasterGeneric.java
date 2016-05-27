package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterGeneric implements ToMasterMsg {
	int cvid;
	int blockNumber;

	public ToMasterGeneric(int cvid, int bn){
		this.cvid = cvid;
		this.blockNumber =  bn;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			//cv.slaveGenericMsg(blockNumber);
		}
	}
}