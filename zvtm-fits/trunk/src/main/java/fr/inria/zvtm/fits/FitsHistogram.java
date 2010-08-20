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
    private static final double DEFAULT_BIN_WIDTH = 8;

    public FitsHistogram(int[] data, int min, int max){
        int i = 0;
        double height = 100;
        for(int val: data){
            double h = (Math.sqrt(val) * height) / Math.sqrt(max - min); 
            VRectangle bar = new VRectangle(i, h, 0, DEFAULT_BIN_WIDTH/2, h, 
                    new Color(0,0,255,127));
            bar.setBorderColor(new Color(0,0,255,180));
            addChild(bar);
            i += DEFAULT_BIN_WIDTH;
        }
    }

    public static FitsHistogram fromFitsImage(FitsImage image){
        Histogram hist = image.getUnderlyingImage().getHistogram();
        int[] data = new int[128]; 
        for(int i=0; i<hist.getCounts().length; ++i){
            data[i/(hist.getCounts().length / data.length)] += hist.getCounts()[i];
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<data.length; ++j){
            if (data[j] < min){
                min = data[j];
            }
            if(data[j] > max){
                max = data[j];
            }
        }
        return new FitsHistogram(data, min, max);
    }
}

