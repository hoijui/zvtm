/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.io.File;
import java.io.FilenameFilter;

import fr.inria.zvtm.engine.Utils;

import fr.inria.zuist.engine.Region;

public class Launcher {
    
    static final String VIEWER_TYPE_TILEDIMAGE = "I";
    static final String VIEWER_TYPE_DEBUGGER = "D";
    
    public static void main(String[] args){
        File xmlSceneFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		String viewerType = VIEWER_TYPE_DEBUGGER;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("smooth")){Region.setDefaultTransitions(Region.FADE_IN, Region.FADE_OUT);}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){Launcher.printCmdLineHelp();System.exit(0);}
			}
			else if (args[i].toUpperCase().equals(VIEWER_TYPE_TILEDIMAGE) || args[i].toUpperCase().equals(VIEWER_TYPE_DEBUGGER)){
			    viewerType = args[i];
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){
                    if (f.isDirectory()){
                        // if arg is a directory, take first xml file we find in that directory
                        String[] xmlFiles = f.list(new FilenameFilter(){
                                                public boolean accept(File dir, String name){return name.endsWith(".xml");}
                                            });
                        if (xmlFiles.length > 0){
                            xmlSceneFile = new File(f, xmlFiles[0]);
                        }
                    }
                    else {
                        xmlSceneFile = f;                        
                    }
                }
            }
		}
		if (ogl){
		    System.setProperty("sun.java2d.opengl", "True");
		}
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        if (viewerType.equals(VIEWER_TYPE_TILEDIMAGE)){
            new TiledImageViewer(fs, ogl, aa, xmlSceneFile);            
        }
        else {
            new Viewer(fs, ogl, aa, xmlSceneFile);
        }
    }
    
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -jar target/zuist-engine-X.X.X.jar <zuist_scene_file.xml> [viewer] [options]");    	
		System.out.println("Viewer:\n\tI: tiled image scene");
		System.out.println("\tD: debugger");
		System.out.println("\nOptions:\n\t-fs: fullscreen mode");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
        System.out.println("\t-noaa: no antialiasing");
        System.out.println("\t-smooth: default to smooth transitions between levels when none specified");
    }
    
}
