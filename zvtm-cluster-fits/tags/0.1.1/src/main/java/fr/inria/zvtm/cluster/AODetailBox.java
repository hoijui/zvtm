package fr.inria.zvtm.cluster;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VText;

//A pop-up status widget for astro objects
class AODetailBox {
    static final Color BACKGROUND_COLOR = new Color(249,247,240);
    protected static final Color DETAIL_BOX_STROKE_COLOR = new Color(198, 146, 200);
    private final double STATUS_RECT_WIDTH=15;
    private VRoundRect box;
    private VText text;
    private VirtualSpace parentSpace;

    AODetailBox(VirtualSpace parentSpace){
        if(parentSpace == null){
            throw new IllegalArgumentException("non-null parentSpace expected");
        }
        this.parentSpace = parentSpace;
        box = new VRoundRect();
        box.setColor(BACKGROUND_COLOR);
        box.setBorderColor(DETAIL_BOX_STROKE_COLOR);
        box.setStroke(new BasicStroke(3));
        text = new VText(0, 0, 0, Color.BLACK, "");
        text.setTextAnchor(VText.TEXT_ANCHOR_MIDDLE);
        parentSpace.addGlyph(box);
        parentSpace.addGlyph(text);
        parentSpace.hide(box);
        parentSpace.hide(text);
    }

    void onEnterGlyph(Glyph g){
        update(g);
    }

    void onExitGlyph(Glyph g){
        //update(g);
        hide();
    }

    void dispose(){
        parentSpace.removeGlyph(box);
        parentSpace.removeGlyph(text);
    }

    private void update(Glyph glyph){
        if(!(glyph.getOwner() instanceof AstroObject)){
            hide();
            return;
        } 

        AstroObject astroObj = (AstroObject)glyph.getOwner();

        text.setText(astroObj.getIdentifier());
        box.setHeight(getVTextHeight(text) + 15);
        box.setWidth(getVTextWidth(text) + 20);
        moveTo(glyph.vx, glyph.vy);
        show();
    }

    void moveTo(double vx, double vy){
        box.moveTo(vx + box.getWidth()/2,  vy);
        text.moveTo(box.vx , box.vy);
    }

    private void hide(){
        parentSpace.hide(box);
        parentSpace.hide(text);
    }

    private void show(){
        parentSpace.show(box);
        parentSpace.show(text);
    }

    /**
     * Returns the <b>approximate</b> height of a VText, in 
     * virtual space units.
     */ 
    private static double getVTextHeight(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).getHeight();
    }

    /**
     * Returns the <b>approximate</b> width of a VText, in 
     * virtual space units.
     */ 
    private static double getVTextWidth(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).charWidth('e')*text.getText().length();
    }

}

