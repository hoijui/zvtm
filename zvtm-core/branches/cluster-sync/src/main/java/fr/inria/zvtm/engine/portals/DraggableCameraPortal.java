/*   FILE: DraggableCameraPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.Graphics2D;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;

/**A portal showing what is seen through a camera. The portal featurs a thin horizontal bar at the top which is used to drag it. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class DraggableCameraPortal extends CameraPortal {

    /**Color of horizontal bar used to drag the portal.*/
    Color barColor = Color.RED;

    /**Height of horizontal bar used to drag the portal.*/
    int barHeight = 10;

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cam camera associated with the portal
     */
    public DraggableCameraPortal(int x, int y, int w, int h, Camera cam){
	    super(x, y, w, h, cam);
    }

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cams camera associated with the portal
     */
    public DraggableCameraPortal(int x, int y, int w, int h, Camera[] cams){
	    super(x, y, w, h, cams, 1f);
    }

    /**Set color of horizontal bar used to drag the portal.
     *@param bc color of the bar*/
    public void setDragBarColor(Color bc){
	    this.barColor = bc;
    }

    /**Get color of horizontal bar used to drag the portal.
     *@return color of bar*/
    public Color getDragBarColor(){
	    return barColor;
    }

    /**Set height of horizontal bar used to drag the portal.
     *@param bh height of the bar*/
    public void setDragBarHeight(int bh){
	    this.barHeight = bh;
    }

    /**Get height of horizontal bar used to drag the portal.
     *@return height of bar*/
    public int getDragBarHeight(){
	    return barHeight;
    }

    /**detects whether the given point is inside this portal's horizontal bar or not
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public boolean coordInsideBar(int cx, int cy){
        return ((cx >= x+halfBorderWidth) && (cx <= x+w+halfBorderWidth) &&
            (cy >= y+halfBorderWidth) && (cy <= y+barHeight+halfBorderWidth));
    }

    @Override
    void paintPortalFrame(Graphics2D g2d, Stroke standardStroke){
        g2d.setColor(barColor);
        g2d.fillRect(x, y, w, barHeight+(int)halfBorderWidth);
        if (borderColor != null){
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.setColor(borderColor);
            g2d.drawRect(x, y, w, h);
            g2d.setStroke(standardStroke);
        }
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
    }

}
