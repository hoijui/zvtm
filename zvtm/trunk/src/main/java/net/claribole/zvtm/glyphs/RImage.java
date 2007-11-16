/*   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.GradientPaint;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VImage;

/**
 * Image with a reflection. The image itself can possibly be translucent.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VImage
 *@see com.xerox.VTM.glyphs.VImageOr
 *@see net.claribole.zvtm.glyphs.VImageST
 *@see net.claribole.zvtm.glyphs.VImageOrST
 */

public class RImage extends VImageST {
    
    // include reflection in height computation
    boolean irihc = false;
    
    /**
        *@param img image to be displayed
        */
    public RImage(Image img, float a){
        super(img, a);
        this.image = createReflection(img);
    }

    /** Construct an image at (x, y) with original scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     */
    public RImage(long x,long y,float z,Image img, float a){
        super(x, y, z, img, a);
        this.image = createReflection(img);
    }

    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
     */
    public RImage(long x, long y, float z, Image img, double scale, float a){
        super(x, y, z, img, scale, a);
        this.image = createReflection(img);
    }
    
    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
     *@param hir true if height measurement should include mirrored version of the image (doubles the image's height) - defaults to false
     *@param gp custom alpha mask (can be null, in which case the default one 0.5->0 is applied)
     */
    public RImage(long x, long y, float z, Image img, double scale, float a, boolean hir, GradientPaint gp){
        super(x, y, z, img, scale, a);
        if (gp != null){
            this.image = createReflection(img, gp);
        }
        else {
            this.image = createReflection(img);
        }
        this.irihc = hir;
        if (this.irihc){
            ///XXX:TBW
        }
    }
    
    public boolean includesReflectionInHeightComputation(){
        return irihc;
    }
    
    public Object clone(){
    	RImage res = new RImage(vx, vy, 0, image, alpha);
    	res.setWidth(vw);
    	res.setHeight(vh);
    	res.borderColor = this.borderColor;
    	res.mouseInsideColor = this.mouseInsideColor;
    	res.bColor = this.bColor;
    	res.setDrawBorderPolicy(drawBorder);
    	res.setZoomSensitive(zoomSensitive);
    	return res;
    }

    public static BufferedImage getBufferedImageFromFile(File f){
        try {
            return ImageIO.read(f);
        }
        catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }
    
    public static BufferedImage createReflection(Image src){
        return createReflection(src,
            new GradientPaint(0, 0, new Color(1.0f, 1.0f, 1.0f, 0.5f),
                0, src.getHeight(null) / 2, new Color(1.0f, 1.0f, 1.0f, 0.0f)));
    }
    
    public static BufferedImage createReflection(Image src, GradientPaint mask){
        // code strongly inspired by the book Filthy Rich Clients, by Chet Haase and Romain Guy
        int height = src.getHeight(null);
        BufferedImage target = new BufferedImage(src.getWidth(null), height * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = target.createGraphics();
         // Paints original image
        g2.drawImage(src, 0, 0, null);
        // Paints mirrored image
        g2.scale(1.0, -1.0);
        g2.drawImage(src, 0, -height - height, null);
        g2.scale(1.0, -1.0);
        // Move to the origin of the clone
        g2.translate(0, height);
        // Sets the alpha mask
        g2.setPaint(mask);
        // Sets the alpha composite
        g2.setComposite(java.awt.AlphaComposite.DstIn);
        // Paints the mask
        g2.fillRect(0, 0, src.getWidth(null), height);
        g2.dispose();
        return target;
    }

}





