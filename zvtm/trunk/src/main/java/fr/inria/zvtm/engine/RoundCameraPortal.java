/*   FILE: RoundCameraPortal.java
 *   DATE OF CREATION:  Sun Jun 18 16:44:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.engine.portals;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;

/**A portal showing what is seen through a camera. Shape: circular.
   The Camera should not be used in any other View or Portal.*/

public class RoundCameraPortal extends CameraPortal {

    Ellipse2D clippingShape;

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
        clippingShape = new Ellipse2D.Float(x, y, w, h);
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south].
       Although the region seen is actually an oval, we approximate it to the bounding rectangle.*/
    public double[] getVisibleRegion(){
	    return super.getVisibleRegion();
    }

    /**detects whether the given point is inside this portal or not 
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public boolean coordInside(int cx, int cy){
	return clippingShape.contains(cx, cy);
    }

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void move(int dx, int dy){
	super.move(dx, dy);
	clippingShape.setFrame(x, y, w, h);
    }

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void moveTo(int x, int y){
	super.moveTo(x, y);
	clippingShape.setFrame(x, y, w, h);
    }

    public void updateDimensions(){
	size.setSize(w, h);
	if (clippingShape != null){clippingShape.setFrame(x, y, w, h);}
	else {clippingShape = new Ellipse2D.Float(x, y, w, h);}
    }
    
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		if (!visible){return;}
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
            uncoef = (camera.focal+camera.altitude) / camera.focal;
            //compute region seen from this view through camera
            viewWC = camera.posx - (w/2)*uncoef;
            viewNC = camera.posy + (h/2)*uncoef;
            viewEC = camera.posx + (w/2)*uncoef;
            viewSC = camera.posy - (h/2)*uncoef;
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
                        }
                    }
                }
            }
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
        if (borderColor != null){
            g2d.setColor(borderColor);
            g2d.draw(clippingShape);
        }
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
    }

}
