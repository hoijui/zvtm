/*   FILE: LocateTask2.java
 *   DATE OF CREATION:  Wed May 10 09:05:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LocateTask2.java,v 1.15 2006/06/08 12:36:23 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.Point;

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

public class LocateTask2 implements Java2DPainter {

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

    static final float START_ALTITUDE = 6250;

    static int ANIM_MOVE_LENGTH = 100;

    static final String LOADING_WORLDMAP_TEXT = "Loading World Map ...";

    static final String OUTPUT_CSV_SEP = ";";
    
    int trialCount = 0;
    
    static final String VIEW_TITLE = "Target Location";
    static final String IP1 = "<html>Draw a rectangle around the location of <strong>";
    static final String IP2 = "</strong> on the map and press the space bar to validate your answer.</html>";
    static final String COMMA = ", ";

    static final int ERR_MSG_DELAY = 10000;

    String questionText;

    static final int DEFAULT_NB_TRIALS_PER_ICID_PAIR = 3;
    static final String IC_LOW = "ICL";
    static final String IC_MEDIUM = "ICM";
    static final String IC_HIGH = "ICH";

    static final long ICH_AREA_MIN = 1;
    static final long ICH_AREA_MAX = 800000;  //  about the size of a viewport at max. zoom (1024 x 768)
    static final long ICM_AREA_MIN = 800000;
    static final long ICM_AREA_MAX = 20000000; // about the size of the 5x5 vicinity of a viewport (1024 x 768 x 25)
    static final long ICL_AREA_MIN = 20000000;
    static final long ICL_AREA_MAX = 2048000000; // fill map size (64000 x 32000)

    Vector IDs4ICL = new Vector();
    Vector IDs4ICM = new Vector();
    Vector IDs4ICH = new Vector();
    int nbWantedIcLow = 9;
    int nbWantedIcMedium = 9;
    int nbWantedIcHigh = 9;
    int countIcLow = 0;
    int countIcMedium = 0;
    int countIcHigh = 0;
    boolean missingAtLeastOneTrial = (countIcLow < nbWantedIcLow)
	|| (countIcMedium < nbWantedIcMedium)
	|| (countIcHigh < nbWantedIcHigh);
    String progressSummary = "";

    /* pools of cities categorized by probable IC (low/medium/high) */
    Vector citiesICL = new Vector();
    int citiesICLindex = 0;
    Vector citiesICM = new Vector();
    int citiesICMindex = 0;
    Vector citiesICH = new Vector();
    int citiesICHindex = 0;

    CityInfo currentCity;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    LocateTask2EventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    /* panel used to display questions and messages */
    InstructionsPanel qpanel;

    boolean sessionStarted = false;

    /* used to forbid going to next trial before a location has
       been specified (if answered yes to the question) */
    boolean noLocationSpecifiedYet = true;

    /* logs */
    String subjectName;
    String subjectID;
    String blockNumber;
    
    File logFile;
    BufferedWriter bwt;
    String lineStart;

    static final String PSTS = "PRESS S TO START";
    static final String EOS = "END OF SESSION";

    /* true when user dragging mouse to draw rectangular region (answer to question) */
    boolean drawingRegion = false;
    /* top left and bottom right corners of rectangular region (answer to question) */
    long northWestLatitude, northWestLongitude, southEastLatitude, southEastLongitude;

    LocateTask2(){
	vsm = new VirtualSpaceManager();
	init();
    }

    public void init(){
	windowLayout();
	qpanel = new InstructionsPanel(QPANEL_X, QPANEL_Y, QPANEL_W, QPANEL_H);
	qpanel.setVisible(true);
	qpanel.say(LOADING_WORLDMAP_TEXT);
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	eh = new LocateTask2EventHandler(this);
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
	vsm.addGlyph(mainMap, mainVSname);
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
	initLog(subjName, subjID, bNum, LogManager.initLogFile(subjID+"-input-trials-block"+bNum, LogManager.TRIAL_DIR));
	parseTrialFiles(trialFile);
	demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
	sessionStarted = true;
 	((JFrame)demoView.getFrame()).toFront();
	nextStep();
    }

    void endSession(){
	sessionStarted = false;
	try {
	    bwt.flush();
	    bwt.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	// dump ICL
	File f1 = LogManager.initLogFile(subjectID+"-ICLleft"+blockNumber, LogManager.TRIAL_DIR);
	dumpRemainingCities(f1, citiesICLindex, citiesICL);
	// dump ICM
	File f2 = LogManager.initLogFile(subjectID+"-ICMleft"+blockNumber, LogManager.TRIAL_DIR);
	dumpRemainingCities(f2, citiesICMindex, citiesICM);
	int block = Integer.parseInt(blockNumber);
	if (block < 3){
	    // for ICH, we actually take the same cities but randomize their sequence
	    File f3 = LogManager.initLogFile(subjectID+"-ICHleft"+blockNumber, LogManager.TRIAL_DIR);
	    randomizeFile(new File(LogManager.TRIAL_DIR_FULL + File.separator + "all-cities-ich.csv"), f3);
	    writeNextBlockFile(LogManager.initLogFile(subjectID+"-block"+(block+1), LogManager.TRIAL_DIR),
			       f1, f2, f3, block);
	}
	qpanel.say(EOS);
    }

    // these have been generated randomly once and for all
    static final String[][] IDlistPerBlock = { // block 1 ID list is in initial-block.csv
	{"8;10;8;12;10;12;10;8;12", "12;10;8;8;12;8;10;10;12", "10;12;8;12;8;10;12;8;10"}, // for block 2
	{"10;12;8;8;12;10;10;12;8", "12;8;8;12;10;10;8;12;10", "10;10;8;8;12;12;8;12;10"}  // for block 3
    };

    static void writeNextBlockFile(File blockFile, File iclF, File icmF, File ichF, int block){
	try {
	    BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blockFile), "UTF-8"));
	    bwr.write(IDlistPerBlock[block-1][0]);
	    bwr.newLine();
	    bwr.write(IDlistPerBlock[block-1][1]);
	    bwr.newLine();
	    bwr.write(IDlistPerBlock[block-1][2]);
	    bwr.newLine();
	    bwr.write(iclF.getName());
	    bwr.newLine();
	    bwr.write(icmF.getName());
	    bwr.newLine();
	    bwr.write(ichF.getName());
	    bwr.newLine();
	    bwr.flush();
	    bwr.close();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    static void dumpRemainingCities(File f, int startIndex, Vector cities){
	try {
	    BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
	    CityInfo ci;
	    for (int i=startIndex;i<cities.size();i++){
		ci = (CityInfo)cities.elementAt(i);
		if (ci.region != null){
		    bwr.write(ci.name + GeoDataStore.CSV_SEP +
			      ci.region + GeoDataStore.CSV_SEP +
			      ci.country + GeoDataStore.CSV_SEP +
			      Long.toString(ci.latitude) + GeoDataStore.CSV_SEP +
			      Long.toString(ci.longitude));
		}
		else {
		    bwr.write(ci.name + GeoDataStore.CSV_SEP +
			      GeoDataStore.CSV_SEP +
			      ci.country + GeoDataStore.CSV_SEP +
			      Long.toString(ci.latitude) + GeoDataStore.CSV_SEP +
			      Long.toString(ci.longitude));
		}
		bwr.newLine();
	    }
	    bwr.flush();
	    bwr.close();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void randomizeFile(File src, File tgt){
	try {
	    FileInputStream fis = new FileInputStream(src);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    Vector lines = new Vector();
	    String line = br.readLine();
	    while (line != null){
		lines.add(line);
		line = br.readLine();
	    }
	    fis.close();
	    BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tgt), "UTF-8"));
	    int lineCount = lines.size();
	    int randIndex;
	    while (lineCount > 0){
		// pick a random line in the set of source lines
		randIndex = (int)Math.floor(Math.random() * (lineCount-1));
		// write it to target
		bwr.write((String)lines.elementAt(randIndex));
		bwr.newLine();
		// remove it from source set
		lines.removeElementAt(randIndex);
		lineCount = lines.size();
	    }
	    bwr.flush();
	    bwr.close();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void initLog(String subjName, String subjID, String bn, File lf){
	subjectName = subjName;
	subjectID = subjID;
	blockNumber = bn;
	lineStart = subjectName + OUTPUT_CSV_SEP +
	    subjectID + OUTPUT_CSV_SEP +
	    blockNumber + OUTPUT_CSV_SEP;
	try {
	    logFile = lf;
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
	}
	catch (IOException ex){ex.printStackTrace();}
	writeHeaders();
    }
    
    void parseTrialFiles(File blockInfo){
	try {
	    FileInputStream fis = new FileInputStream(blockInfo);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    getIDICpairs(br.readLine(), br.readLine(), br.readLine());
	    String directory = blockInfo.getParent();
	    File iclf = new File(directory + File.separator + br.readLine());
	    File icmf = new File(directory + File.separator + br.readLine());
	    File ichf = new File(directory + File.separator + br.readLine());
	    fis.close();
	    /* file containing cities likely to give a low IC */
	    parseCityFile(iclf, citiesICL);
	    /* file containing cities likely to give a low IC */
	    parseCityFile(icmf, citiesICM);
	    /* file containing cities likely to give a low IC */
	    parseCityFile(ichf, citiesICH);
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void parseCityFile(File f, Vector cities){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    String line = br.readLine();
	    while (line != null){
		if (line.length() > 0){cities.add(new CityInfo(line));}
		line = br.readLine();
	    }
	    fis.close();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void getIDICpairs(String iclIDsequence, String icmIDsequence, String ichIDsequence){
	String[] iclids = iclIDsequence.split(GeoDataStore.CSV_SEP);
	for (int i=0;i<iclids.length;i++){
	    IDs4ICL.add(iclids[i]);
	}
	String[] icmids = icmIDsequence.split(GeoDataStore.CSV_SEP);
	for (int i=0;i<icmids.length;i++){
	    IDs4ICM.add(icmids[i]);
	}
	String[] ichids = ichIDsequence.split(GeoDataStore.CSV_SEP);
	for (int i=0;i<ichids.length;i++){
	    IDs4ICH.add(ichids[i]);
	}
	nbWantedIcLow = IDs4ICL.size();
	nbWantedIcMedium = IDs4ICM.size();
	nbWantedIcHigh = IDs4ICH.size();
	updateProgressSummary();
    }

    void writeHeaders(){
	try {
	    bwt.write("Name"+OUTPUT_CSV_SEP+
		      "SID"+OUTPUT_CSV_SEP+
		      "Block"+OUTPUT_CSV_SEP+
		      "Trial"+OUTPUT_CSV_SEP+
		      "ID"+OUTPUT_CSV_SEP+
		      "SLLat"+OUTPUT_CSV_SEP+
		      "SLLon"+OUTPUT_CSV_SEP+
		      "SLAlt"+OUTPUT_CSV_SEP+
		      "City"+OUTPUT_CSV_SEP+
		      "Region"+OUTPUT_CSV_SEP+
		      "Country"+OUTPUT_CSV_SEP+
		      "TLat"+OUTPUT_CSV_SEP+
		      "TLon"+OUTPUT_CSV_SEP+
		      "IC"+OUTPUT_CSV_SEP+ // index of confidence (function of region's area)
		      "Area"+OUTPUT_CSV_SEP+
		      "NWLat"+OUTPUT_CSV_SEP+
		      "NWLon"+OUTPUT_CSV_SEP+
		      "SELat"+OUTPUT_CSV_SEP+
		      "SELon"+OUTPUT_CSV_SEP);
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void nextStep(){
	if (!sessionStarted){return;}
	if (missingAtLeastOneTrial){// there is at least one trial left
	    eh.selectionRect.setVisible(false);
	    getNextCity();
	    startTrial();
	}
	else {
	    endSession();
	}
    }

    void getNextCity(){
	int nbMissingIcLow = nbWantedIcLow - countIcLow;
	int nbMissingIcMedium = nbWantedIcMedium - countIcMedium;
	int nbMissingIcHigh = nbWantedIcHigh - countIcHigh;
	int biggest = Math.max(nbMissingIcLow, Math.max(nbMissingIcMedium, nbMissingIcHigh));
	if (biggest == nbMissingIcLow){
	    System.out.println("Getting a city from ICL");
	    if (citiesICLindex < citiesICL.size()){
		currentCity = (CityInfo)citiesICL.elementAt(citiesICLindex);
		citiesICLindex++;
	    }
	    else if (citiesICMindex < citiesICM.size()){
		System.err.println("No more cities in ICL, looking in ICM");
		currentCity = (CityInfo)citiesICM.elementAt(citiesICMindex);
		citiesICMindex++;
	    }
	    else if (citiesICHindex < citiesICH.size()){
		System.err.println("No more cities in ICL and ICM, looking in ICH");
		currentCity = (CityInfo)citiesICH.elementAt(citiesICHindex);
		citiesICHindex++;
	    }
	    else {
		JOptionPane.showMessageDialog(demoView.getFrame(), "ERROR: no more cities");
		System.err.println("-------------ERROR: no more cities");
	    }
	}
	else if (biggest == nbMissingIcMedium){
	    System.out.println("Getting a city from ICM");
	    if (citiesICMindex < citiesICM.size()){
		currentCity = (CityInfo)citiesICM.elementAt(citiesICMindex);
		citiesICMindex++;
	    }
	    else if (citiesICLindex < citiesICL.size()){
		System.err.println("No more cities in ICM, looking in ICL");
		currentCity = (CityInfo)citiesICL.elementAt(citiesICLindex);
		citiesICLindex++;
	    }
	    else if (citiesICHindex < citiesICH.size()){
		System.err.println("No more cities in ICM and ICL, looking in ICH");
		currentCity = (CityInfo)citiesICH.elementAt(citiesICHindex);
		citiesICHindex++;
	    }
	    else {
		JOptionPane.showMessageDialog(demoView.getFrame(), "ERROR: no more cities");
		System.err.println("-------------ERROR: no more cities");
	    }
	}
	else {// biggest == nbMissingIcHigh
	    System.out.println("Getting a city from ICH");
	    if (citiesICHindex < citiesICH.size()){
		currentCity = (CityInfo)citiesICH.elementAt(citiesICHindex);
		citiesICHindex++;
	    }
	    else if (citiesICMindex < citiesICM.size()){
		System.err.println("No more cities in ICH, looking in ICM");
		currentCity = (CityInfo)citiesICM.elementAt(citiesICMindex);
		citiesICMindex++;
	    }
	    // don't go in ICL, it does not really make sense
	    else {
		JOptionPane.showMessageDialog(demoView.getFrame(), "ERROR: no more cities");
		System.err.println("-------------ERROR: no more cities");
	    }
	}
    }

    void startTrial(){
	getGlobalView();
	noLocationSpecifiedYet = true;
	//DRAW_SELECTION_RECT = false;
	if (currentCity.region == null){
	    questionText = IP1 + currentCity.name +
		COMMA + currentCity.country + IP2;
	}
	else {
	    questionText = IP1 + currentCity.name +
		COMMA + currentCity.region +
		COMMA + currentCity.country + IP2;
	}
	qpanel.say(questionText);
    }

    void drewRegion(long x1, long y1, long x2, long y2){
	noLocationSpecifiedYet = false;
	northWestLatitude = y1;
	northWestLongitude = x1;
	southEastLatitude = y2;
	southEastLongitude = x2;
    }

    void validateRegion(){
	if (!sessionStarted){return;}
 	evaluateAnswer(northWestLatitude, northWestLongitude, southEastLatitude, southEastLongitude);
	nextStep();
    }

    void evaluateAnswer(long nwLat, long nwLon, long seLat, long seLon){
	if (inside(nwLat, nwLon, seLat, seLon, currentCity.latitude, currentCity.longitude)){
	    // only take correct answers into account
	    long area = Math.abs(nwLat-seLat) * Math.abs(nwLon-seLon);
	    if (area >= ICH_AREA_MIN && area <= ICH_AREA_MAX){
		System.err.println("Got an ICH answer");
		if (countIcHigh < nbWantedIcHigh){
		    System.err.println("Registering ICH answer");
		    log(nwLat, nwLon, seLat, seLon, area, IC_HIGH, currentCity);
		    countIcHigh++;
		}
	    }
	    else if (area >= ICM_AREA_MIN && area <= ICM_AREA_MAX){
		System.err.println("Got an ICM answer");
		if (countIcMedium < nbWantedIcMedium){
		    System.err.println("Registering ICM answer");
		    log(nwLat, nwLon, seLat, seLon, area, IC_MEDIUM, currentCity);
		    countIcMedium++;
		}
	    }
	    else if (area >= ICL_AREA_MIN && area <= ICL_AREA_MAX){
		System.err.println("Got an ICL answer");
		if (countIcLow < nbWantedIcLow){
		    System.err.println("Registering ICL answer");
		    log(nwLat, nwLon, seLat, seLon, area, IC_LOW, currentCity);
		    countIcLow++;
		}
	    }
	}
	missingAtLeastOneTrial = (countIcLow < nbWantedIcLow)
	    || (countIcMedium < nbWantedIcMedium)
	    || (countIcHigh < nbWantedIcHigh);
	updateProgressSummary();
	vsm.repaintNow();
    }
    
    String getNextIDforIC(String ic){
	String res;
	if (ic.equals(IC_LOW)){
	    res = (String)IDs4ICL.firstElement();
	    IDs4ICL.removeElementAt(0);
	}
	else if (ic.equals(IC_MEDIUM)){
	    res = (String)IDs4ICM.firstElement();
	    IDs4ICM.removeElementAt(0);
	}
	else {//ic.equals(IC_HIGH)
	    res = (String)IDs4ICH.firstElement();
	    IDs4ICH.removeElementAt(0);
	}
	return res;
    }

    Point computeStartLocation(int id, CityInfo ci){
	Point res = TrialSeriesGenerator.getStartLocation(id, (int)ci.latitude, (int)ci.longitude, (int)GeoDataStore.TARGET_WIDTH);
	if (res != null){
	    return res;
	}
	else {
	    JOptionPane.showMessageDialog(demoView.getFrame(), "Error: could not compute a start location for city " + ci.name);
	    return null;
	}
    }

    void log(long nwLat, long nwLon, long seLat, long seLon, long area, String ic, CityInfo ci){
	String id = getNextIDforIC(ic);
	Point sl = computeStartLocation(Integer.parseInt(id), ci);
	if (sl == null){return;} // could not compute a start location for some reason or another... ignore this city
	try {
	    // Name, SID, Block
	    bwt.write(lineStart);
	    // Trial
	    bwt.write(trialCount + OUTPUT_CSV_SEP);
	    trialCount++;
	    // ID
	    bwt.write(id + OUTPUT_CSV_SEP);
	    try {
		// SLLat, SLLong, SLAlt, "0" is the altitude (always starting at max. zoom)
		bwt.write(Long.toString(sl.y)+ OUTPUT_CSV_SEP + Long.toString(sl.x) + OUTPUT_CSV_SEP + "0" + OUTPUT_CSV_SEP);
		// city, region, country
		if (ci.region == null){
		    bwt.write(ci.name + OUTPUT_CSV_SEP + OUTPUT_CSV_SEP + ci.country + OUTPUT_CSV_SEP);
		}
		else {
		    bwt.write(ci.name + OUTPUT_CSV_SEP + ci.region + OUTPUT_CSV_SEP + ci.country + OUTPUT_CSV_SEP);
		}
		// target latitude, longitude
		bwt.write(ci.latitude + OUTPUT_CSV_SEP + ci.longitude + OUTPUT_CSV_SEP);
		// index of confidence
		bwt.write(ic + OUTPUT_CSV_SEP);
		// answered region (area)
		bwt.write(Long.toString(area) + OUTPUT_CSV_SEP);
		// answered region (NW, SE)
		bwt.write(Long.toString(nwLat) + OUTPUT_CSV_SEP + Long.toString(nwLon) + OUTPUT_CSV_SEP +
			  Long.toString(seLat) + OUTPUT_CSV_SEP + Long.toString(seLon));
	    }
	    catch (NumberFormatException ex){
		ex.printStackTrace();
	    }
	    bwt.newLine();
	    bwt.flush();
	}
	catch (Exception ex){ex.printStackTrace();}
    }

    static boolean inside(long nwLat, long nwLon, long seLat, long seLon, long targetLat, long targetLon){
	return (targetLat <= nwLat && targetLon <= seLon &&
		targetLat >= seLat && targetLon >= nwLon);
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	if (demoCamera.getAltitude() < 7000){
	    Float alt = new Float(demoCamera.getAltitude()+demoCamera.getFocal());
	    vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt,demoCamera. getID());
	}
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	if (demoCamera.getAltitude() > 100){
	    Float alt = new Float(-(demoCamera.getAltitude()+demoCamera.getFocal())/2.0f);
	    vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, demoCamera.getID());
	}
    }

    /*direction should be one of ZGRViewer.MOVE_* */
    void translateView(short direction){
	LongPoint trans;
	long[] rb = demoView.getVisibleRegion(demoCamera);
	if (direction == ZLWorldTask.MOVE_UP){
	    long qt = Math.round((rb[1]-rb[3])/3);
	    trans = new LongPoint(0, qt);
	}
	else if (direction == ZLWorldTask.MOVE_DOWN){
	    long qt = Math.round((rb[3]-rb[1])/3);
	    trans = new LongPoint(0, qt);
	}
	else if (direction == ZLWorldTask.MOVE_RIGHT){
	    long qt = Math.round((rb[2]-rb[0])/3);
	    trans = new LongPoint(qt, 0);
	}
	else {// direction == ZLWorldTask.MOVE_LEFT
	    long qt = Math.round((rb[0]-rb[2])/3);
	    trans = new LongPoint(qt, 0);
	}
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, demoCamera.getID());
    }

    void getGlobalView(){
 	Float alt = new Float(START_ALTITUDE - demoCamera.getAltitude());
	LongPoint trans = new LongPoint(-demoCamera.posx, -demoCamera.posy);
	Vector v = new Vector();
	v.add(alt);
	v.add(trans);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_TRANS_SIG, v, demoCamera.getID());
    }

    void updateProgressSummary(){
	progressSummary  = countIcLow+ "/" + nbWantedIcLow + "    "
	    + countIcMedium + "/" + nbWantedIcMedium + "    "
	    + countIcHigh + "/" + nbWantedIcHigh;
    }

    boolean SHOW_PROGRESS = false;
//     boolean DRAW_SELECTION_RECT = false;

    void switchShowProgress(){
	SHOW_PROGRESS = !SHOW_PROGRESS;
	vsm.repaintNow();
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (SHOW_PROGRESS){
	    g2d.setColor(Color.RED);
	    g2d.drawString(progressSummary, 20, 40);
	}
    }

    public static void main(String[] args){
 	new LocateTask2();
    }
    
}
