/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import edu.jhu.pha.sdss.fits.FITSImage;  
import edu.jhu.pha.sdss.fits.imageio.FITSReaderSpi;

import jsky.coords.WCSTransform;

import nom.tam.fits.FitsException;

import fr.inria.zvtm.fits.filters.HeatFilter;
import fr.inria.zvtm.fits.filters.NopFilter;
import fr.inria.zvtm.fits.filters.RainbowFilter;
import fr.inria.zvtm.fits.DefaultSampler;
import fr.inria.zvtm.fits.Grid;
import fr.inria.zvtm.fits.NomWcsKeywordProvider;
import fr.inria.zvtm.fits.Sampler;
import fr.inria.zvtm.fits.ZScale;

//provide an accessor to scale parameters
class ExFITSImage extends FITSImage{
    ExFITSImage(URL imgUrl) throws Exception{
        super(imgUrl);
    }

    double[] getScaleParams(){ 
        return new double[]{ _min, _max };
    }

}

/**
 * Basic FITS image support. Use the IVOA FITS library internally.
 */
public class FitsImage extends VImage {
    //original image (dataset preserved)
    private final ExFITSImage fitsImage;
    private ImageFilter filter;
    private ScaleMethod scaleMethod = ScaleMethod.LINEAR;
    private WCSTransform wcsTransform;
    private Grid grid = null;

    static {
        IIORegistry.getDefaultInstance().
            registerServiceProvider(new FITSReaderSpi());
    }

    /**
     * Scale methods (transfer functions).
     */
    public enum ScaleMethod {
        ASINH {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_ASINH;
            }
        },
        HISTOGRAM_EQUALIZATION {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_HISTOGRAM_EQUALIZATION;
            }
        },
        LINEAR {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_LINEAR;
            }
        },
        LOG {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_LOG;
            }
        },
        SQUARE {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_SQUARE;
            }
        },
        SQUARE_ROOT {
            @Override int toIvoaValue(){
                return FITSImage.SCALE_SQUARE_ROOT;
            }
        }
        ;
        abstract int toIvoaValue();
    }

    /** 
     * Default color filters. For more control use
     * FitsImage.setColorFilter(ImageFilter).
     */
    public enum ColorFilter {
        HEAT{
            private final ImageFilter INSTANCE = new HeatFilter();
            @Override ImageFilter getFilter(){
                return INSTANCE;
            }
        },
        RAINBOW{
            private final ImageFilter INSTANCE = new RainbowFilter();
            @Override ImageFilter getFilter(){
                return INSTANCE;
            }
        },
        NOP{
            private final ImageFilter INSTANCE = new NopFilter();
            @Override ImageFilter getFilter(){
                return INSTANCE;
            }
        };

        abstract ImageFilter getFilter();
    }

    private final URL imgUrl;

    /**
     * Creates a new FitsImage.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z-index
     * @param imgUrl image location
     * @param scaleFactor scale factor
     * @param useDataMinMax use the FITS header items DATAMIN and DATAMAX to
     * scale images (will produce a blank image if undefined).
     */
    public FitsImage(double x, double y, int z, URL imgUrl, double scaleFactor,
            boolean useDataMinMax) throws IOException {
        super(x,y,z,new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB),scaleFactor);
        this.imgUrl = imgUrl;
        filter = ColorFilter.NOP.getFilter();

        try{
            fitsImage = new ExFITSImage(imgUrl);
            wcsTransform = new WCSTransform(new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader()));
        } catch(Exception e){
            throw new Error(e);
        }
        fitsImage.setScaleMethod(scaleMethod.toIvoaValue());
        if(useDataMinMax){
            try{
                double min = fitsImage.getImageHDU().getMinimumValue();
                double max = fitsImage.getImageHDU().getMaximumValue();
                fitsImage.rescale(min, max, min/2. + max/2.); 
            } catch (Exception fe){
                System.err.println("image rescale failed: " + fe);
            }
        }
        recreateDisplayImage();
    }

    /**
     * Creates a new FitsImage
     */
    public FitsImage(double x, double y, int z, URL imgUrl, double scaleFactor) throws IOException{
        this(x,y,z,imgUrl,scaleFactor,false);
    }

    /**
     * Creates a new FitsImage.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z-index
     * @param imgUrl image location
     */
    public FitsImage(double x, double y, int z, URL imgUrl) throws IOException{
        this(x,y,z,imgUrl,1);
    }

    /**
     * Sets the image scale method.
     * @param scaleMethod the new scale method.
     */
    public void setScaleMethod(ScaleMethod scaleMethod){
            this.scaleMethod = scaleMethod;
            fitsImage.setScaleMethod(scaleMethod.toIvoaValue());
            recreateDisplayImage();
    }

    /**
     * Gets the scale method used by this FitsImage
     */
    public ScaleMethod getScaleMethod(){
        return scaleMethod; 
    }

    /**
     * Returns an array containing {min, max}
     */
    public double[] getScaleParams(){
        return fitsImage.getScaleParams();
    }

    public URL getImageLocation(){
        return imgUrl;
    }

    /**
     * Gets the IVOA Fits image object that underlies this image.
     */
    public FITSImage getUnderlyingImage(){
        return fitsImage;
    }

    /**
     * Flexible version.
     */
    public void setColorFilter(ImageFilter filter){
        this.filter = filter;
        recreateDisplayImage();
    }

    /**
     * Rescales the image.
     * @param min minimum value - image values below the minimum will be shown
     * in black.
     * @param max maximum value - image values above the maximum will be 
     * saturated.
     */
    public void rescale(double min, double max, double sigma){
        try{
            fitsImage.rescale(min, max, sigma);
            recreateDisplayImage();
        } catch(Exception ex){
            throw new Error(ex);
        }
    }

    /**
     * Linear transfer function, bounds are obtained through
     * ZScale
     */
    public void zRescale(){
        zRescale(new DefaultSampler(fitsImage));
    }

    public void zRescale(Sampler sampler){
        double[] bounds = ZScale.computeScale(sampler);
        if(bounds == null){
            return;
        }
        fitsImage.setScaleMethod(FITSImage.SCALE_LINEAR);
        rescale(bounds[0], bounds[1], 1);
    }

    /**
     * Preset defaults version.
     */
    public void setColorFilter(ColorFilter filter){
        setColorFilter(filter.getFilter());
    }

    /** 
     * Returns the color filter used by this FitsImage
     */
    public ImageFilter getColorFilter(){
        return filter;
    }

    /**
     * Converts pixel coordinates to World Coordinates. Returns null if the WCSTransform is not valid.
     * @param x x-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     * @param y y-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     */
    public Point2D.Double pix2wcs(double x, double y){
        return wcsTransform.pix2wcs(x, y);
    }

    /**
     * Converts World Coordinates to pixel coordinates. Returns null if the WCSTransform is invalid, or if the WCS position does not fall within the image.
     */
    public Point2D.Double wcs2pix(double ra, double dec){
        return wcsTransform.wcs2pix(ra, dec);
    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }

    /**
     * May return null
     */
    public Grid getGrid(){
        return grid;
    }

    private void recreateDisplayImage(){
        ImageProducer producer = fitsImage.getSource();
        producer = new FilteredImageSource(producer, filter);
        Image filteredImage = Toolkit.getDefaultToolkit().createImage(producer);
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage compatibleImage = gc.createCompatibleImage(fitsImage.getWidth(),
                fitsImage.getHeight(), fitsImage.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(filteredImage,0,0,null);
        g.dispose();
        setImage(compatibleImage);
    }

}

