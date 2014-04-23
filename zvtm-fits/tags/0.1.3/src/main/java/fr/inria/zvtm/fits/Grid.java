/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.VSegment;

/**
 * A Grid that may be overlaid on a FitsImage
 */
public class Grid extends Composite {
    private static final Color DEFAULT_COLOR = Color.CYAN;
    private static final int NSTEPS = 100; //draw ra or dec curves in NSTEPS

    private Grid(FitsImage image, int pixDistX, int pixDistY){
        if(pixDistX <= 0){
            throw new IllegalArgumentException("pixDistX: strict positive required");
        }
        if(pixDistY <= 0){
            throw new IllegalArgumentException("pixDistY: strict positive required");
        }
        //get wcs coords of the bottom left-hand pixel
        //get wcs coords of a pixel at (pixDistX, pixDistY)
        Point2D.Double origWcs = image.pix2wcs(0,0); //(ra, dec)
        Point2D.Double p1Wcs = image.pix2wcs(pixDistX, pixDistY);
        double raDiff = p1Wcs.x - origWcs.x;
        double decDiff = p1Wcs.y - origWcs.y;

       //related to image width
       int raDivs = (int)Math.ceil(image.getUnderlyingImage().getWidth()/pixDistX) + 1;

       //related to image height
       int decDivs = (int)Math.ceil(image.getUnderlyingImage().getHeight()/pixDistY) + 1;

       //fits to virtual space
       AffineTransform fits2vs = AffineTransform.getTranslateInstance(image.vx - (image.getUnderlyingImage().getWidth()/2), image.vy - (image.getUnderlyingImage().getHeight()/2));

       Point2D.Double oppWcs = image.pix2wcs(image.getWidth(), image.getHeight());

       for(int i=0; i<raDivs; i++){  
           //draw constant ra "line" ("vertical")
           double ra = origWcs.x + (i*raDiff);
           double decStep = (oppWcs.y - origWcs.y)/NSTEPS;
           for(int j=1; j<NSTEPS; j++){
               Point2D from = fits2vs.transform(image.wcs2pix(ra, 
                           origWcs.y + (j-1) * decStep), null);
               Point2D to = fits2vs.transform(image.wcs2pix(ra, 
                           origWcs.y + j*decStep), null);
               VSegment segment = new VSegment(from.getX(), from.getY(), 
                           to.getX(), to.getY(), 0, DEFAULT_COLOR);
                   segment.setStroke(new BasicStroke(2));
               addChild(segment);
           }
       }

       for(int i=0; i<decDivs; i++){
           //draw constant dec "line" ("horizontal")
           double dec = origWcs.y + (i*decDiff);
           double raStep = (oppWcs.x - origWcs.x)/NSTEPS;
           for(int j=1; j<NSTEPS; j++){
               Point2D from = fits2vs.transform(image.wcs2pix(origWcs.x + ((j-1)*raStep), dec), null);
               Point2D to = fits2vs.transform(image.wcs2pix(origWcs.x + j*raStep, dec), null);
               VSegment segment = new VSegment(from.getX(), from.getY(), 
                           to.getX(), to.getY(), 0, DEFAULT_COLOR);
               segment.setStroke(new BasicStroke(2));
               addChild(segment);
           }
       }

    }

    /**
     * @param pixDistX approximate horizontal grid spacing
     * @param pixDistY approximate vertical grid spacing
     */
    public static Grid makeGrid(FitsImage image, int pixDistX, int pixDistY){
        return new Grid(image, pixDistX, pixDistY);
    }

    //setVisible? (test)
}

