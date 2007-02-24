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

    static final String REGION_MAP_PATH = "images/world/europe_1600x1200.png";
    static final String WHOLE_MAP_PATH = "images/world/0000_400x200.png";

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
    static final Color DEFAULT_PORTAL_BORDER_COLOR = Color.WHITE;
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

    float TOWtranslucencyA, TOWtranslucencyB;
    String TOWtranslucencyAstr, TOWtranslucencyBstr;

    static final short BACKGROUND_WORLDMAP = 0;
    static final short BACKGROUND_GRAPH = 1;
    short backgroundType = BACKGROUND_WORLDMAP;

    /* logs */
    BehaviorLogManager blm;

    public BehaviorEval(short bt, String bv){
	initGUI();
	eh = new BehaviorEventHandler(this);
	mView.setEventHandler(eh);
	blm = new BehaviorLogManager(this);
	initBehavior(bv);
	initWorld(bt);
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(0);
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

    static final long WM_ORIG_X = -310;
    static final long WM_ORIG_Y = -2123;

    Glyph NW_TARGET, NE_TARGET, SE_TARGET, SW_TARGET;
    static final Color TARGET_COLOR = Color.RED;
    static final Color DISTRACTOR_COLOR = Color.YELLOW;

    void initWorld(short bt){
	backgroundType = bt;
	if (backgroundType == BACKGROUND_WORLDMAP){
	    VImage im = new VImage(WM_ORIG_X, WM_ORIG_Y, 0, (new ImageIcon(WHOLE_MAP_PATH)).getImage(), 40);
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    im = new VImage(0, 0, 0, (new ImageIcon(REGION_MAP_PATH)).getImage());
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    NW_TARGET = new VRectangle(-135,120,0,5,5,DISTRACTOR_COLOR);
	    NE_TARGET = new VRectangle(95,120,0,5,5,DISTRACTOR_COLOR);
	    SE_TARGET = new VRectangle(95,-120,0,5,5,DISTRACTOR_COLOR);
	    SW_TARGET = new VRectangle(-135,-120,0,5,5,DISTRACTOR_COLOR);
	    vsm.addGlyph(NW_TARGET, mSpace);
	    vsm.addGlyph(NE_TARGET, mSpace);
	    vsm.addGlyph(SE_TARGET, mSpace);
	    vsm.addGlyph(SW_TARGET, mSpace);
// 	    vsm.addGlyph(new VSegment(-20,0,0,500,(float)(45*2*Math.PI/360.0), Color.RED), mSpace);
// 	    vsm.addGlyph(new VSegment(-20,0,0,500,(float)(135*2*Math.PI/360.0), Color.RED), mSpace);
	}
	else {// backgroundType == BACKGROUND_GRAPH
	    //XXX: TBW graph
	}
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
	    oCamera.moveTo(WM_ORIG_X, WM_ORIG_Y);
	    oCamera.setAltitude(5200);
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

    static final int C_OFFSET_X = -20;
    static final int C_OFFSET_Y = -20;

    void updatePanelSize(){
	Dimension d = mView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
	BehaviorInstructionsManager.START_BUTTON_TL_X = panelWidth/2 - BehaviorInstructionsManager.START_BUTTON_W / 2 + C_OFFSET_X;
	BehaviorInstructionsManager.START_BUTTON_TL_Y = panelHeight/2 + BehaviorInstructionsManager.START_BUTTON_H / 2 + C_OFFSET_Y;
	BehaviorInstructionsManager.START_BUTTON_BR_X = BehaviorInstructionsManager.START_BUTTON_TL_X + BehaviorInstructionsManager.START_BUTTON_W;
	BehaviorInstructionsManager.START_BUTTON_BR_Y = BehaviorInstructionsManager.START_BUTTON_TL_Y + BehaviorInstructionsManager.START_BUTTON_H;
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
