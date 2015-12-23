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
import java.awt.AlphaComposite;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.engine.VirtualSpace;

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
        repaint(true);
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
        repaint(true);
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
        return ((cx >= x) && (cx <= x+w) &&
            (cy >= y+borderWidth) && (cy <= y+barHeight+borderWidth));
    }

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
         
        g2d.setClip(x-tx, y-ty, bw, bh);       
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
        //only diff with CameraPortal :/
        g2d.setColor(barColor);
        g2d.fillRect(x-tx+backBufferTX+(int)borderWidth, y-ty+backBufferTY+(int)borderWidth, w-2*(int)borderWidth, barHeight);
        //
        if (borderColor != null){
            g2d.setColor(borderColor);
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.drawRect(x-tx+backBufferTX+borderWidthXYOff, y-ty+backBufferTY+borderWidthXYOff,
                w-borderWidthWHOff, h-borderWidthWHOff);
            g2d.setStroke(standardStroke);
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
    }

}
