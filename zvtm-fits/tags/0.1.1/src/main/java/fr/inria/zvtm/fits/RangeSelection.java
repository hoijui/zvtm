package fr.inria.zvtm.fits;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;

public class RangeSelection extends Composite {
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_WIDTH = 200;

    VRectangle bar;
    VPolygon leftTick;
    VPolygon rightTick;

    public RangeSelection(){
        bar = new VRectangle(0,0,0,BAR_WIDTH,BAR_HEIGHT,new Color(0,200,0,180));
        leftTick = makeTick();
        leftTick.move(-BAR_WIDTH + 20, BAR_HEIGHT);
        rightTick = makeTick();
        rightTick.move(BAR_WIDTH - 20, BAR_HEIGHT);
        addChild(bar);
        addChild(leftTick);
        addChild(rightTick);
    }

    //left value, in [0,1]
    public double getLeftValue(){
        return (double)(leftTick.vx - (this.vx - BAR_WIDTH))/(2 * BAR_WIDTH);
    }

    //right value, in [0,1]
    public double getRightValue(){
        return (double)(rightTick.vx - (this.vx - BAR_WIDTH))/(2 * BAR_WIDTH);
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

        leftTick.moveTo((long)(this.vx - BAR_WIDTH + 2*finalLeft*BAR_WIDTH), leftTick.vy);
        rightTick.moveTo((long)(this.vx - BAR_WIDTH + 2*finalRight*BAR_WIDTH), rightTick.vy);
    }

    //should be an internal method that reacts to mouse events
    public void setLeftTickPos(long xPos){
        //constrained by left end and right tick 
        long finalPos = Math.max(xPos, vx-BAR_WIDTH);
        finalPos = Math.min(finalPos, rightTick.vx);
        leftTick.moveTo(finalPos, leftTick.vy);
    }

    //should be an internal method that reacts to mouse events
    public void setRightTickPos(long xPos){
        //constrained by right end and left tick
        long finalPos = Math.min(xPos, vx+BAR_WIDTH);
        finalPos = Math.max(finalPos, leftTick.vx);
        rightTick.moveTo(finalPos, rightTick.vy);
    }

    //The next couple of methods are a kludge to start giving some
    //interactivity to RangeSelection. We need a better event system
    //but right now I just need to get started.
    public boolean overLeftTick(long xPos, long yPos){
        long[] ltBounds = leftTick.getBounds(); //wnes
        return (xPos >= ltBounds[0]) && (xPos <= ltBounds[2]) &&
            (yPos <= ltBounds[1]) && (yPos >= ltBounds[3]);
    }

    //See above method
    public boolean overRightTick(long xPos, long yPos){
    long[] rtBounds = rightTick.getBounds(); //wnes
        return (xPos >= rtBounds[0]) && (xPos <= rtBounds[2]) &&
            (yPos <= rtBounds[1]) && (yPos >= rtBounds[3]);
    }

    private static final long[] tickCoordsX = {0, 0, -5, -5, 5, 5, 0};
    private static final long[] tickCoordsY = {0, -BAR_HEIGHT, -BAR_HEIGHT-5, 
        -BAR_HEIGHT-5-22, -BAR_HEIGHT-5-22, -BAR_HEIGHT-5, -BAR_HEIGHT};

    private static final LongPoint[] getTickCoords(){
        LongPoint retval[] = new LongPoint[7];
        for(int i=0; i<retval.length; ++i){
            retval[i] = new LongPoint(tickCoordsX[i], tickCoordsY[i]);
        }
        return retval;
    }

    private static final VPolygon makeTick(){
        return new VPolygon(getTickCoords(), 0, new Color(200, 0, 0, 120),
                Color.RED);
    }
}

