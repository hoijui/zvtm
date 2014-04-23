/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits;

import java.util.Arrays;
import java.io.IOException;
import nom.tam.fits.FitsException;
import edu.jhu.pha.sdss.fits.FITSImage; 

/**
 * Implements a partial version of z-scaling.
 * Iterative refinement is not implemented.
 * A FAQ entry about zscale can be found here: 
 * http://iraf.net/article.php/20051205162333315
 */
public class ZScale {
    private static final int MIN_NPIXELS = 5;
    private static final double MAX_REJECT = 0.5; //reject half the pixels at most

    /**
     * @return a 2-element array containing (lowcut, highcut),
     * or null if it could not compute cut values.
     */
    public static double[] computeScale(Sampler sampler){
        double contrast = 0.6; //default 1 

        double sample[] = sampler.getSample(2000);
        if(sample.length < 2){
            System.err.println("insufficient image data");
            return null;
        }
        Arrays.sort(sample);
        double zmin = sample[0];
        double zmax = sample[sample.length - 1];
        System.err.println("zmin = " + zmin + ", zmax = " + zmax);
        int centerIdx = (sample.length - 1) / 2;
        double median = sample[centerIdx];
        int minPix = (int)Math.max(MIN_NPIXELS, sample.length * MAX_REJECT);

        //chop off sample ends 
        int istart = (int)((sample.length - 1) * 0.05);
        int iend = (int)((sample.length - 1) * 0.95);
        int npoints = iend - istart + 1;
        if(npoints < 2){
            System.err.println("insufficient image data");
            return null;
        }

        //fit a line
        double[] lineParms = fitLine(iota(0,1,sample.length), sample);
        
        //Normally, we should iteratively exclude points from the 
        //data set and test if the fitting is correct
        
        //compute lowCut and highCut
        double lowCut = median + (lineParms[0]/contrast)*(1-centerIdx);
        double highCut = median + (lineParms[0]/contrast)*((npoints)-centerIdx);
        
        System.err.println("zscale: lowCut = " + lowCut + ", highCut = " + highCut);

        return new double[]{lowCut, highCut};
}

    /**
     * @return a 2-element array containing (lowcut, highcut),
     * or null if it could not compute cut values.
     */
    public static double[] computeScale(FITSImage image){
        return computeScale(new DefaultSampler(image));
    }

    /**
     * Returns an 2-element array containing slope, intercept.
     */
    private static double[] fitLine(double[] xdata, double[] ydata){
        if(xdata.length != ydata.length){
            throw new Error("xdata and ydata should have an equal size");
        }
        int n = xdata.length;
        double sumx = 0, sumy = 0, sumx2 = 0;
        for(int i=0; i<n; ++i){
            sumx += xdata[i];
            sumx2 += (xdata[i] * xdata[i]);
            sumy += ydata[i];
        }
        double xavg = sumx / n;
        double yavg = sumy / n;

        double xxbar = 0.0, xybar = 0.0;
        for (int i=0; i<n; ++i) {
            xxbar += (xdata[i] - xavg) * (xdata[i] - xavg);
            xybar += (xdata[i] - xavg) * (ydata[i] - yavg);
        }

        double slope = xybar / xxbar;
        double intercept = yavg - slope * xavg;
        return new double[]{slope, intercept};
    }

    private static double[] iota(double start, double delta, int n){
        double[] retval = new double[n];
        retval[0] = start;
        for(int i=1; i<retval.length; ++i){
            retval[i] = retval[i-1] + delta;
        }
        return retval;
    }

    private void testLinReg(){
        double[] xdata = {1, 2, 3, 3.9, 4, 4.5, 5, 6.2};
        double[] ydata = {5, 3.1, 0.8, 0.1, -1.2, -2.8, -3, -5};  
        double[] lineParms = fitLine(xdata, ydata);
        System.out.println("y = " + lineParms[0] + " * x + " + lineParms[1]);
        //we expect something like y = -2x + 7
    }

    public static void main(String[] args) throws Exception{
        FITSImage img = new FITSImage(args[0]);
        computeScale(img);
    }
}

