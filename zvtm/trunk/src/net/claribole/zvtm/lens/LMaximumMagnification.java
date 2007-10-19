/*   FILE: LMaximumMagnification.java
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

/**lens animation: maximum magnification
 * @author Emmanuel Pietriga
 */

public class LMaximumMagnification extends LAnimation{

    /** step values for maximum magnification */
    public float[] steps;

    /** 
     *@param l lens to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    public LMaximumMagnification(Lens l,AnimManager mgr,long d){
	started=false;
	target=l;
	parent=mgr;	
	duration=d;
	type=AnimManager.LS_MM;
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
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length){
 		synchronized(target){
		    try {
			target.setMaximumMagnification(steps[step], false);
		    }
		    catch (ArrayIndexOutOfBoundsException ex){System.err.println("Error while animating lens maximum magnification");}
 		}
	    }
	    else {
 		synchronized(target){
		    try {
			target.setMaximumMagnification(steps[steps.length-1], false);
		    }
		    catch (ArrayIndexOutOfBoundsException ex){System.err.println("Error while animating lens maximum magnification");}
		    parent.killLAnim(this,type);
 		}
	    }
	}
    }

    public void conclude(){
	target.setMaximumMagnification(steps[steps.length-1], false);
	if (finalRasterSize != -1){
	    synchronized(target){
		target.setMagRasterDimensions(finalRasterSize);
	    }
	}
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.LENS, AnimManager.LS_MM);
	}
    }

}