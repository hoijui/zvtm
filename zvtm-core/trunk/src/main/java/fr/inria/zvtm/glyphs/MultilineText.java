package fr.inria.zvtm.glyphs;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

/**
 * 2011-03-17: in early development, do not use this yet!
 * Multiline text.
 * By default, text will be rendered on one line. Specifiy a width
 * constraint to make the text overflow on multiple lines. Specify
 * a height constraint to trim the text. A height constraint is only 
 * meaningful if a width constraint has been defined.
 * The 'hot spot' of a MultilineText instance is its top-left corner.
 * This is unlike VText.
 */
public class MultilineText extends VText {
    // See http://java.sun.com/developer/onlineTraining/Media/2DText/style.html#multiple
    private double widthConstraint = Double.POSITIVE_INFINITY;
    private double heightConstraint = Double.POSITIVE_INFINITY;
    private LineBreakMeasurer lbm;
    private AttributedString atText;
    private static final FontRenderContext DEFAULT_FRC = 
        new FontRenderContext(null, false, false);

    public MultilineText(String text){
        super(text);
        atText = new AttributedString(text);
        atText.addAttribute(TextAttribute.FONT,
                usesSpecificFont() ? getFont() : getMainFont());
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
    }

    /**
     * Width constraint, in virtual space units.
     */
    public void setWidthConstraint(double constraint){
        widthConstraint = constraint;
    }

    /**
     * Height constraint, in virtual space units.
     * Text will be truncated if it overflows the
     * height constraint.
     */
    public void setHeightConstraint(double constraint){
        heightConstraint = constraint;
    }

    @Override public void setText(String text){
        super.setText(text);
        atText = new AttributedString(text);
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
    }

    @Override public void setFont(Font f){
        super.setFont(f);
        atText.addAttribute(TextAttribute.FONT, 
                usesSpecificFont() ? getFont() : getMainFont());
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC); 
    }

    @Override public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
        if (!validBounds(i)){return true;}
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            //if glyph hotspot is in the region, it is obviously visible
            return true;
        }
        return (vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy>=sb) && ((vy-pc[i].ch)<=nb);
    }

    @Override public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        float formatWidth = (float)widthConstraint;

        if (alphaC != null && alphaC.getAlpha()==0){return;}
        double trueCoef = scaleFactor * coef;
        if (trueCoef*fontSize > VText.TEXT_AS_LINE_PROJ_COEF || !zoomSensitive || !pc[i].valid){
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            g.setFont((font!=null) ? font : getMainFont());	
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
            if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
            g.setTransform(at);
            int rectH = Math.round(pc[i].ch / scaleFactor);
            
            g.setColor(this.color);
            float drawPosY = 0;
            lbm.setPosition(atText.getIterator().getBeginIndex());
            int paragraphEnd = atText.getIterator().getEndIndex();
            TextLayout layout = null;
            while(lbm.getPosition() < paragraphEnd &&
                    drawPosY <= heightConstraint){
                layout = lbm.nextLayout((float)widthConstraint);
                drawPosY += layout.getAscent();
                layout.draw(g, 0, drawPosY);
                
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            g.setTransform(stdT);
            if(!pc[i].valid){
                if(widthConstraint == Double.POSITIVE_INFINITY){
                    if(layout == null){
                        pc[i].cw = 0;
                    } else {
                        pc[i].cw = (int)(layout.getBounds().getWidth() * scaleFactor);
                    } 
                } else {
                    pc[i].cw = (int)(widthConstraint * scaleFactor);
                }
                pc[i].ch = (int)(drawPosY * scaleFactor);
                pc[i].valid = true;
            }
        }
        else {
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
    }

    @Override public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        draw(g, vW, vH, i, stdS, stdT, dx, dy);
    }
}
