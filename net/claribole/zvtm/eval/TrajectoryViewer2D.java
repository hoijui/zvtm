/*   FILE: TrajectoryViewer2D.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TrajectoryViewer2D.java,v 1.7 2006/06/02 12:17:21 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class TrajectoryViewer2D implements Java2DPainter {

    static final int TIME_STEP = 20;

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 700;

    static final String VIEW_TITLE = "Trajectory View (2D)";

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H, VIEW_X, VIEW_Y;

    /* main 8000x4000 map (always displayed) */
    VImage mainMap;
    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;

    static final float START_ALTITUDE = 6250;
    static final float FLOOR_ALTITUDE = 100.0f;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    TV2DEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    // player
    TrajectoryPlayer2D tp;

    /* holds trajectory data */
    CinematicInfo[] ci;
    VRectangle[] trajectory;

    // information about block displayed on screen
    String info = "";
    String infoHeader;
    String subjectName;
    String subjectID;
    String blockNumber;
    String technique;
    // information about viewport (required to compute region seen through camera from altitude data)
    int viewportWidth;
    int viewportHeight;

    String ID;

    boolean LOAD_ANSWER = true;

    public TrajectoryViewer2D(){
	vsm = new VirtualSpaceManager();
	init();
    }

    void init(){
	eh = new TV2DEventHandler(this);
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, VIEW_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	updatePanelSize();
	demoView.setEventHandler(eh);
	demoView.getPanel().addComponentListener(eh);
	demoView.setNotifyMouseMoved(true);
	demoCamera.setAltitude(START_ALTITUDE);
	demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	tp = new TrajectoryPlayer2D(this);
	info = LocateTask2.LOADING_WORLDMAP_TEXT;
    	initMap();
	System.gc();
	info = "";
    }

    void initMap(){
	mainMap = new VImage(MapData.M1000x, MapData.M1000y, 0,
 			     (new ImageIcon(MapData.M1000path)).getImage(),
			     MapData.MN000factor.doubleValue());
	mainMap.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(mainMap, LocateTask.mainVSname);
	mainVS.atBottom(mainMap);
    }

    void loadFile(){
	JFileChooser fc = new JFileChooser(new File(LogManager.LOG_DIR_FULL));
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle("Select Log File");
	int returnVal = fc.showOpenDialog(demoView.getFrame());
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    parseCinematics(fc.getSelectedFile());
	    displayTrajectory();
	}
    }

    void reset(){
	Vector v = mainVS.getAllGlyphs();
	v = (Vector)v.clone();
	Glyph g;
	for (int i=0;i<v.size();i++){
	    g = (Glyph)v.elementAt(i);
	    if (g != mainMap){
		mainVS.destroyGlyph(g);
	    }
	}
	tp.reset();
    }
    
    void parseCinematics(File f){
	String whichTrial = JOptionPane.showInputDialog("Which trial do you want to visualize?");
	if (whichTrial == null){return;}
	try {
	    FileInputStream fis = new FileInputStream(f);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    String line = br.readLine();
	    subjectName = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    subjectID = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    technique = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    line = br.readLine();
	    blockNumber = line.split(LogManager.OUTPUT_CSV_SEP)[1];
	    infoHeader = "SID=" + subjectID + " - Block=" + blockNumber + " - Trial=" + whichTrial + " - " + technique;
	    line = br.readLine();
	    viewportWidth = Integer.parseInt(line.substring(5));
	    line = br.readLine();
	    viewportHeight = Integer.parseInt(line.substring(5));
	    br.readLine(); // next line contains column headers
	    line = br.readLine();
	    Vector v = new Vector();
	    String items[];
	    ID = null;
	    while (line != null){
		items = line.split(LogManager.OUTPUT_CSV_SEP);
		if (items[0].equals(whichTrial)){
		    v.add(new CinematicInfo(items[0], items[2], items[3], items[4], items[5],
					    items[6], items[7], items[8], viewportWidth, viewportHeight,
					    items[9]));
		    if (ID == null){
			double dist = Double.parseDouble(items[1]);
			ID = Long.toString(Math.round(Math.log(1+dist/LogManager.TARGET_WIDTH)/Math.log(2)));
		    }
		}
		line = br.readLine();
	    }
	    infoHeader += " - ID="+ ID;


 	    Vector v2 = new Vector();
	    long time = 0;
	    long duration = ((CinematicInfo)v.lastElement()).time;
	    int currentIndex = 0;
	    CinematicInfo c1,c2;
	    while (time < duration && currentIndex < v.size()){
		c1 = (CinematicInfo)v.elementAt(currentIndex);
		c2 = (CinematicInfo)v.elementAt(currentIndex+1);
		if (time >= c2.time){
		    currentIndex++;
		    v2.add(c2);
		}
		else {
		    v2.add(c1);
		}
		time += TIME_STEP;
	    }
	    v2.add(v.lastElement());
	    ci = new CinematicInfo[v2.size()];
	    for (int i=0;i<ci.length;i++){
		ci[i] = (CinematicInfo)v2.elementAt(i);
	    }
	    v.clear();
	    info = infoHeader;
	    if (LOAD_ANSWER){
		loadAnswer(f, whichTrial);
	    }
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void displayTrajectory(){
	trajectory = new VRectangle[ci.length];
	for (int i=0;i<ci.length;i++){
	    trajectory[i] = new VRectangle(ci[i].cx,ci[i].cy,0,5,5,Color.RED);
	    trajectory[i].setPaintBorder(false);
	    vsm.addGlyph(trajectory[i], mainVS);
	}
    }

    void loadAnswer(File cinematicFile, String trialNumber){
	String cfap = cinematicFile.getAbsolutePath();
	int i = cfap.indexOf("cinematic");
	try {
	    FileInputStream fis = new FileInputStream(new File(cfap.substring(0, i) + "trial" + cfap.substring(i+9)));
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	    String line = br.readLine();
	    String[] items;
	    while (line != null){
		items = line.split(LogManager.OUTPUT_CSV_SEP);
		if (items[4].equals(trialNumber)){
		    long nwx = Long.parseLong(items[18]);
		    long nwy = Long.parseLong(items[17]);
		    long sex = Long.parseLong(items[20]);
		    long sey = Long.parseLong(items[19]);
		    VRectangle r = new VRectangle((nwx+sex)/2, (nwy+sey)/2, 0, (sex-nwx)/2, (nwy-sey)/2, Color.YELLOW);
		    r.setBorderColor(Color.YELLOW);
		    r.setFill(false);
		    vsm.addGlyph(r, mainVS);
		    break;
		}
		line = br.readLine();
	    }
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    void playOrPause(){
	if (!tp.animationStarted){
	    tp.startAnimation(ci);
	}
	else if (!tp.animationEnded){
	    tp.pauseOrResume();
	}
    }

    void setLens(int t){
	eh.lensType = t;
    }

    void moveLens(int x, int y, boolean write){
	lens.setAbsolutePosition(x, y);
	vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
 	    lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2));
	    lens.setBufferThreshold(1.5f);
	}
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	setLens(WorldTaskEventHandler.ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
	Vector cadata = new Vector();
	// -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
	Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
	if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
	    cadata.add(deltAlt);
	    cadata.add(new LongPoint(c2x-demoCamera.posx, c2y-demoCamera.posy));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new TV2DZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), null);
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
				     Math.round((c2y-demoCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new TV2DZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), null);
	}
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
	Vector cadata = new Vector();
	cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
	cadata.add(new LongPoint(c2x-demoCamera.posx, c2y-demoCamera.posy));
	// create lens if it does not exist
	if (lens == null){
	    lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2));
	    lens.setBufferThreshold(1.5f);
	}
	// animate lens and camera simultaneously
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, demoCamera.getID(), null);
	setLens(WorldTaskEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					 lens.getID(), new TV2DZP2LensAction(this));
    }

    void setMagFactor(double m){
	MAG_FACTOR = m;
	INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca){
	synchronized (lens){
	    double nmf = MAG_FACTOR + magOffset;
	    if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
		setMagFactor(nmf);
		if (zooming == WorldTaskEventHandler.ZOOMOUT_LENS){
		    /* if unzooming, we want to keep the focus point stable, and unzoom the context
		       this means that camera altitude must be adjusted to keep altitude + lens mag
		       factor constant in the lens focus region. The camera must also be translated
		       to keep the same region of the virtual space under the focus region */
		    float a1 = demoCamera.getAltitude();
		    lens.setMaximumMagnification((float)nmf, true);
		    /* explanation for the altitude offset computation: the projected size of an object
		       in the focus region (in lens space) should remain the same before and after the
		       change of magnification factor. The size of an object is a function of the
		       projection coefficient (see any Glyph.projectForLens() method). This means that
		       the following equation must be true, where F is the camera's focal distance, a1
		       is the camera's altitude before the move and a2 is the camera altitude after the
		       move:
		       MAG_FACTOR * F / (F + a1) = MAG_FACTOR + magOffset * F / (F + a2)
		       From this we can get the altitude difference (a2 - a1)                       */
		    demoCamera.altitudeOffset((float)((a1+demoCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
		    /* explanation for the X offset computation: the position in X of an object in the
		       focus region (lens space) should remain the same before and after the change of
		       magnification factor. This means that the following equation must be true (taken
		       by simplifying pc[i].lcx computation in a projectForLens() method):
		       (vx-(lensx1))*coef1 = (vx-(lensx2))*coef2
		       -- coef1 is actually MAG_FACTOR * F/(F+a1)
		       -- coef2 is actually (MAG_FACTOR + magOffset) * F/(F+a2)
		       -- lensx1 is actually camera.posx1 + ((F+a1)/F) * lens.lx
		       -- lensx2 is actually camera.posx2 + ((F+a2)/F) * lens.lx
		       Given that (MAG_FACTOR + magOffset) / (F+a2) = MAG_FACTOR / (F+a1)
		       we eventually have:
		       Xoffset = (a1 - a2) / F * lens.lx   (lens.lx being the position of the lens's center in
		       JPanel coordinates w.r.t the view's center - see Lens.java)
		    */
		    demoCamera.move(Math.round((a1-demoCamera.getAltitude())/demoCamera.getFocal()*lens.lx),
				    -Math.round((a1-demoCamera.getAltitude())/demoCamera.getFocal()*lens.ly));
		}
		else {
		    vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
						     lens.getID(), null);
		}
	    }
	}
    }

    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    void brieflySay(final String s, final int delay){
	/* display some message for delay ms
	   and revert back to previous message */
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    String oldInfo = info;
		    info = s;
		    vsm.repaintNow();
		    sleep(delay);
		    info = oldInfo;
		    vsm.repaintNow();
		    return null; 
		}
	    };
	worker.start();
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(Color.RED);
	g2d.drawString(info, 20, 30);
	tp.paint(g2d, viewWidth, viewHeight);
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = SCREEN_HEIGHT;
	if (VIEW_H > VIEW_MAX_H){VIEW_H = VIEW_MAX_H;}
	if (Utilities.osIsMacOS()){
	    VIEW_H -= 22;
	}
    }

    public static void main(String[] args){
	new TrajectoryViewer2D();
    }

}