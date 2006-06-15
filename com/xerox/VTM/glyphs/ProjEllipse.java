/*   FILE: ProjEllipse.java
 *   DATE OF CREATION:   Oct 14 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 * $Id: ProjEllipse.java,v 1.4 2005/12/08 09:08:21 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.geom.Ellipse2D;

/**project coordinates of an ellipse
 * @author Emmanuel Pietriga
 */

class ProjEllipse extends ProjectedCoords {

    /**main shape*/
    Ellipse2D ellipse=new Ellipse2D.Float();
    /**main shape size in camera space*/
    float cvw,cvh;

    /**main shape*/
    Ellipse2D lellipse=new Ellipse2D.Float();
    /**main shape size in lens space*/
    float lcvw,lcvh;

}
