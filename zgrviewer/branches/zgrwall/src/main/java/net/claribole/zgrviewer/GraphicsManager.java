/*   FILE: GraphicsManager.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */ 

package net.claribole.zgrviewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import java.awt.event.ComponentListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.DraggableCameraPortal;
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.lens.FixedSizeLens;
import fr.inria.zvtm.lens.LInfSCBLens;
import fr.inria.zvtm.lens.SCBLens;
import fr.inria.zvtm.lens.FSGaussianLens;
import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.RectangleNR;
import fr.inria.zvtm.glyphs.CircleNR;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.svg.Metadata;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.PortalEventHandler;
import fr.inria.zvtm.engine.TransitionManager;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

/* Multiscale feature manager */

public class GraphicsManager implements ComponentListener, CameraListener, Java2DPainter {

    static final Color FADE_COLOR = Color.WHITE;

    public VirtualSpaceManager vsm;
    AnimationManager animator;
    VirtualSpace mSpace;   // virtual space containing graph
    VirtualSpace mnSpace;  // virtual space containing pie menu
    VirtualSpace rSpace;   // virtual space containing rectangle representing region seen through main camera (used in overview)
    static final String mainSpaceName = "graphSpace";
    static final String menuSpace = "menuSpace";
    /*name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view*/
    static final String rdRegionVirtualSpaceName = "radarSpace";
    /*represents the region seen by main view in the radar view*/
    VRectangle observedRegion;

    public View mainView;
    private ClusteredView clusteredView;
    private WallCursor wallCursor;
    private WallController wallController;
    private VirtualSpace wCursorVs;
    private Camera wCursorCam;
    private boolean dragging = false;
    private Location dragStart;

    View rView;
    static final String RADAR_VIEW_NAME = "Overview";
    Camera mainCamera;
    JPanel mainViewPanel;

    PeriodicActionManager paMngr;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    
    /* misc. lens settings */
    Lens lens;
    SCBLens fLens;
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

    /* Some versions of GraphViz generate a rectangle representing the graph's bounding box.
     * We don't want this rectangle neither to be highlighted nor sensitive to zoom clicks,
     * so we try to find it after the SVG has been parsed, and we give it special treatment.
     * null if no bounding box was found in the SVG.
     */
    VRectangleOr boundingBox;

    ZGRGlassPane gp;

    ZGRApplication zapp;

    /* logical structure available only if a recent version of GraphViz
       was used to generate the SVG file (e.g. 2.13, maybe earlier) */
    LogicalStructure lstruct = null;

    private static final int BLOCK_WIDTH = 2760;
    private static final int BLOCK_HEIGHT = 1840;
    private static final int BLOCK_COUNT_HORIZ = 8;
    private static final int BLOCK_COUNT_VERT = 4;

    GraphicsManager(ZGRApplication za){
	this.zapp = za;
    }

    Vector createZVTMelements(boolean applet){
        vsm = VirtualSpaceManager.INSTANCE;
        VText.setMainFont(ConfigManager.defaultFont);
        Glyph.setDefaultMouseInsideColor(ConfigManager.HIGHLIGHT_COLOR);
        animator = vsm.getAnimationManager();
        //vsm.setDebug(true);
        mSpace = vsm.addVirtualSpace(mainSpaceName);
        wCursorVs = vsm.addVirtualSpace("cursorSpace");
        wCursorCam = wCursorVs.addCamera();
        wallCursor = new WallCursor(wCursorVs);
        // camera #0 for main view
        mainCamera = mSpace.addCamera();
        mainCamera.setZoomFloor(-90);
        // camera #1 for radar view
        mSpace.addCamera();
        mnSpace = vsm.addVirtualSpace(menuSpace);
        // camera for pie menu
        mnSpace.addCamera().setAltitude(10);
        rSpace = vsm.addVirtualSpace(rdRegionVirtualSpaceName);
        // camera for rectangle representing region seen in main viewport (in overview)
        rSpace.addCamera();
        // DragMag portal camera (camera #2)
        dmCamera = mSpace.addCamera();
        RectangleNR seg1;
        RectangleNR seg2;
        observedRegion = new VRectangle(0, 0, 0, 10, 10, ConfigManager.OBSERVED_REGION_COLOR, ConfigManager.OBSERVED_REGION_CROSSHAIR_COLOR, 0.5f);
        //500 should be sufficient as the radar window is
        seg1 = new RectangleNR(0, 0, 0, 0, 500, ConfigManager.OBSERVED_REGION_CROSSHAIR_COLOR);
        //not resizable and is 300x200 (see rdW,rdH below)
        seg2 = new RectangleNR(0, 0, 0, 500, 0, ConfigManager.OBSERVED_REGION_CROSSHAIR_COLOR);
        if (!(Utils.osIsWindows() || Utils.osIsMacOS())){
            observedRegion.setFilled(false);
        }
        rSpace.addGlyph(observedRegion);
        rSpace.addGlyph(seg1);
        rSpace.addGlyph(seg2);
        Glyph.stickToGlyph(seg1, observedRegion);
        Glyph.stickToGlyph(seg2, observedRegion);
        observedRegion.setSensitivity(false);
        tp = new ToolPalette(this);
        Vector cameras = new Vector();
        cameras.add(mSpace.getCamera(0));
        cameras.add(mnSpace.getCamera(0));
        cameras.add(tp.getPaletteCamera());
        cameras.add(wCursorCam);
        return cameras;
    }

    void createFrameView(Vector cameras, short vt, JMenuBar jmb){
        mainView = vsm.addFrameView(cameras, ConfigManager.MAIN_TITLE, vt,
            ConfigManager.mainViewW, ConfigManager.mainViewH,
            true, false, jmb);

        //experimental cluster stuff
         ClusterGeometry clGeom = new ClusterGeometry(
                BLOCK_WIDTH,
                BLOCK_HEIGHT,
                BLOCK_COUNT_HORIZ,
                BLOCK_COUNT_VERT);
		clusteredView = 
            new ClusteredView(
                    clGeom,
                    BLOCK_COUNT_VERT-1, //origin (block number)
                    BLOCK_COUNT_HORIZ, //use complete
                    BLOCK_COUNT_VERT,  //cluster surface
                    cameras);
        clusteredView.setBackgroundColor(Color.WHITE);
        vsm.addClusteredView(clusteredView);
        wallController = new WallController(clGeom);
        wallController.setListener(this);
      
        mainView.setLocation(ConfigManager.mainViewX,ConfigManager.mainViewY);
        mainView.getFrame().addComponentListener(this);
        gp = new ZGRGlassPane(this);
        ((JFrame)mainView.getFrame()).setGlassPane(gp);
    }


    JPanel createPanelView(Vector cameras, int w, int h){
	vsm.addPanelView(cameras, ConfigManager.MAIN_TITLE, w, h);
	mainView = vsm.getView(ConfigManager.MAIN_TITLE);
	return mainView.getPanel();
    }

    void parameterizeView(BaseEventHandler eh){
	paMngr = new PeriodicActionManager(this);
	mainView.setBackgroundColor(cfgMngr.backgroundColor);
	meh = eh;
	// same event handler handling all layers for now
	//XXX: TBD: refactor event handler code taking advantage of new one handler per layer functionality 
	mainView.setEventHandler((ViewEventHandler)eh, 0);
	mainView.setEventHandler((ViewEventHandler)eh, 1);
	mainView.setEventHandler((ViewEventHandler)eh, 2);
	mainView.setNotifyMouseMoved(true);
    mainCamera.addListener(this);

	mainView.setVisible(true);
	mainView.getPanel().addMouseMotionListener(paMngr);
	paMngr.start();
	mainView.setJava2DPainter(paMngr, Java2DPainter.AFTER_PORTALS);
	mainView.setJava2DPainter(this, Java2DPainter.FOREGROUND);

    activateDynaSpot(ConfigManager.DYNASPOT, false);
    mainView.getCursor().setDynaSpotColor(Color.RED);

	mainViewPanel = mainView.getPanel();
	setAntialiasing(ConfigManager.ANTIALIASING);

	initDM();
	updatePanelSize();
	previousLocations=new Vector();

    }

    public void onLeftPress(){
        dragging = true;
        dragStart = mainCamera.getLocation();
    }

    public void onLeftRelease(){
        dragging = false;
    }

    /** 
     * Currently called by WallController
     */
    void onPointerCoordsUpdate(final int x, final int y, final boolean leftMouse, final boolean rightMouse, final int wheel){
        //update cursor position
        //if dragging, update main camera position
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){ 
                LongPoint spcCoordsCursor = clusteredView.viewToSpaceCoords(wCursorCam, x, y); 
                LongPoint spcCoordsMain = clusteredView.viewToSpaceCoords(mainCamera, x, y); 

                if(dragging){
                    //drag camera
                    mainCamera.move(0.01*(spcCoordsMain.x - dragStart.vx),
                        0.01*(spcCoordsMain.y - dragStart.vy));
                    dragStart = mainCamera.getLocation();
                }
            
                wallCursor.moveTo(spcCoordsCursor.x, spcCoordsCursor.y);
            }
        });
    }

    void activateDynaSpot(boolean b, boolean updatePrefs){
        if (updatePrefs){ConfigManager.DYNASPOT = b;}
        mainView.getCursor().activateDynaSpot(b);
    }

    void setConfigManager(ConfigManager cm){
	this.cfgMngr = cm;
    }

	void reset(){
		mSpace.removeAllGlyphs();
		mSpace.addGlyph(magWindow);
		mSpace.hide(magWindow);
		previousLocations.removeAllElements();
		highlightedEdges.removeAllElements();
		highlightedNodes.removeAllElements();
		originalEdgeColor.removeAllElements();
		originalNodeFillColor.removeAllElements();
		originalNodeBorderColor.removeAllElements();
	}

    void initDM(){
	magWindow = new VRectangle(0, 0, 0, 1, 1, GraphicsManager.DM_COLOR);
	magWindow.setFilled(false);
	magWindow.setBorderColor(GraphicsManager.DM_COLOR);
	mSpace.addGlyph(magWindow);
	mSpace.hide(magWindow);	
    }

	/* Starting at version ? (somewhere between 1.16 and 2.8), GraphViz programs generate a polygon that bounds the entire graph.
		* Attempt to identify it so that when clicking in what appears to be empty space (but is actually the bounding box),
		* the view does not get unzoomed. Also prevent border highlighting when the cursor enters this bounding box.
		*/
	void seekBoundingBox(){
		Vector v = mSpace.getAllGlyphs();
		VRectangleOr largestRectangle = null;
		VRectangleOr r;
		int lri = -1; // largest rectangle's index
		// First identify the largest rectangle
		for (int i=0;i<v.size();i++){
			if (v.elementAt(i) instanceof VRectangleOr){
				r = (VRectangleOr)v.elementAt(i);
				if (largestRectangle == null || bigger(r, largestRectangle)){
					// first rectangle encountered in the list, or compare this rectangle to biggest rectangle at this time
					largestRectangle = r;
					lri = i;
				}
			}
		}
		if (lri == -1){return;}
		// Then check that all other nodes are contained within that rectangle.
		//XXX: disabled check for objects lower in the stack as their seems to be some unidentified object that messes around
		// in some versions of GraphViz such as 2.14 - this workaround should be fairly safe until we identify that object
//		for (int i=0;i<lri;i++){
//			if (!containedIn((Glyph)v.elementAt(i), largestRectangle)){
//				System.out.println(v.elementAt(i));
//				//return;
//			}
//		}
		for (int i=lri+1;i<v.size();i++){
			if (!containedIn((Glyph)v.elementAt(i), largestRectangle)){
				return;
			}
		}
		// If they are, then it is very likely that the rectangle is a bounding box.
		boundingBox = largestRectangle;
		boundingBox.setVisible(false);
		boundingBox.setSensitivity(false);
	}

    boolean bigger(VRectangleOr r1, VRectangleOr r2){
        // returns true if r1 bigger than r2
	    return (r1.getWidth()*r1.getHeight() > r2.getWidth()*r2.getHeight());
    }

    boolean containedIn(Glyph g, VRectangle r){
	if (g instanceof DPath || g instanceof VText){
	    return true;// don't take edges and text into accout, would be too costly (and would require one repaint for text)
	}
	else {// just get geometrical center for other glyphs ; this is an approximation, but it should work 
	    return g.vx > r.vx-r.getWidth() && g.vx < r.vx+r.getWidth()
		&& g.vy > r.vy-r.getHeight() && g.vy < r.vy+r.getHeight();
	}
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

    /*----------  Reveal graph (after loading) --------------*/

	void reveal(){
		Camera c = mSpace.getCamera(0);
		Location l = mainView.getGlobalView(c);
		c.posx = l.vx;
		c.posy = l.vy;
		c.updatePrecisePosition();
		c.setAltitude(l.alt-c.getFocal());
		rememberLocation(mSpace.getCamera(0).getLocation());
		TransitionManager.fadeIn(mainView, 500, vsm);
		getGlobalView();
	}

    /*-------------     Navigation              -------------*/

    void getGlobalView(){
	Location l = mainView.getGlobalView(mSpace.getCamera(0),ConfigManager.ANIM_MOVE_LENGTH);
	rememberLocation(mSpace.getCamera(0).getLocation());
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getHigherView(){
        Camera c=mainView.getCameraNumber(0);
        rememberLocation(c.getLocation());
        Float alt=new Float(c.getAltitude()+c.getFocal());
        Animation a = animator.getAnimationFactory().createCameraAltAnim(ConfigManager.ANIM_MOVE_LENGTH, c,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        animator.startAnimation(a, false);
    }

    /*higher view (multiply altitude by altitudeFactor)*/
    void getLowerView(){
        Camera c=mainView.getCameraNumber(0);
        rememberLocation(c.getLocation());
        Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
        Animation a = animator.getAnimationFactory().createCameraAltAnim(ConfigManager.ANIM_MOVE_LENGTH, c,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        animator.startAnimation(a, false);
    }

    /* Direction should be one of GraphicsManager.MOVE_* */
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
        else {
            //direction==DOWN_LEFT
            long qt=Math.round((rb[3]-rb[1])/2.4);
            long qt2=Math.round((rb[0]-rb[2])/2.4);
            trans=new LongPoint(qt,qt2);
        }
        Animation a = animator.getAnimationFactory().createCameraTranslation(ConfigManager.ANIM_MOVE_LENGTH, c,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        animator.startAnimation(a, false);
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
            Vector animParams = Location.getDifference(mSpace.getCamera(0).getLocation(), (Location)previousLocations.lastElement());
            Animation at = animator.getAnimationFactory().createCameraTranslation(ConfigManager.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (LongPoint)animParams.elementAt(1), true, SlowInSlowOutInterpolator.getInstance(), null);
            Animation aa = animator.getAnimationFactory().createCameraAltAnim(ConfigManager.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Float)animParams.elementAt(0), true, SlowInSlowOutInterpolator.getInstance(), null);
            animator.startAnimation(at, false);
            animator.startAnimation(aa, false);
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
                vsm.addFrameView(cameras, RADAR_VIEW_NAME, View.STD_VIEW, ConfigManager.rdW, ConfigManager.rdH, false, true);
                reh = new RadarEvtHdlr(this);
                rView = vsm.getView(RADAR_VIEW_NAME);
                rView.setBackgroundColor(cfgMngr.backgroundColor);
                // same event handler handling all layers for now
                //XXX: TBD: refactor event handler code taking advantage of new one-handler-per-layer functionality 
                rView.setEventHandler(reh, 0);
                rView.setEventHandler(reh, 1);
                rView.setResizable(false);
                rView.setActiveLayer(1);
                rView.setCursorIcon(java.awt.Cursor.MOVE_CURSOR);
                rView.getGlobalView(mSpace.getCamera(1),100);
                // give null arguments because the method does not really care
                cameraMoved(null, null, 0);
            }
            else {
                rView.toFront();
            }
        }
    }

    public void cameraMoved(Camera cam, LongPoint coord, float alt){
        if (rView!=null){
            Camera c0=mSpace.getCamera(1);
            Camera c1=rSpace.getCamera(0);
            c1.posx=c0.posx;
            c1.posy=c0.posy;
            c1.focal=c0.focal;
            c1.altitude=c0.altitude;
            c1.updatePrecisePosition();
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

    void centerRADAR_VIEW_NAME(){
	if (rView != null){
	    rView.getGlobalView(mSpace.getCamera(1),ConfigManager.ANIM_MOVE_LENGTH);
	    cameraMoved(null, null, 0);
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
        Animation a = animator.getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        animator.startAnimation(a, false);
        setLens(GraphicsManager.ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - mainCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - mainCamera.posy));
        //Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            Animation al = animator.getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = animator.getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mainCamera,
                new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy), true, IdentityInterpolator.getInstance(), null);
            Animation aa = animator.getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mainCamera,
                deltAlt, true, IdentityInterpolator.getInstance(), null);
            animator.startAnimation(al, false);
            animator.startAnimation(at, false);
            animator.startAnimation(aa, false);
        }
        else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            Animation al = animator.getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = animator.getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mainCamera,
                new LongPoint(Math.round((c2x-mainCamera.posx)*ratio), Math.round((c2y-mainCamera.posy)*ratio)), true, IdentityInterpolator.getInstance(), null);
            Animation aa = animator.getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mainCamera,
                actualDeltAlt, true, IdentityInterpolator.getInstance(), null);
            animator.startAnimation(al, false);
            animator.startAnimation(at, false);
            animator.startAnimation(aa, false);
        }
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = mainCamera.getAltitude()+mainCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - mainCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - mainCamera.posy));
        // create lens if it does not exist
        if (lens == null){
            lens = mainView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        Animation al = animator.getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        Animation at = animator.getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mainCamera,
            new LongPoint(c2x-mainCamera.posx, c2y-mainCamera.posy), true, IdentityInterpolator.getInstance(), null);
        Animation aa = animator.getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mainCamera,
            new Float(cameraAbsAlt*(MAG_FACTOR-1)), true, IdentityInterpolator.getInstance(), null);
        animator.startAnimation(al, false);
        animator.startAnimation(at, false);
        animator.startAnimation(aa, false);
        setLens(GraphicsManager.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
        // make lens disappear (killing anim)
        Animation a = animator.getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
        animator.startAnimation(a, false);
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
                    Animation a = animator.getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
                        new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
                    animator.startAnimation(a, false);
                }
            }
        }
    }

    Lens getLensDefinition(int x, int y){
        Lens res;
        if (tp.isFadingLensNavMode()){
            fLens = new LInfSCBLens(1.0f, 0.0f, 0.95f, 100, x - panelWidth/2, y - panelHeight/2);
            res = fLens;
        }
        else {
            // isProbingLensNavMode()
            res = new FSGaussianLens(1.0f, 100, 50, x - panelWidth/2, y - panelHeight/2);
            // unset any previous fading lens to make sure it gets garbage collected
            fLens = null;
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
        //animator.createPortalAnimation(GraphicsManager.DM_PORTAL_ANIM_TIME, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
        Animation as = animator.getAnimationFactory().createPortalSizeAnim(GraphicsManager.DM_PORTAL_ANIM_TIME, dmPortal,
            GraphicsManager.DM_PORTAL_WIDTH-w, GraphicsManager.DM_PORTAL_HEIGHT-h, true,
            IdentityInterpolator.getInstance(), null);
        Animation at = animator.getAnimationFactory().createPortalTranslation(GraphicsManager.DM_PORTAL_ANIM_TIME, dmPortal,
            new Point(GraphicsManager.DM_PORTAL_INITIAL_X_OFFSET-w/2, GraphicsManager.DM_PORTAL_INITIAL_Y_OFFSET-h/2), true,
            IdentityInterpolator.getInstance(), null);
        animator.startAnimation(as, false);
        animator.startAnimation(at, false);
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

    Glyph highlightedLabel;
    Color originalHighlightedLabelColor;

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
		    zapp.setStatusBarText(AppletUtils.rankString(searchIndex+1) + " of " + matchSize + " matches");
		}
		else {
		    zapp.setStatusBarText("1 match");
		}
		//center on the entity
		Glyph g = (Glyph)matchingList.elementAt(searchIndex);
		// g is the lastMatchingEntity
		mainView.centerOnGlyph(g, mSpace.getCamera(0), ConfigManager.ANIM_MOVE_LENGTH, true, ConfigManager.MAG_FACTOR * 1.5f);
		highlight(g);
		vsm.repaintNow();
	    }
	    else {
		zapp.setStatusBarText("No match");
	    }
	}
    }

    /*reset the search variables after it is finished*/
    void resetSearch(String s){
	searchIndex = -1;
	lastSearchedString = s.toLowerCase();
	matchingList.removeAllElements();
	if (highlightedLabel != null){
	    highlightedLabel.setColor(originalHighlightedLabelColor);
	    highlightedLabel = null;
	}
    }

    /* color the label found by search */
    void highlight(Glyph g){
	// de-highlight previous label (if any)
	if (highlightedLabel != null){
	    highlightedLabel.setColor(originalHighlightedLabelColor);
	}
	highlightedLabel = g;
	originalHighlightedLabelColor = highlightedLabel.getColor();
	highlightedLabel.setColor(ConfigManager.HIGHLIGHT_COLOR);
    }
    
    /* -------- Progress bar on glass pane -----------*/
    
    
    

    /* -------------- Font management ----------------*/

    void assignFontToGraph(){
	Font f = fr.inria.zvtm.fonts.FontDialog.getFontDialog((JFrame)mainView.getFrame(), ConfigManager.defaultFont);
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
	    VText.setMainFont(ConfigManager.defaultFont);
	}
    }
    
    public void componentResized(ComponentEvent e){
	if (e.getSource() == mainView.getFrame()){
	    updatePanelSize();
	    //update rectangle showing observed region in radar view when main view's aspect ratio changes
	    cameraMoved(null, null, 0);
	    //update SD_ZOOM_THRESHOLD
	    Dimension sz = mainView.getFrame().getSize();
	    cfgMngr.setSDZoomThreshold(0.3 * Math.sqrt(Math.pow(sz.width, 2) + Math.pow(sz.height, 2)));
	}
    }

    public void componentHidden(ComponentEvent e){}

    public void componentMoved(ComponentEvent e){}

    public void componentShown(ComponentEvent e){}

    /* ------------- Logical structure ----------------- */
    void buildLogicalStructure(){
	// clone the structure as we are about to remove elements from it for convenience (it is supposed to be read-only)
	Vector glyphs = (Vector)mSpace.getAllGlyphs().clone();
	glyphs.remove(magWindow);
	glyphs.remove(boundingBox);
	lstruct = LogicalStructure.build(glyphs);
	if (lstruct == null){// building the logical structure failed
	    tp.hideLogicalTools();
	    mainView.setStatusBarText(Messages.FAILED_TO_BUILD_LOGICAL_STRUCT);
	}
	else {
	    tp.showLogicalTools();
	}
	/* take care of converting the owner of glyphs that were not processed as structural elements
	   (which should have remained Metadata instances). Convert these old owners into LElem instances
	   (as this is what ZGRViewer now expects to get when calling Glyph.getOwner()) */
	glyphs = mSpace.getAllGlyphs(); // not cloning here because we are just reading the structure
	Glyph g;
	for (int i=0;i<glyphs.size();i++){
	    g = (Glyph)glyphs.elementAt(i);
	    if (g.getOwner() != null && g.getOwner() instanceof Metadata){
		g.setOwner(new LElem((Metadata)g.getOwner()));
	    }
	}
    }

    /* ------------- Highlighting ----------------- */

    static final BasicStroke HIGHLIGHT_STROKE = new BasicStroke(2.0f);

    Vector highlightedEdges = new Vector();
    Vector originalEdgeColor = new Vector();
    Vector originalEdgeStroke = new Vector();
    
    Vector highlightedNodes = new Vector();
    Vector originalNodeBorderColor = new Vector();
    Vector originalNodeFillColor = new Vector();
    Vector originalNodeStroke = new Vector();

    void highlightElement(Glyph g, Camera cam, VCursor cursor, boolean highlight){
	Object o = null;
	if (g != null && g != boundingBox){// clicked inside a node
	    o = g.getOwner();
	}
	else {
	    // if cursor was not in a shape, try to detect a label
	    Vector otherGlyphs = cursor.getIntersectingTexts(cam);
	    if (otherGlyphs != null && otherGlyphs.size() > 0){
		g = (Glyph)otherGlyphs.firstElement();
		if (g.getOwner() != null){o = g.getOwner();}
	    }
	    // or an edge
	    else {
		otherGlyphs = cursor.getIntersectingPaths(cam);
		if (otherGlyphs != null && otherGlyphs.size() > 0){
		    g = (Glyph)otherGlyphs.firstElement();
		    if (g.getOwner() != null){o = g.getOwner();}
		}
		else {// could not detect anything, consider that user clicked on background
		    // unhighlight anything that could have been highlighted
		    unhighlightAll();
		}
	    }
	}
	if (o != null){
	    if (o instanceof LNode){
		highlightNode((LNode)o, highlight);
	    }
	    else if (o instanceof LEdge){
		highlightEdge((LEdge)o, highlight);
	    }
	}
    }

    synchronized void highlightNode(LNode n, boolean highlight){
	if (highlight){
	    Glyph g;
	    Glyph[] gs;
	    // highlight node itself
	    for (int i=0;i<n.glyphs.length;i++){
		g = n.glyphs[i];
		if (highlightedNodes.contains(g)){continue;}
		highlightedNodes.add(g);
		highlightNodeGlyph(g);
	    }
	    // for all edges linked to this node
	    for (int i=0;i<n.edges.length;i++){
		// highlight edge itself
		for (int j=0;j<n.edges[i].glyphs.length;j++){
		    g = n.edges[i].glyphs[j];
		    /* prevent elements from being processed more than once
		       this can happen when a node links to itself, and
		       this makes the highlighting mechanism think that
		       the arc's original color is the selection color */
		    if (highlightedEdges.contains(g)){continue;}
		    highlightedEdges.add(g);
		    highlightEdgeGlyph(g);
		}
		// highlight node at other end of arc
		// (can't rely on edge direction as edges might be undirected)
		gs = (n.edges[i].tail == n) ? n.edges[i].head.glyphs : n.edges[i].tail.glyphs;
		for (int j=0;j<gs.length;j++){
		    g = gs[j];
		    /* prevent elements from being processed more than once
		       this can happen when a node links to itself, and
		       this makes the highlighting mechanism think that
		       the arc's original color is the selection color */
		    if (highlightedNodes.contains(g)){continue;}
		    highlightedNodes.add(g);
		    highlightNodeGlyph(g);
		}
	    }
	}
	else {
	    unhighlightAll();
	}
    }

    synchronized void highlightEdge(LEdge e, boolean highlight){
	if (highlight){
	    Glyph g;
	    // highlight edge itself
	    for (int i=0;i<e.glyphs.length;i++){
		g = e.glyphs[i];
		if (highlightedEdges.contains(g)){continue;}
		highlightedEdges.add(g);
		highlightEdgeGlyph(g);
	    }
	    // highlight tail
	    for (int i=0;i<e.tail.glyphs.length;i++){
		g = e.tail.glyphs[i];
		/* prevent elements from being processed more than once
		   this can happen when a node links to itself, and
		   this makes the highlighting mechanism think that
		   the arc's original color is the selection color */
		if (highlightedNodes.contains(g)){continue;}
		highlightedNodes.add(g);
		highlightNodeGlyph(g);
	    }
	    // highlight head
	    for (int i=0;i<e.head.glyphs.length;i++){
		g = e.head.glyphs[i];
		/* prevent elements from being processed more than once
		   this can happen when a node links to itself, and
		   this makes the highlighting mechanism think that
		   the arc's original color is the selection color */
		if (highlightedNodes.contains(g)){continue;}
		highlightedNodes.add(g);
		highlightNodeGlyph(g);
	    }
	}
	else {
	    unhighlightAll();
	}
    }

    void highlightNodeGlyph(Glyph g){
        originalNodeStroke.add(g.getStroke());
        if (g instanceof ClosedShape){
            // keep both originalXXXColor vectors at the same length/indexes for a given glyph
            originalNodeFillColor.add(null);
            originalNodeBorderColor.add(((ClosedShape)g).getDefaultBorderColor());
            if (g.isBorderDrawn()){
                g.setBorderColor(ConfigManager.HIGHLIGHT_COLOR);
            }
        }
        else {
            // keep both originalXXXColor vectors at the same length/indexes for a given glyph
            originalNodeFillColor.add(null);
            originalNodeBorderColor.add(g.getDefaultColor());
            g.setColor(ConfigManager.HIGHLIGHT_COLOR);
        }
        g.setStroke(HIGHLIGHT_STROKE);
    }
    
    void highlightEdgeGlyph(Glyph g){
        originalEdgeColor.add(g.getDefaultColor());
        originalEdgeStroke.add(g.getStroke());
        if (g instanceof ClosedShape){
            if (g.isFilled()){
                // use border color to fill arrow head shape
                g.setColor(ConfigManager.HIGHLIGHT_COLOR);
            }
            if (g.isBorderDrawn()){
                g.setBorderColor(ConfigManager.HIGHLIGHT_COLOR);
            }
        }
        else {
            g.setColor(ConfigManager.HIGHLIGHT_COLOR);	    
        }
        g.setStroke(HIGHLIGHT_STROKE);
    }

    void unhighlightAll(){
        unhighlightAllEdges();
        unhighlightAllNodes();
    }

    void unhighlightAllNodes(){
        Glyph g;
        for (int i=0;i<highlightedNodes.size();i++){
            g = (Glyph)highlightedNodes.elementAt(i);
            if (g instanceof ClosedShape){
                if (g.isBorderDrawn()){
                    g.setBorderColor((Color)originalNodeBorderColor.elementAt(i));
                }
            }
            else {
                g.setColor((Color)originalNodeBorderColor.elementAt(i));
            }
            g.setStroke((BasicStroke)originalNodeStroke.elementAt(i));
        }
        highlightedNodes.removeAllElements();
        originalNodeBorderColor.removeAllElements();
        originalNodeFillColor.removeAllElements();
        originalNodeStroke.removeAllElements();
    }

    void unhighlightAllEdges(){
        Glyph g;
        for (int i=0;i<highlightedEdges.size();i++){
            g = (Glyph)highlightedEdges.elementAt(i);
            if (g instanceof ClosedShape){
                if (g.isFilled()){
                    g.setColor((Color)originalEdgeColor.elementAt(i));
                }
                if (g.isBorderDrawn()){
                    g.setBorderColor((Color)originalEdgeColor.elementAt(i));
                }
            }
            else {
                g.setColor((Color)originalEdgeColor.elementAt(i));
            }
            g.setStroke((BasicStroke)originalEdgeStroke.elementAt(i));
        }
        highlightedEdges.removeAllElements();
        originalEdgeColor.removeAllElements();
        originalEdgeStroke.removeAllElements();
    }

	/* -------------- Bring and Go mode (previously called Fresnel mode) -------------------- */
	
	static final int BRING_ANIM_DURATION = 300;
	static final double BRING_DISTANCE_FACTOR = 1.5;
	
	static final float FADED_ELEMENTS_TRANSLUCENCY = 0.1f;
	static final float SECOND_STEP_TRANSLUCENCY = 0.3f;
	
	boolean isBringingAndGoing = false;
	
	Vector broughtElements = new Vector();
	
	Vector elementsToFade;
	float[] alphaOfElementsToFade;

	void enterBringAndGoMode(){}
	
	void exitBringAndGoMode(){}
	
	void startBringAndGo(Glyph g){
		isBringingAndGoing = true;
		LNode n = LogicalStructure.getNode(g);
		if (n == null){return;}
		// prepare list of elements to fade out
		// (it contains too many elements at this point, but these will be
		// progressively removed from the list as we process connected nodes)
		elementsToFade = (Vector)mSpace.getAllGlyphs().clone();
		elementsToFade.remove(magWindow);
		ClosedShape thisEndShape = n.getShape();
		Glyph[] glyphs = n.getGlyphs();
		for (int i=0;i<glyphs.length;i++){
			elementsToFade.remove(glyphs[i]);
		}
		// compute layout of brought nodes
		double thisEndBoundingCircleRadius = thisEndShape.getSize();
		// distance between two rings
        double RING_STEP = 3 * thisEndBoundingCircleRadius;
        LEdge[] arcs = n.getAllArcs();
        // sort them according to distance from start node
        // (so as to try to keep the closest ones closer to the start node)
        Arrays.sort(arcs, new DistanceComparator(n));
        Hashtable node2broughtPosition = new Hashtable();
        RingManager rm = new RingManager();
        for (int i=0;i<arcs.length;i++){
            // ignore arcs that start and end at this node
            if (arcs[i].isLoop()){continue;}
            // process all others
            LNode otherEnd = arcs[i].getOtherEnd(n);
            ClosedShape otherEndShape = otherEnd.getShape();
            double d = Math.sqrt(Math.pow(otherEndShape.vx-thisEndShape.vx, 2) + Math.pow(otherEndShape.vy-thisEndShape.vy, 2));
            // use this end's shape size when getting ring because we're rescaling
            // brought nodes to match the size of the current node
            Ring ring = rm.getRing(Math.atan2(otherEndShape.vy-thisEndShape.vy, otherEndShape.vx-thisEndShape.vx), thisEndShape.getSize(), RING_STEP);
            double bd = ring.rank * RING_STEP;
            double ratio = bd / d;
            long bx = thisEndShape.vx + Math.round(ratio * (otherEndShape.vx-thisEndShape.vx));
            long by = thisEndShape.vy + Math.round(ratio * (otherEndShape.vy-thisEndShape.vy));
            node2broughtPosition.put(otherEnd, new LongPoint(bx, by));
        }
        // actually bring the arcs and nodes (animation)
        for (int i=0;i<arcs.length;i++){
            // ignore arcs that start and end at this node
            if (arcs[i].isLoop()){continue;}
            LNode otherEnd = arcs[i].getOtherEnd(n);
            ClosedShape otherEndShape = otherEnd.getShape();
            bring(arcs[i], otherEnd, thisEndShape.vx, thisEndShape.vy, otherEndShape.vx, otherEndShape.vy, thisEndShape.getSize(), node2broughtPosition);
        }
        // make other elements translucent (fade them out)
        alphaOfElementsToFade = new float[elementsToFade.size()];
        for (int i=0;i<elementsToFade.size();i++){
            try {
                Translucent t = (Translucent)elementsToFade.elementAt(i);
                alphaOfElementsToFade[i] = t.getTranslucencyValue();
                //XXX:TBW animate only if total number of objects is not too high
                Animation a = animator.getAnimationFactory().createTranslucencyAnim(
                    BRING_ANIM_DURATION, t, FADED_ELEMENTS_TRANSLUCENCY, false, IdentityInterpolator.getInstance(), null);
                animator.startAnimation(a, true);
            }
            catch (ClassCastException e){e.printStackTrace();}
        }
	}
	
	void endBringAndGo(Glyph g){
        isBringingAndGoing = false;
        // new camera location (if g != null)
        LongPoint newCameraLocation = null;
        if (!broughtElements.isEmpty()){
            for (int i=broughtElements.size()-1;i>=0;i--){
                LongPoint l = sendBack((BroughtElement)broughtElements.elementAt(i), g);
                if (l != null){
                    newCameraLocation = l;
                }
            }
        }
        for (int i=0;i<elementsToFade.size();i++){
            try {
                //XXX:TBW animate only if total number of objects is not too high
                Animation a = animator.getAnimationFactory().createTranslucencyAnim(
                    BRING_ANIM_DURATION, (Translucent)elementsToFade.elementAt(i), alphaOfElementsToFade[i],
                    false, IdentityInterpolator.getInstance(), null);
                animator.startAnimation(a, true);
            }
            catch (ClassCastException e){e.printStackTrace();}
        }
        elementsToFade.clear();
        if (newCameraLocation != null){
            Animation a = animator.getAnimationFactory().createCameraTranslation(
                BRING_ANIM_DURATION, mainCamera, newCameraLocation, false, SlowInSlowOutInterpolator.getInstance(), null);
            animator.startAnimation(a, true);
        }
	}
	
	void bring(LEdge arc, LNode node, long sx, long sy, long ex, long ey, float size, Hashtable node2broughtPosition){
	    broughtElements.add(BroughtElement.rememberPreviousState(node));
	    broughtElements.add(BroughtElement.rememberPreviousState(arc));
		// deal with node glyphs
		ClosedShape nodeShape = node.getShape();


        if (size < nodeShape.getSize()){
            Animation szAnim = animator.getAnimationFactory().createGlyphSizeAnim(
                BRING_ANIM_DURATION, nodeShape, size,
                false, SlowInSlowOutInterpolator.getInstance(), null);
            animator.startAnimation(szAnim, true);
            VText label = node.getLabel();
            if (label != null){
                label.setScale(size/nodeShape.getSize());
            }
        }


		elementsToFade.remove(nodeShape);
		LongPoint bposition = (LongPoint)node2broughtPosition.get(node);
		LongPoint translation = new LongPoint(bposition.x-nodeShape.vx, bposition.y-nodeShape.vy);
		Glyph[] glyphs = node.getGlyphs();
		for (int i=0;i<glyphs.length;i++){
			elementsToFade.remove(glyphs[i]);
			Animation posAnim = animator.getAnimationFactory().createGlyphTranslation(
                BRING_ANIM_DURATION, glyphs[i], translation,
                true, SlowInSlowOutInterpolator.getInstance(), null);
            animator.startAnimation(posAnim, true);
		}



		// deal with the edge's spline
		DPath spline = arc.getSpline();
		elementsToFade.remove(spline);
		LongPoint asp = spline.getStartPoint();
		LongPoint aep = spline.getEndPoint();
		LongPoint sp, ep;
		if (Math.sqrt(Math.pow(asp.x-ex,2) + Math.pow(asp.y-ey,2)) < Math.sqrt(Math.pow(asp.x-sx,2) + Math.pow(asp.y-sy,2))){
			sp = new LongPoint(bposition.x, bposition.y);
			ep = new LongPoint(sx, sy);
		}
		else {
			sp = new LongPoint(sx, sy);
			ep = new LongPoint(bposition.x, bposition.y);
		}
		mSpace.above(spline, boundingBox);
		LongPoint[] flatCoords = DPath.getFlattenedCoordinates(spline, sp, ep, true);
		Animation a = animator.getAnimationFactory().createPathAnim(BRING_ANIM_DURATION, spline, flatCoords,
		    false, SlowInSlowOutInterpolator.getInstance(), null);
		animator.startAnimation(a, true);
		// deal with the other glyphs that make the edge
		glyphs = arc.getGlyphs();
		for (int i=0;i<glyphs.length;i++){
			if (glyphs[i] != spline){
				elementsToFade.remove(glyphs[i]);
				if (glyphs[i] instanceof VText){
					// put any label at the center of the edge (simplest thing we can do)
					a = animator.getAnimationFactory().createGlyphTranslation(
					    BRING_ANIM_DURATION, glyphs[i], new LongPoint(bposition.x-sx, bposition.y-sy),
                        true, SlowInSlowOutInterpolator.getInstance(), null);
                    animator.startAnimation(a, true);
				}
				else {
					// probably a tail or head decoration ; just hide it for now, we don't know how to transform them correctly
					glyphs[i].setVisible(false);
				}
			}
		}
		LEdge[] otherArcs = node.getOtherArcs(arc);
		Glyph oe;
		for (int i=0;i<otherArcs.length;i++){
			broughtElements.add(BroughtElement.rememberPreviousState(otherArcs[i]));
			spline = otherArcs[i].getSpline();
			elementsToFade.remove(spline);
			asp = spline.getStartPoint();
			aep = spline.getEndPoint();
			if (node2broughtPosition.containsKey(otherArcs[i].getTail())
			    && node2broughtPosition.containsKey(otherArcs[i].getHead())){
				sp = (LongPoint)node2broughtPosition.get(otherArcs[i].getTail());
				ep = (LongPoint)node2broughtPosition.get(otherArcs[i].getHead());
			}
			else {
				oe = otherArcs[i].getOtherEnd(node).getShape();
				if (Math.sqrt(Math.pow(asp.x-ex,2) + Math.pow(asp.y-ey,2)) <= Math.sqrt(Math.pow(aep.x-ex,2) + Math.pow(aep.y-ey,2))){
					sp = new LongPoint(bposition.x, bposition.y);
					ep = oe.getLocation();
				}
				else {
					sp = oe.getLocation();
					ep = new LongPoint(bposition.x, bposition.y);
				}
			}
			flatCoords = DPath.getFlattenedCoordinates(spline, sp, ep, true);
			mSpace.above(spline, boundingBox);
			a = animator.getAnimationFactory().createPathAnim(BRING_ANIM_DURATION, spline, flatCoords,
    		    false, SlowInSlowOutInterpolator.getInstance(), null);
    		animator.startAnimation(a, true);
			spline.setTranslucencyValue(SECOND_STEP_TRANSLUCENCY);
		}		
    }
    
    LongPoint sendBack(BroughtElement be, Glyph g){
		broughtElements.remove(be);
        return be.restorePreviousState(BRING_ANIM_DURATION, g);
    }
	
	/* ----------------------- Link sliding navigation ----------------------------------- */
	
	static final int SLIDER_CURSOR_SIZE = 6;
	static final Color SLIDER_CURSOR_FILL = Color.WHITE;
	
	static final int SELECTION_RADIUS = 100;
	static final Color SELECTION_RADIUS_COLOR = Color.RED;
	
	boolean isLinkSliding = false;
	LinkSliderCalc[] lscs;
	int lsci = -1;
	
	DPath slidingLink;
	Color slidingLinkActualColor = null;
	Point2D mPos = new Point2D.Double();
	
	CircleNR slideCursor, selectionRadius;
	Point2D cPos;

    LNode closestNode;

	int screen_cursor_x,screen_cursor_y;
	Robot awtRobot;
	
	Point2D mtPos = new Point2D.Double();
	
	void attemptLinkSliding(long press_vx, long press_vy, int scr_x, int scr_y){
		long vieww = mainView.getVisibleRegionWidth(mainCamera);
		lsci = 0;
	    if (lstruct != null){
    		closestNode = lstruct.nodes[0];
            ClosedShape nodeShape = closestNode.getShape();
    		double shortestDistance = Math.sqrt(Math.pow(nodeShape.vx-press_vx,2)+Math.pow(nodeShape.vy-press_vy,2));
    		double distance;
    		for (int i=1;i<lstruct.nodes.length;i++){
    			nodeShape = lstruct.nodes[i].getShape();
    			distance = Math.sqrt(Math.pow(nodeShape.vx-press_vx,2)+Math.pow(nodeShape.vy-press_vy,2));
    			if (distance < shortestDistance){
    				closestNode = lstruct.nodes[i];
    				shortestDistance = distance;
    			}
    		}
    		if (shortestDistance < 2*closestNode.getShape().getSize()){
    			// if clicked near a node, select edge connected to this node closest to the click point
    			LEdge[] arcs = closestNode.getAllArcs();
    			if (arcs.length == 0){return;}
    			lscs = new LinkSliderCalc[arcs.length];
    			slidingLink = arcs[0].getSpline();
    			lscs[0] = new LinkSliderCalc(slidingLink, vieww);
    			mPos.setLocation(press_vx, press_vy);
    			lscs[0].updateMousePosition(mPos);
    			cPos = lscs[0].getPositionAlongPath();
    			shortestDistance = Math.sqrt(Math.pow(cPos.getX()-mPos.getX(),2) + Math.pow(cPos.getY()-mPos.getY(),2));
    			for (int i=1;i<arcs.length;i++){
    				lscs[i] = new LinkSliderCalc(arcs[i].getSpline(), vieww);
    				lscs[i].updateMousePosition(mPos);
    				cPos = lscs[i].getPositionAlongPath();
    				distance = Math.sqrt(Math.pow(cPos.getX()-mPos.getX(),2) + Math.pow(cPos.getY()-mPos.getY(),2));
    				if (distance < shortestDistance){
    					shortestDistance = distance;
    					slidingLink = arcs[i].getSpline();
    					lsci = i;
    				}
    			}
    			startLinkSliding(press_vx, press_vy, scr_x, scr_y);
    			return;
    		}
	    }
        // else select the edge hovered by the cursor (if any) - works even if no knowledge about logical structure
        closestNode = null;
        Vector pum = mainView.getCursor().getIntersectingPaths(mainCamera, 10);
        if (pum.size() > 0){
            slidingLink = (DPath)pum.firstElement();
            lscs = new LinkSliderCalc[1];
            lscs[lsci] = new LinkSliderCalc(slidingLink, vieww);
            startLinkSliding(press_vx, press_vy, scr_x, scr_y);
        }
	}
	
	void startLinkSliding(final long press_vx, final long press_vy, int px, int py){
		mainView.getCursor().setVisibility(false);
		isLinkSliding = true;
		screen_cursor_x = px + panelWidth/2;
		screen_cursor_y = py + panelHeight/2;
		mainView.getPanel().setNoEventCoordinates(panelWidth/2, panelHeight/2);
		try {
			awtRobot = new Robot();
		}
		catch (java.awt.AWTException e){ 
			e.printStackTrace();
		}
		// chosen link
		slidingLinkActualColor = slidingLink.getColor();
		slidingLink.setColor(ConfigManager.HIGHLIGHT_COLOR);
		// add cursor on link
		slideCursor = new CircleNR(press_vx, press_vy, 0, SLIDER_CURSOR_SIZE, SLIDER_CURSOR_FILL, ConfigManager.HIGHLIGHT_COLOR);
		slideCursor.setStrokeWidth(SLIDER_CURSOR_SIZE/2.0f);
		mSpace.addGlyph(slideCursor);
		// display selection radius, circular zone that allows for arc switching when starting from a node
		if (closestNode != null){
    		selectionRadius = new CircleNR(closestNode.getShape().vx, closestNode.getShape().vy, 0, SELECTION_RADIUS, Color.WHITE, SELECTION_RADIUS_COLOR);
    		selectionRadius.setFilled(false);
    		selectionRadius.setStrokeWidth(2.0f);
    		mSpace.addGlyph(selectionRadius);	    
		}
		// center camera on selection
	    Animation a = animator.getAnimationFactory().createCameraTranslation(200, mainCamera, new LongPoint(press_vx, press_vy), false,
	                                                                                          SlowInSlowOutInterpolator.getInstance(),
	                                                                                          new EndAction(){public void execute(Object subject, Animation.Dimension dimension){linkSlider(press_vx, press_vy, true);}});
	    animator.startAnimation(a, false);
	}
	
	void linkSlider(long vx, long vy, boolean centerCursor){
        boolean withinSelectionRadius = mainView.getCursor().isUnderCursor(selectionRadius);
		mPos.setLocation(vx, vy);
		lscs[lsci].updateMousePosition(mPos);
		if (!withinSelectionRadius || centerCursor){
		    // constrained sliding on link
		    awtRobot.mouseMove(screen_cursor_x, screen_cursor_y);
		}
        else {
            // relatively free movements in link selection area around nodes
            mtPos.setLocation(vx, vy);
            int newlsci = lsci;
            Point2D tPos = lscs[lsci].getPositionAlongPath();
            double shortestDistance = Math.sqrt(Math.pow(tPos.getX()-mtPos.getX(),2) + Math.pow(cPos.getY()-mtPos.getY(),2));
            double distance;
            for (int i=0;i<lsci;i++){
                lscs[i].updateMousePosition(mtPos);
                tPos = lscs[i].getPositionAlongPath();
                distance = Math.sqrt(Math.pow(tPos.getX()-mtPos.getX(),2) + Math.pow(cPos.getY()-mtPos.getY(),2));
                if (distance < shortestDistance){
                    shortestDistance = distance;
                    newlsci = i;
                }
            }
            for (int i=lsci+1;i<lscs.length;i++){
                lscs[i].updateMousePosition(mtPos);
                tPos = lscs[i].getPositionAlongPath();
                distance = Math.sqrt(Math.pow(tPos.getX()-mtPos.getX(),2) + Math.pow(cPos.getY()-mtPos.getY(),2));
                if (distance < shortestDistance){
                    shortestDistance = distance;
                    newlsci = i;
                }
            }
            if (newlsci != lsci){
                slidingLink.setColor(slidingLinkActualColor);                
                lsci = newlsci;
                slidingLink = lscs[lsci].getPath();
                slidingLinkActualColor = slidingLink.getColor();
        		slidingLink.setColor(ConfigManager.HIGHLIGHT_COLOR);
            }
        }
		cPos = lscs[lsci].getPositionAlongPath();
		slideCursor.moveTo(Math.round(cPos.getX()), Math.round(cPos.getY()));
		mainCamera.moveTo(Math.round(cPos.getX()), Math.round(cPos.getY()));
		//mainCamera.setAltitude((float)(Camera.DEFAULT_FOCAL/lsc.getScale() - Camera.DEFAULT_FOCAL));
	}
	
	void endLinkSliding(){
		mainView.getPanel().setNoEventCoordinates(ViewPanel.NO_COORDS, ViewPanel.NO_COORDS);
        mainView.getCursor().setVisibility(true);
		mSpace.removeGlyph(slideCursor);
		if (selectionRadius != null){
		    mSpace.removeGlyph(selectionRadius);
		    selectionRadius = null;
		}
		slidingLink.setColor(slidingLinkActualColor);
		slidingLink = null;
		closestNode = null;
		isLinkSliding = false;
		lscs = null;
		lsci = -1;
		awtRobot = null;
	}
	
}

class ZGRGlassPane extends JComponent {
    
    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);    
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);
    
    String msg = Messages.EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;
    
    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;
    
    GraphicsManager grMngr;
    
    ZGRGlassPane(GraphicsManager grMngr){
        super();
        this.grMngr = grMngr;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }
    
    void setProgress(int c){
        completion = c;
        prX = grMngr.panelWidth/2-BAR_WIDTH/2;
        prY = grMngr.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
    void setMessage(String m){
        msg = m;
        msgX = grMngr.panelWidth/2-BAR_WIDTH/2;
        msgY = grMngr.panelHeight/2-BAR_HEIGHT/2 - 10;
        repaint(msgX, msgY-50, 200, 70);
    }
    
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (msg != Messages.EMPTY_STRING){
            g2.setColor(MSG_COLOR);
            g2.setFont(ConfigManager.defaultFont);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class RingManager {
	
	Ring[] rings = new Ring[0];
	
	Ring getRing(double direction, double size, double ringStep){
		// normalize direction in [0,2Pi[
		if (direction < 0){direction = 2 * Math.PI + direction;}
		// look for a ring where the new object could be placed, starting with the innermost one
		for (int i=0;i<rings.length;i++){
			double a = Math.abs(Math.atan2(size, rings[i].rank * ringStep));
			if (!rings[i].intersectsConeOfInfluence(direction-a, direction+a)){
				rings[i].addNode(direction-a, direction+a);
				return rings[i];
			}
		}
		// if couldn't find any room, create a new ring
		Ring r = createNewRing();
		double a = Math.abs(Math.atan2(size, ringStep));
		r.addNode(direction-a, direction+a);
		return r;
	}
	
	private Ring createNewRing(){
		Ring[] tr = new Ring[rings.length+1];
		System.arraycopy(rings, 0, tr, 0, rings.length);
		tr[rings.length] = new Ring(tr.length);
		rings = tr;
		return rings[rings.length-1];
	}
	
}

class Ring {

	/* rank of this ring (starts at 1) */
	int rank;
  /* cones of influence, for each item, first element is the smallest angle in [0, 2Pi[, second the largest angle in [0, 2Pi[ */
	double[][] cones = new double[0][2];
	
	Ring(int r){
		this.rank = r;
	}
	
	void addNode(double a1, double a2){
		// compute its cone of influence
		double[][] tc = new double[cones.length+1][2];
		System.arraycopy(cones, 0, tc, 0, cones.length);
		// normalize angles in [0,2Pi[
		if (a1 < 0){a1 = 2 * Math.PI + a1;}
		if (a2 < 0){a2 = 2 * Math.PI + a2;}
		tc[cones.length][0] = Math.min(a1, a2);
		tc[cones.length][1] = Math.max(a1, a2);
		cones = tc;
	}
	
	boolean intersectsConeOfInfluence(double a1, double a2){
		for (int i=0;i<cones.length;i++){
			if (a2 > cones[i][0] && a1 < cones[i][1]){return true;}
		}
		return false;
	}
	
}

class DistanceComparator implements java.util.Comparator {

	LNode centerNode;
	Glyph centerShape;

	DistanceComparator(LNode cn){
		this.centerNode = cn;
		this.centerShape = cn.getShape();
	}
    
	public int compare(Object o1, Object o2){
		Glyph n1 = ((LEdge)o1).getOtherEnd(centerNode).getShape();
		Glyph n2 = ((LEdge)o2).getOtherEnd(centerNode).getShape();
		double d1 = Math.pow(centerShape.vx-n1.vx, 2) + Math.pow(centerShape.vy-n1.vy, 2);
		double d2 = Math.pow(centerShape.vx-n2.vx, 2) + Math.pow(centerShape.vy-n2.vy, 2);
		if (d1 < d2){
			return -1;
		}
		else if (d1 > d2){
			return 1;
		}
		else {
			return 0;
		}
	}
        
}
