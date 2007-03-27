
/*   FILE: ZLWorldTask.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZLWorldTask.java,v 1.69 2006/06/02 14:01:42 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Point;
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

public class ZLWorldTask implements PostAnimationAction, MapApplication {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H, CONSOLE_W, MAP_MONITOR_W, INSTRUCTIONS_W;
    int CONSOLE_MON_H = 100;
    int INSTRUCTIONS_H = 0;
    int VIEW_X, VIEW_Y, CONSOLE_X, CONSOLE_Y, MAP_MONITOR_X, MAP_MONITOR_Y, INSTRUCTIONS_X, INSTRUCTIONS_Y;
    
    /* what windows and what information should be displayed */
    static boolean SHOW_MEMORY_USAGE = false;
    static boolean SHOW_COORDS = false;
    static boolean SHOW_MAP_MONITOR = false;
    static boolean SHOW_CONSOLE = false;
    static boolean SHOW_INSTRUCTIONS = true;

    /*translation constants*/
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    static int ANIM_MOVE_LENGTH = 300;

    /*text window showing various messages*/
    Console console;

    /*graphical widget showing what maps are loaded*/
    MapMonitor mapMonitor;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;
    int hpanelWidth, hpanelHeight;

    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;
    /* Main World map */
    static final long MAP_WIDTH = Math.round(MAIN_MAP_WIDTH * MapData.MN000factor.doubleValue());
    static final long MAP_HEIGHT = Math.round(MAIN_MAP_HEIGHT * MapData.MN000factor.doubleValue());
    static final long HALF_MAP_WIDTH = Math.round(MAP_WIDTH/2.0);
    static final long HALF_MAP_HEIGHT = Math.round(MAP_HEIGHT/2.0);

    static final String LOADING_WORLDMAP_TEXT = "Loading World Map ("+MAIN_MAP_WIDTH+"x"+MAIN_MAP_HEIGHT+") ...";

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    WorldTaskEventHandler eh;
    /* map manager */
    ZLWorldTaskMapManager ewmm;
    /* geographical data manager (cities, regions, countries)*/
    GeoDataStore gds;
    /* log manager (trials) */
    LogManager logm;

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

    /* Overview */
    OverviewPortal ovPortal;
    Camera overviewCamera;

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    /* GRID */
    static final Color GRID_COLOR = new Color(156,53,53);
    static final int GRID_DEPTH = 8;
    int currentLevel = -1;


//     static final float[] SHOW_GRID_ANIM_PARAMS = {0, 0, 0, 0, 0, 0, 1.0f};
//     static final float[] HIDE_GRID_ANIM_PARAMS = {0, 0, 0, 0, 0, 0, -1.0f};

    static final float START_ALTITUDE = 10000;
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

    ZLWorldScreenSaver screenSaver;
    
    static java.awt.Robot robot;

    final static short ZL_TECHNIQUE = 0;  // Zoom + Lens
    final static short PZ_TECHNIQUE = 1;  // Pan Zoom centered on view
    final static short RZ_TECHNIQUE = 2;  // Pan Zoom centered on view
    final static short PZA_TECHNIQUE = 3; // Pan + Zoom centered on cursor
    final static short DZ_TECHNIQUE = 4;  // Pan + Discrete zoom without animated transitions
    final static short DZA_TECHNIQUE = 5; // Pan + Discrete zoom with animated transitions
    final static short PZL_TECHNIQUE = 6; // Pan + Zoom (centered on view) & Zoom + Lens
    final static short SS_TECHNIQUE = 7;  // screen saver mode (for test and debugging)
    final static short DM_TECHNIQUE = 8; // DragMag

    final static String PZ_TECHNIQUE_NAME = "Pan-Zoom (centered on view)";
    final static String PZA_TECHNIQUE_NAME = "Pan-Zoom (centered on cursor)";
    final static String ZL_TECHNIQUE_NAME = "Lens-Zoom";
    final static String PZL_TECHNIQUE_NAME = "Pan-Zoom-Lens";
    final static String SS_TECHNIQUE_NAME = "Screen Saver";
    final static String DZ_TECHNIQUE_NAME = "Pan-Discrete Zoom";
    final static String DZA_TECHNIQUE_NAME = "Pan-Discrete Zoom (animated transitions)";
    final static String RZ_TECHNIQUE_NAME = "Region Zoom (animated transitions)";
    final static String DM_TECHNIQUE_NAME = "DragMag";
    short technique = ZL_TECHNIQUE;
    String techniqueName;

    static final int[] vispad = {100,100,100,100};

    ZLWorldTask(short t, boolean showConsole, boolean showMapMonitor,
		boolean showInstructions, boolean trainingData){
	SHOW_MAP_MONITOR = showMapMonitor;
	SHOW_CONSOLE = showConsole;
	SHOW_INSTRUCTIONS = showInstructions;
	vsm = new VirtualSpaceManager();
	vsm.setDebug(true);
	gds = new GeoDataStore(this, trainingData);
	init(t);
    }

    public void init(short t){
	try {
	    robot = new java.awt.Robot();
	}
	catch(java.awt.AWTException ex){ex.printStackTrace();}
	this.technique = t;
	if (this.technique == ZL_TECHNIQUE){
	    eh = new ZLEventHandler(this);
	    techniqueName = ZL_TECHNIQUE_NAME;
	}
	else if (this.technique == PZ_TECHNIQUE){
	    eh = new PZEventHandler(this);
	    techniqueName = PZ_TECHNIQUE_NAME;
	}
	else if (this.technique == RZ_TECHNIQUE){
	    eh = new RZEventHandler(this);
	    techniqueName = RZ_TECHNIQUE_NAME;
	}
	else if (this.technique == PZL_TECHNIQUE){
	    eh = new PZLEventHandler(this);
	    techniqueName = PZL_TECHNIQUE_NAME;
	}
	else if (this.technique == PZA_TECHNIQUE){
	    eh = new PZAEventHandler(this);
	    techniqueName = PZA_TECHNIQUE_NAME;
	}
	else if (this.technique == DZ_TECHNIQUE){
	    eh = new DZEventHandler(this);
	    techniqueName = DZ_TECHNIQUE_NAME;
	}
	else if (this.technique == DZA_TECHNIQUE){
	    eh = new DZAEventHandler(this);
	    techniqueName = DZA_TECHNIQUE_NAME;
	}
	else if (this.technique == DM_TECHNIQUE){
	    eh = new DMEventHandler(this);
	    techniqueName = DM_TECHNIQUE_NAME;
	}
	else {
	    eh = new SSEventHandler(this);
	    techniqueName = SS_TECHNIQUE_NAME;
	    SHOW_SELECTION_RECT = false;
	}
	windowLayout();
	vsm.setMainFont(GeoDataStore.CITY_FONT);
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	portalCamera = vsm.addCamera(mainVSname);
	overviewCamera = vsm.addCamera(mainVSname);
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
	if (SHOW_CONSOLE){
	    initConsole(CONSOLE_X, CONSOLE_Y, CONSOLE_W, CONSOLE_MON_H, true);
	    console.append(Utils.miscInfo, Console.GRAY_STYLE);
	}
	demoCamera.setAltitude(START_ALTITUDE);
	ewmm = new ZLWorldTaskMapManager(this, vsm, mainVS, demoCamera, demoView);
	if (SHOW_MAP_MONITOR){
	    initMapMonitor(MAP_MONITOR_X, MAP_MONITOR_Y, MAP_MONITOR_W, CONSOLE_MON_H);
	    ewmm.setMapMonitor(mapMonitor);
	    ewmm.initMap();
	    mapMonitor.updateMaps();
	}
	else {
	    ewmm.initMap();
	}
// 	buildGrid();
	gds.buildAll();
	logm = new LogManager(this);
	System.gc();
	if (this.technique == SS_TECHNIQUE){
	    screenSaver = new ZLWorldScreenSaver(this);
	}
	else if (this.technique == DM_TECHNIQUE){
	    initDM();
	}
	else {
	    logm.im.say(LocateTask.PSTS);
	}
    }

    void initDM(){
	dmRegion = new VRectangle(0,0,0,1,1,Color.RED);
	dmRegion.setFilled(false);
	dmRegion.setBorderColor(Color.RED);
	vsm.addGlyph(dmRegion, mainVS);
	mainVS.hide(dmRegion);
    }

    void createOverview(){
	ovPortal = new OverviewPortal(VIEW_W-AbstractTaskInstructionsManager.PADDING-DM_PORTAL_WIDTH,
				      VIEW_H-AbstractTaskInstructionsManager.PADDING-DM_PORTAL_HEIGHT/2,
				      DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT/2, overviewCamera, demoCamera);
	ovPortal.setPortalEventHandler(new WorldOverviewEventHandler(this));
	ovPortal.setBackgroundColor(Color.LIGHT_GRAY);
	ovPortal.setObservedRegionTranslucency(0.5f);
	vsm.addPortal(ovPortal, demoView);
	ovPortal.setBorder(Color.RED);
	updateOverview();
	vsm.repaintNow();
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
	if (SHOW_CONSOLE || SHOW_MAP_MONITOR){
	    VIEW_H = SCREEN_HEIGHT - CONSOLE_MON_H;
	    if (VIEW_H > VIEW_MAX_H){VIEW_H = VIEW_MAX_H;}
	    if (Utilities.osIsMacOS()){
		VIEW_H -= 22;
	    }
	    if (SHOW_CONSOLE){
		if (SHOW_MAP_MONITOR){
		    CONSOLE_X = VIEW_X;
		    CONSOLE_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
		    CONSOLE_W = SCREEN_WIDTH / 2;
		    MAP_MONITOR_X = VIEW_X + CONSOLE_W;
		    MAP_MONITOR_Y = CONSOLE_Y;
		    MAP_MONITOR_W = SCREEN_WIDTH / 2;
		}
		else {
		    CONSOLE_X = VIEW_X;
		    CONSOLE_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
		    CONSOLE_W = SCREEN_WIDTH / 2;
		}
	    }
	    else if (SHOW_MAP_MONITOR){
		MAP_MONITOR_X = VIEW_X;
		MAP_MONITOR_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
		MAP_MONITOR_W = SCREEN_WIDTH / 2;
	    }
	}
	else {// only showing the main view (+instructions)
	    VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
	    VIEW_H -= INSTRUCTIONS_H;
	}
	INSTRUCTIONS_W = VIEW_W;
	INSTRUCTIONS_X = VIEW_X;
	INSTRUCTIONS_Y = VIEW_H;
	if (Utilities.osIsMacOS()){
	    INSTRUCTIONS_Y += 22;
	    if (INSTRUCTIONS_Y+INSTRUCTIONS_H > SCREEN_HEIGHT){
		INSTRUCTIONS_H = SCREEN_HEIGHT - INSTRUCTIONS_Y;
	    }
	}
    }

    void initConsole(int x, int y, int w, int h, boolean visible){
	console = new Console("Console", x, y, w, h, visible);
    }

    void initMapMonitor(int x, int y, int w, int h){
	mapMonitor = new MapMonitor("Map Monitor", x, y, w, h, this);
    }

    void buildGrid(){
	// frame
	ZSegment s = new ZSegment(-HALF_MAP_WIDTH, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(HALF_MAP_WIDTH, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, -HALF_MAP_HEIGHT, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	s = new ZSegment(0, HALF_MAP_HEIGHT, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
	vsm.addGlyph(s, mainVSname);
	// grid (built recursively, max. rec depth control by GRID_DEPTH)
	tmpHGrid = new Vector();
	tmpVGrid = new Vector();
	buildHorizontalGridLevel(-HALF_MAP_HEIGHT, HALF_MAP_HEIGHT, 0);
	buildVerticalGridLevel(-HALF_MAP_WIDTH, HALF_MAP_WIDTH, 0);
	storeGrid();
	showGridLevel(1);
    }

    void buildHorizontalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(0, c, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
// 	s.setTransparencyValue(0);
	storeSegmentInHGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	//mainVS.hide(s);
	s.setVisible(false);
	if (depth < GRID_DEPTH){
	    buildHorizontalGridLevel(c1, c, depth+1);
	    buildHorizontalGridLevel(c, c2, depth+1);
	}
    }

    void buildVerticalGridLevel(long c1, long c2, int depth){
	long c = (c1+c2)/2;
	ZSegment s = new ZSegment(c, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
// 	s.setTransparencyValue(0);
	storeSegmentInVGrid(s, depth);
	vsm.addGlyph(s, mainVSname);
	//mainVS.hide(s);
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
// 		    mainVS.hide(hGridLevels[i][j]);
		    hGridLevels[i][j].setVisible(false);
// 		    vsm.animator.createGlyphAnimation(GRID_ANIM_TIME, AnimManager.GL_COLOR_LIN, HIDE_GRID_ANIM_PARAMS,
// 						      hGridLevels[i][j].getID(), this);
		}
	    }
	    for (int i=level+1;i<=currentLevel;i++){
		for (int j=0;j<vGridLevels[i].length;j++){
// 		    mainVS.hide(vGridLevels[i][j]);
		    vGridLevels[i][j].setVisible(false);
// 		    vsm.animator.createGlyphAnimation(GRID_ANIM_TIME, AnimManager.GL_COLOR_LIN, HIDE_GRID_ANIM_PARAMS,
// 						      vGridLevels[i][j].getID(), this);
		}
	    }
	}
	else if (level > currentLevel){
	    for (int i=currentLevel+1;i<=level;i++){
		for (int j=0;j<hGridLevels[i].length;j++){
// 		    mainVS.show(hGridLevels[i][j]);
		    hGridLevels[i][j].setVisible(true);
// 		    hGridLevels[i][j].setVisible(true);
// 		    vsm.animator.createGlyphAnimation(GRID_ANIM_TIME, AnimManager.GL_COLOR_LIN, SHOW_GRID_ANIM_PARAMS,
// 						      hGridLevels[i][j].getID(), null);
		}
	    }
	    for (int i=currentLevel+1;i<=level;i++){
		for (int j=0;j<vGridLevels[i].length;j++){
// 		    mainVS.show(vGridLevels[i][j]);
		    vGridLevels[i][j].setVisible(true);
// 		    vGridLevels[i][j].setVisible(true);
// 		    vsm.animator.createGlyphAnimation(GRID_ANIM_TIME, AnimManager.GL_COLOR_LIN, SHOW_GRID_ANIM_PARAMS,
// 						      vGridLevels[i][j].getID(), null);
		}
	    }
	}
	currentLevel = level;
    }

    void setLens(int t){
	eh.lensType = t;
	switch (eh.lensType){
	case WorldTaskEventHandler.ZOOMIN_LENS:{
	    logm.lensStatus = LogManager.ZOOMIN_LENS;
	    logm.lensPositionChanged(true);
	    break;
	}
	case WorldTaskEventHandler.ZOOMOUT_LENS:{
	    logm.lensStatus = LogManager.ZOOMOUT_LENS;
	    logm.lensPositionChanged(true);
	    break;
	}
	case WorldTaskEventHandler.NO_LENS:{
	    logm.lensStatus = LogManager.NO_LENS;
	    logm.lensxS = LogManager.NaN;
	    logm.lensyS = LogManager.NaN;
	    break;
	}
	}
    }

    void moveLens(int x, int y, boolean write){
	lens.setAbsolutePosition(x, y);
	logm.lensPositionChanged(write);
	vsm.repaintNow();
    }

    void dzoomIn(long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
	Vector cadata = new Vector();
	// -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
	float deltAlt = (float)((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
	if (cameraAbsAlt + deltAlt > FLOOR_ALTITUDE){
	    demoCamera.altitudeOffset(deltAlt);
	    demoCamera.move(c2x-demoCamera.posx, c2y-demoCamera.posy);
	}
	else {
	    float actualDeltAlt = FLOOR_ALTITUDE - cameraAbsAlt;
	    double ratio = actualDeltAlt / deltAlt;
	    demoCamera.altitudeOffset(actualDeltAlt);
	    demoCamera.move(Math.round((c2x-demoCamera.posx)*ratio),
			    Math.round((c2y-demoCamera.posy)*ratio));
	}	
	eh.cameraMoved();
    }

    void dzoomOut(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
	Vector cadata = new Vector();
	demoCamera.altitudeOffset((float)(cameraAbsAlt*(MAG_FACTOR-1)));
	// uncomment for ZCC
 	demoCamera.move(c2x-demoCamera.posx, c2y-demoCamera.posy);
	eh.cameraMoved();
    }

    void dazoomIn(long mx, long my){
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
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
				     Math.round((c2y-demoCamera.posy)*ratio)));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
	}
    }

    void dazoomOut(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
	Vector cadata = new Vector();
	// uncomment for ZCC
	cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
	cadata.add(new LongPoint(c2x-demoCamera.posx, c2y-demoCamera.posy));
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, demoCamera.getID(), new ZLWorldZOP1CameraAction(this));
	// uncomment for ZVC
// 	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_LIN,
// 					   new Float(cameraAbsAlt*(MAG_FACTOR-1)),
// 					   demoCamera.getID(), new ZLWorldZOP1CameraAction(this));	
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
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio2 = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio2),
				     Math.round((c2y-demoCamera.posy)*ratio2)));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
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
					   cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
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
					     lens.getID(), new ZLWorldZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
				     Math.round((c2y-demoCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZLWorldZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
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
					   cadata, demoCamera.getID(), new ZLWorldZOP1CameraAction(this));
	setLens(WorldTaskEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					 lens.getID(), new ZLWorldZOP2LensAction(this));
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

    void triggerDM(int x, int y){
	if (dmPortal != null){// portal is active, destroy it it
	    killDM();
	    logm.lensStatus = AbstractTaskLogManager.NO_LENS;
	}
	else {// portal not active, create it
	    createDM(x, y);
	    logm.lensStatus = AbstractTaskLogManager.DM_LENS;
	}
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
	vsm.animator.createPortalAnimation(150, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
    }

    void killDM(){
	vsm.destroyPortal(dmPortal);
	dmPortal = null;
	mainVS.hide(dmRegion);
	paintLinks = false;
	((DMEventHandler)eh).inDMZoomWindow = false;
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
	    ((DMEventHandler)eh).inDMZoomWindow = false;
	}
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
    }

    void updateLabels(float a){
	gds.updateLabelLevel(a);
    }

    void altitudeChanged(){
	// update  map level, visible maps and grid level as it
	// was prevented between zoom{int,out} phases 1 and 2
	ewmm.updateMapLevel(demoCamera.getAltitude());
	long[] wnes = demoView.getVisibleRegion(demoCamera);
	//ewmm.updateVisibleMaps(wnes, false, (short)0);
	updateLabels(demoCamera.getAltitude());
	updateGridLevel(Math.max(wnes[2]-wnes[0], wnes[1]-wnes[3]));
    }

    void updateGridLevel(long visibleSize){
// 	if (visibleSize < 1200){
// 	    showGridLevel(8);
// 	}
// 	else if (visibleSize < 2400){
// 	    showGridLevel(7);
// 	}
// 	else if (visibleSize < 4800){
// 	    showGridLevel(6);
// 	}
// 	else if (visibleSize < 9600){
// 	    showGridLevel(5);
// 	}
// 	else if (visibleSize < 19200){
// 	    showGridLevel(4);
// 	}
// 	else if (visibleSize < 38400){
// 	    showGridLevel(3);
// 	}
// 	else if (visibleSize < 76800){
// 	    showGridLevel(2);
// 	}
// 	else {
// 	    showGridLevel(1);
// 	}
    }

    static float MAX_OVERVIEW_ALT = 30000.0f;

    void updateOverview(){
	// update overview's altitude
	float newAlt = (float)((demoCamera.getAltitude()+demoCamera.getFocal())*24-demoCamera.getFocal());
	overviewCamera.setAltitude((newAlt > MAX_OVERVIEW_ALT) ? MAX_OVERVIEW_ALT : newAlt);
    }

    void centerOverview(){
	if (overviewCamera.getAltitude() < MAX_OVERVIEW_ALT){
	    vsm.animator.createCameraAnimation(300, AnimManager.CA_TRANS_SIG,
					       new LongPoint(demoCamera.posx-overviewCamera.posx, demoCamera.posy-overviewCamera.posy),
					       overviewCamera.getID(), null);
	}
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(demoCamera,ANIM_MOVE_LENGTH);
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Float alt=new Float(demoCamera.getAltitude()+demoCamera.getFocal());
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,demoCamera.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Float alt=new Float(-(demoCamera.getAltitude()+demoCamera.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,demoCamera.getID());
    }

    /*direction should be one of ZGRViewer.MOVE_* */
    void translateView(short direction){
	Camera c=demoView.getCameraNumber(0);
	LongPoint trans;
	long[] rb=demoView.getVisibleRegion(c);
	if (direction==MOVE_UP){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    long qt=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else {// direction==MOVE_LEFT
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
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
	//mainVS.hide((Glyph)target);
	((Glyph)target).setVisible(false);
    }

    public void writeOnConsole(String s){
	console.append(s);
    }

    public void writeOnConsole(String s, Style st){
	console.append(s, st);
    }

    void gc(){
	if (SHOW_CONSOLE){
	    console.append("Garbage collector running...\n", Console.GRAY_STYLE);
	    System.gc();
	    console.append("Garbage collection ended\n", Console.GRAY_STYLE);
	}
	else {
	    System.gc();
	}
	if (SHOW_MEMORY_USAGE){vsm.repaintNow();}
    }

    public static void main(String[] args){
	/* First argument is the technique: see ZL_TECHNIQUE, PZ_TECHNIQUE, PZA_TECHNIQUE, PZL_TECHNIQUE or SS_TECHNIQUE for appropriate values
	   Second argument is either 1 (show console for messages) or 0 (don't show it)
	   Third argument is either 1 (show map manager monitor) or 0 (don't show it) */
	short tech = (args.length > 0) ? Short.parseShort(args[0]) : SS_TECHNIQUE;
	boolean sc = (args.length > 1) ? (Short.parseShort(args[1])==1) : false;
	boolean smm = (args.length > 2) ? (Short.parseShort(args[2])==1) : false;
	boolean si = (args.length > 3) ? (Short.parseShort(args[3])==1) : true;
	boolean td = (args.length > 4) ? (Short.parseShort(args[4])==1) : false;
	new ZLWorldTask(tech, sc, smm, si, td);
    }
    
}
