/*   FILE: GVLoader.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id:  $
 */ 

package net.claribole.zgrviewer;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.io.File;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.Location;

import org.w3c.dom.Document;

/* Multiscale feature manager */

class GVLoader {
    
    Object application; // instance of ZGRViewer or ZGRApplet
    
    GraphicsManager grMngr;
    ConfigManager cfgMngr;
    DOTManager dotMngr;
    
    GVLoader(Object app, GraphicsManager gm, ConfigManager cm, DOTManager dm){
	this.application = app;
	this.grMngr = gm;
	this.cfgMngr = cm;
	this.dotMngr = dm;
    }

    void open(short prg, boolean parser){// prg is the program to use DOTManager.*_PROGRAM, use the integrated parser or not
	if (ConfigManager.checkProgram(prg)){
	    openDOTFile(prg, parser);
	}
	else {
	    Object[] options = {"Yes", "No"};
	    int option = JOptionPane.showOptionDialog(null, ConfigManager.getDirStatus(),
						      "Warning", JOptionPane.DEFAULT_OPTION,
						      JOptionPane.WARNING_MESSAGE, null,
						      options, options[0]);
	    if (option == JOptionPane.OK_OPTION){
		openDOTFile(prg, parser);
	    }
	}
    }
    
    void openDOTFile(final short prg, final boolean parser){
	final JFileChooser fc = new JFileChooser(ConfigManager.m_LastDir!=null ? ConfigManager.m_LastDir : ConfigManager.m_PrjDir);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Find DOT File");
	int returnVal= fc.showOpenDialog(grMngr.mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			grMngr.reset();
			loadFile(fc.getSelectedFile(), prg, parser);
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
	int returnVal= fc.showOpenDialog(grMngr.mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			grMngr.reset();
			loadSVG(fc.getSelectedFile());
			return null; 
		    }
		};
	    worker.start();
	}
    }

    void openOther(){
	new CallBox((ZGRViewer)application, grMngr);
    }

    void loadFile(File f, short prg, boolean parser){//f is the DOT file to load, prg is the program to use DOTManager.*_PROGRAM
	if (f.exists()){
	    ConfigManager.m_LastDir=f.getParentFile();
	    cfgMngr.lastFileOpened = f;
	    dotMngr.lastProgramUsed = prg;
	    if (grMngr.mainView.isBlank() == null){grMngr.mainView.setBlank(cfgMngr.backgroundColor);}
	    dotMngr.load(f, prg, parser);
	    //in case a font was defined in the SVG file, make it the font used here (to show in Prefs)
	    ConfigManager.defaultFont = grMngr.vsm.getMainFont();
	    grMngr.mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
	    grMngr.reveal();
	    if (grMngr.previousLocations.size()==1){grMngr.previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	    if (grMngr.rView != null){
		grMngr.vsm.getGlobalView(grMngr.mSpace.getCamera(1),100);
		grMngr.cameraMoved();
	    }
	}
    }

    void loadSVG(File f){
	ProgPanel pp=new ProgPanel("Parsing SVG...","Loading SVG File");
	try {
	    pp.setPBValue(30);
	    cfgMngr.lastFileOpened = f;
	    dotMngr.lastProgramUsed = DOTManager.SVG_FILE;
	    Document svgDoc=Utils.parse(f,false);
	    pp.setLabel("Displaying...");
	    pp.setPBValue(80);
	    if (grMngr.mainView.isBlank() == null){grMngr.mainView.setBlank(cfgMngr.backgroundColor);}
	    SVGReader.load(svgDoc, grMngr.vsm, grMngr.mainSpace, true, f.toURL().toString());
	    grMngr.seekBoundingBox();
	    grMngr.buildLogicalStructure();
	    ConfigManager.defaultFont=grMngr.vsm.getMainFont();
	    grMngr.mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
// 	    grMngr.getGlobalView();
	    grMngr.reveal();
	    if (grMngr.previousLocations.size()==1){grMngr.previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	    if (grMngr.rView != null){
		grMngr.vsm.getGlobalView(grMngr.mSpace.getCamera(1),100);
		grMngr.cameraMoved();
	    }
	    pp.destroy();
	}
	catch (Exception ex){
	    grMngr.reveal();
	    pp.destroy();
	    ex.printStackTrace();
	    JOptionPane.showMessageDialog(grMngr.mainView.getFrame(),Messages.loadError+f.toString());
	}
    }

    /* method used by ZGRViewer - Applet to get the server-side generated SVG file */
    void loadSVG(String svgFileURL){
	try {
	    Document svgDoc = AppletUtils.parse(svgFileURL, false);
	    if (svgDoc != null){
		if (grMngr.mainView.isBlank() == null){grMngr.mainView.setBlank(cfgMngr.backgroundColor);}
		SVGReader.load(svgDoc, grMngr.vsm, grMngr.mainSpace, true, svgFileURL);
		grMngr.seekBoundingBox();
		grMngr.buildLogicalStructure();
		ConfigManager.defaultFont = grMngr.vsm.getMainFont();
		grMngr.reveal();
		//do not remember camera's initial location (before global view)
		if (grMngr.previousLocations.size()==1){grMngr.previousLocations.removeElementAt(0);}
		if (grMngr.rView != null){
		    grMngr.vsm.getGlobalView(grMngr.mSpace.getCamera(1), 100);
		}
		grMngr.cameraMoved();
	    }
	    else {
		System.err.println("An error occured while loading file " + svgFileURL);
	    }
	}
	catch (Exception ex){grMngr.reveal();ex.printStackTrace();}
    }


    void load(String commandLine, String sourceFile){
	grMngr.reset();
	dotMngr.loadCustom(sourceFile, commandLine);
	//in case a font was defined in the SVG file, make it the font used here (to show in Prefs)
	ConfigManager.defaultFont = grMngr.vsm.getMainFont();
	grMngr.mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+sourceFile);
// 	grMngr.getGlobalView();
	grMngr.reveal();
	if (grMngr.previousLocations.size()==1){grMngr.previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	if (grMngr.rView != null){
	    grMngr.vsm.getGlobalView(grMngr.mSpace.getCamera(1),100);
	    grMngr.cameraMoved();
	}
    }

    void reloadFile(){
        //XXX: TODO: support integrated parser during reload
	if (cfgMngr.lastFileOpened != null){
	    grMngr.reset();
	    if (dotMngr.lastProgramUsed == DOTManager.SVG_FILE){
		this.loadSVG(cfgMngr.lastFileOpened);
	    }
	    else {
		this.loadFile(cfgMngr.lastFileOpened, dotMngr.lastProgramUsed, false);
	    }
	}
    }
    
}
