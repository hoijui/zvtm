/*   FILE: LocateTask.java
 *   DATE OF CREATION:  Sat Apr 22 10:05:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LocateTask.java,v 1.29 2006/05/24 08:25:19 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.Java2DPainter;

public class LocateTask implements Java2DPainter {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H, QPANEL_W;
    int QPANEL_H = 100;
    int VIEW_X, VIEW_Y, QPANEL_X, QPANEL_Y;
    
    /* main 8000x4000 map (always displayed) */
    VImage mainMap;
    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;
    static final long MAP_WIDTH = Math.round(MAIN_MAP_WIDTH * MapData.MN000factor.doubleValue());
    static final long MAP_HEIGHT = Math.round(MAIN_MAP_HEIGHT * MapData.MN000factor.doubleValue());
    static final long HALF_MAP_WIDTH = Math.round(MAP_WIDTH/2.0);
    static final long HALF_MAP_HEIGHT = Math.round(MAP_HEIGHT/2.0);

    static final String LOADING_WORLDMAP_TEXT = "Loading World Map ...";

    static final String VIEW_TITLE = "Target Location";

    static final String QP1 = "<html>Do you have any idea of where <strong>";
    static final String QP2 = "</strong> is located?</html>";
    static final String IP1 = "<html>Click on the location of <strong>";
    static final String IP2 = "</strong> on the map and press the space bar to validate your answer.</html>";
    static final String COMMA = ", ";

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);

    private boolean SHOW_START_LOC = false;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    LocateTaskEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    static final float START_ALTITUDE = 6250;

    /* panel used to display questions and messages */
    LocateTaskQPanel qpanel;

    boolean sessionStarted = false;
    int trialCount;
    boolean locatingCity = false;

    /* used to forbid going to next trial before a location has
       been specified (if answered yes to the question) */
    boolean noLocationSpecifiedYet = true;

    /* trials */
    TrialInfo[] trials;
    /* logs */
    String subjectName;
    String subjectID;
    String blockNumber;
    
    File logFile;
    BufferedWriter bwt;
    String lineStart;

    static final String PSBTC = "PRESS SPACE BAR TO CONTINUE";
    static final String PSTS = "PRESS S TO START";
    static final String EOS = "END OF SESSION";
    String msg = PSBTC;

    LocateTask(boolean ssl){
	SHOW_START_LOC = ssl;
	vsm = new VirtualSpaceManager();
	init();
    }

    public void init(){
	eh = new LocateTaskEventHandler(this);
	windowLayout();
	qpanel = new LocateTaskQPanel(QPANEL_X, QPANEL_Y, QPANEL_W, QPANEL_H, this);
	qpanel.say(LOADING_WORLDMAP_TEXT);
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	demoView.setEventHandler(eh);
	demoCamera.setAltitude(START_ALTITUDE);
	initMap();
	System.gc();
	qpanel.say(PSTS);
    }

    void initMap(){
	mainMap = new VImage(MapData.M1000x, MapData.M1000y, 0,
 			     (new ImageIcon(MapData.M1000path)).getImage(),
			     MapData.MN000factor.doubleValue());
	mainMap.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(mainMap, LocateTask.mainVSname);
	mainVS.atBottom(mainMap);
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    /*uncomment the following line if the Windows toolbar should be visible*/
	    // SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = SCREEN_HEIGHT - QPANEL_H;
	if (VIEW_H > VIEW_MAX_H){VIEW_H = VIEW_MAX_H;}
	if (Utilities.osIsMacOS()){
	    VIEW_H -= 22;
	}
	QPANEL_X = VIEW_X;
	QPANEL_Y = SCREEN_HEIGHT - QPANEL_H;
	QPANEL_W = VIEW_W;
    }

    void startSession(){
	qpanel.say(" ");
	String subjName = JOptionPane.showInputDialog("Subject Name");
	if (subjName == null){qpanel.say(PSTS);return;}
	String subjID = JOptionPane.showInputDialog("Subject ID");
	if (subjID == null){qpanel.say(PSTS);return;}
 	String bNum = JOptionPane.showInputDialog("Block number");
 	if (bNum == null){qpanel.say(PSTS);return;}
	JFileChooser fc = new JFileChooser(new File(LogManager.TRIAL_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Trial File");
	int returnVal= fc.showOpenDialog(demoView.getFrame());
	File trialFile = null;
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    trialFile = fc.getSelectedFile();
	}
	else {
	    qpanel.say(PSTS);
	    return;
	}
	initLog(subjName, subjID, bNum, LogManager.initLogFile(subjID+"-locate-block"+bNum, LogManager.LOG_DIR));
	parseTrialFile(trialFile);
	qpanel.say(PSBTC);
	trialCount = -1;
	sessionStarted = true;
	demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
 	((JFrame)demoView.getFrame()).toFront();
    }

    void endSession(){
	sessionStarted = false;
	try {
	    bwt.flush();
	    bwt.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	qpanel.say(EOS);
    }

    void initLog(String subjName, String subjID, String bn, File lf){
	subjectName = subjName;
	subjectID = subjID;
	blockNumber = bn;
	lineStart = subjectName + "\t" +
	    subjectID + "\t" +
	    blockNumber + "\t";
	try {
	    logFile = lf;
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
	}
	catch (IOException ex){ex.printStackTrace();}
	writeHeaders();
    }

    void parseTrialFile(File f){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    Vector v = new Vector();
	    String line = br.readLine();
	    line = br.readLine(); // ignore first line (column headers)
	    while (line != null){
		v.add(new TrialInfo(line));
		line = br.readLine();
	    }
	    trials = new TrialInfo[v.size()];
	    for (int j=0;j<trials.length;j++){
		trials[j] = (TrialInfo)v.elementAt(j);
	    }
	    if (SHOW_START_LOC){showStartLocationsOnMap();}
	    v.clear();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void showStartLocationsOnMap(){
	for (int i=0;i<trials.length;i++){
	    vsm.addGlyph(new VRectangle(trials[i].startLongitude, trials[i].startLatitude, 0,
					200, 200, Color.RED),
			 mainVS);
	    vsm.addGlyph(new VRectangle(trials[i].targetLongitude, trials[i].targetLatitude, 0,
					200, 200, Color.GREEN),
			 mainVS);
	    vsm.addGlyph(new VSegment(trials[i].targetLongitude, trials[i].targetLatitude, 0,
					Color.ORANGE, trials[i].startLongitude, trials[i].startLatitude),
			 mainVS);
	}
    }

    void writeHeaders(){
	try {
	    bwt.write("Name\tSID\tBlock #\tTrial #\tID\tKnows\tDistance\tAnswerLat\tAnswerLon\tActualLat\tActualLon\tCity\tRegion\tCountry");
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void nextStep(){
	if (!sessionStarted){return;}
	trialCount++;
	if (trialCount < trials.length){// there is at least one trial left
	    startTrial();
	}
	else {
	    endSession();
	}
    }

    void startTrial(){
	noLocationSpecifiedYet = true;
	if (trials[trialCount].targetRegion == null){
	    qpanel.ask(QP1 + trials[trialCount].targetName +
		       COMMA + trials[trialCount].targetCountry + QP2);
	}
	else {
	    qpanel.ask(QP1 + trials[trialCount].targetName +
		       COMMA + trials[trialCount].targetRegion +
		       COMMA + trials[trialCount].targetCountry + QP2);
	}
    }

    void aboutToLocateCity(){
	demoView.setJava2DPainter(null, Java2DPainter.FOREGROUND);
	locatingCity = true;
	if (trials[trialCount].targetRegion == null){
	    qpanel.say(IP1 + trials[trialCount].targetName +
		       COMMA + trials[trialCount].targetCountry + IP2);
	}
	else {
	    qpanel.say(IP1 + trials[trialCount].targetName +
		       COMMA + trials[trialCount].targetRegion +
		       COMMA + trials[trialCount].targetCountry + IP2);
	}
    }

    VSegment t1, t2;
    static final long HINT_W = 500;

    void locatingCity(long lon, long lat){
	noLocationSpecifiedYet = false;
	if (t1 != null){
	    t1.moveTo(lon, lat);
	    t2.moveTo(lon, lat);
	}
	else {
	    t1 = new VSegment(lon, lat, 0, HINT_W, 0, Color.RED);
	    t2 = new VSegment(lon, lat, 0, 0, HINT_W, Color.RED);
	    vsm.addGlyph(t1, mainVSname);
	    vsm.addGlyph(t2, mainVSname);
	}
    }

    void locatedCity(){
	demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
	log(t1.vy, t1.vx, true);
	mainVS.destroyGlyph(t1);
	mainVS.destroyGlyph(t2);
	t1 = null;
	t2 = null;
	locatingCity = false;
	nextStep();
    }

    void doesNotKnow(){
	log(0, 0, false);
	nextStep();
    }

    void log(long lat, long lon, boolean knows){
	try {
	    bwt.write(lineStart);
	    bwt.write(trialCount + "\t");
	    TrialInfo ti = trials[trialCount];
	    bwt.write(ti.id + "\t");
	    if (knows){
		bwt.write("Y\t" +
			  TrialInfo.doubleFormatter(Math.sqrt(Math.pow(lat-ti.targetLatitude, 2) + 
							      Math.pow(lon-ti.targetLongitude, 2))) + "\t" +
			  lat + "\t" + lon+"\t");
	    }
	    else {
		bwt.write("N\t\t\t\t");
	    }
	    if (ti.targetRegion == null){
		bwt.write(ti.targetLatitude + "\t" + ti.targetLongitude + "\t" +
			  ti.targetName + "\t\t" + ti.targetCountry);		
	    }
	    else {
		bwt.write(ti.targetLatitude + "\t" + ti.targetLongitude + "\t" +
			  ti.targetName + "\t" + ti.targetRegion + "\t" + ti.targetCountry);
	    }
	    bwt.newLine();
	    bwt.flush();
	}
	catch (Exception ex){ex.printStackTrace();}
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=demoView.getCameraNumber(0);
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ZLWorldTask.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=demoView.getCameraNumber(0);
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ZLWorldTask.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(Color.BLACK);
	g2d.setComposite(acST);
	g2d.fillRect(0, 0, viewWidth, viewHeight);
	g2d.setComposite(com.xerox.VTM.glyphs.Translucent.acO);
    }

    public static void main(String[] args){
	boolean ssl = (args.length >= 1) ? (Short.parseShort(args[0])==1) : false;
	new LocateTask(ssl);
    }
    
}
