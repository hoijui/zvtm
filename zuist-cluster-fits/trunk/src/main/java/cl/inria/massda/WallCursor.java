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

    private SIRectangle hRect;
    private SIRectangle vRect;

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

        hRect = new SIRectangle(xPos, yPos, 0, length, thickness, color);
        hRect.setDrawBorder(false);
        vRect = new SIRectangle(xPos, yPos, 0, thickness, length, color);
        vRect.setDrawBorder(false);
        target.addGlyph(hRect);
        target.addGlyph(vRect);
    }

    public void dispose(){
        target.removeGlyph(hRect);
        target.removeGlyph(vRect);
    }

    public void moveTo(double x, double y){
        hRect.moveTo(x, y);
        vRect.moveTo(x, y);        
    }
    
    public void move(double x, double y){
        hRect.move(x, y);
        vRect.move(x, y);
    }

    /**
     * Gets the cursor position, in virtual space units.
     * @return the cursor position, in virtual space units.
     */
   public  Point2D getLocation(){
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

    public void setVisible(boolean v) { hRect.setVisible(v); vRect.setVisible(v);}

    public boolean isVisible() {
        return hRect.isVisible() || vRect.isVisible();
    }

}