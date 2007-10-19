/*   FILE: PAnimation.java
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
import net.claribole.zvtm.engine.PostAnimationAction;

/**portal animation
 * @author Emmanuel Pietriga
 */

public abstract class PAnimation {

    /**lens to be animated*/
    public Portal target;
    /**current time*/
    Date now;
    /**animation began at this time*/ 
    long startTime;
    /**animation will last <I>duration</I> ms*/
    long duration;
    /**how much of the animation has been done*/
    double progression;
    /**has it started*/
    boolean started;
    int step;
    /**animation manager*/
    AnimManager parent;
    /**type of animation - one of AnimManager.{PT_TRANS, PT_SZ, PT_BOTH}*/
    public String type;
    /**action to be performed after animation ends*/
    PostAnimationAction paa;

    public abstract void start();

    public abstract void animate();

    public abstract void conclude();

    public void setPostAnimationAction(PostAnimationAction paa){
	this.paa = paa;
    }

    public abstract void postAnimAction();

}