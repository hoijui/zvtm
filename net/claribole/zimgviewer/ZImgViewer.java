/*   FILE: ZImgViewer.java
 *   DATE OF CREATION:   Thu May 29 16:06:36 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zimgviewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import net.claribole.zvtm.engine.Location;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VImageOr;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.svg.SVGWriter;

public class ZImgViewer {

    static VirtualSpaceManager vsm;
    static String mainSpace="imgspace";

    static View mainView;

    static int ANIM_MOVE_LENGTH=300;

    static int IMAGE_WIDTH=400;

    static final String aboutMsg="ZVTM Image Viewer v 0.1.0\n\nA Zoomable Image Visualizer based on the ZVTM\nhttp://zvtm.sourceforge.net\n\nWritten by Emmanuel Pietriga (emmanuel@w3.org,emmanuel@claribole.net)";

    ZimgEvtHdlr meh;

    File lastDir=new File(".");
    
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

    static boolean DISPLAY_FILE_NAMES = false;

    ZImgViewer(){
	initConfig();
	//init GUI after config as we load some GUI prefs from the config file
	initGUI();
    }

    void initConfig(){
	previousLocations=new Vector();
    }

    void initGUI(){
	Utils.initLookAndFeel();
	vsm=new VirtualSpaceManager();
	vsm.setZoomLimit(-90);
	vsm.setMouseInsideGlyphColor(Color.white);
	vsm.setDebug(true);
	vsm.addVirtualSpace(mainSpace);
	vsm.addCamera(mainSpace);
	Vector vc1=new Vector();
	vc1.add(vsm.getVirtualSpace(mainSpace).getCamera(0));
	JMenuBar jmb=initViewMenu();
 	mainView=vsm.addExternalView(vc1,"ZVTM Image Viewer", View.STD_VIEW, 800,600,true,false,jmb);
	mainView.setLocation(0,0);
	mainView.setBackgroundColor(Color.lightGray);
	meh=new ZimgEvtHdlr(this);
	mainView.setEventHandler(meh);
	mainView.setVisible(true);
    }

    JMenuBar initViewMenu(){
	final JMenuItem openI=new JMenuItem("Browse...");
	openI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
	final JMenuItem svgI=new JMenuItem("Export to SVG...");
	final JMenuItem exitI=new JMenuItem("Exit");
	exitI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	final JMenuItem backI=new JMenuItem("Back");
	backI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,0));
	final JMenuItem globvI=new JMenuItem("Global View");
	globvI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,0));
	final JMenuItem aboutI=new JMenuItem("About...");

	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (e.getSource()==openI){browse();}
		    else if (e.getSource()==svgI){toSVG();}
		    else if (e.getSource()==globvI){getGlobalView();}
		    else if (e.getSource()==backI){moveBack();}
		    else if (e.getSource()==exitI){exit();}
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
	jm1.add(openI);
	jm1.addSeparator();
	jm1.add(svgI);
	jm1.addSeparator();
	jm1.add(exitI);
	jm2.add(backI);
	jm2.add(globvI);
	jm3.add(aboutI);
	openI.addActionListener(a0);
	svgI.addActionListener(a0);
	exitI.addActionListener(a0);
	globvI.addActionListener(a0);
	backI.addActionListener(a0);
	aboutI.addActionListener(a0);
	return jmb;
    }

    void reset(){
	VirtualSpace vs=vsm.getVirtualSpace(mainSpace);
	Vector v=vs.getAllGlyphs();
	for (int i=v.size()-1;i>=0;i--){
	    vs.destroyGlyph((Glyph)v.elementAt(i));
	}
    }

    void browse(){
	final JFileChooser fc=new JFileChooser(lastDir!=null ? lastDir : new File("."));
 	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	fc.setDialogTitle("Browse Directory");
	int returnVal= fc.showOpenDialog(mainView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			lastDir=fc.getSelectedFile().getParentFile();
			openDirectory(fc.getSelectedFile());
			return null; 
		    }
		};
	    worker.start();
	}
    }
    
    void openDirectory(File f){
	reset();
	if (f!=null){//loading the content of a given directory
	    if (f.isDirectory()){
		File[] content=f.listFiles();
		if (content.length>0){
		    int nbCol=(int)Math.ceil(Math.sqrt(content.length));
		    int colIndex=0;
		    int rowIndex=0;
		    int i=0;
		    while (i<content.length){
			loadImage(content[i],Math.round(colIndex*2.5*IMAGE_WIDTH),Math.round(rowIndex*2.5*IMAGE_WIDTH));
			colIndex++;
			if (colIndex==nbCol){colIndex=0;rowIndex++;}
			i++;
		    }
		}
	    }
	    else {//loading a single file
		loadImage(f,0,0);
	    }
	    getGlobalView();
	}
    }

    void loadImage(File f,long x,long y){
	if (Utils.hasImageExtension(f)){
	    try {
		VImageOr im=new VImageOr(x,y,0,(new ImageIcon(f.toURL())).getImage(), 0);
		im.setDrawBorderPolicy(VImage.DRAW_BORDER_MOUSE_INSIDE);
		vsm.addGlyph(im,mainSpace);
		im.setWidth(IMAGE_WIDTH);
		if (ZImgViewer.DISPLAY_FILE_NAMES){
		    VText tx=new VText(x,Math.round(y-im.getHeight()*1.2),0,Color.black,f.getName(),VText.TEXT_ANCHOR_MIDDLE);
		    vsm.addGlyph(tx,mainSpace);
		    vsm.stickToGlyph(tx,im);
		}
		im.setOwner(new ImageInfo(im,f));
	    }
	    catch (java.net.MalformedURLException ex){ex.printStackTrace();}
	}
    }

    void toSVG(){
	final JFileChooser fc=new JFileChooser(".");
	fc.setDialogTitle("Export to SVG");
	int returnVal=fc.showSaveDialog(mainView.getFrame());
	final ZImgViewer appli = this;
	if (returnVal==JFileChooser.APPROVE_OPTION) {
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			appli.exportSVG(fc.getSelectedFile());
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
	    mainView.setStatusBarText("Exporting to SVG "+f.toString()+" ... (This operation can take some time if the model contains bitmap icons)");
	    if (f.exists()){f.delete();}
	    SVGWriter svgw=new SVGWriter();
	    Document d=svgw.exportVirtualSpace(vsm.getVirtualSpace(mainSpace),new DOMImplementationImpl(),f);
	    Utils.serialize(d,f);
	    mainView.setStatusBarText("Exporting to SVG "+f.toString()+" ...done");
	    mainView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
	}
    }    

    void getGlobalView(){
	Location l=vsm.getGlobalView(vsm.getActiveCamera(),ANIM_MOVE_LENGTH);
	rememberLocation(vsm.getActiveCamera().getLocation());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*direction should be one of ZImgViewer.MOVE_* */
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
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
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
	    Location currentlc=vsm.getActiveCamera().getLocation();
	    Vector animParams=Location.getDifference(currentlc,newlc);
	    vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_TRANS_SIG,animParams,vsm.getActiveCamera().getID());
	    previousLocations.removeElementAt(previousLocations.size()-1);
	}
    }

    void rotateGlyph(Glyph g, Float deltaAngle){
	ZImgViewer.vsm.animator.createGlyphAnimation(500, AnimManager.GL_ROT_SIG, deltaAngle, g.getID());
    }

    void resizeGlyph(Glyph g, Float sizeFactor){
	ZImgViewer.vsm.animator.createGlyphAnimation(500, AnimManager.GL_SZ_SIG, sizeFactor, g.getID());
    }

    void about(){
	javax.swing.JOptionPane.showMessageDialog(mainView.getFrame(),aboutMsg);
    }

    void exit(){
	System.exit(0);
    }

    public static void main(String[] args){
	for (int i=0;i<args.length;i++){
	    if (args[i].startsWith("-")){
		if (args[i].equals("--help")){
		    System.out.println("\n\njava net.claribole.zimgrviewer.ZImgViewer");
		    System.out.println("\n\t-f : display file names");
		    System.exit(0);
		}
		else if (args[i].equals("-f")){
		    ZImgViewer.DISPLAY_FILE_NAMES = true;
		}
	    }
	}
	System.out.println("--help for command line options");
	ZImgViewer appli=new ZImgViewer();
    }
    
}
