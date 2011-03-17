package fr.inria.zvtm.glyphs;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

/**
 * 2011-03-17: in early development, do not use this yet!
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

    @Override public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        float formatWidth = (float)widthConstraint;

        if (!pc[i].valid){
            g.setFont((font!=null) ? font : getMainFont());
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(text,g);
            // cw and ch actually hold width and height of text *in virtual space*
            pc[i].cw = (int)Math.round(bounds.getWidth() * scaleFactor);
            pc[i].ch = (int)Math.round(bounds.getHeight() * scaleFactor);
            pc[i].valid=true;
        }
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        double trueCoef = scaleFactor * coef;
        if (trueCoef*fontSize > VText.TEXT_AS_LINE_PROJ_COEF || !zoomSensitive){
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            g.setFont((font!=null) ? font : getMainFont());	
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
            if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
            g.setTransform(at);
            int rectH = Math.round(pc[i].ch / scaleFactor);
            
            g.setColor(this.color);
            //g.drawString(text, 0.0f, 0.0f);
            float drawPosY = 0;
            lbm.setPosition(atText.getIterator().getBeginIndex());
            int paragraphEnd = atText.getIterator().getEndIndex();
            while(lbm.getPosition() < paragraphEnd &&
                    drawPosY <= heightConstraint){
                TextLayout layout = lbm.nextLayout((float)widthConstraint);
                drawPosY += layout.getAscent();
                layout.draw(g, 0, drawPosY);
                
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            g.setTransform(stdT);
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
