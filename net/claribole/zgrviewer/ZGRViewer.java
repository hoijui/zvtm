/*   FILE: ZGRViewer.java
 *   DATE OF CREATION:   Thu Jan 09 14:13:31 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id: ZGRViewer.java,v 1.41 2006/06/15 06:54:24 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.glyphs.PieMenu;
import net.claribole.zvtm.glyphs.PieMenuFactory;
import net.claribole.zvtm.lens.*;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.svg.SVGReader;


public class ZGRViewer implements AnimationListener {

    static String zgrvURI="http://zvtm.sourceforge.net/zgrviewer";

    public static VirtualSpaceManager vsm;
    static VirtualSpace mSpace;
    static VirtualSpace mnSpace;
    static VirtualSpace rSpace;
    static String mainSpace = "graphSpace";
    static String menuSpace = "menuSpace";
    /*name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view*/
    static String rdRegionVirtualSpace = "radarSpace";
    /*represents the region seen by main view in the radar view*/
    static VRectangle observedRegion;

    public static View mainView;
    static View rView;
    static String radarView = "Overview";
    Camera mainCamera;

    static ConfigManager cfgMngr;
    static DOTManager dotMngr;
    static TooltipManager tpMngr;

    ZgrvEvtHdlr meh;
    RadarEvtHdlr reh;
    
    static File cmdLineDOTFile=null;
    static String cmdLinePrg=null;

    /*remember previous camera locations so that we can get back*/
    static final int MAX_PREV_LOC=10;
    static Vector previousLocations;

    /*translation constants*/
    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static short MOVE_UP_LEFT=4;
    static short MOVE_UP_RIGHT=5;
    static short MOVE_DOWN_LEFT=6;
    static short MOVE_DOWN_RIGHT=7;

    PieMenu mainPieMenu, subPieMenu;

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    static final float FLOOR_ALTITUDE = -90.0f;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    ZGRViewer(int acc){
	initConfig();
	//init GUI after config as we load some GUI prefs from the config file
	initGUI(acc);
	if (cmdLineDOTFile!=null){loadCmdLineFile();}
    }

    void loadCmdLineFile(){
	if (cmdLinePrg!=null){
	    if (cmdLinePrg.equals("-Pneato")){
		loadFile(cmdLineDOTFile,"neato", false);
	    }
	    else if (cmdLinePrg.equals("-Pdot")){
		loadFile(cmdLineDOTFile,"dot", false);
	    }
	    else if (cmdLinePrg.equals("-Psvg")){
		loadSVG(cmdLineDOTFile);
	    }
	    else {
		System.err.println("Bad option: "+cmdLinePrg);
		System.err.println("Only -Pdot, -Pneato and -Psvg are allowed");
		System.exit(0);
	    }
	}
	else {
	    System.err.println("No flag specifying what program to use (-Pdot, -Pneato or -Psvg), dot will be used");
	    loadFile(cmdLineDOTFile,"dot", false);
	}
    }

    void initConfig(){
	dotMngr=new DOTManager(this);
	cfgMngr=new ConfigManager(this);
	cfgMngr.loadConfig();  //have to test for existence of config file
	cfgMngr.initPlugins();
	previousLocations=new Vector();
    }

    void initGUI(int acc){
	Utils.initLookAndFeel();
	vsm=new VirtualSpaceManager();
	vsm.setMainFont(ConfigManager.defaultFont);
	vsm.setZoomLimit(-90);
	vsm.setMouseInsideGlyphColor(Color.red);
	//vsm.setDebug(true);
	mSpace = vsm.addVirtualSpace(mainSpace);
	mainCamera = vsm.addCamera(mainSpace); //camera 0 for main view
	vsm.addCamera(mainSpace); //camera 1 for radar view
	mnSpace = vsm.addVirtualSpace(menuSpace);
	vsm.addCamera(menuSpace).setAltitude(10);
	rSpace = vsm.addVirtualSpace(rdRegionVirtualSpace);
	vsm.addCamera(rdRegionVirtualSpace);
	RectangleNR seg1;
	RectangleNR seg2;
	ZGRViewer.observedRegion=new VRectangleST(0, 0, 0, 10, 10, ConfigManager.OBSERVED_REGION_COLOR);
	ZGRViewer.observedRegion.setBorderColor(ConfigManager.OBSERVED_REGION_BORDER_COLOR);
	seg1=new RectangleNR(0,0,0,0,500,new Color(115,83,115));  //500 should be sufficient as the radar window is
	seg2=new RectangleNR(0,0,0,500,0,new Color(115,83,115));  //not resizable and is 300x200 (see rdW,rdH below)
	if (!(Utilities.osIsWindows() || Utilities.osIsMacOS())){
	    ZGRViewer.observedRegion.setFill(false);
	}
	ZGRViewer.vsm.addGlyph(ZGRViewer.observedRegion,ZGRViewer.rdRegionVirtualSpace);
	ZGRViewer.vsm.addGlyph(seg1,ZGRViewer.rdRegionVirtualSpace);
	ZGRViewer.vsm.addGlyph(seg2,ZGRViewer.rdRegionVirtualSpace);
	ZGRViewer.vsm.stickToGlyph(seg1,ZGRViewer.observedRegion);
	ZGRViewer.vsm.stickToGlyph(seg2,ZGRViewer.observedRegion);
	ZGRViewer.observedRegion.setSensitivity(false);
	Vector vc1=new Vector();
	vc1.add(vsm.getVirtualSpace(mainSpace).getCamera(0));
	vc1.add(vsm.getVirtualSpace(menuSpace).getCamera(0));
	JMenuBar jmb=initViewMenu(acc);
 	if (acc == 1){
	    mainView=vsm.addExternalView(vc1,ConfigManager.MAIN_TITLE, View.VOLATILE_VIEW, ConfigManager.mainViewW,ConfigManager.mainViewH,true,false,jmb);
	}
	else if (acc == 2){
	    mainView=vsm.addExternalView(vc1,ConfigManager.MAIN_TITLE, View.OPENGL_VIEW, ConfigManager.mainViewW,ConfigManager.mainViewH,true,false,jmb);
	}
 	else {
	    mainView=vsm.addExternalView(vc1,ConfigManager.MAIN_TITLE, View.STD_VIEW, ConfigManager.mainViewW,ConfigManager.mainViewH,true,false,jmb);
	}
	mainView.setLocation(ConfigManager.mainViewX,ConfigManager.mainViewY);
	mainView.setBackgroundColor(ConfigManager.backgroundColor);
	meh=new ZgrvEvtHdlr(this);
	mainView.setEventHandler(meh);
	mainView.getPanel().addComponentListener(meh);
	mainView.setNotifyMouseMoved(true);
	mainView.setJava2DPainter(meh, Java2DPainter.AFTER_DISTORTION);
	vsm.animator.setAnimationListener(this);
	mainView.getFrame().addComponentListener(cfgMngr);
	mainView.setVisible(true);
	setAntialiasing(ConfigManager.ANTIALIASING);
	tpMngr = new TooltipManager(this);
	mainView.getPanel().addMouseMotionListener(tpMngr);
	tpMngr.start();
	mainView.setJava2DPainter(tpMngr, Java2DPainter.FOREGROUND);
	updatePanelSize();
    }

    JMenuBar initViewMenu(int accelerationMode){
 	JMenu open=new JMenu("Open");
        JMenu openD = new JMenu("Open with dot...");
        JMenu openN = new JMenu("Open with neato...");
        final JMenuItem openO = new JMenuItem("Open with...");
	openO.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openDG = new JMenuItem("SVG pipeline (standard)...");
        openDG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openNG = new JMenuItem("SVG pipeline (standard)...");
        openNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openDI = new JMenuItem("DOT pipeline (experimental)...");
        final JMenuItem openNI = new JMenuItem("DOT pipeline (experimental)...");
        final JMenuItem openS=new JMenuItem("Open SVG generated by GraphViz...");
	final JMenuItem reloadI = new JMenuItem("Reload current file");
	reloadI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	final JMenuItem pngI=new JMenuItem("Export to PNG (current view)...");
	final JMenuItem svgI=new JMenuItem("Export to SVG...");
	final JMenuItem printI=new JMenuItem("Print (current view)...");
	final JMenuItem exitI=new JMenuItem("Exit");
	printI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	final JMenuItem backI=new JMenuItem("Back");
	backI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,0));
	final JMenuItem globvI=new JMenuItem("Global View");
	globvI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,0));
	final JMenuItem radarI=new JMenuItem("Overview");
	radarI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	final JMenuItem searchI=new JMenuItem("Find...");
	searchI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	final JMenuItem fontI=new JMenuItem("Set Font...");
	final JMenuItem prefsI=new JMenuItem("Preferences...");
	final JMenuItem helpI=new JMenuItem("Commands...");
	final JMenuItem aboutI=new JMenuItem("About...");
	exitI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (e.getSource()==openDG){open(0, false);}
		    else if (e.getSource()==openNG){open(1, false);}
		    else if (e.getSource()==openDI){open(0, true);}
		    else if (e.getSource()==openNI){open(1, true);}
		    else if (e.getSource()==openS){openSVGFile();}
		    else if (e.getSource()==openO){openOther();}
		    else if (e.getSource()==reloadI){reloadFile();}
		    else if (e.getSource()==globvI){getGlobalView();}
		    else if (e.getSource()==radarI){showRadarView(true);}
		    else if (e.getSource()==searchI){showSearchBox();}
		    else if (e.getSource()==backI){moveBack();}
		    else if (e.getSource()==fontI){assignFontToGraph();}
		    else if (e.getSource()==pngI){savePNG();}
		    else if (e.getSource()==svgI){saveSVG();}
		    else if (e.getSource()==printI){print();}
		    else if (e.getSource()==prefsI){showPreferences();}
		    else if (e.getSource()==exitI){exit();}
		    else if (e.getSource()==helpI){help();}
		    else if (e.getSource()==aboutI){about();}
		}
	    };
	JMenuBar jmb=new JMenuBar();
	JMenu jm1=new JMenu("File");
	JMenu jm2=new JMenu("View");
	JMenu jm3=new JMenu("Help");
	jmb.add(jm1);
	jmb.add(jm2);
	jmb.add(jm3);
	open.add(openD);
	open.add(openN);
	open.add(openO);
	openD.add(openDG);
	openD.add(openDI);
	openN.add(openNG);
	openN.add(openNI);
	open.addSeparator();
	open.add(openS);
 	jm1.add(open);
	jm1.add(reloadI);
	jm1.addSeparator();
	jm1.add(pngI);
	jm1.add(svgI);
	jm1.addSeparator();
	jm1.add(printI);
	jm1.addSeparator();
	jm1.add(exitI);
	jm2.add(backI);
	jm2.add(globvI);
	jm2.add(radarI);
	jm2.addSeparator();
	jm2.add(searchI);
	jm2.addSeparator();
	jm2.add(fontI);
	jm2.addSeparator();
	jm2.add(prefsI);
	jm3.add(helpI);
	jm3.add(aboutI);
	openDG.addActionListener(a0);
	openDI.addActionListener(a0);
	openNG.addActionListener(a0);
	openNI.addActionListener(a0);
	openS.addActionListener(a0);
	openO.addActionListener(a0);
	reloadI.addActionListener(a0);
	pngI.addActionListener(a0);
	svgI.addActionListener(a0);
	printI.addActionListener(a0);
	exitI.addActionListener(a0);
	globvI.addActionListener(a0);
	radarI.addActionListener(a0);
	searchI.addActionListener(a0);
	backI.addActionListener(a0);
	fontI.addActionListener(a0);
	prefsI.addActionListener(a0);
	helpI.addActionListener(a0);
	aboutI.addActionListener(a0);
	if (accelerationMode == 2){printI.setEnabled(false);}
	return jmb;
    }

    void reset(){
	vsm.destroyGlyphsInSpace(mainSpace);
	previousLocations.removeAllElements();
    }

    void open(int dotOrNeato, boolean parser){//0=dot 1=neato, use the integrated parser or not
	if (dotOrNeato==0){
	    if (ConfigManager.checkDot()){
		openDotFile(parser);
	    }
	    else {
		Object[] options={"Yes","No"};
		int option=JOptionPane.showOptionDialog(null,ConfigManager.getDirStatus(),"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
		if (option==JOptionPane.OK_OPTION){
		    openDotFile(parser);
		}
	    }
	}
	else {
	    if (ConfigManager.checkNeato()){
		openNeatoFile(parser);
	    }
	    else {
		Object[] options={"Yes","No"};
		int option=JOptionPane.showOptionDialog(null,ConfigManager.getDirStatus(),"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
		if (option==JOptionPane.OK_OPTION){
		    openNeatoFile(parser);
		}
	    }
	}
    }

    void openDotFile(final boolean parser){
	final JFileChooser fc = new JFileChooser(ConfigManager.m_LastDir!=null ? ConfigManager.m_LastDir : ConfigManager.m_PrjDir);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Find DOT File (dot)");
	int returnVal= fc.showOpenDialog(mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			reset();
			loadFile(fc.getSelectedFile(),"dot", parser);
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void openNeatoFile(final boolean parser){
	final JFileChooser fc = new JFileChooser(ConfigManager.m_LastDir!=null ? ConfigManager.m_LastDir : ConfigManager.m_PrjDir);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Find DOT File (neato)");
	int returnVal= fc.showOpenDialog(mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			reset();
			loadFile(fc.getSelectedFile(),"neato", parser);
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void openSVGFile(){
	final JFileChooser fc = new JFileChooser(ConfigManager.m_LastDir!=null ? ConfigManager.m_LastDir : ConfigManager.m_PrjDir);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Find SVG File");
	int returnVal= fc.showOpenDialog(mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			reset();
			loadSVG(fc.getSelectedFile());
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void openOther(){
	new CallBox(this);
    }

    void loadFile(File f,String prg, boolean parser){//f=DOT file to load, prg=program to use ("dot" or "neato")
	if (f.exists()){
	    ConfigManager.m_LastDir=f.getParentFile();
	    cfgMngr.lastFileOpened = f;
	    cfgMngr.lastProgramUsed = (prg.equals("neato")) ? ConfigManager.NEATO_PROGRAM : ConfigManager.DOT_PROGRAM;
	    dotMngr.load(f,prg, parser);
	    //in case a font was defined in the SVG file, make it the font used here (to show in Prefs)
	    ConfigManager.defaultFont=vsm.getMainFont();
	    mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
	    getGlobalView();
	    if (previousLocations.size()==1){previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	    if (ZGRViewer.rView != null){
		vsm.getGlobalView(mSpace.getCamera(1),100);
		cameraMoved();
	    }
	}
    }

    void loadSVG(File f){
	ProgPanel pp=new ProgPanel("Parsing SVG...","Loading SVG File");
	try {
	    pp.setPBValue(30);
	    cfgMngr.lastFileOpened = f;
	    cfgMngr.lastProgramUsed = ConfigManager.SVG_FILE;
	    Document svgDoc=Utils.parse(f,false);
	    pp.setLabel("Displaying...");
	    pp.setPBValue(80);
	    SVGReader.load(svgDoc,ZGRViewer.vsm,ZGRViewer.mainSpace,true);
	    ConfigManager.defaultFont=vsm.getMainFont();
	    mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
	    getGlobalView();
	    if (previousLocations.size()==1){previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	    if (ZGRViewer.rView != null){
		vsm.getGlobalView(mSpace.getCamera(1),100);
		cameraMoved();
	    }
	    pp.destroy();
	}
	catch (Exception ex){
	    pp.destroy();
	    JOptionPane.showMessageDialog(mainView.getFrame(),Messages.loadError+f.toString());
	}
    }

    void reloadFile(){
        // TODO: support integrated parser during reload
	if (cfgMngr.lastFileOpened != null){
	    reset();
	    switch (cfgMngr.lastProgramUsed){
	    case ConfigManager.NEATO_PROGRAM: {
		this.loadFile(cfgMngr.lastFileOpened, "neato", false);
		break;
	    }
	    case ConfigManager.SVG_FILE: {
		this.loadSVG(cfgMngr.lastFileOpened);
		break;
	    }
	    default: {//ConfigManager.DOT_PROGRAM
		this.loadFile(cfgMngr.lastFileOpened, "dot", false);
		break;
	    }
	    }
	}
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(mSpace.getCamera(0),ConfigManager.ANIM_MOVE_LENGTH);
	rememberLocation(mSpace.getCamera(0).getLocation());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*direction should be one of ZGRViewer.MOVE_* */
    void translateView(short direction){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	LongPoint trans;
	long[] rb=mainView.getVisibleRegion(c);
	if (direction==MOVE_UP){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    long qt=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else if (direction==MOVE_LEFT){
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else if (direction==MOVE_UP_LEFT){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_UP_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_DOWN_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else {//direction==DOWN_LEFT
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
    }

    void rememberLocation(Location l){
	if (previousLocations.size()>=MAX_PREV_LOC){// as a result of release/click being undifferentiated)
	    previousLocations.removeElementAt(0);
	}
	if (previousLocations.size()>0){
	    if (!Location.equals((Location)previousLocations.lastElement(),l)){
		previousLocations.add(l);
	    }
	}
	else {previousLocations.add(l);}
    }

    void moveBack(){
	if (previousLocations.size()>0){
	    Location newlc=(Location)previousLocations.lastElement();
	    Location currentlc = mSpace.getCamera(0).getLocation();
	    Vector animParams=Location.getDifference(currentlc,newlc);
	    vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_TRANS_SIG,
					       animParams, mSpace.getCamera(0).getID());
	    previousLocations.removeElementAt(previousLocations.size()-1);
	}
    }

    void savePNG(){
    	final long[] vr = mainView.getVisibleRegion(mSpace.getCamera(0));
    	SwingWorker sw = new SwingWorker(){
		public 	Object construct(){
		    new PNGExportWindow(vr[2] - vr[0], vr[1]-vr[3]);
		    return null;
		}
	    };
	sw.start();
    }

    void saveSVG(){
	final JFileChooser fc=new JFileChooser(ConfigManager.m_LastExportDir!=null ? ConfigManager.m_LastExportDir : ConfigManager.m_PrjDir);
	fc.setDialogTitle("Export SVG");
	int returnVal=fc.showSaveDialog(mainView.getFrame());
	if (returnVal==JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			exportSVG(fc.getSelectedFile());
			return null;
		    }
		};
	    worker.start();
	}
    }

    /*export the entire RDF graph as SVG locally*/
    public void exportSVG(File f){
	if (f!=null){
	    mainView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	    ConfigManager.m_LastExportDir=f.getParentFile();
	    mainView.setStatusBarText("Exporting to SVG "+f.toString()+" ...");
	    if (f.exists()){f.delete();}
	    com.xerox.VTM.svg.SVGWriter svgw=new com.xerox.VTM.svg.SVGWriter();
	    Document d=svgw.exportVirtualSpace(vsm.getVirtualSpace(mainSpace),new DOMImplementationImpl(),f);
	    Utils.serialize(d,f);
	    mainView.setStatusBarText("Exporting to SVG "+f.toString()+" ...done");
	    mainView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	}
    }

    void print(){
    	final long[] vr = mainView.getVisibleRegion(mSpace.getCamera(0));
    	SwingWorker sw = new SwingWorker(){
		public 	Object construct(){
		    new PrintWindow(vr[2] - vr[0], vr[1]-vr[3]);
		    return null;
		}
	    };
	sw.start();
    }

    /*show/hide radar view*/
    void showRadarView(boolean b){
	if (b){
	    if (ZGRViewer.rView == null){
		Vector cameras = new Vector();
		cameras.add(mSpace.getCamera(1));
		cameras.add(rSpace.getCamera(0));
		vsm.addExternalView(cameras, ZGRViewer.radarView, View.STD_VIEW, ConfigManager.rdW, ConfigManager.rdH, false, true);
		reh = new RadarEvtHdlr(this);
		ZGRViewer.rView = vsm.getView(ZGRViewer.radarView);
		ZGRViewer.rView.setBackgroundColor(ConfigManager.backgroundColor);
		ZGRViewer.rView.setEventHandler(reh);
		ZGRViewer.rView.setResizable(false);
		ZGRViewer.rView.setActiveLayer(1);
		ZGRViewer.rView.setCursorIcon(java.awt.Cursor.MOVE_CURSOR);
		vsm.getGlobalView(mSpace.getCamera(1),100);
		cameraMoved();
	    }
	    else {
		ZGRViewer.rView.toFront();
	    }
	}
    }

    public void cameraMoved(){//interface AnimationListener (com.xerox.VTM.engine)
	if (ZGRViewer.rView!=null){
	    Camera c0=mSpace.getCamera(1);
	    Camera c1=rSpace.getCamera(0);
	    c1.posx=c0.posx;
	    c1.posy=c0.posy;
	    c1.focal=c0.focal;
	    c1.altitude=c0.altitude;
	    long[] wnes=ZGRViewer.mainView.getVisibleRegion(mSpace.getCamera(0));
	    observedRegion.moveTo((wnes[0]+wnes[2])/2,(wnes[3]+wnes[1])/2);
	    observedRegion.setWidth((wnes[2]-wnes[0])/2);
	    observedRegion.setHeight((wnes[1]-wnes[3])/2);
	}
	vsm.repaintNow();
    }

    void displayMainPieMenu(boolean b){
	if (b){
	    PieMenuFactory.setItemFillColor(ConfigManager.OBSERVED_REGION_COLOR);
	    PieMenuFactory.setItemBorderColor(ConfigManager.OBSERVED_REGION_BORDER_COLOR);
	    PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
	    PieMenuFactory.setSelectedItemBorderColor(null);
	    PieMenuFactory.setLabelColor(ConfigManager.OBSERVED_REGION_BORDER_COLOR);
	    PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
	    PieMenuFactory.setTranslucency(0.95f);
	    PieMenuFactory.setSensitivityRadius(0.5);
	    PieMenuFactory.setAngle(-Math.PI/2.0);
	    PieMenuFactory.setRadius(150);
	    mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, mainView, vsm);
	    Glyph[] items = mainPieMenu.getItems();
	    items[0].setType(Messages.PM_ENTRY);
	    items[1].setType(Messages.PM_SUBMN);
	    items[2].setType(Messages.PM_ENTRY);
	    items[3].setType(Messages.PM_SUBMN);
	}
	else {
	    mainPieMenu.destroy(0);
	    mainPieMenu = null;
	}
    }

    void displaySubMenu(Glyph menuItem, boolean b){
	if (b){
	    int index = mainPieMenu.getItemIndex(menuItem);
	    if (index != -1){
		String label = mainPieMenu.getLabels()[index].getText();
		PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
		PieMenuFactory.setItemFillColor(ConfigManager.OBSERVED_REGION_COLOR);
		PieMenuFactory.setItemBorderColor(ConfigManager.OBSERVED_REGION_BORDER_COLOR);
		PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
		PieMenuFactory.setSelectedItemBorderColor(null);
		PieMenuFactory.setSensitivityRadius(1.0);
		PieMenuFactory.setTranslucency(0.95f);
		PieMenuFactory.setRadius(120);
		Glyph[] items;
		if (label == Messages.PM_FILE){
		    subPieMenu = PieMenuFactory.createPieMenu(Messages.fileMenuLabels, Messages.fileMenuLabelOffsets, 0 , mainView, vsm);
		    items = subPieMenu.getItems();
		    for (int i=0;i<items.length;i++){
			items[i].setType(Messages.PM_ENTRY);
		    }
		}
		else if (label == Messages.PM_EXPORT){
		    subPieMenu = PieMenuFactory.createPieMenu(Messages.exportMenuLabels, 0 , mainView, vsm);
		    items = subPieMenu.getItems();
		    for (int i=0;i<items.length;i++){
			items[i].setType(Messages.PM_ENTRY);
		    }
		}
	    }
	}
	else {
	    subPieMenu.destroy(0);
	    subPieMenu = null;
	}
    }

    void pieMenuEvent(Glyph menuItem){
	int index = mainPieMenu.getItemIndex(menuItem);
	String label;
	if (index != -1){
	    label = mainPieMenu.getLabels()[index].getText();
	    if (label == Messages.PM_BACK){moveBack();}
	    else if (label == Messages.PM_GLOBALVIEW){getGlobalView();}
	}
	else {
	    index = subPieMenu.getItemIndex(menuItem);
	    if (index != -1){
		label = subPieMenu.getLabels()[index].getText();
		if (label == Messages.PM_OPENDOTSVG){open(0, false);}
		else if (label == Messages.PM_OPENDOTDOT){open(0, true);}
		else if (label == Messages.PM_OPENNEATOSVG){open(1, false);}
		else if (label == Messages.PM_OPENNEATODOT){open(1, true);}
		else if (label == Messages.PM_OPENSVG){openSVGFile();}
		else if (label == Messages.PM_OPENOTHER){openOther();}
		else if (label == Messages.PM_EXPSVG){saveSVG();}
		else if (label == Messages.PM_EXPPNG){savePNG();}
		else if (label == Messages.PM_EXPPRINT){print();}
	    }
	}
    }

    void updateMainViewFromRadar(){
	Camera c0 = mSpace.getCamera(0);
	c0.posx = observedRegion.vx;
	c0.posy = observedRegion.vy;
	vsm.repaintNow();
    }

    void centerRadarView(){
	if (ZGRViewer.rView != null){
	    vsm.getGlobalView(mSpace.getCamera(1),ConfigManager.ANIM_MOVE_LENGTH);
	    cameraMoved();
	}
    }

    /*--------------------------- Lens management --------------------------*/

    
    void setLens(int t){
	meh.lensType = t;
    }

    void moveLens(int x, int y, boolean write){
	lens.setAbsolutePosition(x, y);
	vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
 	    lens = mainView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	setLens(ZgrvEvtHdlr.ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
	long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - mainCamera.posx));
	long c2y = Math.round(my - INV_MAG_FACTOR * (my - mainCamera.posy));
	Vector cadata = new Vector();
	// -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
	Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
	if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
	    cadata.add(deltAlt);
	    cadata.add(new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, mainCamera.getID(), null);
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-mainCamera.posx)*ratio),
				     Math.round((c2y-mainCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, mainCamera.getID(), null);
	}
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - mainCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - mainCamera.posy));
	Vector cadata = new Vector();
	cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
	cadata.add(new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy));
	// create lens if it does not exist
	if (lens == null){
	    lens = mainView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	// animate lens and camera simultaneously
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, mainCamera.getID(), null);
	setLens(ZgrvEvtHdlr.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					 lens.getID(), new ZP2LensAction(this));
    }

    void setMagFactor(double m){
	MAG_FACTOR = m;
	INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca){
	synchronized (lens){
	    double nmf = MAG_FACTOR + magOffset;
	    if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
		setMagFactor(nmf);
		if (zooming == ZgrvEvtHdlr.ZOOMOUT_LENS){
		    /* if unzooming, we want to keep the focus point stable, and unzoom the context
		       this means that camera altitude must be adjusted to keep altitude + lens mag
		       factor constant in the lens focus region. The camera must also be translated
		       to keep the same region of the virtual space under the focus region */
		    float a1 = mainCamera.getAltitude();
		    lens.setMaximumMagnification((float)nmf, true);
		    /* explanation for the altitude offset computation: the projected size of an object
		       in the focus region (in lens space) should remain the same before and after the
		       change of magnification factor. The size of an object is a function of the
		       projection coefficient (see any Glyph.projectForLens() method). This means that
		       the following equation must be true, where F is the camera's focal distance, a1
		       is the camera's altitude before the move and a2 is the camera altitude after the
		       move:
		       MAG_FACTOR * F / (F + a1) = MAG_FACTOR + magOffset * F / (F + a2)
		       From this we can get the altitude difference (a2 - a1)                       */
		    mainCamera.altitudeOffset((float)((a1+mainCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
		    /* explanation for the X offset computation: the position in X of an object in the
		       focus region (lens space) should remain the same before and after the change of
		       magnification factor. This means that the following equation must be true (taken
		       by simplifying pc[i].lcx computation in a projectForLens() method):
		       (vx-(lensx1))*coef1 = (vx-(lensx2))*coef2
		       -- coef1 is actually MAG_FACTOR * F/(F+a1)
		       -- coef2 is actually (MAG_FACTOR + magOffset) * F/(F+a2)
		       -- lensx1 is actually camera.posx1 + ((F+a1)/F) * lens.lx
		       -- lensx2 is actually camera.posx2 + ((F+a2)/F) * lens.lx
		       Given that (MAG_FACTOR + magOffset) / (F+a2) = MAG_FACTOR / (F+a1)
		       we eventually have:
		       Xoffset = (a1 - a2) / F * lens.lx   (lens.lx being the position of the lens's center in
		       JPanel coordinates w.r.t the view's center - see Lens.java)
		    */
		    mainCamera.move(Math.round((a1-mainCamera.getAltitude())/mainCamera.getFocal()*lens.lx),
				    -Math.round((a1-mainCamera.getAltitude())/mainCamera.getFocal()*lens.ly));
		}
		else {
		    vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
						     lens.getID(), null);
		}
	    }
	}
    }

    Lens getLensDefinition(int x, int y){
	Lens res = null;
	res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
// 	switch (lensFamily){
// 	case L1_Linear:{res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L1_InverseCosine:{res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L1_Manhattan:{res = new L1FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L2_Gaussian:{res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L2_Linear:{res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L2_InverseCosine:{res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L2_Manhattan:{res = new FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
// 	case L2_Scrambling:{res = new FSScramblingLens(1.0f, LENS_R1, 1, x - panelWidth/2, y - panelHeight/2);break;}
// 	case LInf_Linear:{res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case LInf_InverseCosine:{res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
// 	case LInf_Manhattan:{res = new LInfFSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
// 	}
	return res;
    }

    void updatePanelSize(){
	Dimension d = mainView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    /* Web & URL */

    //open up the default or user-specified browser (netscape, ie,...) and try to display the content uri
    void displayURLinBrowser(String uri){
	if (ConfigManager.webBrowser==null){ConfigManager.webBrowser=new WebBrowser();}
	ConfigManager.webBrowser.show(uri);
    }

    void assignFontToGraph(){
	Font f=net.claribole.zvtm.fonts.FontDialog.getFontDialog((JFrame)mainView.getFrame(),ConfigManager.defaultFont);
	if (f!=null){
	    ConfigManager.defaultFont=f;
	    Vector glyphs = mSpace.getAllGlyphs();
	    Object g;
	    for (int i=0;i<glyphs.size();i++){
		g = glyphs.elementAt(i);
		if (g instanceof VText){
		    ((VText)g).setSpecialFont(null);
		}
	    }
	    vsm.setMainFont(ConfigManager.defaultFont);
	}
    }

    void showPreferences(){
	PrefWindow dp=new PrefWindow(this);
	dp.setLocationRelativeTo(mainView.getFrame());
	dp.setVisible(true);
    }

    void showSearchBox(){
	SearchBox sb = new SearchBox(this);
	sb.setLocationRelativeTo(mainView.getFrame());
	sb.setVisible(true);
    }

    /*antialias ON/OFF for views*/
    void setAntialiasing(boolean b){
	ConfigManager.ANTIALIASING=b;
	mainView.setAntialiasing(ConfigManager.ANTIALIASING);
    }

    void saveConfiguration(){
	cfgMngr.saveConfig();
    }

    void help(){
	TextViewer tv=new TextViewer(new StringBuffer(Messages.commands),"Commands",0,false);
	tv.setLocationRelativeTo(mainView.getFrame());
	tv.setVisible(true);
    }

    void about(){
	javax.swing.JOptionPane.showMessageDialog(mainView.getFrame(),Messages.about);
    }

    void exit(){
	cfgMngr.saveCommandLines();
	tpMngr.stop();
	cfgMngr.terminatePlugins();
	System.exit(0);
    }

    public static void main(String[] args){
	if (Utilities.osIsMacOS()){
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
	int acceleratedView = 0;
	for (int i=0;i<args.length;i++){
	    if (args[i].startsWith("-")){
		if (args[i].equals("--help")){
		    System.out.println("\n\njava net.claribole.zgrviewer.ZGRViewer [options] [file]");
		    System.out.println("[options] -volatile ZVTM will run in VolatileImage accelerated mode (requires JDK 1.4 or later)");
		    System.out.println("          -opengl   ZVTM will run in OpenGL accelerated mode (requires JDK 1.5 or later)");
		    System.out.println("          -Pxxx     where xxx={dot, neato, svg} to specify what program to use to compute the [file]'s layout");
		    System.out.println("[file]    can be a relative or full path ; use the native OS syntax\n\n");
		    System.exit(0);
		}
		else if (args[i].equals("-opengl")){
		    System.setProperty("sun.java2d.opengl", "true");
		    System.out.println("OpenGL accelerated mode");
		    acceleratedView = 2;
		}
		else if (args[i].equals("-volatile")){System.out.println("Volatile Image accelerated mode");acceleratedView = 1;}
		else if (args[i].startsWith("-P")){cmdLinePrg=args[i];}
	    }
	    else {//the only other stuff allowed as a cmd line param is a dot file
		File f=new File(args[i]);
		if (f.exists()){cmdLineDOTFile=f;}
	    }
	}
	System.out.println("--help for command line options");
	ZGRViewer appli=new ZGRViewer(acceleratedView);
    }
    
}
