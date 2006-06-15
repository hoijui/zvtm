/*   FILE: LensRendering.java
 *   DATE OF CREATION:  Mon May 29 08:34:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LensRendering.java,v 1.1 2006/05/29 07:29:11 epietrig Exp $
 */

package net.claribole.zvtm.glyphs;

import java.awt.Color;

/**
 * Lens rendering interface.
 * Makes it possible to change some rendering attributes depending on whether the glyph is seen through a distortion lens or not.
 * @author Emmanuel Pietriga
 **/

public interface LensRendering {

    /**make this glyph (in)visible when seen through a lens (the glyph remains sensitive to cursor in/out events)<br>
     *@param b true to make glyph visible, false to make it invisible
     */
    public void setVisibleThroughLens(boolean b);

    /**get this glyph's visibility state when seen through the lens (returns true if visible)*/
    public boolean isVisibleThroughLens();

    /**set the color used to paint the glyph's interior*/
    public void setFillColorThroughLens(Color c);
    /**set the color used to paint the glyph's border*/
    public void setBorderColorThroughLens(Color c);

    /**get the color used to paint the glyph's interior*/
    public Color getFillColorThroughLens();
    /**get the color used to paint the glyph's border*/
    public Color getBorderColorThroughLens();

}
