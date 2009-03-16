/*   FILE: CAnimation.java
 *   DATE OF CREATION:   Jul 17 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Tue Mar 26 09:52:40 2002 by Emmanuel Pietriga
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

/**camera animation
 * @author Emmanuel Pietriga
 */

abstract class CAnimation{

    /**camera to be animated*/
    Camera target;
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
    /**type of animation - one of AnimManager.{CA_TRANS, CA_ALT, CA_BOTH}*/
    String type;
    /**action to be performed after animation ends*/
    PostAnimationAction paa;

    abstract void start();

    abstract void animate();

    protected abstract void conclude();

    public void setPostAnimationAction(PostAnimationAction paa){
	this.paa = paa;
    }

    public abstract void postAnimAction();

}
