/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

/** Listen to region-related camera events.
 *@author Emmanuel PIetriga
 */

public interface RegionListener {
	
	/**Fired when a camera declared in SceneManager enters Region r. 
	   In other words, when the region becomes visible in the view
	   (camera being in the corresponding altitude range).*/
	public void enteredRegion(Region r);
	
	/**Fired when a camera declared in SceneManager leaves Region r. 
	   In other words, when the region is no longer visible in the view
	   (either offscreen or camera not in the corresponding altitude range).*/
	public void exitedRegion(Region r);
	
}
