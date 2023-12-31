/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Vector;

import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.Histogram;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;

import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VImageOr;

import jsky.image.fits.codec.FITSImage;
import jsky.image.ImageProcessor;
import jsky.image.ImageLookup;

//Fits support provided by JSky instead of IVOA FITS
//Note: JSkyFitsImage requires JAI (Java Advanced Imaging)

public class JSkyFitsImage extends VImage {

    FITSImage fitsImage;

    URL furl;
    final ImageProcessor proc;

    double originLowCut;
    double originHighCut;

    /** Construct an image at (0, 0) with original scale.
     *@param file path to FITS file
     */
    public JSkyFitsImage(String file) throws MalformedURLException {
        this(new URL(file));
    }

    /** Construct an image at (0, 0) with original scale.
     *@param fitsURL FITS file URL
     */
    public JSkyFitsImage(URL fitsURL){
        this(0, 0, 0, fitsURL, 1.0, 1.0f);
    }

    /** Construct an image at (x, y) with original scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param fitsURL FITS file URL
     */
    public JSkyFitsImage(double x, double y, int z, URL fitsURL){
        this(x, y, z, fitsURL, 1.0, 1.0f);
    }

    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param fitsURL FITS file URL
     *@param scale scaleFactor w.r.t original image size
     */
    public JSkyFitsImage(double x, double y, int z, URL fitsURL, double scale){
        this(x, y, z, fitsURL, scale, 1.0f);
    }

    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param fitsURL FITS file URL
     *@param scale scaleFactor w.r.t original image size
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public JSkyFitsImage(double x, double y, int z, URL fitsURL, double scale, float alpha){
        super(x, y, z, null, scale, alpha);
        this.furl = fitsURL;
        try {
            String urlStr = furl.toString();
            if(urlStr.indexOf("file:") == 0){
                fitsImage = new FITSImage(urlStr.substring(urlStr.indexOf(":")+1));
            }
            else {
                fitsImage = new FITSImage(urlStr);
            }
            RenderedImageAdapter ria = new RenderedImageAdapter(fitsImage);
            Rectangle2D.Double region = new Rectangle2D.Double(0,0, fitsImage.getWidth(), fitsImage.getHeight());
            try{
                proc = new ImageProcessor(ria, region);
                originLowCut = proc.getLowCut();
                originHighCut = proc.getHighCut();
                updateDisplayedImage();
                vw = image.getWidth(null) * scale;
                vh = image.getHeight(null) * scale;
            }
            catch(java.lang.IllegalArgumentException ie){
                throw new Error("Could not create ImageProcesor: " + ie);
            }
        }
        catch (Exception e){
            throw new Error("Could not create FitsImage: " + e);
        }
    }

    /**
     * Get the underlying JSky FITSImage instance.
     */
    public FITSImage getRawFITSImage(){
        return fitsImage;
    }

    /**
     * Get URL of underlying FITS Image.
     */
    public URL getFITSImageURL(){
        return furl;
    }

    /**
     * Get min cut level.
     */
    public double getMinValue(){
        return proc.getMinValue();
    }

    /**
     * Get max cut level.
     */
    public double getMaxValue(){
        return proc.getMaxValue();
    }

    /**
     * Get the scale algorithm used to render this FITS image.
     */
    public ScaleAlgorithm getScaleAlgorithm(){
        switch (proc.getScaleAlgorithm()){
            case ImageLookup.HIST_EQ:{return ScaleAlgorithm.HIST_EQ;}
            case ImageLookup.LINEAR_SCALE:{return ScaleAlgorithm.LINEAR;}
            case ImageLookup.LOG_SCALE:{return ScaleAlgorithm.LOG;}
            case ImageLookup.SQRT_SCALE:{return ScaleAlgorithm.SQRT;}
            default:{return ScaleAlgorithm.HIST_EQ;}
        }
    }

    /**
     * Does nothing on purpose.
     * The BufferedImage should only be updated from the FITS image through #updateDisplayedImage()
     */
    @Override
    public void setImage(Image i){
        //
        return;
    }

    /**
     * Update the image to be rendered on screen. This method is called whenever the FITS image's content changes, or when the scale algorithm or color lookup table is changed.
     */
    public void updateDisplayedImage(){
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        image = gc.createCompatibleImage(fitsImage.getWidth(), fitsImage.getHeight(), Transparency.TRANSLUCENT);
        Graphics2D g2 = ((BufferedImage)image).createGraphics();
        g2.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(1,1));
        g2.dispose();
        image.setAccelerationPriority(1);
    }

    /**
     * Sets the color lookup table.
     *@param tableName Name of color lookup table to be used to render this FITS image. Currently accepted values include: "Standard", "Aips0", "Background", "Color", "Red", "Green", "Blue", "Blulut", "Ramp", "Real", "Heat", "Light", "Pastel", "Smooth", "Idl2", "Idl4", "Idl5", "Idl6", "Idl11", "Idl12", "Idl14", "Idl15", "Isophot", "Manycolor", "Stairs8", "Stairs9", "Random", "Random1", "Random2", "Random3", "Random4", "Rainbow", "Rainbow1", "Rainbow2", "Rainbow3", "Rainbow4".
     *@param updateDisplay true if the ZVTM views observing this image should be repainted at once.
     */
    public void setColorLookupTable(String tableName, boolean updateDisplay){
        proc.setColorLookupTable(tableName);
        proc.update();
        if (updateDisplay){
            updateDisplayedImage();
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    /** Get the color lookup table currently set for this image.
     *@return the color looktup table's name
     */
    public String getColorLookupTable(){
        return proc.getColorLookupTableName();
    }

    /**
     * Sets the scale algorithm used to render this FITS image.
     *@param updateDisplay true if the ZVTM views observing this image should be repainted at once.
     */
    public void setScaleAlgorithm(ScaleAlgorithm algorithm, boolean updateDisplay){
        proc.setScaleAlgorithm(algorithm.toJSkyValue());
        proc.update();
        if (updateDisplay){
            updateDisplayedImage();
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    /**
     * Sets the cut levels for this image.
     *@param updateDisplay true if the ZVTM views observing this image should be repainted at once.
     */
    public void setCutLevels(double lowCut, double highCut, boolean updateDisplay){
        proc.setCutLevels(lowCut, highCut);
        proc.update();
        if (updateDisplay){
            updateDisplayedImage();
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    /**
     * Returns an array containing [lowCut, highCut]
     */
    public double[] getCutLevels(){
        return new double[]{proc.getLowCut(), proc.getHighCut()};
    }

    public double[] getOriginCutLevels(){
        return new double[]{originLowCut, originHighCut};
    }

    /**
     * Sets the image cut levels automatically using median filtering on the given area of the image.
     *@param updateDisplay true if the ZVTM views observing this image should be repainted at once.
     */
    public void autoSetCutLevels(Rectangle2D.Double rect, boolean updateDisplay){
        proc.autoSetCutLevels(rect);
        proc.update();
        if (updateDisplay){
            updateDisplayedImage();
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    /**
     * Sets the image cut levels automatically using median filtering.
     *@param updateDisplay true if the ZVTM views observing this image should be repainted at once.
     */
    public void autoSetCutLevels(boolean updateDisplay){
        autoSetCutLevels(new Rectangle2D.Double(0,0,fitsImage.getWidth(),fitsImage.getHeight()), updateDisplay);
    }

    public int getDataType(){
        return proc.getSourceImage().getSampleModel().getDataType();
    }

    public Histogram getHistogram(int numValues){
        Rectangle2D.Double region = new Rectangle2D.Double(0,0, fitsImage.getWidth(), fitsImage.getHeight());
        ROI roi = new ROIShape(region);
        return proc.getHistogram(numValues, roi);
    }

    public enum ScaleAlgorithm {
        LINEAR{
            @Override int toJSkyValue(){
                return ImageLookup.LINEAR_SCALE;
            }
        },
        LOG{
            @Override int toJSkyValue(){
                return ImageLookup.LOG_SCALE;
            }
        },
        HIST_EQ{
            @Override int toJSkyValue(){
                return ImageLookup.HIST_EQ;
            }
        },
        SQRT{
            @Override int toJSkyValue(){
                return ImageLookup.SQRT_SCALE;
            }
        };
        abstract int toJSkyValue();
    }

}
