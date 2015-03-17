/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.ImageIcon;
import java.awt.geom.Point2D;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.widgets.TranslucentTextArea;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Emmanuel Pietriga
 */

public class SlippyMapViewer implements Java2DPainter {

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    static final Color BACKGROUND_COLOR = Color.WHITE;

    boolean SHOW_MEMORY_USAGE = false;

    boolean UPDATE_MAPS = true;

    static final short ZMAP_LAYER = 0;
    static final short BOUNDARY_LAYER = 1;

    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Map Layer";
    static final String bSpaceName = "Boundary Layer";
    VirtualSpace mSpace, bSpace;
    Camera mCamera, bCamera, ovCamera;
    static final String mViewName = "Slippy Map Viewer";
    View mView;
    ExplorerEventHandler eh;

    SceneManager sm;
    NavigationManager nm;

    boolean antialiasing = false;

    public SlippyMapViewer(SMVOptions options){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setResolution(80);
        nm = new NavigationManager(this);
        initGUI(options);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        initScene();
        // if (xmlSceneFile != null){
        //     System.out.println("Loading ZUIST map "+xmlSceneFile.getName());
        //     gp.setLabel("Loading ZUIST map "+xmlSceneFile.getName());
        //     sm.loadScene(parseXML(xmlSceneFile), xmlSceneFile.getParentFile(), true, gp);
        // }
        // gp.setVisible(false);
        // gp.setLabel(WEGlassPane.EMPTY_STRING);
        EndAction ea  = new EndAction(){
                public void execute(Object subject, Animation.Dimension dimension){
                   sm.setUpdateLevel(true);
                   sm.enableRegionUpdater(true);
                }
           };
        nm.getGlobalView(ea);
        eh.cameraMoved(mCamera, null, 0);
        nm.createOverview();
        nm.updateOverview();
    }

    void initGUI(SMVOptions options){
        windowLayout();
        antialiasing = !options.noaa;
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mCamera = mSpace.addCamera();
        ovCamera = mSpace.addCamera();
        bCamera = bSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(bCamera);
        //mCamera.stick(bCamera, true);
        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, null);
        if (options.fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setAntialiasing(antialiasing);
        eh = new ExplorerEventHandler(this);
        mCamera.addListener(eh);
        mView.setListener(eh, ZMAP_LAYER);
        mView.setListener(eh, BOUNDARY_LAYER);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        mView.getCursor().setColor(Color.BLACK);
        mView.getCursor().setHintColor(Color.WHITE);
        mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
        updatePanelSize();
        mView.setActiveLayer(BOUNDARY_LAYER);
        mView.getPanel().getComponent().addComponentListener(eh);
    }

    void windowLayout(){
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    int getDisplayWidth(){
        return VIEW_W;
    }

    int getDisplayHeight(){
        return VIEW_H;
    }

    int getColumnCount(){
        return 1;
    }

    int getRowCount(){
        return 1;
    }

    /* tells whether antialising was requested at launch time or not */
    boolean isAAEnabled(){
        return antialiasing;
    }

    /*-------------     Navigation       -------------*/

    void initScene(){
        //XXX:TBW
    }

    /*-------------     Navigation       -------------*/

    void altitudeChanged(){}

    void updatePanelSize(){
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        if (nm != null && nm.ovPortal != null){
            nm.ovPortal.moveTo(panelWidth-nm.ovPortal.getDimensions().width-1, panelHeight-nm.ovPortal.getDimensions().height-1);
        }
    }

    void toggleMemoryUsageDisplay(){
        SHOW_MEMORY_USAGE = !SHOW_MEMORY_USAGE;
        vsm.repaint();
    }

    void toggleUpdateMaps(){
        UPDATE_MAPS = !UPDATE_MAPS;
        sm.setUpdateLevel(UPDATE_MAPS);
    }

    void gc(){
        System.gc();
        if (SHOW_MEMORY_USAGE){
            vsm.repaint();
        }
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){}

    void exit(){
        System.exit(0);
    }

    public static void main(String[] args){
        SMVOptions options = new SMVOptions();
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
        new SlippyMapViewer(options);
    }

}
