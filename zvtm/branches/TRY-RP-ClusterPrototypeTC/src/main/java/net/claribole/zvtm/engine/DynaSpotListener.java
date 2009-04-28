/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package net.claribole.zvtm.engine;

import com.xerox.VTM.engine.VCursor;

public interface DynaSpotListener {
	
	public void spotSizeChanged(VCursor c, int dynaSpotRadius);
	
}
