/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval.alphalens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import javax.swing.JOptionPane;

import java.util.Vector;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.lens.*;

public class EvalFitts implements Java2DPainter {

    /* techniques */
    static final short TECHNIQUE_FL = 2; // fading lens
    static final short TECHNIQUE_ML = 3; // melting lens
    static final short TECHNIQUE_DL = 1; // distortion lens
    static final short TECHNIQUE_HL = 0; // manhattan lens
    static final short TECHNIQUE_SCF = 4;// speed-coupled flattening
    static final String[] TECHNIQUE_NAMES = {"Manhattan_Lens", "Distortion_Lens", "Fading_Lens", "Melting_Lens", "Speed-coupled flattening"}; 
    static final String[] TECHNIQUE_NAMES_ABBR = {"HL", "DL", "FL", "ML", "SC"}; 
    short technique = TECHNIQUE_FL;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    /* ZVTM components */
    static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera;

    FittsEventHandler eh;

    static double D = 800;
    static double W1_6 = 2 * EvalFitts.LENS_INNER_RADIUS / 6.0 * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL;
    static double W1_10 = 2 * EvalFitts.LENS_INNER_RADIUS / 10.0 * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL;
    static double W1_14 = 2 * EvalFitts.LENS_INNER_RADIUS / 14.0 * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL;
    static long W2_6 = 40;
    static long W2_10 = 40;
    static long W2_14 = 40;

    /* lens */
    static final Color LENS_BOUNDARY_COLOR = Color.RED;
    static final Color LENS_OBSERVED_REGION_COLOR = Color.RED;
    float magFactor = 8.0f;
    static final int LENS_INNER_RADIUS = 50;
    static final int LENS_OUTER_RADIUS = 100;
    FixedSizeLens lens;
    TemporalLens tlens;

    /* cursor */
    static final Color CURSOR_COLOR = Color.WHITE;

    static final Color START_BUTTON_COLOR = Color.RED;

    /* padding for lenses */
    static final int[] vispad = {100, 100, 100, 100};
    static final Color PADDING_COLOR = Color.BLACK;

    static final Color INSTRUCTIONS_COLOR = Color.WHITE;

    static final float CAM_ALT = 900.0f;  // so as to get a proj coef of 0.1 (focal is 100.0f)

    /* target */
    static final Color HTARGET_COLOR = Color.WHITE;
    static final Color TARGET_COLOR = Color.WHITE;
    static int NB_TARGETS_PER_TRIAL = 24;
    VCircle[] targets;
    static final long TARGET_R_POS = Math.round(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL / 2.0);

    /* grid color */
    static final Color GRID_COLOR = Color.GRAY;
    static final long GRID_STEP = 200;
    static final long GRID_W = 16000;
    static final long GRID_H = 12000;

    /* logs */
    String TRIAL_FILE_NAME;

    static final boolean WRITE_CINEMATIC = true;

    static final String LOG_FILE_EXT = ".csv";
    static final String INPUT_CSV_SEP = ";";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;
    File tlogFile, clogFile;
    BufferedWriter bwt, bwc;
    String subjectID;
    String subjectName;
    String blockNumber;

    IDSequence idSeq;
    int trialCount = -1;
    boolean trialStarted = false;
    long startTime;
    long hitTime;
    long lastHitTime;

    int[] nbErrors = new int[NB_TARGETS_PER_TRIAL];
    long[] timeToTarget = new long[NB_TARGETS_PER_TRIAL];
    int hitCount = 0;

    static final int ERROR_DELAY = 500;
    
    public EvalFitts(short t, String f){
	initGUI();
	this.technique = t;
	this.TRIAL_FILE_NAME = f;
	mViewName = TECHNIQUE_NAMES[this.technique];
	eh = new FittsEventHandler(this);
	mView.setEventHandler(eh);
	loadTrials();
	initScene();
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(CAM_ALT);
	say(Messages.PSTS);
    }

    void initGUI(){
	windowLayout();
	vsm = new VirtualSpaceManager();
	mSpace = vsm.addVirtualSpace(mSpaceName);
	mCamera = vsm.addCamera(mSpaceName);
	Vector v = new Vector();
	v.add(mCamera);
	mView = vsm.addExternalView(v, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	mView.setVisibilityPadding(vispad);
	mView.getPanel().addComponentListener(eh);
	mView.setNotifyMouseMoved(true);
	mView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	//mView.setAntialiasing(true);
	mView.mouse.setColor(CURSOR_COLOR);
	mView.mouse.setSize(5);
	updatePanelSize();
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    //SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void initScene(){
	mView.setBackgroundColor(EvalFitts.BACKGROUND_COLOR);
	// grid
	VRectangle r;
	long x = -GRID_W / 2;
	for (int i=0;i<GRID_W/GRID_STEP+1;i++){
	    r = new VRectangle(x, 0, 0, 4, GRID_H/2, GRID_COLOR);
	    r.setDrawBorder(false);
	    vsm.addGlyph(r, mSpace);
	    x += GRID_STEP;
	}
	long y = -GRID_H / 2;
	for (int i=0;i<GRID_H/GRID_STEP+1;i++){
	    r = new VRectangle(0, y, 0, GRID_W/2, 4, GRID_COLOR);
	    r.setDrawBorder(false);
	    vsm.addGlyph(r, mSpace);
	    y += GRID_STEP;
	}
	targets = new VCircle[NB_TARGETS_PER_TRIAL];
	double angle = 0;
	for (int i=0;i<NB_TARGETS_PER_TRIAL;i++){
	    x = Math.round(TARGET_R_POS * Math.cos(angle));
	    y = Math.round(TARGET_R_POS * Math.sin(angle));
	    targets[i] = new VCircle(x, y, 0, Math.round(W2_6/2), TARGET_COLOR);
	    targets[i].setDrawBorder(false);
	    targets[i].setVisible(false);
	    vsm.addGlyph(targets[i], mSpace);
	    // lay out targets so that they between each side of the circle (ISO9241-9)
	    if (i % 2 == 0){angle += Math.PI;}
	    else {angle += 2 * Math.PI / ((double)NB_TARGETS_PER_TRIAL) - Math.PI;}
	}
    }

    void loadTrials(){
	try {
	    File trialFile = new File(TRIAL_DIR_FULL + File.separator + TRIAL_FILE_NAME);
	    FileInputStream fis = new FileInputStream(trialFile);
	    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
	    String line = br.readLine();
	    idSeq = new IDSequence();
	    while (line != null){
		if (line.length() > 0){
		    idSeq.addSequence(line.split(INPUT_CSV_SEP));
		}
		line = br.readLine();
	    }
	    fis.close();
	    idSeq.computeWsAndIDs();
	}
	catch (Exception ex){ex.printStackTrace();}
    }

    void initLogs(){
	subjectName = JOptionPane.showInputDialog("Subject Name");
	subjectID = JOptionPane.showInputDialog("Subject ID");
	blockNumber = JOptionPane.showInputDialog("Block");
	tlogFile = initLogFile(subjectID+"-"+TECHNIQUE_NAMES_ABBR[technique]+"-block"+blockNumber+"-trials", LOG_DIR);
	clogFile = initLogFile(subjectID+"-"+TECHNIQUE_NAMES_ABBR[technique]+"-block"+blockNumber+"-cinematic", LOG_DIR);
	try {
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tlogFile), "UTF-8"));
	    writeTrialHeaders();
	    if (WRITE_CINEMATIC){
		bwc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clogFile), "UTF-8"));
		writeCinematicHeaders();
	    }
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

    void writeTrialHeaders(){
	try {
	    // trial column headers
	    bwt.write("Name" + OUTPUT_CSV_SEP +
		      "SID" + OUTPUT_CSV_SEP +
		      "Technique" + OUTPUT_CSV_SEP +
		      "MM" + OUTPUT_CSV_SEP +
		      "Block" + OUTPUT_CSV_SEP +
		      "Trial" + OUTPUT_CSV_SEP +
		      "ID" + OUTPUT_CSV_SEP +
		      "Hit" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP +
		      "Errors");
	    bwt.newLine();
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeCinematicHeaders(){
	try {
	    // trial column headers
	    bwc.write("Trial" + OUTPUT_CSV_SEP +
		      "Hit" + OUTPUT_CSV_SEP +
		      "Time" + OUTPUT_CSV_SEP +
		      "lx" + OUTPUT_CSV_SEP +
		      "ly" + OUTPUT_CSV_SEP +
		      "Errors");
	    bwc.newLine();
	    bwc.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    /* ------------ TRIAL MANAGEMENT ------------- */

    void startSession(){
	initLogs();
	initNextTrial();
    }

    void endSession(){
	try {
	    bwt.flush();
	    bwt.close();
	    say(Messages.EOS);
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void startTrial(int jpx, int jpy){
	if (trialStarted){return;}
	trialStarted = true;
	setLens(jpx, jpy);
	mView.mouse.setSize(0);
	showStartButton(false);
	say(null);
	startTime = System.currentTimeMillis();
	lastHitTime = startTime;
    }

    void endTrial(){
	trialStarted = false;
	unsetLens();
	mView.mouse.setSize(5);
	flushTrial();
	flushCinematic();
	if (trialCount+1 < idSeq.length()){
	    initNextTrial();
	}
	else {
	    endSession();
	}
    }

    void initNextTrial(){
	trialCount++;
	hitCount = 0;
	for (int i=0;i<nbErrors.length;i++){
	    nbErrors[i] = 0;
	}
	for (int i=0;i<targets.length;i++){
	    targets[i].sizeTo(idSeq.Ws[trialCount]/2.0f);
	}
	highlight(hitCount, true);
	magFactor = idSeq.MMs[trialCount];
	showStartButton(true);
	say("Trial " + (trialCount+1) + " / " + idSeq.length() + " - " + Messages.PSBTC);
    }

    long[] rif = new long[4];

    void selectTarget(){
	// do not take early clicks into account if an error is currently being displayed,
	// or if actual MM of dynamic lens is not high enough, or if translucence of a fading lens is too high
	if (warning
	    || (technique==TECHNIQUE_FL && ((TFadingLens)lens).getFocusTranslucencyValue() < 0.4f)
	    || (technique==TECHNIQUE_SCF && ((DGaussianLens)lens).getActualMaximumMagnification() < 0.6f*lens.getMaximumMagnification())){return;}
	Glyph target = targets[hitCount];
	lens.getVisibleRegionInFocus(mCamera, rif);
	if (Math.sqrt(Math.pow((rif[0]+rif[2])/2.0-target.vx,2) + Math.pow((rif[3]+rif[1])/2.0-target.vy,2)) <= (rif[2]-rif[0])/2.0-target.getSize()){
	    // target is in focus region
	    hitTarget();
	}
	else {
	    warn(Messages.TARGET_NOT_IN_FOCUS, ERROR_DELAY);
	    nbErrors[hitCount] += 1;
	    writeCinematic();
	}
    }

    void hitTarget(){
	hitTime = System.currentTimeMillis();
	timeToTarget[hitCount] = hitTime - lastHitTime;
	lastHitTime = hitTime;
	highlight(hitCount, false);
	hitCount++;
	if (hitCount < NB_TARGETS_PER_TRIAL){
	    highlight(hitCount, true);
	    vsm.repaintNow();
	}
	else {
	    endTrial();
	}
    }

    void highlight(int targetIndex, boolean b){
	if (b){
	    targets[targetIndex].setVisible(true);
// 	    targets[targetIndex].setColor(HTARGET_COLOR);
	}
	else {
	    targets[targetIndex].setVisible(false);
// 	    targets[targetIndex].setColor(TARGET_COLOR);	    
	}
    }

    void flushTrial(){
	try {
	    for (int i=0;i<timeToTarget.length;i++){
		bwt.write(subjectName + OUTPUT_CSV_SEP +
			  subjectID + OUTPUT_CSV_SEP +
			  TECHNIQUE_NAMES_ABBR[technique] + OUTPUT_CSV_SEP +
			  magFactor + OUTPUT_CSV_SEP +
			  blockNumber + OUTPUT_CSV_SEP +
			  trialCount + OUTPUT_CSV_SEP +
			  idSeq.IDs[trialCount] + OUTPUT_CSV_SEP +
			  i + OUTPUT_CSV_SEP +  // hit index
			  timeToTarget[i] + OUTPUT_CSV_SEP +
			  nbErrors[i]);
		bwt.newLine();
	    }
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void writeCinematic(){
	try {
	    bwc.write(trialCount + OUTPUT_CSV_SEP +
		      hitCount + OUTPUT_CSV_SEP +
		      (System.currentTimeMillis()-startTime) + OUTPUT_CSV_SEP +
		      lens.lx + OUTPUT_CSV_SEP +
		      lens.ly + OUTPUT_CSV_SEP +
		      nbErrors[hitCount]);
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
    
    /* -------------- lenses ----------------- */

    void setLens(int x, int y){
	switch(technique){
	case TECHNIQUE_FL:{
	    tlens = new TFadingLens(magFactor, 0.0f, 0.95f, LENS_OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    ((TFadingLens)tlens).setBoundaryColor(LENS_BOUNDARY_COLOR);
	    ((TFadingLens)tlens).setObservedRegionColor(LENS_OBSERVED_REGION_COLOR);
	    lens = (FixedSizeLens)tlens;
	    break;
	}
	case TECHNIQUE_ML:{
	    tlens = new MeltingLens(magFactor, 0.0f, 0.80f, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    lens = (FixedSizeLens)tlens;
	    lens.setInnerRadiusColor(LENS_BOUNDARY_COLOR);
	    break;
	}
	case TECHNIQUE_DL:{
	    tlens = new DistortionLens(magFactor, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    lens = (FixedSizeLens)tlens;
	    lens.setInnerRadiusColor(LENS_BOUNDARY_COLOR);
	    break;
	}
	case TECHNIQUE_HL:{
	    lens = new FSManhattanLens(magFactor, LENS_OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)lens).setBoundaryColor(LENS_BOUNDARY_COLOR);
	    tlens = null;
	    break;
	}
	case TECHNIQUE_SCF:{
	    tlens = new DGaussianLens(magFactor, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    lens = (FixedSizeLens)tlens;
	    lens.setInnerRadiusColor(LENS_BOUNDARY_COLOR);
	    lens.setOuterRadiusColor(LENS_BOUNDARY_COLOR);
	    break;
	}
	}
	mView.setLens(lens);
    }

    void unsetLens(){
	mView.setLens(null);
	lens.dispose();
    }

    void moveLens(int x, int y, long absTime){
	if (tlens != null){// dealing with a fading lens
	    tlens.setAbsolutePosition(x, y, absTime);
	}
	else {// dealing with a probing lens
	    lens.setAbsolutePosition(x, y);
	}
	vsm.repaintNow();
	if (WRITE_CINEMATIC && trialStarted){
	    writeCinematic();
	}
    }


    /* ------------ LOW-LEVEL GRAPHICS ------------- */

    void updatePanelSize(){
	Dimension d = mView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    void say(String s){
	instructions = (s != null && s.length() == 0) ? null : s;
	vsm.repaintNow();
    }

    boolean warning = false;
    String warningText = null;
    
    void warn(final String s, final int duration){
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    warningText = s;
		    warning = true;
		    vsm.repaintNow();
		    sleep(duration);
		    warning = false;
		    warningText = null;
		    vsm.repaintNow();
		    return null;
		}
	    };
	worker.start();
    }

    void showStartButton(boolean b){
	drawStartButton = b;
	vsm.repaintNow();
    }

    boolean cursorInsideStartButton(int jpx, int jpy){
	return jpx >= panelWidth/2-10 &&
	    jpx <= panelWidth/2+10 &&
	    jpy >= panelHeight/2-10 &&
	    jpy <= panelHeight/2+10;
    }

    String instructions = null;
    boolean drawStartButton = false;
    
    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	drawVisibilityPadding(g2d, viewWidth, viewHeight);
	if (instructions != null){
	    g2d.setColor(INSTRUCTIONS_COLOR);
	    g2d.drawString(instructions, vispad[0], viewHeight-vispad[3]/2);
	}
	if (warning && warningText != null){
	    g2d.setColor(Color.BLACK);
	    g2d.fillRect(0, viewHeight/2-100, viewWidth, 200);
	    g2d.setColor(Color.RED);
	    g2d.drawString(warningText, viewWidth/2-50, viewHeight/2);
	}
	if (drawStartButton){
	    g2d.setColor(START_BUTTON_COLOR);
	    g2d.fillRect(viewWidth/2-10, viewHeight/2-10, 20, 20);
	}
    }

    void drawVisibilityPadding(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(PADDING_COLOR);
	g2d.fillRect(0, 0, viewWidth, vispad[1]);
	g2d.fillRect(0, vispad[1], vispad[0], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(viewWidth-vispad[2], vispad[1], vispad[2], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(0, viewHeight-vispad[3]-1, viewWidth, vispad[3]+1);
    }

    void exit(){
	System.exit(0);
    }

    public static void main(String[] args){
	try {
	    if (args.length >= 4){
		EvalFitts.VIEW_MAX_W = Integer.parseInt(args[3]);
		EvalFitts.VIEW_MAX_H = Integer.parseInt(args[4]);
	    }
	    new EvalFitts(Short.parseShort(args[0]), args[1]);
	}
	catch (Exception ex){
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Fading Lens");
	    new EvalFitts(EvalFitts.TECHNIQUE_FL, "fitts.csv");
	}
    }

}
