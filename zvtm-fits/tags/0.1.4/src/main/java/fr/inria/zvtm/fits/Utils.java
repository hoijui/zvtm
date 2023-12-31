/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.RGBImageFilter;

import java.util.Scanner;

/**
 * Miscellaneous utility functions
 */
public class Utils {
    private static final int NB_POINTS = 20;

    public static String VERSION;

    static {
        Scanner sc = new Scanner(Utils.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                Utils.VERSION = sc.next().trim();
            }
        }
    }

    private Utils(){}

    public static MultipleGradientPaint makeGradient(RGBImageFilter filter){
         Point2D start = new Point2D.Float(0, 0);
         Point2D end = new Point2D.Float(250, 0);
         float fractions[] = new float[NB_POINTS];
         Color colors[] = new java.awt.Color[NB_POINTS];

         for(int i=0; i<NB_POINTS; ++i){
             float fraction = (float)i/(NB_POINTS-1);
             int cval = (int)(255*fraction);
             int color = cval | (cval << 8) | (cval << 16);
             fractions[i] = fraction;
             colors[i] = new Color(filter.filterRGB(0,0,color));
         }

        return new LinearGradientPaint(start, end, fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    }
}

