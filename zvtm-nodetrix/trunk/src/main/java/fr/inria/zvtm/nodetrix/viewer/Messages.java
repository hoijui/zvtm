/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix.viewer;

import java.util.Scanner;

public class Messages {

	static final String EMPTY_STRING = "";

	static String VERSION;

	static {
	    Scanner sc = new Scanner(Messages.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                Messages.VERSION = sc.next();
            }
        }
	}

    static final String AUTHORS = "Author: Emmanuel Pietriga and Benjamin Bach";
    static final String APP_NAME = "ZVTM NodeTrix Visualizer";
    static final String CREDITS_NAMES = "Based on: ZVTM, LinLogLayout";
    static final String ABOUT_DEPENDENCIES = "Based upon: ZVTM (http://zvtm.sf.net), LinLogLayout (http://code.google.com/p/linloglayout/)";

    static final String H_4_HELP = "--help for command line options";
    
    static final String LOAD_FILE = "Load graph file";

    static final String PROCESSING = "Processing ";
    
    static final String mSpaceName = "Main Space";
    static final String aboutSpaceName = "About layer";
    public static final String mViewName = "ZVTM-NodeTrix";
    
    protected static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -jar target/zvtm-nodetrix-0.1.0-SNAPSHOT.jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}
