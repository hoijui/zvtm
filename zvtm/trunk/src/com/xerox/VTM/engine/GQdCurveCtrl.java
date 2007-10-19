/*   FILE: GQdCurveCtrl.java
 *   DATE OF CREATION:   Oct 05 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Tue Mar 26 09:47:06 2002 by Emmanuel Pietriga
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

import com.xerox.VTM.glyphs.VQdCurve;

/**glyph animation: control point of a quadratic curve
 * @author Emmanuel Pietriga
 */

class GQdCurveCtrl extends GCurveCtrl{

    VQdCurve trueTarget;

    /** 
     *@param g glyph to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    GQdCurveCtrl(VQdCurve g,AnimManager mgr,long d){
	started=false;
	target=g;
	trueTarget=g;
	parent=mgr;
	duration=d;
	type=AnimManager.GL_CTRL;
    }

    boolean animate() {//the returned boolean says will be used to know if anything happened here, i.e. if iot is necessary to repaint
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		trueTarget.setCtrlPoint(steps[step].r,steps[step].theta);
	    }
	    else {
		trueTarget.setCtrlPoint(steps[steps.length-1].r,steps[steps.length-1].theta);
		parent.killCurveAnim(this);
	    }
	    return true;
	}
	else return false;
    }

    protected void conclude(){
	trueTarget.setCtrlPoint(steps[steps.length-1].r,steps[steps.length-1].theta);
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_CTRL);
	}
    }

}
