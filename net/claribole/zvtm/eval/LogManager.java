/*   FILE: LogManager.java
 *   DATE OF CREATION:  Mon Apr 10 14:28:35 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.Vector;

import com.xerox.VTM.engine.SwingWorker;
import net.claribole.zvtm.engine.Java2DPainter;

class LogManager implements Java2DPainter {

    static final String LOG_FILE_EXT = ".csv";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;

    static final String PSBTC = "PRESS SPACE BAR TO CONTINUE";
    static final String EOS = "END OF SESSION";
    static final String IFST = "INITIALIZING FIRST TRIAL...  (PLEASE WAIT)";
    static final String INXT = "INITIALIZING NEXT TRIAL...  (PLEASE WAIT)";
    String msg = IFST;

    static final int WARN_MSG_DELAY = 500;
    static final String TARGET_ERR = "ERROR: Target is not within selection region";

    static final double MAX_D = Math.sqrt(Math.pow(64000,2) + Math.pow(32000,2));

    /* codes for technique */
    static final String ZL = "ZL";     // Zoom + Lens
    static final String PZ = "PZVC";   // Pan + Zoom centered on view
    static final String RZ = "RZ";     // Region zooming
    static final String PZA = "PZCC";  // Pan + Zoom centered on cursor
    static final String PZL = "ALL";   // Pan + Zoom (centered on view) & Zoom + Lens
    static final String DZ = "DZ";     // Pan + Discrete Zoom centered on cursor
    static final String DZA = "DZA";   // Pan + Discrete Zoom centered on cursor with animated transitions

    /* codes for lens status */
    static final String NO_LENS = "0";
    static final String ZOOMIN_LENS = "1";
    static final String ZOOMOUT_LENS = "2";
    String lensStatus = NO_LENS;

    static final String NaN = "NaN";
    String lensxS = NaN;
    String lensyS = NaN;
    String lensmmS = TrialInfo.doubleFormatter(ZLWorldTask.DEFAULT_MAG_FACTOR);

    static final double TARGET_WIDTH = (new Long(GeoDataStore.TARGET_WIDTH)).doubleValue();

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);

    ZLWorldTask application;

    String subjectID;
    String subjectName;
    String techniqueName;
    String blockNumber;
    int trialCount;
    String trialCountStr;
    double distance; // distance from start location to target
    String distanceStr;
    int nbZIOswitches;
    int nbErrors;

    long trialStartTime;
    long trialDuration;

    String lineStart;
    File logFile;
    File cinematicFile;
    BufferedWriter bwt, bwc;

    boolean sessionStarted = false;
    boolean trialStarted = false;

    boolean trialInitInProgress = false;

    TrialInfo[] trials;

    InstructionsManager im;
    String[] instructionText = {"", "", "", ""};

    LogManager(ZLWorldTask app){
	this.application = app;
	im = new InstructionsManager(app);
	this.application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_DISTORTION);
    }

//     void initInstructionsPanel(){
// 	ip = new InstructionsPanel(this.application.INSTRUCTIONS_X, this.application.INSTRUCTIONS_Y,
// 				   this.application.INSTRUCTIONS_W, this.application.INSTRUCTIONS_H);
// 	if (this.application.SHOW_INSTRUCTIONS){
// 	    im.setVisible(true);
// 	}
// 	((JFrame)application.demoView.getFrame()).toFront();
//     }

    void startSession(){
	im.say(" ");
	JFileChooser fc = new JFileChooser(new File(TRIAL_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Trial File");
	int returnVal= fc.showOpenDialog(application.demoView.getFrame());
	File trialFile = null;
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    trialFile = fc.getSelectedFile();
	}
	else {
	    im.say(LocateTask.PSTS);
	    return;
	}
	init(application.technique, trialFile);
	sessionStarted = true;
    }

    void init(short technique, File tf){
	trialCount = -1;
	techniqueName = getTechniqueName(technique);
	parseTrialFile(tf);
	try {
	    logFile = initLogFile(subjectID+"-"+techniqueName+"-trial-block"+blockNumber, LOG_DIR);
	    cinematicFile = initLogFile(subjectID+"-"+techniqueName+"-cinematic-block"+blockNumber, LOG_DIR);
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
	    bwc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cinematicFile), "UTF-8"));
	}
	catch (IOException ex){ex.printStackTrace();}
	writeHeaders();
	trialStarted = false;
	trialInitInProgress = true;
	System.gc();
	application.demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	initNextTrial();
    }

    // returns block number
    void parseTrialFile(File f){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    Vector v = new Vector();
	    String line = br.readLine();
	    line = br.readLine(); // ignore first line (column headers)
	    initLineStart(line);
	    while (line != null){
		v.add(new TrialInfo(line));
		line = br.readLine();
	    }
	    trials = new TrialInfo[v.size()];
	    for (int i=0;i<trials.length;i++){
		trials[i] = (TrialInfo)v.elementAt(i);
	    }
	    v.clear();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    // init first columns of each line in output trials file
    void initLineStart(String anInputTrialLine){
	String[] items = anInputTrialLine.split(GeoDataStore.CSV_SEP);
	subjectName = items[0];
	subjectID = items[1];
	blockNumber = items[2];
	lineStart = subjectName + OUTPUT_CSV_SEP +
	    subjectID + OUTPUT_CSV_SEP +
	    techniqueName + OUTPUT_CSV_SEP +
	    blockNumber + OUTPUT_CSV_SEP;
    }

    void endSession(){
	msg = EOS;
	application.demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	sessionStarted = false;
	try {
	    bwt.flush();
	    bwc.flush();
	    bwt.close();
	    bwc.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	im.say(" ");
    }

    void writeHeaders(){
	try {
	    // trial column headers
	    bwt.write("Name" + OUTPUT_CSV_SEP +
		      "SID" + OUTPUT_CSV_SEP +
		      "Technique" + OUTPUT_CSV_SEP +
		      "Block" + OUTPUT_CSV_SEP +
		      "Trial" + OUTPUT_CSV_SEP +
		      "ID" + OUTPUT_CSV_SEP +
		      "Distance" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP +
		      "Nb Switches" + OUTPUT_CSV_SEP +
		      "Nb Errors" + OUTPUT_CSV_SEP +
		      "City" + OUTPUT_CSV_SEP +
		      "Region" + OUTPUT_CSV_SEP +
		      "Country" + OUTPUT_CSV_SEP +
		      "TLat" + OUTPUT_CSV_SEP +
		      "TLon" + OUTPUT_CSV_SEP +
		      "IC" + OUTPUT_CSV_SEP +
		      "Area" + OUTPUT_CSV_SEP +
		      "NWLat" + OUTPUT_CSV_SEP +
		      "NWLon" + OUTPUT_CSV_SEP +
		      "SELat" + OUTPUT_CSV_SEP +
		      "SELon");
	    bwt.newLine();
	    bwt.flush();
	    // cinematic file header (misc. info)
	    bwc.write("# Name" + OUTPUT_CSV_SEP + subjectName);
	    bwc.newLine();
	    bwc.write("# SID" + OUTPUT_CSV_SEP + subjectID);
	    bwc.newLine();
	    bwc.write("# Technique" + OUTPUT_CSV_SEP + techniqueName);
	    bwc.newLine();
	    bwc.write("# Block" + OUTPUT_CSV_SEP + blockNumber);
	    bwc.newLine();
	    bwc.write("# vw" + OUTPUT_CSV_SEP + application.panelWidth);
	    bwc.newLine();
	    bwc.write("# vh" + OUTPUT_CSV_SEP + application.panelHeight);
	    bwc.newLine();
	    // cinematic column headers
	    bwc.write("Trial" + OUTPUT_CSV_SEP +
		      "Distance" + OUTPUT_CSV_SEP +
		      "Lens" + OUTPUT_CSV_SEP +
		      "cx" + OUTPUT_CSV_SEP +
		      "cy" + OUTPUT_CSV_SEP +
		      "cz" + OUTPUT_CSV_SEP +
		      "lx" + OUTPUT_CSV_SEP +
		      "ly" + OUTPUT_CSV_SEP +
		      "mm" + OUTPUT_CSV_SEP +
		      "time");
	    bwc.newLine();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void startTrial(){
	trialStarted = true;
	nbZIOswitches = 0;
	nbErrors = 0;
	application.eh.zoomDirection = WorldTaskEventHandler.NOT_ZOOMING;
 	application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_DISTORTION);
	trialStartTime = System.currentTimeMillis();
    }

    void initNextTrial(){
	trialCount++;
	distance = Math.sqrt(Math.pow(trials[trialCount].targetLatitude - trials[trialCount].startLatitude, 2) +
			     Math.pow(trials[trialCount].targetLongitude - trials[trialCount].startLongitude, 2));
	distanceStr = TrialInfo.doubleFormatter(distance);
	application.demoCamera.posx = trials[trialCount].startLongitude;
	application.demoCamera.posy = trials[trialCount].startLatitude;
	application.demoCamera.updatePrecisePosition();
	application.demoCamera.altitude = trials[trialCount].startAltitude;
	application.eh.cameraMoved();
	application.vsm.repaintNow();
	showNextTrialInfo();
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    while (application.ewmm.requestQueue.size() > 0){
			sleep(500);
		    }
		    msg = PSBTC + " - Trial " + (trialCount+1) + " of " + trials.length;
		    LogManager.this.trialInitInProgress = false;
		    application.vsm.repaintNow();
		    return null; 
		}
	    };
	worker.start();
    }

    void endTrial(){
	trialDuration = System.currentTimeMillis() - trialStartTime;
	trialStarted = false;
	writeTrial();
	flushCinematic();
	if (trialCount +1 >= trials.length){
	    endSession();
	}
	else {// there it at least one trila left
	    System.gc();
	    trialInitInProgress = true;
	    msg = INXT;
	    application.demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	}
    }

    void nextStep(){
	if (!sessionStarted || trialInitInProgress){return;}
	if (trialStarted){// subject wants to end the trial
	    if (targetWithinRange()){
		endTrial();
		if (trialCount+1 < trials.length){// there is at least one trial left
		    initNextTrial();
		}
	    }
	    else {
		wrongTarget();
	    }
	}
	else {// subject is in between two trials
	    if (trialCount < trials.length){// there is at least one trial left
		trialCountStr = Integer.toString(trialCount);
		startTrial();
	    }
	}
    }

    boolean targetWithinRange(){
	// test whether target is within bounding box
	// defined by SELECTION_RECT or not
	// return true only if camera is at lowest altitude possible
	return (application.cameraOnFloor &&
		(Math.abs(application.demoCamera.posx - trials[trialCount].targetLongitude) < application.SELECTION_RECT_HW) &&
		(Math.abs(application.demoCamera.posy - trials[trialCount].targetLatitude) < application.SELECTION_RECT_HH));
    }

    void wrongTarget(){
	nbErrors++;
 	im.warn(TARGET_ERR, instructionText, WARN_MSG_DELAY);
    }

    static final String GTIP1 = "Go to:";
    static final String GTIP2 = "In:";
    static final String GTIP3 = ", ";

    void showNextTrialInfo(){
	if (trials[trialCount].targetRegion == null){
	    instructionText[0] = GTIP1;
	    instructionText[1] = trials[trialCount].targetName;
	    instructionText[2] = GTIP2;
	    instructionText[3] = trials[trialCount].targetCountry;
	}
	else {
	    instructionText[0] = GTIP1;
	    instructionText[1] = trials[trialCount].targetName + GTIP3 + trials[trialCount].targetRegion;
	    instructionText[2] = GTIP2;
	    instructionText[3] = trials[trialCount].targetCountry;
	}
	im.say(instructionText);
    }

    void writeTrial(){
	try {
	    // subject name + subject ID + technique
	    bwt.write(lineStart);
	    // trial + D + time + nb switches + nb errors
	    bwt.write(trialCountStr + OUTPUT_CSV_SEP +
		      Math.round(Math.log(1+distance/TARGET_WIDTH)/Math.log(2)) + OUTPUT_CSV_SEP +
		      distanceStr + OUTPUT_CSV_SEP +
		      Long.toString(trialDuration) + OUTPUT_CSV_SEP +
		      Integer.toString(nbZIOswitches) + OUTPUT_CSV_SEP +
		      Integer.toString(nbErrors) + OUTPUT_CSV_SEP +		      
		      trials[trialCount].cityInfo);
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeCinematic(){
	try {
// 	    // subject name + subject ID + technique
// 	    bwc.write(lineStart);
	    // trial + D + time + nb switches + nb errors
	    bwc.write(trialCountStr + OUTPUT_CSV_SEP +
		      distanceStr + OUTPUT_CSV_SEP +
		      lensStatus + OUTPUT_CSV_SEP +
		      Long.toString(application.demoCamera.posx) + OUTPUT_CSV_SEP +
		      Long.toString(application.demoCamera.posy) + OUTPUT_CSV_SEP +
		      TrialInfo.floatFormatter(application.demoCamera.altitude) + OUTPUT_CSV_SEP +
		      lensxS + OUTPUT_CSV_SEP + lensyS + OUTPUT_CSV_SEP + lensmmS +
		      OUTPUT_CSV_SEP + Long.toString(System.currentTimeMillis()-trialStartTime));
	    bwc.newLine();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void flushCinematic(){
	try {
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void switchedZoomDirection(){
	nbZIOswitches++;
    }

    void lensPositionChanged(boolean write){
	lensxS = Integer.toString(application.lens.lx);
	lensyS = Integer.toString(application.lens.ly);
	if (write && trialStarted){
	    writeCinematic();
	}
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	im.drawFrame(g2d, viewWidth, viewHeight);
	im.writeInstructions(g2d, viewWidth, viewHeight);
	g2d.setColor(Color.BLACK);
	g2d.setComposite(acST);
	g2d.fillRect(0, viewHeight / 2 - 100, viewWidth, 220);
	g2d.setComposite(com.xerox.VTM.glyphs.Translucent.acO);
	g2d.setColor(Color.WHITE);
	g2d.drawString(msg, viewWidth/2 - 75, viewHeight/2);
    }

    static File initLogFile(String fileName, String dirName){
	String outputFile = dirName + File.separator + fileName + LOG_FILE_EXT;
	File file = new File(outputFile);
	int i = 0;
	while (file.exists()){
	    i++;
	    file = new File(outputFile.substring(0,outputFile.length()-4) + "-" + i + LOG_FILE_EXT);
	}
	return file;
    }

    static String getTechniqueName(short t){
	switch (t){
	case ZLWorldTask.ZL_TECHNIQUE:{return "ZL";}
	case ZLWorldTask.PZ_TECHNIQUE:{return "PZVC";}
	case ZLWorldTask.PZL_TECHNIQUE:{return "PZZL";}
	case ZLWorldTask.PZA_TECHNIQUE:{return "PZCC";}
	case ZLWorldTask.DZ_TECHNIQUE:{return "DZ";}
	case ZLWorldTask.DZA_TECHNIQUE:{return "DZA";}
	case ZLWorldTask.RZ_TECHNIQUE:{return "RZ";}
	}
	return "";
    }

}