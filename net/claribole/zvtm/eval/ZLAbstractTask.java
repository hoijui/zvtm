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
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Point;
import javax.swing.text.Style;

import java.util.Vector;

import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class ZLAbstractTask implements PostAnimationAction, Java2DPainter {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    static final Font DEFAULT_FONT = new Font("Dialog",Font.PLAIN,10);

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

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 60;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static final int GRID_ANIM_TIME = 500;
    static double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;

    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;
    ZSegment[][] hGridLevels = new ZSegment[GRID_DEPTH+1][];
    ZSegment[][] vGridLevels = new ZSegment[GRID_DEPTH+1][];
    Vector tmpHGrid;
    Vector tmpVGrid;

    Camera portalCamera;
    /* DragMag */
    static final int DM_PORTAL_WIDTH = 200;
    static final int DM_PORTAL_HEIGHT = 200;
    static final int DM_PORTAL_INITIAL_X_OFFSET = 150;
    static final int DM_PORTAL_INITIAL_Y_OFFSET = 150;
    DraggableCameraPortal dmPortal;
    VRectangle dmRegion;
    int dmRegionW, dmRegionN, dmRegionE, dmRegionS;
    boolean paintLinks = false;

    OverviewPortal ovPortal;

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    /* GRID */
    static final Color GRID_COLOR = new Color(156,53,53);
    static final int GRID_DEPTH = 5;
    int currentLevel = -1;

    static final String GLYPH_TYPE_GRID = "G";
    static final String GLYPH_TYPE_WORLD = "W";

    static final float START_ALTITUDE = 20000.0f;
    static final float FLOOR_ALTITUDE = 300.0f;

    boolean cameraOnFloor = false;

    int SELECTION_RECT_X = 0;
    int SELECTION_RECT_Y = 0;
    int SELECTION_RECT_W = 16;
    int SELECTION_RECT_H = 16;
    int SELECTION_RECT_HW = SELECTION_RECT_W / 2;
    int SELECTION_RECT_HH = SELECTION_RECT_H / 2;
    boolean SHOW_SELECTION_RECT = true;
    final static Color SELECTION_RECT_COLOR = Color.RED;
    
    static java.awt.Robot robot;

    final static short PZ_TECHNIQUE = 1;  // Pan Zoom centered on view
    final static short PZO_TECHNIQUE = 2;  // Pan Zoom centered on view + overview
    final static short PZL_TECHNIQUE = 3;  // Pan Zoom + Probing Lenses
    final static short DM_TECHNIQUE = 4;  // Drag Mag

    final static String PZ_TECHNIQUE_NAME = "Pan-Zoom";
    final static String PZO_TECHNIQUE_NAME = "Pan-Zoom + Overview";
    final static String PZL_TECHNIQUE_NAME = "Pan Zoom + Probing Lenses";
    final static String DM_TECHNIQUE_NAME = "Drag Mag";
    short technique = PZ_TECHNIQUE;
    String techniqueName;

    static final int[] vispad = {100,100,100,100};

    /* world */
    static int TREE_DEPTH = 2; // we want subjects to navigate through 4 levels, includes root
    static int DENSITY = 3;

    static final long SMALLEST_ELEMENT_WIDTH = 500;
    static final long MUL_FACTOR = 50;
    static final double ROUND_CORNER_RATIO = 5;

    static long WORLD_WIDTH;
    static long WORLD_HEIGHT;
    static long HALF_WORLD_WIDTH;
    static long HALF_WORLD_HEIGHT;

    static final Color DEEPEST_LEVEL_COLOR = Color.WHITE;
    static final Color DISC_BORDER_COLOR = Color.BLACK;
    static final Color VISITED_BORDER_COLOR = Color.RED;

    static final float NEXT_LEVEL_VIS_FACTOR = 2.0f; /* this controls the minimum size of the parent object for its content to
							appear (provided all necessary conditions for it to appear are met) */
    static long[] widthByLevel;
    static int[] cornerByLevel;
    static LongPoint[][] offsetsByLevel;
    ZRoundRect[][] elementsByLevel;
    static final Color[] COLOR_BY_LEVEL = {Color.GRAY, Color.WHITE};
    boolean[][] visitsByLevel = new boolean[TREE_DEPTH][DENSITY*DENSITY];
    static {
	// compute size of all levels
	widthByLevel = new long[TREE_DEPTH];
	cornerByLevel = new int[TREE_DEPTH];
	offsetsByLevel = new LongPoint[TREE_DEPTH][DENSITY*DENSITY];
	for (int i=0;i<TREE_DEPTH;i++){
	    widthByLevel[i] = SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(MUL_FACTOR, (TREE_DEPTH-i-1)));
	    cornerByLevel[i] = ((int)Math.round(widthByLevel[i]/ROUND_CORNER_RATIO));
	    if (i>0){
		long step = Math.round(widthByLevel[i-1]/((double)DENSITY)) * 2;
		long y = widthByLevel[i-1] - step/2;
		for (int j=0;j<DENSITY;j++){
		    long x = -widthByLevel[i-1] + step/2;
		    for (int k=0;k<DENSITY;k++){
			offsetsByLevel[i][j*DENSITY+k] = new LongPoint(x, y);
			x += step;
		    }
		    y -= step;
		}
	    }
	}
	WORLD_WIDTH = widthByLevel[0] * 2;
	WORLD_HEIGHT = WORLD_WIDTH;
	HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
	HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
// 	for (int i=0;i<16;i++){
// 	    System.err.println(";"+Math.round(Math.random()*8*ZLAbstractTask.widthByLevel[0]-4*ZLAbstractTask.widthByLevel[0])+";"+Math.round(Math.random()*8*ZLAbstractTask.widthByLevel[0]-4*ZLAbstractTask.widthByLevel[0]));
// 	}
    }

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
	windowLayout();
	this.technique = t;
	if (this.technique == PZ_TECHNIQUE){
 	    eh = new AbstractTaskPZEventHandler(this);
	    techniqueName = PZ_TECHNIQUE_NAME;
	}
	if (this.technique == PZO_TECHNIQUE){
 	    eh = new AbstractTaskPZOEventHandler(this);
	    techniqueName = PZO_TECHNIQUE_NAME;
	}
	else if (this.technique == PZL_TECHNIQUE){
 	    eh = new AbstractTaskPZLEventHandler(this);
	    techniqueName = PZL_TECHNIQUE_NAME;
	}
	else if (this.technique == DM_TECHNIQUE){
 	    eh = new AbstractTaskDMEventHandler(this);
	    techniqueName = DM_TECHNIQUE_NAME;
	}
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit((int)FLOOR_ALTITUDE);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	portalCamera = vsm.addCamera(mainVSname);
	demoView = vsm.addExternalView(cameras, techniqueName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	logm = new AbstractTaskLogManager(this);
	demoView.setEventHandler(eh);
	demoView.setVisibilityPadding(vispad);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	robot.mouseMove(VIEW_X+VIEW_W/2, VIEW_Y+VIEW_H/2);
	updatePanelSize();
	demoView.getPanel().addComponentListener(eh);
	demoView.setNotifyMouseMoved(true);
	demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
	buildWorld();
    	buildGrid();
	if (this.technique == DM_TECHNIQUE){
	    initDM();
	}
	else if (this.technique == PZO_TECHNIQUE){
	    initOverview();
	}
	System.gc();
	logm.im.say(LocateTask.PSTS);
    }

    void initDM(){
	dmRegion = new VRectangle(0,0,0,1,1,Color.RED);
	dmRegion.setFill(false);
	dmRegion.setBorderColor(Color.RED);
	vsm.addGlyph(dmRegion, mainVS);
	mainVS.hide(dmRegion);
    }

    void initOverview(){
	ovPortal = new OverviewPortal(VIEW_W-AbstractTaskInstructionsManager.PADDING-DM_PORTAL_WIDTH,
				      VIEW_H-AbstractTaskInstructionsManager.PADDING-DM_PORTAL_HEIGHT,
				      DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera, demoCamera);
	ovPortal.setPortalEventHandler((PortalEventHandler)eh);
	ovPortal.setBackgroundColor(Color.LIGHT_GRAY);
	vsm.addPortal(ovPortal, demoView);
	ovPortal.setBorder(Color.BLACK);
	updateOverview();
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

    void buildWorld(){
	elementsByLevel = new ZRoundRect[TREE_DEPTH][DENSITY*DENSITY];
	elementsByLevel[0][0] = new ZRoundRect(0, 0, 0,
					       widthByLevel[0], widthByLevel[0],
					       COLOR_BY_LEVEL[0],
					       cornerByLevel[0], cornerByLevel[0], false);
	vsm.addGlyph(elementsByLevel[0][0], mainVS);
	elementsByLevel[0][0].setPaintBorder(false);
	for (int i=1;i<TREE_DEPTH;i++){
	    for (int j=0;j<DENSITY;j++){
		for (int k=0;k<DENSITY;k++){
		    elementsByLevel[i][j*DENSITY+k] = new ZRoundRect(offsetsByLevel[i][j*DENSITY+k].x,
								     offsetsByLevel[i][j*DENSITY+k].y,
								     0,
								     widthByLevel[i], widthByLevel[i],
								     COLOR_BY_LEVEL[1],
								     cornerByLevel[i], cornerByLevel[i], false);
		    vsm.addGlyph(elementsByLevel[i][j*DENSITY+k], mainVS);
		    elementsByLevel[i][j*DENSITY+k].setPaintBorder(true); // actual drawing of the border will depend on the rendering size for each rectangle
		    elementsByLevel[i][j*DENSITY+k].setBorderColor(DISC_BORDER_COLOR);
		}
	    }
	}
	resetVisits();
 	hideTargets();
    }

    void resetWorld(){
	for (int i=2;i<TREE_DEPTH;i++){
	    for (int j=0;j<DENSITY;j++){
		for (int k=0;k<DENSITY;k++){
		    elementsByLevel[i][j*DENSITY+k].moveTo(offsetsByLevel[i][j*DENSITY+k].x,
								     offsetsByLevel[i][j*DENSITY+k].y);
		}
	    }
	}
	for (int i=1;i<TREE_DEPTH;i++){
	    for (int j=0;j<DENSITY*DENSITY;j++){
		elementsByLevel[i][j].setColor(COLOR_BY_LEVEL[i]);
		elementsByLevel[i][j].setBorderColor(DISC_BORDER_COLOR);
	    }
	}
	resetVisits();
 	hideTargets();
    }

    void resetVisits(){
	for (int i=1;i<TREE_DEPTH;i++){
	    for (int j=0;j<DENSITY*DENSITY;j++){
		visitsByLevel[i][j] = false;
	    }
	}
    }

    void hideTargets(){
	for (int i=1;i<TREE_DEPTH;i++){
	    for (int j=0;j<DENSITY*DENSITY;j++){
		elementsByLevel[i][j].renderRound(false);
	    }
	}
    }

    void buildGrid(){
	// frame
	ZSegment s = new ZSegment(-HALF_WORLD_WIDTH, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
	s.setType(GLYPH_TYPE_GRID);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(HALF_WORLD_WIDTH, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
	s.setType(GLYPH_TYPE_GRID);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, -HALF_WORLD_HEIGHT, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
	s.setType(GLYPH_TYPE_GRID);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, HALF_WORLD_HEIGHT, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
	s.setType(GLYPH_TYPE_GRID);
	vsm.addGlyph(s, mainVSname);
	// grid (built recursively, max. rec depth control by GRID_DEPTH)
	tmpHGrid = new Vector();
	tmpVGrid = new Vector();
	buildHorizontalGridLevel(-HALF_WORLD_HEIGHT, HALF_WORLD_HEIGHT, 0);
	buildVerticalGridLevel(-HALF_WORLD_WIDTH, HALF_WORLD_WIDTH, 0);
	storeGrid();
	showGridLevel(1);
    }

    void buildHorizontalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(0, c, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
	storeSegmentInHGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	s.setType(GLYPH_TYPE_GRID);
	s.setVisible(false);
	if (depth < GRID_DEPTH){
	    buildHorizontalGridLevel(c1, c, depth+1);
	    buildHorizontalGridLevel(c, c2, depth+1);
	}
    }

    void buildVerticalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(c, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
	storeSegmentInVGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	s.setType(GLYPH_TYPE_GRID);
	s.setVisible(false);
	if (depth < GRID_DEPTH){
	    buildVerticalGridLevel(c1, c, depth+1);
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
	for (int i=0;i<tmpVGrid.size();i++){
	    v = (Vector)tmpVGrid.elementAt(i);
	    levelSize = v.size();
	    vGridLevels[i] = new ZSegment[levelSize];
	    for (int j=0;j<v.size();j++){
		vGridLevels[i][j] = (ZSegment)v.elementAt(j);
	    }
	}
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

    void updateGridLevel(long visibleSize){
	if (visibleSize < 48828.0f){
	    showGridLevel(5);
	}
	else if (visibleSize < 97656.0f){
	    showGridLevel(4);
	}
	else if (visibleSize < 195312.0f){
	    showGridLevel(3);
	}
	else if (visibleSize < 390625.0f){
	    showGridLevel(2);
	}
	else {
	    showGridLevel(1);
	}
    }

    void updateOverview(){
	// update overview's altitude
	portalCamera.setAltitude((float)((demoCamera.getAltitude()+demoCamera.getFocal())*24-demoCamera.getFocal()));
    }

    void centerOverview(){
	vsm.animator.createCameraAnimation(300, AnimManager.CA_TRANS_SIG,
					   new LongPoint(demoCamera.posx-portalCamera.posx, demoCamera.posy-portalCamera.posy),
					   portalCamera.getID(), null);
// 	portalCamera.moveTo(demoCamera.posx, demoCamera.posy);
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
	    lens.setBufferThreshold(1);
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
	    lens.setBufferThreshold(1);
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

    void killLens(){
	vsm.getOwningView(lens.getID()).setLens(null);
	lens.dispose();
	setMagFactor(ZLAbstractTask.DEFAULT_MAG_FACTOR);
	lens = null;
	setLens(WorldTaskEventHandler.NO_LENS);
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

    void triggerDM(int x, int y){
	if (dmPortal != null){// portal is active, destroy it it
	    killDM();
	    logm.lensStatus = AbstractTaskLogManager.NO_LENS;
	}
	else {// portal not active, create it
	    createDM(x, y);
	    logm.lensStatus = AbstractTaskLogManager.DM_LENS;
	}
	logm.portalPositionChanged(true);
    }

    void createDM(int x, int y){
	dmPortal = new DraggableCameraPortal(x, y, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera);
	dmPortal.setPortalEventHandler((PortalEventHandler)eh);
	dmPortal.setBackgroundColor(Color.LIGHT_GRAY);
	vsm.addPortal(dmPortal, demoView);
	dmPortal.setBorder(Color.RED);
	Location l = dmPortal.getSeamlessView(demoCamera);
	portalCamera.moveTo(l.vx, l.vy);
	portalCamera.setAltitude((float) ((demoCamera.getAltitude()+demoCamera.getFocal())/(DEFAULT_MAG_FACTOR)-demoCamera.getFocal()));
	updateDMRegion();
	int w = Math.round(dmRegion.getWidth() * 2 * demoCamera.getFocal() / ((float)(demoCamera.getFocal()+demoCamera.getAltitude())));
	int h = Math.round(dmRegion.getHeight() * 2 * demoCamera.getFocal() / ((float)(demoCamera.getFocal()+demoCamera.getAltitude())));
	dmPortal.sizeTo(w, h);
	mainVS.show(dmRegion);
	paintLinks = true;
	Point[] data = {new Point(DM_PORTAL_WIDTH-w, DM_PORTAL_HEIGHT-h),
			new Point(DM_PORTAL_INITIAL_X_OFFSET-w/2, DM_PORTAL_INITIAL_Y_OFFSET-h/2)};
	vsm.animator.createPortalAnimation(150, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), new DMActivator(this, x,y));
    }

    void killDM(){
	vsm.destroyPortal(dmPortal);
	dmPortal = null;
	mainVS.hide(dmRegion);
	paintLinks = false;
	((AbstractTaskDMEventHandler)eh).inPortal = false;
    }
    
    void meetDM(){
	if (dmPortal != null){
	    Vector data = new Vector();
	    data.add(new Float(portalCamera.getAltitude()-demoCamera.getAltitude()));
	    // take dragmag's center as the context's center
	    data.add(new LongPoint(portalCamera.posx-demoCamera.posx, portalCamera.posy-demoCamera.posy)); 
	    vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_TRANS_SIG,data,demoCamera.getID());
	    vsm.destroyPortal(dmPortal);
	    dmPortal = null;
	    mainVS.hide(dmRegion);
	    paintLinks = false;
	    ((AbstractTaskDMEventHandler)eh).inPortal = false;
	}
    }
    
    void altitudeChanged(){
	long[] wnes = demoView.getVisibleRegion(demoCamera);
	updateGridLevel(Math.max(wnes[2]-wnes[0], wnes[1]-wnes[3]));
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(demoCamera,ANIM_MOVE_LENGTH);
    }

    long[] dmwnes = new long[4];

    void updateDMRegion(){
	if (dmPortal == null){return;}
	dmPortal.getVisibleRegion(dmwnes);
	dmRegion.moveTo(portalCamera.posx, portalCamera.posy);
	dmRegion.setWidth((dmwnes[2]-dmwnes[0]) / 2 + 1);
	dmRegion.setHeight((dmwnes[1]-dmwnes[3]) / 2 + 1);
    }

    void updateDMWindow(){
	portalCamera.moveTo(dmRegion.vx, dmRegion.vy);
	logm.portalPositionChanged(true);
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (paintLinks){
	    float coef=(float)(demoCamera.focal/(demoCamera.focal+demoCamera.altitude));
	    int dmRegionX = (viewWidth/2) + Math.round((dmRegion.vx-demoCamera.posx)*coef);
	    int dmRegionY = (viewHeight/2) - Math.round((dmRegion.vy-demoCamera.posy)*coef);
	    int dmRegionW = Math.round(dmRegion.getWidth()*coef);
	    int dmRegionH = Math.round(dmRegion.getHeight()*coef);
	    g2d.setColor(Color.RED);
	    g2d.drawLine(dmRegionX-dmRegionW, dmRegionY-dmRegionH, dmPortal.x, dmPortal.y);
	    g2d.drawLine(dmRegionX+dmRegionW, dmRegionY-dmRegionH, dmPortal.x+dmPortal.w, dmPortal.y);
	    g2d.drawLine(dmRegionX-dmRegionW, dmRegionY+dmRegionH, dmPortal.x, dmPortal.y+dmPortal.h);
	    g2d.drawLine(dmRegionX+dmRegionW, dmRegionY+dmRegionH, dmPortal.x+dmPortal.w, dmPortal.y+dmPortal.h);
	}
    }
 
    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
	hpanelWidth = panelWidth / 2;
	hpanelHeight = panelHeight / 2;
	SELECTION_RECT_X = panelWidth/2 - SELECTION_RECT_W / 2;
	SELECTION_RECT_Y = panelHeight/2 - SELECTION_RECT_H / 2;
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
	/* First argument is the technique: see PZO_TECHNIQUE, PZ_TECHNIQUE, PZL_TECHNIQUE, DM_TECHNIQUE for appropriate values*/
	short tech = (args.length > 0) ? Short.parseShort(args[0]) : PZ_TECHNIQUE;
	new ZLAbstractTask(tech);
    }
    
}
