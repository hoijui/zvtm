/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.io.File;
import java.io.FilenameFilter;

import fr.inria.zvtm.engine.Utils;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ResourceDescription;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Launcher {

    static void setHTTPAuthentication(String user, String password){
        if (user != null && password != null){
            ResourceDescription.setHTTPUser(user);
            ResourceDescription.setHTTPPassword(password);
        }
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
        Launcher.setHTTPAuthentication(options.httpUser, options.httpPassword);
        if (options.basic_debugger){
            new Viewer(options);
        }
        else {
            new TiledImageViewer(options);
        }
    }

}
