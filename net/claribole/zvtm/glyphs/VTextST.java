/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 22 17:20:34 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.lens.Lens;

/**
 * Translucent Standalone Text.  This version is less efficient than VText, but it can be made translucent. It cannot be reoriented (see VTextOr*).<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VText
 *@see com.xerox.VTM.glyphs.VTextOr
 *@see com.xerox.VTM.glyphs.LText
 *@see com.xerox.VTM.glyphs.LBText
 *@see net.claribole.zvtm.glyphs.VTextOrST
 */

public class VTextST extends VText implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    /**
     *@param t text string
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VTextST(String t, float a){
	super(t);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VTextST(long x,long y,float z,Color c,String t, float a){
	super(x, y, z, c, t);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VTextST(long x,long y,float z,Color c,String t,short ta, float a){
	super(x, y, z, c, t, ta);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public float getTranslucencyValue(){
	return alpha;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){
	    //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].cw = (int)bounds.getWidth();
		    pc[i].ch = (int)bounds.getHeight();
		    pc[i].valid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawString(text, 0.0f, 0.0f);
		    g.setComposite(acO);
		}
		else {
		    g.drawString(text, 0.0f, 0.0f);
		}
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].valid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].cw = (int)bounds.getWidth();
		    pc[i].ch = (int)bounds.getHeight();
		    pc[i].valid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawString(text, 0.0f, 0.0f);
		    g.setComposite(acO);
		}
		else {
		    g.drawString(text, 0.0f, 0.0f);
		}
	    }
	    g.setTransform(stdT);
	}
	else {
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	    }
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	g.setColor(this.color);
	if (coef*fontSize>vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){
	    //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
	    if (font!=null){
		g.setFont(font);
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].lcw = (int)bounds.getWidth();
		    pc[i].lch = (int)bounds.getHeight();
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawString(text, 0.0f, 0.0f);
		    g.setComposite(acO);
		}
		else {
		    g.drawString(text, 0.0f, 0.0f);
		}
		g.setFont(VirtualSpaceManager.getMainFont());
	    }
	    else {
		if (!pc[i].lvalid){
		    bounds = g.getFontMetrics().getStringBounds(text,g);
		    pc[i].lcw = (int)bounds.getWidth();
		    pc[i].lch = (int)bounds.getHeight();
		    pc[i].lvalid=true;
		}
		if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
		else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
		else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
		if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(coef,coef));}
		g.setTransform(at);
		if (alpha < 1.0f){
		    g.setComposite(acST);
		    g.drawString(text, 0.0f, 0.0f);
		    g.setComposite(acO);
		}
		else {
		    g.drawString(text, 0.0f, 0.0f);
		}
	    }
	    g.setTransform(stdT);
	}
	else {
	    if (alpha < 1.0f){
		g.setComposite(acST);
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		g.setComposite(acO);
	    }
	    else {
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	    }
	}
    }

    public Object clone(){
	VTextST res = new VTextST(vx, vy, 0, color, (new StringBuffer(text)).toString(), text_anchor, alpha);
	res.mouseInsideColor = this.mouseInsideColor;
	return res;
    }

}
