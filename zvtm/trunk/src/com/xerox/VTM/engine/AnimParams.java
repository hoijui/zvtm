/*   FILE: AnimParams.java
 *   DATE OF CREATION:   Jul 18 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
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

import net.claribole.zvtm.engine.PostAnimationAction;

/**
 * class used to temporarily store animation parameters in case it is being delayed by Animation Manager - used for glyph, camera and lens animations
 * @author Emmanuel Pietriga
 */

class AnimParams{

    /**duration of animation in milliseconds*/
    long duration;
    /**type of animation*/
    short type;
    /**data associated to this animation*/
    Object data;
    /**kill object after animation ends*/
    boolean kgaa;
    /**action to be performed after animation ends*/
    PostAnimationAction paa;
    
    /**
     *@param d duration of animation in milliseconds
     *@param t type of animation
     *@param dt data associated to this animation
     */
    AnimParams(long d,short t,Object dt, PostAnimationAction paa){
	duration=d;
	type=t;
	data=dt;
	kgaa = false;
	this.paa = paa;
    }

    /**
     *@param d duration of animation in milliseconds
     *@param t type of animation
     *@param dt data associated to this animation
     *@param k true -> kill object after animation ends
     */
    AnimParams(long d,short t,Object dt, boolean k, PostAnimationAction paa){
	duration=d;
	type=t;
	data=dt;
	kgaa = k;
	this.paa = paa;
    }

}
