/*   FILE: BehaviorEval.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

public class BehaviorEval implements TOWApplication, RepaintListener {

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    static final String MAP_PATH = "images/world/1000_1600x1200.png";

    /* ZVTM components */
    static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Behavior Evaluation";
    Camera mCamera, oCamera;

    BehaviorEventHandler eh;

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

//     /* target to acquire */
//     static final Color TARGET_COLOR = Color.BLUE;
//     static final Color INSIDE_TARGET_COLOR = Color.WHITE;
//     /* targets are always in NW, NE, SW or SE directions
//        distance computed is the projected distance on X and Y axes
//        for convenience (hence the sqrt(2) division) */
//     static final long TARGET_DISTANCE = Math.round(3000 / Math.sqrt(2));
//     static final long TARGET_DEFAULT_SIZE = 100;
//     static final int TARGET_MIN_PROJ_SIZE = 4;
//     ZCircle target;
    VImage map;

    float TOWtranslucencyA, TOWtranslucencyB;
    String TOWtranslucencyAstr, TOWtranslucencyBstr;

    static final short BACKGROUND_WORLDMAP = 0;
    static final short BACKGROUND_GRAPH = 1;
    short backgroundType = BACKGROUND_WORLDMAP;

    /* logs */
    BehaviorLogManager blm;

    public BehaviorEval(short bt, String bv){
	backgroundType = bt;
	initGUI();
	eh = new BehaviorEventHandler(this);
	mView.setEventHandler(eh);
	blm = new BehaviorLogManager(this);
	initBehavior(bv);
	initWorld();
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(0);
	centerOverview(false);
	updateOverview();
	vsm.repaintNow(mView, this);
    }

    public void viewRepainted(View v){
	blm.im.say(AcquireLogManager.PSTS);
	v.removeRepaintListener();
    }

    void initBehavior(String t){
	if (t.equals(BehaviorBlock.BEHAVIOR_FT_STR)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_FT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_FT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_FT_STR;
	}
	else if (t.equals(BehaviorBlock.BEHAVIOR_IT_STR)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_IT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_IT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_IT_STR;
	}
	else if (t.equals(BehaviorBlock.BEHAVIOR_DT_STR)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_DT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_DT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_DT_STR;
	}
	TOWtranslucencyAstr = String.valueOf(TOWtranslucencyA);
	TOWtranslucencyBstr = String.valueOf(TOWtranslucencyB);
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
	mView.setBackgroundColor(BehaviorEval.BACKGROUND_COLOR);
	mView.setAntialiasing(false);
	updatePanelSize();
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
	VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }


    void initWorld(){
	if (backgroundType == BACKGROUND_WORLDMAP){
	    map = new VImage(0, 0, 0, (new ImageIcon(MAP_PATH)).getImage());
	    map.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(map, mSpace);
	}
	else {
	    //XXX: TBW graph
	}
    }

    void centerOverview(boolean animate){
	if (animate){
	    vsm.animator.createCameraAnimation(OVERVIEW_CENTERING_TRANSLATE_TIME, AnimManager.CA_TRANS_SIG,
					       new LongPoint(mCamera.posx-oCamera.posx, mCamera.posy-oCamera.posy),
					       oCamera.getID(), blm);
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
	    killPortal();
	}
	else {// portal not active, create it
	    to = getPortal(x, y);
	    to.setBackgroundColor(BACKGROUND_COLOR);
	    to.setPortalEventHandler((PortalEventHandler)eh);
	    vsm.addPortal(to, mView);
 	    to.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
	    oCamera.moveTo(0, 0);
	    centerOverview(false);
	}
    }

    TrailingOverview getPortal(int x, int y){
	TrailingOverview res = new TrailingOverview(x-TOW_CONTRACTED_WIDTH/2, y-TOW_CONTRACTED_HEIGHT/2,
						    TOW_CONTRACTED_WIDTH, TOW_CONTRACTED_HEIGHT,
						    oCamera, mCamera, 0.0f, TOW_PORTAL_X_OFFSET, TOW_PORTAL_Y_OFFSET);
	res.setTranslucencyParameters(TOWtranslucencyA, TOWtranslucencyB);
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
	BehaviorInstructionsManager.START_BUTTON_TL_X = panelWidth/2 - BehaviorInstructionsManager.START_BUTTON_W / 2;
	BehaviorInstructionsManager.START_BUTTON_TL_Y = panelHeight/2 + BehaviorInstructionsManager.START_BUTTON_H / 2;
	BehaviorInstructionsManager.START_BUTTON_BR_X = BehaviorInstructionsManager.START_BUTTON_TL_X + BehaviorInstructionsManager.START_BUTTON_W;
	BehaviorInstructionsManager.START_BUTTON_BR_Y = BehaviorInstructionsManager.START_BUTTON_TL_Y + BehaviorInstructionsManager.START_BUTTON_H;
	System.err.println(BehaviorInstructionsManager.START_BUTTON_TL_X+" "+BehaviorInstructionsManager.START_BUTTON_TL_Y+" "+BehaviorInstructionsManager.START_BUTTON_BR_X+" "+BehaviorInstructionsManager.START_BUTTON_BR_Y);
    }

    void exit(){
	if (to != null){to.dispose();}
	System.exit(0);
    }
    
    public static void main(String[] args){
	try {
	    String t = BehaviorBlock.BEHAVIOR_FT_STR;
	    if (args.length >= 2){
		t = args[1];
		if (args.length >= 4){
		    BehaviorEval.VIEW_MAX_W = Integer.parseInt(args[2]);
		    BehaviorEval.VIEW_MAX_H = Integer.parseInt(args[3]);
		}
	    }
	    new BehaviorEval(Short.parseShort(args[0]), t);
	}
	catch (Exception ex){
	    System.err.println("Usage:\n\tjava -cp [...] net.claribole.eval.to.BehaviorEval <backgroundType> <FT>|<IT>|<DT> [<window width> <window height>]");
	    System.exit(0);
	}
    }

}
