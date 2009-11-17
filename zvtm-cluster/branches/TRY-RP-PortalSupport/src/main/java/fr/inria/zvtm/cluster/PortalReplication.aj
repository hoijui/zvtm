/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.CameraPortal;

/**
 * Quick and dirty portal replication logic, for Mathieu's game.
 * We only provide CameraPortal, plus the following operations:
 *  - ClusteredView#addPortal
 *  - ClusteredView#destroyPortal
 *  - CameraPortal#moveTo
 */
privileged aspect PortalReplication {
	declare parents: CameraPortal implements Identifiable;
	private final ObjId CameraPortal.id = ObjIdFactory.next();
	public final ObjId CameraPortal.getObjId(){ return id; }

	Camera CameraPortal.getCamera() { return camera; }
}

