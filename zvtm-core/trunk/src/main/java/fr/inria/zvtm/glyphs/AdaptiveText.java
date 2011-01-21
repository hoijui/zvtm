package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * An adaptative version of VText
 */
public class AdaptiveText extends VText {

    private TextShortener shortener = PrefixTextShortener.INSTANCE;
    private double vsHeight;
    private double vsWidth; //virtual space width (max)
    private double textPxWidth;
    private double textPxHeight;
    //note: we use the font size as the minimal pixel text height
    
    public AdaptiveText(double x, double y, int z, Color c, String t, double vsWidth, double vsHeight){
        super(x,y,z,c,t);
        this.vsHeight = vsHeight;
        this.vsWidth = vsWidth;
        computePixelBounds();
    }
    
    //computes approximate text bounds
    private void computePixelBounds(){
        textPxWidth = Toolkit.getDefaultToolkit().getFontMetrics(getMainFont()).charWidth('e')*getText().length();
        textPxHeight = Toolkit.getDefaultToolkit().getFontMetrics(getMainFont()).getHeight();
    }

    /**
     * Sets the shortener for this AdaptiveText.
     * By default, a PrefixTextShortener is used.
     */
    public void setShortener(TextShortener shortener){
        this.shortener = shortener;
    }

    /**
     * @inheritDoc
     */
    @Override public void setText(String text){
        super.setText(text);
        computePixelBounds();
    }

    /**
     * Ignored (setting the scale is not meaningful
     * in the case of AdaptiveText)
     */
    @Override public void setScale(float ignored){} 

    /**
     * Ignored (AdaptiveText is zoom sensitive)
     */
    @Override public void setZoomSensitive(boolean ignored){} 

    /**
     * Since we specify a width and height, visibility rules
     * are easier than the VText ones.
     */
    @Override public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
        return (vx + (vsWidth/2) >= wb &&
                vx - (vsWidth/2) <= eb &&
                vy + (vsHeight/2) >= sb &&
                vy - (vsHeight/2) <= nb); 
    }

    @Override public boolean containedInRegion(double wb, double nb, double eb, double sb, int i){
        return (vx - (vsWidth/2) >= wb &&
                vx + (vsWidth/2) <= eb &&
                vy - (vsHeight/2) >= sb &&
                vy + (vsHeight/2) <= nb);
    }

    @Override public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        String finalTxt = getText();
        double xscr = vsWidth*coef; //vsWidth, translated to pixels
        double yscr = vsHeight*coef; //vsHeight, translated to pixels
        System.err.println("coef = " + coef);
        System.err.println("xscr = " + xscr + ", textPxWidth = " + textPxWidth);
        System.err.println("yscr = " + yscr + ", textPxHeight = " + textPxHeight);
        if(yscr < textPxHeight){
            setScale((float)(yscr/textPxHeight));
        }
        if(xscr < textPxWidth){
            int txLen = (int)Math.floor(xscr/textPxWidth*getText().length());
            finalTxt = shortener.shorten(finalTxt, txLen);
        }

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
			AffineTransform at;
			if (text_anchor==TEXT_ANCHOR_START){
			    at = AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
			}
			else if (text_anchor==TEXT_ANCHOR_MIDDLE){
			    at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);
			    }
			else {
			    at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);
			}
			if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
			g.setTransform(at);
			int rectH = Math.round(pc[i].ch / scaleFactor);
			if (alphaC != null){
				g.setComposite(alphaC);
				if (isBorderDrawn()){
				    g.setColor(borderColor);
	                g.fillRect(dx-paddingX, dy-rectH+1+2*paddingY, Math.round(pc[i].cw / scaleFactor+paddingX), rectH-1+2*paddingY);
				}
	    		g.setColor(this.color);
				g.drawString(finalTxt, 0.0f, 0.0f);
				g.setComposite(acO);
			}
			else {
				if (isBorderDrawn()){
				    g.setColor(borderColor);
	                g.fillRect(dx-paddingX, dy-rectH+1+2*paddingY, Math.round(pc[i].cw / scaleFactor+paddingX), rectH-1+2*paddingY);
				}
	    		g.setColor(this.color);
				g.drawString(finalTxt, 0.0f, 0.0f);
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

