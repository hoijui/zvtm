/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */

package fr.inria.zvtm.fits;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.VRectangle;


import fr.inria.zvtm.fits.examples.FitsMenu;

import edu.jhu.pha.sdss.fits.FITSImage;
import edu.jhu.pha.sdss.fits.Histogram;

//import jsky.image.fits.codec.FITSImage;

/**
 * Graphical representation of an histogram
 */
public class FitsHistogram extends Composite {
    public static final double DEFAULT_BIN_WIDTH = 6;
    public static final Color DEFAULT_FILL_COLOR = new Color(0,0,255,127);
    public static final Color SELECTED_FILL_COLOR = new Color(0,0,255,210);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0,0,255,180);

    double width;
    double height = 100;
    VRectangle[] bars = new VRectangle[128];

    public FitsHistogram(int[] data, int min, int max, Color fillColor){

        width = DEFAULT_BIN_WIDTH*data.length;
        VRectangle backgrown = new VRectangle(width/2, height/2, FitsMenu.Z_BTN, width, height, Color.GRAY, Color.BLACK, 0.2f);
        addChild(backgrown);

        int i = 0;
        //int val;
        for(int j = 0; j< data.length; j++){
            //val = data[j];
            double h = (Math.sqrt(data[j]) * height) / Math.sqrt(max - min);
            int hh = (int)(h);
            hh = ( hh % 2 == 0) ? hh : hh + 1;
            VRectangle bar = new VRectangle(i+DEFAULT_BIN_WIDTH/2, (int)(hh/2), FitsMenu.Z_BTN, DEFAULT_BIN_WIDTH, (int)(hh), fillColor);
            bar.setBorderColor(DEFAULT_BORDER_COLOR);
            addChild(bar);
            bars[j] = bar;
            i += DEFAULT_BIN_WIDTH;
        }

    }

    public VRectangle[] getBars(){
        return bars;
    }

    public FitsHistogram(int[] data, int min, int max){
        this(data, min, max, DEFAULT_FILL_COLOR);
    }

    //Scale method linear
    public static FitsHistogram fromFitsImage(FitsImage image, Color fillColor){
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
        return new FitsHistogram(data, min, max, fillColor);
    }

    /*
    public static FitsHistogram fromJSkyFitsImage(FitsImage image, Color fillColor){
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
        return new FitsHistogram(data, min, max, fillColor);
    }
    */


    public double getHeight(){
        return height;
    }
    public double getWidth(){
        return width;
    }

    public static FitsHistogram fromFitsImage(FitsImage image){
        return fromFitsImage(image, DEFAULT_FILL_COLOR);
    }


}

