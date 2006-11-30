/*   FILE: GraphicsManager.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id:  $
 */ 

package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import java.awt.event.ComponentListener;

import java.util.Vector;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.DraggableCameraPortal;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.Java2DPainter;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.RectangleNR;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleST;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.PortalEventHandler;


/* Multiscale feature manager */

public class GraphicsManager implements ComponentListener, AnimationListener, Java2DPainter {

    public VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    VirtualSpace mnSpace;
    VirtualSpace rSpace;
    static final String mainSpace = "graphSpace";
    static final String menuSpace = "menuSpace";
    /*name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view*/
    static final String rdRegionVirtualSpace = "radarSpace";
    /*represents the region seen by main view in the radar view*/
    VRectangle observedRegion;

    public View mainView;
    View rView;
    static final String radarView = "Overview";
    Camera mainCamera;
    JPanel mainViewPanel;

    PeriodicActionManager paMngr;


    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    
    /* misc. lens settings */
    Lens lens;
    TFadingLens fLens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static final double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    static final float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    /* DragMag */
    static final int DM_PORTAL_WIDTH = 200;
    static final int DM_PORTAL_HEIGHT = 200;
    static final int DM_PORTAL_INITIAL_X_OFFSET = 150;
    static final int DM_PORTAL_INITIAL_Y_OFFSET = 150;
    static final int DM_PORTAL_ANIM_TIME = 150;
    static final Color DM_COLOR = Color.RED;
    Camera dmCamera;
    DraggableCameraPortal dmPortal;
    VRectangle magWindow;
    int magWindowW, magWindowN, magWindowE, magWindowS;
    boolean paintLinks = false;


    static final float FLOOR_ALTITUDE = -90.0f;

    /*translation constants*/
    static final short MOVE_UP=0;
    static final short MOVE_DOWN=1;
    static final short MOVE_LEFT=2;
    static final short MOVE_RIGHT=3;
    static final short MOVE_UP_LEFT=4;
    static final short MOVE_UP_RIGHT=5;
    static final short MOVE_DOWN_LEFT=6;
    static final short MOVE_DOWN_RIGHT=7;

    ToolPalette tp;

    BaseEventHandler meh;
    RadarEvtHdlr reh;

    ConfigManager cfgMngr;

    /*remember previous camera locations so that we can get back*/
    static final int MAX_PREV_LOC=10;
    Vector previousLocations;

    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;

    /*quick search variables*/
    int searchIndex = 0;
    String lastSearchedString = "";
    Vector matchingList = new Vector();

    StatusBar statusBar;

    GraphicsManager(StatusBar sb){
	this.statusBar = sb;
    }

    Vector createZVTMelements(boolean applet){
	vsm = new VirtualSpaceManager(applet);
	vsm.setMainFont(ConfigManager.defaultFont);
	vsm.setZoomLimit(-90);
	vsm.setMouseInsideGlyphColor(Color.red);
	//vsm.setDebug(true);
	mSpace = vsm.addVirtualSpace(mainSpace);
	mainCamera = vsm.addCamera(mainSpace); // camera #0 for main view
	vsm.addCamera(mainSpace); // camera #1 for radar view
	mnSpace = vsm.addVirtualSpace(menuSpace);
	// camera for pie menu
	vsm.addCamera(menuSpace).setAltitude(10);
	rSpace = vsm.addVirtualSpace(rdRegionVirtualSpace);
	// camera for rectangle representing region seen in main viewport (in overview)
	vsm.addCamera(rdRegionVirtualSpace);
	// DragMag portal camera (camera #2)
	dmCamera = vsm.addCamera(mainSpace);
	RectangleNR seg1;
	RectangleNR seg2;
	observedRegion=new VRectangleST(0, 0, 0, 10, 10, ConfigManager.OBSERVED_REGION_COLOR);
	observedRegion.setBorderColor(ConfigManager.OBSERVED_REGION_BORDER_COLOR);
	seg1=new RectangleNR(0,0,0,0,500,new Color(115,83,115));  //500 should be sufficient as the radar window is
	seg2=new RectangleNR(0,0,0,500,0,new Color(115,83,115));  //not resizable and is 300x200 (see rdW,rdH below)
	if (!(Utilities.osIsWindows() || Utilities.osIsMacOS())){
	    observedRegion.setFill(false);
	}
	vsm.addGlyph(observedRegion,rdRegionVirtualSpace);
	vsm.addGlyph(seg1,rdRegionVirtualSpace);
	vsm.addGlyph(seg2,rdRegionVirtualSpace);
	vsm.stickToGlyph(seg1,observedRegion);
	vsm.stickToGlyph(seg2,observedRegion);
	observedRegion.setSensitivity(false);
	tp = new ToolPalette(this);
	Vector cameras = new Vector();
	cameras.add(vsm.getVirtualSpace(mainSpace).getCamera(0));
	cameras.add(vsm.getVirtualSpace(menuSpace).getCamera(0));
	cameras.add(tp.getPaletteCamera());
	return cameras;
    }

    void createFrameView(Vector cameras, int acc, JMenuBar jmb){
	if (acc == 1){
	    mainView = vsm.addExternalView(cameras, ConfigManager.MAIN_TITLE, View.VOLATILE_VIEW,
					   ConfigManager.mainViewW, ConfigManager.mainViewH,
					   true, false, jmb);
	}
	else if (acc == 2){
	    mainView = vsm.addExternalView(cameras, ConfigManager.MAIN_TITLE, View.OPENGL_VIEW,
					   ConfigManager.mainViewW, ConfigManager.mainViewH,
					   true, false, jmb);
	}
	else {
	    mainView = vsm.addExternalView(cameras, ConfigManager.MAIN_TITLE, View.STD_VIEW,
					   ConfigManager.mainViewW, ConfigManager.mainViewH,
					   true, false, jmb);
	}
	mainView.setLocation(ConfigManager.mainViewX,ConfigManager.mainViewY);
	mainView.getFrame().addComponentListener(this);
    }

    JPanel createPanelView(Vector cameras, int w, int h){
	vsm.addPanelView(cameras, ConfigManager.MAIN_TITLE, w, h);
	mainView = vsm.getView(ConfigManager.MAIN_TITLE);
	return mainView.getPanel();
    }

    void parameterizeView(BaseEventHandler eh){
	paMngr = new PeriodicActionManager(this);
	mainView.setBackgroundColor(ConfigManager.backgroundColor);
	meh = eh;
	mainView.setEventHandler((ViewEventHandler)eh);
	mainView.setNotifyMouseMoved(true);
	vsm.animator.setAnimationListener(this);
	mainView.setVisible(true);
	mainView.getPanel().addMouseMotionListener(paMngr);
	paMngr.start();
	mainView.setJava2DPainter(paMngr, Java2DPainter.AFTER_PORTALS);
	mainView.setJava2DPainter(this, Java2DPainter.FOREGROUND);

	mainViewPanel = mainView.getPanel();
	setAntialiasing(ConfigManager.ANTIALIASING);

	initDM();
	updatePanelSize();
	previousLocations=new Vector();

    }

    void setConfigManager(ConfigManager cm){
	this.cfgMngr = cm;
    }

    void reset(){
	vsm.destroyGlyphsInSpace(mainSpace);
	vsm.addGlyph(magWindow, mSpace);
	mSpace.hide(magWindow);
	previousLocations.removeAllElements();
    }

    void initDM(){
	magWindow = new VRectangle(0, 0, 0, 1, 1, GraphicsManager.DM_COLOR);
	magWindow.setFill(false);
	magWindow.setBorderColor(GraphicsManager.DM_COLOR);
	vsm.addGlyph(magWindow, mSpace);
	mSpace.hide(magWindow);
    }

    /*antialias ON/OFF for views*/
    void setAntialiasing(boolean b){
	ConfigManager.ANTIALIASING = b;
	mainView.setAntialiasing(ConfigManager.ANTIALIASING);
    }

    /*-------------     Window resizing     -----------------*/

    void updatePanelSize(){
	tp.displayPalette(false);
	try {
	    panelWidth = mainViewPanel.getWidth();
	    panelHeight = mainViewPanel.getHeight();
	    paMngr.requestToolPaletteRelocation();
	}
	catch(NullPointerException ex){}
    }

    /*-------------     Navigation              -------------*/

    void getGlobalView(){
	Location l=vsm.getGlobalView(mSpace.getCamera(0),ConfigManager.ANIM_MOVE_LENGTH);
	rememberLocation(mSpace.getCamera(0).getLocation());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*direction should be one of GraphicsManager.MOVE_* */
    void translateView(short direction){
	Camera c=mainView.getCameraNumber(0);
	rememberLocation(c.getLocation());
	LongPoint trans;
	long[] rb=mainView.getVisibleRegion(c);
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
	else if (direction==MOVE_LEFT){
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else if (direction==MOVE_UP_LEFT){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_UP_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else if (direction==MOVE_DOWN_RIGHT){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	else {//direction==DOWN_LEFT
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    long qt2=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,qt2);
	}
	vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
    }

    void rememberLocation(Location l){
	if (previousLocations.size()>=MAX_PREV_LOC){// as a result of release/click being undifferentiated)
	    previousLocations.removeElementAt(0);
	}
	if (previousLocations.size()>0){
	    if (!Location.equals((Location)previousLocations.lastElement(),l)){
		previousLocations.add(l);
	    }
	}
	else {previousLocations.add(l);}
    }

    void moveBack(){
	if (previousLocations.size()>0){
	    Location newlc=(Location)previousLocations.lastElement();
	    Location currentlc = mSpace.getCamera(0).getLocation();
	    Vector animParams=Location.getDifference(currentlc,newlc);
	    vsm.animator.createCameraAnimation(ConfigManager.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_TRANS_SIG,
					       animParams, mSpace.getCamera(0).getID());
	    previousLocations.removeElementAt(previousLocations.size()-1);
	}
    }

    /*show/hide radar view*/
    void showRadarView(boolean b){
	if (b){
	    if (rView == null){
		Vector cameras = new Vector();
		cameras.add(mSpace.getCamera(1));
		cameras.add(rSpace.getCamera(0));
		vsm.addExternalView(cameras, radarView, View.STD_VIEW, ConfigManager.rdW, ConfigManager.rdH, false, true);
		reh = new RadarEvtHdlr(this);
		rView = vsm.getView(radarView);
		rView.setBackgroundColor(ConfigManager.backgroundColor);
		rView.setEventHandler(reh);
		rView.setResizable(false);
		rView.setActiveLayer(1);
		rView.setCursorIcon(java.awt.Cursor.MOVE_CURSOR);
		vsm.getGlobalView(mSpace.getCamera(1),100);
		cameraMoved();
	    }
	    else {
		rView.toFront();
	    }
	}
    }

    public void cameraMoved(){//interface AnimationListener (com.xerox.VTM.engine)
	if (rView!=null){
	    Camera c0=mSpace.getCamera(1);
	    Camera c1=rSpace.getCamera(0);
	    c1.posx=c0.posx;
	    c1.posy=c0.posy;
	    c1.focal=c0.focal;
	    c1.altitude=c0.altitude;
	    long[] wnes=mainView.getVisibleRegion(mSpace.getCamera(0));
	    observedRegion.moveTo((wnes[0]+wnes[2])/2,(wnes[3]+wnes[1])/2);
	    observedRegion.setWidth((wnes[2]-wnes[0])/2);
	    observedRegion.setHeight((wnes[1]-wnes[3])/2);
	}
	vsm.repaintNow();
    }

    void updateMainViewFromRadar(){
	Camera c0 = mSpace.getCamera(0);
	c0.posx = observedRegion.vx;
	c0.posy = observedRegion.vy;
	vsm.repaintNow();
    }

    void centerRadarView(){
	if (rView != null){
	    vsm.getGlobalView(mSpace.getCamera(1),ConfigManager.ANIM_MOVE_LENGTH);
	    cameraMoved();
	}
    }

    /*--------------------------- Lens management --------------------------*/

    void setLens(int t){
	lensType = t;
    }

    void moveLens(int x, int y, long absTime){
	if (fLens != null){// dealing with a fading lens
	    fLens.setAbsolutePosition(x, y, absTime);
	}
	else {// dealing with a probing lens
	    lens.setAbsolutePosition(x, y);
	}
	vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
 	    lens = mainView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	setLens(GraphicsManager.ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
	long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - mainCamera.posx));
	long c2y = Math.round(my - INV_MAG_FACTOR * (my - mainCamera.posy));
	Vector cadata = new Vector();
	// -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
	Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
	if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
	    cadata.add(deltAlt);
	    cadata.add(new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, mainCamera.getID(), null);
	}
	else {
	    Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
	    double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
	    cadata.add(actualDeltAlt);
	    cadata.add(new LongPoint(Math.round((c2x-mainCamera.posx)*ratio),
				     Math.round((c2y-mainCamera.posy)*ratio)));
	    // animate lens and camera simultaneously (lens will die at the end)
	    vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					     lens.getID(), new ZP2LensAction(this));
	    vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					       cadata, mainCamera.getID(), null);
	}
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - mainCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - mainCamera.posy));
	Vector cadata = new Vector();
	cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
	cadata.add(new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy));
	// create lens if it does not exist
	if (lens == null){
	    lens = mainView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	// animate lens and camera simultaneously
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
					 lens.getID(), null);
	vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
					   cadata, mainCamera.getID(), null);
	setLens(GraphicsManager.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
					 lens.getID(), new ZP2LensAction(this));
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
		if (zooming == GraphicsManager.ZOOMOUT_LENS){
		    /* if unzooming, we want to keep the focus point stable, and unzoom the context
		       this means that camera altitude must be adjusted to keep altitude + lens mag
		       factor constant in the lens focus region. The camera must also be translated
		       to keep the same region of the virtual space under the focus region */
		    float a1 = mainCamera.getAltitude();
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
		    mainCamera.altitudeOffset((float)((a1+mainCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
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
		    mainCamera.move(Math.round((a1-mainCamera.getAltitude())/mainCamera.getFocal()*lens.lx),
				    -Math.round((a1-mainCamera.getAltitude())/mainCamera.getFocal()*lens.ly));
		}
		else {
		    vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
						     lens.getID(), null);
		}
	    }
	}
    }

    Lens getLensDefinition(int x, int y){
	Lens res;
	if (tp.isFadingLensNavMode()){
	    fLens = new LInfTFadingLens(1.0f, 0.0f, 0.95f, 100, x - panelWidth/2, y - panelHeight/2);
	    res = fLens;
	}
	else if (tp.isMeltingLensNavMode()){
	    res = new TGaussianLens(1.0f, 0.0f, 0.90f, 150, 50, x - panelWidth/2, y - panelHeight/2);
	    fLens = null;
	}
	else {// isProbingLensNavMode()
	    res = new FSGaussianLens(1.0f, 100, 50, x - panelWidth/2, y - panelHeight/2);
	    fLens = null; // unset any previous fading lens to make sure it gets garbage collected
	}
	return res;
    }
    
    /*-------------        DragMag        -----------------*/

    void triggerDM(int x, int y){
	if (dmPortal != null){// portal is active, destroy it
	    killDM();
	}
	else {// portal not active, create it
	    createDM(x, y);
	}	
    }

    void createDM(int x, int y){
	dmPortal = new DraggableCameraPortal(x, y, GraphicsManager.DM_PORTAL_WIDTH, GraphicsManager.DM_PORTAL_HEIGHT, dmCamera);
	dmPortal.setPortalEventHandler((PortalEventHandler)meh);
	dmPortal.setBackgroundColor(mainView.getBackgroundColor());
	vsm.addPortal(dmPortal, mainView);
	dmPortal.setBorder(GraphicsManager.DM_COLOR);
	Location l = dmPortal.getSeamlessView(mainCamera);
	dmCamera.moveTo(l.vx, l.vy);
	dmCamera.setAltitude((float)((mainCamera.getAltitude()+mainCamera.getFocal())/(DEFAULT_MAG_FACTOR)-mainCamera.getFocal()));
	updateMagWindow();
	int w = Math.round(magWindow.getWidth() * 2 * mainCamera.getFocal() / ((float)(mainCamera.getFocal()+mainCamera.getAltitude())));
	int h = Math.round(magWindow.getHeight() * 2 * mainCamera.getFocal() / ((float)(mainCamera.getFocal()+mainCamera.getAltitude())));
	dmPortal.sizeTo(w, h);
	mSpace.onTop(magWindow);
	mSpace.show(magWindow);
	paintLinks = true;
	Point[] data = {new Point(GraphicsManager.DM_PORTAL_WIDTH-w, GraphicsManager.DM_PORTAL_HEIGHT-h),
			new Point(GraphicsManager.DM_PORTAL_INITIAL_X_OFFSET-w/2, GraphicsManager.DM_PORTAL_INITIAL_Y_OFFSET-h/2)};
	vsm.animator.createPortalAnimation(GraphicsManager.DM_PORTAL_ANIM_TIME, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
    }

    void killDM(){
	if (dmPortal != null){
	    vsm.destroyPortal(dmPortal);
	    dmPortal = null;
	    mSpace.hide(magWindow);
	    paintLinks = false;
	}
	meh.resetDragMagInteraction();
    }

    long[] dmwnes = new long[4];

    void updateMagWindow(){
	if (dmPortal == null){return;}
	dmPortal.getVisibleRegion(dmwnes);
	magWindow.moveTo(dmCamera.posx, dmCamera.posy);
	magWindow.setWidth((dmwnes[2]-dmwnes[0]) / 2 + 1);
	magWindow.setHeight((dmwnes[1]-dmwnes[3]) / 2 + 1);
    }

    void updateZoomWindow(){
	dmCamera.moveTo(magWindow.vx, magWindow.vy);
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (paintLinks){
	    float coef=(float)(mainCamera.focal/(mainCamera.focal+mainCamera.altitude));
	    int magWindowX = (viewWidth/2) + Math.round((magWindow.vx-mainCamera.posx)*coef);
	    int magWindowY = (viewHeight/2) - Math.round((magWindow.vy-mainCamera.posy)*coef);
	    int magWindowW = Math.round(magWindow.getWidth()*coef);
	    int magWindowH = Math.round(magWindow.getHeight()*coef);
	    g2d.setColor(GraphicsManager.DM_COLOR);
	    g2d.drawLine(magWindowX-magWindowW, magWindowY-magWindowH, dmPortal.x, dmPortal.y);
	    g2d.drawLine(magWindowX+magWindowW, magWindowY-magWindowH, dmPortal.x+dmPortal.w, dmPortal.y);
	    g2d.drawLine(magWindowX-magWindowW, magWindowY+magWindowH, dmPortal.x, dmPortal.y+dmPortal.h);
	    g2d.drawLine(magWindowX+magWindowW, magWindowY+magWindowH, dmPortal.x+dmPortal.w, dmPortal.y+dmPortal.h);
	}
    }

    /* ---------- search -----------*/

    /*given a string, centers on a VText with this string in it*/
    void search(String s, int direction){
	if (s.length()>0){
	    if (!s.toLowerCase().equals(lastSearchedString)){//searching a new string - reinitialize everything
		resetSearch(s);
		Glyph[] gl = mSpace.getVisibleGlyphList();
		for (int i=0;i<gl.length;i++){
		    if (gl[i] instanceof VText){
			if ((((VText)gl[i]).getText() != null) &&
			    (((VText)gl[i]).getText().toLowerCase().indexOf(lastSearchedString)!=-1)){
			    matchingList.add(gl[i]);
			}
		    }
		}
	    }
	    int matchSize = matchingList.size();
	    if (matchSize > 0){
		//get prev/next entry in the list of matching elements
		searchIndex = searchIndex + direction;
		if (searchIndex < 0){// if reached start/end of list, go to end/start (loop)
		    searchIndex = matchSize - 1;
		}
		else if (searchIndex >= matchSize){
		    searchIndex = 0;
		}
		if (matchSize > 1){
		    statusBar.setStatusBarText(AppletUtils.rankString(searchIndex+1) + " of " + matchSize + " matches");
		}
		else {
		    statusBar.setStatusBarText("1 match");
		}
		//center on the entity
		Glyph g = (Glyph)matchingList.elementAt(searchIndex);
		vsm.centerOnGlyph(g/*lastMatchingEntity*/, mSpace.getCamera(0), ConfigManager.ANIM_MOVE_LENGTH, true, ConfigManager.MAG_FACTOR * 1.5f);
	    }
	    else {
		statusBar.setStatusBarText("No match");
	    }
	}
    }

    /*reset the search variables after it is finished*/
    void resetSearch(String s){
	searchIndex = -1;
	lastSearchedString = s.toLowerCase();
	matchingList.removeAllElements();
    }

    /* -------------- Font management ----------------*/

    void assignFontToGraph(){
	Font f = net.claribole.zvtm.fonts.FontDialog.getFontDialog((JFrame)mainView.getFrame(), ConfigManager.defaultFont);
	if (f!=null){
	    ConfigManager.defaultFont=f;
	    Vector glyphs = mSpace.getAllGlyphs();
	    Object g;
	    for (int i=0;i<glyphs.size();i++){
		g = glyphs.elementAt(i);
		if (g instanceof VText){
		    ((VText)g).setSpecialFont(null);
		}
	    }
	    vsm.setMainFont(ConfigManager.defaultFont);
	}
    }
    
    public void componentResized(ComponentEvent e){
	if (e.getSource() == mainView.getFrame()){
	    updatePanelSize();
	    //update rectangle showing observed region in radar view when main view's aspect ratio changes
	    cameraMoved();
	    //update SD_ZOOM_THRESHOLD
	    Dimension sz = mainView.getFrame().getSize();
	    cfgMngr.setSDZoomThreshold(0.3 * Math.sqrt(Math.pow(sz.width, 2) + Math.pow(sz.height, 2)));
	}
    }

    public void componentHidden(ComponentEvent e){}

    public void componentMoved(ComponentEvent e){}

    public void componentShown(ComponentEvent e){}

}
