/*   FILE: VImage.java
 *   DATE OF CREATION:  Wed Mar 21 17:48:03 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VImageOr;

/**
 * Reorient-able, translucent Bitmap Image. This version is less efficient than all others, but it can be reoriented and made translucent.<br>
 * If the image features its own alpha channel, the rendering will combine both the embedded image's channel and the translucency settings defined through the Translucent interface.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VImage
 *@see com.xerox.VTM.glyphs.VImageOr
 *@see net.claribole.zvtm.glyphs.VImageST
 */

public class VImageOrST extends VImageOr implements Translucent {
    
    AlphaComposite acST;
    float alpha=0.5f;

    /**
     *@param img image to be displayed
     *@param or orientation
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VImageOrST(Image img, float or, float a){
	super(img, or);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param img image to be displayed
     *@param or orientation
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VImageOrST(long x, long y, int z, Image img, float or, float a){
	super(x, y, z, img, or);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public float getTranslucencyValue(){
	return alpha;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alpha == 0){return;}
        if ((pc[i].cw>1) && (pc[i].ch>1)){
            if (zoomSensitive){
                trueCoef = scaleFactor*coef;
            }
            else{
                trueCoef = scaleFactor;
            }
            // a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
            if (trueCoef!=1.0f){
                // translate
                at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                if (orient != 0){
                    // rotate
                    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                }
                // rescale
                at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
                // draw
                if (alpha < 1.0f){
                    // translucent
                    g.setComposite(acST);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].p);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].p);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                }
            }
            else {
                if (alpha < 1.0f){
                    // translucent
                    g.setComposite(acST);
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].p);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].p);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alpha == 0){return;}
        if ((pc[i].lcw>1) && (pc[i].lch>1)){
            if (zoomSensitive){
                trueCoef=scaleFactor*coef;
            }
            else{
                trueCoef=scaleFactor;
            }
            //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} 
            if (trueCoef!=1.0f){
                // translate
                at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                if (orient!=0){
                    // rotate
                    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                }
                // rescale
                at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
                // draw
                if (alpha < 1.0f){
                    // translucent
                    g.setComposite(acST);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].lp);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].lp);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                }
            }
            else {
                if (alpha < 1.0f){
                    // translucent
                    g.setComposite(acST);
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].lp);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if (drawBorder==1){
                        if (pc[i].prevMouseIn){
                            g.setColor(borderColor);
                            g.drawPolygon(pc[i].lp);
                        }
                    }
                    else if (drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(dx+dx+pc[i].lcx,dy+pc[i].lcy,1,1);
        }
    }

}
