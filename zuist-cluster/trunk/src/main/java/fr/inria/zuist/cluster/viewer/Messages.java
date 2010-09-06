/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Messages.java 2879 2010-02-09 16:19:03Z epietrig $
 */

package fr.inria.zuist.cluster.viewer;

import java.awt.geom.Point2D;

class Messages {
		
	static final String VERSION = "0.3.0-SNAPSHOT";

	static final String PM_ENTRY = "mpmE";

    static final String PM_BACK = "Back";
    static final String PM_GLOBALVIEW = "Global View";
    static final String PM_OPEN = "Open...";
    static final String PM_RELOAD = "Reload";
    
    static final String INFO_HIDE = "Hide Info";
    static final String INFO_SHOW = "Show Info";
    static final String CONSOLE_HIDE = "Hide Console";
    static final String CONSOLE_SHOW = "Show Console";
    
    static final String ALTITUDE = "Altitude: ";
    static final String LEVEL = "Level: ";

    static final String[] mainMenuLabels = {PM_GLOBALVIEW, PM_OPEN, PM_BACK, PM_RELOAD};
    static final Point2D.Double[] mainMenuLabelOffsets = {new Point2D.Double(10, 0), new Point2D.Double(0, 0),
						     new Point2D.Double(-10, 0), new Point2D.Double(0, -10)};

}
