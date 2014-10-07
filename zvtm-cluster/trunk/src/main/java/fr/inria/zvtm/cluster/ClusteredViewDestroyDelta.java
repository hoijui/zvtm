/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class ClusteredViewDestroyDelta implements Delta {
    private final ObjId<ClusteredView> objId;

    ClusteredViewDestroyDelta(ClusteredView cv){
        this.objId = cv.getObjId();
    }

    public void apply(SlaveUpdater updater){
        ClusteredView cv = updater.getSlaveObject(objId);
        if(cv != null){
            updater.destroyLocalView(cv);
            updater.removeSlaveObject(objId);
        } else {
            System.err.println("warning: trying to access a non-existent ClusteredView");
        }
    }

    @Override public String toString(){
        return "ClusteredViewDestroyDelta, view id: " + objId;
    }
}

