/*   FILE: AbstractTaskLogManager.java
 *   DATE OF CREATION:  Mon Apr 10 14:28:35 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskLogManager.java,v 1.35 2006/06/01 06:40:01 epietrig Exp $
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

import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.ZRoundRect;
import com.xerox.VTM.engine.SwingWorker;
import net.claribole.zvtm.engine.Java2DPainter;

class AbstractTaskLogManager implements Java2DPainter {

    static final String LOG_FILE_EXT = ".csv";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;

    static final String PSBTC = "PRESS SPACE BAR TO CONTINUE";
    static final String PSTS = "PRESS S TO START";
    static final String INTPW = "Initializing next trial. Please wait...";
    static final String EOS = "END OF SESSION";
    String msg = PSBTC;

    static final int WARN_MSG_DELAY = 500;
    static final String TARGET_ERR = "ERROR: Target is not within selection region";

    /* codes for technique */
    static final String ZL = "ZL";     // Probing lenses
    static final String PZ = "PZVC";   // Pan + Zoom centered on view
    static final String RZ = "RZ";     // Region zooming
    static final String PZL = "PZL";   // Pan + Zoom + Probing Lenses
    static final String DM = "DM";     // Drag mag

    /* codes for lens status */
    static final String NO_LENS = "0";
    static final String ZOOMIN_LENS = "1";
    static final String ZOOMOUT_LENS = "2";
    String lensStatus = NO_LENS;

    static final String NaN = "NaN";
    String lensxS = NaN;
    String lensyS = NaN;
    String lensmmS = TrialInfo.doubleFormatter(ZLAbstractTask.DEFAULT_MAG_FACTOR);

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);

    ZLAbstractTask application;

    String subjectID;
    String subjectName;
    String techniqueName;
    String blockNumber;
    int trialCount;
    String trialCountStr;
    int nbZIOswitches;
    int nbErrors;

    long trialStartTime;
    long trialDuration;
    int trialDensity;

    String lineStart;
    File logFile;
    File cinematicFile;
    BufferedWriter bwt, bwc;

    boolean sessionStarted = false;
    boolean trialStarted = false;

    AbstractTrialInfo[] trials;
    ZRoundRect deepestTarget;

    AbstractTaskInstructionsManager im;

    AbstractTaskLogManager(ZLAbstractTask app){
	this.application = app;
	im = new AbstractTaskInstructionsManager(app);
	this.application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_DISTORTION);
    }

    void startSession(){
	im.say(" ");
	init(application.technique);
	sessionStarted = true;
    }

    void init(short technique){
	trialCount = -1;
	techniqueName = getTechniqueName(technique);
	subjectName = JOptionPane.showInputDialog("Subject Name");
	if (subjectName == null){im.say(PSTS);return;}
	subjectID = JOptionPane.showInputDialog("Subject ID");
	if (subjectID == null){im.say(PSTS);return;}
 	blockNumber = JOptionPane.showInputDialog("Block number");
 	if (blockNumber == null){im.say(PSTS);return;}
	initLineStart();
	JFileChooser fc = new JFileChooser(new File(LogManager.TRIAL_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Trial File");
	int returnVal= fc.showOpenDialog(application.demoView.getFrame());
	File trialFile = null;
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    trialFile = fc.getSelectedFile();
	}
	else {
	    im.say(PSTS);
	    return;
	}
	generateTrials(trialFile);
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
	application.demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	initNextTrial();
    }

    void generateTrials(File f){
	try {
	    FileInputStream fis = new FileInputStream(f);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    Vector v = new Vector();
	    String line = br.readLine();
	    Vector trialLines = new Vector();
	    boolean firstTrial = true;
	    String[] info = null;
	    while (line != null){
		if (line.startsWith("# Trial")){
		    if (!firstTrial){
			// store trial for previous lines
			v.add(new AbstractTrialInfo(Integer.parseInt(info[0].substring(8)), // 8 = card("# Trial=")
						    Integer.parseInt(info[1].substring(8)), // 8 = card("Density=")
						    trialLines));
			trialLines.clear();
		    }
		    else {
			firstTrial = false;
		    }
		    info = line.split(AbstractWorldGenerator.CSV_SEP);
		}
		else {
		    trialLines.add(line);
		}
		line = br.readLine();
	    }
	    // store last trial
	    v.add(new AbstractTrialInfo(Integer.parseInt(info[0].substring(8)), // 8 = card("# Trial=")
					Integer.parseInt(info[1].substring(8)), // 8 = card("Density=")
					trialLines));

	    trials = new AbstractTrialInfo[v.size()];
	    for (int i=0;i<trials.length;i++){
		trials[i] = (AbstractTrialInfo)v.elementAt(i);
	    }
	    v.clear();
	}
	catch (IOException ex){ex.printStackTrace();}
    }
    
    int currentDepth;
    
    void updateWorld(long[] visibleRegion, short altChange){
	if (lensStatus == NO_LENS){
	    currentDepth = trials[trialCount].root.updateWorld(visibleRegion, altChange);
	    application.vsm.repaintNow();
	}
    }

    // init first columns of each line in output trials file
    void initLineStart(){
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
		      "Density" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP +
		      "Nb Switches" + OUTPUT_CSV_SEP +
		      "Nb Errors");
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
		      "Lens" + OUTPUT_CSV_SEP +
		      "cx" + OUTPUT_CSV_SEP +
		      "cy" + OUTPUT_CSV_SEP +
		      "cz" + OUTPUT_CSV_SEP +
		      "lx" + OUTPUT_CSV_SEP +
		      "ly" + OUTPUT_CSV_SEP +
		      "mm" + OUTPUT_CSV_SEP +
		      "time" + OUTPUT_CSV_SEP +
		      "currentRegion");
	    bwc.newLine();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void startTrial(){
	trialStarted = true;
	nbZIOswitches = 0;
	nbErrors = 0;
	application.eh.zoomDirection = AbstractTaskEventHandler.NOT_ZOOMING;
 	application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_DISTORTION);
	trialStartTime = System.currentTimeMillis();
    }

    void initNextTrial(){
	trialCount++;
	if (trialCount > 0){trials[trialCount-1].removeFromVirtualSpace(application.mainVS);}
	trialDensity = trials[trialCount].density;
	trials[trialCount].addToVirtualSpace(application.vsm, application.mainVS);
	deepestTarget = trials[trialCount].root.getDeepestTarget();
	application.demoCamera.posx = 0;
	application.demoCamera.posy = 0;
	application.demoCamera.updatePrecisePosition();
	application.demoCamera.altitude = ZLAbstractTask.START_ALTITUDE;
	msg = PSBTC + " - Trial " + (trialCount+1) + " of " + trials.length;
	application.eh.cameraMoved();
	application.vsm.repaintNow();
	// need to call it twice because of visibleRegion update issue
	application.eh.cameraMoved();
    }

    void endTrial(){
	trialDuration = System.currentTimeMillis() - trialStartTime;
	trialStarted = false;
	writeTrial();
	flushCinematic();
	if (trialCount +1 >= trials.length){
	    endSession();
	}
	else {// there it at least one trial left
	    if (application.dmPortal != null){application.killDM();}
	    if (application.lens != null){
		application.killLens();
	    }
	    msg = INTPW;
	    application.demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	    application.demoView.repaintNow();
	    System.gc();
	}
    }

    void nextStep(long vx, long vy){
	if (!sessionStarted){return;}
	if (trialStarted){// subject wants to end the trial
	    if (targetWithinRange(vx, vy)){
		endTrial();
		if (trialCount+1 < trials.length){// there is at least one trial left
		    final SwingWorker worker=new SwingWorker(){
			    public Object construct(){
				initNextTrial();
				return null; 
			    }
			};
		    worker.start();
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

    boolean targetWithinRange(long vx, long vy){
	return (vx >= deepestTarget.vx - deepestTarget.getWidth() &&
		vx <= deepestTarget.vx + deepestTarget.getWidth() &&
		vy >= deepestTarget.vy - deepestTarget.getHeight() &&
		vy <= deepestTarget.vy + deepestTarget.getHeight());
    }

    void wrongTarget(){
	nbErrors++;
 	im.warn(TARGET_ERR, "", WARN_MSG_DELAY);
    }

    void writeTrial(){
	try {
	    // subject name + subject ID + technique
	    bwt.write(lineStart);
	    // trial + D + time + nb switches + nb errors
	    bwt.write(trialCountStr + OUTPUT_CSV_SEP +
		      trialDensity + OUTPUT_CSV_SEP +
		      Long.toString(trialDuration) + OUTPUT_CSV_SEP +
		      Integer.toString(nbZIOswitches) + OUTPUT_CSV_SEP +
		      Integer.toString(nbErrors));
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeCinematic(){
	try {
	    // trial + D + time + nb switches + nb errors
	    bwc.write(trialCountStr + OUTPUT_CSV_SEP +
		      lensStatus + OUTPUT_CSV_SEP +
		      Long.toString(application.demoCamera.posx) + OUTPUT_CSV_SEP +
		      Long.toString(application.demoCamera.posy) + OUTPUT_CSV_SEP +
		      TrialInfo.floatFormatter(application.demoCamera.altitude) + OUTPUT_CSV_SEP +
		      lensxS + OUTPUT_CSV_SEP + lensyS + OUTPUT_CSV_SEP + lensmmS +
		      OUTPUT_CSV_SEP + Long.toString(System.currentTimeMillis()-trialStartTime)
		       + OUTPUT_CSV_SEP + currentDepth);
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
	try {
	    lensxS = Integer.toString(application.lens.lx);
	    lensyS = Integer.toString(application.lens.ly);
	    if (write && trialStarted){
		writeCinematic();
	    }
	}
	catch (NullPointerException ex){System.err.println("Did not write cienmatic info");ex.printStackTrace();}
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	im.drawFrame(g2d, viewWidth, viewHeight);
	im.writeInstructions(g2d, viewWidth, viewHeight);
	g2d.setColor(Color.BLACK);
	g2d.setComposite(acST);
	g2d.fillRect(0, viewHeight / 2 - 100, viewWidth, 220);
	g2d.setComposite(com.xerox.VTM.glyphs.Transparent.acO);
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
	case ZLAbstractTask.ZL_TECHNIQUE:{return ZL;}
	case ZLAbstractTask.PZ_TECHNIQUE:{return PZ;}
	case ZLAbstractTask.RZ_TECHNIQUE:{return RZ;}
	case ZLAbstractTask.PZL_TECHNIQUE:{return PZL;}
	case ZLAbstractTask.DM_TECHNIQUE:{return DM;}
	}
	return "";
    }

}