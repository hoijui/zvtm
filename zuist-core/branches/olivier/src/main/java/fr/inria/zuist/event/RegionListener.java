/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RegionListener.java 5516 2015-04-27 15:09:27Z epietrig $
 */

package fr.inria.zuist.event;

import fr.inria.zuist.engine.Region;

/** Listen to region-related camera events.
 *@author Emmanuel PIetriga
 */

public interface RegionListener {

    /**Fired when a RegionPicker enters Region r, or when a camera declared in SceneManager enters it.
       In other words, when the region becomes visible in the view
       (camera being in the corresponding altitude range).*/
    public void enteredRegion(Region r);

    /**Fired when a RegionPicker leaves Region r, or when a camera declared in SceneManager leaves it.
       In other words, when the region is no longer visible in the view
       (either offscreen or camera not in the corresponding altitude range).*/
    public void exitedRegion(Region r);

}
