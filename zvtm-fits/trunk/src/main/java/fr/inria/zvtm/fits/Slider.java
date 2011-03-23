/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits;

import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;

public class Slider extends Composite {
    private static final double BAR_HEIGHT = 20;
    private static final double BAR_WIDTH = 400;

    VRectangle bar;
    VPolygon tick;

    public Slider(){
        bar = new VRectangle(0,0,0,BAR_WIDTH,BAR_HEIGHT,new Color(100,30,90,180));
        tick = makeTick();
        tick.move(-BAR_WIDTH/2 + 20, BAR_HEIGHT/2);
        addChild(bar);
        addChild(tick);
    }

    //Slider value, in [0,1]
    public double getValue(){
        return (tick.vx - (this.vx - bar.getWidth()/2)) / bar.getWidth();
    }

    //val in [0,1]
    public void setTickVal(double val){
        //clip to [0,1]
        double finalVal = Math.max(0, val);
        finalVal = Math.min(finalVal, 1);

        tick.moveTo(this.vx + (finalVal-0.5)*bar.getWidth(), tick.vy);
    }

    //should be an internal method that reacts to mouse events
    //xPos in VirtualSpace coords
    public void setTickPos(double xPos){
        double finalPos = Math.max(xPos, vx-(bar.getWidth()/2));
        finalPos = Math.min(finalPos, vx+(bar.getWidth()/2));
        tick.moveTo(finalPos, tick.vy);
    }

    //The next couple of methods are a kludge to start giving some
    //interactivity to the slider. We need a better event system
    //but right now I just need to get started.
    //XXX make more robust
    public boolean overTick(double xPos, double yPos){
        //approximate tick by the lower, rectangular part
        Point2D.Double[] tickCoords = tick.getAbsoluteVertices();
        return (xPos >= tickCoords[2].x) && (xPos <= tickCoords[4].x) &&
            (yPos <= tickCoords[2].y) && (yPos >= tickCoords[4].y); 
    }

    private static final double[] tickCoordsX = {0, 0, -25, -25, 25, 25, 0};
    private static final double[] tickCoordsY = {0, -BAR_HEIGHT, -BAR_HEIGHT-5, 
        -BAR_HEIGHT-5-22, -BAR_HEIGHT-5-22, -BAR_HEIGHT-5, -BAR_HEIGHT};

    private static final Point2D.Double[] getTickCoords(double[] yCoords){
        Point2D.Double retval[] = new Point2D.Double[7];
        for(int i=0; i<retval.length; ++i){
            retval[i] = new Point2D.Double(tickCoordsX[i], yCoords[i]);
        }
        return retval;
    }

    private static final VPolygon makeTick(){
        VPolygon retval = new VPolygon(getTickCoords(tickCoordsY), 0, 
                new Color(200, 0, 0, 120),
                Color.RED);
        retval.setStroke(new BasicStroke(2));
        return retval;
    }
}

