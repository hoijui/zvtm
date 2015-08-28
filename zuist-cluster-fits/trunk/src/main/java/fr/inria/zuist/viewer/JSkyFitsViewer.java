/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FitsViewer.java 5249 2014-12-11 19:33:30Z fdelcampo $
 */

package fr.inria.zuist.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import org.json.JSONObject;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zuist.engine.SceneObserver;
import fr.inria.zuist.engine.ViewSceneObserver;


//import fr.inria.zvtm.engine.LongPoint;
//import fr.inria.zvtm.engine.Utilities;
import java.awt.geom.Point2D;
import fr.inria.zvtm.engine.Utils;

import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VCross;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.JSkyFitsImage;

//import fr.inria.zvtm.glyphs.PieMenu;
//import fr.inria.zvtm.glyphs.PieMenuFactory;
import fr.inria.zvtm.widgets.PieMenu;
import fr.inria.zvtm.widgets.PieMenuFactory;

import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.AnimationManager;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.SceneBuilder;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.engine.RegionPicker;
import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.ProgressListener;
import fr.inria.zuist.od.ObjectDescription;
import fr.inria.zuist.od.JSkyFitsImageDescription;
import fr.inria.zuist.engine.JSkyFitsResourceHandler;
import fr.inria.zuist.engine.TaggedViewSceneObserver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

// filter
import java.awt.image.ImageFilter;

import cl.inria.massda.SmartiesManager;
import cl.inria.massda.TuioEventHandler;

import edu.jhu.pha.sdss.fits.FITSImage; 
import jsky.coords.WCSTransform;
import nom.tam.fits.FitsException;
import fr.inria.zvtm.fits.NomWcsKeywordProvider;

import cl.inria.massda.PythonWCS;
//import cl.inria.massda.SmartiesManager.MyCursor;

import java.util.Observer;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import fr.inria.zvtm.fits.simbad.SimbadCatQuery;
import fr.inria.zvtm.fits.simbad.AstroObject;

import jsky.coords.WorldCoords;

import java.awt.BasicStroke;

/**
 * @author Emmanuel Pietriga, Fernando del Campo
 */

//public class JSkyFitsViewer extends FitsViewer implements Java2DPainter, RegionListener, LevelListener {
public class JSkyFitsViewer implements Java2DPainter, LevelListener { // RegionListener, 
    
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

    public static double SCENE_W = 12000;//11520;//12000;
    public static double SCENE_H = 4500;//4320;//4500;
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    
    static final String mSpaceKsName = "SceneKsSpace";
    static final String mSpaceHName = "SceneHSpace";
    static final String mSpaceJName = "SceneJSpace";
    static final String mSpaceName = "SceneSpace";
    static final String catalogSpaceName = "CatalogSpace";
    static final String pMnSpaceName = "PieMenuSpace";
    static final String mnSpaceName = "MenuSpace";
    static final String cursorSpaceName = "CursorSpace";
    static final String ovSpaceName = "OverlaySpace";

    
    static final int LAYER_SCENE_KS = 0;
    static final int LAYER_SCENE_H = 1;
    static final int LAYER_SCENE_J = 2;
    static final int LAYER_SCENE = 3;
    static final int LAYER_CATALOG = 4;
    static final int LAYER_PIEMENU = 5;
    static final int LAYER_MENU = 6;
    static final int LAYER_CURSOR = 7;
    static final int LAYER_OVERLAY = 8;


    String tag;

    public static final String[] TAGS = {"mSpaceKs", "mSpaceH", "mSpaceJ"};

    public static final Font FONT_LAYER_SCENE = new Font("Bold", Font.PLAIN, 46);

    //static final int LAYER_CURSOR = 7;

    
    VirtualSpace mSpaceKs, mSpaceH, mSpaceJ;
    public VirtualSpace mSpace;
    VirtualSpace catalogSpace;
    VirtualSpace pMnSpace; //menuSpace;
    public VirtualSpace mnSpace;
    public VirtualSpace cursorSpace;
    VirtualSpace ovSpace;
    
    
    Camera mCameraKs, mCameraH, mCameraJ;
    public Camera mCamera;
    Camera catalogCamera;
    Camera mnCamera; // menuCamera;
    public Camera cursorCamera;

    String mCameraAltStr = Messages.ALTITUDE + "0";
    String levelStr = Messages.LEVEL + "0";
    static final String mViewName = "ZUIST Viewer";
    View mView;

    ClusterGeometry clGeom;
    ClusteredView clView;
    
    JSkyFitsViewerEventHandler eh;

    SceneManager sm;

    FitsOverlayManager ovm;
    //VWGlassPane gp;
    PieMenu mainPieMenu;

    //FitsImage.ColorFilter cfilter = FitsImage.ColorFilter.RAINBOW;
    //FitsImage.ScaleMethod scaleMethod = FitsImage.ScaleMethod.LINEAR;

    //public FitsMenu menu;
    double[] globalScaleParams = {Double.MAX_VALUE, Double.MIN_VALUE};
    
    SmartiesManager smartiesMngr;
    TuioEventHandler teh;

    public RegionPicker rPicker;

    VText wcsLabel;
    VText layerSceneLabel;

    String reference;

    public PythonWCS pythonWCS;

    double[] sceneBounds = null;
    double sceneWidth = 0, sceneHeight= 0;

    AnimationManager am = VirtualSpaceManager.INSTANCE.getAnimationManager();

    //@Override
    String cfilter = "Standard";
    //@Override
    JSkyFitsImage.ScaleAlgorithm scaleMethod = JSkyFitsImage.ScaleAlgorithm.LINEAR;

    //@Override
    public JSkyFitsMenu menu;
    
    //@Override
    public JSkyFitsImageDescription fitsImageDescRef;

    private DrawSymbol draw;
    private Query query;

    

    static HashMap<String,String> parseSceneOptions(Options options){
        HashMap<String,String> res = new HashMap(2,1);
        if (options.httpUser != null){
            res.put(SceneManager.HTTP_AUTH_USER, options.httpUser);
        }
        if (options.httpPassword != null){
            res.put(SceneManager.HTTP_AUTH_PASSWORD, options.httpPassword);
        }
        return res;
    }

    
    public JSkyFitsViewer(Options options){

        ovm = new FitsOverlayManager(this);
        //initGUI(fullscreen, opengl, antialiased);
        initGUI(options);
        VirtualSpace[]  sceneSpaces = {mSpaceKs, mSpaceH, mSpaceJ, mSpace};
        Camera[] sceneCameras = {mCameraKs, mCameraH, mCameraJ, mCamera};

        //sm = new SceneManager(sceneSpaces, sceneCameras, parseSceneOptions(options));
        //sm.setRegionListener(this);
        //sm.setLevelListener(this);

        
        SceneObserver[] observers = new SceneObserver[]{/*new ViewSceneObserver(
            mCameraKs.getOwningView(), mCameraKs, mSpaceKs), new ViewSceneObserver(
            mCameraH.getOwningView(), mCameraH, mSpaceH),new ViewSceneObserver(
            mCameraJ.getOwningView(), mCameraJ, mSpaceJ), */new ViewSceneObserver(
            mCamera.getOwningView(), mCamera, mSpace)/* , new ViewSceneObserver(
            cursorCamera.getOwningView(), cursorCamera, cursorSpace)*/ };
        

        HashMap<String, VirtualSpace> t2s = new HashMap<String, VirtualSpace>(4,1);
        t2s.put("mSpaceKs", mSpaceKs);
        t2s.put("mSpaceH", mSpaceH);
        t2s.put("mSpaceJ", mSpaceJ);
        t2s.put("mSpace", mSpace);
        
        SceneObserver[] so = {new TaggedViewSceneObserver(mCamera.getOwningView(), mCamera, t2s)};

        //sm = new SceneManager(so, new HashMap<String,String>(1,1));

        sm = new SceneManager(observers, new HashMap<String,String>(1,1));
        sm.setResourceHandler(JSkyFitsResourceHandler.RESOURCE_TYPE_FITS,
                              new JSkyFitsResourceHandler());

        pythonWCS = new PythonWCS();

        previousLocations = new Vector();
        ovm.initConsole();
        if (options.xmlSceneFile != null){
            sm.enableRegionUpdater(false);
            File xmlSceneFile = new File(options.xmlSceneFile);
            System.out.println("load scene: " + options.xmlSceneFile);
            openScene(xmlSceneFile);
            /*
            loadScene(xmlSceneFile);
            EndAction ea  = new EndAction(){
                   public void execute(Object subject, Animation.Dimension dimension){
                       sm.setUpdateLevel(true);
                       sm.enableRegionUpdater(true);
                       
                   }
               };
            getGlobalView(ea);
            */
        }

        ovm.toggleConsole();

        //System.out.println("setActiveLayer(LAYER_SCENE)");
        //mView.setActiveLayer(LAYER_SCENE);
        

        //menu.drawHistogram();
        //smartiesMngr = new SmartiesManager(this);
        teh = new TuioEventHandler(this);

        layerSceneLabel = new VText(0d, 0d, 1, Color.RED, "");
        layerSceneLabel.setFont(FONT_LAYER_SCENE);
        layerSceneLabel.setTranslucencyValue(0.9f);

        //super(options);
       
        smartiesMngr = new SmartiesManager(this);

        draw = new DrawSymbol();
        query = new Query();

    }

    void initGUI(Options options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("JSkyFitsViewer");
        
        mSpaceKs = vsm.addVirtualSpace(mSpaceKsName);
        mSpaceH = vsm.addVirtualSpace(mSpaceHName);
        mSpaceJ = vsm.addVirtualSpace(mSpaceJName);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        catalogSpace = vsm.addVirtualSpace(catalogSpaceName);
        pMnSpace = vsm.addVirtualSpace(pMnSpaceName);
        mnSpace = vsm.addVirtualSpace(mnSpaceName);
        cursorSpace = vsm.addVirtualSpace(cursorSpaceName);
        ovSpace = vsm.addVirtualSpace(ovSpaceName);
        
        mCameraKs = mSpaceKs.addCamera();
        mCameraH = mSpaceH.addCamera();
        mCameraJ = mSpaceJ.addCamera();
        mCamera = mSpace.addCamera();
        catalogCamera = catalogSpace.addCamera();
        
        mCamera.stick(mCameraKs);
        mCamera.stick(mCameraH);
        mCamera.stick(mCameraJ);
        mCamera.stick(catalogCamera);
        //mCamera.stick(cursorCamera);

        pMnSpace.addCamera().setAltitude(10);
        mnCamera = mnSpace.addCamera();
        cursorCamera = cursorSpace.addCamera();
        ovSpace.addCamera();

        Vector cameras = new Vector();
        cameras.add(mCameraKs);
        cameras.add(mCameraH);
        cameras.add(mCameraJ);
        cameras.add(mCamera);
        cameras.add(catalogCamera);
        cameras.add(vsm.getVirtualSpace(pMnSpaceName).getCamera(0));
        cameras.add(mnCamera);
        cameras.add(cursorCamera);
        cameras.add(vsm.getVirtualSpace(ovSpaceName).getCamera(0));

        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, initMenu());
        
        if (options.fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();

		//gp = new VWGlassPane(this);
		//((JFrame)mView.getFrame()).setGlassPane(gp);
        //gp.setValue(0);
        //if(options.xmlSceneFile != null) gp.setVisible(true);
        //else gp.setVisible(false);

        eh = new JSkyFitsViewerEventHandler(this);

        menu = new JSkyFitsMenu(this);
        
        /*
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(eh, 1);
        mView.setEventHandler(ovm, 2);
        */

        mView.setListener(eh, LAYER_SCENE_KS);
        mView.setListener(eh, LAYER_SCENE_H);
        mView.setListener(eh, LAYER_SCENE_J);
        mView.setListener(eh, LAYER_SCENE);
        mView.setListener(eh, LAYER_CATALOG);
        mView.setListener(eh, LAYER_PIEMENU);
        mView.setListener(menu, LAYER_MENU);
        mView.setListener(eh, LAYER_CURSOR);
        mView.setListener(ovm, LAYER_OVERLAY);

        mView.getCursor().getPicker().setListener(menu);
        
		//mCamera.addListener(eh);

        //mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.setAntialiasing(!options.noaa);//antialiased);
		mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
		//mView.getPanel().addComponentListener(eh);
		//mView.getPanel().getComponent().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};

        clGeom = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
        
        Vector<Camera> sceneCam = new Vector<Camera>();

        sceneCam.add(mCameraKs);
        sceneCam.add(mCameraH);
        sceneCam.add(mCameraJ);
        sceneCam.add(mCamera);
        sceneCam.add(catalogCamera);
        sceneCam.add(cursorCamera);

        clView = new ClusteredView(clGeom, options.numRows-1, options.numCols, options.numRows, sceneCam);
        clView.setBackgroundColor(Color.GRAY);
        vsm.addClusteredView(clView);


		//mView.setActiveLayer(LAYER_SCENE);
        mView.getFrame().addComponentListener(ca0);
		
    }

    public void setColorFilter(ImageFilter filter){
        //hi.setColorFilter(filter);
    }

    public int getDisplayWidth(){
        return SCREEN_WIDTH;
    }

    public int getDisplayHeight(){
        return SCREEN_HEIGHT;
    }

    JMenuItem infoMI, consoleMI;

    public JMenuBar initMenu(){
        final JMenuItem openMI = new JMenuItem("Open...");
        openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem reloadMI = new JMenuItem("Reload");
        reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem exitMI = new JMenuItem("Exit");
        exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        infoMI = new JMenuItem(Messages.INFO_SHOW);
        infoMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        consoleMI = new JMenuItem(Messages.CONSOLE_HIDE);
        consoleMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        final JMenuItem gcMI = new JMenuItem("Run Garbage Collector");
        gcMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        final JMenuItem aboutMI = new JMenuItem("About...");
        final JMenuItem shortcutMI = new JMenuItem("Shortcut");
        ActionListener a0 = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (e.getSource()==openMI){openFile();}
                else if (e.getSource()==reloadMI){reload();}
                else if (e.getSource()==exitMI){exit();}
                else if (e.getSource()==infoMI){toggleMiscInfoDisplay();}
                else if (e.getSource()==gcMI){gc();}
                else if (e.getSource()==consoleMI){ovm.toggleConsole();}
                else if (e.getSource()==aboutMI){about();}
                else if (e.getSource()==shortcutMI){shortCut();}
            }
        };
        JMenuBar jmb = new JMenuBar();
        JMenu fileM = new JMenu("File");
        JMenu viewM = new JMenu("View");
        JMenu helpM = new JMenu("Help");
        fileM.add(openMI);
        fileM.add(reloadMI);
        fileM.addSeparator();
        fileM.add(exitMI);
        viewM.add(infoMI);
        viewM.add(gcMI);
        viewM.add(consoleMI);
        helpM.add(aboutMI);
        helpM.add(shortcutMI);
        jmb.add(fileM);
        jmb.add(viewM);
        jmb.add(helpM);
        openMI.addActionListener(a0);
        reloadMI.addActionListener(a0);
        exitMI.addActionListener(a0);
        infoMI.addActionListener(a0);
        consoleMI.addActionListener(a0);
        gcMI.addActionListener(a0);
        aboutMI.addActionListener(a0);
        shortcutMI.addActionListener(a0);
        return jmb;
    }

    public String getTagScene(){
        return tag;
    }

    public void setTagScene(String tag){
        this.tag = tag;
        layerSceneLabel.setText(tag);
        //cursorSpace.addGlyph(layerSceneLabel);
        
        cursorSpace.addGlyph(layerSceneLabel);
        //layerSceneLabel.setTranslucencyValue(0f);

        Animation a = am.getAnimationFactory().createTranslucencyAnim(800,
            layerSceneLabel, 0f, false, IdentityInterpolator.getInstance(),
            new EndAction(){
                public void execute(Object subject, Animation.Dimension dimension){

                    cursorSpace.removeGlyph(layerSceneLabel);
                    layerSceneLabel.setTranslucencyValue(0.9f);
                }
            });
        am.startAnimation(a, true);
    }


    public void hideTag(String tag){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                //System.out.println("getLayerIndex: " + ((JSkyFitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
                if( ((JSkyFitsImageDescription)desc).hasTag(tag)){
                    ((JSkyFitsImageDescription)desc).setVisible(false);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.hide(g);
        }*/
    }

    public void showTag(String tag, float alpha){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                //System.out.println("getLayerIndex: " + ((JSkyFitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
                if( ((JSkyFitsImageDescription)desc).hasTag(tag)){
                    ((JSkyFitsImageDescription)desc).setVisible(true);
                    ((JSkyFitsImageDescription)desc).setTranslucencyValue(alpha);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.show(g);
            g.setTranslucencyValue(alpha);
        }
        */
    }

    void displayMainPieMenu(boolean b){
        if (b){
            PieMenuFactory.setItemFillColor(ConfigManager.PIEMENU_FILL_COLOR);
            PieMenuFactory.setItemBorderColor(ConfigManager.PIEMENU_BORDER_COLOR);
            PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
            PieMenuFactory.setSelectedItemBorderColor(null);
            PieMenuFactory.setLabelColor(ConfigManager.PIEMENU_BORDER_COLOR);
            PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
            PieMenuFactory.setTranslucency(0.7f);
            PieMenuFactory.setSensitivityRadius(0.5);
            PieMenuFactory.setAngle(-Math.PI/2.0);
            PieMenuFactory.setRadius(150);
            mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, mView);
            Glyph[] items = mainPieMenu.getItems();
            items[0].setType(Messages.PM_ENTRY);
            items[1].setType(Messages.PM_ENTRY);
            items[2].setType(Messages.PM_ENTRY);
            items[3].setType(Messages.PM_ENTRY);
        }
        else {
            mainPieMenu.destroy(0);
            mainPieMenu = null;
        }
    }

    void pieMenuEvent(Glyph menuItem){
        int index = mainPieMenu.getItemIndex(menuItem);
        if (index != -1){
            String label = mainPieMenu.getLabels()[index].getText();
            if (label == Messages.PM_BACK){moveBack();}
            else if (label == Messages.PM_GLOBALVIEW){getGlobalView(null);}
            else if (label == Messages.PM_OPEN){openFile();}
            else if (label == Messages.PM_RELOAD){reload();}
        }
    }

    void windowLayout(){    
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        } else{
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
                    /*
                    reset();
                    sm.setUpdateLevel(false);
                    sm.enableRegionUpdater(false);
                    loadScene(fc.getSelectedFile());
                    EndAction ea  = new EndAction(){
                           public void execute(Object subject, Animation.Dimension dimension){
                               sm.setUpdateLevel(true);
                               sm.enableRegionUpdater(true);
                               
                           }
                       };
                    getGlobalView(ea);
                    */
                    openScene(fc.getSelectedFile());
                    return null; 
                }
            };
            worker.start();
        }
    }

    void setupSceneBounds()
    {
        int l = 0;
        while (sm.getRegionsAtLevel(l) == null){
            l++;
            if (l > sm.getLevelCount()){
                l = -1;
                break;
            }
        }
        if (l > -1){
            sceneBounds = sm.getLevel(l).getBounds();
            System.out.println(
                "Bounds ("+ l+ ") WNES: "
                + sceneBounds[0] +" "+ sceneBounds[1] +" "+  sceneBounds[2] +" "+ sceneBounds[3]);
            sceneWidth = - sceneBounds[0] +  sceneBounds[2];
            sceneHeight = sceneBounds[1]  - sceneBounds[3];
        }
    }

    public void getGlobalView(EndAction ea){
        if (sceneBounds == null) {return;}

        mCamera.moveTo(
            (sceneBounds[0] +  sceneBounds[2])/2, (sceneBounds[1] + sceneBounds[3])/2);
        double fw = sceneWidth /  getDisplayWidth();
        double fh = sceneHeight / getDisplayHeight();
        double f = fw;
        //System.out.println("fw: " + fw + ", fh: " + fh);
        if (fh > fw) f = fh;
        double a = (mCamera.focal + mCamera.altitude) / mCamera.focal;
        double newz = mCamera.focal * a * f - mCamera.focal;
        mCamera.setAltitude(newz);

        //Location l =  mView.centerOnRegion(zfCamera,0,sceneBounds[0], sceneBounds[1],sceneBounds[2], sceneBounds[3]);
        //zfCamera.setLocation(l);
        if (ea != null) {
            ea.execute(null,null);
        }
    }

    /* Higher view */
    public void getHigherView(){
        rememberLocation(mCamera.getLocation());
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
//        vsm.animator.createCameraAnimation(JSkyFitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(JSkyFitsViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    public void getLowerView(){
        rememberLocation(mCamera.getLocation());
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
//        vsm.animator.createCameraAnimation(FitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(JSkyFitsViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of FitsViewer.MOVE_* */
    public void translateView(short direction){
        //LongPoint trans;
        Point2D.Double trans;
        //long[] rb = mView.getVisibleRegion(mCamera);
        double[] rb = mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            //long qt = Math.round((rb[1]-rb[3])/4.0);
            double qt = (rb[1]-rb[3])/4.0;
            //trans = new LongPoint(0,qt);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            //trans = new LongPoint(0,qt);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            //trans = new LongPoint(qt,0);
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            //trans = new LongPoint(qt,0);
            trans = new Point2D.Double(qt,0);
        }
//        vsm.animator.createCameraAnimation(JSkyFitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(JSkyFitsViewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
        
    }

    /* x,y in (X Window) screen coordinate */
    public void directTranslate(double x, double y){
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        Location l = mCamera.getLocation();
        double newx = l.getX() + a*x;
        double newy = l.getY() + a*y;
        mCamera.setLocation(new Location(newx, newy, l.getAltitude()));
    }

    public void zoomAnimated(double f, EndAction ea){
        Float alt = new Float(f);
        //f.out.println(" f: " + f);
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
            alt, true, SlowInSlowOutInterpolator.getInstance(), ea);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    public void centeredZoom(double f, double x, double y){
        Location l = mCamera.getLocation();
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        double newz = mCamera.focal * a * f - mCamera.focal;
        if (newz < 0){
            newz = 0;
            f = mCamera.focal / (a*mCamera.focal);
        }

        System.out.println("x: " + x + " - y: " + y);

        double xx = (long)((double)x - (SCENE_W/2.0))*a + l.getX();
        double yy = (long)(-(double)y + (SCENE_H/2.0))*a + l.getY();

        System.out.println("xx: " + xx + " - yy: " + yy);

        double dx = l.getX() - xx;
        double dy = l.getY() - yy;

        System.out.println("dx: " + dx + " - dy: " + dy);

        double newx = l.getX() + (f*dx - dx); // *a/(mCamera.altitude+ mCamera.focal));
        double newy = l.getY() + (f*dy - dy);

        System.out.println("newx: " + newx + " - newy: " + newy + " - newz: " + newz);

        mCamera.setLocation(new Location(newx, newy, newz));
    }

    public void traslateAnimated(double x, double y, EndAction ea){
        Location l = mCamera.getLocation();
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        double[] r = windowToViewCoordinate(x, y);
        double dx = l.getX() - r[0];
        double dy = l.getY() - r[1];

        Point2D.Double trans = new Point2D.Double(-dx,-dy);

        Animation ani = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Viewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), ea);
        vsm.getAnimationManager().startAnimation(ani, false);
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

    public void openScene(File xmlSceneFile) {
        reset();
        sm.setUpdateLevel(false);
        sm.enableRegionUpdater(false);
        loadScene(xmlSceneFile);
        EndAction ea  = new EndAction(){
           public void execute(Object subject, Animation.Dimension dimension){
               sm.setUpdateLevel(true);
               sm.enableRegionUpdater(true);
           }
        };
        setupSceneBounds();
        getGlobalView(ea);
        // create a picker that will only consider regions visible at ZUIST levels 3 through 5 (any of these levels or all of them)
        System.out.println("levelCount: " + sm.getLevelCount());
        rPicker = sm.createRegionPicker(0, sm.getLevelCount());
        rPicker.setListener(new RegionPickerListener(this));
    }

    public Point2D.Double viewToSpace(Camera cam, int jpx, int jpy){
        Location camLoc = cam.getLocation();
        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = mView.getPanelSize();

        //find coords of view origin in the virtual space
        double viewOrigX = camLoc.vx - (0.5*viewSize.width*altCoef);
        double viewOrigY = camLoc.vy + (0.5*viewSize.height*altCoef);

        return new Point2D.Double(
                viewOrigX + (altCoef*jpx),
                viewOrigY - (altCoef*jpy));
    }

    void toggleMenu(){
        mnCamera.setEnabled(!mnCamera.isEnabled());
    }

    //@Override
    //public void setColorFilter(JSkyFitsImage.ColorFilter filter){
    public void setColorLookupTable(String filter){
        System.out.println("app.setColorLookupTable(" + filter + ")");
        for(ObjectDescription desc: sm.getObjectDescriptions()){
           // System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){
                ((JSkyFitsImageDescription)desc).setColorLookupTable(filter, true);
            }
        } 
    }

    //@Override
    public void setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm method){
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            //System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){
                ((JSkyFitsImageDescription)desc).setScaleAlgorithm(method, true);
            }
        } 
    }

    //@Override
    public void rescale(double min, double max){
    	//System.out.println("rescale");
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            //System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){
                ((JSkyFitsImageDescription)desc).rescale(min, max, true);
            }
        }
    }

    public void rescaleGlobal(boolean global){
        System.out.println("rescaleGlobal("+global+")");

        boolean globalData = false;
        if(!globalData)
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            //System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){
                if(((JSkyFitsImageDescription)desc).isCreatedWithGlobalData()){
                    globalData = true;
                    //break;
                }
                double[] localScaleParams = ((JSkyFitsImageDescription)desc).getLocalScaleParams();
                if(localScaleParams[0] < globalScaleParams[0]) globalScaleParams[0] = localScaleParams[0];
                if(localScaleParams[1] > globalScaleParams[1]) globalScaleParams[1] = localScaleParams[1];
            }
        }
        if(globalData)
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            //System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){ 
                ((JSkyFitsImageDescription)desc).setRescaleGlobal(globalScaleParams[0], globalScaleParams[1]);
                ((JSkyFitsImageDescription)desc).changeMode(JSkyFitsImageDescription.GLOBAL);
                if(global) ((JSkyFitsImageDescription)desc).rescaleGlobal();
                else ((JSkyFitsImageDescription)desc).rescaleLocal();
            }
        }
        //System.out.println(" min: " + globalScaleParams[0] + " max: " + globalScaleParams[1] );
        /*
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
                if(global) ((FitsImageDescription)desc).rescaleGlobal();
                else ((FitsImageDescription)desc).rescaleLocal();
            }
        }
        */
    }

    public double[] getGlobalScale(){

        if (fitsImageDescRef != null)
            return fitsImageDescRef.getGlobalScaleParams();
        else
            return new double[2];
    }


    /*
    public void orientTo(double angle){
        System.out.println("angle: "+angle);
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).orientTo(angle);
            }
        }
    }
    */


    public double[] windowToViewCoordinate(double x, double y){
        Location l = mCamera.getLocation();
        //System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        //Location lc = cursorCamera.getLocation();
        //System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());

        //
        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        double xx = (long)((double)x - ((double)SCENE_W/2.0));
        double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        xx = l.getX()+ a*xx;
        yy = l.getY()+ a*yy;

        //double xx = l.getX()+ a*x;
        //double yy = l.getY()+ a*y;

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
    }

    public double[] windowToViewCoordinateFromSmarties(double x, double y){
        Location l = mCamera.getLocation();
        //System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        Location lc = cursorCamera.getLocation();
        //System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());

        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        //double xx = (long)((double)x - ((double)SCENE_W/2.0));
        //double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        //xx = l.getX()+ a*xx;
        //yy = l.getY()+ a*yy;

        double xx = l.getX()+ lc.getX() + a*x;//x/a;//a*x;
        double yy = l.getY()+ lc.getY() + a*y;//y/a;//a*y;

        //System.out.println("xx: "+ xx + " - yy: " + yy);

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
    }

    


    public void centerOnObject(String id){
        ovm.sayInConsole("Centering on object "+id+"\n");
        ObjectDescription od = sm.getObject(id);
        if (od != null){
            Glyph g = od.getGlyph();
            if (g != null){
                rememberLocation(mCamera.getLocation());
                mCamera.getOwningView().centerOnGlyph(g, mCamera, JSkyFitsViewer.ANIM_MOVE_LENGTH, true, 1.2f);             
            }
        }
    }

    /*
    public void centerOnRegion(String id){
        ovm.sayInConsole("Centering on region "+id+"\n");
        Region r = sm.getRegion(id);
        if (r != null){
            Glyph g = r.getBounds();
            if (g != null){
                rememberLocation(mCamera.getLocation());
                mCamera.getOwningView().centerOnGlyph(g, mCamera, JSkyFitsViewer.ANIM_MOVE_LENGTH, true, 1.2f);             
            }
        }       
    }
    */

    Vector previousLocations;
    static final int MAX_PREV_LOC = 100;
    
    public void rememberLocation(){
        rememberLocation(mCamera.getLocation());
    }
    
    public void rememberLocation(Location l){
        if (previousLocations.size() >= MAX_PREV_LOC){
            // as a result of release/click being undifferentiated)
            previousLocations.removeElementAt(0);
        }
        if (previousLocations.size()>0){
            if (!Location.equals((Location)previousLocations.lastElement(),l)){
                previousLocations.add(l);
            }
        }
        else {previousLocations.add(l);}
    }
    
    public void moveBack(){
        if (previousLocations.size()>0){
            Vector animParams = Location.getDifference(mSpace.getCamera(0).getLocation(), (Location)previousLocations.lastElement());
            sm.setUpdateLevel(false);
            class LevelUpdater implements EndAction {
                public void execute(Object subject, Animation.Dimension dimension){
                    sm.setUpdateLevel(true);
                }
            }
            Animation at = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(JSkyFitsViewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Point2D.Double)animParams.elementAt(1), true, SlowInSlowOutInterpolator.getInstance(), null);
            Animation aa = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(JSkyFitsViewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Double)animParams.elementAt(0), true, SlowInSlowOutInterpolator.getInstance(), new LevelUpdater());
            vsm.getAnimationManager().startAnimation(at, false);
            vsm.getAnimationManager().startAnimation(aa, false);
            previousLocations.removeElementAt(previousLocations.size()-1);
        }
    }
    
    void altitudeChanged(){
        mCameraAltStr = Messages.ALTITUDE + String.valueOf(mCamera.altitude);
    }
    
    void updatePanelSize(){
        //Dimension d = mView.getPanel().getSize();
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        if (ovm.console != null){
            ovm.updateConsoleBounds();
        }
    }

    /* ---- Debug information ----*/
    
    public void enteredRegion(Region r){
        ovm.sayInConsole("Entered region "+r.getID()+"\n");
    }

    public void exitedRegion(Region r){
        ovm.sayInConsole("Exited region "+r.getID()+"\n");
    }

    public void enteredLevel(int depth){
        ovm.sayInConsole("Entered level "+depth+"\n");
        levelStr = Messages.LEVEL + String.valueOf(depth);
    }

    public void exitedLevel(int depth){
        ovm.sayInConsole("Exited level "+depth+"\n");
    }
    
    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;    
    boolean SHOW_MISC_INFO = true;

    void toggleMiscInfoDisplay(){
        SHOW_MISC_INFO = !SHOW_MISC_INFO;
        if (SHOW_MISC_INFO){
            infoMI.setText(Messages.INFO_HIDE);
        }
        else {
            infoMI.setText(Messages.INFO_SHOW);
        }
        //vsm.repaintNow();
        vsm.repaint();
    }

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    static final Color MID_DARK_GRAY = new Color(64,64,64);

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
        totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
        usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(20,
            3,
            200,
            13);
        g2d.setColor(JSkyFitsViewer.MID_DARK_GRAY);
        g2d.fillRect(20,
            3,
            totalMemRatio * 2,
            13);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(20,
            3,
            usedMemRatio * 2,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(20,
            3,
            200,
            13);
        g2d.drawString(usedMemRatio + "%", 50, 14);
        g2d.drawString(totalMemRatio + "%", 100, 14);
        g2d.drawString(maxMem/1048576 + " Mb", 160, 14);    
    }

    // consider 1000 as the maximum number of requests that can be in the queue at any given time
    // 1000 is the default value ; adapt for each scene depending on the number of objects
    // as this could vary dramatically from one scene to another - see loadScene()
    float MAX_NB_REQUESTS = 1000;
    static final int REQ_QUEUE_BAR_WIDTH = 100;
    static final int REQ_QUEUE_BAR_HEIGHT = 6;
    
    void showReqQueueStatus(Graphics2D g2d, int viewWidth, int viewHeight){
        float ratio = sm.getPendingRequestQueueSize()/(MAX_NB_REQUESTS);
        if (ratio > 1.0f){
            // do not go over gauge boundary, even if actual number of requests goes beyond MAX_NB_REQUESTS
            ratio = 1.0f;
        }
        g2d.setColor(Color.GRAY);
        g2d.fillRect(viewWidth-Math.round(REQ_QUEUE_BAR_WIDTH * ratio)-10, 7, Math.round(REQ_QUEUE_BAR_WIDTH * ratio), REQ_QUEUE_BAR_HEIGHT);
        g2d.drawRect(viewWidth-REQ_QUEUE_BAR_WIDTH-10, 7, REQ_QUEUE_BAR_WIDTH, REQ_QUEUE_BAR_HEIGHT);
    }
    
    void showAltitude(Graphics2D g2d, int viewWidth, int viewHeight){        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(240,
            3,
            190,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(240,
            3,
            190,
            13);
        g2d.drawString(levelStr, 250, 14);
        g2d.drawString(mCameraAltStr, 310, 14);
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!SHOW_MISC_INFO){return;}
        g2d.setComposite(acST);
        showMemoryUsage(g2d, viewWidth, viewHeight);
        showReqQueueStatus(g2d, viewWidth, viewHeight);
        showAltitude(g2d, viewWidth, viewHeight);
        g2d.setComposite(Translucent.acO);
    }

    /* ----- Misc  ------*/
    
    void about(){
        ovm.showAbout();
    }

    void shortCut(){
        ovm.showShortcut();
    }

    void gc(){
        System.gc();
        if (SHOW_MISC_INFO){
            //vsm.repaintNow();
            vsm.repaint();
        }
    }
    
    void exit(){
        System.exit(0);
    }


    
    void loadScene(File xmlSceneFile){
        try {
            ovm.sayInConsole("Loading "+xmlSceneFile.getCanonicalPath()+"\n");
            System.out.println("Loading "+xmlSceneFile.getCanonicalPath()+"\n");
            mView.setTitle(mViewName + " - " + xmlSceneFile.getCanonicalPath());
            System.out.println(mViewName + " - " + xmlSceneFile.getCanonicalPath());    
        }
        catch (IOException ex){}
        //gp.setValue(0);
        //gp.setVisible(true);
        SCENE_FILE = xmlSceneFile;
        SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        System.out.println("dir: "+SCENE_FILE_DIR + " - file: " + SCENE_FILE);
        System.out.println("loadScene...");
        //sm.loadScene(SceneManager.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
        sm.loadScene(SceneBuilder.parseXML(SCENE_FILE), SCENE_FILE_DIR, true);
        HashMap sceneAttributes = sm.getSceneAttributes();
        if (sceneAttributes.containsKey(SceneBuilder._background)){
            mView.setBackgroundColor((Color)sceneAttributes.get(SceneBuilder._background));
            clView.setBackgroundColor((Color)sceneAttributes.get(SceneBuilder._background));
        }


        MAX_NB_REQUESTS = sm.getObjectCount() / 100;
        //gp.setVisible(false);
        //gp.setLabel(VWGlassPane.EMPTY_STRING);
        
        //mCamera.setAltitude(0.0f); XXX

        //loadFitsReference();
    }

    public void coordinateWCS(Point2D.Double xy, String id){

        try {

            double[] coord = windowToViewCoordinateFromSmarties(xy.getX(), xy.getY());
            double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

            /*
            System.out.println("coordinateWCS(" + xy.getX() + ", " + xy.getY() + ")");
            
            System.out.println("windowToViewCoordinateFromSmarties: (" + coord[0] + ", " + coord[1] + ")");
            System.out.println("windowToViewCoordinateFromSmarties Ref: (" + fitsImageDescRef.getX() + ", " + fitsImageDescRef.getY() + ")");

            

            System.out.println("coordinateWCS("+coord[0]+", "+coord[1]+")");
            System.out.println("reference("+fitsImageDescRef.getX()+", "+fitsImageDescRef.getY()+")");
            System.out.println("a: " + a);
            System.out.println("factor: " + fitsImageDescRef.getFactor());

            
            System.out.println( "(" + ((coord[0]-fitsImageDescRef.getX()+fitsImageDescRef.getWidthWithFactor()/2)/a  ) + ", " + (( coord[1]-fitsImageDescRef.getY()+fitsImageDescRef.getHeightWithFactor()/2)/a ) + ")");


            System.out.println( "width: " +fitsImageDescRef.getWidth() + " height:" +fitsImageDescRef.getHeight());

            System.out.println( "width/factor: " +(fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor() ) + " height/a:" + (fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()) ) ;

           

            //double x = (coord[0] - fitsImageDescRef.getX())/fitsImageDescRef.getFactor() + fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor()/2 ;
            //double y = (coord[1] - fitsImageDescRef.getY())/fitsImageDescRef.getFactor() + fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()/2 ;

            */

            if(fitsImageDescRef != null){
                System.out.print("xy: ");
                System.out.print(xy);
                System.out.print(" -- coord: ");
                System.out.print(coord);
                double x = (coord[0] - fitsImageDescRef.getX()) + fitsImageDescRef.getWidth()/2 ;
                double y = (coord[1] - fitsImageDescRef.getY()) + fitsImageDescRef.getHeight()/2 ;

                System.out.println( "(" + x + ", " + y + ")");

                System.out.println("size: " + fitsImageDescRef.getWidthWithFactor() + ", " +  fitsImageDescRef.getHeightWithFactor());

                //pythonWCS.changeCoordinateSystem(galacticalSystem);
                //pythonWCS.sendCoordinate(x, y, mc);
                pythonWCS.sendPix2World(x, y, id);
            }

        } catch (NullPointerException e){
            e.printStackTrace(System.out);
        }

        
    }

    public double[] coordinateTransform(Camera from, Camera to, double x_from, double y_from){

        Location l = to.getLocation();
        double a = (to.focal + to.getAltitude()) / to.focal;

        Location lc = from.getLocation();

        double xx = l.getX()+ lc.getX() + a*x_from;
        double yy = l.getY()+ lc.getY() + a*y_from;

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;

        return r;
    }

    public double[] windowToViewCoordinateFromCoordinateWCS(double x, double y){

        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        Location lc = cursorCamera.getLocation();

        Location l = mCamera.getLocation();

        System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());
        System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        System.out.println("fromCoordinateWCS: " + x + " " + y);
        if(fitsImageDescRef != null){
            System.out.println("fitsReference: " + fitsImageDescRef.getX() + " " + fitsImageDescRef.getY() + " -- " + (fitsImageDescRef.getWidth()/2) + " " + (fitsImageDescRef.getHeight()/2) );
            x = x + fitsImageDescRef.getX() - fitsImageDescRef.getWidth()/2;
            y = y + fitsImageDescRef.getY() - fitsImageDescRef.getHeight()/2;
        }

        

        System.out.println(x + ", " + y);

        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        //double xx = (long)((double)x - ((double)SCENE_W/2.0));
        //double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        //xx = l.getX()+ a*xx;
        //yy = l.getY()+ a*yy;

        double xx = x;//x;//l.getX() + a*x;//x/a;//a*x;
        double yy = y;//y;//l.getY() + a*y;//y/a;//a*y;

        //System.out.println("xx: "+ xx + " - yy: " + yy);

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
    }


    public void loadHistogram(){

        System.out.println("loadHistogram()");

        if(fitsImageDescRef != null){
            try{
                System.out.println(SCENE_FILE_DIR + "/" + fitsImageDescRef.getHistogram());
                File f = new File(SCENE_FILE_DIR + "/" + fitsImageDescRef.getHistogram());
                if (f.exists()){
                    InputStream is = new FileInputStream(f);
                    String jsonTxt = IOUtils.toString(is);
                    //System.out.println(jsonTxt);
                    JSONArray jsonHistogram = new JSONArray(jsonTxt);
                    //System.out.println(json);
                    int len = jsonHistogram.length();
                    System.out.println("len: " + len);
                    int[] data = new int[len];
                    int max = 0;
                    int min = Integer.MAX_VALUE;
                    for(int i = 0; i < len; i++){
                        JSONObject hist = jsonHistogram.getJSONObject(i);
                        System.out.println(hist);
                        data[i] = hist.getInt("count/width");
                        if(data[i] > max) max = data[i];
                        if(data[i] < min) min = data[i];
                    }
                    menu.buildHistogram(new JSkyFitsHistogram(data, min, max));

                }
            } catch (IOException ioe){

            } catch (JSONException je){

            }
            
        }
    }


    public void loadFitsReference(){
        if(reference == null){
            for(ObjectDescription desc: sm.getObjectDescriptions()){
                if(desc instanceof JSkyFitsImageDescription){
                    if( ((JSkyFitsImageDescription)desc).isReference() && ((JSkyFitsImageDescription)desc).hasTag(getTagScene()) ){
                        System.out.println("Reference");
                        System.out.println(desc);

                        fitsImageDescRef = (JSkyFitsImageDescription)desc;

                        loadHistogram();

                        if(pythonWCS != null){
                            System.out.println("pythonWCS.setReference("+fitsImageDescRef.getSrc().getPath()+")");
                            pythonWCS.setReference(fitsImageDescRef.getSrc().getPath());


                        } else {
                            System.out.println("pythonWCS == null. setReference() failed");
                        }

                        /*
                        try{
                            fitsImageDescRef = (FitsImageDescription)desc;
                            //FITSImage fitsImage = new FITSImage( fitsImageDescRef.getSrc() );
                            //wcsKeyProviderRef = new NomWcsKeywordProvider( fitsImage.getFits().getHDU(0).getHeader());
                            //wcsTransformRef = new WCSTransform(wcsKeyProviderRef);

                        } catch(IOException ioe){
                            wcsTransformRef = null;
                        } catch (FitsException fe){
                            wcsTransformRef = null;
                        } catch(FITSImage.NoImageDataFoundException nidfe){
                            wcsTransformRef = null;
                        } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                            wcsTransformRef = null;
                        }
                        */

                        break;
                    }
                }
            }
        } else {

            System.out.println("ref: " + reference);

            fitsImageDescRef = null;
            /*
            try{
                
                
                FITSImage fitsImage = new FITSImage( reference );
                wcsKeyProviderRef = new NomWcsKeywordProvider( fitsImage.getFits().getHDU(0).getHeader());
                wcsTransformRef = new WCSTransform(wcsKeyProviderRef);

            } catch(IOException ioe){
                wcsTransformRef = null;
            } catch (FitsException fe){
                wcsTransformRef = null;
            } catch(FITSImage.NoImageDataFoundException nidfe){
                wcsTransformRef = null;
            } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                wcsTransformRef = null;
            }
            */

        }
        
        
        System.out.println("loaded JSky Fits Reference");
    }

    

    
    class Query implements Observer{
        public static final String T_QUERY = "Query";
        Point2D.Double center;
        Point2D.Double onCircle;
        public Query(){
            pythonWCS.addObserver(this);
        }

        public void callQuery(Point2D.Double center, Point2D.Double onCircle){
            this.center = null;
            this.onCircle = null;
            pythonWCS.sendPix2World(center.x, center.y, T_QUERY+"_CENTER");
            pythonWCS.sendPix2World(onCircle.x, onCircle.y, T_QUERY+"_ONCIRCLE");
        }

        @Override
        public void update(Observable obs, Object obj){
            if(obj instanceof JSONObject){
                try{

                    System.out.println(obj);

                    JSONObject json = (JSONObject) obj;

                    String name = json.getString("name");

                    if(name.equals("pix2world")){

                        String id = json.getString("id");
                        final double ra = json.getDouble("ra");
                        final double dec = json.getDouble("dec");

                        if(id.equals(T_QUERY+"_CENTER")){
                            center = new Point2D.Double(ra, dec);
                        } else if(id.equals(T_QUERY+"_ONCIRCLE")) {
                            onCircle = new Point2D.Double(ra, dec);
                        }

                        if(isDone()){

                            //compute radius in arcmin
                            final WorldCoords wc = new WorldCoords(center.getX(), center.getY());
                            System.out.println("center: ("+center.getX()+", "+center.getY()+")");
                            WorldCoords wcDummy = new WorldCoords(onCircle.getX(), onCircle.getY());
                            System.out.println("onCircle: ("+onCircle.getX()+", "+onCircle.getY()+")");


                            final double distArcMin = wc.dist(wcDummy);
                            //perform catalog query
                            System.err.println("Querying Simbad at " + wc + " with a radius of " + distArcMin + " arcminutes");
                            // symbolSpace.removeAllGlyphs();
                            new SwingWorker(){
                                @Override public List<AstroObject> construct(){
                                    List<AstroObject> objs = null;
                                    try{
                                        //objs = SimbadCatQuery.makeSimbadCoordQuery(wc.getRaDeg(), wc.getDecDeg(), distArcMin);
                                        System.out.println("SimbadCatQuery.makeSimbadCoordQuery("+wc.getRaDeg()+", "+wc.getDecDeg()+", "+distArcMin+")");
                                        objs = SimbadCatQuery.makeSimbadCoordQuery(wc.getRaDeg(), wc.getDecDeg(), distArcMin);
                                    } catch(IOException ioe){
                                        ioe.printStackTrace();
                                    } finally {
                                        return objs;
                                    }
                                }
                                @Override public void finished(){
                                    List<AstroObject> objs = (List<AstroObject>)get();
                                    System.out.println("drawSymbols("+objs+")");
                                    drawSymbols(objs);
                                    eh.fadeOutRightClickSelection();

                                }
                            }.start();

                            center = null;
                            onCircle = null;
                            //pythonWCS.deleteObserver(this);

                        }
                    }

                    System.out.println("updateQuery()");
                } catch(JSONException e){
                    System.out.println(e);
                }
            }
        }

        public boolean isDone(){
            return center != null && onCircle != null;
        }
    }




    void querySimbad(Point2D.Double center, Point2D.Double onCircle){

        System.out.println("center");
        System.out.println(center);
        System.out.println("onCircle");
        System.out.println(onCircle);

        /*
        double x = (center.x); //+ fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
        double y = (center.y);// + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

        System.out.println( "(" + center.x + " - " + fitsImageDescRef.getX() + ") + " + fitsImageDescRef.getWidth() + "/2 = " + x  );
        System.out.println( "(" + center.y + " - " + fitsImageDescRef.getY() + ") + " + fitsImageDescRef.getHeight() + "/2 = " + y );

        center.x = x;
        center.y = y;

        VCross cr = new VCross(x, y, 1000, 200, 200, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        x = (onCircle.x + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
        y = (onCircle.y + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

        System.out.println( "(" + onCircle.x + " - " + fitsImageDescRef.getX() + ") + " + fitsImageDescRef.getWidth() + "/2 = " + x  );
        System.out.println( "(" + onCircle.y + " - " + fitsImageDescRef.getY() + ") + " + fitsImageDescRef.getHeight() + "/2 = " + y );

        onCircle.x = x;
        onCircle.y = y;

        */

        //VCross cr;


        /*
        VCross cr = new VCross(center.x, center.y, 1000, 400, 400, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        cr = new VCross(onCircle.x, onCircle.y, 1000, 400, 400, Color.BLUE, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);


        cr = new VCross(onCircle.x, onCircle.y, 1000, 200, 200, Color.BLUE, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);
        */

        center.x = center.x - fitsImageDescRef.getX() + fitsImageDescRef.getWidth()/2;
        center.y = center.y - fitsImageDescRef.getY() + fitsImageDescRef.getHeight()/2;

        onCircle.x = onCircle.x - fitsImageDescRef.getX() + fitsImageDescRef.getWidth()/2;
        onCircle.y = onCircle.y - fitsImageDescRef.getY() + fitsImageDescRef.getHeight()/2;


        /*
        cr = new VCross(center.x, center.y, 1000, 200, 200, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);


        cr = new VCross(fitsImageDescRef.getX(), fitsImageDescRef.getY(), 1000, fitsImageDescRef.getWidth(), fitsImageDescRef.getHeight(), Color.GRAY, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        cr = new VCross(0, 0, 1000, 1000000, 1000000, Color.BLACK, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        */
        
        query.callQuery(center, onCircle);

        // XXX
        //Point2D.Double centerWCS = coordinateWCS(xy); //new Point2D.Double();//img.vs2wcs(center.x, center.y);
        //Point2D.Double onCircleWCS = new Point2D.Double();//img.vs2wcs(onCircle.x, onCircle.y);

        
    }


    class DrawSymbol implements Observer{

        //String id;
        //AstroObject obj;

        public static final String T_DRAW = "Draw";

        public DrawSymbol(){
            pythonWCS.addObserver(this);
        }

        public void drawSymbol(double ra, double dec, String id){
            pythonWCS.sendWorld2Pix(ra, dec, id);
        }

        /*
        public DrawSymbol(double[] ras, double[] decs, String[] ids){
            pythonWCS.addObserver(this);
            pythonWCS.sendWorld2Pix(ras, decs, ids);
        }
        */

        @Override
        public void update(Observable obs, Object obj){
            if(obj instanceof JSONObject){
                try{

                    System.out.println(obj);

                    JSONObject json = (JSONObject) obj;

                    String name = json.getString("name");
                    

                    if(name.equals("world2pix")){

                        System.out.println("Draw Symbols: update");
                        System.out.println(json);
                        String id = json.getString("id");
                        //double x = (json.getDouble("x") + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
                        //double y = (json.getDouble("y") + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;
                        double[] l = windowToViewCoordinateFromCoordinateWCS(json.getDouble("x"), json.getDouble("y"));
                        System.out.println("location: " + l[0] + ", " + l[1]);
                        VCross cr = new VCross(l[0], l[1], 100, 20, 20, Color.YELLOW, Color.WHITE, .8f);
                        cr.setStroke(AstroObject.AO_STROKE);
                        VText lb = new VText(l[0]+10, l[1]+10, 101, Color.YELLOW, id, VText.TEXT_ANCHOR_START);
                        lb.setBorderColor(Color.BLACK);
                        lb.setTranslucencyValue(.6f);
                        catalogSpace.addGlyph(cr);
                        catalogSpace.addGlyph(lb);
                        


                        //cr.setOwner(this.obj);
                        //lb.setOwner(this.obj);
                        cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                        lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);

                        /*
                        JSONArray arr = json.getJSONArray();

                        for(int i = 0; arr.length(); i++ ){

                            JSONObject iter = arr.getJSONObject(i);
                            String id = iter.getString("id");
                            double x = (iter.getDouble("x") + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
                            double y = (iter.getDouble("y") + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

                            VCross cr = new VCross(x, y, 100, 10, 10, Color.RED, Color.WHITE, .8f);
                            cr.setStroke(AstroObject.AO_STROKE);
                            VText lb = new VText(x+10, y+10, 101, Color.RED, id, VText.TEXT_ANCHOR_START);
                            lb.setBorderColor(Color.BLACK);
                            lb.setTranslucencyValue(.6f);
                            mSpace.addGlyph(cr);
                            mSpace.addGlyph(lb);
                            //cr.setOwner(this.obj);
                            //lb.setOwner(this.obj);
                            cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                            lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                        }
                        */
                        

                        //pythonWCS.deleteObserver(this);

                    }
                }catch(JSONException e){
                    System.out.println(e);
                }
            }
        }
    }

    void drawSymbols(List<AstroObject> objs){

        System.out.println("drawSymbols: size:" + objs.size());

        /*

        double[] ras = new double[objs.size()];
        double[] decs = new double[objs.size()];
        String[] ids = new String[objs.size()];

        AstroObject ao;
        for(int i = 0; i <  objs.size(); i++){
            ao = objs.get(i);
            ras[i] = ao.getRa();
            decs[i] = ao.getDec();
            ids[i] = ao.getIdentifier();
        }

        new DrawSymbol(ras, decs, ids);

        */
        
        for(AstroObject obj: objs){

            System.out.print("AstroObject: ");
            System.out.println(obj);
            
            draw.drawSymbol(obj.getRa(), obj.getDec(), obj.getIdentifier());

            // XXX
            /*
            Point2D.Double p = new Point2D.Double();//img.wcs2vs(obj.getRa(), obj.getDec());
            VCross cr = new VCross(p.x, p.y, 100, 10, 10, Color.RED, Color.WHITE, .8f);
            cr.setStroke(AstroObject.AO_STROKE);
            VText lb = new VText(p.x+10, p.y+10, 101, Color.RED, obj.getIdentifier(), VText.TEXT_ANCHOR_START);
            lb.setBorderColor(Color.BLACK);
            lb.setTranslucencyValue(.6f);
            mSpace.addGlyph(cr);
            mSpace.addGlyph(lb);
            cr.setOwner(obj);
            lb.setOwner(obj);
            cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
            lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);
            */
        }
        
    }


    public static void main(String[] args){

    	Options options = new Options();
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
        new JSkyFitsViewer(options);

    }

    
}




class VWGlassPane extends JComponent implements ProgressListener {
    
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
    
    JSkyFitsViewer application;
    
    VWGlassPane(JSkyFitsViewer app){
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
            g2.setFont(ConfigManager.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class ConfigManager {

	static Color PIEMENU_FILL_COLOR = Color.BLACK;
	static Color PIEMENU_BORDER_COLOR = Color.WHITE;
	static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
	
	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    static final Font PIEMENU_FONT = DEFAULT_FONT;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

}



class RegionPickerListener implements RegionListener{


    JSkyFitsViewer app;

    public RegionPickerListener(JSkyFitsViewer app){
        this.app = app;
    }

    @Override
    public void enteredRegion(Region r){
        //System.out.println("enteredRegion");
        ObjectDescription[] objects = r.getObjectsInRegion();
        for( ObjectDescription desc : objects){
            if(desc instanceof JSkyFitsImageDescription){
                JSkyFitsImageDescription obj = (JSkyFitsImageDescription)desc;
                if( obj.isReference() && obj.hasTag(app.getTagScene())){
                    if( !obj.equals(app.fitsImageDescRef) ){
                        app.fitsImageDescRef = obj;
                        app.loadHistogram();
                        if(app.pythonWCS != null){
                            System.out.println("enteredRegion: " + r.getID());
                            System.out.println("pythonWCS.setReference("+app.fitsImageDescRef.getSrc().getPath()+")");
                            app.pythonWCS.setReference(app.fitsImageDescRef.getSrc().getPath());
                        } else {
                            System.out.println("pythonWCS == null. setReference() failed");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exitedRegion(Region r){
        //System.out.println("exitedRegion");
    }
}
