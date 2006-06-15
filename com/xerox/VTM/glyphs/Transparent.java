/*   FILE: Transparent.java
 *   DATE OF CREATION:   Dec 24 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:32:08 2002 by Emmanuel Pietriga
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
 * $Id: Transparent.java,v 1.2 2005/12/08 09:08:21 epietrig Exp $
 */

package com.xerox.VTM.glyphs;


import java.awt.AlphaComposite;


/**
 * Transparency interface - implemented by all V[xxx]ST glyphs. Opacity is between 0.0 (fully transparent) and 1.0 (fully opaque)
 * @author Emmanuel Pietriga
 **/

public interface Transparent {

    /**original alpha composite (opaque)*/
    static final AlphaComposite acO=AlphaComposite.getInstance(AlphaComposite.SRC_OVER);  //opaque

    /**
     *set alpha channel value (transparency)
     *@param a [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public void setTransparencyValue(float a);


    /**get alpha value (transparency) for this glyph*/
    public float getTransparencyValue();

}
