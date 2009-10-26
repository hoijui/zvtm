/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.engine;

import java.awt.Color;

/** Interface implemented by handlers of the various resource types.
 *@author Emmanuel Pietriga
 */

public interface ResourceHandler {
    
    public ResourceDescription createResourceDescription(long x, long y, long w, long h, String id, int zindex, Region region, 
                                                         String imagePath, boolean sensitivity, Color stroke, Object im);
    
}
