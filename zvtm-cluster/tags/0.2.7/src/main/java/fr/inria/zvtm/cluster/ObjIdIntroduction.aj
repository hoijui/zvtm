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
    declare parents: VirtualSpace extends DefaultIdentifiable;
    declare parents: Glyph extends  DefaultIdentifiable;
    declare parents: Camera extends DefaultIdentifiable;
    }

