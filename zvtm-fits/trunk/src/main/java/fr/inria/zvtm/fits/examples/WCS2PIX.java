/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: WCSExample.java 5441 2015-03-31 10:37:57Z epietrig $
 */

package fr.inria.zvtm.fits.examples;

// import edu.jhu.pha.sdss.fits.FITSImage;

import java.awt.geom.Point2D;
import java.net.URL;
import jsky.coords.WCSTransform;
import jsky.coords.WorldCoords;

import fr.inria.zvtm.glyphs.JSkyFitsImage;
import jsky.image.fits.codec.FITSImage;
import jsky.image.fits.FITSKeywordProvider;

/**
 * Computes pixel location within a FITS image from WCS coordinates
 */
class WCS2PIX {
    public static void main(String[] args) throws Exception{
        if(args.length != 3){
            System.out.println("usage: WCS2PIX URL ra dec");
            System.out.println("(where ra and dec are... radec)");
            return;
        }
        JSkyFitsImage jfi = new JSkyFitsImage(new URL(args[0]));
        WCSTransform transform = new WCSTransform(new FITSKeywordProvider(jfi.getRawFITSImage()));
        Point2D.Double p = transform.wcs2pix(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
        System.out.println((int)p.x+" "+(int)p.y);
    }
}


// SIMBAD 274.71592, -13.83936
// DS9 WCS-SEX: 18:18:51.821, -13:50:21.70
// DS9 PIX: 1116, 851
// astropy: 274.71592, -13.83936 -> 1116.52507597   850.56945216

// JSKY-CAT:
// 1116, 851 -> 18:18:51.784, -13:50:21.69
// 18:18:51.821, -13:50:21.70 -> 1120, 846

// WCS2PIX:
// 274.71592, -13.83936 -> 1120, 847
