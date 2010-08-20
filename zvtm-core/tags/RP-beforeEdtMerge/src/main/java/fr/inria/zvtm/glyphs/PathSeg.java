/*   FILE: PathSeg.java
 *   DATE OF CREATION:   Feb 08 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Fri Feb 08 15:21:54 2002 by Emmanuel Pietriga
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
 */

package fr.inria.zvtm.glyphs;

  /**
   * Used to decompose VPath in segments.
   * @author Emmanuel Pietriga
   **/

public class PathSeg {

    /**X coordinate*/
    public long x;
    /**Y coordinate*/
    public long y;
    /**W coordinate*/
    public long w;
    /**H coordinate*/
    public long h;

    PathSeg(long xc,long yc,long wc,long hc){
	x=xc;
	y=yc;
	w=wc;
	h=hc;
    }


}
