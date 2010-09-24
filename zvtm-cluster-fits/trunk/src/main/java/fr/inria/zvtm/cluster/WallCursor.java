package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.SIRectangle;

class WallCursor {
    private double thickness;
    private double length;
    private double xPos = 0;
    private double yPos = 0;
    private Color color;
    private final VirtualSpace target;

    private SIRectangle hRect;
    private SIRectangle vRect;

    WallCursor(VirtualSpace target){
        this(target, 20, 160, Color.RED);
    }

    WallCursor(VirtualSpace target, double thickness, double length){
        this(target, thickness, length, Color.RED);
    }

    WallCursor(VirtualSpace target, double thickness, double length, Color color){
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
        target.onTop(hRect, 1); //XXX make configurable
        target.onTop(vRect, 1); //XXX make configurable
    }

    void dispose(){
        target.removeGlyph(hRect);
        target.removeGlyph(vRect);
    }

    void setZindex(int zIndex){
        hRect.setZindex(zIndex);
        vRect.setZindex(zIndex);
    }

    void moveTo(double x, double y){
        xPos = x;
        yPos = y;
        hRect.moveTo(x, y);
        vRect.moveTo(x, y);
    }

    Point2D.Double getPosition(){
        return new Point2D.Double(xPos, yPos);
    }

    void setVisible(boolean v) { hRect.setVisible(v); vRect.setVisible(v);}

}
