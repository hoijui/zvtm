package fr.inria.zuist.cluster.viewer;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.SIRectangle;

class WallCursor {
    private double thickness;
    private double halfLength;
    private double xPos = 0;
    private double yPos = 0;
    private Color color;
    private final VirtualSpace target;

    private SIRectangle hRect;
    private SIRectangle vRect;

    WallCursor(VirtualSpace target){
        this(target, 10, 80, Color.RED);
    }

    WallCursor(VirtualSpace target, double thickness, double halfLength){
        this(target, thickness, halfLength, Color.RED);
    }

    WallCursor(VirtualSpace target, double thickness, double halfLength, Color color){
        this.target = target;
        this.thickness = thickness;
        this.halfLength = halfLength;
        this.color = color;

        hRect = new SIRectangle(xPos, yPos, 0, halfLength, thickness, color);
        hRect.setDrawBorder(false);
        vRect = new SIRectangle(xPos, yPos, 0, thickness, halfLength, color);
        vRect.setDrawBorder(false);
        target.addGlyph(hRect);
        target.addGlyph(vRect);
    }

    void dispose(){
        target.removeGlyph(hRect);
        target.removeGlyph(vRect);
    }

    void moveTo(double x, double y){
        hRect.moveTo(x, y);
        vRect.moveTo(x, y);
    }

    void setVisible(boolean v) { hRect.setVisible(v); vRect.setVisible(v);}

}
