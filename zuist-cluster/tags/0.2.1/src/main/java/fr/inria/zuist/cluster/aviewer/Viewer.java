/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Viewer.java 2985 2010-02-26 16:11:05Z epietrig $
 */

package fr.inria.zuist.cluster.aviewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.widgets.PieMenu;
import fr.inria.zvtm.widgets.PieMenuFactory;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.RegionListener;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;



/**
 * @author Emmanuel Pietriga
 * @author Romain Primet
 * @author Olivier Chapuis
 */

public class Viewer implements Java2DPainter, RegionListener, LevelListener {
    
    File SCENE_FILE, SCENE_FILE_DIR;
        
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;
    static int VIEW_MAX_H = 768;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Scene Space";
    static final String mnSpaceName = "PieMenu Space";
    static final String ovSpaceName = "Overlay Space";
    VirtualSpace mSpace, ovSpace, cursorSpace;
    Camera mCamera;
    Camera cursorCamera;
    String mCameraAltStr = Messages.ALTITUDE + "0";
    String levelStr = Messages.LEVEL + "0";
    static final String mViewName = "ZUIST Viewer";
    View mView;
    ViewerEventHandler eh;

    SceneManager sm;

    private Vector<Camera> sceneCam;
    private ClusteredView clusteredView;
    private ClusterGeometry withoutBezels;
    private ClusterGeometry withBezels;
    private boolean sceneUnderBezels = true;

    public boolean desktoponly = false; 
    public int numRows, numCols;
    public double[] sceneBounds = null;
    public double sceneWidth = 0, sceneHeight= 0;

    private final boolean standalone;
    
    OverlayManager ovm;
    VWGlassPane gp;
    PieMenu mainPieMenu;


    //Toggle view bezels on/off
    public void toggleClusterView()
    {
	    VirtualSpaceManager.INSTANCE.destroyClusteredView(clusteredView);
	    sceneUnderBezels = !sceneUnderBezels;
	    if(sceneUnderBezels){
		    clusteredView = new ClusteredView(
			    withBezels,
			    numRows-1, //origin (block number)
			    numCols, //use complete
			    numRows, //cluster surface
			    sceneCam);
	    } else {
		    clusteredView = new ClusteredView(
			    withoutBezels,
			    numRows-1, //origin (block number)
			    numCols, //use complete
			    numRows, //cluster surface
			    sceneCam);
	    }
	    VirtualSpaceManager.INSTANCE.addClusteredView(clusteredView);
    }

    public Viewer(ViewerOptions options, File xmlSceneFile)
    {
	    this.standalone = options.standalone;
	    this.desktoponly =  options.desktop;
	    ovm = new OverlayManager(this);
	    initGUI(options);
	    VirtualSpace[]  sceneSpaces = {mSpace};
	    Camera[] sceneCameras = {mCamera};
	    sm = new SceneManager(sceneSpaces, sceneCameras);
	    sm.setRegionListener(this);
	    sm.setLevelListener(this);
	    previousLocations = new Vector();
	    ovm.initConsole();
	    if (xmlSceneFile != null){
		    sm.enableRegionUpdater(false);
		    loadScene(xmlSceneFile);
		    EndAction ea  = new EndAction(){
			    public void execute(Object subject, Animation.Dimension dimension){
				    sm.setUpdateLevel(true);
				    sm.enableRegionUpdater(true);
			    }
		    };
		    getGlobalView(ea);
	    }
	    ovm.toggleConsole();

	    vSmarties vs = new vSmarties(this);
	    new TuioTouchManager(this);
	    //System.out.println("Viewer created");
    }

    private void initGUI(ViewerOptions options)
    {
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("ZuistCluster");
        mSpace = vsm.addVirtualSpace(mSpaceName);
        VirtualSpace mnSpace = vsm.addVirtualSpace(mnSpaceName);
        mCamera = mSpace.addCamera();
	mnSpace.addCamera().setAltitude(10);
        ovSpace = vsm.addVirtualSpace(ovSpaceName);
	ovSpace.addCamera();
        cursorSpace = vsm.addVirtualSpace("cursorSpace");
        cursorCamera = cursorSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(cursorCamera);
	cameras.add(vsm.getVirtualSpace(mnSpaceName).getCamera(0));
	cameras.add(vsm.getVirtualSpace(ovSpaceName).getCamera(0));
        if(options.standalone){
            mView = vsm.addFrameView(
		    cameras, mViewName,
		    (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, 
		    VIEW_W, VIEW_H, false, false, !options.fullscreen, initMenu());
        } else {
		mView = vsm.addPanelView(
			cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW,
			VIEW_W, VIEW_H);
        }
        sceneCam = new Vector<Camera>();
        sceneCam.add(mCamera);
        sceneCam.add(cursorCamera);
	numCols = options.numCols;
	numRows = options.numRows;
        withoutBezels = new ClusterGeometry(
                options.blockWidth,
                options.blockHeight,
                options.numCols,
                options.numRows);
        withBezels = withoutBezels.addBezels(options.mullionWidth,options.mullionHeight);
	clusteredView = 
		new ClusteredView(
			withBezels,
			numRows-1, //origin (block number)
			options.numCols, //use complete
			options.numRows, //cluster surface
			sceneCam);
        clusteredView.setBackgroundColor(Color.GRAY);
        vsm.addClusteredView(clusteredView);
        //wallCursor = new WallCursor(cursorSpace, 8, 120, Color.RED);
        if (options.fullscreen)
	{
		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(
			(JFrame)mView.getFrame());
        }
        else
	{
		mView.setVisible(true);
        }
        updatePanelSize();
	gp = new VWGlassPane(this);
	((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new ViewerEventHandler(this);
        mView.setListener(eh); //, 0);
        //mView.setListener(eh, 1);
        mView.setListener(ovm, 2);
	mCamera.addListener(eh);
        mView.setNotifyCursorMoved(true);
        mView.setBackgroundColor(Color.WHITE);
	mView.setAntialiasing(!options.noaa);
	mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
	mView.getPanel().getComponent().addComponentListener(eh);
	ComponentAdapter ca0 = new ComponentAdapter(){
		public void componentResized(ComponentEvent e){
			updatePanelSize();
		}
	};
	mView.getFrame().addComponentListener(ca0);
    }

    public double getDisplayWidth()
    {
	    Dimension d;
	    if(desktoponly)
	    {
		    d = mView.getPanelSize();
	    }
	    else
	    {
		    d = clusteredView.getSize();
	    }
	    return d.getWidth(); 
    }

    public double getDisplayHeight()
    {
	     Dimension  d;
	    if(desktoponly)
	    {
		    d = mView.getPanelSize();
	    }
	    else
	    {
		    d = clusteredView.getSize();
	    }
	     return d.getHeight(); 
    }

    /* x,y in (X Window) screen coordinate */
    public void directTranslate(double x, double y)
    {
	    double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;

	    Location l = mCamera.getLocation();
	    double newx = l.getX()+a*x;
	    double newy = l.getY()+a*y;
	    
	    mCamera.setLocation(new Location(newx, newy, l.getAltitude()));
    }

    /* x,y in (X Window) screen coordinate */
    public void centredZoom(double f, double x, double y)
    {
	    Location cgl = mCamera.getLocation();
	    double a = (mCamera.focal + mCamera.altitude) / mCamera.focal;

	    //System.out.println("centredZoom: " + a + " " + mCamera.altitude + " " + f);

	    double newz = mCamera.focal * a * f - mCamera.focal;
	    //System.out.println("newz: " + newz);

	    if (newz <= 0.0)
	    {	
		    newz = 0;
		    f = mCamera.focal/ (a * mCamera.focal);
		    // System.out.println("newz is <= 0, fixing f: " + f);
	    }

	    double[] r = windowToViewCoordinate(x, y);

	    double dx = cgl.getX() - r[0];
	    double dy = cgl.getY() - r[1];
	    double newx, newy;
	    newx = cgl.getX() + (f*dx - dx); // *a/(mCamera.altitude+ mCamera.focal));
	    newy = cgl.getY() + (f*dy - dy);
	    mCamera.setLocation(new Location(newx, newy, newz));
    }

    public double[] windowToViewCoordinate(double x, double y)
    {
	Location cgl = mCamera.getLocation();
	double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;
	// 
	double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
	double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
	//
	xx = cgl.getX()+ a*xx;
	yy = cgl.getY()+ a*yy;
	
	double[] r = new double[2];
	r[0] = xx;
	r[1] = yy;

	return r;
    }



    JMenuItem infoMI, consoleMI;

	private JMenuBar initMenu(){
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem reloadMI = new JMenuItem("Reload");
		reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		infoMI = new JMenuItem(Messages.INFO_SHOW);
		infoMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        final JMenuItem bezelsMI = new JMenuItem("Toggle bezels");
		consoleMI = new JMenuItem(Messages.CONSOLE_HIDE);
		consoleMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		final JMenuItem gcMI = new JMenuItem("Run Garbage Collector");
		gcMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		final JMenuItem aboutMI = new JMenuItem("About...");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==openMI){openFile();}
				else if (e.getSource()==reloadMI){reload();}
				else if (e.getSource()==exitMI){exit();}
				else if (e.getSource()==infoMI){toggleMiscInfoDisplay();}
				else if (e.getSource()==bezelsMI){toggleClusterView();}
				else if (e.getSource()==gcMI){gc();}
				else if (e.getSource()==consoleMI){ovm.toggleConsole();}
				else if (e.getSource()==aboutMI){about();}
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
        viewM.add(bezelsMI);
		viewM.add(gcMI);
		viewM.add(consoleMI);
		helpM.add(aboutMI);
		jmb.add(fileM);
		jmb.add(viewM);
		jmb.add(helpM);
		openMI.addActionListener(a0);
		reloadMI.addActionListener(a0);
		exitMI.addActionListener(a0);
		infoMI.addActionListener(a0);
		bezelsMI.addActionListener(a0);
		consoleMI.addActionListener(a0);
		gcMI.addActionListener(a0);
		aboutMI.addActionListener(a0);
		return jmb;
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
			mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, mView, vsm);
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
					openScene(fc.getSelectedFile());
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

	public void openSceneDestroy(String xmlSceneFile)
	{
		// this does not work !!
		mSpace.removeAllGlyphs();
		VirtualSpaceManager.INSTANCE.destroyClusteredView(clusteredView);
		sm.setUpdateLevel(false);
		sm.enableRegionUpdater(false);
		for (int l = 0; l < sm.getLevelCount(); l++)
		{
			sm.destroyRegionsAtLevel(l);
		}
		sm.shutdown();
		sm = null;
		VirtualSpace[]  sceneSpaces = {mSpace};
		Camera[] sceneCameras = {mCamera};
		sm = new SceneManager(sceneSpaces, sceneCameras);
		sm.setRegionListener(this);
		sm.setLevelListener(this);
		if(sceneUnderBezels)
		{
			clusteredView = new ClusteredView(
				withBezels,
				numRows-1, //origin (block number)
				numCols, //use complete
				numRows, //cluster surface
				sceneCam);
		} else {
			clusteredView = new ClusteredView(
				withoutBezels,
				numRows-1, //origin (block number)
				numCols, //use complete
				numRows, //cluster surface
				sceneCam);
		}
		VirtualSpaceManager.INSTANCE.addClusteredView(clusteredView);
		openScene(new File(xmlSceneFile));
	}

	public void openScene(String xmlSceneFile) {
		openScene(new File(xmlSceneFile));
	}

	public void openScene(File xmlSceneFile)
	{
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
		getGlobalView(ea);
	}
	
	void loadScene(File xmlSceneFile){
		try {
			ovm.sayInConsole("Loading "+xmlSceneFile.getCanonicalPath()+"\n");
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
			clusteredView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
		}
		MAX_NB_REQUESTS = sm.getObjectCount() / 100;
		gp.setVisible(false);
		gp.setLabel(VWGlassPane.EMPTY_STRING);

		sceneWidth = 0; sceneHeight= 0;
		sceneBounds = null;
		int l = 0;
		while (sm.getRegionsAtLevel(l) == null){
			l++;
			if (l > sm.getLevelCount()){
				l = -1;
				break;
			}
		}
		if (l > -1){
			rememberLocation(mCamera.getLocation());
			// wnes
			sceneBounds = sm.getLevel(l).getBounds();
			System.out.println(
				"Bounds ("+ l+ ") WNES: " 
				+ sceneBounds[0] +" "+ sceneBounds[1] +" "+  sceneBounds[2] +" "+ sceneBounds[3]);
			sceneWidth = - sceneBounds[0] +  sceneBounds[2];
			sceneHeight = sceneBounds[1]  - sceneBounds[3];
		}

		if (sceneBounds != null)
		{
			mCamera.moveTo((sceneBounds[0] +  sceneBounds[2])/2, (sceneBounds[1]  + sceneBounds[3])/2);
		}
		mCamera.setAltitude(0.0);
	}
    
    /*-------------     Navigation       -------------*/

    void getGlobalView(EndAction ea)
    {
	    // ok on the desktop ...
	    if (sceneBounds != null) {
		    rememberLocation(mCamera.getLocation());
		    if (false)
		    {
			    mCamera.getOwningView().centerOnRegion(
				    mCamera, Viewer.ANIM_MOVE_LENGTH,
				    sceneBounds[0], sceneBounds[1], sceneBounds[2], sceneBounds[3], ea);
		    }
		    else
		    {
			    mCamera.moveTo(
				    (sceneBounds[0] +  sceneBounds[2])/2, (sceneBounds[1] + sceneBounds[3])/2);
			    double fw = (double) sceneWidth / (double) getDisplayWidth();
			    double fh = (double) sceneHeight / (double) getDisplayHeight();
			    double f = fw;
			    //System.out.println("fw: " + fw + ", fh: " + fh);
			    if (fh > fw) f = fh;
			    mCamera.setAltitude(0.0);
			    double a = (mCamera.focal + mCamera.altitude) / mCamera.focal;
			    double newz = mCamera.focal * a * f - mCamera.focal;
			    mCamera.setAltitude(newz);
			    if (ea != null)
			    {
				    ea.execute(null,null);
			    }
		    }
	    }
    }

    /* Higher view */
    void getHigherView() {
	    rememberLocation(mCamera.getLocation());
	    Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
	    Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(
		    Viewer.ANIM_MOVE_LENGTH, mCamera,
		    alt, true, SlowInSlowOutInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(a, false);
    }

    public View getView(){
        return mView;
    }

    /* Higher view */
    void getLowerView(){
		rememberLocation(mCamera.getLocation());
		Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
		Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(
			Viewer.ANIM_MOVE_LENGTH, mCamera,
			alt, true, SlowInSlowOutInterpolator.getInstance(), null);
		vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            double qt = (rb[1]-rb[3])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(
		Viewer.ANIM_MOVE_LENGTH, mCamera,
		trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

	void centerOnObject(String id){
		ovm.sayInConsole("Centering on object "+id+"\n");
		ObjectDescription od = sm.getObject(id);
		if (od != null){
			Glyph g = od.getGlyph();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				mCamera.getOwningView().centerOnGlyph(g, mCamera, Viewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}
	}

	void centerOnRegion(String id){
		ovm.sayInConsole("Centering on region "+id+"\n");
		Region r = sm.getRegion(id);
		if (r != null){
			Glyph g = r.getBounds();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				mCamera.getOwningView().centerOnGlyph(g, mCamera, Viewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}		
	}

	Vector previousLocations;
	static final int MAX_PREV_LOC = 100;
	
	void rememberLocation(){
		rememberLocation(mCamera.getLocation());
	}
    
	void rememberLocation(Location l)
	{
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
	
	void moveBack()
	{		
		if (previousLocations.size()>0)
		{
			Vector animParams = Location.getDifference(
				mSpace.getCamera(0).getLocation(), (Location)previousLocations.lastElement());
			sm.setUpdateLevel(false);
      
			class LevelUpdater implements EndAction {
				public void execute(Object subject, Animation.Dimension dimension){
					sm.setUpdateLevel(true);
				}
			}
			Animation at = 
				vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(
					Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
					(Point2D.Double)animParams.elementAt(1), true,
					SlowInSlowOutInterpolator.getInstance(), null);
			Animation aa = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(
				Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
				(Float)animParams.elementAt(0), true, SlowInSlowOutInterpolator.getInstance(),
				new LevelUpdater());
			vsm.getAnimationManager().startAnimation(at, false);
			vsm.getAnimationManager().startAnimation(aa, false);
			previousLocations.removeElementAt(previousLocations.size()-1);
		}
	}
	
	void altitudeChanged(){
		mCameraAltStr = Messages.ALTITUDE + String.valueOf(mCamera.altitude);
	}
    
    void updatePanelSize(){
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
        g2d.setColor(Viewer.MID_DARK_GRAY);
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

	void gc(){
		System.gc();
		if (SHOW_MISC_INFO){
			vsm.repaint();
		}
    }
    
    public void destroy(){
        mView.destroyView();
        sm.reset();
        sm.shutdown();
    }

    void exit(){
        destroy();
        if(standalone){
            System.exit(0);
        }
    }

    public static void main(String[] args){
        File xmlSceneFile = null;
	
        System.out.println("--help for command line options");

        //new Viewer(true, fs, ogl, aa, xmlSceneFile);
        ViewerOptions options = new ViewerOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }
        if(!(options.arguments.isEmpty())){
            xmlSceneFile = new File(options.arguments.get(0));
        }
        new Viewer(options, xmlSceneFile);
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
    
    Viewer application;
    
    VWGlassPane(Viewer app){
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


