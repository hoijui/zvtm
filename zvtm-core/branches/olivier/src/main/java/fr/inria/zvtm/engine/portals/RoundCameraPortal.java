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
import java.awt.AlphaComposite;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.engine.VirtualSpace;

/**A portal showing what is seen through a camera. Shape: circular.
   The Camera should not be used in any other View or Portal.*/

public class RoundCameraPortal extends CameraPortal {

    Ellipse2D clippingShape, borderShape, borderPickingShape;

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
        int xx = x;
        int yy = y;
        if (bufferDraw){
            xx = 0; yy = 0;
        }
        clippingShape = new Ellipse2D.Float(xx, yy, w, h);
        borderShape = new Ellipse2D.Float(xx+borderWidthXYOff, yy+borderWidthXYOff, w-borderWidthWHOff, h-borderWidthWHOff);
        borderPickingShape = new Ellipse2D.Float(xx+2*borderWidthXYOff, yy+2*borderWidthXYOff, w-2*borderWidthWHOff, h-2*borderWidthWHOff);
    }

    private void doSetFrame() {
        int xx = x;
        int yy = y;
        if (bufferDraw){
            xx = 0; yy = 0;
        }
        clippingShape.setFrame(xx, yy, w, h);
        borderShape.setFrame(xx+borderWidthXYOff, yy+borderWidthXYOff, w-borderWidthWHOff, h-borderWidthWHOff);
        borderPickingShape.setFrame(xx+2*borderWidthXYOff, yy+2*borderWidthXYOff, w-2*borderWidthWHOff, h-2*borderWidthWHOff);
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
        if (bufferDraw){
            cx = cx-x;
            cy = cy-y;
        }
	    return borderShape.contains(cx, cy);
    }

    @Override
    public boolean coordInsideBorder(int cx, int cy){
        if (bufferDraw){
            cx = cx-x;
            cy = cy-y;
        }
        return (clippingShape.contains(cx, cy) && !borderPickingShape.contains(cx, cy));
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
        repaint(true);
    }

    @Override
    public void setBorderWidth(float w){
        super.setBorderWidth(w);
        doSetFrame();
    }

    // FIXME: cluster && big portal in bufferDraw !!! (clipping !!!)
    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        bufferDraw = owningView.getDrawPortalsOffScreen();
        if (bufferDraw){
            //System.out.println("draw portal.... ? "+ repaintASAP);
            updateOffscreenBuffer(viewWidth, viewHeight);
            if (wasOutOfView &&
                !(x+w < 0 || y+h < 0 || x >= viewWidth || y >= viewHeight)){
                repaintASAP=true;
                wasOutOfView=false;
            }
            if (repaintASAP){
                repaintASAP=false;
                // System.out.println("draw portal.... "+x+" "+y+" "+w+" "+h+" "+buffx+" "+backBufferW);
                try {
                    paintOnBack(stableRefToBackBufferGraphics, viewWidth, viewHeight, x, y);
                }
                catch (NullPointerException ex0){
                    //ex0.printStackTrace(); 
                }
            }
        }
        else{
            paintOnBack(g2d, viewWidth, viewHeight, 0, 0);
        }
    }

    @Override
    protected void paintOnBack(Graphics2D g2d, int viewWidth, int viewHeight, int tx, int ty){
        if (!visible){return;}
        //Check if the portal is out of the view
        if (x+w < 0 || y+h < 0 || x >= viewWidth || y >= viewHeight){
            // clear the buffer ???
            wasOutOfView = true;
            return;
        }
        wasOutOfView=false;
        int bw = w;
        int bh = h;
        if (bufferDraw){
            bw = backBufferW;
            bh = backBufferH;
        }
        if (alphaC != null){
            // portal is not opaque
            if (alphaC.getAlpha() == 0){
                // portal is totally transparent
                if (bufferDraw){
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                    g2d.fillRect(0, 0, w, h);
                    g2d.setComposite(Translucent.acO);
                }
                return;
            }
            if (!bufferDraw){
                g2d.setComposite(alphaC);
            }
        }

        g2d.setClip(clippingShape);
        if (bufferDraw){
            if (bkgColor == null){
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                g2d.fillRect(0, 0, bw, bh);
                g2d.setComposite(Translucent.acO);
            }
        }
        if (bkgColor != null){
            g2d.setColor(bkgColor);
            g2d.fillRect(x-tx, y-ty, bw, bh);
        }
        standardStroke = g2d.getStroke();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        standardTransform = g2d.getTransform();
        for (int u=0; u < cameras.size(); u++){
            Camera cam = cameras.elementAt(u);
            VirtualSpace spa = cameraSpaces.elementAt(u);
            int idx = camIndexs.elementAt(u);
            drawnGlyphs = spa.getDrawnGlyphs(idx);
            synchronized(drawnGlyphs){
                drawnGlyphs.removeAllElements();
                duncoef = (cam.focal+cam.altitude) / cam.focal;
                //compute region seen from this view through camera
                viewWC = cam.vx - (w/2d) * duncoef - (double)(backBufferTX)* duncoef ;
                viewNC = cam.vy + (h/2d) * duncoef + (double)(backBufferTY)* duncoef;
                viewEC =  viewWC + (bw/1d)  * duncoef;
                viewSC = viewNC - (bh/1d) * duncoef;
                gll = spa.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        synchronized(gll[i]){
                            if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cam)){
                                //if glyph is at least partially visible in the reg. seen from this view, display
                                gll[i].project(cam, size); // an invisible glyph should still be projected
                                if (gll[i].isVisible()){      // as it can be sensitive
                                    gll[i].draw(g2d, bw, bh, idx, standardStroke, standardTransform,
                                        x-tx+backBufferTX, y-ty+backBufferTY);
                                }
                                spa.drewGlyph(gll[i], idx);
                            }
                        }
                    }
                }
            }
        }
        
        if (borderColor != null){
            g2d.setColor(borderColor);
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.draw(clippingShape);
            g2d.setStroke(standardStroke);
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
    }

}
