/*   FILE: Messages.java
 *   DATE OF CREATION:  Mon Oct 23 08:47:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.gnb;

import java.awt.Font;

class Messages {

    static final String ARIAL_UNICODE_FONT_NAME = "Arial Unicode MS";
    static final String DEFAULT_FONT_NAME = "Dialog";
    static final Font CITY_FONT = new Font((Utils.osIsWindows()) ? ARIAL_UNICODE_FONT_NAME : DEFAULT_FONT_NAME, Font.PLAIN, 10);
    static final Font REGION_FONT = new Font(DEFAULT_FONT_NAME, Font.ITALIC, 40);
    static final Font COUNTRY_FONT = new Font(DEFAULT_FONT_NAME, Font.BOLD, 100);

    static final Font SMALL_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 10);
    static final Font TINY_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 9);

    static final String MAIN_VIEW_TITLE = "Geonames Browser";

    static final String LOADING_COUNTRIES = "Loading countries";
    static final String LOADING_REGIONS = "Loading states, provinces, federal subjects";
    static final String LOADING_CITIES = "Loading cities";

    static final String CP_FISHEYE_PANEL_TITLE = "Magnification Lens";
    /* these should be ordered according to the values of GeonamesBrowser.L{2,Inf}_* */
    static final String[] MAG_LENS_NAMES = {"Round/Gaussian", "Round/Linear", "Round/InvCosine",
					    "Square/Linear", "Square/InvCosine", "Square/Manhattan",
					    "Round/Melting", "Square/Melting", "Square/Fading"};

    static final String CP_LAYOUT_PANEL_TITLE = "Default Layout";

    static final String CP_DETAIL_PANEL_TITLE = "Details";

    static final String ADAPT_MAPS_CHECKBOX = "Load high-res map tiles";

}