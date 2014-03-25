/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
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
import java.awt.Cursor;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JPanel;

import java.util.Vector;
import java.util.HashMap;
import java.util.Hashtable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
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
import fr.inria.zvtm.engine.AgileGLCanvasFactory;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ImageDescription;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;

import org.w3c.dom.Document;
import fr.inria.zvtm.engine.portals.CameraPortal;

import fr.inria.zvtm.widgets.PieMenu;
import fr.inria.zvtm.widgets.PieMenuFactory;
import fr.inria.zvtm.engine.Java2DPainter;

/**
 * @author Emmanuel Pietriga
 */

public class TiledImageViewer implements LevelListener {

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
    VirtualSpace mSpace, aboutSpace, orthoSpace;
    VirtualSpace scanSpace;
    VirtualSpace littSpace;
    VirtualSpace menuSpace;
    VirtualSpace lenseSpace;
    Camera mCamera, ovCamera, orthoCamera, menuCamera;
    Camera scanCamera;
    Camera littCamera;
    Camera lenseCamera;
    static final String mViewName = "ZUIST Tiled Image Viewer";
    View mView, orthoView;
    TIVEventHandler eh;
    CameraPortal cp;

    SceneManager sm;
    TIVNavigationManager nm;
    Overlay ovm;

    WEGlassPane gp;
    int layer=0;

    //static int covisualization = 0;
    static int covisualization2=4;
    static int swipe = 1;
    static int alpha_swipe=2;
    static int lenses=3;
    static int routeLens = 5;
    static int none=6;
    Vector <VirtualSpace> swipes = new Vector();
    PieMenu mainPieMenu;

    static int mode=routeLens;
    //Define mode here. 
    //Covisualization2 puts Ortho layer and Scan layer side by side. Purple cursor follows the movement in the active side.
    //In swipe mode change Ortho by Scan typing "k"
    //In alpha_swipe mode update scan translucency typing "j" and "h"
    // In lenses, one click activates the lense and another remove it. To activate the pie menu, right click

    Hashtable <String, Integer> layersIndex  = new Hashtable <String, Integer> ();
    Hashtable <View, VirtualSpace> covisHash = new Hashtable <View, VirtualSpace> ();
    JFrame frame;
    JSplitPane splitPane;
    //Camera for virtualSpace that will be magnified.
    public Camera magnifyCamera;
    public static File pathFile=null;



    public TiledImageViewer(boolean fullscreen, boolean opengl, boolean antialiased, File xmlSceneFile){
        ovm = new Overlay(this);
        initGUI(fullscreen, opengl, antialiased);
        nm = new TIVNavigationManager(this);
        ovm.init();
        eh.nm = this.nm;
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        VirtualSpace[]  sceneLenseSpaces = {mSpace,orthoSpace, littSpace, scanSpace, lenseSpace};
        VirtualSpace[]  sceneSpaces = {mSpace,orthoSpace, littSpace, scanSpace};
        //VirtualSpace[]  sceneSpaces = {mSpace, orthoSpace,scanSpace};
        Camera[] sceneLenseCameras = {mCamera,orthoCamera, littCamera, scanCamera, lenseCamera};
        Camera[] sceneCameras = {mCamera,orthoCamera, littCamera, scanCamera};
        //Camera[] sceneCameras = {mCamera, orthoCamera, scanCamera};
        //mCamera.setZoomFloor(-100);
        if(mode == routeLens)
            sm = new SceneManager(sceneLenseSpaces, sceneLenseCameras);
        else
            sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setLevelListener(this);
		if (xmlSceneFile != null){
			loadScene(xmlSceneFile);
			HashMap sa = sm.getSceneAttributes();
			if (sa.containsKey(SceneManager._background)){
			    BACKGROUND_COLOR = (Color)sa.get(SceneManager._background);
			}
		}
        if (BACKGROUND_COLOR.getRGB() == -1){
            mView.getCursor().setColor(Color.BLACK);
    		mView.getCursor().setHintColor(Color.BLACK);
        }
        else {
            mView.getCursor().setColor(Color.WHITE);
    		mView.getCursor().setHintColor(Color.WHITE);
            if(mode==covisualization2)
            {
                orthoView.getCursor().setColor(Color.WHITE);
                orthoView.getCursor().setHintColor(Color.WHITE);
            }
        }
        if(mode!=covisualization2 && mode!=routeLens)
		  nm.createOverview(sm.getRegionsAtLevel(0)[0]);
        

        nm.loadMode(mode);

        //nm.updateOverview();
        
        
    }

    void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
       
        aboutSpace = vsm.addVirtualSpace(aboutSpaceName);
		aboutSpace.addCamera();
        //scanSpace = vsm.addVirtualSpace("scan");
        scanSpace = vsm.addVirtualSpace("scan");
        scanCamera = scanSpace.addCamera();
        orthoSpace = vsm.addVirtualSpace("Ortho");
        orthoCamera = orthoSpace.addCamera();
        
        //orthoCamera = mSpace.addCamera();
        littSpace = vsm.addVirtualSpace("litt");
        littCamera = littSpace.addCamera();

        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = mSpace.addCamera();
        ovCamera = mSpace.addCamera();

        lenseSpace = vsm.addVirtualSpace("Lense");
        lenseCamera = lenseSpace.addCamera();

        Vector cameras = new Vector();
        Vector camerasOrtho = new Vector();
        cameras.add(mCamera);
        cameras.add(aboutSpace.getCamera(0));
        cameras.add(orthoCamera);
        camerasOrtho.add(orthoCamera);
        cameras.add(scanCamera);
        //if(mode!=0)
        cameras.add(littCamera);
        if(mode==routeLens)
            {cameras.add(lenseCamera);}
        menuSpace = vsm.addVirtualSpace("menu");
        menuCamera=menuSpace.addCamera();
        cameras.add(menuCamera);
           
        layersIndex.put("Ortho",2);
        layersIndex.put("Scan",3);
        if(mode == lenses)
        { layersIndex.put("Littoral",4); }

        nm.contextLayer = Messages.SCAN;
        nm.lenseLayer = Messages.ORTHO;
       
        mCamera.stick(orthoCamera);
        mCamera.stick(scanCamera);
        mCamera.stick(littCamera);
        mCamera.stick(lenseCamera);
        //mCamera.stick(menuCamera);
        if (opengl){
            View.registerViewPanelFactory(AgileGLCanvasFactory.AGILE_GLC_VIEW, new AgileGLCanvasFactory());
            mView = vsm.addFrameView(cameras, mViewName, AgileGLCanvasFactory.AGILE_GLC_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, (!fullscreen) ? initMenu() : null);
        }
        else {
            mView = vsm.addFrameView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, (!fullscreen) ? initMenu() : null);
            //orthoView = vsm.addFrameView(camerasOrtho, "Ortho", View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, (!fullscreen) ? initMenu() : null);
            if(mode==covisualization2)
            {
                orthoView=vsm.addPanelView(camerasOrtho, "Ortho", View.STD_VIEW, VIEW_W, VIEW_H);
                createCovisualization();
            }
        }
        if (fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
            if(mode==covisualization2)
                orthoView.setVisible(true);
        }
        eh = new TIVEventHandler(this);
        mView.setListener(eh, 0);
        mView.setListener(ovm, 1);
        mView.setBackgroundColor(BACKGROUND_COLOR);
		mView.setAntialiasing(antialiased);
        mView.getPanel().getComponent().addComponentListener(eh);
        mCamera.addListener(eh);
        mView.setListener(eh,menuCamera);
        if(mode==covisualization2)
        {
            orthoView.setListener(eh,orthoCamera);
            orthoView.getPanel().getComponent().addComponentListener(eh);
            //orthoView.setAntialiasing(antialiased);
            orthoCamera.addListener(eh);
        }
        updatePanelSize();
        mView.setActiveLayer(0);
        //orthoView.setActiveLayer(orthoCamera);

        //Setting lens and context cameras.
        if(mode==lenses)
        { mView.setLayerVisibility(new boolean[]{true,true,false,true,false,true}, new boolean[]{false, false, true,false,false,false}); }
        if(mode==swipe || mode==alpha_swipe)
        {
            mView.setLayerVisibility(new boolean[]{true,true,true,true,false,false}, new boolean[]{false, false, true,false,false,false});
        }
        if(mode==covisualization2)
        { mView.setLayerVisibility(new boolean[]{true,true,false,true,false,false}, new boolean[]{false, false, true,false,false,false}); }
        if(mode==routeLens)
            { mView.setLayerVisibility(new boolean[]{true,true,false,true,false,false,false}, new boolean[]{false,false,false,false,false,true,false}); }
        //mCamera.unstick(orthoCamera);
        //scanCamera.move(VIEW_W/2,0);
        if(mode==swipe)
        {
            swipes.add(scanSpace);
            swipes.add(orthoSpace);
        }

        if(mode==covisualization2)
        { ((JFrame)mView.getFrame()).setVisible(false); }
        
    }

    //For covisualization mode. Creates two views, one for Orthoimages and the other for Scan. They are synchronized.
    void createCovisualization()
    {
        frame = new JFrame(mViewName);
        //frame=((JFrame)mView.getFrame());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


       splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, (JPanel)mView.getPanel().getComponent(), (JPanel)orthoView.getPanel().getComponent());
       
        //splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(VIEW_W/2);
 
        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        ((JPanel)orthoView.getPanel().getComponent()).setMinimumSize(minimumSize);
        ((JPanel)mView.getPanel().getComponent()).setMinimumSize(minimumSize);
 
        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(VIEW_W, VIEW_H));
        splitPane.setResizeWeight(0.5);
        frame.add(splitPane);
        frame.pack();
        frame.setVisible(true);
        covisHash.put(mView, scanSpace);
        covisHash.put(orthoView, orthoSpace);

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
	    sm.loadScene(SceneManager.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
	    HashMap sceneAttributes = sm.getSceneAttributes();
	    if (sceneAttributes.containsKey(SceneManager._background)){
	        mView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
	    }
	    gp.setVisible(false);
	    gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        //orthoCamera.setAltitude(0.0f);
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


    //Creates the regions for the lens' space when magnifying. Only in mode = routeLens
    public void loadLenseSpace()
    {
        short[] transitions = {Region.APPEAR,Region.DISAPPEAR,Region.APPEAR,Region.DISAPPEAR};
        Vector <Integer> levels = new Vector <Integer>();
        for (int i=0; i<sm.getLevelCount(); i++)
            if(sm.getRegionsAtLevel(i).length>0)
                levels.add(i);
        int min=(Integer)Collections.min(levels);
        int max = (Integer)Collections.max(levels);
        for (Region r: sm.regionsAtLayer(magnifyCamera))
        {
            if(r.getLowestLevel()>min)
            {
                int nl=r.getLowestLevel()-1;
                if(r.getLowestLevel()==max)
                {
                    Region nr = sm.createRegion(r.getX(), r.getY(), r.getWidth(), r.getHeight(), max, max,r.getID()+max+"-lense", r.getTitle(), sm.getLayerIndex(lenseCamera),transitions,Region.ORDERING_DISTANCE,true,null,null);
                    for (ObjectDescription o: r.getObjectsInRegion())
                    {
                        ImageDescription img;
                        try {
                            img=(ImageDescription)o;
                            sm.createImageDescription(img.getX(),img.getY(),nr.getWidth(),nr.getHeight(),img.getID()+max+"-lense",img.getZindex(),nr,img.getURL(),o.isSensitive(),null,1.0f,"");
                        }
                        catch(Exception e) {System.out.println("No glyphs added to lenseSpace");}
                    }
                }  
                Region nr = sm.createRegion(r.getX(), r.getY(), r.getWidth(), r.getHeight(), nl, nl,r.getID()+nl+"-lense", r.getTitle(), sm.getLayerIndex(lenseCamera),transitions,Region.ORDERING_DISTANCE,true,null,null);
                for (ObjectDescription o: r.getObjectsInRegion())
                {
                    ImageDescription img;
                    try {
                        img=(ImageDescription)o;
                        sm.createImageDescription(img.getX(),img.getY(),nr.getWidth(),nr.getHeight(),img.getID()+nl+"-lense",img.getZindex(),nr,img.getURL(),o.isSensitive(),null,1.0f,"");
                    }
                    catch(Exception e) {System.out.println("No glyphs added to lenseSpace");}
                }
            }
        }
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
        File xmlSceneFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
        File pathFile=null;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("smooth")){Region.setDefaultTransitions(Region.FADE_IN, Region.FADE_OUT);}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){TiledImageViewer.printCmdLineHelp();System.exit(0);}
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
		//if (ogl){
		//    System.setProperty("sun.java2d.opengl", "True");
		//}
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        new TiledImageViewer(fs, ogl, aa, xmlSceneFile);
    }

    private static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -cp target/timingframework-1.0.jar:zuist-engine-0.2.0-SNAPSHOT.jar:target/:target/:target/zvtm-0.10.0-SNAPSHOT.jar <path_to_scene_dir> [-fs] [-opengl]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
        System.out.println("\t-smooth: default to smooth transitions between levels when none specified");
    }


    public void enteredLevel(int depth){
    System.out.println("Entered level "+depth+"\n");
    //levelStr = Messages.LEVEL + String.valueOf(depth);
    }

    public void exitedLevel(int depth){
       System.out.println("Exited level "+depth+"\n");
    }

    void exchangeLayers()
    {
        if(layer==0)
        {
            mView.setLayerVisibility(new boolean[]{false,true,true}, new boolean[]{true, true,false});
        }
        if(layer==1)
        {
            mView.setLayerVisibility(new boolean[]{true,true,false}, new boolean[]{false, true,true});
        }
        layer=Math.abs(layer-1);
    }

    //Pie menu for switching representations. Only in mode=lenses.
    /*------ Pie Menu -------*/
    static Color PIEMENU_FILL_COLOR = Color.BLACK;
    static Color PIEMENU_BORDER_COLOR = Color.WHITE;
    static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
    static final Font PIEMENU_FONT = new Font("Dialog", Font.PLAIN, 12);

    void displayMainPieMenu(boolean b){
        if (b){
            PieMenuFactory.setItemFillColor(PIEMENU_FILL_COLOR);
            PieMenuFactory.setItemBorderColor(PIEMENU_BORDER_COLOR);
            PieMenuFactory.setSelectedItemFillColor(PIEMENU_INSIDE_COLOR);
            PieMenuFactory.setSelectedItemBorderColor(null);
            PieMenuFactory.setLabelColor(PIEMENU_BORDER_COLOR);
            PieMenuFactory.setFont(PIEMENU_FONT);
            PieMenuFactory.setTranslucency(0.7f);
            PieMenuFactory.setSensitivityRadius(0.5);
            PieMenuFactory.setAngle(-Math.PI/2.0);
            PieMenuFactory.setRadius(150);
            //PieMenuFactory.setRingInnerRatio(0.4f);
            ArrayList <String> l = new ArrayList <String> ();
            String [] labels = new String [l.size()];
            if(nm.lens!=null)
            {
                //PieMenuFactory.setRingInnerRatio(0.6f);
                for (String key : layersIndex.keySet())
                {
                    if (key != nm.lenseLayer)
                    {
                        l.add(key);
                    }
                } 
            }
            else
                for (String key : layersIndex.keySet())
                {
                    if (key != nm.contextLayer)
                    {
                        l.add(key);
                    }
                }  
            mainPieMenu = PieMenuFactory.createPieMenu(l.toArray(labels), Messages.layerLabelOffsets, 0, mView, vsm);
            Glyph[] items = mainPieMenu.getItems();
            items[0].setType(Messages.PM_ENTRY);
            items[1].setType(Messages.PM_ENTRY);
            //items[2].setType(Messages.PM_ENTRY);
            //items[3].setType(Messages.PM_ENTRY);
            System.out.println("PIE MENU: TRUE");
        }
        else {
            mainPieMenu.destroy(0);
            mainPieMenu = null;
            System.out.println("PIE MENU: FALSE");
        }
    }

    void pieMenuEvent(Glyph menuItem){
        int index = mainPieMenu.getItemIndex(menuItem);
        if (index != -1){
            String label = mainPieMenu.getLabels()[index].getText();
            if (label == Messages.ORTHO){ nm.changeLayers(label);//moveBack();
                System.out.println(label);}
            else if (label == Messages.LITT){//getGlobalView(null);
            System.out.println(label);
            nm.changeLayers(label);}
            else if (label == Messages.SCAN){//openFile();
            System.out.println(label);
            nm.changeLayers(label);}
            //else if (label == Messages.PM_RELOAD){//reload();
            //System.out.println(label);}           
        }

    }

    void updateLayer(Glyph menuItem){
        int index = mainPieMenu.getItemIndex(menuItem);
        if (index != -1){
            String label = mainPieMenu.getLabels()[index].getText();
            if(nm.lense)
                nm.lenseLayer = label;
            else
                nm.contextLayer = label;         
        }
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

    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";

    TiledImageViewer application;

    boolean showingAbout = false;
    VRectangle fadeAbout;
    VImage insituLogo, inriaLogo;
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
            insituLogo = new RImage(200, -70, 0, (new ImageIcon(this.getClass().getResource(INSITU_LOGO_PATH))).getImage(), 1.0f);
            aboutLines[3] = new VText(0, -170, 0, Color.WHITE, "Based on the ZVTM toolkit", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[4] = new VText(0, -200, 0, Color.WHITE, "http://zvtm.sf.net", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            application.aboutSpace.addGlyph(fadeAbout);
            application.aboutSpace.addGlyph(inriaLogo);
            application.aboutSpace.addGlyph(insituLogo);
			for (int i=0;i<aboutLines.length;i++){
	            application.aboutSpace.addGlyph(aboutLines[i]);
			}
            showingAbout = true;
        }
		application.mView.setActiveLayer(1);
    }

    void hideAbout(){
        if (showingAbout){
            showingAbout = false;
            if (insituLogo != null){
                application.aboutSpace.removeGlyph(insituLogo);
                insituLogo = null;
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



