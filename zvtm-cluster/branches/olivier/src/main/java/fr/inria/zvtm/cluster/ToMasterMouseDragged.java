/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterMouseDragged extends ToMasterPress { //implements ToMasterMsg {

	public ToMasterMouseDragged(int cvid, int bn, int button, int mod, int jpx, int jpy){
		super(cvid, bn, button, mod, jpx, jpy);
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveMouseDragged(blockNumber, button, mod, jpx, jpy);
		}
	}
}