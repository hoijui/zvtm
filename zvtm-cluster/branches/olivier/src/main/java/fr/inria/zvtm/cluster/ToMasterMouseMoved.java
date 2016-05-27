/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterMouseMoved extends ToMasterGeneric {
	int jpx;
	int jpy;

	public ToMasterMouseMoved(int cvid, int bn, int jpx, int jpy){
		super(cvid, bn);
		this.jpx = jpx;
		this.jpy = jpy;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveMouseMoved(blockNumber,jpx, jpy);
		}
	}
}