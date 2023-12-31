/*   FILE: VBText.java
 *   DATE OF CREATION:   May 24 2007
 *   AUTHOR :            Boris Trofimov (trofimov@lri.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Standalone Text with background.<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).<br>
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @see com.xerox.VTM.glyphs.VText
 */
public class VBText extends VText {

	/**
	 * Border color of background rectangle
	 */
	Color borderColor = Color.black;

	/**
	 * Fill color of background rectangle
	 */
	Color fillColor = Color.white;

	/**
	 * Offset between text and vertical borders
	 */
	public int paddingX = 10;

	/**
	 * Offset betwenn text and horizontal borders
	 */
	public int paddingY = 10;

	/**
	 *@param x coordinate in virtual space
	 *@param y coordinate in virtual space
	 *@param z z-index (pass 0 if you do not use z-ordering)
	 *@param c color of the text
	 *@param t text string
	 */
	public VBText(long x, long y, int z, Color c, String t) {
		super(x, y, z, c, t);
		this.sensit = true;
	}

	/**
	 * @param x coordinate in virtual space
	 * @param y coordinate in virtual space
	 * @param z altitude
	 * @param textColor color of the text
	 * @param borderColor color of the border
	 * @param fillColor color of the background
	 * @param text text string
	 */
	public VBText(long x, long y, int z, Color textColor, Color borderColor, Color fillColor, String text) {
		super(x, y, z, textColor, text);
		this.fillColor = fillColor;
		this.borderColor = borderColor;
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
	 */
	public VBText(long x, long y, int z, Color textColor, Color borderColor, Color fillColor, String text, short ta) {
		super(x, y, z, textColor, text, ta);
		this.fillColor = fillColor;
		this.borderColor = borderColor;
	}

	public boolean coordInside(int x, int y, int camIndex) {
		boolean res;
		if (text_anchor == TEXT_ANCHOR_START) {
			res =  x >= pc[camIndex].cx
				&& x <= pc[camIndex].cx + coef * pc[camIndex].cw
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * pc[camIndex].ch;
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
			res =  x >= pc[camIndex].cx - coef * pc[camIndex].cw / 2
				&& x <= pc[camIndex].cx + coef * pc[camIndex].cw / 2
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * pc[camIndex].ch;
		}
		else {
			res =  x >= pc[camIndex].cx - coef * pc[camIndex].cw
				&& x <= pc[camIndex].cx
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * pc[camIndex].ch;
		}
		return res;
	}
	
	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		trueCoef = scaleFactor * coef;
		if (trueCoef * fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (!pc[i].valid)
			{
				bounds = g.getFontMetrics().getStringBounds(text, g);
				pc[i].cw = (int)Math.round((bounds.getWidth() + 2 * paddingX) * scaleFactor);
				pc[i].ch = (int)Math.round((bounds.getHeight() + 2 * paddingY) * scaleFactor);
				pc[i].valid = true;
			}
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

			g.setColor(fillColor);
			int rectW = Math.round(pc[i].cw / scaleFactor);
			int rectH = Math.round(pc[i].ch / scaleFactor);
			g.fillRect(dx, dy - rectH, rectW, rectH);

			g.setColor(borderColor);
			g.drawRect(dx, dy - rectH, rectW, rectH);

			g.setColor(this.color);
			g.drawString(text, paddingX, -paddingY);
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx + pc[i].cx, dy + pc[i].cy, 1, 1);
		}
	}


	public void drawForLens(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		trueCoef = scaleFactor * coef;
		if (trueCoef * fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (!pc[i].lvalid) {
				bounds = g.getFontMetrics().getStringBounds(text, g);
				pc[i].lcw = (int) Math.round((bounds.getWidth() + 2 * paddingX) * scaleFactor);
				pc[i].lch = (int) Math.round((bounds.getHeight() + 2 * paddingY) * scaleFactor);
				pc[i].lvalid = true;
			}
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

			g.setColor(fillColor);
			int rectW = Math.round(pc[i].lcw / scaleFactor);
			int rectH = Math.round(pc[i].lch / scaleFactor);
			g.fillRect(dx, dy - rectH, rectW, rectH);

			g.setColor(borderColor);
			g.drawRect(dx, dy - rectH, rectW, rectH);

			g.setColor(this.color);
			g.drawString(text, paddingX, -paddingY);
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx + pc[i].lcx, dy + pc[i].lcy, 1, 1);
		}
	}

	public short mouseInOut(int x, int y, int camIndex) {
		short res;
		if (coordInside(x, y, camIndex)) {//if the mouse is inside the glyph
			if (!pc[camIndex].prevMouseIn) {//if it was not inside it last time, mouse has entered the glyph
				pc[camIndex].prevMouseIn = true;
				res = Glyph.ENTERED_GLYPH;
			}
			else {
				return Glyph.NO_CURSOR_EVENT;
			}  //if it was inside last time, nothing has changed
		}
		else {//if the mouse is not inside the glyph
			if (pc[camIndex].prevMouseIn) {//if it was inside it last time, mouse has exited the glyph
				pc[camIndex].prevMouseIn = false;
				res = Glyph.EXITED_GLYPH;
			}
			else {
				res = Glyph.NO_CURSOR_EVENT;
			}  //if it was not inside last time, nothing has changed
		}
		return res;
	}	

	/**
	 * Set the VBText's border color.
	 */
	public void setBorderColor(Color c) {
		borderColor = c;
	}

	/**
	 * Get the VBText's border color.
	 */
	public Color getBorderColor(){
		return borderColor;
	}

	/**
	 * Set the VBText's background fill color.
	 */
	public void setBackgroundFillColor(Color c) {
		fillColor = c;
	}

	/**
	 * Get the VBText's background fill color.
	 */
	public Color getBackgroundFillColor() {
		return fillColor;
	}
}
