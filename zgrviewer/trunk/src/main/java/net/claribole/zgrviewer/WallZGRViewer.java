/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */

package net.claribole.zgrviewer;

import javax.swing.JMenuBar;

import java.util.Vector;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class WallZGRViewer extends ZGRViewer {

    ClusterGeometry cg;

    public WallZGRViewer(ZGROptions options){
        super(options);
    }

    @Override
    void initGUI(ZGROptions options, boolean viewOnJPanel){
        VirtualSpaceManager.INSTANCE.setMaster("zgrv");
        exitVMonClose = !viewOnJPanel;
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZING);
        Utils.initLookAndFeel();
        JMenuBar jmb = initViewMenu();
        grMngr.createFrameView(grMngr.createZVTMelements(false), options.opengl ? View.OPENGL_VIEW : View.STD_VIEW, jmb);
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_VIEW_CREATED);
        grMngr.parameterizeView(new ZgrvEvtHdlr(this, this.grMngr));
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZED);
        // wall
        cg = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
        Vector ccameras = new Vector(1);
        ccameras.add(grMngr.mainCamera);
        ClusteredView cv = new ClusteredView(cg, options.numRows-1, options.numCols, options.numRows, ccameras);
        VirtualSpaceManager.INSTANCE.addClusteredView(cv);
        cv.setBackgroundColor(cfgMngr.backgroundColor);
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
