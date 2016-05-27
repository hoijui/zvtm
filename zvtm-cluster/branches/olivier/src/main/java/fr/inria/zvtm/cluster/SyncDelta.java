/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class SyncDelta implements Delta {
	private final ObjId<ClusteredView> objId;
	private final boolean dosync;

	SyncDelta(ClusteredView cv, boolean b){
		this.objId = cv.getObjId();
		dosync = b;
	}

    public void apply(SlaveUpdater updater){
    	ClusteredView cv = updater.getSlaveObject(objId);
        updater.setSyncronous(cv, dosync);
    }
}

