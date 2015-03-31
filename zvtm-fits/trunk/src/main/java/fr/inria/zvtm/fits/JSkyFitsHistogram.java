/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */

package fr.inria.zvtm.fits;


import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zvtm.glyphs.VRectangle;

import fr.inria.zvtm.fits.examples.JSkyFitsMenu;

import javax.media.jai.Histogram;
//import javax.media.jai.ROI;
//import javax.media.jai.ROIShape;

import jsky.image.BasicImageReadableProcessor;
import jsky.image.ImageChangeEvent;
import jsky.image.ImageProcessor;

/**
 * Graphical representation of an histogram
 */
public class JSkyFitsHistogram extends Composite {
    public static final double DEFAULT_BIN_WIDTH = 6;
    public static final Color DEFAULT_FILL_COLOR = new Color(0,0,255,127);
    public static final Color SELECTED_FILL_COLOR = new Color(0,0,255,210);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0,0,255,180);

    public static final int HISTOGRAM_SIZE = 128;

    double width;
    double height = 100;
    VRectangle[] bars = new VRectangle[128];


    public JSkyFitsHistogram(int[] data, int min, int max, Color fillColor){

        width = DEFAULT_BIN_WIDTH*data.length;
        VRectangle backgrown = new VRectangle(width/2, height/2, JSkyFitsMenu.Z_BTN, width, height, Color.GRAY, Color.BLACK, 0.2f);
        addChild(backgrown);

        int i = 0;
        //int val;
        for(int j = 0; j< data.length; j++){
            //val = data[j];
            double h = (Math.sqrt(data[j]) * height) / Math.sqrt(max - min);
            int hh = (int)(h);
            hh = ( hh % 2 == 0) ? hh : hh + 1;
            VRectangle bar = new VRectangle(i+DEFAULT_BIN_WIDTH/2, (int)(hh/2), JSkyFitsMenu.Z_BTN, DEFAULT_BIN_WIDTH, (int)(hh), fillColor);
            bar.setBorderColor(DEFAULT_BORDER_COLOR);
            addChild(bar);
            bars[j] = bar;
            i += DEFAULT_BIN_WIDTH;
        }

    }

    public VRectangle[] getBars(){
        return bars;
    }

    public JSkyFitsHistogram(int[] data, int min, int max){
        this(data, min, max, DEFAULT_FILL_COLOR);
    }

    //Scale method linear
    public static JSkyFitsHistogram fromFitsImage(JSkyFitsImage image, Color fillColor){
    	/*
        Histogram hist = image.getHistogram( 2048 );//HISTOGRAM_SIZE);
        int[] data = new int[HISTOGRAM_SIZE];

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        System.out.println("Num Bands: " + hist.getNumBands());
        System.out.println("Num Bins: " + hist.getNumBins().length);
        for(int i = 0; i < hist.getNumBins().length; i++){
        	System.out.println(hist.getNumBins()[i]);
        }
        System.out.println("Num low values: " + hist.getLowValue().length);
        for(int i = 0; i < hist.getLowValue().length; i++){
        	System.out.println(hist.getLowValue()[i]);
        }
        System.out.println("Num high values: " + hist.getLowValue().length);
        for(int i = 0; i < hist.getHighValue().length; i++){
        	System.out.println(hist.getHighValue()[i]);
        }
        */

	/**
	 *
	 * Plot a histogram for the image
	 */

        double[] cutlevels = image.getCutLevels();

		double lowCut = cutlevels[0];
        double highCut = cutlevels[1];

        int numValues = HISTOGRAM_SIZE;
        int dataType = image.getDataType();
        boolean isFloatingPoint = (dataType == DataBuffer.TYPE_FLOAT || dataType == DataBuffer.TYPE_DOUBLE);
        double n = highCut - lowCut;

        if (n < numValues && !isFloatingPoint) {
            numValues = (int) n;
        }

        if (numValues <= 0) {
        	System.out.println("return numValues: " + numValues);
            //chart.getXYPlot().setDataset(new SimpleDataset());
            //return;
        }

        double[] xValues = new double[numValues];
        int[] yValues = new int[numValues];
        double m = lowCut;
        double factor = n / numValues;

        // the X values are the pixel values
        // the Y values are the number of pixels in a given range
        for (int i = 0; i < numValues; i++, m += factor) {
            xValues[i] = m;
            yValues[i] = 0;
        }
        if (factor >= 0.0) {

            Histogram histogram = image.getHistogram(numValues);
            yValues = histogram.getBins(0);
            //chart.getXYPlot().setDataset(new SimpleDataset(xValues, yValues));
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<yValues.length; ++j){
            if (yValues[j] < min){
                min = yValues[j];
            }
            if(yValues[j] > max){
                max = yValues[j];
            }
        }





        /*
        for(int i=0; i<HISTOGRAM_SIZE; ++i){
            //data[i/(hist.getCounts().length / data.length)] += hist.getCounts()[i];


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
        */
        return new JSkyFitsHistogram(yValues, min, max, fillColor);
    }


    public double getHeight(){
        return height;
    }
    public double getWidth(){
        return width;
    }


    public static JSkyFitsHistogram fromFitsImage(JSkyFitsImage image){
        return fromFitsImage(image, DEFAULT_FILL_COLOR);
    }


}