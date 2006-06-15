/*   FILE: Java2DPainter.java
 *   DATE OF CREATION:  Fri Aug 26 09:31:59 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Java2DPainter.java,v 1.2 2006/03/04 15:04:07 epietrig Exp $
 */ 

package net.claribole.zvtm.engine;

import java.awt.Graphics2D;

/**
 * Java2D painting operations interface (hook for direct Java2D painting in ZVTM views)
 * @author Emmanuel Pietriga
 **/

public interface Java2DPainter {

    public static final short BACKGROUND = 0;
    public static final short FOREGROUND = 1;
    public static final short AFTER_DISTORTION = 2;
    
    /**painting instructions (called by the associated view at each repaint)<br>This method is called at one of the following two times depending on how this painter was registered with the View: before ZVTM glyphs are painted (BACKGROUND), after ZVTM glyphs have been painted (FOREGROUND), after a distortion lens has been applied (ABOVE_LENS).
     *@param g2d the Graphics context on which to paint
     *@param viewWidth the associated View's width
     *@param viewHeight the associated View's height
     */
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight);

}
