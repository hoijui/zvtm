/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */

package net.claribole.zgrviewer;

import javax.swing.JMenuBar;

import fr.inria.zvtm.engine.View;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class WallZGRViewer extends ZGRViewer {

    public WallZGRViewer(ZGROptions options){
        super(options);
    }

    @Override
    void initGUI(ZGROptions options, boolean viewOnJPanel){
        exitVMonClose = !viewOnJPanel;
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZING);
        Utils.initLookAndFeel();
        JMenuBar jmb = initViewMenu();
        grMngr.createFrameView(grMngr.createZVTMelements(false), options.opengl ? View.OPENGL_VIEW : View.STD_VIEW, jmb);
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_VIEW_CREATED);
        grMngr.parameterizeView(new ZgrvEvtHdlr(this, this.grMngr));
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZED);
    }

    public static void main(String[] args){
        ZGROptions options = new ZGROptions();
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
        System.out.println("--help for command line options");
        new WallZGRViewer(options);
    }

}
