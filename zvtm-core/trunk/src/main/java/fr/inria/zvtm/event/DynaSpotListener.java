/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.event;

import fr.inria.zvtm.engine.VCursor;

/** Listen to <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">DynaSpot</a> events. */

public interface DynaSpotListener {
	
	/** DynaSpot activation area size changed. */
	public void spotSizeChanged(VCursor c, int dynaSpotRadius);
	
}
