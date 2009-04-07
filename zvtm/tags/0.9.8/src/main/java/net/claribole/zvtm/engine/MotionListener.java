/*   FILE: MotionListener.java
 *   DATE OF CREATION:  Sat Jan 29 10:03:21 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import com.xerox.VTM.engine.Camera;

/**
 * Camera motion listener interface
 * @author Emmanuel Pietriga
 **/

public interface MotionListener {

    /**called by camera to which this listener is registered to when its position changes */
    public void translation(Camera c);

    /**called by camera to which this listener is registered to when its altitude changes*/
    public void zoom(Camera c);

}
