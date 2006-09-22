/*   FILE: AbstractVisitViewer.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractVisitViewer.java,v 1.7 2006/06/02 12:17:21 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.util.Vector;
import java.util.Hashtable;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class AbstractVisitViewer implements Java2DPainter {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;

    static final String VIEW_TITLE = "Visit View";

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H, VIEW_X, VIEW_Y;

    static final float START_ALTITUDE = 10000;

    static final String GLYPH_TYPE_LINK = "LNK";

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    AbstractVVEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

//     // information about block displayed on screen
//     String infoHeader;
//     String subjectName;
//     String subjectID;
//     String blockNumber;
//     String technique;
    // information about viewport (required to compute region seen through camera from altitude data)
    int viewportWidth;
    int viewportHeight;

    String info;

    public AbstractVisitViewer(){
	vsm = new VirtualSpaceManager();
	init();
    }

    void init(){
	eh = new AbstractVVEventHandler(this);
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	updatePanelSize();
	demoView.setEventHandler(eh);
	demoView.getPanel().addComponentListener(eh);
	demoCamera.setAltitude(START_ALTITUDE);
	demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
	buildWorld();
	System.gc();
    }

    static long WORLD_WIDTH;
    static long WORLD_HEIGHT;
    static long HALF_WORLD_WIDTH;
    static long HALF_WORLD_HEIGHT;
    static long[] widthByLevel;
    static int[] cornerByLevel;
    static LongPoint[][] offsetsByLevel;
    ZRoundRect[][] elementsByLevel;
    boolean[][] visitsByLevel = new boolean[ZLAbstractTask.TREE_DEPTH][ZLAbstractTask.DENSITY*ZLAbstractTask.DENSITY];
    static {
	// compute size of all levels
	widthByLevel = new long[ZLAbstractTask.TREE_DEPTH];
	cornerByLevel = new int[ZLAbstractTask.TREE_DEPTH];
	offsetsByLevel = new LongPoint[ZLAbstractTask.TREE_DEPTH][ZLAbstractTask.DENSITY*ZLAbstractTask.DENSITY];
	for (int i=0;i<ZLAbstractTask.TREE_DEPTH;i++){
	    widthByLevel[i] = ZLAbstractTask.SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(ZLAbstractTask.MUL_FACTOR, (ZLAbstractTask.TREE_DEPTH-i-1)));
	    cornerByLevel[i] = ((int)Math.round(widthByLevel[i]/ZLAbstractTask.ROUND_CORNER_RATIO));
	    if (i>0){
		long step = Math.round(widthByLevel[i-1]/((double)ZLAbstractTask.DENSITY)) * 2;
		long y = widthByLevel[i-1] - step/2;
		for (int j=0;j<ZLAbstractTask.DENSITY;j++){
		    long x = -widthByLevel[i-1] + step/2;
		    for (int k=0;k<ZLAbstractTask.DENSITY;k++){
			offsetsByLevel[i][j*ZLAbstractTask.DENSITY+k] = new LongPoint(x, y);
			x += step;
		    }
		    y -= step;
		}
	    }
	}
	WORLD_WIDTH = widthByLevel[0] * 2;
	WORLD_HEIGHT = WORLD_WIDTH;
	HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
	HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
    }

    static Hashtable id2element = new Hashtable();

    void buildWorld(){
	elementsByLevel = new ZRoundRect[ZLAbstractTask.TREE_DEPTH][ZLAbstractTask.DENSITY*ZLAbstractTask.DENSITY];
	elementsByLevel[0][0] = new ZRoundRect(0, 0, 0,
					       widthByLevel[0], widthByLevel[0],
					       ZLAbstractTask.COLOR_BY_LEVEL[0],
					       cornerByLevel[0], cornerByLevel[0], false);
	vsm.addGlyph(elementsByLevel[0][0], mainVS);
	elementsByLevel[0][0].setPaintBorder(false);
	for (int i=1;i<ZLAbstractTask.TREE_DEPTH;i++){
	    for (int j=0;j<ZLAbstractTask.DENSITY;j++){
		for (int k=0;k<ZLAbstractTask.DENSITY;k++){
		    elementsByLevel[i][j*ZLAbstractTask.DENSITY+k] = new ZRoundRect(offsetsByLevel[i][j*ZLAbstractTask.DENSITY+k].x,
								     offsetsByLevel[i][j*ZLAbstractTask.DENSITY+k].y,
								     0,
								     widthByLevel[i], widthByLevel[i],
								     ZLAbstractTask.COLOR_BY_LEVEL[1],
								     cornerByLevel[i], cornerByLevel[i], false);
		    vsm.addGlyph(elementsByLevel[i][j*ZLAbstractTask.DENSITY+k], mainVS);
		    elementsByLevel[i][j*ZLAbstractTask.DENSITY+k].setPaintBorder(true); // actual drawing of the border will depend on the rendering size for each rectangle
		    elementsByLevel[i][j*ZLAbstractTask.DENSITY+k].setBorderColor(ZLAbstractTask.DISC_BORDER_COLOR);
		    elementsByLevel[i][j*ZLAbstractTask.DENSITY+k].setType(ZLAbstractTask.GLYPH_TYPE_WORLD);
		    id2element.put(String.valueOf(j*ZLAbstractTask.DENSITY+k+1), elementsByLevel[i][j*ZLAbstractTask.DENSITY+k]);
		}
	    }
	}
    }

    void loadFile(){
	JFileChooser fc = new JFileChooser(new File(LogManager.LOG_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Log File");
	int returnVal = fc.showOpenDialog(demoView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    parseTrials(fc.getSelectedFile());
	}
    }

    int showingVisitNumber = -1;
    
    VisitInfo[] vi = new VisitInfo[0];
    
    void parseTrials(File f){
// 	String whichTrial = JOptionPane.showInputDialog("Which trial do you want to visualize?");
// 	if (whichTrial == null){return;}
	try {
	    FileInputStream fis = new FileInputStream(f);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    String line = br.readLine();
	    line = br.readLine();
	    String[] items;
	    int tn = 0;
	    while (line != null && line.length() > 0){
		items = line.split(LogManager.OUTPUT_CSV_SEP);
		VisitInfo[] tmpA = new VisitInfo[vi.length+1];
		System.arraycopy(vi,0,tmpA,0,vi.length);
		tmpA[vi.length] = new VisitInfo(items, tn);
		vi = tmpA;
		line = br.readLine();
		tn++;
	    }
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void resetLinks(){
	Vector v = mainVS.getGlyphsOfType(GLYPH_TYPE_LINK);
	for (int i=0;i<v.size();i++){
	    mainVS.destroyGlyph((Glyph)v.elementAt(i));
	}
    }

    void showVisitSequence(int i){
	info = vi[i].technique + " " + vi[i].trial;
	ZRoundRect startG, endG;
	for (int j=1;j<vi[i].visitSeq.length;j++){
	    startG = (ZRoundRect)id2element.get(String.valueOf(vi[i].visitSeq[j-1]));
	    endG = (ZRoundRect)id2element.get(String.valueOf(vi[i].visitSeq[j]));
	    VSegment link = new VSegment(startG.vx, startG.vy, 0, Color.BLACK, endG.vx, endG.vy);
	    link.setType(GLYPH_TYPE_LINK);
	    vsm.addGlyph(link, mainVS);
	}
    }

    void showNextVisitSequence(){
	if (showingVisitNumber < vi.length-1){
	    resetLinks();
	    showingVisitNumber++;
	    showVisitSequence(showingVisitNumber);
	}
    }
    
    void showPreviousVisitSequence(){
	if (showingVisitNumber > 0){
	    resetLinks();	    
	    showingVisitNumber--;
	    showVisitSequence(showingVisitNumber);
	}	
    }

    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (showingVisitNumber != -1){
	    g2d.setColor(Color.RED);
	    g2d.drawString(info, 20, 30);
	}
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = SCREEN_HEIGHT;
	if (VIEW_H > VIEW_MAX_H){VIEW_H = VIEW_MAX_H;}
	if (Utilities.osIsMacOS()){
	    VIEW_H -= 22;
	}
    }

    public static void main(String[] args){
	new AbstractVisitViewer();
    }

}