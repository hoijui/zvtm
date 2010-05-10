/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.glyphs;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
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

/**
 * Basic FITS image support. Use the IVOA FITS library internally.
 */
public class FitsImage extends VImage {
    //original image (dataset preserved)
    private final FITSImage fitsImage;
    private ImageFilter filter;

    static {
        IIORegistry.getDefaultInstance().
            registerServiceProvider(new FITSReaderSpi());
    }

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

    private final URL imgUrl;

    /**
     * Creates a new FitsImage.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z-index
     * @param imgUrl image location
     * @param scaleFactor scale factor
     */
    public FitsImage(long x, long y, int z, URL imgUrl, float scaleFactor) throws IOException {
        super(x,y,z,new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB),scaleFactor);
        this.imgUrl = imgUrl;
        filter = new NopFilter();
        //create compatible image and use this for display
        fitsImage = (FITSImage)(ImageIO.read(imgUrl));
        recreateDisplayImage();
    }

    /**
     * Creates a new FitsImage.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z-index
     * @param imgUrl image location
     */
    public FitsImage(long x, long y, int z, URL imgUrl) throws IOException {
        this(x,y,z,imgUrl,1);
    }

    /**
     * Sets the image scale method.
     * @param scaleMethod the new scale method.
     */
    public void setScaleMethod(ScaleMethod scaleMethod){
            fitsImage.setScaleMethod(scaleMethod.toIvoaValue());
            recreateDisplayImage();
    }

    public URL getImageLocation(){
        return imgUrl;
    }

    public void setColorFilter(ImageFilter filter){
        this.filter = filter;
        recreateDisplayImage();
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

    private static class NopFilter extends RGBImageFilter {
        public NopFilter(){
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            return rgb;
        }
    }

    private static class RedFilter extends RGBImageFilter {
            public RedFilter() {
                // The filter's operation does not depend on the
                // pixel's location, so IndexColorModels can be
                // filtered directly.
                canFilterIndexColorModel = true;
            }

            public int filterRGB(int x, int y, int rgb) {
                return rgb & 0xffff0000; //ARGB?
            }
        }
}

