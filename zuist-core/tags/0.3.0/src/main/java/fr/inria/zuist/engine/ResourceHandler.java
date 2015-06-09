/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

import java.net.URL;

/** Interface implemented by handlers of the various resource types.
 *@author Emmanuel Pietriga
 */

public interface ResourceHandler {
    
    public ResourceDescription createResourceDescription(long x, long y, String id, int zindex, Region region, 
                                                         URL resourceURL, boolean sensitivity, Color stroke, String params);
                                                         
}
