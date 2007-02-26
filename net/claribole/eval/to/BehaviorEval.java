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

    float TOWtranslucencyA, TOWtranslucencyB;
    String TOWtranslucencyAstr, TOWtranslucencyBstr;

    static final short BACKGROUND_WORLDMAP = 0;
    static final short BACKGROUND_GRAPH = 1;
    static final String BACKGROUND_WORLDMAP_STR = "WL";
    static final String BACKGROUND_GRAPH_STR = "GR";
    short backgroundType = BACKGROUND_WORLDMAP;
    static String getBackgroundType(short bt){
	if (bt == BACKGROUND_GRAPH){
	    return BACKGROUND_GRAPH_STR;
	}
	else if (bt == BACKGROUND_WORLDMAP){
	    return BACKGROUND_WORLDMAP_STR;
	}
	else {
	    return "";
	}
    }

    static final LongPoint[] WORLD_DISTRACTORS = {new LongPoint(-471, 111),
						  new LongPoint(-357, 264),
						  new LongPoint(167, 340),
						  new LongPoint(373, 148),
						  new LongPoint(356, 481),
						  new LongPoint(-208, 51),
						  new LongPoint(-287, 170),
						  new LongPoint(-587, 250),
						  new LongPoint(-602, 471),
						  new LongPoint(282, 77),
						  new LongPoint(227, -257),
						  new LongPoint(409, -131),
						  new LongPoint(-340, -108),
						  new LongPoint(-47, 37),
						  new LongPoint(18, 16),
						  new LongPoint(-36, -68),
						  new LongPoint(38, -36),
						  new LongPoint(-101, -74),
						  new LongPoint(-175, -230),
						  new LongPoint(-326, -369),
						  new LongPoint(-417, -277),
						  new LongPoint(-721, -401),
						  new LongPoint(-710, 246),
						  new LongPoint(-568, -202),
						  new LongPoint(570, -341),
						  new LongPoint(308, -128),
						  new LongPoint(359, -458),
						  new LongPoint(271, -49),
						  new LongPoint(618, -69),
						  new LongPoint(47, 266),
						  new LongPoint(490, 245),
						  new LongPoint(270, 252)};

    /* logs */
    BehaviorLogManager blm;

    public BehaviorEval(short bt, String bv, String tl){
	initGUI();
	blm = new BehaviorLogManager(this);
	initTargetAbstractLocation(tl);
	mView.setEventHandler(eh);
	initBehavior(bv);
	initWorld(bt);
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(0);
	vsm.repaintNow(mView, this);
    }

    void initTargetAbstractLocation(String tl){
	if (tl.equals(BehaviorBlock.TARGET_MAIN_VIEWPORT)){
	    eh = new BehaviorMVEventHandler(this);
	    blm.abstractTargetLocation = BehaviorBlock.TARGET_MAIN_VIEWPORT;
	}
	else if (tl.equals(BehaviorBlock.TARGET_TRAILING_WIDGET)){
	    eh = new BehaviorTWEventHandler(this);
	    blm.abstractTargetLocation = BehaviorBlock.TARGET_TRAILING_WIDGET;
	}
	else {
	    System.err.println("Error: unknown abstract target location: "+tl);
	    exit();
	}
    }

    void initBehavior(String t){
	if (t.equals(BehaviorBlock.BEHAVIOR_FT)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_FT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_FT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_FT;
	}
	else if (t.equals(BehaviorBlock.BEHAVIOR_IT)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_IT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_IT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_IT;
	}
	else if (t.equals(BehaviorBlock.BEHAVIOR_DT)){
	    TOWtranslucencyA = BehaviorBlock.BEHAVIOR_DT_A;
	    TOWtranslucencyB = BehaviorBlock.BEHAVIOR_DT_B;
	    blm.behaviorName = BehaviorBlock.BEHAVIOR_DT;
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
	    // distractors
	    for (int i=0;i<WORLD_DISTRACTORS.length;i++){
		vsm.addGlyph(new VRectangle(WORLD_DISTRACTORS[i].x, WORLD_DISTRACTORS[i].y, 0, 5, 5, DISTRACTOR_COLOR), mSpace);
	    }
	    // potential targets
	    NW_TARGET = new VRectangle(-135,120,0,5,5,DISTRACTOR_COLOR);
	    NE_TARGET = new VRectangle(95,120,0,5,5,DISTRACTOR_COLOR);
	    SE_TARGET = new VRectangle(95,-120,0,5,5,DISTRACTOR_COLOR);
	    SW_TARGET = new VRectangle(-135,-120,0,5,5,DISTRACTOR_COLOR);
	    vsm.addGlyph(NW_TARGET, mSpace);
	    vsm.addGlyph(NE_TARGET, mSpace);
	    vsm.addGlyph(SE_TARGET, mSpace);
	    vsm.addGlyph(SW_TARGET, mSpace);
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

    public void viewRepainted(View v){
	blm.im.say(AcquireLogManager.PSTS);
	v.removeRepaintListener();
    }

    void exit(){
	if (to != null){to.dispose();}
	System.exit(0);
    }
    
    public static void main(String[] args){
	try {
	    String beh = BehaviorBlock.BEHAVIOR_FT;
	    String tar = BehaviorBlock.TARGET_MAIN_VIEWPORT;
	    if (args.length >= 3){
		beh = args[1];
		tar = args[2];
		if (args.length >= 5){
		    BehaviorEval.VIEW_MAX_W = Integer.parseInt(args[3]);
		    BehaviorEval.VIEW_MAX_H = Integer.parseInt(args[4]);
		}
	    }
	    new BehaviorEval(Short.parseShort(args[0]), beh, tar);
	}
	catch (Exception ex){
	    System.err.println("Usage:\n\tjava -cp [...] net.claribole.eval.to.BehaviorEval <backgroundType> <FT>|<IT>|<DT> <MV>|<TW> [<window width> <window height>]");
	    System.exit(0);
	}
    }

}
