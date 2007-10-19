/*   FILE: PTransResize.java
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

public class PTransResize extends PAnimation {

    /** step values for resizing (x,y)*/
    public Point[] tsteps;
    /** step values for resizing (w,h)*/
    public Point[] ssteps;

    /** 
     *@param p portal to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    public PTransResize(Portal p,AnimManager mgr,long d){
	started = false;
	target = p;
	parent = mgr;	
	duration = d;
	type = AnimManager.PT_BOTH;
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
	    step = (int)Math.round(tsteps.length*progression);
	    if (step<tsteps.length) {
		target.x = tsteps[step].x;
		target.y = tsteps[step].y;
		target.w = ssteps[step].x;
		target.h = ssteps[step].y;
	    }
	    else {
		target.x = tsteps[tsteps.length-1].x;
		target.y = tsteps[tsteps.length-1].y;
		target.w = ssteps[ssteps.length-1].x;
		target.h = ssteps[ssteps.length-1].y;
		target.updateDimensions();
		parent.killPAnim(this,type);
	    }
	}
    }

    public void conclude(){
	target.x = tsteps[tsteps.length-1].x;
	target.y = tsteps[tsteps.length-1].y;
	target.w = ssteps[ssteps.length-1].x;
	target.h = ssteps[ssteps.length-1].y;
	target.updateDimensions();
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.PORTAL, AnimManager.PT_BOTH);
	}
    }

}