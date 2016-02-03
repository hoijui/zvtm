/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2016. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.event;

import fr.inria.zuist.od.ObjectDescription;
import fr.inria.zvtm.engine.VirtualSpace;

/** Listen to object-related events.
 *@author Emmanuel PIetriga
 */

public interface ObjectListener {

    /**Fired when an object is loaded/created.
     *@param od corresponding object description.
     *@param vs in which VirtualSpace.
     */
    public void objectCreated(ObjectDescription od, VirtualSpace vs);

    /**Fired when an object is unloaded/destroyed.
     *@param od corresponding object description.
     *@param vs in which VirtualSpace.
     */
    public void objectDestroyed(ObjectDescription od, VirtualSpace vs);

}
