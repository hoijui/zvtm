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

import java.io.File;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.TrailingOverview;
import net.claribole.zvtm.engine.RepaintListener;
import net.claribole.eval.Utils;

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

    static final String NE_REGION_MAP_PATH = "images/world/europe_1600x1200.png";
    static final String SE_REGION_MAP_PATH = "images/world/india_1600x1200.png";
    static final String NW_REGION_MAP_PATH = "images/world/peru_1600x1200.png";
    static final String SW_REGION_MAP_PATH = "images/world/patagonia_1600x1200.png";
    static final String WHOLE_MAP_PATH = "images/world/0000_400x200.png";
    static final String GRAPH_PATH = "data/graphs/tw.svg";

    /* ZVTM components */
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
    Color DEFAULT_PORTAL_BORDER_COLOR = Color.BLACK;
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

    static final Color BACKGROUND_COLOR = Color.WHITE;

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

    static final long GR_ORIG_X_MV = -7150;
    static final long GR_ORIG_Y_MV = -4400;
    static final long GR_ORIG_X_TW = -6500;
    static final long GR_ORIG_Y_TW = -5500;

    Glyph NW_TARGET_R1, NE_TARGET_R1, SE_TARGET_R1, SW_TARGET_R1;
    Glyph NW_TARGET_R2, NE_TARGET_R2, SE_TARGET_R2, SW_TARGET_R2;
    Glyph NW_TARGET_R3, NE_TARGET_R3, SE_TARGET_R3, SW_TARGET_R3;
    static final Color TARGET_COLOR = Color.RED;
    static final Color DISTRACTOR_COLOR = Color.YELLOW;
    static final Color MOUSE_INSIDE_DISTRACTOR_COLOR = Color.RED;

    void initWorld(short bt){
	backgroundType = bt;
	if (backgroundType == BACKGROUND_WORLDMAP){
	    DEFAULT_PORTAL_BORDER_COLOR = Color.WHITE;
	    VImage im = new VImage(WM_ORIG_X, WM_ORIG_Y, 0, (new ImageIcon(WHOLE_MAP_PATH)).getImage(), 40);
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    im = new VImage(BehaviorBlock.C_NE_X, BehaviorBlock.C_NE_Y, 0, (new ImageIcon(NE_REGION_MAP_PATH)).getImage());
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    im = new VImage(BehaviorBlock.C_SE_X, BehaviorBlock.C_SE_Y, 0, (new ImageIcon(SE_REGION_MAP_PATH)).getImage());
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    im = new VImage(BehaviorBlock.C_NW_X, BehaviorBlock.C_NW_Y, 0, (new ImageIcon(NW_REGION_MAP_PATH)).getImage());
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    im = new VImage(BehaviorBlock.C_SW_X, BehaviorBlock.C_SW_Y, 0, (new ImageIcon(SW_REGION_MAP_PATH)).getImage());
	    im.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(im, mSpace);
	    if (blm.abstractTargetLocation.equals(BehaviorBlock.TARGET_MAIN_VIEWPORT)){
		// distractors
		for (int i=0;i<Distractors.WORLD_DISTRACTORS.length;i++){
		    vsm.addGlyph(new VRectangle(Distractors.WORLD_DISTRACTORS[i].x, Distractors.WORLD_DISTRACTORS[i].y, 0, 5, 5, DISTRACTOR_COLOR), mSpace);
		}
		// potential targets R1
		NW_TARGET_R1 = new VRectangle(-135,120,0,5,5,DISTRACTOR_COLOR);
		NE_TARGET_R1 = new VRectangle(95,120,0,5,5,DISTRACTOR_COLOR);
		SE_TARGET_R1 = new VRectangle(95,-120,0,5,5,DISTRACTOR_COLOR);
		SW_TARGET_R1 = new VRectangle(-135,-120,0,5,5,DISTRACTOR_COLOR);
		vsm.addGlyph(NW_TARGET_R1, mSpace);
		vsm.addGlyph(NE_TARGET_R1, mSpace);
		vsm.addGlyph(SE_TARGET_R1, mSpace);
		vsm.addGlyph(SW_TARGET_R1, mSpace);
		// potential targets R2
		NW_TARGET_R2 = new VRectangle(-245,226,0,5,5,DISTRACTOR_COLOR);
		NE_TARGET_R2 = new VRectangle(205,226,0,5,5,DISTRACTOR_COLOR);
		SE_TARGET_R2 = new VRectangle(205,-226,0,5,5,DISTRACTOR_COLOR);
		SW_TARGET_R2 = new VRectangle(-245,-226,0,5,5,DISTRACTOR_COLOR);
		vsm.addGlyph(NW_TARGET_R2, mSpace);
		vsm.addGlyph(NE_TARGET_R2, mSpace);
		vsm.addGlyph(SE_TARGET_R2, mSpace);
		vsm.addGlyph(SW_TARGET_R2, mSpace);
		// potential targets R3
		NW_TARGET_R3 = new VRectangle(-445,424,0,5,5,DISTRACTOR_COLOR);
		NE_TARGET_R3 = new VRectangle(405,424,0,5,5,DISTRACTOR_COLOR);
		SE_TARGET_R3 = new VRectangle(405,-424,0,5,5,DISTRACTOR_COLOR);
		SW_TARGET_R3 = new VRectangle(-445,-424,0,5,5,DISTRACTOR_COLOR);
		vsm.addGlyph(NW_TARGET_R3, mSpace);
		vsm.addGlyph(NE_TARGET_R3, mSpace);
		vsm.addGlyph(SE_TARGET_R3, mSpace);
		vsm.addGlyph(SW_TARGET_R3, mSpace);
	    }
// 	    vsm.addGlyph(new VSegment(-20,0,0,1000,(float)(45*2*Math.PI/360.0),Color.BLUE), mSpace);
// 	    vsm.addGlyph(new VSegment(-20,0,0,1000,(float)(135*2*Math.PI/360.0),Color.BLUE), mSpace);
// 	    VCircle c = new VCircle(-20,0,0,160,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
// 	    c = new VCircle(-20,0,0,320,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
// 	    c = new VCircle(-20,0,0,600,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
	}
	else {// backgroundType == BACKGROUND_GRAPH
	    DEFAULT_PORTAL_BORDER_COLOR = Color.BLACK;
	    // distractors generated by GraphViz/neato
	    File f = new File(GRAPH_PATH);
	    long offset_X = 0;
	    long offset_Y = 0;
	    try {
		if (blm.abstractTargetLocation.equals(BehaviorBlock.TARGET_MAIN_VIEWPORT)){
		    SVGReader.setPositionOffset(GR_ORIG_X_MV, GR_ORIG_Y_MV);
		}
		else {
		    SVGReader.setPositionOffset(GR_ORIG_X_TW, GR_ORIG_Y_TW);
		    // coordinates in Distractors have been computed w.r.t GR_ORIG_X_MV and GR_ORIG_Y_MV
		    offset_X = GR_ORIG_X_TW - GR_ORIG_X_MV;
		    offset_Y = GR_ORIG_Y_TW - GR_ORIG_Y_MV;
		}
		SVGReader.load(Utils.parseXML(f, false), vsm, mSpaceName, false, f.toURL().toString());
	    }
	    catch (Exception ex){ex.printStackTrace();}

	    // additional distractors
	    for (int i=0;i<Distractors.GRAPH_DISTRACTOR_NODES.length;i++){
		vsm.addGlyph(new VRectangle(Distractors.GRAPH_DISTRACTOR_NODES[i].x+offset_X, Distractors.GRAPH_DISTRACTOR_NODES[i].y+offset_Y,
					    0, 5, 5, DISTRACTOR_COLOR), mSpace);
	    }
	    for (int i=0;i<Distractors.GRAPH_DISTRACTOR_EDGES_HEAD.length;i++){
		vsm.addGlyph(new VSegment(Distractors.GRAPH_DISTRACTOR_EDGES_TAIL[i].x+offset_X, Distractors.GRAPH_DISTRACTOR_EDGES_TAIL[i].y+offset_Y,
					  0, Color.BLACK,
					  Distractors.GRAPH_DISTRACTOR_EDGES_HEAD[i].x+offset_X, Distractors.GRAPH_DISTRACTOR_EDGES_HEAD[i].y+offset_Y),
			     mSpace);
	    }
	    // potential targets
	    // potential targets R1
	    NW_TARGET_R1 = new VRectangle(-135+offset_X,120+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    NE_TARGET_R1 = new VRectangle(95+offset_X,120+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SE_TARGET_R1 = new VRectangle(95+offset_X,-120+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SW_TARGET_R1 = new VRectangle(-135+offset_X,-120+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    vsm.addGlyph(NW_TARGET_R1, mSpace);
	    vsm.addGlyph(NE_TARGET_R1, mSpace);
	    vsm.addGlyph(SE_TARGET_R1, mSpace);
	    vsm.addGlyph(SW_TARGET_R1, mSpace);
	    // potential targets R2
	    NW_TARGET_R2 = new VRectangle(-245+offset_X,234+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    NE_TARGET_R2 = new VRectangle(205+offset_X,226+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SE_TARGET_R2 = new VRectangle(205+offset_X,-226+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SW_TARGET_R2 = new VRectangle(-245+offset_X,-226+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    vsm.addGlyph(NW_TARGET_R2, mSpace);
	    vsm.addGlyph(NE_TARGET_R2, mSpace);
	    vsm.addGlyph(SE_TARGET_R2, mSpace);
	    vsm.addGlyph(SW_TARGET_R2, mSpace);
	    // potential targets R3
	    NW_TARGET_R3 = new VRectangle(-445+offset_X,424+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    NE_TARGET_R3 = new VRectangle(413+offset_X,424+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SE_TARGET_R3 = new VRectangle(405+offset_X,-424+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    SW_TARGET_R3 = new VRectangle(-445+offset_X,-430+offset_Y,0,5,5,DISTRACTOR_COLOR);
	    vsm.addGlyph(NW_TARGET_R3, mSpace);
	    vsm.addGlyph(NE_TARGET_R3, mSpace);
	    vsm.addGlyph(SE_TARGET_R3, mSpace);
	    vsm.addGlyph(SW_TARGET_R3, mSpace);
	    Vector v = mSpace.getAllGlyphs();
	    Object o;
	    Glyph g;
	    for (int i=0;i<v.size();i++){
		o = v.elementAt(i);
		if (o instanceof VRectangle){
		    g = (Glyph)o;
		    g.setFill(true);
		    g.setColor(DISTRACTOR_COLOR);
		    g.setMouseInsideBorderColor(MOUSE_INSIDE_DISTRACTOR_COLOR);
		}
	    }
// 	    vsm.addGlyph(new VSegment(-20,0,0,1000,(float)(45*2*Math.PI/360.0),Color.BLUE), mSpace);
// 	    vsm.addGlyph(new VSegment(-20,0,0,1000,(float)(135*2*Math.PI/360.0),Color.BLUE), mSpace);
// 	    VCircle c = new VCircle(-20,0,0,160,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
// 	    c = new VCircle(-20,0,0,320,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
// 	    c = new VCircle(-20,0,0,600,Color.BLUE);
// 	    c.setFill(false);
// 	    c.setBorderColor(Color.BLUE);
// 	    vsm.addGlyph(c, mSpace);
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
