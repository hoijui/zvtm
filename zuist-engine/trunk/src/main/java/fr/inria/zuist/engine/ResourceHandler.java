/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

import java.net.URL;

/** Interface implemented by handlers of the various resource types.
 * Resource types include PDF documents, FITS images, as well as scene fragments (scenes dynamically loaded within a master scene).
 *@author Emmanuel Pietriga
 */

public interface ResourceHandler {
    
    /** Create a description of this resource.
     *@param x x-coordinate in scene
     *@param y y-coordinate in scene
     *@param id ID of object in scene
     *@param zindex z-index (layer). Feed 0 if you don't know.
     *@param region containing region
     *@param resourceURL path to bitmap resource (any valid absolute URL)
     *@param sensitivity should the glyph representing this resource be made sensitive to cursor events or not.
     *@param stroke border color (null if no border)
     *@param params a string of parameters specific to each resource type (see documentation for each resource type).
     */
    public ResourceDescription createResourceDescription(double x, double y, String id, int zindex, Region region, 
                                                         URL resourceURL, boolean sensitivity, Color stroke, String params);
                                                         
}
