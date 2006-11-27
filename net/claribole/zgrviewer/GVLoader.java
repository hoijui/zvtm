/*   FILE: GVLoader.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id:  $
 */ 

package net.claribole.zgrviewer;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.io.File;

import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.svg.SVGReader;

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
	int returnVal= fc.showOpenDialog(grMngr.mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			grMngr.reset();
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
	int returnVal= fc.showOpenDialog(grMngr.mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			grMngr.reset();
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

    void loadFile(File f,String prg, boolean parser){//f=DOT file to load, prg=program to use ("dot" or "neato")
	if (f.exists()){
	    ConfigManager.m_LastDir=f.getParentFile();
	    cfgMngr.lastFileOpened = f;
	    cfgMngr.lastProgramUsed = (prg.equals("neato")) ? ConfigManager.NEATO_PROGRAM : ConfigManager.DOT_PROGRAM;
	    dotMngr.load(f,prg, parser);
	    //in case a font was defined in the SVG file, make it the font used here (to show in Prefs)
	    ConfigManager.defaultFont = grMngr.vsm.getMainFont();
	    grMngr.mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
	    grMngr.getGlobalView();
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
	    cfgMngr.lastProgramUsed = ConfigManager.SVG_FILE;
	    Document svgDoc=Utils.parse(f,false);
	    pp.setLabel("Displaying...");
	    pp.setPBValue(80);
	    SVGReader.load(svgDoc, grMngr.vsm, grMngr.mainSpace, true);
	    ConfigManager.defaultFont=grMngr.vsm.getMainFont();
	    grMngr.mainView.setTitle(ConfigManager.MAIN_TITLE+" - "+f.getAbsolutePath());
	    grMngr.getGlobalView();
	    if (grMngr.previousLocations.size()==1){grMngr.previousLocations.removeElementAt(0);} //do not remember camera's initial location (before global view)
	    if (grMngr.rView != null){
		grMngr.vsm.getGlobalView(grMngr.mSpace.getCamera(1),100);
		grMngr.cameraMoved();
	    }
	    pp.destroy();
	}
	catch (Exception ex){
	    pp.destroy();
	    JOptionPane.showMessageDialog(grMngr.mainView.getFrame(),Messages.loadError+f.toString());
	}
    }

    void reloadFile(){
        // TODO: support integrated parser during reload
	if (cfgMngr.lastFileOpened != null){
	    grMngr.reset();
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
    
}
