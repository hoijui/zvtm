/*   FILE: ProjPolygon.java
 *   DATE OF CREATION:   Mon Jan 13 13:35:38 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: ProjPolygon.java,v 1.3 2005/12/05 15:21:15 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Polygon;

/**project coordinates of a custom shape
 * @author Emmanuel Pietriga
 */

class ProjPolygon extends BProjectedCoordsP {

    /**polygon vertices (projected in main space)*/
    int[] xpcoords;
    /**polygon vertices (projected in main space)*/
    int[] ypcoords;

    /**polygon vertices (projected in lens space)*/
    int[] lxpcoords;
    /**polygon vertices (projected in lens space)*/
    int[] lypcoords;

    ProjPolygon(int nbVertices){
	xpcoords = new int[nbVertices];
	ypcoords = new int[nbVertices];
	lxpcoords = new int[nbVertices];
	lypcoords = new int[nbVertices];
    }

}
