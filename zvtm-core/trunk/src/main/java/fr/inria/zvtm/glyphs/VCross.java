/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Cross, with modifiable width and height.
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VRectangle
 */

public class VCross<T> extends VRectangle {

    public VCross(){
        this(0, 0, 0, 10, 10, Color.WHITE, Color.BLACK, 1f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param w width in virtual space
        *@param h height in virtual space
        *@param c cross color
        *@param bc border color
        */
    public VCross(double x, double y, int z, double w, double h, Color c){
        this(x, y, z, w, h, c, Color.BLACK, 1f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param w width in virtual space
        *@param h height in virtual space
        *@param c cross color
        *@param bc border color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VCross(double x, double y, int z, double w, double h, Color c, Color bc, float alpha){
        super(x, y, z, w, h, c, bc, alpha);
        setDrawBorder(false);
    }

    @Override
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].cw == 1) && (pc[i].ch==1)){
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
        else {
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx,dy+pc[i].cy+pc[i].ch);
                        g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
                        ((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
                            // [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
                            // the rectangle, in which case the border would not be visible;
                            // the fact that the rectangle intersects the viewport has already been tested by the main
                            // clipping algorithm
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                        }
                    }
                    g.setStroke(stdS);
                }
                else {
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx,dy+pc[i].cy+pc[i].ch);
                        g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
                        ((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
                            // [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
                            // the rectangle, in which case the border would not be visible;
                            // the fact that the rectangle intersects the viewport has already been tested by the main
                            // clipping algorithm
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                        }
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx,dy+pc[i].cy+pc[i].ch);
                        g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
                        ((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
                            // [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
                            // the rectangle, in which case the border would not be visible;
                            // the fact that the rectangle intersects the viewport has already been tested by the main
                            // clipping algorithm
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                        }
                    }
                    g.setStroke(stdS);
                }
                else {
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,dx+pc[i].cx,dy+pc[i].cy+pc[i].ch);
                        g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].cx-pc[i].cw)>0) || ((dy+pc[i].cy-pc[i].ch)>0) ||
                        ((dx+pc[i].cx-pc[i].cw+2*pc[i].cw-1)<vW) || ((dy+pc[i].cy-pc[i].ch+2*pc[i].ch-1)<vH)){
                            // [C1] draw complex border only if it is actually visible (just test that viewport is not fully within
                            // the rectangle, in which case the border would not be visible;
                            // the fact that the rectangle intersects the viewport has already been tested by the main
                            // clipping algorithm
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].lcw==1) && (pc[i].lch==1)){
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
            }
        }
        else {
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx,dy+pc[i].lcy+pc[i].lch);
                        g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
                        ((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
                            // see [C1] above for explanations about this test
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                        }
                    }
                    g.setStroke(stdS);
                }
                else {
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx,dy+pc[i].lcy+pc[i].lch);
                        g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
                        ((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
                            // see [C1] above for explanations about this test
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                        }
                    }
                    g.setStroke(stdS);
                }
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx,dy+pc[i].lcy+pc[i].lch);
                        g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
                        ((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
                            // see [C1] above for explanations about this test
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                        }
                    }
                    g.setStroke(stdS);
                }
                else {
                    if (filled){
                        g.setColor(this.color);
                        g.drawLine(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,dx+pc[i].lcx,dy+pc[i].lcy+pc[i].lch);
                        g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy);
                    }
                    if (paintBorder){
                        if (((dx+pc[i].lcx-pc[i].lcw)>0) || ((dy+pc[i].lcy-pc[i].lch)>0) ||
                        ((dx+pc[i].lcx-pc[i].lcw+2*pc[i].lcw-1)<vW) || ((dy+pc[i].lcy-pc[i].lch+2*pc[i].lch-1)<vH)){
                            // see [C1] above for explanations about this test
                            g.setColor(borderColor);
                            g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Shape getJava2DShape(){
        //XXX: not implemented yet
        return null;
    }

    @Override
    public Object clone(){
        VCross res = new VCross(vx, vy, vz, vw, vh, color, borderColor, getTranslucencyValue());
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }

}

