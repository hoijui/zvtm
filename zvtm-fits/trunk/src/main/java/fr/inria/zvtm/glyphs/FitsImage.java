/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.glyphs;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
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

    private void recreateDisplayImage(){
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage compatibleImage = gc.createCompatibleImage(fitsImage.getWidth(),
                fitsImage.getHeight(), fitsImage.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(fitsImage,0,0,null);
        g.dispose();
        setImage(compatibleImage);
    }
}

