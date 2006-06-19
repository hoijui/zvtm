/*   FILE: ZLAbstractTask.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import javax.swing.text.Style;

import java.util.Vector;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class ZLAbstractTask implements PostAnimationAction {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    
    static int ANIM_MOVE_LENGTH = 300;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;
    int hpanelWidth, hpanelHeight;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    AbstractTaskEventHandler eh;
    /* log manager (trials) */
    AbstractTaskLogManager logm;
    /* abstract world manager */
    AbstractWorldManager wm;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static final int GRID_ANIM_TIME = 500;
    static double DEFAULT_MAG_FACTOR = 6.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;

    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;
    ZSegment[][] hGridLevels = new ZSegment[GRID_DEPTH+1][];
    ZSegment[][] vGridLevels = new ZSegment[GRID_DEPTH+1][];
    Vector tmpHGrid;
    Vector tmpVGrid;

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    /* GRID */
    static final Color GRID_COLOR = new Color(156,53,53);
    static final int GRID_DEPTH = 12;
    int currentLevel = -1;

//     static final float START_ALTITUDE = 100000000000.0f;
    static final float FLOOR_ALTITUDE = 100.0f;

    boolean cameraOnFloor = false;

    int SELECTION_RECT_X = 0;
    int SELECTION_RECT_Y = 0;
    int SELECTION_RECT_W = 16;
    int SELECTION_RECT_H = 16;
    int SELECTION_RECT_HW = SELECTION_RECT_W / 2;
    int SELECTION_RECT_HH = SELECTION_RECT_H / 2;
    boolean SHOW_SELECTION_RECT = true;
    final static Color SELECTION_RECT_COLOR = Color.BLUE;
    
    static java.awt.Robot robot;

    final static short ZL_TECHNIQUE = 0;  // Zoom + Lens
    final static short PZ_TECHNIQUE = 1;  // Pan Zoom centered on view
    final static short PZL_TECHNIQUE = 2;  // Pan Zoom centered on view

    final static String PZ_TECHNIQUE_NAME = "Pan-Zoom (centered on view)";
    final static String ZL_TECHNIQUE_NAME = "Probing Lens";
    final static String PZL_TECHNIQUE_NAME = "Region Zoom (animated transitions)";
    short technique = ZL_TECHNIQUE;
    String techniqueName;

    static final int[] vispad = {100,100,100,100};

    ZLAbstractTask(short t){
	vsm = new VirtualSpaceManager();
	vsm.setDebug(true);
	init(t);
    }

    public void init(short t){
	try {
	    robot = new java.awt.Robot();
	}
	catch(java.awt.AWTException ex){ex.printStackTrace();}
	this.technique = t;
	if (this.technique == ZL_TECHNIQUE){
 	    eh = new AbstractTaskZLEventHandler(this);
	    techniqueName = ZL_TECHNIQUE_NAME;
	}
	else if (this.technique == PZ_TECHNIQUE){
 	    eh = new AbstractTaskPZEventHandler(this);
	    techniqueName = PZ_TECHNIQUE_NAME;
	}
	else if (this.technique == PZL_TECHNIQUE){
 	    eh = new AbstractTaskPZLEventHandler(this);
	    techniqueName = PZL_TECHNIQUE_NAME;
	}
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, techniqueName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	demoView.setVisibilityPadding(vispad);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	robot.mouseMove(VIEW_X+VIEW_W/2, VIEW_Y+VIEW_H/2);
	updatePanelSize();
	demoView.setEventHandler(eh);
	demoView.getPanel().addComponentListener(eh);
	demoView.setNotifyMouseMoved(true);
//  	demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
// 	demoCamera.setAltitude(START_ALTITUDE);
	wm = new AbstractWorldManager(this);
	wm.generateWorld();
 	buildGrid();
	logm = new AbstractTaskLogManager(this);
	getGlobalView();
	System.gc();
	logm.im.say(LocateTask.PSTS);
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    /*uncomment the following line if the Windows toolbar should be visible*/
	    // SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void buildGrid(){
	// frame
	ZSegment s = new ZSegment(-AbstractWorldManager.HALF_WORLD_WIDTH, 0, 0, 0, AbstractWorldManager.HALF_WORLD_HEIGHT, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(AbstractWorldManager.HALF_WORLD_WIDTH, 0, 0, 0, AbstractWorldManager.HALF_WORLD_HEIGHT, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, -AbstractWorldManager.HALF_WORLD_HEIGHT, 0, AbstractWorldManager.HALF_WORLD_WIDTH, 0, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, AbstractWorldManager.HALF_WORLD_HEIGHT, 0, AbstractWorldManager.HALF_WORLD_WIDTH, 0, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	// grid (built recursively, max. rec depth control by GRID_DEPTH)
	tmpHGrid = new Vector();
	tmpVGrid = new Vector();
	buildHorizontalGridLevel(-AbstractWorldManager.HALF_WORLD_HEIGHT, AbstractWorldManager.HALF_WORLD_HEIGHT, 0);
	buildVerticalGridLevel(-AbstractWorldManager.HALF_WORLD_WIDTH, AbstractWorldManager.HALF_WORLD_WIDTH, 0);
	storeGrid();
	showGridLevel(1);
    }

    void buildHorizontalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(0, c, 0, AbstractWorldManager.HALF_WORLD_WIDTH, 0, GRID_COLOR);
	storeSegmentInHGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	s.setVisible(false);
	if (depth < GRID_DEPTH){
	    buildHorizontalGridLevel(c1, c, depth+1);
	    buildHorizontalGridLevel(c, c2, depth+1);
	}
    }

    void buildVerticalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(c, 0, 0, 0, AbstractWorldManager.HALF_WORLD_HEIGHT, GRID_COLOR);
	storeSegmentInVGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	s.setVisible(false);
	if (depth < GRID_DEPTH+1){// not GRID_DEPTH because we want to maintain the same spacing between parallels
	    buildVerticalGridLevel(c1, c, depth+1);// and meridians and map is twice as large as it is high
	    buildVerticalGridLevel(c, c2, depth+1);
	}
    }

    void storeSegmentInHGrid(ZSegment s, int depth){
	if (tmpHGrid.size() > depth){
	    Vector v = (Vector)tmpHGrid.elementAt(depth);
	    v.add(s);
	}
	else {
	    Vector v = new Vector();
	    v.add(s);
	    tmpHGrid.add(v);
	}
    }

    void storeSegmentInVGrid(ZSegment s, int depth){
	if (tmpVGrid.size() > depth){
	    Vector v = (Vector)tmpVGrid.elementAt(depth);
	    v.add(s);
	}
	else {
	    Vector v = new Vector();
	    v.add(s);
	    tmpVGrid.add(v);
	}
    }

    void storeGrid(){
	int levelSize;
	Vector v;
	for (int i=0;i<tmpHGrid.size();i++){
	    v = (Vector)tmpHGrid.elementAt(i);
	    levelSize = v.size();
	    hGridLevels[i] = new ZSegment[levelSize];
	    for (int j=0;j<v.size();j++){
		hGridLevels[i][j] = (ZSegment)v.elementAt(j);
	    }
	}
	/* levels 0 and 1 are merged in a single level to keep a 1:1 ratio
	   between parallels and meridians */
	for (int i=1;i<tmpVGrid.size();i++){
	    v = (Vector)tmpVGrid.elementAt(i);
	    levelSize = v.size();
	    vGridLevels[i-1] = new ZSegment[levelSize];
	    for (int j=0;j<v.size();j++){
		vGridLevels[i-1][j] = (ZSegment)v.elementAt(j);
	    }
	}
	/* putting top-level meridian as first element of level just below,
	   which then becomes the top level for meridians*/ 
	ZSegment[] level0 = new ZSegment[vGridLevels[0].length+1];
	System.arraycopy(vGridLevels[0], 0, level0, 1, vGridLevels[0].length);
	level0[0] = (ZSegment)((Vector)tmpVGrid.elementAt(0)).elementAt(0);
	vGridLevels[0] = level0;
    }

    /*incremental display of the grid*/
    void showGridLevel(int level){
	if (level > GRID_DEPTH || level < -1 || level == currentLevel){
	    return;
	}
	if (level < currentLevel){
	    for (int i=level+1;i<=currentLevel;i++){
		for (int j=0;j<hGridLevels[i].length;j++){
		    hGridLevels[i][j].setVisible(false);
		}
	    }
	    for (int i=level+1;i<=currentLevel;i++){
		for (int j=0;j<vGridLevels[i].length;j++){
		    vGridLevels[i][j].setVisible(false);
		}
	    }
	}
	else if (level > currentLevel){
	    for (int i=currentLevel+1;i<=level;i++){
		for (int j=0;j<hGridLevels[i].length;j++){
		    hGridLevels[i][j].setVisible(true);
		}
	    }
	    for (int i=currentLevel+1;i<=level;i++){
		for (int j=0;j<vGridLevels[i].length;j++){
		    vGridLevels[i][j].setVisible(true);
		}
	    }
	}
	currentLevel = level;
    }

    void setLens(int t){
	eh.lensType = t;
	switch (eh.lensType){
	case AbstractTaskEventHandler.ZOOMIN_LENS:{
	    logm.lensStatus = AbstractTaskLogManager.ZOOMIN_LENS;
	    logm.lensPositionChanged(true);
	    break;
	}
	case AbstractTaskEventHandler.ZOOMOUT_LENS:{
	    logm.lensStatus = AbstractTaskLogManager.ZOOMOUT_LENS;
	    logm.lensPositionChanged(true);
	    break;
	}
	case AbstractTaskEventHandler.NO_LENS:{
	    logm.lensStatus = AbstractTaskLogManager.NO_LENS;
	    logm.lensxS = AbstractTaskLogManager.NaN;
	    logm.lensyS = AbstractTaskLogManager.NaN;
	    break;
	}
	}
    }

    void moveLens(int x, int y, boolean write){
	lens.setAbsolutePosition(x, y);
	logm.lensPositionChanged(write);
	vsm.repaintNow();
    }

    void zoomInRegion(long[] wnes){
	long c2x = (wnes[2]+wnes[0]) / 2;  //new coords where camera should go
	long c2y = (wnes[1]+wnes[3]) / 2;
	long[] regionBounds = demoView.getVisibleRegion(demoCamera);
	// region that will be visible after translation, but before zoom/unzoom
	// (need to compute zoom) ; we only take left and down because we only need
	// horizontal and vertical ratios, which are equals for left and right, up and down
	long[] newRegionDimensions = {regionBounds[0] + c2x - demoCamera.posx,
				  regionBounds[3] + c2y - demoCamera.posy};
	float ratio = 0;
	// compute the mult factor for altitude to see all stuff on X
	if (newRegionDimensions[0] != 0){
	    ratio = (c2x-wnes[0]) / ((float)(c2x-newRegionDimensions[0]));
	}
	// same for Y ; take the max of both
	if (newRegionDimensions[1] != 0){
	    float tmpRatio = (c2y-wnes[3]) / ((float)(c2y-newRegionDimensions[1]));
	    if (tmpRatio > ratio){ratio = tmpRatio;}
	}
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	Float deltAlt = new Float(cameraAbsAlt * Math.abs(ratio) - cameraAbsAlt);
	Vector cadata = new Vector();
	if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
	    cadata.add(deltAlt);
	    cadata.add(new LongPoint(c2x-demoCamera.posx, c2y-demoCamera.posy));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio2 = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio2),
				     Math.round((c2y-demoCamera.posy)*ratio2)));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
	}
    }

    void zoomOutOfRegion(long[] wnes){
	// only zooming out, no translation
	long[] viewportRegion = demoView.getVisibleRegion(demoCamera);
	float ratio = Math.min((viewportRegion[2]-viewportRegion[0])/((float)(wnes[2]-wnes[0])),
			       (viewportRegion[1]-viewportRegion[3])/((float)(wnes[1]-wnes[3])));
	float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
	Float deltAlt = new Float((ratio-1) * cameraAbsAlt);
	Vector cadata = new Vector();
	cadata.add(deltAlt);
	cadata.add(new LongPoint(0,0));
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
	    Dimension d = demoView.getPanel().getSize();
 	    lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2,
						       x - d.width/2,
						       y - d.height/2));
	    lens.setBufferThreshold(1.5f);
	}
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	setLens(AbstractTaskEventHandler.ZOOMIN_LENS);
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
					     lens.getID(), new AbstractTaskZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
				     Math.round((c2y-demoCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new AbstractTaskZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
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
	    Dimension d = demoView.getPanel().getSize();
	    lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2,
						       x - d.width/2,
						       y - d.height/2));
	    lens.setBufferThreshold(1.5f);
	}
	// animate lens and camera simultaneously
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, demoCamera.getID(), null);
	setLens(AbstractTaskEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					 lens.getID(), new AbstractTaskZOP2LensAction(this));
    }

    void setMagFactor(double m){
	MAG_FACTOR = m;
	INV_MAG_FACTOR = 1 / MAG_FACTOR;
	if (logm.trialStarted){
	    logm.lensmmS = TrialInfo.doubleFormatter(MAG_FACTOR);
	    logm.writeCinematic();
	}
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca){
	synchronized (lens){
	    double nmf = MAG_FACTOR + magOffset;
	    if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
		setMagFactor(nmf);
		if (zooming == AbstractTaskEventHandler.ZOOMOUT_LENS){
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

    void altitudeChanged(){
	long[] wnes = demoView.getVisibleRegion(demoCamera);
	updateGridLevel(Math.max(wnes[2]-wnes[0], wnes[1]-wnes[3]));
    }

    void updateGridLevel(long visibleSize){
	if (visibleSize < 9765625.0f){
	    showGridLevel(12);
	}
	else if (visibleSize < 19531250.0f){
	    showGridLevel(11);
	}
	else if (visibleSize < 39062500.0f){
	    showGridLevel(10);
	}
	else if (visibleSize < 78125000.0f){
	    showGridLevel(9);
	}
	else if (visibleSize < 156250000.0f){
	    showGridLevel(8);
	}
	else if (visibleSize < 312500000.0f){
	    showGridLevel(7);
	}
	else if (visibleSize < 625000000.0f){
	    showGridLevel(6);
	}
	else if (visibleSize < 1250000000.0f){
	    showGridLevel(5);
	}
	else if (visibleSize < 2500000000.0f){
	    showGridLevel(4);
	}
	else if (visibleSize < 5000000000.0f){
	    showGridLevel(3);
	}
	else if (visibleSize < 10000000000.0f){
	    showGridLevel(2);
	}
	else {
	    showGridLevel(1);
	}
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(demoCamera,ANIM_MOVE_LENGTH);
    }

    int CENTER_CROSS_SIZE = 15;
    int CENTER_W = 0;
    int CENTER_N = 0;
 
    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
	hpanelWidth = panelWidth / 2;
	hpanelHeight = panelHeight / 2;
	SELECTION_RECT_X = panelWidth/2 - SELECTION_RECT_W / 2;
	SELECTION_RECT_Y = panelHeight/2 - SELECTION_RECT_H / 2;
	CENTER_W = hpanelWidth - CENTER_CROSS_SIZE / 2;
	CENTER_N = hpanelHeight - CENTER_CROSS_SIZE / 2;
    }

    void cameraIsOnFloor(boolean b){
	if (b != cameraOnFloor){
	    cameraOnFloor = b;
	}
    }

    /*Post animation action interface (grid)*/
    public void animationEnded(Object target, short type, String dimension){
	((Glyph)target).setVisible(false);
    }

    void gc(){
	System.gc();
    }

    public static void main(String[] args){
	/* First argument is the technique: see ZL_TECHNIQUE, PZ_TECHNIQUE, PZA_TECHNIQUE, PZL_TECHNIQUE or SS_TECHNIQUE for appropriate values
	   Second argument is either 1 (show console for messages) or 0 (don't show it)
	   Third argument is either 1 (show map manager monitor) or 0 (don't show it) */
	short tech = (args.length > 0) ? Short.parseShort(args[0]) : ZL_TECHNIQUE;
	new ZLAbstractTask(tech);
    }
    
}
