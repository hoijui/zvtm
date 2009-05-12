/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Translucent;

/**
 * Translucent Standalone Text with background.<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).<br>
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @see com.xerox.VTM.glyphs.VText
 */
public class VBTextST extends VBText implements Translucent {

    AlphaComposite acST;
    float alpha = 0.5f;

	/**
	 *@param x coordinate in virtual space
	 *@param y coordinate in virtual space
	 *@param z z-index (pass 0 if you do not use z-ordering)
	 *@param c color of the text
	 *@param t text string
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
	 */
	public VBTextST(long x, long y, int z, Color c, String t, float a) {
		super(x, y, z, c, t);
		alpha = a;
		acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**
	 * @param x coordinate in virtual space
	 * @param y coordinate in virtual space
	 * @param z altitude
	 * @param textColor color of the text
	 * @param borderColor color of the border
	 * @param fillColor color of the background
	 * @param text text string
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
	 */
	public VBTextST(long x, long y, int z, Color textColor, Color borderColor, Color fillColor, String text, float a) {
		super(x, y, z, textColor, borderColor, fillColor, text);
		alpha = a;
		acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**
	 * @param x coordinate in virtual space
	 * @param y coordinate in virtual space
	 * @param z altitude
	 * @param textColor color of the text
	 * @param borderColor color of the border
	 * @param fillColor color of the background
	 * @param text text string
	 * @param ta text anchor
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
	 */
	public VBTextST(long x, long y, int z, Color textColor, Color borderColor, Color fillColor, String text, short ta, float a) {
		super(x, y, z, textColor, borderColor, fillColor, text, ta);
		alpha = a;
		acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	public void setTranslucencyValue(float a){
		alpha = a;
		acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	public float getTranslucencyValue(){
		return alpha;
	}

	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		if (!pc[i].valid){
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			bounds = g.getFontMetrics().getStringBounds(text, g);
			pc[i].cw = (int)Math.round((bounds.getWidth() + 2 * paddingX) * scaleFactor);
			pc[i].ch = (int)Math.round((bounds.getHeight() + 2 * paddingY) * scaleFactor);
			pc[i].valid = true;
		}
		if (alpha == 0){return;}
		trueCoef = scaleFactor * coef;
		if (trueCoef * fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (text_anchor == TEXT_ANCHOR_START) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx, dy + pc[i].cy);
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx - pc[i].cw * coef / 2f, dy + pc[i].cy);
			}
			else {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx - pc[i].cw * coef, dy + pc[i].cy);
			}
			if (zoomSensitive) {
				at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));
			}
			g.setTransform(at);
			int rectW = Math.round(pc[i].cw / scaleFactor);
			int rectH = Math.round(pc[i].ch / scaleFactor);
			g.setColor(fillColor);
			if (alpha < 1.0f){
				// translucent
				g.setComposite(acST);
				g.fillRect(dx, dy-rectH+1, rectW, rectH-1);
				g.setColor(borderColor);
				g.drawRect(dx, dy-rectH+1, rectW, rectH-1);
				g.setColor(this.color);
				g.drawString(text, paddingX, -paddingY);
				g.setComposite(acO);
			}
			else {
				// opaque
				g.fillRect(dx, dy-rectH, rectW, rectH);
				g.setColor(borderColor);
				g.drawRect(dx, dy-rectH, rectW, rectH);
				g.setColor(this.color);
				g.drawString(text, paddingX, -paddingY);
			}
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx + pc[i].cx, dy + pc[i].cy, 1, 1);
		}
	}


	public void drawForLens(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		if (!pc[i].lvalid) {
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			bounds = g.getFontMetrics().getStringBounds(text, g);
			pc[i].lcw = (int) Math.round((bounds.getWidth() + 2 * paddingX) * scaleFactor);
			pc[i].lch = (int) Math.round((bounds.getHeight() + 2 * paddingY) * scaleFactor);
			pc[i].lvalid = true;
		}
		if (alpha == 0){return;}
		trueCoef = scaleFactor * coef;
		if (trueCoef * fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (text_anchor == TEXT_ANCHOR_START) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx, dy + pc[i].lcy);
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - pc[i].lcw * coef / 2f, dy + pc[i].lcy);
			}
			else {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - pc[i].lcw * coef, dy + pc[i].lcy);
			}
			if (zoomSensitive) {
				at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));
			}
			g.setTransform(at);
			int rectW = Math.round(pc[i].lcw / scaleFactor);
			int rectH = Math.round(pc[i].lch / scaleFactor);
			if (alpha < 1.0f){
				// translucent
				g.setComposite(acST);
				g.setColor(fillColor);
				g.fillRect(dx, dy-rectH+1, rectW, rectH-1);
				g.setColor(borderColor);
				g.drawRect(dx, dy-rectH+1, rectW, rectH-1);
				g.setColor(this.color);
				g.drawString(text, paddingX, -paddingY);
				g.setComposite(acO);
			}
			else {
				g.setColor(fillColor);
				g.fillRect(dx, dy - rectH, rectW+1, rectH-1);
				g.setColor(borderColor);
				g.drawRect(dx, dy - rectH, rectW, rectH);
				g.setColor(this.color);
				g.drawString(text, paddingX, -paddingY);
			}
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx + pc[i].lcx, dy + pc[i].lcy, 1, 1);
		}
	}

}
