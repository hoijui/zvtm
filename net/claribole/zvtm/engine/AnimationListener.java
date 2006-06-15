/*   FILE: AnimationListener.java
 *   DATE OF CREATION:   Nov 08 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Fri Nov 08 09:35:54 2002 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
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

package net.claribole.zvtm.engine;


/**
 * Animation Listener interface - AnimManager calls back method notify() each time it makes a change to a camera or (possibly each time the animation thread runs through the loop) - set by client application if it wants to be notified of animations currently running  (for instance when the camera is moved)
 * @author Emmanuel Pietriga
 **/

public interface AnimationListener {

    /**called by AnimManager each time it runs through the animation loop and modifies a camera position/altitude*/
    public void cameraMoved();

}
