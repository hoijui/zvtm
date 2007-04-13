/*   FILE: Eval.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2007. All Rights Reserved
 *
 * $Id$
 */ 

package net.claribole.eval.to;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

public class Eval implements TOWApplication {

    /* techniques */
    static final short TECHNIQUE_OV = 0;
    static final String TECHNIQUE_OV_NAME = "Overview + Detail"; 
    static final short TECHNIQUE_TOW = 1;
    static final String TECHNIQUE_TOW_NAME = "Trailing Overview";
    static final short TECHNIQUE_TOWINV = 2;
    static final String TECHNIQUE_TOWINV_NAME = "Trailing Overview Inv";
    static final short TECHNIQUE_TOWINVNOEXP = 3;
    static final String TECHNIQUE_TOWINVNOEXP_NAME = "Trailing Overview Inv No Exp";
    short technique = TECHNIQUE_TOW;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1280;
    static int VIEW_MAX_H = 1024;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    /* ZVTM components */
    static final Color BACKGROUND_COLOR = Color.WHITE;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera, oCamera;

    BaseEventHandler eh;

    /* generic overview settings */
    static int OVERVIEW_WIDTH = 150;
    static int OVERVIEW_HEIGHT = 150;
    static final Color DEFAULT_PORTAL_BORDER_COLOR = Color.BLACK;
    static final Color INSIDE_PORTAL_BORDER_COLOR = Color.RED;
    static final float OVERVIEW_CAMERA_ALTITUDE_FACTOR = 48.0f;
    static final int OVERVIEW_CENTERING_TRANSLATE_TIME = 300;
    
    /* trailing overview settings */
    static final int TOW_SWITCH_ANIM_TIME = 500;
    static final int TOW_CONTRACTED_WIDTH = 50;
    static final int TOW_CONTRACTED_HEIGHT = 50;
    static final int TOW_HORIZONTAL_EXPANSION_OFFSET = OVERVIEW_WIDTH - TOW_CONTRACTED_WIDTH;
    static final int TOW_VERTICAL_EXPANSION_OFFSET = OVERVIEW_HEIGHT - TOW_CONTRACTED_HEIGHT;
    static final int TOW_PORTAL_X_OFFSET = -120;
    static final int TOW_PORTAL_Y_OFFSET = 120;
    TrailingOverview to;

    /* standard overview settings */
    OverviewPortal op;
    
    public Eval(short t){
	initGUI();
	if (t == TECHNIQUE_TOW){
	    this.technique = TECHNIQUE_TOW;
	    mViewName = TECHNIQUE_TOW_NAME;
	    eh = new TOWEventHandler(this);
	}
	else if (t == TECHNIQUE_TOWINV){
	    this.technique = TECHNIQUE_TOWINV;
	    mViewName = TECHNIQUE_TOWINV_NAME;
	    eh = new TOWEventHandler(this);
	}
	else if (t == TECHNIQUE_TOWINVNOEXP){
	    this.technique = TECHNIQUE_TOWINVNOEXP;
	    mViewName = TECHNIQUE_TOWINVNOEXP_NAME;
	    eh = new TOWInvNoExpEventHandler(this);
	}
	else {
	    this.technique = TECHNIQUE_OV;
	    mViewName = TECHNIQUE_OV_NAME;
	    eh = new OVEventHandler(this);
	    initOverview();
	}
	mView.setEventHandler(eh);
	initWorld();
	Location l = vsm.getGlobalView(mCamera);
	mCamera.moveTo(l.vx, l.vy);
	mCamera.setAltitude(l.alt);
	centerOverview(false);
	updateOverview();
    }

    void initGUI(){
	windowLayout();
	vsm = new VirtualSpaceManager();
	mSpace = vsm.addVirtualSpace(mSpaceName);
	mCamera = vsm.addCamera(mSpaceName);
	oCamera = vsm.addCamera(mSpaceName);
	Vector v = new Vector();
	v.add(mCamera);
	mView = vsm.addExternalView(v, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, true);
	mView.getPanel().addComponentListener(eh);
	mView.setNotifyMouseMoved(true);
	mView.setBackgroundColor(Eval.BACKGROUND_COLOR);
	updatePanelSize();
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

    void initOverview(){
	op = new OverviewPortal(panelWidth-OVERVIEW_WIDTH-1,
				panelHeight-OVERVIEW_HEIGHT-1,
				OVERVIEW_WIDTH, OVERVIEW_HEIGHT, oCamera, mCamera);
	op.setPortalEventHandler((PortalEventHandler)eh);
	op.setBackgroundColor(Eval.BACKGROUND_COLOR);
	vsm.addPortal(op, mView);
	op.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
	op.setObservedRegionTranslucency(0.5f);
	op.setObservedRegionListener((ObservedRegionListener)eh);
    }

    void initWorld(){
	long randomX = 0;
	long randomY = 0;
	long randomS = 0;
	float randomO = 0;
	float randomSat = 0;
	double shapeType = 0;
	Glyph g;
	float[] vertices = {1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f, 1.0f, 0.8f};
	for (int i=0;i<200;i++){
	    randomX = Math.round(Math.random()*6000);
	    randomY = Math.round(Math.random()*6000);
	    randomS = Math.round(Math.random()*199)+20;
	    randomO = (float)(Math.random()*2*Math.PI);
	    randomSat = (float)Math.random();
	    shapeType = Math.random();
	    if (shapeType<0.2){
		g = new VTriangleOr(randomX, randomY, 0, randomS, Color.getHSBColor(0.66f, randomSat, 0.8f), randomO);
	    }
	    else if (shapeType<0.4){
		g = new VDiamondOr(randomX, randomY, 0, randomS, Color.getHSBColor(0.66f, randomSat, 0.8f), randomO);
	    }
	    else if (shapeType<0.6){
		g = new VOctagonOr(randomX, randomY, 0, randomS, Color.getHSBColor(0.66f, randomSat, 0.8f), Color.BLACK, randomO);
	    }
	    else if (shapeType<0.8){
		g = new VRectangleOr(randomX, randomY, 0, randomS, randomS, Color.getHSBColor(0.66f, randomSat, 0.8f), randomO);
	    }
	    else {
		g = new VShape(randomX, randomY, 0, randomS, vertices, Color.getHSBColor(0.66f, randomSat, 0.8f), randomO);
	    }
	    vsm.addGlyph(g, mSpace);
	}
    }

    void centerOverview(boolean animate){
	if (animate){
	    vsm.animator.createCameraAnimation(OVERVIEW_CENTERING_TRANSLATE_TIME, AnimManager.CA_TRANS_SIG,
					       new LongPoint(mCamera.posx-oCamera.posx, mCamera.posy-oCamera.posy), oCamera.getID());
	}
	else {
	    oCamera.moveTo(mCamera.posx, mCamera.posy);
	}
    }
    
    void updateOverview(){// update overview camera's altitude
	oCamera.setAltitude((float)((mCamera.getAltitude()+mCamera.getFocal())*OVERVIEW_CAMERA_ALTITUDE_FACTOR-mCamera.getFocal()));
    }

    void switchPortal(int x, int y){
	if (to != null){// portal is active, destroy it it
	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(-0.5f),
					       to.getID(), new PortalKiller(this));
	}
	else {// portal not active, create it
	    to = getPortal(x, y, technique != TECHNIQUE_TOWINVNOEXP, technique != TECHNIQUE_TOW);
	    to.setBackgroundColor(BACKGROUND_COLOR);
	    to.setPortalEventHandler((PortalEventHandler)eh);
 	    to.setObservedRegionListener((ObservedRegionListener)eh);
	    vsm.addPortal(to, mView);
 	    to.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(0.5f),
					       to.getID(), null);
	    oCamera.moveTo(0, 0);
	    updateOverview();
	    centerOverview(false);
	}
    }

    TrailingOverview getPortal(int x, int y, boolean shrinked, boolean inv){
	if (inv){
	    if (shrinked){
		return new TrailingOverview(x-TOW_CONTRACTED_WIDTH/2, y-TOW_CONTRACTED_HEIGHT/2,
					    TOW_CONTRACTED_WIDTH, TOW_CONTRACTED_HEIGHT,
					    oCamera, mCamera, 0.0f,
					    TOW_PORTAL_X_OFFSET, TOW_PORTAL_Y_OFFSET);
	    }
	    else {
		TrailingOverview res = new TrailingOverview(x-OVERVIEW_WIDTH/2, y-OVERVIEW_HEIGHT/2,
							    OVERVIEW_WIDTH, OVERVIEW_HEIGHT,
							    oCamera, mCamera, 0.0f,
							    TOW_PORTAL_X_OFFSET,
							    TOW_PORTAL_Y_OFFSET);
		res.setTranslucencyParameters(5.0f/3.0f, -2.0f/3.0f);
		return res;
	    }
	}
	else {
	    TrailingOverview res = new TrailingOverview(x-TOW_CONTRACTED_WIDTH/2, y-TOW_CONTRACTED_HEIGHT/2,
							TOW_CONTRACTED_WIDTH, TOW_CONTRACTED_HEIGHT,
							oCamera, mCamera, 0.0f,
							TOW_PORTAL_X_OFFSET, TOW_PORTAL_Y_OFFSET);
	    res.setCutoffFrequencyParameters(0.4, 0.3);
	    return res;
	}
    }

    public void killPortal(){
	vsm.destroyPortal(to);
	to.dispose();
	to = null;
	vsm.repaintNow();
    }

    void updatePanelSize(){
	Dimension d = mView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    void exit(){
	if (to != null){to.dispose();}
	if (op != null){op.dispose();}
	System.exit(0);
    }
    
    public static void main(String[] args){
	try {
	    if (args.length >= 3){
		Eval.VIEW_MAX_W = Integer.parseInt(args[1]);
		Eval.VIEW_MAX_H = Integer.parseInt(args[2]);
	    }
	    new Eval(Short.parseShort(args[0]));
	}
	catch (Exception ex){
	    ex.printStackTrace();
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Trailing Overview");
	    new Eval(Eval.TECHNIQUE_TOW);
	}
    }

}
