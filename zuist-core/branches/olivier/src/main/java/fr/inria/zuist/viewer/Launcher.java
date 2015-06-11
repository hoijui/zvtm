/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Launcher.java 5393 2015-03-22 20:11:08Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.util.HashMap;

import java.io.File;
import java.io.FilenameFilter;

import fr.inria.zvtm.engine.Utils;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Launcher {

    static HashMap<String,String> parseSceneOptions(ViewerOptions options){
        HashMap<String,String> res = new HashMap(2,1);
        if (options.httpUser != null){
            res.put(SceneManager.HTTP_AUTH_USER, options.httpUser);
        }
        if (options.httpPassword != null){
            res.put(SceneManager.HTTP_AUTH_PASSWORD, options.httpPassword);
        }
        return res;
    }

    public static void main(String[] args){
        ViewerOptions options = new ViewerOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }
        if (!options.fullscreen && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        if (options.debug){
            SceneManager.setDebugMode(true);
        }
        if (options.basic_debugger){
            new Viewer(options);
        }
        else {
            new TiledImageViewer(options);
        }
    }

}
