/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.io.Serializable;

import fr.inria.zvtm.engine.portals.Portal;

public interface PortalReplicator extends Serializable {
   public Portal createPortal(SlaveUpdater updater);
}


