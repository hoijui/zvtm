/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

import java.util.Vector;
import java.util.HashMap;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.RImage;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.event.RepaintListener;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.SceneBuilder;
import fr.inria.zuist.engine.SceneObserver;
import fr.inria.zuist.engine.ViewSceneObserver;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.event.ProgressListener;

import org.w3c.dom.Document;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Emmanuel Pietriga
 */

public class TiledImageViewer {

    File SCENE_FILE, SCENE_FILE_DIR;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    static Color BACKGROUND_COLOR = Color.BLACK;

    boolean UPDATE_TILES = true;

    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Image Layer";
    static final String aboutSpaceName = "About layer";
    VirtualSpace mSpace, dmSpace, lensSpace, aboutSpace;
    Camera mCamera, ovCamera, dmCamera, lensCamera;
    static final String mViewName = "ZUIST Tiled Image Viewer";
    View mView;
    TIVEventHandler eh;

    SceneManager sm;
    TIVNavigationManager nm;
    Overlay ovm;

    WEGlassPane gp;

    ViewSceneObserver mso;

    public TiledImageViewer(ViewerOptions options){
        ovm = new Overlay(this);
        initGUI(options);
        nm = new TIVNavigationManager(this);
        mView.setJava2DPainter(nm, Java2DPainter.FOREGROUND);
        ovm.init();
        eh.nm = this.nm;
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        // VirtualSpace[]  sceneSpaces = {mSpace};
        // Camera[] sceneCameras = {mCamera};
        mso = new ViewSceneObserver(mView, mCamera, mSpace);
        SceneObserver[] sceneObservers = {mso};
        sm = new SceneManager(sceneObservers, Launcher.parseSceneOptions(options));
        if (options.smooth){
            Region.setDefaultTransitions(Region.FADE_IN, Region.FADE_OUT);
        }
        if (options.zuistScenePath != null){
            loadScene(new File(options.zuistScenePath));
            HashMap sa = sm.getSceneAttributes();
            if (sa.containsKey(SceneBuilder._background)){
                BACKGROUND_COLOR = (Color)sa.get(SceneBuilder._background);
            }
        }
        if (BACKGROUND_COLOR.getRGB() == -1){
            mView.getCursor().setColor(Color.BLACK);
            mView.getCursor().setHintColor(Color.BLACK);
        }
        else {
            mView.getCursor().setColor(Color.WHITE);
            mView.getCursor().setHintColor(Color.WHITE);
        }
        nm.createOverview(sm.getRegionsAtLevel(0)[0]);
        nm.updateOverview();
        nm.initDM();
    }

    void initGUI(ViewerOptions options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        dmSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        lensSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        mCamera = mSpace.addCamera();
        ovCamera = mSpace.addCamera();
        dmCamera = dmSpace.addCamera();
        lensCamera = lensSpace.addCamera();
        aboutSpace = vsm.addVirtualSpace(aboutSpaceName);
        aboutSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(lensCamera);
        cameras.add(aboutSpace.getCamera(0));
        mCamera.stick(lensCamera);
        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, (!options.fullscreen) ? initMenu() : null);
        if (options.fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setLayerVisibility(new boolean[]{true, false, true}, new boolean[]{false, true, false});
        eh = new TIVEventHandler(this);
        mView.setListener(eh, 0);
        mView.setListener(ovm, 2);
        mView.getCursor().getPicker().setListener(eh);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        mView.setAntialiasing(!options.noaa);
        mView.getPanel().getComponent().addComponentListener(eh);
        mCamera.addListener(eh);
        updatePanelSize();
        mView.setActiveLayer(0);
    }

    private JMenuBar initMenu(){
        final JMenuItem openMI = new JMenuItem(Messages.OPEN);
        openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem reloadMI = new JMenuItem(Messages.RELOAD);
        reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem exitMI = new JMenuItem(Messages.EXIT);
        final JCheckBoxMenuItem overviewMI = new JCheckBoxMenuItem(Messages.OVERVIEW, true);
        final JMenuItem aboutMI = new JMenuItem(Messages.ABOUT);
        ActionListener a0 = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (e.getSource()==openMI){openFile();}
                else if (e.getSource()==reloadMI){reload();}
                else if (e.getSource()==exitMI){exit();}
                else if (e.getSource()==overviewMI){nm.showOverview(overviewMI.isSelected());}
                else if (e.getSource()==aboutMI){ovm.showAbout();}
            }
        };
        JMenuBar jmb = new JMenuBar();
        JMenu fileM = new JMenu(Messages.FILE);
        JMenu viewM = new JMenu(Messages.VIEW);
        JMenu helpM = new JMenu(Messages.HELP);
        fileM.add(openMI);
        fileM.add(reloadMI);
        fileM.addSeparator();
        fileM.add(exitMI);
        viewM.add(overviewMI);
        helpM.add(aboutMI);
        jmb.add(fileM);
        jmb.add(viewM);
        jmb.add(helpM);
        openMI.addActionListener(a0);
        reloadMI.addActionListener(a0);
        exitMI.addActionListener(a0);
        overviewMI.addActionListener(a0);
        aboutMI.addActionListener(a0);
        return jmb;
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


    /*-------------  Scene management    -------------*/

    void reset(){
        sm.reset();
        mSpace.removeAllGlyphs();
    }

    void openFile(){
        final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle("Find ZUIST Scene File");
        int returnVal= fc.showOpenDialog(mView.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION){
            final SwingWorker worker = new SwingWorker(){
                public Object construct(){
                    sm.setUpdateLevel(false);
                    sm.enableRegionUpdater(false);
                    reset();
                    loadScene(fc.getSelectedFile());
                    return null;
                }
            };
            worker.start();
        }
    }

    void reload(){
        if (SCENE_FILE==null){return;}
        final SwingWorker worker = new SwingWorker(){
            public Object construct(){
                reset();
                loadScene(SCENE_FILE);
                return null;
            }
        };
        worker.start();
    }

    void loadScene(File xmlSceneFile){
        try {
            mView.setTitle(mViewName + " - " + xmlSceneFile.getCanonicalPath());
        }
        catch (IOException ex){}
        gp.setValue(0);
        gp.setVisible(true);
        SCENE_FILE = xmlSceneFile;
        SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        sm.loadScene(SceneBuilder.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
        HashMap sceneAttributes = sm.getSceneAttributes();
        if (sceneAttributes.containsKey(SceneBuilder._background)){
            mView.setBackgroundColor((Color)sceneAttributes.get(SceneBuilder._background));
        }
        gp.setVisible(false);
        gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        EndAction ea  = new EndAction(){
               public void execute(Object subject, Animation.Dimension dimension){
                   sm.setUpdateLevel(true);
                   sm.enableRegionUpdater(true);
               }
           };
        nm.getGlobalView(ea);
    }

    void updatePanelSize(){
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        if (nm != null && nm.ovPortal != null){
            nm.ovPortal.moveTo(panelWidth-nm.ovPortal.getDimensions().width-1, panelHeight-nm.ovPortal.getDimensions().height-1);
        }
    }

    void toggleUpdateTiles(){
        UPDATE_TILES = !UPDATE_TILES;
        sm.setUpdateLevel(UPDATE_TILES);
    }

    /* ---- Benchmark animation ----*/

    Animation cameraAlt;

    void toggleBenchAnim(){
        if (cameraAlt == null){
            animate(20000);
        }
        else {
            vsm.getAnimationManager().stopAnimation(cameraAlt);
            cameraAlt = null;
        }
    }

    void animate(final double gvAlt){
        cameraAlt = vsm.getAnimationManager().getAnimationFactory().createAnimation(
           5000, Animation.INFINITE, Animation.RepeatBehavior.REVERSE, mCamera, Animation.Dimension.ALTITUDE,
           new DefaultTimingHandler(){
               public void timingEvent(float fraction, Object subject, Animation.Dimension dim){
                   Camera c = (Camera)subject;
                   c.setAltitude(2*Double.valueOf(fraction*gvAlt).doubleValue());
               }
           },
           SlowInSlowOutInterpolator.getInstance()
        );
        vsm.getAnimationManager().startAnimation(cameraAlt, false);
    }

    void gc(){
        System.gc();
    }

    void exit(){
        System.exit(0);
    }

    public static void main(String[] args){
        final ViewerOptions options = new ViewerOptions();
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
        javax.swing.SwingUtilities.invokeLater(
            new Runnable(){
                public void run() {
                    new TiledImageViewer(options);
                }
            }
        );
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

    TiledImageViewer application;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    WEGlassPane(TiledImageViewer app){
        super();
        this.application = app;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }

    public void setValue(int c){
        completion = c;
        prX = application.panelWidth/2-BAR_WIDTH/2;
        prY = application.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

    public void setLabel(String m){
        msg = m;
        msgX = application.panelWidth/2-BAR_WIDTH/2;
        msgY = application.panelHeight/2-BAR_HEIGHT/2 - 10;
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

class Overlay implements ViewListener {

    static final Color SAY_MSG_COLOR = Color.LIGHT_GRAY;
    static final Font SAY_MSG_FONT = new Font("Arial", Font.PLAIN, 24);
    static final int SAY_DURATION = 500;

    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;

    static final String ILDA_LOGO_PATH = "/images/ilda.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";

    TiledImageViewer application;

    boolean showingAbout = false;
    VRectangle fadeAbout;
    VImage ildaLogo, inriaLogo;
    VText[] aboutLines;

    VRectangle fadedRegion;
    VText sayGlyph;

    Overlay(TiledImageViewer app){
        this.application = app;
    }

    void init(){
        fadedRegion = new VRectangle(0, 0, 0, 10, 10, FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
        application.aboutSpace.addGlyph(fadedRegion);
        fadedRegion.setVisible(false);
        sayGlyph = new VText(0, -10, 0, SAY_MSG_COLOR, " ", VText.TEXT_ANCHOR_MIDDLE);
        sayGlyph.setFont(SAY_MSG_FONT);
        application.aboutSpace.addGlyph(sayGlyph);
        sayGlyph.setVisible(false);
    }

    void showAbout(){
        if (!showingAbout){
            fadeAbout = new VRectangle(0, 0, 0, Math.round(application.panelWidth/1.05), Math.round(application.panelHeight/1.5),
                FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[5];
            aboutLines[0] = new VText(0, 150, 0, Color.WHITE, "ZUIST Tiled Image Viewer", VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 110, 0, Color.WHITE, "v"+Messages.VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 0, 0, Color.WHITE, "By Emmanuel Pietriga and Romain Primet", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            RImage.setReflectionHeight(0.7f);
            inriaLogo = new RImage(-150, -70, 0, (new ImageIcon(this.getClass().getResource(INRIA_LOGO_PATH))).getImage(), 1.0f);
            ildaLogo = new RImage(200, -70, 0, (new ImageIcon(this.getClass().getResource(ILDA_LOGO_PATH))).getImage(), 1.0f);
            inriaLogo.setDrawBorder(false);
            ildaLogo.setDrawBorder(false);
            aboutLines[3] = new VText(0, -170, 0, Color.WHITE, "Based on the ZVTM toolkit", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[4] = new VText(0, -200, 0, Color.WHITE, "http://zvtm.sf.net", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            application.aboutSpace.addGlyph(fadeAbout);
            application.aboutSpace.addGlyph(inriaLogo);
            application.aboutSpace.addGlyph(ildaLogo);
            for (int i=0;i<aboutLines.length;i++){
                application.aboutSpace.addGlyph(aboutLines[i]);
            }
            showingAbout = true;
        }
        application.mView.setActiveLayer(2);
    }

    void hideAbout(){
        if (showingAbout){
            showingAbout = false;
            if (ildaLogo != null){
                application.aboutSpace.removeGlyph(ildaLogo);
                ildaLogo = null;
            }
            if (inriaLogo != null){
                application.aboutSpace.removeGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null){
                application.aboutSpace.removeGlyph(fadeAbout);
                fadeAbout = null;
            }
            for (int i=0;i<aboutLines.length;i++){
                if (aboutLines[i] != null){
                    application.aboutSpace.removeGlyph(aboutLines[i]);
                    aboutLines[i] = null;
                }
            }
        }
        application.mView.setActiveLayer(0);
    }

    void say(final String msg){
        final SwingWorker worker = new SwingWorker(){
            public Object construct(){
                showMessage(msg);
                sleep(SAY_DURATION);
                hideMessage();
                return null;
            }
            };
        worker.start();
    }

    void showMessage(String msg){
        fadedRegion.setWidth(application.panelWidth-2);
        fadedRegion.setHeight(100);
        sayGlyph.setText(msg);
        fadedRegion.setVisible(true);
        sayGlyph.setVisible(true);
    }

    void hideMessage(){
        fadedRegion.setVisible(false);
        sayGlyph.setVisible(false);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        hideAbout();
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        hideAbout();
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
        application.exit();
    }
}
