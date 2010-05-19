/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.glyphs;

import java.awt.Color;
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

import fr.inria.zvtm.fits.HeatFilter;
import fr.inria.zvtm.fits.NopFilter;
import fr.inria.zvtm.fits.RainbowFilter;

/**
 * Basic FITS image support. Use the IVOA FITS library internally.
 */
public class FitsImage extends VImage {
    //original image (dataset preserved)
    private final FITSImage fitsImage;
    private ImageFilter filter;
    private ScaleMethod scaleMethod = ScaleMethod.LINEAR;

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

    //Default color filters. For more control use
    //FitsImage.setColorFilter(ImageFilter)
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
     */
    public FitsImage(long x, long y, int z, URL imgUrl, float scaleFactor) throws IOException {
        super(x,y,z,new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB),scaleFactor);
        this.imgUrl = imgUrl;
        filter = new NopFilter();
        //create compatible image and use this for display
        fitsImage = (FITSImage)(ImageIO.read(imgUrl));
        fitsImage.setScaleMethod(scaleMethod.toIvoaValue());
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
            this.scaleMethod = scaleMethod;
            fitsImage.setScaleMethod(scaleMethod.toIvoaValue());
            recreateDisplayImage();
    }

    public ScaleMethod getScaleMethod(){
        return scaleMethod; 
    }

    public URL getImageLocation(){
        return imgUrl;
    }

    /**
     * Flexible version.
     */
    public void setColorFilter(ImageFilter filter){
        this.filter = filter;
        recreateDisplayImage();
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

