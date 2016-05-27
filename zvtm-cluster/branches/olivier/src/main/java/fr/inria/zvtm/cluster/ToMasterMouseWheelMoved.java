/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

public class ToMasterMouseWheelMoved extends ToMasterMouseMoved { //implements ToMasterMsg {
	short wheelDirection;
	
	public ToMasterMouseWheelMoved(int cvid, int bn, short wheelDirection, int jpx, int jpy){
		super(cvid, bn, jpx, jpy);
		this.wheelDirection = wheelDirection;
	}

	public void apply(){
		ClusteredView cv = VirtualSpaceManager.INSTANCE.getClusteredViewById(cvid);
		if (cv != null)	{
			cv.slaveMouseWheelMoved(blockNumber, wheelDirection, jpx, jpy);
		}
	}
}