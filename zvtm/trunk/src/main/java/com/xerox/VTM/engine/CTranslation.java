/*   FILE: CTranslation.java
 *   DATE OF CREATION:   Jul 17 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package com.xerox.VTM.engine;

import java.util.Date;
import net.claribole.zvtm.engine.PostAnimationAction;

/**camera animation: translation
 * @author Emmanuel Pietriga
 */

class CTranslation extends CAnimation{

    /** step values for translation (x,y)*/
    LongPoint[] steps;

    /** 
     *@param c camera to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    CTranslation(Camera c,AnimManager mgr,long d){
	started=false;
	target=c;
	parent=mgr;	
	duration=d;
	type=AnimManager.CA_TRANS;
    }

    void start(){		
	now=new Date();
	startTime=now.getTime();
	started=true;
    }

    void animate() {
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		target.posx=steps[step].x;
		target.posy=steps[step].y;
		target.updatePrecisePosition();
		if (step > 0){
		    target.propagateMove(steps[step].x-steps[step-1].x,
					 steps[step].y-steps[step-1].y);
		}
	    }
	    else {
		target.posx=steps[steps.length-1].x;
		target.posy=steps[steps.length-1].y;
		target.updatePrecisePosition();
		if (step > 1){
		    target.propagateMove(steps[steps.length-1].x-steps[steps.length-2].x,
					 steps[steps.length-1].y-steps[steps.length-2].y);
		}
		parent.killCAnim(this,type);
	    }
	}
    }

    protected void conclude(){
	target.posx=steps[steps.length-1].x;
	target.posy=steps[steps.length-1].y;
	target.updatePrecisePosition();
	if (steps.length > 1){
	    target.propagateMove(steps[steps.length-1].x-steps[steps.length-2].x,
				 steps[steps.length-1].y-steps[steps.length-2].y);
	}
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.CAMERA, AnimManager.CA_TRANS);
	}
    }

}
