/*   FILE: RoundCameraPortal.java
 *   DATE OF CREATION:  Sun Jun 18 16:44:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;

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

    public RoundCameraPortal(int x, int y, int w, int h, Vector<Camera> cvect){
        this(x, y, w, h, cvect.elementAt(0), 1f);
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
            viewWC = camera.vx - (w/2d)*duncoef;
            viewNC = camera.vy + (h/2d)*duncoef;
            viewEC = camera.vx + (w/2d)*duncoef;
            viewSC = camera.vy - (h/2d)*duncoef;
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
