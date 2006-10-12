/*   FILE: Eval.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id:  $
 */ 

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

public class Eval {

    /* techniques */
    static final short TECHNIQUE_OV = 0;
    static final String TECHNIQUE_OV_NAME = "Overview + Detail"; 
    static final short TECHNIQUE_TOW = 1;
    static final String TECHNIQUE_TOW_NAME = "Trailing Overview";
    short technique = TECHNIQUE_TOW;

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static final int VIEW_MAX_W = 1280;
    static final int VIEW_MAX_H = 1024;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    /* ZVTM components */
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    View mView;
    String mViewName = "Evaluation";
    Camera mCamera, oCamera;

    BaseEventHandler eh;

    public Eval(short t){
	if (t == TECHNIQUE_OV){
	    this.technique = TECHNIQUE_OV;
	    mViewName = TECHNIQUE_OV_NAME;
	    eh = new OVEventHandler(this);
	}
	else {
	    this.technique = TECHNIQUE_TOW;
	    mViewName = TECHNIQUE_TOW_NAME;
	    eh = new TOWEventHandler(this);
	}
	initGUI();
	initWorld();
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
	mView.setEventHandler(eh);
	
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

    void initWorld(){
	
    }
    
    public static void main(String[] args){
	try {
	    new Eval(Short.parseShort(args[0]));
	}
	catch (Exception ex){
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Trailing Overview");
	    new Eval(Eval.TECHNIQUE_TOW);
	}
    }

}