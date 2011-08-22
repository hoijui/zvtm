package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VCircle;

class CircleSelectionManager {
    private final VirtualSpace parentSpace;
    private final VCircle circle;
    private boolean active = false;

    CircleSelectionManager(VirtualSpace parentSpace){
        this.parentSpace = parentSpace;
        circle = new VCircle(0,0,AstroRad.IMG_OVERLAY_ZINDEX,1, new Color(0,0,0,0));
        circle.setBorderColor(Color.RED);
        circle.setVisible(false);
        parentSpace.addGlyph(circle);
    }
    
    void dispose(){
        parentSpace.removeGlyph(circle);
    }

    //XXX rename to activate()? (and get rid of the params)
    boolean onKeyType(char c, KeyEvent ke){
        if(c == 'q'){
            active = true;
            System.err.println("circle selection active");
        }
        return active;
    }

    /**
     * @param x x-coordinate, in virtual space units.
     * @param y y-coordinate, in virtual space units.
     */
    boolean onPress1(double x, double y){
        if(active){
            System.err.println("Begin circle sel");
            circle.moveTo(x, y);
            circle.setVisible(true);
        }
        return active;
    }

    //circle radius, in virtual space units.
    double getVsRadius(){
        return circle.getSize();
    }

    //center, in virtual space coordinates
    Point2D.Double getVsCenter(){
        return new Point2D.Double(circle.vx, circle.vy);
    }

    /**
     * @param x x-coordinate, in virtual space units.
     * @param y y-coordinate, in virtual space units.
     */
    boolean onRelease1(double x, double y){
        boolean retval = active;
        active = false;
        circle.setVisible(false);
        return retval;
    }

    /**
     * @param x x-coordinate, in virtual space units.
     * @param y y-coordinate, in virtual space units.
     */
    boolean onDrag(double x, double y){
        if(active){
            //resize circle
            circle.sizeTo(dist(circle.vx, circle.vy, x, y));
        }
        return active;
    }

    private double dist(double x1, double y1, double x2, double y2){
        return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
    }

}

