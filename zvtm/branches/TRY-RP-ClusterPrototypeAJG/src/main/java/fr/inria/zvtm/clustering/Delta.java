package fr.inria.zvtm.clustering;

import java.io.Serializable;
import fr.inria.zvtm.engine.VirtualSpace;

/**
 * A delta that can be applied to a VirtualSpace
 * to change its state. Examples of deltas are
 * a glyph position or color change, a rectangle creation,
 * a glyph deletion, ...
 * Deltas are sent over the wire and therefore should be
 * serializable.
 * Important note: this interface is called 'Delta' because
 * a Delta enables us to move from one VirtualSpace state to
 * the next. 
 * It does *not* imply a glyph- or camera-level delta, i.e.
 * GlyphPosDelta will probably store *absolute* coordinates and
 * internally use moveTo and not move. That said, it should not
 * make any difference for users. It is simply a design choice.
 */
interface Delta extends Serializable {
	public void apply(VirtualSpace vs);
}

