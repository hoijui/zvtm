/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.event;

import fr.inria.zvtm.glyphs.Glyph;

/** Interface to handle picking events (entering/exiting a glyph).
 * @author Emmanuel Pietriga
 */

public interface PickerListener {

    /*----------------- Glyph events -------------------- */

    /** Cursor entered glyph callback.
     *@param g Glyph instance the cursor just entered
     */
    public void enterGlyph(Glyph g);

    /** Cursor exited glyph callback.
     *@param g Glyph instance the cursor just exited
     */
    public void exitGlyph(Glyph g);

}
