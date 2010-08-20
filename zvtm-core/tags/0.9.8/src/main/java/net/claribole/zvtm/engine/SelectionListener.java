/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.engine;

import com.xerox.VTM.glyphs.Glyph;

public interface SelectionListener {
    
    public void glyphSelected(Glyph g, boolean selected);
    
}
