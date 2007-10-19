/*   FILE: CTransZoom.java
 *   DATE OF CREATION:   Aug 04 2000
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

/**camera animation: translation+zoom (altitude change)
 * @author Emmanuel Pietriga
 */

class CTransZoom extends CAnimation{

    /** step values for altitude */
    float[] zsteps;
    /** step values for translation (x,y)*/
    LongPoint[] tsteps;

    /** 
     *@param c camera to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    CTransZoom(Camera c,AnimManager mgr,long d){
	started=false;
	target=c;
	parent=mgr;	
	duration=d;
	type=AnimManager.CA_BOTH;
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
	    step=(int)Math.round(zsteps.length*progression);
	    if (step<zsteps.length) {
		target.setAltitude(zsteps[step]);
		target.posx=tsteps[step].x;
		target.posy=tsteps[step].y;
		target.updatePrecisePosition();
		if (step > 0){
		    target.propagateMove(tsteps[step].x-tsteps[step-1].x,
					 tsteps[step].y-tsteps[step-1].y);
		}
	    }
	    else {
		target.setAltitude(zsteps[zsteps.length-1]);
		target.posx=tsteps[tsteps.length-1].x;
		target.posy=tsteps[tsteps.length-1].y;
		target.updatePrecisePosition();
		if (step > 1){
		    target.propagateMove(tsteps[tsteps.length-1].x-tsteps[tsteps.length-2].x,
					 tsteps[tsteps.length-1].y-tsteps[tsteps.length-2].y);
		}
		parent.killCAnim(this,type);
	    }
	}
    }

    protected void conclude(){
	target.setAltitude(zsteps[zsteps.length-1]);
	target.posx=tsteps[tsteps.length-1].x;
	target.posy=tsteps[tsteps.length-1].y;
	target.updatePrecisePosition();
	if (tsteps.length > 1){
	    target.propagateMove(tsteps[tsteps.length-1].x-tsteps[tsteps.length-2].x,
				 tsteps[tsteps.length-1].y-tsteps[tsteps.length-2].y);
	}
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.CAMERA, AnimManager.CA_BOTH);
	}
    }

}
