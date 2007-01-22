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

import net.claribole.zvtm.engine.Java2DPainter;

public class AcquireLogManager {

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
		      "cx" + OUTPUT_CSV_SEP +
		      "cy" + OUTPUT_CSV_SEP +
		      "cz" + OUTPUT_CSV_SEP +
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

    void initNextTrial(){
	incTrialCount();
	resetTargetCount();
	im.say(TRIAL_STR + String.valueOf(trialCount+1) + OF_STR + String.valueOf(block.direction.length));
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

    long trialStartTime;

    void startTrial(){
	im.say(null);
	trialStarted = true;
	trialStartTime = System.currentTimeMillis();
	
    }

    void nextTarget(){
	//XXX:TBW
    }

    void endTrial(){
	trialStarted = false;
	//XXX:TBW
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