/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file LICENSE.
 *
 * $Id:  $
 */

package fr.inria.ilda.ilsd;

import java.awt.Color;
import java.awt.Font;

import fr.inria.zvtm.widgets.PieMenuFactory;

class Config {

    static final Color BACKGROUND_COLOR = Color.BLACK;
    static final Color CURSOR_COLOR = Color.WHITE;

    static boolean MASTER_ANTIALIASING = true;
    static boolean CLUSTER_ANTIALIASING = true;

    static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    /* PIEMENU */

    static final Font PIEMENU_FONT = DEFAULT_FONT;

    static Color PIEMENU_FILL_COLOR = Color.BLACK;
    static Color PIEMENU_BORDER_COLOR = Color.WHITE;
    static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;

    static {
        PieMenuFactory.setItemFillColor(PIEMENU_FILL_COLOR);
        PieMenuFactory.setItemBorderColor(PIEMENU_BORDER_COLOR);
        PieMenuFactory.setSelectedItemFillColor(PIEMENU_INSIDE_COLOR);
        PieMenuFactory.setSelectedItemBorderColor(null);
        PieMenuFactory.setLabelColor(PIEMENU_BORDER_COLOR);
        PieMenuFactory.setFont(PIEMENU_FONT);
        PieMenuFactory.setAngle(0);
    }

    /* ------------ Glyph types ---------- */

    static final String T_MPMI = "mpm";

}
