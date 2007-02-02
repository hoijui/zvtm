/*   FILE: AcquireLogManager.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

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

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.PostAnimationAction;

public class AcquireLogManager implements PostAnimationAction {

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

    static final int MIN_DELAY_BETWEEN_TRIALS = 500;

    AcquireEval application;

    AcquireBlock block;

    String subjectID;
    String subjectName;
    String techniqueName;
    String blockNumber;
    int trialCount;
    String trialCountStr;
    int targetCount;
    String targetCountStr;

    String directionStr;
    String IDStr;

    boolean sessionStarted = false;
    boolean trialStarted = false;

    String lineStart;
    File logFile;
    File cinematicFile;
    BufferedWriter bwt, bwc;

    AcquireInstructionsManager im;

    AcquireLogManager(AcquireEval app){
	this.application = app;
	im = new AcquireInstructionsManager(this.application, this);
	application.mView.setJava2DPainter(im, Java2DPainter.AFTER_PORTALS);
    }
    
    void startSession(){
	init(application.technique);
	sessionStarted = true;
    }

    void init(short technique){
	trialCount = -1;
	targetCount = -1;
	techniqueName = getTechniqueName(technique);
	subjectName = JOptionPane.showInputDialog("Subject Name");
 	if (subjectName == null){im.say(PSTS);return;}
	subjectID = JOptionPane.showInputDialog("Subject ID");
 	if (subjectID == null){im.say(PSTS);return;}
 	blockNumber = JOptionPane.showInputDialog("Block number");
  	if (blockNumber == null){im.say(PSTS);return;}
 	initLineStart();
	JFileChooser fc = new JFileChooser(new File(AcquireLogManager.TRIAL_DIR_FULL));
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
	    logFile = initLogFile(subjectID+"-"+techniqueName+"-trial-block"+blockNumber, LOG_DIR);
 	    cinematicFile = initLogFile(subjectID+"-"+techniqueName+"-cinematic-block"+blockNumber, LOG_DIR);
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
		block = new AcquireBlock(line);
	    }
	    else {
		im.say(PSTS);
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
		      "Block" + OUTPUT_CSV_SEP +
		      "Trial" + OUTPUT_CSV_SEP +
		      "Target" + OUTPUT_CSV_SEP +
		      "Direction" + OUTPUT_CSV_SEP +
		      "ID" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP);
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
		      "Target" + OUTPUT_CSV_SEP +
		      "Direction" + OUTPUT_CSV_SEP +
		      "ID" + OUTPUT_CSV_SEP +
		      "mx" + OUTPUT_CSV_SEP +
		      "my" + OUTPUT_CSV_SEP +
		      "cx" + OUTPUT_CSV_SEP +
		      "cy" + OUTPUT_CSV_SEP +
		      "ox" + OUTPUT_CSV_SEP +
		      "oy" + OUTPUT_CSV_SEP +
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
	    techniqueName + OUTPUT_CSV_SEP +
	    blockNumber + OUTPUT_CSV_SEP;
    }

    int selectionRegionSize, selectionRegionHSize;

    boolean validTarget(long cursorx, long cursory, Glyph g){
	return (g == application.target
		//|| Math.sqrt(Math.pow(cursorx-application.mCamera.posx,2)+Math.pow(cursory-application.mCamera.posy,2)) <= selectionRegionHSize
		)
	    && (Math.sqrt(Math.pow(application.target.vx-application.mCamera.posx,2)+Math.pow(application.target.vy-application.mCamera.posy,2)) + block.size[trialCount] <= selectionRegionHSize);
    }
    
    void initNextTrial(){
	incTrialCount();
	resetTargetCount();
	// reset camera to (0,0) in virtual space
	resetCamera();
	if (trialCount > 0){// ask for a postponed camera reset in case the camera is in the
	    resetRequest = true; // middle of an animation at the time of the first reset
	}
	// reinitialize target at first location in right direction and distance
	application.mSpace.destroyGlyph(application.target);  // destroy target and create new one instead of moving existing one
	application.target = block.moveTarget(trialCount, 0, 0);
	application.vsm.addGlyph(application.target, application.mSpace); // to circumvent a problem in ZVTM's picking mechanism 
                                                              // that does not detect cursor exiting a glyph that is not in the viewport
	application.target.setVisible(false);
	selectionRegionSize = Math.round(block.size[trialCount] * AcquireEval.SELECTION_REGION_SIZE_FACTOR * 2);
	selectionRegionHSize = selectionRegionSize / 2;
	directionStr = AcquireBlock.getDirection(block.direction[trialCount]);
	IDStr = String.valueOf(block.ID[trialCount]);
	im.say(TRIAL_STR + String.valueOf(trialCount+1) + OF_STR + String.valueOf(block.direction.length), MIN_DELAY_BETWEEN_TRIALS);
    }

    void resetTargetCount(){
	targetCount = 0;
	targetCountStr = "0";
    }

    void incTargetCount(){
	targetCount++;
	targetCountStr = String.valueOf(targetCount);
    }

    void incTrialCount(){
	trialCount++;
	trialCountStr = String.valueOf(trialCount);
    }

    boolean resetRequest = false;

    public void animationEnded(Object target, short type, String dimension){
	if (resetRequest){
	    resetRequest = false;
	    resetCamera();
	}
    }

    void resetCamera(){
	application.mCamera.moveTo(0, 0);
	application.centerOverview(false);
    }

    static final int NB_TARGETS_PER_TRIAL = 1;
    long previousTime, currentTime, trialStartTime;
    long[] intermediateTimes = new long[NB_TARGETS_PER_TRIAL];

    void startTrial(){
	im.say(null);
	application.target.setVisible(true);
	trialStarted = true;
	trialStartTime = System.currentTimeMillis();
	previousTime = trialStartTime;
	resetRequest = false; // make sure there is no orphan camera reset request
    }

    void nextTarget(){
	currentTime = System.currentTimeMillis();
	intermediateTimes[targetCount] = currentTime - previousTime;
	previousTime = currentTime;
	incTargetCount();
	if (targetCount < NB_TARGETS_PER_TRIAL){
	    // move target to next location (offset)
	    application.mSpace.destroyGlyph(application.target);  // destroy target and create new one instead of moving existing one
	    application.target = block.moveTarget(trialCount, application.mCamera.posx, application.mCamera.posy);
	    application.vsm.addGlyph(application.target, application.mSpace); // to circumvent a problem in ZVTM's picking mechanism
	    application.target.setVisible(false);                 // that does not detect cursor exiting a glyph that is not in the viewport
	}
	else {// this was the last target, end the trial
	    endTrial();
	}
    }

    void endTrial(){
	trialStarted = false;
	try {
	    for (int i=0;i<NB_TARGETS_PER_TRIAL;i++){
		bwt.write(lineStart +
			  trialCountStr + OUTPUT_CSV_SEP +
			  String.valueOf(i) + OUTPUT_CSV_SEP +
			  directionStr + OUTPUT_CSV_SEP +
			  IDStr + OUTPUT_CSV_SEP +
			  intermediateTimes[i]);
		bwt.newLine();
	    }
	    bwt.flush();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
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

    static final String CURSOR_INSIDE_PORTAL_STR = "1";
    static final String CURSOR_OUTSIDE_PORTAL_STR = "0";
    long cinematicTime = 0;

    void writeCinematic(int jpx, int jpy, int px, int py){
	cinematicTime = System.currentTimeMillis() - trialStartTime;
	try {
	    bwc.write(trialCountStr + OUTPUT_CSV_SEP +
		      targetCountStr + OUTPUT_CSV_SEP +
		      directionStr + OUTPUT_CSV_SEP +
		      IDStr + OUTPUT_CSV_SEP +
		      String.valueOf(jpx) + OUTPUT_CSV_SEP +
		      String.valueOf(jpy) + OUTPUT_CSV_SEP +
		      String.valueOf(application.mCamera.posx) + OUTPUT_CSV_SEP +
		      String.valueOf(application.mCamera.posy) + OUTPUT_CSV_SEP +
		      String.valueOf(application.oCamera.posx) + OUTPUT_CSV_SEP +
		      String.valueOf(application.oCamera.posy) + OUTPUT_CSV_SEP +
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

    static String getTechniqueName(short t){
	switch (t){
	case AcquireEval.TECHNIQUE_TOW:{return AcquireEval.TECHNIQUE_TOW_NAME;}
 	case AcquireEval.TECHNIQUE_OV:{return AcquireEval.TECHNIQUE_OV_NAME;}
	}
	return "";
    }

}