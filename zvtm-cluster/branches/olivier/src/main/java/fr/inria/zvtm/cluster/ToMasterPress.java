package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterPress extends ToMasterGeneric { //implements ToMasterMsg {
	int button;
	int mod;
	int jpx;
	int jpy;

	public ToMasterPress(int cvid, int bn, int button, int mod, int jpx, int jpy){
		super(cvid, bn);
		this.button = button;
		this.mod = mod;
		this.jpx = jpx;
		this.jpy = jpy;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slavePress(blockNumber, button, mod, jpx, jpy);
		}
	}
}