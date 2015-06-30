/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file LICENSE.
 *
 * $Id: $
 */

package fr.inria.ilda.ilsd;

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

import java.util.Vector;
import java.util.HashMap;
import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.PickerVS;
// import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.widgets.PieMenu;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.event.ProgressListener;
import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Emmanuel Pietriga
 */

public class ILSD {

    File SCENE_FILE, SCENE_FILE_DIR;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1280;
    static int VIEW_MAX_H = 1024;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    static final short SLIDE_LAYER = 0;
    static final short ANNOT_LAYER = 1;
    static final short MENU_LAYER = 2;

    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String SLIDES_SPACE_STR = "ZUIST Slides Layer";
    static final String ANNOT_SPACE_STR = "Annotation Layer";
    static final String MENU_SPACE_STR = "Command/Menu Layer";
    static VirtualSpace slideSpace, dSpace, mnSpace;
    Camera mCamera, dCamera, mnCamera;
    static final String MAIN_VIEW_TITLE = "ILSD";
    ClusteredView cv;
    ClusterGeometry cg;

    PickerVS dSpacePicker;
    PickerVS mnSpacePicker;

    View mView;
    MVEventListener eh;
    MenuEventListener meh;

    SceneManager sm;
    Navigation nav;

    WEGlassPane gp;

    public ILSD(ILSDOptions options){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setResolution(80);
        initGUI(options);
        nav = new Navigation(this);
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        gp.setValue(0);
        gp.setVisible(true);
        VirtualSpace[] sceneSpaces = {slideSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras, new HashMap<String,String>(1,1));
        if (options.path_to_zuist_slides != null){
            File xmlSceneFile = new File(options.path_to_zuist_slides);
            loadSlides(xmlSceneFile);
		}
        gp.setVisible(false);
        gp.setLabel(WEGlassPane.EMPTY_STRING);
    }

    void initGUI(ILSDOptions options){
        vsm = VirtualSpaceManager.INSTANCE;
        VirtualSpaceManager.INSTANCE.setMaster("ILSD");
        Config.MASTER_ANTIALIASING = !options.noaa;
        windowLayout();
        slideSpace = vsm.addVirtualSpace(SLIDES_SPACE_STR);
        dSpace = vsm.addVirtualSpace(ANNOT_SPACE_STR);
        mnSpace = vsm.addVirtualSpace(MENU_SPACE_STR);
        mCamera = slideSpace.addCamera();
        dCamera = dSpace.addCamera();
        mnCamera = mnSpace.addCamera();
        Vector cameras = new Vector(3);
        cameras.add(mCamera);
        cameras.add(dCamera);
        cameras.add(mnCamera);
        mCamera.stick(dCamera, true);
        mView = vsm.addFrameView(cameras, MAIN_VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, null);
        if (options.fullscreen &&
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setAntialiasing(Config.MASTER_ANTIALIASING);
        eh = new MVEventListener(this);
        meh = new MenuEventListener(this);
        mCamera.addListener(eh);
        mView.setListener(eh, SLIDE_LAYER);
        mView.setListener(eh, ANNOT_LAYER);
        mView.setListener(meh, MENU_LAYER);
        // mView.getCursor().getPicker().setListener(eh);
        dSpacePicker = new PickerVS();
        dSpace.registerPicker(dSpacePicker);
        dSpacePicker.setListener(eh);
        mnSpacePicker = new PickerVS();
        mnSpace.registerPicker(mnSpacePicker);
        mnSpacePicker.setListener(meh);
        mView.setBackgroundColor(Config.BACKGROUND_COLOR);
        mView.getCursor().setColor(Config.CURSOR_COLOR);
        mView.getCursor().setHintColor(Config.CURSOR_COLOR);
        updatePanelSize();
        mView.getPanel().getComponent().addComponentListener(eh);
        mView.setActiveLayer(ANNOT_LAYER);

        cg = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
        Vector ccameras = new Vector(3);
        ccameras.add(mCamera);
        ccameras.add(dCamera);
        ccameras.add(mnCamera);
        cv = new ClusteredView(cg, options.numRows-1, options.numCols, options.numRows, ccameras);
        vsm.addClusteredView(cv);
        cv.setBackgroundColor(Config.BACKGROUND_COLOR);

    }

    void windowLayout(){
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void updatePanelSize(){
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }

    void loadSlides(File zuistSceneFile){
        sm.enableRegionUpdater(false);
        gp.setLabel("Loading " + zuistSceneFile.getName());
        loadScene(zuistSceneFile, gp);
        EndAction ea  = new EndAction(){
            public void execute(Object subject, Animation.Dimension dimension){
                sm.setUpdateLevel(true);
                sm.enableRegionUpdater(true);
            }
        };
        nav.getGlobalView(ea);
        // eh.cameraMoved(mCamera, null, 0);
    }

    void loadScene(File xmlSceneFile, ProgressListener pl){
        SCENE_FILE = xmlSceneFile;
        SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        sm.loadScene(SceneManager.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, pl);
        HashMap sceneAttributes = sm.getSceneAttributes();
        if (sceneAttributes.containsKey(SceneManager._background)){
            mView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
            // clusteredView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
        }
        // mCamera.setAltitude(0.0f);
    }

    void gc(){
        System.gc();
    }

    void exit(){
        System.exit(0);
    }

    public static void main(String[] args) throws Exception{
        ILSDOptions options = new ILSDOptions();
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
        new ILSD(options);
    }

}

class WEGlassPane extends JComponent implements ProgressListener {

    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    static final String EMPTY_STRING = "";
    String msg = EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;

    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;

    ILSD app;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    WEGlassPane(ILSD app){
        super();
        this.app = app;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }

    public void setValue(int c){
        completion = c;
        prX = app.panelWidth/2-BAR_WIDTH/2;
        prY = app.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

    public void setLabel(String m){
        msg = m;
        msgX = app.panelWidth/2-BAR_WIDTH/2;
        msgY = app.panelHeight/2-BAR_HEIGHT/2 - 10;
        repaint(msgX, msgY-50, 400, 70);
    }

    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (msg != EMPTY_STRING){
            g2.setColor(MSG_COLOR);
            g2.setFont(GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

}
