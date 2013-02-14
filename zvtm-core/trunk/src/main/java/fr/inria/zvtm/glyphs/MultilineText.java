/*   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
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
 * Multiline text.
 * By default, text will be rendered on one line. Specifiy a width
 * constraint to make the text overflow on multiple lines. Specify
 * a height constraint to truncate the text. A height constraint is only
 * meaningful if a width constraint has been defined.
 * The 'hot spot' of a MultilineText instance is its top-left corner.
 * This is unlike VText.
 * Note: setting a background color is unsupported.
 */
public class MultilineText<T> extends VText {

    // See http://java.sun.com/developer/onlineTraining/Media/2DText/style.html#multiple
    private double widthConstraint = Double.POSITIVE_INFINITY;
    private double heightConstraint = Double.POSITIVE_INFINITY;
    private LineBreakMeasurer lbm;
    private AttributedString atText;
    private static final FontRenderContext DEFAULT_FRC =
        new FontRenderContext(null, false, false);

    public MultilineText(String text){
        super(text);
        initLbm();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param bkg background color (null if not painted)
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public MultilineText(double x, double y, int z, Color c, String t, short ta, float scale, float alpha){
        super(x, y, z, c, t, ta, scale, alpha);
        initLbm();
    }

    void initLbm(){
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
        invalidate();
    }

    /**
     * Height constraint, in virtual space units.
     * Text will be truncated if it overflows the
     * height constraint.
     */
    public void setHeightConstraint(double constraint){
        heightConstraint = constraint;
        invalidate();
    }

    /**
     * Gets the width constraint for this MultilineText.
     * A value of Double.POSITIVE_INFINITY means that the
     * width is unconstrained.
     */
    public double getWidthConstraint(){
        return widthConstraint;
    }

    public double getHeightConstraint(){
        return heightConstraint;
    }

    @Override public void setText(String text){
        super.setText(text);
        atText = new AttributedString(text);
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
        invalidate();
    }

    @Override public void setFont(Font f){
        super.setFont(f);
        atText.addAttribute(TextAttribute.FONT,
                usesSpecificFont() ? getFont() : getMainFont());
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
        invalidate();
    }

    @Override public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
        if (!validBounds(i)){return true;}
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            //if glyph hotspot is in the region, it is obviously visible
            return true;
        }
        return (vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy>=sb) && ((vy-pc[i].ch)<=nb);
    }

    @Override public boolean containedInRegion(double wb, double nb, double eb, double sb, int i){
        return (vx >= wb) && (vy <= nb) &&
            (vx + pc[i].cw <= eb) && (vy - pc[i].ch >= sb);
    }

    @Override public boolean coordInside(int jpx, int jpy, int camIndex, double cvx, double cvy){
        return (cvx >= vx) && (cvy <= vy) &&
            (cvx <= vx+pc[camIndex].cw) && (cvy >= vy-pc[camIndex].ch);
    }

    @Override public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        // float formatWidth = (float)widthConstraint;
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        double trueCoef = scaleFactor * coef;
        if (trueCoef*fontSize > VText.TEXT_AS_LINE_PROJ_COEF || !zoomSensitive || !pc[i].valid){
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            g.setFont((font!=null) ? font : getMainFont());
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
            if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
            g.setTransform(at);
            // int rectH = Math.round(pc[i].ch / scaleFactor);
            g.setColor(this.color);
            float drawPosY = 0;
            lbm.setPosition(atText.getIterator().getBeginIndex());
            int paragraphEnd = atText.getIterator().getEndIndex();
            TextLayout layout = null;
            Rectangle2D lbounds;
            while(lbm.getPosition() < paragraphEnd &&
                  drawPosY <= heightConstraint){
                layout = lbm.nextLayout((float)widthConstraint);
                drawPosY += layout.getAscent();
                if (text_anchor==TEXT_ANCHOR_START){
                    layout.draw(g, 0, drawPosY);
                }
                else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                    lbounds = layout.getBounds();
                    layout.draw(g, (float)(widthConstraint/2f-lbounds.getWidth()/2f), drawPosY);
                }
                else {
                    // text_anchor == TEXT_ANCHOR_END
                    lbounds = layout.getBounds();
                    layout.draw(g, (int)Math.round(widthConstraint-lbounds.getWidth()), drawPosY);
                }
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

    @Override public Object clone(){
        throw new UnsupportedOperationException("Cannot clone MultilineText");
    }
}
