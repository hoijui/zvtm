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
import fr.inria.zvtm.glyphs.VText;

/**
 * A Grid that may be overlaid on a FitsImage
 */
public class Grid extends Composite {
    private static final Color DEFAULT_COLOR = Color.BLUE;
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
               //VText label = new VText(from.getX()+50, from.getY()+50, 0, Color.RED, "(" + ra + ", " + (decStep*(j-1) + origWcs.y) + ")" );
               //addChild(label);
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
               //VText label = new VText(from.getX()+50, from.getY()+50, 0, Color.RED, "(" + (origWcs.x + (j-1)*raStep) + ", " + dec + ")");
               //addChild(label);
           }
       }

    }

  private Grid(FitsImage image, int pixDist){
    if(pixDist <= 0){
      throw new IllegalArgumentException("pixDistX: strict positive required");
    }
    double scaleFactor = image.getScale();
    System.out.println("scaleFactor: " + scaleFactor);

    //Point2D.Double position = image.getLocation();
    double width = image.getFitsWidth()*scaleFactor;
    double height = image.getFitsHeight()*scaleFactor;


    //get wcs coords of the bottom left-hand pixel
    //get wcs coords of a pixel at (pixDistX, pixDistY)
    Point2D.Double[] pointWcs = {image.pix2wcs(0, 0), image.pix2wcs(width, 0), image.pix2wcs(0, height), image.pix2wcs(width, height)}; //(ra, dec)

    Point2D.Double min = image.pix2wcs(0, 0);
    Point2D.Double max = image.pix2wcs(0, 0);
    int i = 0;
    for(Point2D.Double point : pointWcs){
        System.out.println("pointWcs["+(i++)+"]: " + point);
        if(point.x < min.x) min.x = point.x;
        if(point.y < min.y) min.y = point.y;
        if(point.x > max.x) max.x = point.x;
        if(point.y > max.y) max.y = point.y;
    }
    System.out.println("min: " + min);
    System.out.println("max: " + max);

    Point2D.Double origWcs = image.pix2wcs(0,0); //(ra, dec)
    Point2D.Double p1Wcs = image.pix2wcs(pixDist*scaleFactor, pixDist*scaleFactor);
    double raDiff = Math.abs(p1Wcs.x - origWcs.x);
    double decDiff = Math.abs(p1Wcs.y - origWcs.y);
    System.out.println("raDiff: " + raDiff);
    System.out.println("decDiff: " + decDiff);
    raDiff = Math.ceil(raDiff*10000.d)/10000.d;
    decDiff = Math.ceil(decDiff*10000.d)/10000.d;
    
    if(raDiff < 0.001) raDiff = 0.001;
    if(decDiff < 0.001) decDiff = 0.001;

    System.out.println("raDiff: " + raDiff);
    System.out.println("decDiff: " + decDiff);

    //fits to virtual space
    AffineTransform fits2vs = AffineTransform.getTranslateInstance(image.vx - (width/2), image.vy - (height/2));

    double ii = (Math.ceil(min.x*10.d)/10.d );
    double jj;
    while(ii <= max.x){
      jj = (Math.ceil(min.y*10.d)/10.d - decDiff);
      while(jj <= max.y){
        //System.out.println(ii + ", " + jj);
        Point2D from = fits2vs.transform(image.wcs2pix(ii, jj), null);
        jj += decDiff;
        Point2D to = fits2vs.transform(image.wcs2pix(ii, jj), null);

        boolean outline_from = false;
        boolean outline_to = false;

        if(from.getX() < image.vx - width/2){
          outline_from = true;
        }
        if(from.getX() > image.vx + width/2){
          outline_from = true;
        }
        if(from.getY() < image.vy - height/2){
          outline_from = true;
        }
        if(from.getY() > image.vy + height/2){
          outline_from = true;
        }
        if(to.getX() > image.vx + width/2){
          outline_to = true;
        }
        if(to.getX() < image.vx - width/2){
          outline_to = true;
        }
        if(to.getY() > image.vy + height/2){
          outline_to = true;
        }
        if(to.getY() < image.vy - height/2){
          outline_to = true;
        }
        
        if(!outline_from && !outline_to){
          VSegment segment = new VSegment(from.getX(), from.getY(), 
          to.getX(), to.getY(), 0, DEFAULT_COLOR);
          segment.setStroke(new BasicStroke(2));
          addChild(segment);
        }else if( !(outline_from && outline_to) ){

          if(outline_from){

            cutLine(from, to, image.vx, image.vy, width, height);

          } else if(outline_to){

            cutLine(to, from, image.vx, image.vy, width, height);
            
          }

          VSegment segment = new VSegment(from.getX(), from.getY(), 
          to.getX(), to.getY(), 0, DEFAULT_COLOR);
          segment.setStroke(new BasicStroke(2));
          addChild(segment);
        }
        
      }
      ii += raDiff;
    }

    jj = (Math.ceil(min.y*10.d)/10.d );
    while(jj <= max.y){
      ii = (Math.ceil(min.x*10.d)/10.d - raDiff);
      while(ii <= max.x){
        //System.out.println(ii + ", " + jj);
        Point2D from = fits2vs.transform(image.wcs2pix(ii, jj), null);
        ii += raDiff;
        Point2D to = fits2vs.transform(image.wcs2pix(ii, jj), null);

        boolean outline_from = false;
        boolean outline_to = false;

        if(from.getX() < image.vx - width/2){
          outline_from = true;
          //from.setLocation(min.x, from.getY());
        }
        if(from.getX() > image.vx + width/2){
          outline_from = true;
          //from.setLocation(max.x, from.getY());
        }
        if(from.getY() < image.vy - height/2){
          outline_from = true;
          //from.setLocation(from.getX(), min.y);
        }
        if(from.getY() > image.vy + height/2){
          outline_from = true;
          //from.setLocation(from.getX(), max.y);
        }
        if(to.getX() > image.vx + width/2){
          outline_to = true;
          //to.setLocation(max.x, to.getY());
        }
        if(to.getX() < image.vx - width/2){
          outline_to = true;
          //to.setLocation(min.x, to.getY());
        }
        if(to.getY() > image.vy + height/2){
          outline_to = true;
          //to.setLocation(to.getX(), max.y);
        }
        if(to.getY() < image.vy - height/2){
          outline_to = true;
          //to.setLocation(to.getX(), min.y);
        }
        
        if(!outline_from && !outline_to){
          VSegment segment = new VSegment(from.getX(), from.getY(), 
          to.getX(), to.getY(), 0, DEFAULT_COLOR);
          segment.setStroke(new BasicStroke(2));
          addChild(segment);
        }else if( !(outline_from && outline_to) ){

          if(outline_from){
            cutLine(from, to, image.vx, image.vy, width, height);

          } else if(outline_to){
            cutLine(to, from, image.vx, image.vy, width, height);
          }

          VSegment segment = new VSegment(from.getX(), from.getY(), 
          to.getX(), to.getY(), 0, DEFAULT_COLOR);
          segment.setStroke(new BasicStroke(2));
          addChild(segment);
        }
      }
      jj += decDiff;
    }



    //Point2D from = fits2vs.transform(image.wcs2pix(min.getX(), min.getY()), null);
    //Point2D to = fits2vs.transform(image.wcs2pix(max.x, max.y), null);

    /*

    Point2D.Double p1Wcs = image.pix2wcs(pixDist, pixDist);
    double raDiff = p1Wcs.x - pointWcs[0].x;
    double decDiff = p1Wcs.y - pointWcs[0].y;


    System.out.println("p1Wcs: " + p1Wcs);
    System.out.println("raDiff: " + raDiff);
    System.out.println("decDiff: " + decDiff);
    */

  }

  private void cutLine(Point2D outline, Point2D inline, double vx, double vy, double width, double height){

    if(outline.getX() < vx - width/2 && outline.getY() < vy + height/2 && outline.getY() > vy - height/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                              new Point2D.Double(vx - width/2, vy + height/2), outline, inline);
      outline.setLocation(intersection.getX(), intersection.getY());

    }else if(outline.getX() > vx + width/2 && outline.getY() < vy + height/2 && outline.getY() > vy - height/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx + width/2, vy - height/2), 
                                              new Point2D.Double(vx + width/2, vy + height/2), outline, inline);
      outline.setLocation(intersection.getX(), intersection.getY());

    }else if(outline.getY() < vy + width/2 && outline.getX() < vx + width/2 && outline.getX() > vx - width/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                              new Point2D.Double(vx + width/2, vy - height/2), outline, inline);
      outline.setLocation(intersection.getX(), intersection.getY());

    }else if(outline.getY() > vy - width/2 && outline.getX() < vx + width/2 && outline.getX() > vx - width/2){
      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy + height/2), 
                                              new Point2D.Double(vx + width/2, vy + height/2), outline, inline);
      outline.setLocation(intersection.getX(), intersection.getY());

    }else if(outline.getX() < vx - width/2 && outline.getY() > vy + height/2){
      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                              new Point2D.Double(vx - width/2, vy + height/2), outline, inline);

      if(intersection.getX() == vx-width/2 || intersection.getY() == vy+height/2 ){
        outline.setLocation(intersection.getX(), intersection.getY());
      } else {
        intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy + height/2), 
                                          new Point2D.Double(vx + width/2, vy + height/2), outline, inline);
        outline.setLocation(intersection.getX(), intersection.getY());
      }

    }else if(outline.getX() < vx - width/2 && outline.getY() < vy - height/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                              new Point2D.Double(vx - width/2, vy + height/2), outline, inline);

      if(intersection.getX() == vx-width/2 || intersection.getY() == vy-height/2 ){
        outline.setLocation(intersection.getX(), intersection.getY());
      } else {
        intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                          new Point2D.Double(vx + width/2, vy - height/2), outline, inline);
        outline.setLocation(intersection.getX(), intersection.getY());
      }

    }else if(outline.getX() > vx + width/2 && outline.getY() > vy + height/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx + width/2, vy - height/2), 
                                              new Point2D.Double(vx + width/2, vy + height/2), outline, inline);

      if(intersection.getX() == vx+width/2 || intersection.getY() == vy+height/2 ){
        outline.setLocation(intersection.getX(), intersection.getY());
      } else {
        intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy + height/2), 
                                          new Point2D.Double(vx + width/2, vy + height/2), outline, inline);
        outline.setLocation(intersection.getX(), intersection.getY());
      }

    }else if(outline.getX() > vx + width/2 && outline.getY() < vy - height/2){

      Point2D intersection = getIntersectionPoint( new Point2D.Double(vx + width/2, vy - height/2), 
                                              new Point2D.Double(vx + width/2, vy + height/2), outline, inline);

      if(intersection.getX() == vx+width/2 || intersection.getY() == vy-height/2 ){
        outline.setLocation(intersection.getX(), intersection.getY());
      } else {
        intersection = getIntersectionPoint( new Point2D.Double(vx - width/2, vy - height/2), 
                                          new Point2D.Double(vx + width/2, vy - height/2), outline, inline);
        outline.setLocation(intersection.getX(), intersection.getY());
      }
    }

  }

  private Point2D getIntersectionPoint(Point2D x1, Point2D x2, Point2D x3, Point2D x4){

    double d = (x1.getX()-x2.getX())*(x3.getY()-x4.getY()) - (x1.getY()-x2.getY())*(x3.getX()-x4.getX());
    if(d == 0) return null;

    double xi = ((x3.getX()-x4.getX())*(x1.getX()*x2.getY()-x1.getY()*x2.getX())-(x1.getX()-x2.getX())*(x3.getX()*x4.getY()-x3.getY()*x4.getX()))/d;
    double yi = ((x3.getY()-x4.getY())*(x1.getX()*x2.getY()-x1.getY()*x2.getX())-(x1.getY()-x2.getY())*(x3.getX()*x4.getY()-x3.getY()*x4.getX()))/d;
    return new Point2D.Double(xi, yi);
  }

  /**
   * @param pixDistX approximate horizontal grid spacing
   * @param pixDistY approximate vertical grid spacing
   */
  public static Grid makeGrid(FitsImage image, int pixDist){
      return new Grid(image, pixDist);
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

