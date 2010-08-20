/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Messages.java 2904 2010-02-11 10:08:00Z epietrig $
 */

package fr.inria.zvtm.demo;

public class Messages {

	static final String EMPTY_STRING = "";

	static final String V = "v";
	static final String VERSION = "0.1.0-SNAPSHOT";
    static final String AUTHORS = "Author: Emmanuel Pietriga";
    static final String APP_NAME = "ZVTM FITS Viewer";
    static final String CREDITS_NAMES = "Based on: ZVTM";
    static final String ABOUT_DEPENDENCIES = "Based upon: ZVTM (http://zvtm.sf.net)";

    static final String H_4_HELP = "--help for command line options";
    
    static final String LOAD_FILE = "Load file";
    
    static final String PROCESSING = "Processing ";
    
    static final String mSpaceName = "fits space";
    static final String aboutSpaceName = "About layer";
    static final String mViewName = "ZVTM Fits Viewer";
    
    protected static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -jar target/zvtm-fits-"+VERSION+".jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}
