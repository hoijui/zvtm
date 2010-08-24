package fr.inria.zvtm.fits;

import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;
import java.awt.geom.Point2D;

public class RangeSelection extends Composite {
    private static final double BAR_HEIGHT = 20;
    private static final double BAR_WIDTH = 400;

    VRectangle bar;
    VPolygon leftTick;
    VPolygon rightTick;

    public RangeSelection(){
        bar = new VRectangle(0,0,0,BAR_WIDTH,BAR_HEIGHT,new Color(0,200,0,180));
        leftTick = makeLeftTick();
        leftTick.move(-BAR_WIDTH/2 + 20, BAR_HEIGHT/2);
        rightTick = makeRightTick();
        rightTick.move(BAR_WIDTH/2 - 20, BAR_HEIGHT/2);
        addChild(bar);
        addChild(leftTick);
        addChild(rightTick);
    }

    //left value, in [0,1]
    public double getLeftValue(){
        return (leftTick.vx - (this.vx - bar.getWidth()/2)) / bar.getWidth();
    }

    //right value, in [0,1]
    public double getRightValue(){
        return (rightTick.vx - (this.vx - bar.getWidth()/2)) / bar.getWidth();
    }

    //left value, in [0,1]
    public void setTicksVal(double left, double right){
        if(left > right){
            System.err.println("Cannot set left tick above right tick");
            return;
        }
        double finalLeft = Math.max(0, left);
        finalLeft = Math.min(finalLeft, 1);
        double finalRight = Math.max(0, right);
        finalRight = Math.min(finalRight, 1);

        leftTick.moveTo(this.vx + (finalLeft-0.5)*bar.getWidth(), leftTick.vy);
        rightTick.moveTo(this.vx + (finalRight-0.5)*bar.getWidth(), rightTick.vy);
    }

    //should be an internal method that reacts to mouse events
    public void setLeftTickPos(double xPos){
        //constrained by left end and right tick 
        double finalPos = Math.max(xPos, vx-(bar.getWidth()/2));
        finalPos = Math.min(finalPos, rightTick.vx);
        leftTick.moveTo(finalPos, leftTick.vy);
    }

    //should be an internal method that reacts to mouse events
    public void setRightTickPos(double xPos){
        //constrained by right end and left tick
        double finalPos = Math.min(xPos, vx+(bar.getWidth()/2));
        finalPos = Math.max(finalPos, leftTick.vx);
        rightTick.moveTo(finalPos, rightTick.vy);
    }

    //The next couple of methods are a kludge to start giving some
    //interactivity to RangeSelection. We need a better event system
    //but right now I just need to get started.
    public boolean overLeftTick(double xPos, double yPos){
        //approximate tick by the lower, rectangular part
        Point2D.Double[] tickCoords = leftTick.getAbsoluteVertices();
        return (xPos >= tickCoords[2].x) && (xPos <= tickCoords[4].x) &&
            (yPos <= tickCoords[2].y) && (yPos >= tickCoords[4].y); 
    }

    //See above method
    public boolean overRightTick(double xPos, double yPos){
        //approximate tick by the lower, rectangular part
        Point2D.Double[] tickCoords = rightTick.getAbsoluteVertices();
        return (xPos >= tickCoords[2].x) && (xPos <= tickCoords[4].x) &&
            (yPos <= tickCoords[2].y) && (yPos >= tickCoords[4].y); 
    }

    private static final double[] tickCoordsX = {0, 0, -5, -5, 5, 5, 0};
    private static final double[] tickCoordsLY = {0, -BAR_HEIGHT, -BAR_HEIGHT-5, 
        -BAR_HEIGHT-5-22, -BAR_HEIGHT-5-22, -BAR_HEIGHT-5, -BAR_HEIGHT};
    private static final double[] tickCoordsRY = {0, -BAR_HEIGHT, -BAR_HEIGHT-5, 
        -BAR_HEIGHT-5-32, -BAR_HEIGHT-5-32, -BAR_HEIGHT-5, -BAR_HEIGHT};

    private static final Point2D.Double[] getTickCoords(double[] yCoords){
        Point2D.Double retval[] = new Point2D.Double[7];
        for(int i=0; i<retval.length; ++i){
            retval[i] = new Point2D.Double(tickCoordsX[i], yCoords[i]);
        }
        return retval;
    }

    private static final VPolygon makeLeftTick(){
        return new VPolygon(getTickCoords(tickCoordsLY), 0, 
                new Color(200, 0, 0, 120),
                Color.RED);
    }

    private static final VPolygon makeRightTick(){
        return new VPolygon(getTickCoords(tickCoordsRY), 0, 
                new Color(200, 200, 0, 120),
                Color.YELLOW);
    }
}

