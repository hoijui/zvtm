/*   FILE: TOWApplication.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import net.claribole.zvtm.engine.OverviewPortal;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.RepaintListener;
import net.claribole.zvtm.engine.TrailingOverview;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import net.claribole.eval.glyphs.ZCircle;

public class AcquireEval implements TOWApplication, RepaintListener {

    /* techniques */
    static final short TECHNIQUE_OV = 0;
    static final String TECHNIQUE_OV_NAME = "OD"; 
    static final short TECHNIQUE_TOW = 1;
    static final String TECHNIQUE_TOW_NAME = "TO";
    short technique = TECHNIQUE_TOW;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    /* ZVTM components */
    static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera, oCamera;

    AcquireBaseEventHandler eh;

    /* generic overview settings */
    static int OVERVIEW_WIDTH = 150;
    static int OVERVIEW_HEIGHT = 150;
    static final Color DEFAULT_PORTAL_BORDER_COLOR = Color.BLACK;
    static final Color INSIDE_PORTAL_BORDER_COLOR = Color.RED;
    static final float OVERVIEW_CAMERA_ALTITUDE_FACTOR = 36.0f;
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

    /* target to acquire */
    static final Color TARGET_COLOR = Color.BLUE;
    static final Color INSIDE_TARGET_COLOR = Color.WHITE;
    /* targets are always in NW, NE, SW or SE directions
       distance computed is the projected distance on X and Y axes
       for convenience (hence the sqrt(2) division) */
    static final long TARGET_DISTANCE = Math.round(3000 / Math.sqrt(2));
    static final long TARGET_DEFAULT_SIZE = 100;
    static final int TARGET_MIN_PROJ_SIZE = 4;
    ZCircle target;

    static final Color SELECTION_REGION_COLOR = Color.BLACK;
    static final float SELECTION_REGION_SIZE_FACTOR = 2.0f;

    /* overview altitudes */
    static float FULL_SIZE_OVERVIEW_ALTITUDE = TARGET_DISTANCE * 2.5f * Camera.DEFAULT_FOCAL / (OVERVIEW_WIDTH) - Camera.DEFAULT_FOCAL;
    static float CONTRACTED_OVERVIEW_ALTITUDE = TARGET_DISTANCE * 2.5f * Camera.DEFAULT_FOCAL / (TOW_CONTRACTED_WIDTH) - Camera.DEFAULT_FOCAL;

    float TOWtranslucencyA, TOWtranslucencyB;

    /* logs */
    AcquireLogManager alm;

    public AcquireEval(short t, float ta, float tb){
	TOWtranslucencyA = ta;
	TOWtranslucencyB = tb;
	initGUI();
	if (t == TECHNIQUE_OV){
	    this.technique = TECHNIQUE_OV;
	    mViewName = TECHNIQUE_OV_NAME;
	    eh = new AcquireOVEventHandler(this);
	    initOverview();
	}
	else {
	    this.technique = TECHNIQUE_TOW;
	    mViewName = TECHNIQUE_TOW_NAME;
	    eh = new AcquireTOWEventHandler(this);
	}
	mView.setEventHandler(eh);
	alm = new AcquireLogManager(this);
	initWorld();
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(0);
	centerOverview(false);
 	initOverviewAltitudes();
	vsm.repaintNow(mView, this);
    }

    public void viewRepainted(View v){
	alm.im.say(AcquireLogManager.PSTS);
	v.removeRepaintListener();
    }

    void initGUI(){
	windowLayout();
	vsm = new VirtualSpaceManager();
	mSpace = vsm.addVirtualSpace(mSpaceName);
	mCamera = vsm.addCamera(mSpaceName);
	oCamera = vsm.addCamera(mSpaceName);
	Vector v = new Vector();
	v.add(mCamera);
	mView = vsm.addExternalView(v, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
	mView.getPanel().addComponentListener(eh);
	mView.setNotifyMouseMoved(true);
	mView.setBackgroundColor(AcquireEval.BACKGROUND_COLOR);
	mView.setAntialiasing(true);
	updatePanelSize();
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
// 	    SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void initOverview(){
	op = new OverviewPortal(0,
				panelHeight-OVERVIEW_HEIGHT-1,
				OVERVIEW_WIDTH, OVERVIEW_HEIGHT, oCamera, mCamera);
	op.setPortalEventHandler((PortalEventHandler)eh);
	op.setBackgroundColor(AcquireEval.BACKGROUND_COLOR);
	vsm.addPortal(op, mView);
	op.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
	op.setObservedRegionTranslucency(0.5f);
// 	op.setObservedRegionListener((ObservedRegionListener)eh);
    }

    void initWorld(){
	target = new ZCircle(0, 0, 0, TARGET_DEFAULT_SIZE, TARGET_COLOR);
	target.setMinimumProjectedSize(TARGET_MIN_PROJ_SIZE);
	vsm.addGlyph(target, mSpace);
	target.setMouseInsideHighlightColor(INSIDE_TARGET_COLOR);
    }

    void centerOverview(boolean animate){
	if (animate){
	    vsm.animator.createCameraAnimation(OVERVIEW_CENTERING_TRANSLATE_TIME, AnimManager.CA_TRANS_SIG,
					       new LongPoint(mCamera.posx-oCamera.posx, mCamera.posy-oCamera.posy),
					       oCamera.getID(), alm);
	}
	else {
	    oCamera.moveTo(mCamera.posx, mCamera.posy);
	}
    }

    void initOverviewAltitudes(){// set overview camera's altitude (contracted and expanded states)
	oCamera.setAltitude((this.technique == TECHNIQUE_OV) ? FULL_SIZE_OVERVIEW_ALTITUDE : CONTRACTED_OVERVIEW_ALTITUDE);
    }

    void switchPortal(int x, int y){
	if (to != null){// portal is active, destroy it it
// 	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(-0.5f),
// 					       to.getID(), new PortalKiller(this));
	    killPortal();
	}
	else {// portal not active, create it
	    to = getPortal(x, y);
	    to.setBackgroundColor(BACKGROUND_COLOR);
	    to.setPortalEventHandler((PortalEventHandler)eh);
 	    //to.setObservedRegionListener((ObservedRegionListener)eh);
	    vsm.addPortal(to, mView);
 	    to.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
// 	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(0.5f),
// 					       to.getID(), null);
	    oCamera.moveTo(0, 0);
// 	    updateOverview();
	    centerOverview(false);
	}
    }

    TrailingOverview getPortal(int x, int y){
	TrailingOverview res = new TrailingOverview(x-TOW_CONTRACTED_WIDTH/2, y-TOW_CONTRACTED_HEIGHT/2,
						    TOW_CONTRACTED_WIDTH, TOW_CONTRACTED_HEIGHT,
						    oCamera, mCamera, 0.0f, TOW_PORTAL_X_OFFSET, TOW_PORTAL_Y_OFFSET);
	res.setCutoffFrequencyParameters(0.5,0.25);
	res.setTranslucencyParameters(0, 0.3f);
	return res;
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
	updateStartButton((short)-1);
    }

    void updateStartButton(short sl){
	AcquireInstructionsManager.START_BUTTON_TL_X = panelWidth/2 - AcquireInstructionsManager.START_BUTTON_W / 2;
	AcquireInstructionsManager.START_BUTTON_TL_Y = panelHeight/2 + AcquireInstructionsManager.START_BUTTON_H / 2;
	AcquireInstructionsManager.START_BUTTON_BR_X = AcquireInstructionsManager.START_BUTTON_TL_X + AcquireInstructionsManager.START_BUTTON_W;
	AcquireInstructionsManager.START_BUTTON_BR_Y = AcquireInstructionsManager.START_BUTTON_TL_Y + AcquireInstructionsManager.START_BUTTON_H;
	switch(sl){
	case AcquireBlock.START_LOC_TL:{
	    AcquireInstructionsManager.START_BUTTON_TL_X -= panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_TL_Y -= panelHeight / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_X -= panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_Y -= panelHeight / 4;
	    break;
	}
	case AcquireBlock.START_LOC_TR:{
	    AcquireInstructionsManager.START_BUTTON_TL_X += panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_TL_Y -= panelHeight / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_X += panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_Y -= panelHeight / 4;
	    break;
	}
	case AcquireBlock.START_LOC_BL:{
	    AcquireInstructionsManager.START_BUTTON_TL_X -= panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_TL_Y += panelHeight / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_X -= panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_Y += panelHeight / 4;
	    break;
	}
	case AcquireBlock.START_LOC_BR:{
	    AcquireInstructionsManager.START_BUTTON_TL_X += panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_TL_Y += panelHeight / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_X += panelWidth / 4;
	    AcquireInstructionsManager.START_BUTTON_BR_Y += panelHeight / 4;
	    break;
	}
	default:{break;}
	}
    }

    void exit(){
	if (to != null){to.dispose();}
	if (op != null){op.dispose();}
	System.exit(0);
    }
    
    public static void main(String[] args){
	try {
	    float a = -0.5f;
	    float b = 0.5f;
	    if (args.length >= 3){
		AcquireEval.VIEW_MAX_W = Integer.parseInt(args[1]);
		AcquireEval.VIEW_MAX_H = Integer.parseInt(args[2]);
	    }
	    new AcquireEval(Short.parseShort(args[0]), a, b);
	}
	catch (Exception ex){
	    ex.printStackTrace();
	    System.err.println("Usage:\n\tjava -cp [...] net.claribole.eval.to.AcquireEval <technique> [<window width> <window height>]");
	    System.exit(0);
	}
    }

}
