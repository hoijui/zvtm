/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import fr.inria.zvtm.glyphs.Glyph;

//XXX test: use create deltas to implement save/restore in 
//GraffitiWall
public interface CreateDelta extends Delta {
	public Glyph create();
}

