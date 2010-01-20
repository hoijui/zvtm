/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.layout.jung;

import fr.inria.zvtm.engine.LongPoint;

public class Messages {

	static final String EMPTY_STRING = "";

	static final String V = "v";
	static final String VERSION = "0.3.0-SNAPSHOT";
    static final String AUTHORS = "Author: Emmanuel Pietriga";
    static final String APP_NAME = "Jung Visualizer";
    static final String CREDITS_NAMES = "Based on: ZVTM";
    static final String ABOUT_DEPENDENCIES = "Based upon: ZVTM (http://zvtm.sf.net)";

    static final String H_4_HELP = "--help for command line options";
    
    static final String LOAD_FILE = "Load file";
    
    static final String PROCESSING = "Processing ";
    
    static final String mSpaceName = "Graph Space";
    static final String aboutSpaceName = "About layer";
    static final String menuSpaceName = "Pie Menu";
    static final String mViewName = "Jung Visualizer";
    
    /* pie menu*/
    static final String PM_ENTRY = "mpmE";
    static final String PM_SUBMN = "mpmS";

    static final String PM_LAYOUT = "Layout...";
    static final String PM_FIND = "Find...";
    static final String PM_BACK = "Back";
    static final String PM_GLOBALVIEW = "Global View";

    static final String[] MAIN_MENU_LABELS = {PM_GLOBALVIEW, PM_LAYOUT, PM_BACK, PM_FIND};
    static final LongPoint[] MAIN_MENU_LABEL_OFFSETS = {new LongPoint(5, -3), new LongPoint(0, 0),
						                                new LongPoint(-10, -3), new LongPoint(0, -10)};

    static final String PM_LAYOUT_SPRING = "Spring";
    static final String PM_LAYOUT_CIRCLE = "Circle";
    static final String PM_LAYOUT_KK = "Kam.-Kawai";
    static final String PM_LAYOUT_UPDATE = "Update";
    static final String PM_LAYOUT_ISOM = "ISOM";
    static final String PM_LAYOUT_FR = "Fruch.-Reing.";

    static final String[] LAYOUT_MENU_LABELS = {PM_LAYOUT_CIRCLE, PM_LAYOUT_FR, PM_LAYOUT_SPRING, PM_LAYOUT_KK, PM_LAYOUT_ISOM, PM_LAYOUT_UPDATE};
    static final LongPoint[] LAYOUT_MENU_LABEL_OFFSETS = {new LongPoint(-8, 3), new LongPoint(-8, 2),
						                                  new LongPoint(0, -10), new LongPoint(9, 2),
						                                  new LongPoint(8, 3), new LongPoint(0, 5)};
    /* cmd line help */
    protected static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -jar target/zvtm-layout-"+VERSION+".jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}
