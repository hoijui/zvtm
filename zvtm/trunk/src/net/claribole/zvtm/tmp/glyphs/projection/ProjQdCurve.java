/*   FILE: ProjQdCurve.java
 *   DATE OF CREATION:   Oct 02 2001
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
 * $Id$
 */

package net.claribole.zvtm.glyphs.projection;

import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

/**project coordinates of a quadratic curve
 * @author Emmanuel Pietriga
 */

public class ProjQdCurve extends BProjectedCoords {

    /**curve*/
    public QuadCurve2D.Double quad=new QuadCurve2D.Double();
    /**start point*/
    public Point2D.Double start=new Point2D.Double();
    /**end point*/
    public Point2D.Double end=new Point2D.Double();
    /**control point (controls both start and end points tangent vectors)*/
    public Point2D.Double ctrl=new Point2D.Double();

    /**curve*/
    public QuadCurve2D.Double lquad=new QuadCurve2D.Double();
    /**start point*/
    public Point2D.Double lstart=new Point2D.Double();
    /**end point*/
    public Point2D.Double lend=new Point2D.Double();
    /**control point (controls both start and end points tangent vectors)*/
    public Point2D.Double lctrl=new Point2D.Double();
}
