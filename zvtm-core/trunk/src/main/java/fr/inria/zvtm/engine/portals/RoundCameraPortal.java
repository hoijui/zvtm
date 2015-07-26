/*   FILE: RoundCameraPortal.java
 *   DATE OF CREATION:  Sun Jun 18 16:44:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.Glyph;

/**A portal showing what is seen through a camera. Shape: circular.
   The Camera should not be used in any other View or Portal.*/

public class RoundCameraPortal extends CameraPortal {

    Ellipse2D clippingShape, borderShape;

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     */
    public RoundCameraPortal(int x, int y, int w, int h, Camera c){
        this(x, y, w, h, c, 1f);
    }

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public RoundCameraPortal(int x, int y, int w, int h, Camera c, float a){
        super(x, y, w, h, c);
        createShape();
    }

    private void createShape() {
        clippingShape = new Ellipse2D.Float(x+halfBorderWidth, y+halfBorderWidth, w-borderWidth, h-borderWidth);
        borderShape = new Ellipse2D.Float(x-halfBorderWidth, y-halfBorderWidth, w+borderWidth, h+borderWidth);
    }

    private void doSetFrame() {
        clippingShape.setFrame(x+halfBorderWidth, y+halfBorderWidth, w-borderWidth, h-borderWidth);
        borderShape.setFrame(x-halfBorderWidth, y-halfBorderWidth, w+borderWidth, h+borderWidth);
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     * Although the region seen is actually an oval, we approximate it to the bounding rectangle.
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    @Override
    public double[] getVisibleRegion(){
	    return super.getVisibleRegion();
    }

    @Override
    public boolean coordInside(int cx, int cy){
	    return borderShape.contains(cx, cy);
    }

    @Override
    public boolean coordInsideBorder(int cx, int cy){
        return (borderShape.contains(cx, cy) && !clippingShape.contains(cx, cy));
    }

    /** Move the portal inside the view (relative).
     *@param dx x-offset (JPanel coordinates system)
     *@param dy y-offset (JPanel coordinates system)
     */
    @Override
    public void move(int dx, int dy){
        super.move(dx, dy);
        doSetFrame();
    }

    /** Move the portal inside the view (absolute).
     *@param x new x-coordinate (JPanel coordinates system)
     *@param y new y-coordinate (JPanel coordinates system)
     */
    @Override
    public void moveTo(int x, int y){
        super.moveTo(x, y);
        doSetFrame();
    }

    @Override
    public void updateDimensions(){
        size.setSize(w, h);
        if (clippingShape != null){ doSetFrame(); }
        else { createShape();}
    }

    @Override
    public void setBorderWidth(float w){
        super.setBorderWidth(w);
        doSetFrame();
    }

    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		if (!visible){return;}
        //Check if the portal is out of the view
        if (x+w+borderWidth < 0 || y+h+borderWidth < 0 ||
            x-borderWidth >= viewWidth || y-borderWidth >= viewHeight){
            return;
        }
		if (alphaC != null){
            // portal is not is not opaque
            if (alphaC.getAlpha() == 0){
                // portal is totally transparent
                return;
            }
            g2d.setComposite(alphaC);
        }
        g2d.setClip(clippingShape);
        if (bkgColor != null){
            g2d.setColor(bkgColor);
            g2d.fill(clippingShape);
        }
        /*Graphics2d's original stroke. Passed to each glyph in case it needs to modifiy the stroke when painting itself*/
        Stroke standardStroke = g2d.getStroke();
        /*Graphics2d's original affine transform. Passed to each glyph in case it needs to modifiy the affine transform when painting itself*/
        AffineTransform standardTransform= g2d.getTransform();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        double[] wnes;
        double duncoef;
        for (int j=0;j<cameras.length;j++){
            Camera c = cameras[j];
            VirtualSpace vs = cameraSpaces[j];
            Vector<Glyph> drawnGlyphs = vs.getDrawnGlyphs(c.getIndex());
            synchronized(drawnGlyphs){
                drawnGlyphs.removeAllElements();
                duncoef = (c.focal+c.altitude) / c.focal;
                //compute region seen from this view through camera
                wnes = new double[]{c.vx - (w/2d) * duncoef,
                                    c.vy + (h/2d) * duncoef,
                                    c.vx + (w/2d) * duncoef,
                                    c.vy - (h/2d) * duncoef};
                Glyph[] gll = vs.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        synchronized(gll[i]){
                            if (gll[i].visibleInViewport(wnes[0], wnes[1], wnes[2], wnes[3], c)){
                                //if glyph is at least partially visible in the reg. seen from this view, display
                                gll[i].project(c, size); // an invisible glyph should still be projected
                                if (gll[i].isVisible()){      // as it can be sensitive
                                    gll[i].draw(g2d, w, h, c.getIndex(), standardStroke, standardTransform, x, y);
                                }
                                vs.drewGlyph(gll[i], c.getIndex());
                            }
                        }
                    }
                }
            }
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
        if (borderColor != null){
            g2d.setColor(borderColor);
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.draw(clippingShape);
            g2d.setStroke(standardStroke);
        }
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
    }

}
