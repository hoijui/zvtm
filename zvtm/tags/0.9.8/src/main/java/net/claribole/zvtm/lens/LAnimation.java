/*   FILE: LAnimation.java
 *   DATE OF CREATION:  Wed Nov 10 14:39:58 2004
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

/**lens animation
 * @author Emmanuel Pietriga
 */

public abstract class LAnimation{

    /**lens to be animated*/
    public Lens target;
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
    /**type of animation - one of AnimManager.{LS_MM, LS_RD, LS_BOTH}*/
    String type;
    /**action to be performed after animation ends*/
    PostAnimationAction paa;

    int finalRasterSize = -1;

    public abstract void start();

    public abstract void animate();

    public abstract void conclude();

    public int getFinalRasterSize(){
	return finalRasterSize;
    }

    public void setFinalRasterSize(int size){
	finalRasterSize = size;
    }

    public void updateRaster(){
	if (finalRasterSize != -1){
	    target.setMagRasterDimensions(finalRasterSize);
	}
    }

    public void setPostAnimationAction(PostAnimationAction paa){
	this.paa = paa;
    }

    public abstract void postAnimAction();

}