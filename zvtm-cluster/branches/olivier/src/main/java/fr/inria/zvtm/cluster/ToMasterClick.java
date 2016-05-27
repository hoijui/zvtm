/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterClick extends ToMasterPress { //implements ToMasterMsg {
	int clickNumber;

	public ToMasterClick(int cvid, int bn, int button, int mod, int jpx, int jpy, int clickNumber){
		super(cvid, bn, button, mod, jpx, jpy);
		this.clickNumber = clickNumber;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveClick(blockNumber, button, mod, jpx, jpy, clickNumber);
		}
	}
}