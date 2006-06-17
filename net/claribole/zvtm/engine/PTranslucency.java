/*   FILE: PTranslucency.java
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

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Transparent;

/**portal animation: translucency
 * @author Emmanuel Pietriga
 */

public class PTranslucency extends PAnimation {

    /** step values for translucency (alpha channel)*/
    public float[] steps;

    /** 
     *@param c camera to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    public PTranslucency(Portal p,AnimManager mgr,long d){
	started = false;
	target = p;
	parent = mgr;	
	duration = d;
	type = AnimManager.PT_ALPHA;
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
		((Transparent)target).setTransparencyValue(steps[step]);
	    }
	    else {
		((Transparent)target).setTransparencyValue(steps[steps.length-1]);
		parent.killPAnim(this,type);
	    }
	}
    }

    public void conclude(){
	((Transparent)target).setTransparencyValue(steps[steps.length-1]);
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.PORTAL, AnimManager.PT_ALPHA);
	}
    }

}