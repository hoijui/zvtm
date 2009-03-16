/*   FILE: FSLMaxMagRadii.java
 *   DATE OF CREATION:  Wed Nov 10 14:42:24 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.lens;

import java.util.Date;

import com.xerox.VTM.engine.AnimManager;
import net.claribole.zvtm.engine.PostAnimationAction;

/**lens animation: maximum magnification and radii
 * @author Emmanuel Pietriga
 */

public class FSLMaxMagRadii extends LAnimation{

    /** step values for maximum magnification */
    public float[] mmsteps;
    /** step values for radii */
    public int[][] rsteps;

    /** cast of target for performance reasons*/
    protected FixedSizeLens fsTarget;

    /** 
     *@param l lens to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    public FSLMaxMagRadii(FixedSizeLens l,AnimManager mgr,long d){
	started=false;
	target=l;
	fsTarget = l;
	parent=mgr;	
	duration=d;
	type=AnimManager.LS_BOTH;
    }

    public void start(){
	now=new Date();
	startTime=now.getTime();
	started=true;
    }

    public void animate() {
	if (started){
	    now=new Date();
	    progression=(float)((now.getTime()-startTime)/(float)duration);
	    step=(int)Math.round(mmsteps.length*progression);
	    if (step < mmsteps.length) {//mmsteps.length should be = to rsteps.length
		synchronized(target){
		    fsTarget.setMMandRadii(mmsteps[step], rsteps[step][0], rsteps[step][1], false);
		}
	    }
	    else {
		synchronized(target){
		    fsTarget.setMMandRadii(mmsteps[mmsteps.length-1], rsteps[rsteps.length-1][0], rsteps[rsteps.length-1][1], false);
		    parent.killLAnim(this,type);
		}
	    }
	}
    }

    public void conclude(){
	synchronized(target){
	    fsTarget.setMMandRadii(mmsteps[mmsteps.length-1], rsteps[rsteps.length-1][0], rsteps[rsteps.length-1][1], false);
	}
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.LENS, AnimManager.LS_BOTH);
	}
    }

}