/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: WallCursor.java 2014-03-04 13:04:22Z fdelcampo $
 */

package cl.inria.massda;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.SIRectangle;




/**
 * A cursor suitable for interacting with the clustered viewer on a large display.
 */
public class WallCursor {
    private double thickness;
    private double length;
    private double xPos = 0;
    private double yPos = 0;
    private Color color;
    private final VirtualSpace target;

    private static double PROP = 2.5;

    private SIRectangle hRect;
    private SIRectangle vRect;
    private SIRectangle hRectLeft;
    private SIRectangle hRectRight;
    private SIRectangle vRectUp;
    private SIRectangle vRectDown;
    private double displace;

    public WallCursor(VirtualSpace target){
        this(target, 5, 50, Color.RED);
    }

    public WallCursor(VirtualSpace target, double thickness, double length){
        this(target, thickness, length, Color.RED);
    }

    public WallCursor(VirtualSpace target, double thickness, double length, Color color){
        this.target = target;
        this.thickness = thickness;
        this.length = length;
        this.color = color;

        double centerSize = length/PROP;
        double otherSize = (length - centerSize)/2;
        displace = centerSize/2+otherSize/2-1;

        hRect = new SIRectangle(xPos, yPos, 0, centerSize, 1, color);
        hRect.setDrawBorder(false);
        hRectLeft = new SIRectangle(xPos-displace, yPos, 0, otherSize, thickness, color);
        hRectLeft.setDrawBorder(false);
        hRectRight = new SIRectangle(xPos+displace, yPos, 0, otherSize, thickness, color);
        hRectRight.setDrawBorder(false);
        vRect = new SIRectangle(xPos, yPos, 0, 1, centerSize, color);
        vRect.setDrawBorder(false);
        vRectUp = new SIRectangle(xPos, yPos-displace, 0, thickness, otherSize, color);
        vRectUp.setDrawBorder(false);
        vRectDown = new SIRectangle(xPos, yPos+displace, 0, thickness, otherSize, color);
        vRectDown.setDrawBorder(false);
        target.addGlyph(hRect);
        target.addGlyph(vRect);
        target.addGlyph(hRectLeft);
        target.addGlyph(vRectUp);
        target.addGlyph(hRectRight);
        target.addGlyph(vRectDown);
    }

    public void dispose(){
        target.removeGlyph(hRect);
        target.removeGlyph(vRect);
        target.removeGlyph(hRectLeft);
        target.removeGlyph(hRectRight);
        target.removeGlyph(vRectUp);
        target.removeGlyph(vRectDown);
    }

    public void moveTo(double x, double y){
        hRect.moveTo(x,y);
        hRectLeft.moveTo(x-displace, y);
        hRectRight.moveTo(x+displace, y);
        vRect.moveTo(x,y);
        vRectUp.moveTo(x, y-displace);
        vRectDown.moveTo(x, y+displace);   
    }
    
    public void move(double x, double y){
        hRect.move(x, y);
        hRectLeft.move(x, y);
        hRectRight.move(x, y);
        vRect.move(x, y);
        vRectUp.move(x, y);
        vRectDown.move(x, y);
    }

    /**
     * Gets the cursor position, in virtual space units.
     * @return the cursor position, in virtual space units.
     */
   public Point2D getLocation(){
        return hRect.getLocation();
    }

    /**
     * Gets the cursor position x-coordinate, in virtual space units.
     * @return the cursor position x-coordinate, in virtual space units.
     */
    public double getX(){
        return getLocation().getX();
    }

    /**
     * Gets the cursor position y-coordinate, in virtual space units.
     * @return the cursor position y-coordinate, in virtual space units.
     */
    public double getY(){
        return getLocation().getY();
    }

    public void setVisible(boolean v) { hRect.setVisible(v); vRect.setVisible(v); hRectLeft.setVisible(v); hRectRight.setVisible(v); vRectUp.setVisible(v); vRectDown.setVisible(v);}

    public boolean isVisible() {
        return hRect.isVisible() || vRect.isVisible() || hRectLeft.isVisible() || hRectRight.isVisible() || vRectUp.isVisible() || vRectDown.isVisible();
    }

}