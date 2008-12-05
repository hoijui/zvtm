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
import java.awt.Font;
import javax.swing.ImageIcon;
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
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.lens.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class EvalAcqLabel implements Java2DPainter {

    /* techniques */
    static final short TECHNIQUE_SCB = 0; // speed-coupled blending
    static final short TECHNIQUE_SCF = 1; // speed-coupled flattening
    static final String[] TECHNIQUE_NAMES = {"Speed_Coupled_Blending_Lens", "Speed_Coupled_Flattening"}; 
    static final String[] TECHNIQUE_NAMES_ABBR = {"SCB", "SCF"}; 
    short technique = TECHNIQUE_SCB;

	/* background */
	static final short BACKGROUND_MAP = 0;
	static final short BACKGROUND_GRAPH = 1;
	static final String[] BACKGROUND_NAMES = {"Map", "Graph"};
	short background = BACKGROUND_MAP;

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
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera;

    AcqLabelEventHandler eh;

    static double D = 800;
//    static double W1_8 = 2 * EvalAcqLabel.LENS_INNER_RADIUS / 8.0 * (Camera.DEFAULT_FOCAL+EvalAcqLabel.CAM_ALT)/Camera.DEFAULT_FOCAL;
//    static double W1_12 = 2 * EvalAcqLabel.LENS_INNER_RADIUS / 12.0 * (Camera.DEFAULT_FOCAL+EvalAcqLabel.CAM_ALT)/Camera.DEFAULT_FOCAL;
    static long W2_8 = 40;
//    static long W2_12 = 40;

    /* lens */
    static final Color LENS_BOUNDARY_COLOR_WM = Color.WHITE;
    static final Color LENS_OBSERVED_REGION_COLOR_WM = Color.WHITE;
    static final Color LENS_BOUNDARY_COLOR_GR = Color.BLUE;
    static final Color LENS_OBSERVED_REGION_COLOR_GR = Color.BLUE;
    float magFactor = 8.0f;
    static final int LENS_INNER_RADIUS = 50;
    static final int LENS_OUTER_RADIUS = 100;
    FixedSizeLens lens;
    TemporalLens tlens;

    /* cursor */
    static final Color CURSOR_COLOR = Color.BLACK;

    static final Color START_BUTTON_COLOR = Color.RED;

    /* padding for lenses */
    static final int[] vispad = {100, 100, 100, 100};
    static final Color PADDING_COLOR = Color.BLACK;

    static final Color INSTRUCTIONS_COLOR = Color.WHITE;

    static final float CAM_ALT = 900.0f;  // so as to get a proj coef of 0.1 (focal is 100.0f)

    /* target */
    static final Color HTARGET_COLOR = Color.RED;
    static final Color TARGET_COLOR_WM = Color.YELLOW;
    static final Color TARGET_BKG_COLOR_WM = Color.BLACK;
    static final Color TARGET_COLOR_GR = Color.BLACK;
    static final Color TARGET_BKG_COLOR_GR = Color.WHITE;

	static final int LABEL_FONT_SIZE_WM = 42;
	static final int LABEL_FONT_SIZE_GR = 30;
	static final Font LABEL_FONT_MM8_WM = new Font("Dialog", Font.PLAIN, LABEL_FONT_SIZE_WM);
	static final Font LABEL_FONT_MM12_WM = new Font("Dialog", Font.PLAIN, Math.round(LABEL_FONT_SIZE_WM*8/12));
	static final Font LABEL_FONT_MM8_GR = new Font("Dialog", Font.PLAIN, LABEL_FONT_SIZE_GR);
	static final Font LABEL_FONT_MM12_GR = new Font("Dialog", Font.PLAIN, Math.round(LABEL_FONT_SIZE_GR*8/12));

    static int NB_TARGETS_PER_TRIAL = 24;
    VBTextST[] targets;
    static final long TARGET_R_POS = Math.round(EvalAcqLabel.D * (Camera.DEFAULT_FOCAL+EvalAcqLabel.CAM_ALT)/Camera.DEFAULT_FOCAL / 2.0);

    static final float OBVIOUS_TARGET = 1.0f;
    static final float FURTIVE_TARGET = 0.2f;
    float targetAlpha = OBVIOUS_TARGET;
    String targetAlphaStr = "1.0";

    /* instructions */
	static final Font INSTRUCTIONS_FONT = new Font("Dialog", Font.PLAIN, 12);
	static final Font INDICATIONS_FONT = new Font("Dialog", Font.PLAIN, 24);

    /* target indicators */
    static final int INDICATOR_LENGTH = 500;
    static final int INDICATOR_THICKNESS = 20;
    static final Color INDICATOR_COLOR = Color.RED;
    static final Color INDICATOR_BORDER = Color.BLACK;
    VRectangle latIndicatorW, latIndicatorE, longIndicatorN, longIndicatorS;

    /* grid color */
    static final Color GRID_COLOR = Color.GRAY;
    static final long GRID_STEP = 200;
    static final long GRID_W = 16000;
    static final long GRID_H = 12000;

    /* logs */
    String TRIAL_FILE_NAME;

    static final boolean WRITE_CINEMATIC = false;

    static final String LOG_FILE_EXT = ".csv";
    static final String INPUT_CSV_SEP = ";";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    File tlogFile, clogFile;
    BufferedWriter bwt, bwc;
    String subjectID;
    String subjectName;
    String blockNumber;

    LabelSequence[] trials;
    int trialCount = -1;
    boolean trialStarted = false;
    long startTime;
    long hitTime;

    int nbReadErrors = 0;
    int nbAcqErrors = 0;
    long timeToTarget = 0;
    int hitCount = 0;

    static final int ERROR_DELAY = 500;
    
	/* t = technique, b = background, mm = lens mag, f = file */
	public EvalAcqLabel(short t, short b, float mm, String f){
		initGUI();
		this.technique = t;
		this.background = b;
		this.magFactor = mm;
		this.TRIAL_FILE_NAME = f;
		mViewName = TECHNIQUE_NAMES[this.technique];
		eh = new AcqLabelEventHandler(this);
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
		if (background == BACKGROUND_MAP){
			mView.setBackgroundColor(Color.GRAY);
			vsm.addGlyph(new VImage(-3500, 2500, 0, (new ImageIcon("images/world/evalacqNW.png")).getImage(), 2.0f), mSpace);
			vsm.addGlyph(new VImage(-3500, -2500, 0, (new ImageIcon("images/world/evalacqSW.png")).getImage(), 2.0f), mSpace);
			vsm.addGlyph(new VImage(3500, 2500, 0, (new ImageIcon("images/world/evalacqNE.png")).getImage(), 2.0f), mSpace);
			vsm.addGlyph(new VImage(3500, -2500, 0, (new ImageIcon("images/world/evalacqSE.png")).getImage(), 2.0f), mSpace);			
		}
		else {
			mView.setBackgroundColor(Color.WHITE);
			try {
				File graphFile = new File("trials/eval_graph.svg");
				Document svgDoc = parse(graphFile);
				SVGReader.setPositionOffset(-8657, -6737); //fix this value on a 1600x1200 setup
	            SVGReader.load(svgDoc, vsm, mSpaceName, false, graphFile.toURI().toURL().toString());
				Vector glyphs = (Vector)mSpace.getAllGlyphs().clone();
				for (int i=0;i<glyphs.size();i++){
					if (glyphs.elementAt(i) instanceof DPath){
						vsm.addGlyph(new DPathS(((DPath)glyphs.elementAt(i)).getJava2DPathIterator(), 0, Color.BLACK, magFactor), mSpace);
						mSpace.removeGlyph((DPath)glyphs.elementAt(i));
					}
					else if (glyphs.elementAt(i) instanceof VCircle){
						VCircle c = (VCircle)glyphs.elementAt(i);
						vsm.addGlyph(new VCircleS(c.vx, c.vy, 0, c.vr, Color.WHITE, Color.BLACK, magFactor), mSpace);
						mSpace.removeGlyph(c);
					}
				}
			}
			catch ( Exception e) { 
				e.printStackTrace();
			}
		}
		latIndicatorW = new VRectangle(-7000, 0, 0, INDICATOR_LENGTH, INDICATOR_THICKNESS, INDICATOR_COLOR, INDICATOR_BORDER);
		//latIndicatorW.setDrawBorder(false);
		vsm.addGlyph(latIndicatorW, mSpace);
		latIndicatorE = new VRectangle(7000, 0, 0, INDICATOR_LENGTH, INDICATOR_THICKNESS, INDICATOR_COLOR, INDICATOR_BORDER);
		//latIndicatorE.setDrawBorder(false);
		vsm.addGlyph(latIndicatorE, mSpace);
		longIndicatorN = new VRectangle(0, 5000, 0, INDICATOR_THICKNESS, INDICATOR_LENGTH, INDICATOR_COLOR, INDICATOR_BORDER);
		//longIndicatorN.setDrawBorder(false);
		vsm.addGlyph(longIndicatorN, mSpace);
		longIndicatorS = new VRectangle(0, -5000, 0, INDICATOR_THICKNESS, INDICATOR_LENGTH, INDICATOR_COLOR, INDICATOR_BORDER);
		//longIndicatorS.setDrawBorder(false);
		vsm.addGlyph(longIndicatorS, mSpace);
	}

	void loadTrials(){
		try {
			File trialFile = new File(TRIAL_FILE_NAME);
			FileInputStream fis = new FileInputStream(trialFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			Vector trialsV = new Vector();
			while (line != null){
				if (line.length() > 0){
					trialsV.add(new LabelSequence(line));
				}
				line = br.readLine();
			}
			fis.close();
			trials = (LabelSequence[])trialsV.toArray(new LabelSequence[trialsV.size()]);
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
//	    if (WRITE_CINEMATIC){
//		bwc = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clogFile), "UTF-8"));
//		writeCinematicHeaders();
//	    }
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
				"Background" + OUTPUT_CSV_SEP +
				"Technique" + OUTPUT_CSV_SEP +
				"MM" + OUTPUT_CSV_SEP +
				"LbLength" + OUTPUT_CSV_SEP +
				"Opacity" + OUTPUT_CSV_SEP +
				"Rank" + OUTPUT_CSV_SEP +
				"Block" + OUTPUT_CSV_SEP +
				"Trial" + OUTPUT_CSV_SEP +
				"Time" + OUTPUT_CSV_SEP +
				"AcqErrors"  + OUTPUT_CSV_SEP +
				"ReadErrors");
			bwt.newLine();
			bwt.flush();
		}
		catch (IOException ex){ex.printStackTrace();}
	}

	void flushTrial(){
		try {
			bwt.write(subjectName + OUTPUT_CSV_SEP +
				subjectID + OUTPUT_CSV_SEP +
				BACKGROUND_NAMES[background] + OUTPUT_CSV_SEP +
				TECHNIQUE_NAMES_ABBR[technique] + OUTPUT_CSV_SEP +
				magFactor + OUTPUT_CSV_SEP +
				trials[trialCount].WORD_LENGTH + OUTPUT_CSV_SEP +
				trials[trialCount].getOpacityStr() + OUTPUT_CSV_SEP +
				trials[trialCount].RANK + OUTPUT_CSV_SEP +
				blockNumber + OUTPUT_CSV_SEP +
				trialCount + OUTPUT_CSV_SEP +
				timeToTarget + OUTPUT_CSV_SEP +
				nbAcqErrors + OUTPUT_CSV_SEP +
				nbReadErrors);
			bwt.newLine();
			bwt.flush();
		}
		catch (IOException ex){ex.printStackTrace();}
	}

	/* ------------ TRIAL MANAGEMENT ------------- */

	boolean sessionStarted = false;

	void startSession(){
		if (sessionStarted){return;}
		initLogs();
		initNextTrial();
		sessionStarted = true;
	}

	void endSession(){
		if (!sessionStarted){return;}
		try {
			bwt.flush();
			bwt.close();
			say(Messages.EOS);
			sessionStarted = false;
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
	highlight(hitCount, true);
	startTime = System.currentTimeMillis();
    }

	void endTrial(boolean success){
		clearTargets();
		trialStarted = false;
		unsetLens();
		mView.mouse.setSize(5);
		if (success){
			flushTrial();
			if (trialCount+1 < trials.length){
				initNextTrial();
			}
			else {
				endSession();
			}			
		}
		else {
			initSameTrial();
		}
	}

	void initSameTrial(){
		nbReadErrors++;
		initTrial();
	}
	
	void initNextTrial(){
		nbReadErrors = 0;
		trialCount++;
		initTrial();
	}

	void initTrial(){
		long x,y;
		targets = new VBTextST[NB_TARGETS_PER_TRIAL];
		double angle = trials[trialCount].RANK * Math.PI/4.0 - Math.PI/2.0;
		for (int i=0;i<NB_TARGETS_PER_TRIAL;i++){
			x = Math.round(TARGET_R_POS * Math.cos(angle));
			y = Math.round(TARGET_R_POS * Math.sin(angle));
			if (background == BACKGROUND_MAP){
				targets[i] = new VBTextST(x, y-20, 0, TARGET_COLOR_WM, TARGET_COLOR_WM, TARGET_BKG_COLOR_WM, trials[trialCount].LABELS[i], VBTextST.TEXT_ANCHOR_MIDDLE, targetAlpha);				
			}
			else {
				targets[i] = new VBTextST(x, y-20, 0, TARGET_COLOR_GR, TARGET_BKG_COLOR_GR, TARGET_BKG_COLOR_GR, trials[trialCount].LABELS[i], VBTextST.TEXT_ANCHOR_MIDDLE, targetAlpha);
			}
			if (background == BACKGROUND_MAP){
				targets[i].setSpecialFont((magFactor >= 10) ? LABEL_FONT_MM12_WM : LABEL_FONT_MM8_WM);				
			}
			else {
				targets[i].setSpecialFont((magFactor >= 10) ? LABEL_FONT_MM12_GR : LABEL_FONT_MM8_GR);
			}
			targets[i].setVisible(false);
			vsm.addGlyph(targets[i], mSpace);
			// lay out targets so that they between each side of the circle (ISO9241-9)
			if (i % 2 == 0){angle += Math.PI;}
			else {angle += 2 * Math.PI / ((double)NB_TARGETS_PER_TRIAL) - Math.PI;}
		}
		hitCount = 0;
		nbAcqErrors = 0;
		setTargetVisibility(trials[trialCount].getOpacity());
		for (int i=0;i<targets.length;i++){
			targets[i].setTranslucencyValue(1.0f);
		}
		showStartButton(true);
		say("Trial " + (trialCount+1) + " / " + trials.length + " - " + Messages.PSBTC);
	}
	
	void clearTargets(){
		for (int i=0;i<NB_TARGETS_PER_TRIAL;i++){
			if (targets[i] != null){
				mSpace.destroyGlyph(targets[i]);
			}
		}
	}

    long[] rif = new long[4];

	void clickOnTarget(){
		// do not take early clicks into account if an error is currently being displayed,
		// or if actual MM of dynamic lens is not high enough, or if translucence of a fading lens is too high
		if (warning
			|| (technique==TECHNIQUE_SCB && ((TFadingLens)lens).getFocusTranslucencyValue() < 0.4f)
			|| (technique==TECHNIQUE_SCF && ((SCFLens)lens).getActualMaximumMagnification() < 0.6f*lens.getMaximumMagnification())){return;}
		Glyph target = targets[hitCount];
		lens.getVisibleRegionInFocus(mCamera, rif);
		if (Math.sqrt(Math.pow((rif[0]+rif[2])/2.0-target.vx,2) + Math.pow((rif[3]+rif[1])/2.0-target.vy,2)) <= 0.66*(rif[2]-rif[0])-1.5*target.getSize()){
			// target is in focus region
			hitTarget();
		}
		else {
			warn(Messages.TARGET_NOT_IN_FOCUS, ERROR_DELAY);
			nbAcqErrors++;
			//	    writeCinematic();
		}
	}

	void hitTarget(){
		highlight(hitCount, false);
		hitCount++;
		if (hitCount < NB_TARGETS_PER_TRIAL){
			highlight(hitCount, true);
			vsm.repaintNow();
		}
		else {
			endTrial(false);
		}
	}

	void selectTarget(long t){
		if (hitCount+1 == trials[trialCount].RANK){
			hitTime = t;
			timeToTarget = hitTime - startTime;
			endTrial(true);
		}
		else {
			nbReadErrors++;
			warn(Messages.TARGET_NOT_RIGHT_ONE, ERROR_DELAY);
		}
	}

    static final int BRIGHT_HIGHLIGHT_TIME = 800;
    
    void highlight(final int targetIndex, boolean b){
	if (b){
	    latIndicatorW.vy = latIndicatorE.vy = targets[targetIndex].vy;
	    longIndicatorN.vx = longIndicatorS.vx = targets[targetIndex].vx;
	    targets[targetIndex].setVisible(true);
	    final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			targets[targetIndex].setTranslucencyValue(1.0f);
			targets[targetIndex].setBackgroundFillColor(HTARGET_COLOR);
			vsm.repaintNow();
			sleep(BRIGHT_HIGHLIGHT_TIME);
			targets[targetIndex].setBackgroundFillColor((background == BACKGROUND_MAP) ? TARGET_BKG_COLOR_WM : TARGET_BKG_COLOR_GR);
			targets[targetIndex].setTranslucencyValue(trials[trialCount].getOpacity());
			vsm.repaintNow();
			return null;
		    }
		};
	    worker.start();
	}
	else {
	    targets[targetIndex].setVisible(false);
	}
    }

    void setTargetVisibility(float a){
	targetAlpha = a;
	targetAlphaStr = Float.toString(targetAlpha);
    }
    
    /* -------------- lenses ----------------- */

    void setLens(int x, int y){
	switch(technique){
	case TECHNIQUE_SCB:{
	    tlens = new TFadingLens(magFactor, 0.0f, 0.95f, LENS_OUTER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    ((TFadingLens)tlens).setBoundaryColor((background == BACKGROUND_MAP) ? LENS_BOUNDARY_COLOR_WM : LENS_BOUNDARY_COLOR_GR);
	    ((TFadingLens)tlens).setObservedRegionColor((background == BACKGROUND_MAP) ? LENS_OBSERVED_REGION_COLOR_WM : LENS_OBSERVED_REGION_COLOR_GR);
	    lens = (FixedSizeLens)tlens;
	    break;
	}
	case TECHNIQUE_SCF:{
	    tlens = new SCFLens(magFactor, LENS_OUTER_RADIUS, LENS_INNER_RADIUS, x - panelWidth/2, y - panelHeight/2);
	    lens = (FixedSizeLens)tlens;
	    lens.setInnerRadiusColor((background == BACKGROUND_MAP) ? LENS_BOUNDARY_COLOR_WM : LENS_BOUNDARY_COLOR_GR);
	    lens.setOuterRadiusColor((background == BACKGROUND_MAP) ? LENS_BOUNDARY_COLOR_WM : LENS_BOUNDARY_COLOR_GR);
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
//	if (WRITE_CINEMATIC && trialStarted){
//	    writeCinematic();
//	}
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
	return !trialStarted &&
	    jpx >= panelWidth/2-10 &&
	    jpx <= panelWidth/2+10 &&
	    jpy >= panelHeight/2-10 &&
	    jpy <= panelHeight/2+10;
    }

    String instructions = null;
    boolean drawStartButton = false;
    
	/*Java2DPainter interface*/
	public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		g2d.setFont(INSTRUCTIONS_FONT);
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
		g2d.setFont(INDICATIONS_FONT);
		if (drawStartButton){
			g2d.setColor(START_BUTTON_COLOR);
			g2d.fillRect(viewWidth/2-10, viewHeight/2-10, 20, 20);
			g2d.setColor(Color.BLACK);
			g2d.fillRect(viewWidth/2-100, viewHeight/2-65, 200, 40);
			g2d.setColor(INSTRUCTIONS_COLOR);
			g2d.drawString(trials[trialCount].getTargetWord(), viewWidth/2-55, viewHeight/2-35);
		}
		if (trialStarted || drawStartButton){
			g2d.setColor(INSTRUCTIONS_COLOR);
			g2d.drawString(trials[trialCount].getTargetWord(), viewWidth/2-55, viewHeight-vispad[3]/2);
			g2d.drawString(trials[trialCount].getTargetWord(), viewWidth/2-55, vispad[3]/2);
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

	public static Document parse(File f){ 
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document res = builder.parse(f);
			return res;
		}
		catch (Exception e){e.printStackTrace();return null;}
	}

	public static void main(String[] args){
		try {
			if (args.length >= 6){
				EvalAcqLabel.VIEW_MAX_W = Integer.parseInt(args[4]);
				EvalAcqLabel.VIEW_MAX_H = Integer.parseInt(args[5]);
			}
			new EvalAcqLabel(Short.parseShort(args[0]), Short.parseShort(args[1]), Float.parseFloat(args[2]), args[3]);
		}
		catch (Exception ex){
			System.err.println("No cmd line parameter to indicate technique, defaulting to Speed-Coupled Blending Lens");
			new EvalAcqLabel(EvalAcqLabel.TECHNIQUE_SCB, EvalAcqLabel.BACKGROUND_MAP, 8, "trials/acqL.csv");
		}
	}

}
