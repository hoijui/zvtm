/*   FILE: FractalKoch.java
 *   DATE OF CREATION:  Thu Dec 30 12:53:03 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;

import net.claribole.zvtm.engine.ViewEventHandler;

public abstract class FractalDemo {

    VirtualSpaceManager vsm;

    String mainSpaceName = "fractalSpace";
    String mainViewName = "Fractal";

    ViewEventHandler eh;

    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static int ANIM_MOVE_LENGTH = 500;

    void translateView(short direction){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	LongPoint trans;
	long[] rb = vsm.getView(mainViewName).getVisibleRegion(c);
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
	else {//MOVE_LEFT
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	vsm.animator.createCameraAnimation(FractalKoch.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, c.getID());
    }

    void getGlobalView(){
	vsm.getGlobalView(vsm.getActiveCamera(), FractalKoch.ANIM_MOVE_LENGTH);
    }

    void getHigherView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	Float alt = new Float(c.getAltitude()+c.getFocal());
	vsm.animator.createCameraAnimation(FractalKoch.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, c.getID());
    }
    
    void getLowerView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	Float alt = new Float(-(c.getAltitude()+c.getFocal())/2.0f);
	vsm.animator.createCameraAnimation(FractalKoch.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, c.getID());
    }

    void exit(){
	System.exit(0);
    }
    
}
