package fr.inria.zvtm.glyphs;

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

    public FitsImage(long x, long y, int z, URL imgUrl) throws IOException {
        super(x,y,z,ImageIO.read(imgUrl));
    }

    public void setScaleMethod(ScaleMethod scaleMethod){
        if(image instanceof FITSImage){
            ((FITSImage)image).setScaleMethod(scaleMethod.toIvoaValue());
        }
    }

}

