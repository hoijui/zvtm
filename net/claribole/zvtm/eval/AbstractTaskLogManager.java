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

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ZRoundRect;
import com.xerox.VTM.engine.SwingWorker;
import net.claribole.zvtm.engine.Java2DPainter;

class AbstractTaskLogManager implements Java2DPainter {

    static final String LOG_FILE_EXT = ".csv";
    static final String INPUT_CSV_SEP = ";";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;

    static final String PBTC = "PRESS BUTTON TO CONTINUE";
    static final String PSTS = "PRESS S TO START";
    static final String INTPW = "Initializing next trial. Please wait...";
    static final String EOS = "END OF SESSION";
    static final String C_BT = "CONTINUE";  // Continue button displayed between trials
    String msg = PBTC;

    static final int ERR_MSG_DELAY = 800;
    static final String TARGET_ERR = "ERROR: Wrong target";

    /* codes for technique */
//     static final String ZL = "ZL";     // Probing lenses
    static final String PZ = "PZVC";   // Pan + Zoom centered on view
    static final String PZO = "PZO";     // Pan + Zoom centered on view + Overview
    static final String PZL = "PZL";   // Pan + Zoom + Probing Lenses
    static final String DM = "DM";     // Drag mag

    /* codes for lens status */
    static final String NO_LENS = "0";
    static final String ZOOMIN_LENS = "1";
    static final String ZOOMOUT_LENS = "2";
    static final String DM_LENS = "3";
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
    int nbMulVis;

    AbstractTrialInfo[] trials;

    long trialStartTime;
    long trialDuration;

    String lineStart;
    File logFile;
    File cinematicFile;
    BufferedWriter bwt, bwc;

    boolean sessionStarted = false;
    boolean trialStarted = false;

    ZRoundRect deepestTarget;

    AbstractTaskInstructionsManager im;

    AbstractTaskLogManager(ZLAbstractTask app){
	this.application = app;
	im = new AbstractTaskInstructionsManager(app);
	this.application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_PORTALS);
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
	    String line = br.readLine();
	    Vector tmpTrials = new Vector();
	    int nbTrials = 0;
	    while (line != null){
		tmpTrials.add(new AbstractTrialInfo(nbTrials++, line.split(INPUT_CSV_SEP)));
		line = br.readLine();
	    }
	    trials = new AbstractTrialInfo[tmpTrials.size()];
	    for (int i=0;i<trials.length;i++){
		trials[i] = (AbstractTrialInfo)tmpTrials.elementAt(i);
	    }
	    tmpTrials.clear();
	}
	catch (IOException ex){ex.printStackTrace();}
    }
    
    void updateWorld(long[] visibleRegion){
  	if (lensStatus == NO_LENS || lensStatus == DM_LENS){
// 	    application.updateWorldLevel(visibleRegion);
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
		      "Time" + OUTPUT_CSV_SEP +
		      "Nb Switches" + OUTPUT_CSV_SEP +
		      "Nb Mul Vis" + OUTPUT_CSV_SEP +
		      "Right target" + AbstractTrialInfo.getHeader());
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
		      "Time");
	    // additional info only for dragmag
	    if (application.technique == ZLAbstractTask.DM_TECHNIQUE){
		bwc.write(OUTPUT_CSV_SEP + "pcx" +
			  OUTPUT_CSV_SEP + "pcy" +
			  OUTPUT_CSV_SEP + "pcz");		
	    }
	    bwc.newLine();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void start(int jpx, int jpy){// cursor coordinates used to check that subject clicked on button
	if (sessionStarted && ZLAbstractTask.clickOnStartButton(jpx, jpy) && trialCount < trials.length){
	    trialCountStr = Integer.toString(trialCount);
	    startTrial();
	}
    }

    void startTrial(){
	trialStarted = true;
	nbZIOswitches = 0;
	nbMulVis = 0;
	application.eh.zoomDirection = AbstractTaskEventHandler.NOT_ZOOMING;
 	application.demoView.setJava2DPainter(im, Java2DPainter.AFTER_DISTORTION);
	trialStartTime = System.currentTimeMillis();
    }

    void initNextTrial(){
	application.resetWorld();
	trialCount++;
	target = null;
	rightTarget = AbstractTaskLogManager.RIGHT_TARGET;
 	application.demoCamera.posx = trials[trialCount].initialCameraPos.x;
 	application.demoCamera.posy = trials[trialCount].initialCameraPos.y;
	application.demoCamera.updatePrecisePosition();
	application.demoCamera.altitude = ZLAbstractTask.START_ALTITUDE;
 	msg = PBTC + " - Trial " + (trialCount+1) + " of " + trials.length;
	application.eh.cameraMoved();
	application.vsm.repaintNow();
	// need to call it twice because of visibleRegion update issue
	application.eh.cameraMoved();
	if (application.technique == ZLAbstractTask.PZO_TECHNIQUE){application.centerOverview();}
    }

    void endTrial(){
	trialDuration = System.currentTimeMillis() - trialStartTime;
	trialStarted = false;
	writeTrial(trialStartTime, trialCount);
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

    ZRoundRect objectToUnveil;

    Glyph target; // null until actual target has been unveiled (for each trial)
    static final String RIGHT_TARGET = "1";
    static final String WRONG_TARGET = "0";
    String rightTarget = AbstractTaskLogManager.RIGHT_TARGET;

    void unveil(long[] wnes){// wnes represents the boundaries
	// of the region taken into account to identify the currently observed object,
	// region which changes depending on the technique (PZ, PZO, PZL, DM)
	if (!sessionStarted){return;}
	if (trialStarted){// subject wants to unveil an object
	    objectToUnveil = getClosestObject(wnes);
	    if (closestObjectInRegion(objectToUnveil, wnes) && // if object is actually visible in viewport
		visibleCorners(objectToUnveil)){               // if object is big enough to identify it as being the target (or not)
		trials[trialCount].newVisit(System.currentTimeMillis()-trialStartTime, (String)objectToUnveil.getOwner());
		if (objectNotVisitedYet(objectToUnveil)){
		    highlightBriefly(objectToUnveil, 400);
		    if (trials[trialCount].targetIndex <= trials[trialCount].nbTargetsVisited){
			// this object is the target for this level
			// replace rectangle with round rectangle
			target = objectToUnveil;
			objectToUnveil.renderRound(true);
		    }
		}
		// else this object is not yet the target, nothing to do (already been marked just before the test above)
	    }
	    // else consider this as an accidental space bar hit
	}
    }

    /* Finds out if object o has already been visited or not. Issues a warning if message has already been visited */
    boolean objectNotVisitedYet(ZRoundRect o){
	boolean res = true;
	for (int i=0;i<application.elementsByLevel[1].length;i++){
	    if (application.elementsByLevel[1][i] == o){
		res = !application.visitsByLevel[1][i];
		if (res){// this is the first visit to this object
		    application.visitsByLevel[1][i] = true;
		}
		else {// the object was visited in the past
		    visitedTarget();
		}
		return res;
	    }
	}
	return res;
    }

    boolean visibleCorners(ZRoundRect r){
	if (application.technique == ZLAbstractTask.PZL_TECHNIQUE){
	    return objectToUnveil.cornersVisibleInLens(application.demoCamera) || objectToUnveil.cornersVisible(application.demoCamera);
	}
	else if (application.technique == ZLAbstractTask.DM_TECHNIQUE){
	    return objectToUnveil.cornersVisible(application.portalCamera) || objectToUnveil.cornersVisible(application.demoCamera);
	}
	else {// PZ, or PZO
	    return objectToUnveil.cornersVisible(application.demoCamera);
	}
    }

    void validateTarget(long[] wnes){
	if (!trialStarted){return;}
	ZRoundRect potentialTarget = getClosestObject(wnes);
	if (target == null || !closestObjectInRegion(potentialTarget, wnes) || potentialTarget != target){
	    // three reasons this could be the wrong choice: - haven't visited enough objects (target not determined yet)
	    //                                               - no object visible in region
	    //                                               - wrong object
	    im.warn(TARGET_ERR, "", ERR_MSG_DELAY);
	    rightTarget = AbstractTaskLogManager.WRONG_TARGET;
	}
	// else choosing right target, nothing specific to do
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

    ZRoundRect getClosestObject(long[] wnes){
	long x = (wnes[0]+wnes[2]) / 2;
	long y = (wnes[1]+wnes[3]) / 2;
	ZRoundRect res = application.elementsByLevel[1][0];
	double smallestDistance = Math.sqrt(Math.pow(res.vx-x, 2) + Math.pow(res.vy-y, 2));
	ZRoundRect r;
	double d;
	for (int i=1;i<application.elementsByLevel[1].length;i++){
	    r = application.elementsByLevel[1][i];
	    d = Math.sqrt(Math.pow(r.vx-x, 2) + Math.pow(r.vy-y, 2));
	    if (d < smallestDistance){
		res = r;
		smallestDistance = d;
	    }
	}
	return res;
    }

    boolean closestObjectInRegion(ZRoundRect target, long[] wnes){
	return (target.vx+target.getWidth() > wnes[0] &&  target.vx-target.getWidth() < wnes[2] &&
		target.vy+target.getHeight() > wnes[3] &&  target.vy-target.getHeight() < wnes[1]);
    }

    /* temporarily change r's border color to give a visual feedback about the inscpetion of this object */
    void highlightBriefly(final ZRoundRect r, final int delay){
	r.setBorderColor(ZLAbstractTask.VISITED_BORDER_COLOR);
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    sleep(delay);
		    r.setBorderColor(ZLAbstractTask.DISC_BORDER_COLOR);
		    return null; 
		}
	    };
	worker.start();
    }

    void visitedTarget(){
	nbMulVis++;
    }

    void writeTrial(long tst, int tc){
	try {
	    // subject name + subject ID + technique
	    bwt.write(lineStart);
	    // trial + D + time + nb switches + nb errors
	    bwt.write(trialCountStr + OUTPUT_CSV_SEP +
		      Long.toString(trialDuration) + OUTPUT_CSV_SEP +
		      Integer.toString(nbZIOswitches) + OUTPUT_CSV_SEP +
		      Integer.toString(nbMulVis) + OUTPUT_CSV_SEP +
		      rightTarget + trials[trialCount].getVisitSummary());
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeCinematic(){
	try {
	    if (application.technique == ZLAbstractTask.DM_TECHNIQUE){
		bwc.write(trialCountStr + OUTPUT_CSV_SEP +
			  lensStatus + OUTPUT_CSV_SEP +
			  Long.toString(application.demoCamera.posx) + OUTPUT_CSV_SEP +
			  Long.toString(application.demoCamera.posy) + OUTPUT_CSV_SEP +
			  TrialInfo.floatFormatter(application.demoCamera.altitude) + OUTPUT_CSV_SEP +
			  lensxS + OUTPUT_CSV_SEP + lensyS + OUTPUT_CSV_SEP + lensmmS +
			  OUTPUT_CSV_SEP + Long.toString(System.currentTimeMillis()-trialStartTime)+ OUTPUT_CSV_SEP + 
			  Long.toString(application.portalCamera.posx) + OUTPUT_CSV_SEP +
			  Long.toString(application.portalCamera.posy) + OUTPUT_CSV_SEP +
			  TrialInfo.floatFormatter(application.portalCamera.altitude));
	    }
	    else {
		bwc.write(trialCountStr + OUTPUT_CSV_SEP +
			  lensStatus + OUTPUT_CSV_SEP +
			  Long.toString(application.demoCamera.posx) + OUTPUT_CSV_SEP +
			  Long.toString(application.demoCamera.posy) + OUTPUT_CSV_SEP +
			  TrialInfo.floatFormatter(application.demoCamera.altitude) + OUTPUT_CSV_SEP +
			  lensxS + OUTPUT_CSV_SEP + lensyS + OUTPUT_CSV_SEP + lensmmS +
			  OUTPUT_CSV_SEP + Long.toString(System.currentTimeMillis()-trialStartTime));
	    }
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
	}
	catch (NullPointerException ex){
	    lensxS = AbstractTaskLogManager.NaN;
	    lensyS = AbstractTaskLogManager.NaN;
	}
	if (write && trialStarted){
	    writeCinematic();
	}
    }

    void portalPositionChanged(boolean write){
	if (application.dmPortal != null){
	    lensxS = Integer.toString(application.dmPortal.x);
	    lensyS = Integer.toString(application.dmPortal.y);
	}
	else {
	    lensxS = AbstractTaskLogManager.NaN;
	    lensyS = AbstractTaskLogManager.NaN;	    
	}
	if (write && trialStarted){
	    writeCinematic();
	}
    }


    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	// padding
	im.drawFrame(g2d, viewWidth, viewHeight);
	im.writeInstructions(g2d, viewWidth, viewHeight);
	// message at center of screen (translucent black strip + text)
	g2d.setColor(Color.BLACK);
	g2d.setComposite(acST);
	g2d.fillRect(0, viewHeight / 2 - 100, viewWidth, 220);
	g2d.setComposite(com.xerox.VTM.glyphs.Transparent.acO);
	g2d.setColor(Color.WHITE);
	g2d.drawString(msg, viewWidth/2 - 105, viewHeight/2);
	// button
	g2d.setColor(Color.GRAY);
	g2d.fillRect(ZLAbstractTask.START_BUTTON_TL_X, ZLAbstractTask.START_BUTTON_TL_Y, ZLAbstractTask.START_BUTTON_W, ZLAbstractTask.START_BUTTON_H);
	g2d.setColor(Color.RED);
	g2d.drawRect(ZLAbstractTask.START_BUTTON_TL_X, ZLAbstractTask.START_BUTTON_TL_Y, ZLAbstractTask.START_BUTTON_W, ZLAbstractTask.START_BUTTON_H);
	g2d.setColor(Color.BLACK);
	g2d.drawString(C_BT, viewWidth/2-25,viewHeight/2+25);
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
// 	case ZLAbstractTask.ZL_TECHNIQUE:{return ZL;}
	case ZLAbstractTask.PZ_TECHNIQUE:{return PZ;}
 	case ZLAbstractTask.PZO_TECHNIQUE:{return PZO;}
	case ZLAbstractTask.PZL_TECHNIQUE:{return PZL;}
	case ZLAbstractTask.DM_TECHNIQUE:{return DM;}
	}
	return "";
    }

}