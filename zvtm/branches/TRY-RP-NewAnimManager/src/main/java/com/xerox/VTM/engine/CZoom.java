/*   FILE: CZoom.java
 *   DATE OF CREATION:   Jul 19 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Tue Mar 26 09:51:14 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 */

package com.xerox.VTM.engine;

import java.util.Date;
import net.claribole.zvtm.engine.PostAnimationAction;

/**camera animation: zoom (altitude change)
 * @author Emmanuel Pietriga
 */

class CZoom extends CAnimation{

    /** step values for altitude */
    float[] steps;

    /** 
     *@param c camera to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    CZoom(Camera c,AnimManager mgr,long d){
	started=false;
	target=c;
	parent=mgr;	
	duration=d;
	type=AnimManager.CA_ALT;
    }

    void start(){		
	now=new Date();
	startTime=now.getTime();
	started=true;
    }

    void animate() {
	if (started){
	    now=new Date();
	    progression=(float)((now.getTime()-startTime)/(float)duration);
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		target.setAltitude(steps[step]);
	    }
	    else {
		target.setAltitude(steps[steps.length-1]);
		parent.killCAnim(this,type);
	    }
	}
    }

    protected void conclude(){
	target.setAltitude(steps[steps.length-1]);
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.CAMERA, AnimManager.CA_ALT);
	}
    }

}
