/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

/** Listen to object-related events.
 *@author Emmanuel PIetriga
 */

public interface ObjectListener {
	
	/**Fired when an object is loaded/created.
	 *@param od corresponding object description.
	 */
	public void objectCreated(ObjectDescription od);

    /**Fired when an object is unloaded/destroyed.
     *@param od corresponding object description.
     */
    public void objectDestroyed(ObjectDescription od);	
	
}
