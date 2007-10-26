/*   FILE: ZLWorldDemo.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.text.Style;

import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.lens.*;
import com.xerox.VTM.glyphs.ZSegment;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

public class ZLWorldDemo implements Java2DPainter, MapApplication {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 860;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    static boolean SHOW_MEMORY_USAGE = false;

    /*translation constants*/
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    static int ANIM_MOVE_LENGTH = 300;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

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
    ZLDemoEventHandler eh;
    /* map manager */
    ZLWorldTaskMapManager ewmm;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    /* misc. lens settings */
    Lens lens;
    TemporalLens tLens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;

    /* should the lens automatically transition to a manhattan lens or not */
    boolean manhattanize = false;

    /* lens distance and drop-off functions */
    static final short L1_Linear = 0;
    static final short L1_InverseCosine = 1;
    static final short L1_Manhattan = 2;
    static final short L2_Gaussian = 3;
    static final short L2_Linear = 4;
    static final short L2_InverseCosine = 5;
    static final short L2_Manhattan = 6;
    static final short L2_Scrambling = 7;
    static final short LInf_Linear = 8;
    static final short LInf_InverseCosine = 9;
    static final short LInf_Manhattan = 10;
    static final short L1_Fresnel = 11;
    static final short L2_Fresnel = 12;
    static final short LInf_Fresnel = 13;
    static final short L2_TGaussian = 14;
    static final short L2_Fading = 15;
    static final short LInf_Fading = 16;
    static final short LInf_Gaussian = 17;
    static final short L3_Linear = 18;
    static final short L3_Manhattan = 19;
    static final short L3_Gaussian = 20;
    static final short L3_InverseCosine = 21;
    static final short L3_Fresnel = 22;
    static final short LInf_TLinear = 23;
    static final short L3_TLinear = 24;
    static final short L2_TLinear = 25;
    static final short L2_DLinear = 26;
    static final short L2_XGaussian = 27;

    short lensFamily = L2_Gaussian;
    static final String View_Title_Prefix = "Probing Lens Demo - ";
    static final String L1_Linear_Title = View_Title_Prefix + "L1 / Linear";
    static final String L1_InverseCosine_Title = View_Title_Prefix + "L1 / Inverse Cosine";
    static final String L1_Manhattan_Title = View_Title_Prefix + "L1 / Manhattan";
    static final String L2_Gaussian_Title = View_Title_Prefix + "L2 / Gaussian";
    static final String L2_Linear_Title = View_Title_Prefix + "L2 / Linear";
    static final String L2_InverseCosine_Title = View_Title_Prefix + "L2 / Inverse Cosine";
    static final String L2_Manhattan_Title = View_Title_Prefix + "L2 / Manhattan";
    static final String L2_Scrambling_Title = View_Title_Prefix + "L2 / Scrambling (for fun)";
    static final String LInf_Linear_Title = View_Title_Prefix + "LInf / Linear";
    static final String LInf_InverseCosine_Title = View_Title_Prefix + "LInf / Inverse Cosine";
    static final String LInf_Manhattan_Title = View_Title_Prefix + "LInf / Manhattan";
    static final String L1_Fresnel_Title = View_Title_Prefix + "L1 / Fresnel";
    static final String L2_Fresnel_Title = View_Title_Prefix + "L2 / Fresnel";
    static final String LInf_Fresnel_Title = View_Title_Prefix + "LInf / Fresnel";
    static final String L2_TGaussian_Title = View_Title_Prefix + "L2 / Translucence Gaussian";
    static final String L2_TLinear_Title = View_Title_Prefix + "L2 / Translucence Linear";
    static final String L2_Fading_Title = View_Title_Prefix + "L2 / Fading";
    static final String LInf_Fading_Title = View_Title_Prefix + "LInf / Fading";
    static final String LInf_Gaussian_Title = View_Title_Prefix + "LInf / Gaussian";
    static final String L3_Linear_Title = View_Title_Prefix + "L3 / Linear";
    static final String L3_InverseCosine_Title = View_Title_Prefix + "L3 / Inverse Cosine";
    static final String L3_Gaussian_Title = View_Title_Prefix + "L3 / Gaussian";
    static final String L3_Manhattan_Title = View_Title_Prefix + "L3 / Manhattan";
    static final String L3_Fresnel_Title = View_Title_Prefix + "L3 / Fresnel";
    static final String LInf_TLinear_Title = View_Title_Prefix + "LInf / Translucence Linear";
    static final String L3_TLinear_Title = View_Title_Prefix + "L3 / Translucence Linear";
    static final String L2_DLinear_Title = View_Title_Prefix + "L2 / Dynamic Linear";
    static final String L2_XGaussian_Title = View_Title_Prefix + "L2 / eXtended Gaussian";

    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    static final float START_ALTITUDE = 10000;
    static final float FLOOR_ALTITUDE = 100.0f;


    /* GRID */
    ZSegment[][] hGridLevels = new ZSegment[GRID_DEPTH+1][];
    ZSegment[][] vGridLevels = new ZSegment[GRID_DEPTH+1][];
    Vector tmpHGrid;
    Vector tmpVGrid;
    static final Color GRID_COLOR = new Color(156,53,53);
    static final int GRID_DEPTH = 8;
    int currentLevel = -1;


    ZLWorldDemo(boolean showGrid){
	vsm = new VirtualSpaceManager();
// 	vsm.setDebug(true);
	init();
	if (showGrid){
	    buildGrid();
	}
	System.gc();
    }

    public void init(){
	eh = new ZLDemoEventHandler(this);
	windowLayout();
	vsm.setMainFont(GeoDataStore.CITY_FONT);
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, L2_Gaussian_Title, View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setBackgroundColor(Color.GRAY);
	demoView.setLocation(VIEW_X, VIEW_Y);
	updatePanelSize();
	demoView.setEventHandler(eh);
	demoView.getPanel().addComponentListener(eh);
	demoView.setNotifyMouseMoved(true);
 	demoView.setJava2DPainter(this, Java2DPainter.AFTER_DISTORTION);
	demoCamera.setAltitude(START_ALTITUDE);
	ewmm = new ZLWorldTaskMapManager(this, vsm, mainVS, demoCamera, demoView);
	ewmm.initMap();
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
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
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
	ZSegment s = new ZSegment(c, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
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
    }

    void moveLens(int x, int y, long absTime){
	if (tLens != null){
	    tLens.setAbsolutePosition(x, y, absTime);
	}
	else {
	    lens.setAbsolutePosition(x, y);
	}
	vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
 	    lens = demoView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), (manhattanize) ? new Manhattanizer(this) : null);
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
					     lens.getID(), new ZLDemoZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLDemoZIP2CameraAction(this));
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
				     Math.round((c2y-demoCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZLDemoZIP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, demoCamera.getID(), new ZLDemoZIP2CameraAction(this));
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
	    lens = demoView.setLens(getLensDefinition(x, y));
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
					 lens.getID(), new ZLDemoZOP2LensAction(this));
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

    float F_V = 1.0f;

    Lens getLensDefinition(int x, int y){
	Lens res = null;
	switch (lensFamily){
	case L1_Linear:{
	    res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L1_InverseCosine:{
	    res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L1_Manhattan:{
	    res = new L1FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)res).setBoundaryColor(Color.RED);
	    tLens = null;
	    break;
	}
	case L2_Gaussian:{
	    res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_Linear:{
	    res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_InverseCosine:{
	    res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_Manhattan:{
	    res = new FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)res).setBoundaryColor(Color.RED);
	    tLens = null;
	    break;
	}
	case L2_Scrambling:{
	    res = new FSScramblingLens(1.0f, LENS_R1, 1, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case LInf_Linear:{
	    res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case LInf_InverseCosine:{
	    res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case LInf_Manhattan:{
	    res = new LInfFSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)res).setBoundaryColor(Color.RED);
	    tLens = null;
	    break;
	}
	case LInf_Gaussian:{
	    res = new LInfFSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
 	case L1_Fresnel:{
	    res = new L1FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_Fresnel:{
	    res = new FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case LInf_Fresnel:{
	    res = new LInfFSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_TGaussian:{
	    res = new TGaussianLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_TLinear:{
	    res = new TLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case LInf_TLinear:{
	    res = new LInfTLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L3_TLinear:{
	    res = new L3TLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_Fading:{
	    tLens = new TFadingLens(1.0f, 0.0f, F_V, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((TFadingLens)tLens).setBoundaryColor(Color.RED);
	    ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
	    res = (Lens)tLens;
	    break;
	}
	case LInf_Fading:{
	    tLens = new LInfTFadingLens(1.0f, 0.0f, 0.95f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((TFadingLens)tLens).setBoundaryColor(Color.RED);
	    ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
	    res = (Lens)tLens;
	    break;
	}
	case L3_Linear:{
	    res = new L3FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L3_InverseCosine:{
	    res = new L3FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L3_Manhattan:{
	    res = new L3FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
	    ((FSManhattanLens)res).setBoundaryColor(Color.RED);
	    tLens = null;
	    break;
	}
	case L3_Gaussian:{
	    res = new L3FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L3_Fresnel:{
	    res = new L3FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	case L2_DLinear:{
	    tLens = new DLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    ((DLinearLens)tLens).setInnerRadiusColor(Color.RED);
	    ((DLinearLens)tLens).setOuterRadiusColor(Color.RED);
	    res = (Lens)tLens;
	    break;
	}
	case L2_XGaussian:{
	    res = new XGaussianLens(1.0f, 0.2f, 1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
	    tLens = null;
	    break;
	}
	}
	return res;
    }

    void altitudeChanged(){
	// update  map level, visible maps and grid level as it
	// was prevented between zoom{int,out} phases 1 and 2
	ewmm.updateMapLevel(demoCamera.getAltitude());
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(demoCamera,ANIM_MOVE_LENGTH);
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=demoView.getCameraNumber(0);
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=demoView.getCameraNumber(0);
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
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

    void switchManhattanizer(){
	manhattanize = !manhattanize;
    }

    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (SHOW_MEMORY_USAGE){showMemoryUsage(g2d, viewWidth, viewHeight);}
    }

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
	totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
	usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
	g2d.setColor(Color.green);
	g2d.fillRect(20,
		     viewHeight - 40,
		     200,
		     15);
	g2d.setColor(Color.orange);
	g2d.fillRect(20,
		     viewHeight - 40,
		     totalMemRatio * 2,
		     15);
	g2d.setColor(Color.red);
	g2d.fillRect(20,
		     viewHeight - 40,
		     usedMemRatio * 2,
		     15);
	g2d.setColor(Color.black);
	g2d.drawRect(20,
		     viewHeight - 40,
		     200,
		     15);
	g2d.drawString(usedMemRatio + "%", 50, viewHeight - 28);
	g2d.drawString(totalMemRatio + "%", 100, viewHeight - 28);
	g2d.drawString(maxMem/1048576 + " Mb", 170, viewHeight - 28);	
    }

    public void writeOnConsole(String s){}

    public void writeOnConsole(String s, Style st){}

    void gc(){
	System.gc();
	if (SHOW_MEMORY_USAGE){vsm.repaintNow();}
    }

    public static void main(String[] args){
        final boolean showGrid = (args.length > 0) ? (new Boolean(args[0])).booleanValue() : false;
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new ZLWorldDemo(showGrid);
            }
        });
    }
    
}
