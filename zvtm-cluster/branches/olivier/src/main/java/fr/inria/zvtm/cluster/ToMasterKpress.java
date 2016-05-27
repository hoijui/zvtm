/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterKpress extends ToMasterKtype {

	public ToMasterKpress(int cvid, int bn, char c, int code, int mod){
		super(cvid, bn, c, code, mod);
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveKpress(blockNumber, c, code, mod);
		}
	}
}