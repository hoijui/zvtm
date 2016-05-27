/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterKtype extends ToMasterGeneric {
	 char c;
	 int code;
	 int mod;

	public ToMasterKtype(int cvid, int bn, char c, int code, int mod){
		super(cvid, bn);
		this.c = c;
		this.code = code;
		this.mod = mod;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveKtype(blockNumber, c, code, mod);
		}
	}
}