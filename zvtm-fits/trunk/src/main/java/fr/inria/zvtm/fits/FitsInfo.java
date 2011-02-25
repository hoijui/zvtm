/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits;

import java.io.IOException;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

/**
 * A simple program that traverses the HDUs of a FITS
 * image and prints related information.
 */
public class FitsInfo{

    public static void main(String[] args) throws FitsException, IOException {
        if(args.length != 1){
            System.out.println("usage: FitsInfo file | url");
        }

        Fits fits = new Fits(args[0]);
        BasicHDU hdu;
        while((hdu = fits.readHDU()) != null){
            hdu.info();
            if(hdu instanceof ImageHDU){
                System.out.println("Bitpix: " + hdu.getBitPix());
                System.out.println("MinVal: " + hdu.getMinimumValue());
                System.out.println("MaxVal: " + hdu.getMaximumValue());
                //System.out.println("Axes: " + hdu.getAxes().length);
            }
        }
    }
}

