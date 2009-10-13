/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.util.ArrayList;
import java.util.Vector;
 
import fr.inria.zvtm.engine.Camera;

class ClusteredViewCreateDelta implements Delta {
	private final ObjId objId;
	private final int origin;
	private final int blockWidth;
	private final int blockHeight;
	private final int nbRows;
	private final int nbCols;
	private final ArrayList<ObjId> camRefs;

	ClusteredViewCreateDelta(ClusteredView cv){
		this.objId = cv.getObjId();
		this.origin = cv.getOrigin();
		this.blockWidth = cv.getBlockWidth();
		this.blockHeight = cv.getBlockHeight();
		this.nbRows = cv.getNbRows();
		this.nbCols = cv.getNbCols();
		this.camRefs = makeCamRefs(cv.getCameras());
	}

	private static final ArrayList<ObjId> 
		makeCamRefs(Vector<Camera> cameras){
			ArrayList<ObjId> retval = new ArrayList<ObjId>();
			for(Camera cam: cameras){
				retval.add(cam.getObjId());
			}
			return retval;
		}

	private final Vector<Camera>
		refsToCameras(SlaveUpdater su){
			Vector<Camera> retval = new Vector<Camera>();
			for(ObjId camRef: camRefs){
				retval.add((Camera)(su.getSlaveObject(camRef)));
			}
			return retval;
		}

	public void apply(SlaveUpdater updater){
		//create and register the local clustered view
		ClusteredView cv = new ClusteredView(origin,
				blockWidth, blockHeight,
				nbRows, nbCols,
				refsToCameras(updater));
		updater.putSlaveObject(objId, cv);
		//set owning view for cameras
		//ask slaveupdater to create a local view
		updater.createLocalView(cv);
	}

	@Override public String toString(){
		return "ClusteredViewCreateDelta, view id: " + objId;
	}
}

