/*   FILE: TrajectoryViewer1D.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Container;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class TrajectoryViewer1D extends JFrame {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1680;
    static final int VIEW_MAX_H = 1050;

    static final String VIEW_TITLE = "Trajectory View (1D)";

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H, VIEW_X, VIEW_Y;
    int TABLE_W, TABLE_X, TABLE_Y;
    int TABLE_H = 200;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    TV1DEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    Color GRAPH_COLOR = Color.RED;

    JTable trialTable;
    TrialTableModel ttm;
    Vector cinematics;
    Vector graphs;

    public TrajectoryViewer1D(){
	super();
	windowLayout();
	Container c = this.getContentPane();
	ttm = new TrialTableModel(0, 6);
	ttm.addTableModelListener(l1);
	trialTable = new JTable(ttm);
	trialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	int widthPerc = TABLE_W / 100;
	TableColumn tc = trialTable.getColumnModel().getColumn(0);
	tc.setPreferredWidth(widthPerc*20);tc.setHeaderValue("SID");
	tc = trialTable.getColumnModel().getColumn(1);
	tc.setPreferredWidth(widthPerc*20);tc.setHeaderValue("Technique");
	tc = trialTable.getColumnModel().getColumn(2);
	tc.setPreferredWidth(widthPerc*20);tc.setHeaderValue("ID");
	tc = trialTable.getColumnModel().getColumn(3);
	tc.setPreferredWidth(widthPerc*10);tc.setHeaderValue("Block");
	tc = trialTable.getColumnModel().getColumn(4);
	tc.setPreferredWidth(widthPerc*20);tc.setHeaderValue("Trial");
	tc = trialTable.getColumnModel().getColumn(5);
	tc.setPreferredWidth(widthPerc*10);tc.setHeaderValue("Display");
	// display Display column as checkbox (it is a boolean)
	TableCellRenderer tcr = trialTable.getDefaultRenderer(Boolean.class);
	tc.setCellRenderer(tcr);
	TableCellEditor tce = trialTable.getDefaultEditor(Boolean.class);
	tc.setCellEditor(tce);
	JScrollPane sp1 = new JScrollPane(trialTable);
	sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	sp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	c.add(sp1);
	vsm = new VirtualSpaceManager();
	init();
	this.pack();
	this.setSize(TABLE_W, TABLE_H);
	this.setLocation(TABLE_X, TABLE_Y);
	this.setVisible(true);
    }

    void init(){
	cinematics = new Vector();
	graphs = new Vector();
	eh = new TV1DEventHandler(this);
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	demoView.setEventHandler(eh);
	demoView.setBackgroundColor(Color.BLACK);
	System.gc();
	reset();
    }

    void loadFile(){
	JFileChooser fc = new JFileChooser(new File(LogManager.LOG_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Log File");
	int returnVal = fc.showOpenDialog(demoView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    parseCinematics(fc.getSelectedFile());
	}
    }

    void reset(){
	Vector v = mainVS.getAllGlyphs();
	v = (Vector)v.clone();
	Glyph g;
	for (int i=0;i<v.size();i++){
	    g = (Glyph)v.elementAt(i);
	    mainVS.destroyGlyph(g);
	}
	cinematics.clear();
	graphs.clear();
	while (trialTable.getRowCount() > 0){
	    ttm.removeRow(0);
	}
    }
    
    void parseCinematics(File f){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    String line = br.readLine();
	    String subjectName = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    String subjectID = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    String technique = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    String blockNumber = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    int viewportWidth = Integer.parseInt(line.substring(5));
	    line = br.readLine();
	    int viewportHeight = Integer.parseInt(line.substring(5));
	    br.readLine(); // next line contains column headers
	    // now starting 
	    line = br.readLine();
	    Vector ciLines = new Vector();
	    String items[];
	    String currentTrial = "0";
	    while (line != null){
		items = line.split(LogManager.OUTPUT_CSV_SEP);
		if (items[0].equals(currentTrial)){// new line for the same trial
		    ciLines.add(items);
		}
		else {// starting a new trial
		    // store previous trial
		    storeTrial(subjectID, technique, blockNumber, currentTrial, ciLines, viewportWidth, viewportHeight);
		    ciLines.clear();
		    currentTrial = items[0];
		}
		line = br.readLine();
	    }
	    // store last trial
	    storeTrial(subjectID, technique, blockNumber, currentTrial, ciLines, viewportWidth, viewportHeight);
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void storeTrial(String subjectID, String technique, String blockNumber, String trialNumber, Vector ciLines,
		    int viewportWidth, int viewportHeight){
	Vector entry = new Vector();
	entry.add(subjectID);
	entry.add(technique);
	double dist = Double.parseDouble(((String[])ciLines.firstElement())[1]);
	String ID = Long.toString(Math.round(Math.log(1+dist/LogManager.TARGET_WIDTH)/Math.log(2)));
	entry.add(ID);
	entry.add(blockNumber);
	entry.add(trialNumber);
	entry.add(Boolean.FALSE);
	ttm.addRow(entry);
	CinematicInfo[] ci = new CinematicInfo[ciLines.size()];
	String[] items;
	for (int i=0;i<ciLines.size();i++){
	    items = (String[])ciLines.elementAt(i);
	    ci[i] = new CinematicInfo(items[0], items[2], items[3], items[4], items[5],
				      items[6], items[7], items[8], viewportWidth, viewportHeight,
				      items[9]);
	}
	cinematics.add(ci);
	long ed;
	long distanceFromStart = 0;
	long previousAltitude = 0;
	VSegment[] segs = new VSegment[ci.length-1];
	Color color = getRandomColor();
	for (int i=1;i<ci.length;i++){
	    ed = Math.round(Math.sqrt(Math.pow(ci[i].cx-ci[i-1].cx,2) + Math.pow(ci[i].cy-ci[i-1].cy,2)));
	    segs[i-1] = new VSegment(distanceFromStart, previousAltitude, 0, color, distanceFromStart+ed, (long)ci[i].ca);
	    previousAltitude = (long)ci[i].ca;
	    distanceFromStart += ed;
	}
	graphs.add(segs);
    }

    void showTrial(int row){
	VSegment[] segs = (VSegment[])graphs.elementAt(row);
	for (int i=0;i<segs.length;i++){
// 	    segs[i].setColor(GRAPH_COLOR);
	    vsm.addGlyph(segs[i], mainVS);
	}
    }

    void hideTrial(int row){
	VSegment[] segs = (VSegment[])graphs.elementAt(row);
	for (int i=0;i<segs.length;i++){
	    mainVS.destroyGlyph(segs[i]);
	}
    }

    void updateAxes(){
    }

    static Color getRandomColor(){
	return Color.getHSBColor((float)Math.random(), 1, 1);
    }
    
    void setGraphColor(){
	GRAPH_COLOR = JColorChooser.showDialog(demoView.getFrame(), "Choose a new color", GRAPH_COLOR);
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
	TABLE_X = VIEW_X;
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = SCREEN_HEIGHT;
	TABLE_W = VIEW_W;
	if (VIEW_H > VIEW_MAX_H){VIEW_H = VIEW_MAX_H;}
	VIEW_H -= TABLE_H;
	TABLE_Y = VIEW_H;
	if (Utilities.osIsMacOS()){
	    VIEW_H -= 22;
	}
    }

    public static void main(String[] args){
	new TrajectoryViewer1D();
    }

    TableModelListener l1 = new TableModelListener(){//listener for trial table
	    public void tableChanged(TableModelEvent e){
		if (e.getType() == TableModelEvent.UPDATE){
		    int row = e.getFirstRow();
		    int column = e.getColumn();
		    if (column == 5){
			if (((Boolean)trialTable.getValueAt(row, column)).booleanValue()){
			    showTrial(row);
			}
			else {
			    hideTrial(row);
			}
		    }
		}
	    }
	};


}