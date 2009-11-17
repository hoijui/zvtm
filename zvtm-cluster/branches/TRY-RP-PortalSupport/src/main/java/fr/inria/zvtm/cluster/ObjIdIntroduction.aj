/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/** 
 * Introduces an object ID into Glyph instances.
 * This object ID will be used for instance to maintain 
 * a shared VirtualSpace state across different address spaces
 */
aspect ObjIdIntroduction {
	declare parents: VirtualSpace implements Identifiable;
	private final ObjId VirtualSpace.id = ObjIdFactory.next();
	public final ObjId VirtualSpace.getObjId(){ return id; }

    declare parents: Glyph implements Identifiable;
	private final ObjId Glyph.id = ObjIdFactory.next();
	public final ObjId Glyph.getObjId(){ return id; }

	declare parents: Camera implements Identifiable;
	private final ObjId Camera.id = ObjIdFactory.next();
	public final ObjId Camera.getObjId(){ return id; }
	}

