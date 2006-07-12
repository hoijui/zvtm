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

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

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

    /* World map */
    static final Double MN000factor = new Double(32.0);
    static final String M1000 = "1000";
    static final String M1000path = "images/world/0000.png";
    static final long M1000x = 0;
    static final long M1000y = 0;
    VImage mainMap;
    static final int MAIN_MAP_WIDTH = 8000;
    static final int MAIN_MAP_HEIGHT = 4000;
    static final long MAP_WIDTH = Math.round(MAIN_MAP_WIDTH * MN000factor.doubleValue());
    static final long MAP_HEIGHT = Math.round(MAIN_MAP_HEIGHT * MN000factor.doubleValue());
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
    static int PORTAL_HEIGHT = 50;
    static int PORTAL_WIDTH_EXPANSION_OFFSET = 150;
    static int PORTAL_HEIGHT_EXPANSION_OFFSET = 50;
    static final int PORTAL_X_OFFSET = -120;
    static final int PORTAL_Y_OFFSET = 120;
    TrailingOverview portal;
    Camera portalCamera;
    static  float PORTAL_CEILING_ALTITUDE;

    boolean dynamicOverview = true;

    PortalWorldDemo(){
	vsm = new VirtualSpaceManager();
 	vsm.setDebug(true);
	init();
    }

    public void init(){
	eh = new PWEventHandler(this);
	vsm.animator.setAnimationListener(eh);
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, "Portal World Demo", View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	demoView.setEventHandler(eh);
	demoView.setNotifyMouseMoved(true);
	portalCamera = vsm.addCamera(mainVSname);
	initMap();
	PORTAL_CEILING_ALTITUDE = mainMap.getHeight() * 2 * Camera.DEFAULT_FOCAL / (PORTAL_HEIGHT + PORTAL_HEIGHT_EXPANSION_OFFSET) - Camera.DEFAULT_FOCAL ;
	getGlobalView(false);
	System.gc();
    }

    void initMap(){
	System.out.print("Loading World Map...");
	mainMap = new VImage(M1000x, M1000y, 0,
			     (new ImageIcon(M1000path)).getImage(),
			     MN000factor.doubleValue());
	mainMap.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(mainMap, mainVS);
	mainVS.atBottom(mainMap);
	System.out.println("OK");
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

    TrailingOverview getPortal(int x, int y){
	return new TrailingOverview(x-PORTAL_WIDTH/2, y-PORTAL_HEIGHT/2, PORTAL_WIDTH, PORTAL_HEIGHT,
				    portalCamera, demoCamera, 0.0f, PORTAL_X_OFFSET, PORTAL_Y_OFFSET);
    }

    void killPortal(){
	vsm.destroyPortal(portal);
	portal.dispose();
	portal = null;
	vsm.repaintNow();
    }

    void switchPortal(int x, int y){
	if (portal != null){// portal is active, destroy it it
	    vsm.animator.createPortalAnimation(ANIM_MOVE_LENGTH, AnimManager.PT_ALPHA_LIN, new Float(-0.5f),
					       portal.getID(), new PortalKiller(this));
	}
	else {// portal not active, create it
	    portal = getPortal(x, y);
	    portal.setBackgroundColor(Color.LIGHT_GRAY);
	    portal.setPortalEventHandler(eh);
	    portal.setObservedRegionListener(eh);
	    vsm.addPortal(portal, demoView);
 	    portal.setBorder(Color.RED);
	    vsm.animator.createPortalAnimation(ANIM_MOVE_LENGTH, AnimManager.PT_ALPHA_LIN, new Float(0.5f),
					       portal.getID(), null);
	    portalCamera.moveTo(0, 0);
	    portalCamera.setAltitude(PORTAL_CEILING_ALTITUDE);
	}
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

    /*higher view*/
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
	Float alt=new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
    }

    /*higher view*/
    void getLowerView(boolean inPortal){
	Camera c;
	if (inPortal){
	    c = portalCamera;
	}
	else {
	    c = demoCamera;
	}
	Float alt=new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_ALT_SIG,alt,c.getID());
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
	vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH,AnimManager.CA_TRANS_SIG,trans,c.getID());
    }

    public static void main(String[] args){
 	new PortalWorldDemo();
    }

}
