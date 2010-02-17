/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Image;
import java.awt.Color;
import java.awt.RenderingHints;
import javax.swing.SwingUtilities;

import java.net.URL;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.ZPDFPageImg;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VRectProgress;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

import com.sun.pdfview.PDFPage;

/** Description of image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class PDFPageDescription extends ResourceDescription {
	
    /* necessary info about an image for instantiation */
    double scale = 1.0;
    Color strokeColor;
    Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    private volatile ZPDFPageImg glyph;
    int page = 1;

    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param sf scale factor
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param p path to resource (any valid URL)
        *@param pg page number (from 1 to N)
        *@param sc border color
        *@param pr parent Region in scene
        */
    PDFPageDescription(String id, long x, long y, int z, double sf, URL p, int pg, Color sc, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.scale = sf;
		this.setURL(p);
		this.page = pg;
        this.strokeColor = sc;
        this.parentRegion = pr;
    }
    
    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param sf scale factor
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param p path to resource (any valid URL)
        *@param pg page number (from 1 to N)
        *@param sc border color
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        *@param pr parent Region in scene
        */
    PDFPageDescription(String id, long x, long y, int z, double sf, URL p, int pg, Color sc, Object im, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.scale = sf;
		this.setURL(p);
		this.page = pg;
        this.strokeColor = sc;
        this.interpolationMethod = (im != null) ? im : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        this.parentRegion = pr;
    }
    
    /** Type of resource.
	 *@return type of resource.
	 */
	public String getType(){
	    return PDFResourceHandler.RESOURCE_TYPE_PDF;
	}

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public void createObject(final VirtualSpace vs, final boolean fadeIn){
        if (glyph == null){
            // open connection to data
            if (showFeedbackWhenFetching){
                final VRectProgress vrp = new VRectProgress(vx, vy, zindex, Math.round(200*scale), Math.round(14*scale),
                                                            Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK, vs);
                vs.addGlyph(vrp);
                //try {
                //URLConnection uc = src.openConnection();
                //int dataLength = uc.getContentLength();
                //byte[] imgData = new byte[dataLength];
                //BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
                //int bytesRead = 0;
                //while (bytesRead < dataLength-1){
                //    int av = bis.available();
                //    if (av > 0){
                //        bis.read(imgData, bytesRead, av);
                //        bytesRead += av;
                //        vrp.setProgress(bytesRead, dataLength);
                //    }
                //    //else {
                //    //    sleep(10);
                //    //}
                //}
                vrp.setProgress(10, 100);
                finishCreatingObject(vs, PDFResourceHandler.getPage(src, page), vrp, fadeIn);
                //}
                //catch(IOException e){
                //    System.err.println("Error fetching Image resource "+src.toString());
                //    e.printStackTrace();
                //}
            }
            else {
                    finishCreatingObject(vs, PDFResourceHandler.getPage(src, page), null, fadeIn);
            }
        }                
    }
    
    private void finishCreatingObject(final VirtualSpace vs, PDFPage p, VRectProgress vrp, boolean fadeIn){
        glyph = new ZPDFPageImg(vx, vy, zindex, p, 1.0f, scale);
        if (strokeColor != null){
            glyph.setBorderColor(strokeColor);
            glyph.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        }
        else {
            glyph.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);            
        }
        if (!sensitive){glyph.setSensitivity(false);}
        glyph.setInterpolationMethod(interpolationMethod);
        if (fadeIn){
            glyph.setTranslucencyValue(0f);
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    vs.addGlyph(glyph);
                }
            });
            if (showFeedbackWhenFetching){
                // remove visual feedback about loading (smoothly)
                Animation a2 = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, vrp,
                    1.0f, false, IdentityInterpolator.getInstance(), new FeedbackHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a2, false);                    
            }
            // smoothly fade glyph in
            Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                1.0f, false, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        }
        else {
            if (showFeedbackWhenFetching){
                vs.removeGlyph(vrp);
            }
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    vs.addGlyph(glyph);
                }
            });
        }
        glyph.setOwner(this);
    }
    
    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public void destroyObject(final VirtualSpace vs, boolean fadeOut){
        if (glyph != null){
            if (fadeOut){
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                    0.0f, false, IdentityInterpolator.getInstance(), new PDFPageHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
                glyph = null;
            }
            else {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        vs.removeGlyph(glyph);
                        glyph.flush();
                        glyph = null;
                    }
                });
            }
        }
    }

    public Glyph getGlyph(){
	    return glyph;
    }
    
}

class PDFPageHideAction implements EndAction {
    
    VirtualSpace vs;
    
    PDFPageHideAction(VirtualSpace vs){
	    this.vs = vs;
    }
    
    public void execute(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((ZPDFPageImg)subject).flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying PDF page " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((ZPDFPageImg)subject).flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying image " + ((Glyph)subject).hashCode() + " failed. Giving up.");
        }	
    }

}
