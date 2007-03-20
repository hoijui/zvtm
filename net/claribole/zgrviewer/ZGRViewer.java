/*   FILE: ZGRViewer.java
 *   DATE OF CREATION:   Thu Jan 09 14:13:31 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id: ZGRViewer.java,v 1.41 2006/06/15 06:54:24 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.glyphs.PieMenu;
import net.claribole.zvtm.glyphs.PieMenuFactory;

import javax.swing.ImageIcon;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;


public class ZGRViewer implements ZGRApplication {

    static ConfigManager cfgMngr;
    static DOTManager dotMngr;

    public GVLoader gvLdr;
    public GraphicsManager grMngr;

    ZgrvEvtHdlr meh;
    
    static File cmdLineDOTFile=null;
    static String cmdLinePrg=null;

    PieMenu mainPieMenu, subPieMenu;


    ZGRViewer(int acc){
	initConfig();
	//init GUI after config as we load some GUI prefs from the config file
	initGUI(acc);
	if (cmdLineDOTFile!=null){loadCmdLineFile();}
    }

    void loadCmdLineFile(){
	if (cmdLinePrg!=null){
	    if (cmdLinePrg.equals("-Pneato")){
		gvLdr.loadFile(cmdLineDOTFile, DOTManager.NEATO_PROGRAM, false);
	    }
	    else if (cmdLinePrg.equals("-Pdot")){
		gvLdr.loadFile(cmdLineDOTFile, DOTManager.DOT_PROGRAM, false);
	    }
	    else if (cmdLinePrg.equals("-Pcirco")){
		gvLdr.loadFile(cmdLineDOTFile, DOTManager.CIRCO_PROGRAM, false);
	    }
	    else if (cmdLinePrg.equals("-Ptwopi")){
		gvLdr.loadFile(cmdLineDOTFile, DOTManager.TWOPI_PROGRAM, false);
	    }
	    else if (cmdLinePrg.equals("-Psvg")){
		gvLdr.loadSVG(cmdLineDOTFile);
	    }
	    else {
		System.err.println("Bad option: " + cmdLinePrg + "\n\t" + Messages.CMD_LINE_ERROR);
		System.exit(0);
	    }
	}
	else {
	    if (cmdLineDOTFile.toString().endsWith(".svg")){
		gvLdr.loadSVG(cmdLineDOTFile);
	    }
	    else {
		gvLdr.loadFile(cmdLineDOTFile, DOTManager.DOT_PROGRAM, false);
	    }
	}
    }

    void initConfig(){
	grMngr = new GraphicsManager(this);
	cfgMngr = new ConfigManager(grMngr, false);
	dotMngr=new DOTManager(grMngr, cfgMngr);
	grMngr.setConfigManager(cfgMngr);
	gvLdr = new GVLoader(this, grMngr, cfgMngr, dotMngr);
	cfgMngr.loadConfig();  //have to test for existence of config file
	cfgMngr.initPlugins(this);
    }

    void initGUI(int acc){
	Utils.initLookAndFeel();
	JMenuBar jmb = initViewMenu(acc);
	grMngr.createFrameView(grMngr.createZVTMelements(false), acc, jmb);
	grMngr.parameterizeView(new ZgrvEvtHdlr(this, this.grMngr));
    }

    JMenuBar initViewMenu(int accelerationMode){
 	JMenu open=new JMenu("Open");
        JMenu openD = new JMenu("Open with dot...");
        JMenu openN = new JMenu("Open with neato...");
        JMenu openC = new JMenu("Open with circo...");
        JMenu openT = new JMenu("Open with twopi...");
        final JMenuItem openO = new JMenuItem("Open with...");
	openO.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openDG = new JMenuItem("SVG pipeline (standard)...");
        openDG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openNG = new JMenuItem("SVG pipeline (standard)...");
        openNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openDI = new JMenuItem("DOT pipeline (experimental)...");
        final JMenuItem openNI = new JMenuItem("DOT pipeline (experimental)...");
        final JMenuItem openCG = new JMenuItem("SVG pipeline (standard)...");
        final JMenuItem openCI = new JMenuItem("DOT pipeline (experimental)...");
        final JMenuItem openTG = new JMenuItem("SVG pipeline (standard)...");
        final JMenuItem openTI = new JMenuItem("DOT pipeline (experimental)...");
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
	final JMenuItem versionI=new JMenuItem("Check for updates...");
	final JMenuItem aboutI=new JMenuItem("About...");
	exitI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (e.getSource()==openDG){gvLdr.open(DOTManager.DOT_PROGRAM, false);}
		    else if (e.getSource()==openNG){gvLdr.open(DOTManager.NEATO_PROGRAM, false);}
		    else if (e.getSource()==openDI){gvLdr.open(DOTManager.DOT_PROGRAM, true);}
		    else if (e.getSource()==openNI){gvLdr.open(DOTManager.NEATO_PROGRAM, true);}
		    else if (e.getSource()==openCG){gvLdr.open(DOTManager.CIRCO_PROGRAM, false);}
		    else if (e.getSource()==openCI){gvLdr.open(DOTManager.CIRCO_PROGRAM, true);}
		    else if (e.getSource()==openTG){gvLdr.open(DOTManager.TWOPI_PROGRAM, false);}
		    else if (e.getSource()==openTI){gvLdr.open(DOTManager.TWOPI_PROGRAM, true);}
		    else if (e.getSource()==openS){gvLdr.openSVGFile();}
		    else if (e.getSource()==openO){gvLdr.openOther();}
		    else if (e.getSource()==reloadI){gvLdr.reloadFile();}
		    else if (e.getSource()==globvI){grMngr.getGlobalView();}
		    else if (e.getSource()==radarI){grMngr.showRadarView(true);}
		    else if (e.getSource()==searchI){showSearchBox();}
		    else if (e.getSource()==backI){grMngr.moveBack();}
		    else if (e.getSource()==fontI){grMngr.assignFontToGraph();}
		    else if (e.getSource()==pngI){savePNG();}
		    else if (e.getSource()==svgI){saveSVG();}
		    else if (e.getSource()==printI){print();}
		    else if (e.getSource()==prefsI){showPreferences();}
		    else if (e.getSource()==exitI){exit();}
		    else if (e.getSource()==helpI){help();}
		    else if (e.getSource()==versionI){checkVersion();}
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
	open.add(openC);
	open.add(openT);
	open.add(openO);
	openD.add(openDG);
	openD.add(openDI);
	openN.add(openNG);
	openN.add(openNI);
	openC.add(openCG);
	openC.add(openCI);
	openT.add(openTG);
	openT.add(openTI);
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
	jm3.add(versionI);
	jm3.add(aboutI);
	openDG.addActionListener(a0);
	openDI.addActionListener(a0);
	openNG.addActionListener(a0);
	openNI.addActionListener(a0);
	openCG.addActionListener(a0);
	openCI.addActionListener(a0);
	openTG.addActionListener(a0);
	openTI.addActionListener(a0);
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
	versionI.addActionListener(a0);
	aboutI.addActionListener(a0);
	if (accelerationMode == 2){printI.setEnabled(false);}
	return jmb;
    }

    void savePNG(){
    	final long[] vr = grMngr.mainView.getVisibleRegion(grMngr.mSpace.getCamera(0));
    	SwingWorker sw = new SwingWorker(){
		public 	Object construct(){
		    new PNGExportWindow(vr[2] - vr[0], vr[1]-vr[3], grMngr);
		    return null;
		}
	    };
	sw.start();
    }

    void saveSVG(){
	final JFileChooser fc=new JFileChooser(ConfigManager.m_LastExportDir!=null ? ConfigManager.m_LastExportDir : ConfigManager.m_PrjDir);
	fc.setDialogTitle("Export SVG");
	int returnVal=fc.showSaveDialog(grMngr.mainView.getFrame());
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
	    grMngr.mainView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
	    ConfigManager.m_LastExportDir=f.getParentFile();
	    setStatusBarText("Exporting to SVG "+f.toString()+" ...");
	    if (f.exists()){f.delete();}
	    com.xerox.VTM.svg.SVGWriter svgw=new com.xerox.VTM.svg.SVGWriter();
	    Document d = svgw.exportVirtualSpace(grMngr.vsm.getVirtualSpace(grMngr.mainSpace), new DOMImplementationImpl(), f);
	    Utils.serialize(d,f);
	    setStatusBarText("Exporting to SVG "+f.toString()+" ...done");
	    grMngr.mainView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	}
    }

    public void setStatusBarText(String s){
	grMngr.mainView.setStatusBarText(s);
    }

    void print(){
    	final long[] vr = grMngr.mainView.getVisibleRegion(grMngr.mSpace.getCamera(0));
    	SwingWorker sw = new SwingWorker(){
		public 	Object construct(){
		    new PrintWindow(vr[2] - vr[0], vr[1]-vr[3], grMngr);
		    return null;
		}
	    };
	sw.start();
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
	    mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, grMngr.mainView, grMngr.vsm);
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
		    subPieMenu = PieMenuFactory.createPieMenu(Messages.fileMenuLabels, Messages.fileMenuLabelOffsets, 0 , grMngr.mainView, grMngr.vsm);
		    items = subPieMenu.getItems();
		    for (int i=0;i<items.length;i++){
			items[i].setType(Messages.PM_ENTRY);
		    }
		}
		else if (label == Messages.PM_EXPORT){
		    subPieMenu = PieMenuFactory.createPieMenu(Messages.exportMenuLabels, 0 , grMngr.mainView, grMngr.vsm);
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
	    if (label == Messages.PM_BACK){grMngr.moveBack();}
	    else if (label == Messages.PM_GLOBALVIEW){grMngr.getGlobalView();}
	}
	else {
	    index = subPieMenu.getItemIndex(menuItem);
	    if (index != -1){
		label = subPieMenu.getLabels()[index].getText();
		if (label == Messages.PM_OPENDOTSVG){gvLdr.open(DOTManager.DOT_PROGRAM, false);}
		else if (label == Messages.PM_OPENNEATOSVG){gvLdr.open(DOTManager.NEATO_PROGRAM, false);}
		else if (label == Messages.PM_OPENCIRCOSVG){gvLdr.open(DOTManager.CIRCO_PROGRAM, false);}
		else if (label == Messages.PM_OPENTWOPISVG){gvLdr.open(DOTManager.TWOPI_PROGRAM, false);}
		else if (label == Messages.PM_OPENSVG){gvLdr.openSVGFile();}
		else if (label == Messages.PM_OPENOTHER){gvLdr.openOther();}
		else if (label == Messages.PM_EXPSVG){saveSVG();}
		else if (label == Messages.PM_EXPPNG){savePNG();}
		else if (label == Messages.PM_EXPPRINT){print();}
	    }
	}
    }

    /* Web & URL */

    //open up the default or user-specified browser (netscape, ie,...) and try to display the content uri
    void displayURLinBrowser(String uri){
	if (ConfigManager.webBrowser==null){ConfigManager.webBrowser=new WebBrowser();}
	ConfigManager.webBrowser.show(uri, grMngr);
    }

    void showPreferences(){
	PrefWindow dp=new PrefWindow(this, grMngr);
	dp.setLocationRelativeTo(grMngr.mainView.getFrame());
	dp.setVisible(true);
    }

    void showSearchBox(){
	SearchBox sb = new SearchBox(grMngr);
	sb.setLocationRelativeTo(grMngr.mainView.getFrame());
	sb.setVisible(true);
    }

    void saveConfiguration(){
	cfgMngr.saveConfig();
    }

    void help(){
	TextViewer tv=new TextViewer(new StringBuffer(Messages.commands),"Commands",0,false);
	tv.setLocationRelativeTo(grMngr.mainView.getFrame());
	tv.setVisible(true);
    }

    public void about(){
	JOptionPane.showMessageDialog(grMngr.mainView.getFrame(),Messages.about);
    }

    static final String CURRENT_VERSION_URL = "http://zvtm.sourceforge.net/zgrviewer/currentVersion";

    public void checkVersion(){
	try {
	    String version = Utils.getTextContent(new URL(CURRENT_VERSION_URL), 10);
	    if (version != null){
		if (version.equals(Messages.VERSION)){
		    JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), Messages.YOU_HAVE_THE_MOST_RECENT_VERSION);
		}
		else {
		    JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), Messages.NEW_VERSION_AVAILABLE+version);
		}
	    }
	    else {
		JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), Messages.COULD_NOT_GET_VERSION_INFO, "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	catch (Exception ex){
	    JOptionPane.showMessageDialog(grMngr.mainView.getFrame(), Messages.COULD_NOT_GET_VERSION_INFO, "Error", JOptionPane.ERROR_MESSAGE);
	}
    }

    void exit(){
	cfgMngr.saveCommandLines();
	grMngr.paMngr.stop();
	cfgMngr.terminatePlugins();
	System.exit(0);
    }

    public static void main(String[] args){
	if (Utils.osIsMacOS()){
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
