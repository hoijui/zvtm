/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.event;

import fr.inria.zvtm.glyphs.Glyph;

public interface SelectionListener {
    
    public void glyphSelected(Glyph g, boolean selected);
    
}
