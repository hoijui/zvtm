/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.layout.jung;

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
    static final String mViewName = "Jung Visualizer";
    
    protected static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -jar target/zvtm-layout-0.3.0-SNAPSHOT.jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}
