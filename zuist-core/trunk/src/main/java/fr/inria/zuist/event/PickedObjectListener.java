/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.event;

import fr.inria.zuist.od.ObjectDescription;

/** Listen to object description picking events.
 *@author Emmanuel PIetriga
 */

public interface PickedObjectListener {

    /**Fired when an ObjectPicker enters ObjectDescription od (regardless of whether the corresponding Glyph has actually been instantiated or not).*/
    public void enteredObject(ObjectDescription od);

    /**Fired when an ObjectPicker leaves ObjectDescription od (regardless of whether the corresponding Glyph has actually been instantiated or not).*/
    public void exitedObject(ObjectDescription od);

}
