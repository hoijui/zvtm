/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;

class ClusteredViewCreateDelta implements Delta {
    private final ObjId<ClusteredView> objId;
    private final ClusterGeometry clGeom;
    private final int origin;
    private final int viewCols;
    private final int viewRows;
    private final ArrayList<ObjId<Camera>> camRefs;
    private final Color bgColor;

    ClusteredViewCreateDelta(ClusteredView cv){
        this.objId = cv.getObjId();
        this.clGeom = cv.getClusterGeometry();
        this.origin = cv.getOrigin();
        this.viewCols = cv.getViewCols();
        this.viewRows = cv.getViewRows();
        this.camRefs = makeCamRefs(cv.getCameras());
        this.bgColor = cv.getBackgroundColor();
    }

    private static final ArrayList<ObjId<Camera>>
        makeCamRefs(Vector<Camera> cameras){
            ArrayList<ObjId<Camera>> retval = new ArrayList<ObjId<Camera>>();
            for(Camera cam: cameras){
                retval.add(cam.getObjId());
            }
            return retval;
        }

    private final Vector<Camera>
        refsToCameras(SlaveUpdater su){
            Vector<Camera> retval = new Vector<Camera>();
            for(ObjId<Camera> camRef: camRefs){
                retval.add(su.getSlaveObject(camRef));
            }
            return retval;
        }

    public void apply(SlaveUpdater updater){
        //create and register the local clustered view
        ClusteredView cv = new ClusteredView(
                clGeom,
                origin,
                viewCols, viewRows,
                refsToCameras(updater));
        cv.setBackgroundColor(bgColor);
        updater.putSlaveObject(objId, cv);
        //set owning view for cameras
        //ask slaveupdater to create a local view
        updater.createLocalView(cv);
    }

    @Override public String toString(){
        return "ClusteredViewCreateDelta, view id: " + objId;
    }
}

