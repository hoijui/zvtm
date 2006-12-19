
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
    static final short TECHNIQUE_FL = 0; // fading lens
    static final short TECHNIQUE_ML = 1; // melting lens
    static final short TECHNIQUE_DL = 2; // distortion lens
    static final short TECHNIQUE_HL = 3; // manhattan lens
    static final String[] TECHNIQUE_NAMES = {"Fading_Lens", "Melting_Lens", "Distortion_Lens", "Manhattan_Lens"}; 
    static final String[] TECHNIQUE_NAMES_ABBR = {"FL", "ML", "DL", "HL"}; 
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
    static final Color BACKGROUND_COLOR = Color.WHITE;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera;

    FittsEventHandler eh;

    /* cursor */
    static final Color CURSOR_COLOR = Color.RED;

    static final Color START_BUTTON_COLOR = Color.BLUE;

    /* padding for lenses */
    static final int[] vispad = {100, 100, 100, 100};
    static final Color PADDING_COLOR = Color.BLACK;

    static final Color INSTRUCTIONS_COLOR = Color.WHITE;

    /* lens */
    static final Color LENS_BOUNDARY_COLOR = Color.RED;
    static final Color LENS_OBSERVED_REGION_COLOR = Color.RED;
    float magFactor = 8.0f;
    static final int LENS_INNER_RADIUS = 50;
    static final int LENS_OUTER_RADIUS = 100;
    Lens lens;
    TFadingLens flens;

    /* target */
    static final Color TARGET_COLOR = Color.BLACK;
    VRectangle target;
    static final long TARGET_X_POS = 6300;
    static final long TARGET_Y_POS = 0;
    static final long TARGET_HEIGHT = 400;

    /* grid color */
    static final Color GRID_COLOR = Color.LIGHT_GRAY;
    static final long GRID_STEP = 1000;
    static final long GRID_W = 16000;
    static final long GRID_H = 12000;

    /* logs */
    static final String LOG_FILE_EXT = ".csv";
    static final String INPUT_CSV_SEP = ";";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    static final String TRIAL_DIR = "trials";
    static final String TRIAL_DIR_FULL = System.getProperty("user.dir") + File.separator + TRIAL_DIR;
    File logFile;
    BufferedWriter bwt;
    String subjectID;
    String subjectName;
    String blockNumber;

    IDSequence idSeq;
    int trialCount = -1;
    boolean trialStarted = false;
    long startTime = 0;
    int nbErrors = 0;

    static final int NB_TARGETS_PER_TRIAL = 8;
    long[] timeToTarget = new long[NB_TARGETS_PER_TRIAL];
    int hitCount = 0;
    
    public EvalFitts(short t, float mf){
	initGUI();
	this.technique = t;
	this.magFactor = mf;
	mViewName = TECHNIQUE_NAMES[this.technique];
	eh = new FittsEventHandler(this);
	mView.setEventHandler(eh);
	loadTrials();
	initScene();
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(1000.0f);
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
	mView.setAntialiasing(true);
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
	long x = -GRID_W / 2;
	for (int i=0;i<GRID_W/GRID_STEP+1;i++){
	    vsm.addGlyph(new BRectangle(x, 0, 0, 1, GRID_H/2, GRID_COLOR), mSpace);
	    x += GRID_STEP;
	}
	long y = -GRID_H / 2;
	for (int i=0;i<GRID_H/GRID_STEP+1;i++){
	    vsm.addGlyph(new BRectangle(0, y, 0, GRID_W/2, 1, GRID_COLOR), mSpace);
	    y += GRID_STEP;
	}	
 	target = new VRectangle(TARGET_X_POS, TARGET_Y_POS, 0, 10, TARGET_HEIGHT, TARGET_COLOR);
 	vsm.addGlyph(target, mSpace);
    }

    void loadTrials(){
	try {
	    File trialFile = new File(TRIAL_DIR_FULL + File.separator + "fitts.csv");
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
	    idSeq.computeWs();
	}
	catch (Exception ex){ex.printStackTrace();}
    }

    void initLogs(){
	subjectName = JOptionPane.showInputDialog("Subject Name");
	subjectID = JOptionPane.showInputDialog("Subject ID");
	blockNumber = JOptionPane.showInputDialog("Block");
	logFile = initLogFile(subjectID+"-"+TECHNIQUE_NAMES_ABBR[technique]+"-MM"+magFactor+"-block"+blockNumber, LOG_DIR);
	try {
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
	    writeHeaders();
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

    void writeHeaders(){
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
	setLens(jpx, jpy);
	showStartButton(false);
	say(null);
	trialStarted = true;
	startTime = System.currentTimeMillis();
    }

    void endTrial(){
	trialStarted = false;
	unsetLens();
	flushTrial();
	if (trialCount+1 < idSeq.length()){
	    initNextTrial();
	}
	else {
	    endSession();
	}
    }

    void initNextTrial(){
	trialCount++;
	nbErrors = 0;
	hitCount = 0;
	target.vx = -TARGET_X_POS;
	target.setWidth(idSeq.Ws[trialCount]);
	showStartButton(true);
	say("Trial " + (trialCount+1) + " / " + idSeq.length() + " - " + Messages.PSBTC);
    }

    void selectTarget(Glyph g){
	if (g == target){// target was hit
	    hitTarget();
	}
	else {
	    nbErrors++;
	}
    }

    void hitTarget(){
	timeToTarget[hitCount] = System.currentTimeMillis() - startTime;
	hitCount++;
	if (hitCount < NB_TARGETS_PER_TRIAL){
	    target.vx = -target.vx;
	    vsm.repaintNow();
	}
	else {
	    endTrial();
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
			  nbErrors);
		bwt.newLine();
	    }
	    bwt.flush();
	}
	catch (IOException ex){ex.printStackTrace();}
    }
    
    /* -------------- lenses ----------------- */

    void setLens(int x, int y){
	switch(technique){
	case TECHNIQUE_FL:{
	    flens = new TFadingLens(magFactor, 0.0f, 0.95f, LENS_OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens.setBoundaryColor(LENS_BOUNDARY_COLOR);
	    flens.setObservedRegionColor(LENS_OBSERVED_REGION_COLOR);
	    lens = flens;
	    break;
	}
	case TECHNIQUE_ML:{
	    lens = new TLinearLens(magFactor, 0.0f, 0.90f, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens = null;
	    break;
	}
	case TECHNIQUE_DL:{
	    lens = new FSGaussianLens(magFactor, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    flens = null;
	    break;
	}
	case TECHNIQUE_HL:{
	    lens = new FSManhattanLens(magFactor, LENS_OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)lens).setBoundaryColor(LENS_BOUNDARY_COLOR);
	    flens = null;
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
	if (flens != null){// dealing with a fading lens
	    flens.setAbsolutePosition(x, y, absTime);
	}
	else {// dealing with a probing lens
	    lens.setAbsolutePosition(x, y);
	}
	vsm.repaintNow();
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
    }

    void drawVisibilityPadding(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(PADDING_COLOR);
	g2d.fillRect(0, 0, viewWidth, vispad[1]);
	g2d.fillRect(0, vispad[1], vispad[0], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(viewWidth-vispad[2], vispad[1], vispad[2], viewHeight-vispad[1]-vispad[3]-1);
	g2d.fillRect(0, viewHeight-vispad[3]-1, viewWidth, vispad[3]+1);
	if (instructions != null){
	    g2d.setColor(INSTRUCTIONS_COLOR);
	    g2d.drawString(instructions, vispad[0], viewHeight-vispad[3]/2);
	}
	if (drawStartButton){
	    g2d.setColor(START_BUTTON_COLOR);
	    g2d.fillRect(viewWidth/2-10, viewHeight/2-10, 20, 20);
	}
    }

    void exit(){
	System.exit(0);
    }

    public static void main(String[] args){
	try {
	    if (args.length >= 4){
		EvalFitts.VIEW_MAX_W = Integer.parseInt(args[2]);
		EvalFitts.VIEW_MAX_H = Integer.parseInt(args[3]);
	    }
	    new EvalFitts(Short.parseShort(args[0]), Float.parseFloat(args[1]));
	}
	catch (Exception ex){
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Fading Lens");
	    new EvalFitts(EvalFitts.TECHNIQUE_FL, 8.0f);
	}
    }

}
