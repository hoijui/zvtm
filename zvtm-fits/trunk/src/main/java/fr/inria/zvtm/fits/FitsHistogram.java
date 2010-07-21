package fr.inria.zvtm.fits;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.VRectangle;
import edu.jhu.pha.sdss.fits.Histogram;

/**
 * Graphical representation of an histogram
 */
public class FitsHistogram extends Composite {
    private static final int DEFAULT_BIN_WIDTH = 6;

    public FitsHistogram(int[] data){
        int i = 0;
        for(int val: data){
            VRectangle bar = new VRectangle(i, val, 0, DEFAULT_BIN_WIDTH/2, val, 
                        new Color(0,0,255,127));
            bar.setBorderColor(new Color(0,0,255,180));
            addChild(bar);
            i += DEFAULT_BIN_WIDTH;
        }
    }

    public static FitsHistogram fromFitsImage(FitsImage image){
        Histogram hist = image.getUnderlyingImage().getHistogram();
        int[] data = new int[256];
        for(int i=0; i<hist.getCounts().length; ++i){
            data[i/256] += hist.getCounts()[i];
        }
        return new FitsHistogram(data);
    }

}

