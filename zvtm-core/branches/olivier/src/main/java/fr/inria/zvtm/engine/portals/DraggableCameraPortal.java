/*   FILE: DraggableCameraPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;

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
     *@param c camera associated with the portal
     */
    public DraggableCameraPortal(int x, int y, int w, int h, Camera c){
	    super(x, y, w, h, c);
    }

    public DraggableCameraPortal(int x, int y, int w, int h, Vector<Camera> cvect){
        super(x, y, w, h, cvect.elementAt(0));
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
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!visible){return;}
        //Check if the portal is out of the view
        if (x+w+halfBorderWidth < 0 || y+h+halfBorderWidth < 0 ||
            x-halfBorderWidth >= viewWidth || y-halfBorderWidth >= viewHeight){
            return;
        }
        if (alphaC != null){
            // portal is not opaque
            if (alphaC.getAlpha() == 0){
                // portal is totally transparent
                return;
            }
            g2d.setComposite(alphaC);
        }
        g2d.setClip(x, y, w, h);
        if (bkgColor != null){
            g2d.setColor(bkgColor);
            g2d.fillRect(x, y, w, h);
        }
        standardStroke = g2d.getStroke();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        standardTransform = g2d.getTransform();
        drawnGlyphs = cameraSpace.getDrawnGlyphs(camIndex);
        synchronized(drawnGlyphs){
            drawnGlyphs.removeAllElements();
            duncoef = (camera.focal+camera.altitude) / camera.focal;
            //compute region seen from this view through camera
            viewWC = camera.vx - (w/2d) * duncoef;
            viewNC = camera.vy + (h/2d) * duncoef;
            viewEC = camera.vx + (w/2d) * duncoef;
            viewSC = camera.vy - (h/2d) * duncoef;
            gll = cameraSpace.getDrawingList();
            for (int i=0;i<gll.length;i++){
                if (gll[i] != null){
                    synchronized(gll[i]){
                        if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, camera)){
                            //if glyph is at least partially visible in the reg. seen from this view, display
                            gll[i].project(camera, size); // an invisible glyph should still be projected
                            if (gll[i].isVisible()){      // as it can be sensitive
                                gll[i].draw(g2d, w, h, camIndex, standardStroke, standardTransform, x, y);
                            }
                            cameraSpace.drewGlyph(gll[i], camIndex);
                        }
                    }
                }
            }
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
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
