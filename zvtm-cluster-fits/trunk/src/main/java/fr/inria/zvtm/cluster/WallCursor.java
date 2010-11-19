package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.SIRectangle;

class WallCursor {
    private double thickness;
    private double length;
    private double xPos = 0;
    private double yPos = 0;
    private Color color;
    private final VirtualSpace target;

    //private SIRectangle hRect;
    //private SIRectangle vRect;
    private Composite cursor;

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

        double qlength = length/4;

        SIRectangle hRect1 = new SIRectangle(xPos-(qlength*3/2), yPos, 0, qlength, thickness, color);
        SIRectangle hRect2 = new SIRectangle(xPos-(qlength/2), yPos, 0, qlength, 1, color);
        SIRectangle hRect3 = new SIRectangle(xPos+(qlength/2), yPos, 0, qlength, 1, color);
        SIRectangle hRect4 = new SIRectangle(xPos+(qlength*3/2), yPos, 0, qlength, thickness, color);
        hRect1.setDrawBorder(false);
        hRect2.setDrawBorder(false);
        hRect3.setDrawBorder(false);
        hRect4.setDrawBorder(false);

        SIRectangle vRect1 = new SIRectangle(xPos, yPos-(qlength*3/2), 0, thickness, qlength, color);
        SIRectangle vRect2 = new SIRectangle(xPos, yPos-(qlength/2), 0, 1, qlength, color);
        SIRectangle vRect3 = new SIRectangle(xPos, yPos+(qlength/2), 0, 1, qlength, color);
        SIRectangle vRect4 = new SIRectangle(xPos, yPos+(qlength*3/2), 0, thickness, qlength, color);
        vRect1.setDrawBorder(false);
        vRect2.setDrawBorder(false);
        vRect3.setDrawBorder(false);
        vRect4.setDrawBorder(false);

        cursor = new Composite(); 
        cursor.addChild(hRect1);
        cursor.addChild(hRect2);
        cursor.addChild(hRect3);
        cursor.addChild(hRect4);
        cursor.addChild(vRect1);
        cursor.addChild(vRect2);
        cursor.addChild(vRect3);
        cursor.addChild(vRect4);
        target.addGlyph(cursor);
        target.onTop(cursor, 0);
    }

    void dispose(){
        target.removeGlyph(cursor);
    }

    void onTop(int zIndex){
        target.onTop(cursor, zIndex);
    }

    void moveTo(double x, double y){
        xPos = x;
        yPos = y;
        cursor.moveTo(x, y);
    }

    Point2D.Double getPosition(){
        return new Point2D.Double(xPos, yPos);
    }

    void setVisible(boolean v) { cursor.setVisible(v);}

}
