/*   FILE: FractalKoch.java
 *   DATE OF CREATION:  Thu Dec 30 12:53:03 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.demo;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.ViewEventHandler;

public abstract class FractalDemo {

    VirtualSpaceManager vsm;
    
    VirtualSpace vs;
    
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

	Animation transAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ANIM_MOVE_LENGTH, c, trans, true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(transAnim, true);	
    }

    void getGlobalView(){
	vsm.getGlobalView(vsm.getActiveCamera(), ANIM_MOVE_LENGTH);
    }

    void getHigherView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	float alt = c.getAltitude()+c.getFocal();

	Animation altAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt, true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(altAnim, true);	
    }
    
    void getLowerView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	float alt = -(c.getAltitude()+c.getFocal())/2.0f;

	Animation altAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt, true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(altAnim, true);
    }

    void exit(){
	System.exit(0);
    }
    
}
