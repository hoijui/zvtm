/*   FILE: AcquireTraining.java
 *   DATE OF CREATION:  Fri Jan 26 10:36:06 2007
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
import java.awt.Graphics2D;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;

public class AcquireTraining implements TOWApplication, Java2DPainter {

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
    String mViewName = "Training";
    Camera mCamera, oCamera;

    AcquireTrainingEventHandler eh;

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

    int acquisitionCount = 0;
    String acquisitionCountStr = String.valueOf(acquisitionCount);

    public AcquireTraining(){
	initGUI();
	eh = new AcquireTrainingEventHandler(this);
	mView.setEventHandler(eh);
	mCamera.moveTo(0, 0);
	mCamera.setAltitude(0);
	initWorld();
// 	centerOverview(false);
// 	updateOverview();
	vsm.repaintNow(mView);
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
	mView.setBackgroundColor(AcquireTraining.BACKGROUND_COLOR);
	mView.setAntialiasing(true);
	mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
	updatePanelSize();
    }

    void initWorld(){
// 	vsm.addGlyph(new ZCircle(100, 100, 0, 40, Color.BLUE), mSpace);
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

//     void centerOverview(boolean animate){
// 	if (animate){
// 	    vsm.animator.createCameraAnimation(OVERVIEW_CENTERING_TRANSLATE_TIME, AnimManager.CA_TRANS_SIG,
// 					       new LongPoint(mCamera.posx-oCamera.posx, mCamera.posy-oCamera.posy),
// 					       oCamera.getID(), null);
// 	}
// 	else {
// 	    oCamera.moveTo(mCamera.posx, mCamera.posy);
// 	}
//     }
    
//     void updateOverview(){// update overview camera's altitude
// 	oCamera.setAltitude((float)((mCamera.getAltitude()+mCamera.getFocal())*OVERVIEW_CAMERA_ALTITUDE_FACTOR-mCamera.getFocal()));
//     }

    static final Color INSIDE_TOW_BACKGROUND_COLOR = Color.GREEN;
    static final Color OUTSIDE_TOW_BACKGROUND_COLOR = BACKGROUND_COLOR;

    void switchPortal(int x, int y){
	if (to != null){// portal is active, destroy it it
	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(-0.5f),
					       to.getID(), new PortalKiller(this));
	}
	else {// portal not active, create it
	    to = getPortal(x, y);
	    to.setBackgroundColor(OUTSIDE_TOW_BACKGROUND_COLOR);
	    to.setPortalEventHandler((PortalEventHandler)eh);
	    vsm.addPortal(to, mView);
 	    to.setBorder(DEFAULT_PORTAL_BORDER_COLOR);
	    vsm.animator.createPortalAnimation(TOW_SWITCH_ANIM_TIME, AnimManager.PT_ALPHA_LIN, new Float(0.5f),
					       to.getID(), null);
	    oCamera.moveTo(5000, 5000);
// 	    centerOverview(false);
	}
    }

    TrailingOverview getPortal(int x, int y){
	return new TrailingOverview(x-TOW_CONTRACTED_WIDTH/2, y-TOW_CONTRACTED_HEIGHT/2,
				    TOW_CONTRACTED_WIDTH, TOW_CONTRACTED_HEIGHT,
				    oCamera, mCamera, 0.0f, TOW_PORTAL_X_OFFSET, TOW_PORTAL_Y_OFFSET);
    }

    public void killPortal(){
	vsm.destroyPortal(to);
	to.dispose();
	to = null;
	vsm.repaintNow();
    }

    void incAcquisitionCount(){
	acquisitionCount++;
	acquisitionCountStr = String.valueOf(acquisitionCount);
    }

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(Color.BLACK);
	g2d.drawString(acquisitionCountStr, 20, 30);
    }

    void updatePanelSize(){
	Dimension d = mView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    void exit(){
	if (to != null){to.dispose();}
	System.exit(0);
    }
    
    public static void main(String[] args){
	if (args.length >= 2){
	    AcquireTraining.VIEW_MAX_W = Integer.parseInt(args[0]);
	    AcquireTraining.VIEW_MAX_H = Integer.parseInt(args[1]);
	}
	new AcquireTraining();
    }

}
