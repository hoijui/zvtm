/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Replicate interesting events on virtual spaces
 */
aspect VirtualSpaceReplication {
	pointcut cameraAdd(VirtualSpace virtualSpace):
		execution(public Camera VirtualSpace.addCamera())
		&& this(virtualSpace)
		&& if(VirtualSpaceManager.INSTANCE.isMaster());

	after(VirtualSpace vs) returning(Camera cam):
	   cameraAdd(vs) && !cflowbelow(cameraAdd(VirtualSpace)){
		   Delta delta = new CameraCreateDelta(vs.getObjId(),
				   cam.getObjId());
		   VirtualSpaceManager.INSTANCE.sendDelta(delta);
	   }	   

	private static class CameraCreateDelta implements Delta{
		private final ObjId vsId;
		private final ObjId camId;

		CameraCreateDelta(ObjId vsId, ObjId camId){
			this.vsId = vsId;
			this.camId = camId;
		}

		public void apply(SlaveUpdater updater){
			VirtualSpace vs = (VirtualSpace)(updater.getSlaveObject(vsId));
			Camera cam = vs.addCamera();
			updater.putSlaveObject(camId, cam);
		}
		
		@Override public String toString(){
			return "CameraCreateDelta";
		}
	}
}

