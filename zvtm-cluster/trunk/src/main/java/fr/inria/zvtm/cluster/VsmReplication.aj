/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Replicates 'interesting' calls made on the Virtual Space Manager
 * (virtual space creation, destruction, global vsm operations)
 */
aspect VsmReplication {

	//advise virtual space creation and destruction
	after(VirtualSpaceManager vsm) returning(VirtualSpace vs):
		target(vsm) && execution(VirtualSpace addVirtualSpace(String)) 
		&& if(VirtualSpaceManager.INSTANCE.isMaster()) {
			vsm.sendDelta(
					new VsCreateDelta(vs.getName(),	vs.getObjId())
					);
		}

	//Note that VirtualSpaceManager being a singleton, we do
	//not need to transport an object reference to it.

	private static class VsCreateDelta implements Delta {
		private final String spaceName;
		private final ObjId spaceId;

		VsCreateDelta(String spaceName, ObjId spaceId){
			this.spaceName = spaceName;
			this.spaceId = spaceId;
		}

		public void apply(SlaveUpdater su){
			VirtualSpace vs = 
				VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
			su.putSlaveObject(spaceId, vs);
		}

		@Override public String toString(){
			return String.format(
					"VsCreateDelta, virtualSpace name: %s id: %s",
					spaceName,
					spaceId
					);
		}
	}

	private static class VsDestroyDelta implements Delta {
		private final String spaceName;
		private final ObjId spaceId;

		VsDestroyDelta(String spaceName, ObjId spaceId){
			this.spaceName = spaceName;
			this.spaceId = spaceId;
		}

		public void apply(SlaveUpdater su){
			su.removeSlaveObject(spaceId);
			VirtualSpaceManager.INSTANCE.destroyVirtualSpace(spaceName);
		}

		@Override public String toString(){
			return String.format(
					"VsDestroyDelta, virtualSpace name: %s",
					spaceName,
					spaceId
					);
		}
	}
}

