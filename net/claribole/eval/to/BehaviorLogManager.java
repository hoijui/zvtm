/*   FILE: BehaviorLogManager.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.PostAnimationAction;

import com.xerox.VTM.glyphs.Glyph;

public class BehaviorLogManager implements PostAnimationAction {

    static final String LOG_FILE_EXT = ".csv";
    static final String INPUT_CSV_SEP = ";";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;

    static final String PBTC = "PRESS BUTTON TO CONTINUE";
    static final String PSTS = "PRESS S TO START";
    static final String EOS = "END OF SESSION";
    static final String C_BT = "CONTINUE";  // Continue button displayed between trials
    static final String TRIAL_STR = "Trial ";
    static final String OF_STR = " of ";

    static final String ERR_MSG = "ERROR";

    static final int MIN_TIME_INSIDE_NEXT_TRIAL_BUTTON = 1500;

    BehaviorEval application;

    BehaviorBlock block;

    String subjectID;
    String subjectName;
    String behaviorName;
    String abstractTargetLocation;
    String blockNumber;
    int trialCount;
    String trialCountStr;
    int errorCount = 0;

    String directionStr;
    String radiusStr;

    Glyph target;

    long trialEndTime, trialStartTime;

    boolean sessionStarted = false;
    boolean trialStarted = false;

    boolean firstTOWAcquisition = true;
    long timeToAcquire = 0;
    int acquisitionCount = 0;

    String lineStart;
    File logFile;
    File cinematicFile;
    BufferedWriter bwt, bwc;

    BehaviorInstructionsManager im;
    boolean waitingForCursorToEnterButton = false;

    BehaviorLogManager(BehaviorEval app){
	this.application = app;
	im = new BehaviorInstructionsManager(this.application, this);
	application.mView.setJava2DPainter(im, Java2DPainter.FOREGROUND);
    }
    
    void startSession(){
	init();
	sessionStarted = true;
    }

    void init(){
	trialCount = -1;
	subjectName = JOptionPane.showInputDialog("Subject Name");
 	if (subjectName == null){im.say(PSTS);return;}
	subjectID = JOptionPane.showInputDialog("Subject ID");
 	if (subjectID == null){im.say(PSTS);return;}
 	blockNumber = JOptionPane.showInputDialog("Block number");
  	if (blockNumber == null){im.say(PSTS);return;}
 	initLineStart();
	JFileChooser fc = new JFileChooser(new File(BehaviorLogManager.TRIAL_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Trial File");
	int returnVal= fc.showOpenDialog(application.mView.getFrame());
	File trialFile = null;
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    trialFile = fc.getSelectedFile();
	    parseTrials(trialFile);
	}
	else {
 	    im.say(PSTS);
	    return;
	}
	try {
	    logFile = initLogFile(subjectID+"-"+behaviorName+"-"+abstractTargetLocation+"-"+BehaviorEval.getBackgroundType(application.backgroundType)+"-trial-block"+blockNumber, LOG_DIR);
 	    cinematicFile = initLogFile(subjectID+"-"+behaviorName+"-"+abstractTargetLocation+"-"+BehaviorEval.getBackgroundType(application.backgroundType)+"-cinematic-block"+blockNumber, LOG_DIR);
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
 	    bwc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cinematicFile), "UTF-8"));
	}
	catch (IOException ex){ex.printStackTrace();}
	writeHeaders();
	trialStarted = false;
	System.gc();
 	initNextTrial();
    }

    void parseTrials(File f){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    String line = br.readLine();
	    if (line != null && line.length() > 0){
		block = new BehaviorBlock(line, abstractTargetLocation);
	    }
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeHeaders(){
	try {
	    // trial file column headers
	    bwt.write("Name" + OUTPUT_CSV_SEP +
		      "SID" + OUTPUT_CSV_SEP +
		      "Technique" + OUTPUT_CSV_SEP +
		      "Background" + OUTPUT_CSV_SEP +
		      "Block" + OUTPUT_CSV_SEP +
		      "Trial" + OUTPUT_CSV_SEP +
		      "Direction" + OUTPUT_CSV_SEP +
		      "Radius" + OUTPUT_CSV_SEP +
		      "AcqTime" + OUTPUT_CSV_SEP +
		      "AcqCount" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP +
		      "Errors");
	    bwt.newLine();
	    bwt.flush();
	    // cinematic file header (misc. info)
	    bwc.write("# Name" + OUTPUT_CSV_SEP + subjectName);
	    bwc.newLine();
	    bwc.write("# SID" + OUTPUT_CSV_SEP + subjectID);
	    bwc.newLine();
	    bwc.write("# Technique" + OUTPUT_CSV_SEP + behaviorName);
	    bwc.newLine();
	    bwc.write("# Background" + OUTPUT_CSV_SEP + BehaviorEval.getBackgroundType(application.backgroundType));
	    bwc.newLine();
	    bwc.write("# Block" + OUTPUT_CSV_SEP + blockNumber);
	    bwc.newLine();
	    bwc.write("# vw" + OUTPUT_CSV_SEP + application.panelWidth);
	    bwc.newLine();
	    bwc.write("# vh" + OUTPUT_CSV_SEP + application.panelHeight);
	    bwc.newLine();
	    // cinematic column headers
	    bwc.write("Trial" + OUTPUT_CSV_SEP +
		      "Direction" + OUTPUT_CSV_SEP +
		      "Radius" + OUTPUT_CSV_SEP +
		      "mx" + OUTPUT_CSV_SEP +
		      "my" + OUTPUT_CSV_SEP +
		      "cx" + OUTPUT_CSV_SEP +
		      "cy" + OUTPUT_CSV_SEP +
		      "px" + OUTPUT_CSV_SEP +
		      "py" + OUTPUT_CSV_SEP +
		      "inPortal" + OUTPUT_CSV_SEP +
		      "Time");
	    bwc.newLine();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    // init first columns of each line in output trials file
    void initLineStart(){
	lineStart = subjectName + OUTPUT_CSV_SEP +
	    subjectID + OUTPUT_CSV_SEP +
	    behaviorName + OUTPUT_CSV_SEP +
	    BehaviorEval.getBackgroundType(application.backgroundType) + OUTPUT_CSV_SEP +
	    blockNumber + OUTPUT_CSV_SEP;
    }

    void validateTarget(){
	endTrial();
    }

    void error(){
	errorCount++;
	im.warn(ERR_MSG);
    }

    void acquiredTOW(long time){
	acquisitionCount++;
	if (firstTOWAcquisition){
	    timeToAcquire = time-trialStartTime; 
	    firstTOWAcquisition = false;
	    block.timeToAcquire[trialCount] = timeToAcquire;
	}
    }
    
    void initNextTrial(){
	if (target != null){
	    if (application.backgroundType == BehaviorEval.BACKGROUND_WORLDMAP){target.setVisible(false);}
	    else {target.setColor(BehaviorEval.DISTRACTOR_COLOR);}
	}
	incTrialCount();
	errorCount = 0;
	firstTOWAcquisition = true;
	acquisitionCount = 0;
	if (trialCount > 0){// ask for a postponed camera reset in case the camera is in the
	    resetRequest = true; // middle of an animation at the time of the first reset
	}
	directionStr = block.direction[trialCount];
	radiusStr = block.radius[trialCount];
	if (abstractTargetLocation.equals(BehaviorBlock.TARGET_MAIN_VIEWPORT)){
	    target = getTarget(directionStr, radiusStr);
	}
	else {// abstractTargetLocation.equals(BehaviorBlock.TARGET_TRAILING_WIDGET)
	    block.moveCamera(trialCount, application.mCamera);
	}
	im.say(TRIAL_STR + String.valueOf(trialCount+1) + OF_STR + String.valueOf(block.direction.length));
	waitingForCursorToEnterButton = true;
    }

    Glyph getTarget(String direction, String radius){
	if (radius.equals(BehaviorBlock.RADIUS_R1)){
	    if (direction.equals(BehaviorBlock.DIRECTION_NW_STR)){
		return application.NW_TARGET_R1;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_NE_STR)){
		return application.NE_TARGET_R1;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SE_STR)){
		return application.SE_TARGET_R1;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SW_STR)){
		return application.SW_TARGET_R1;
	    }
	    else {// DIRECTION_TW_STR: target is observed region inside trailing widget 
		return null;
	    }
	}
	else if (radius.equals(BehaviorBlock.RADIUS_R2)){
	    if (direction.equals(BehaviorBlock.DIRECTION_NW_STR)){
		return application.NW_TARGET_R2;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_NE_STR)){
		return application.NE_TARGET_R2;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SE_STR)){
		return application.SE_TARGET_R2;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SW_STR)){
		return application.SW_TARGET_R2;
	    }
	    else {// DIRECTION_TW_STR: target is observed region inside trailing widget 
		return null;
	    }
	}
	else if (radius.equals(BehaviorBlock.RADIUS_R3)){
	    if (direction.equals(BehaviorBlock.DIRECTION_NW_STR)){
		return application.NW_TARGET_R3;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_NE_STR)){
		return application.NE_TARGET_R3;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SE_STR)){
		return application.SE_TARGET_R3;
	    }
	    else if (direction.equals(BehaviorBlock.DIRECTION_SW_STR)){
		return application.SW_TARGET_R3;
	    }
	    else {// DIRECTION_TW_STR: target is observed region inside trailing widget 
		return null;
	    }
	}
	else {// DIRECTION_TW_STR: target is observed region inside trailing widget, no radius specified
	    return null;
	}
    }

    void incTrialCount(){
	trialCount++;
	trialCountStr = String.valueOf(trialCount);
    }

    boolean resetRequest = false;

    public void animationEnded(Object target, short type, String dimension){
	if (resetRequest){
	    resetRequest = false;
	}
    }

    void startTrial(){
	waitingForCursorToEnterButton = false;
	im.say(null);
	if (target != null){
	    if (application.backgroundType == BehaviorEval.BACKGROUND_WORLDMAP){target.setVisible(true);}
	    else {target.setColor(BehaviorEval.TARGET_COLOR);}
	}
	trialStarted = true;
	trialStartTime = System.currentTimeMillis();
	resetRequest = false; // make sure there is no orphan camera reset request
    }

    void endTrial(){
	trialEndTime = System.currentTimeMillis() - trialStartTime;
	trialStarted = false;
	writeTrial();
	if (trialCount < block.nbTrials-1){
	    initNextTrial();
	}
	else {
	    endSession();
	}
    }

    void endSession(){
	sessionStarted = false;
	try {
	    bwt.close();
	    bwc.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	im.say(EOS);
    }

    void writeTrial(){
	try {
	    bwt.write(lineStart +
		      trialCountStr + OUTPUT_CSV_SEP +
		      directionStr + OUTPUT_CSV_SEP +
		      radiusStr + OUTPUT_CSV_SEP +
		      String.valueOf(block.timeToAcquire[trialCount]) + OUTPUT_CSV_SEP +
		      String.valueOf(acquisitionCount) + OUTPUT_CSV_SEP +
		      String.valueOf(trialEndTime) + OUTPUT_CSV_SEP +
		      String.valueOf(errorCount));
	    bwt.newLine();
	    bwt.flush();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    static final String CURSOR_INSIDE_PORTAL_STR = "1";
    static final String CURSOR_OUTSIDE_PORTAL_STR = "0";
    long cinematicTime = 0;

    void writeCinematic(int jpx, int jpy, int px, int py){
	cinematicTime = System.currentTimeMillis() - trialStartTime;
	try {
	    bwc.write(trialCountStr + OUTPUT_CSV_SEP +
		      directionStr + OUTPUT_CSV_SEP +
		      radiusStr + OUTPUT_CSV_SEP +
		      String.valueOf(jpx) + OUTPUT_CSV_SEP +
		      String.valueOf(jpy) + OUTPUT_CSV_SEP +
		      String.valueOf(application.mCamera.posx) + OUTPUT_CSV_SEP +
		      String.valueOf(application.mCamera.posy) + OUTPUT_CSV_SEP +
		      String.valueOf(px) + OUTPUT_CSV_SEP +
		      String.valueOf(py) + OUTPUT_CSV_SEP +
		      ((application.eh.mouseInsideOverview) ? CURSOR_INSIDE_PORTAL_STR : CURSOR_OUTSIDE_PORTAL_STR) + OUTPUT_CSV_SEP +
		      String.valueOf(cinematicTime));
	    bwc.newLine();
	}
	catch(IOException ex){ex.printStackTrace();}
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

}