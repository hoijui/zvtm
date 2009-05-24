/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import com.xerox.VTM.engine.LongPoint;

class Messages {
	
	static final String USAGE = "Usage:\n\tjava -jar target/zuist-engine-X.X.X.jar <zuist_scene_file.xml> [options]\n\n\t-fs displays the viewer full screen";
	
	static final String VERSION = "0.2.0-SNAPSHOT";

    static final String ABOUT_MSG = "ZUIST Viewer " + VERSION + "\n\nA generic visualization tool for ZUIST multi-scale scenes\nhttp://zvtm.sourceforge.net/zuist\n\nWritten by Emmanuel Pietriga\n(INRIA project In Situ)\nemmanuel.pietriga@inria.fr";
    
	static void printCmdLineHelp(){
		System.out.println(USAGE);
	}
	
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
    static final LongPoint[] mainMenuLabelOffsets = {new LongPoint(10, 0), new LongPoint(0, 0),
						     new LongPoint(-10, 0), new LongPoint(0, -10)};

}
