/*   FILE: PResize.java
 *   DATE OF CREATION:  Sat Jun 17 11:27:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import java.util.Date;

import java.awt.Point;

import com.xerox.VTM.engine.AnimManager;

/**portal animation: translation
 * @author Emmanuel Pietriga
 */

public class PResize extends PAnimation {

    /** step values for resizing (w,h)*/
    public Point[] steps;

    /** 
     *@param c camera to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    public PResize(Portal p,AnimManager mgr,long d){
	started = false;
	target = p;
	parent = mgr;	
	duration = d;
	type = AnimManager.PT_SZ;
    }

    public void start(){		
	now = new Date();
	startTime = now.getTime();
	started = true;
    }

    public void animate() {
	if (started){
	    now = new Date();
	    progression = (double)((now.getTime()-startTime)/(double)duration);
	    step = (int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		target.w = steps[step].x;
		target.h = steps[step].y;
	    }
	    else {
		target.w = steps[steps.length-1].x;
		target.h = steps[steps.length-1].y;
		target.updateDimensions();
		parent.killPAnim(this,type);
	    }
	}
    }

    public void conclude(){
	target.w = steps[steps.length-1].x;
	target.h = steps[steps.length-1].y;
	target.updateDimensions();
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.PORTAL, AnimManager.PT_SZ);
	}
    }

}