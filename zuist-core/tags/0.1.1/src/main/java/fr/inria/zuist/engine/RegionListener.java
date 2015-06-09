/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RegionListener.java,v 1.1 2007/10/02 09:11:28 pietriga Exp $
 */

package fr.inria.zuist.engine;

public interface RegionListener {
	
	public void enteredRegion(Region r);
	
	public void exitedRegion(Region r);
	
}
