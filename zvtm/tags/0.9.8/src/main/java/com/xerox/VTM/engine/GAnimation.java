/*   FILE: GAnimation.java
 *   DATE OF CREATION:   Jul 12 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
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

import com.xerox.VTM.glyphs.Glyph;

/**
 * Glyph animation
 * @author Emmanuel Pietriga
 */

abstract class GAnimation{

    /**glyph to be animated*/
    Glyph target;
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
    /**type of animation - one of AnimManager.{GL_TRANS, GL_SZ, GL_ROT, GL_COLOR, GL_CTRL}*/
    String type;
    /**action to be performed after animation ends*/
    PostAnimationAction paa;

    abstract void start();

    abstract boolean animate(); //the returned boolean says will be used to know if anything happened here, i.e. if iot is necessary to repaint

    protected abstract void conclude();

    public void setPostAnimationAction(PostAnimationAction paa){
	this.paa = paa;
    }

    public abstract void postAnimAction();

}
