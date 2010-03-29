package pzwallzoom;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.RectangleNR;

class WallCursor {
    private long thickness;
    private long halfLength;
    private long xPos = 0;
    private long yPos = 0;
    private Color color;
    private final VirtualSpace target;

    private RectangleNR hRect;
    private RectangleNR vRect;

    WallCursor(VirtualSpace target){
        this(target, 10, 80, Color.RED);
    }

    WallCursor(VirtualSpace target, long thickness, long halfLength){
        this(target, thickness, halfLength, Color.RED);
    }

    WallCursor(VirtualSpace target, long thickness, long halfLength, Color color){
        this.target = target;
        this.thickness = thickness;
        this.halfLength = halfLength;
        this.color = color;

        hRect = new RectangleNR(xPos, yPos, 0, halfLength, thickness, color);
        hRect.setDrawBorder(false);
        vRect = new RectangleNR(xPos, yPos, 0, thickness, halfLength, color);
        vRect.setDrawBorder(false);
        target.addGlyph(hRect);
        target.addGlyph(vRect);
    }

    void dispose(){
        target.removeGlyph(hRect);
        target.removeGlyph(vRect);
    }

    void moveTo(long x, long y){
        hRect.moveTo(x, y);
        vRect.moveTo(x, y);
    }

    void setVisible(boolean v) { hRect.setVisible(v); vRect.setVisible(v);}

}
