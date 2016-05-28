/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class EventForwardingDelta implements Delta {
	private final ObjId<ClusteredView> objId;
	private final boolean dofwd;

	EventForwardingDelta(ClusteredView cv, boolean b){
		this.objId = cv.getObjId();
		dofwd = b;
	}

    public void apply(SlaveUpdater updater){
    	ClusteredView cv = updater.getSlaveObject(objId);
        updater.enableEventForwarding(cv, dofwd);
    }
}

