/*   FILE: PortalWorld.java
 *   DATE OF CREATION:  Sat Jun 17 13:58:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.Vector;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.engine.TrailingOverview;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

public class PortalWorldDemo {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* max dimensions of ZVTM view */
    static final int VIEW_MAX_W = 800;
    static final int VIEW_MAX_H = 600;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    /* Main World map */
    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;
    static final long MAP_WIDTH = Math.round(MAIN_MAP_WIDTH * MapData.MN000factor.doubleValue());
    static final long MAP_HEIGHT = Math.round(MAIN_MAP_HEIGHT * MapData.MN000factor.doubleValue());
    static final long HALF_MAP_WIDTH = Math.round(MAP_WIDTH/2.0);
    static final long HALF_MAP_HEIGHT = Math.round(MAP_HEIGHT/2.0);

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    PWEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    /*translation constants*/
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;

    /*animation timing*/
    static int ANIM_MOVE_LENGTH = 500;

    /* Portal */
    static int PORTAL_WIDTH = 50;
    static int PORTAL_HEIGHT = 25;
    static int PORTAL_WIDTH_EXPANSION_OFFSET = 150;
    static int PORTAL_HEIGHT_EXPANSION_OFFSET = 75;
    static final int PORTAL_X_OFFSET = -120;
    static final int PORTAL_Y_OFFSET = 120;
    TrailingOverview portal;
    Camera portalCamera;
    static  float PORTAL_CEILING_ALTITUDE;
    static  float CONTRACTED_PORTAL_CEILING_ALTITUDE;

    boolean dynamicOverview = true;

    static final short DO_NOT_ADAPT_MAPS = 0;
    static final short ADAPT_MAPS = 1;
    MapManager mm;

    PortalWorldDemo(short am){
	    vsm = VirtualSpaceManager.INSTANCE;
 	vsm.setDebug(true);
	init(am);
    }

    public void init(short am){
	eh = new PWEventHandler(this);
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	demoCamera = vsm.addCamera(mainVSname);
	demoCamera.setZoomFloor(0);
	demoCamera.addListener(eh);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, "Portal World Demo", View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	demoView.setEventHandler(eh);
	demoView.setNotifyMouseMoved(true);
	portalCamera = vsm.addCamera(mainVSname);
	mm = new MapManager(vsm, mainVS, demoCamera, demoView);
	if (am == ADAPT_MAPS){
	    mm.initMap(null, null);
	}
	else {
	    mm.initMap("images/world/0000.png", new Double(32.0));
	    mm.switchAdaptMaps(); // true by default, make it false
	}
	PORTAL_CEILING_ALTITUDE = mm.mainMap.getHeight() * 2 * Camera.DEFAULT_FOCAL / (PORTAL_HEIGHT + PORTAL_HEIGHT_EXPANSION_OFFSET) - Camera.DEFAULT_FOCAL;
	CONTRACTED_PORTAL_CEILING_ALTITUDE = mm.mainMap.getHeight() * 2 * Camera.DEFAULT_FOCAL / (PORTAL_HEIGHT) - Camera.DEFAULT_FOCAL;
	getGlobalView(false);
	System.gc();
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

    void altitudeChanged(){
	// update  map level, visible maps and grid level as it
	// was prevented between zoom{int,out} phases 1 and 2
	mm.updateMapLevel(demoCamera.getAltitude());
    }


    TrailingOverview getPortal(int x, int y){
	TrailingOverview res =  new TrailingOverview(x-PORTAL_WIDTH/2, y-PORTAL_HEIGHT/2, PORTAL_WIDTH, PORTAL_HEIGHT,
						     portalCamera, demoCamera, 0.0f, PORTAL_X_OFFSET, PORTAL_Y_OFFSET);
	res.setTranslucencyParameters(0, 0.3f);
	return res;
    }

    void killPortal(){
	vsm.destroyPortal(portal);
	portal.dispose();
	portal = null;
	vsm.repaintNow();
    }

    void switchPortal(int x, int y){
	if (portal != null){// portal is active, destroy it it
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createTranslucencyAnim(ANIM_MOVE_LENGTH, portal, -0.5f, true,
				    IdentityInterpolator.getInstance(), 
				    new EndAction(){
					public void execute(Object subject,
							    Animation.Dimension dimension){
					    killPortal();
					}
				    });
	    vsm.getAnimationManager().startAnimation(anim, true);
	}
	else {// portal not active, create it
	    portal = getPortal(x, y);
	    portal.setBackgroundColor(Color.LIGHT_GRAY);
	    portal.setPortalEventHandler(eh);
	    portal.setObservedRegionListener(eh);
	    vsm.addPortal(portal, demoView);
 	    portal.setBorder(Color.RED);
	    
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createTranslucencyAnim(ANIM_MOVE_LENGTH, portal, 0.5f, true,
					IdentityInterpolator.getInstance(), null);
	    vsm.getAnimationManager().startAnimation(anim, true);

	    portalCamera.moveTo(0, 0);
	    portalCamera.setAltitude(CONTRACTED_PORTAL_CEILING_ALTITUDE);
	}
    }

    void getTo(int jpx, int jpy){
	LongPoint res = portal.getVSCoordinates(jpx, jpy);
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(200, demoCamera, new LongPoint(res.x, res.y), false,
				    SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    void getGlobalView(boolean inPortal){
	if (inPortal){
	    portal.getGlobalView(ANIM_MOVE_LENGTH, vsm);
	}
	else {
	    vsm.getGlobalView(demoCamera, ANIM_MOVE_LENGTH);
	}
    }

    /*higher view*/
    void getFastHigherView(){
	demoCamera.altitudeOffset((demoCamera.getAltitude()+demoCamera.getFocal())/2.0f);
	vsm.repaintNow();
    }

    /*lower view*/
    void getFastLowerView(){
	demoCamera.altitudeOffset(-(demoCamera.getAltitude()+demoCamera.getFocal())/4.0f);
	vsm.repaintNow();
    }

    /*higher view*/
    void getHigherView(boolean inPortal){
	Camera c;
	if (inPortal){
	    c = portalCamera;
	}
	else {
	    c = demoCamera;
	}
	float alt=c.getAltitude()+c.getFocal();
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt, true,
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    /*lower view*/
    void getLowerView(boolean inPortal){
	Camera c;
	if (inPortal){
	    c = portalCamera;
	}
	else {
	    c = demoCamera;
	}
	float alt=-(c.getAltitude()+c.getFocal())/2.0f;
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt, true,
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    /*direction should be one of ZGRViewer.MOVE_* */
    void translateView(short direction, boolean inPortal){
	Camera c;
	LongPoint trans;
	long[] rb;
	if (inPortal){
	    c = portalCamera;
	    rb = portal.getVisibleRegion();
	}
	else {
	    c = demoCamera;
	    rb = demoView.getVisibleRegion(c);
	}
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
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ANIM_MOVE_LENGTH, c, trans, true,
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    public static void main(String[] args){
	short am = (args.length > 0) ? Short.parseShort(args[0]) : DO_NOT_ADAPT_MAPS;
 	new PortalWorldDemo(am);
    }

}
