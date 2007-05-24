/*   FILE: VBText.java
 *   DATE OF CREATION:   May 24 2007
 *   AUTHOR :            Boris Trofimov (trofimov@lri.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VBText.java 611 2007-04-16 09:30:13Z btrofimov $
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
	 *@param z altitude
	 *@param c color of the text
	 *@param t text string
	 */
	public VBText(long x, long y, float z, Color c, String t) {
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
	public VBText(long x, long y, float z, Color textColor, Color borderColor, Color fillColor, String text) {
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
	public VBText(long x, long y, float z, Color textColor, Color borderColor, Color fillColor, String text, short ta) {
		super(x, y, z, textColor, text, ta);
		this.fillColor = fillColor;
		this.borderColor = borderColor;
	}

	public boolean coordInside(int x, int y, int camIndex) {
		boolean res;
		if (text_anchor == TEXT_ANCHOR_START) {
			res =  x >= pc[camIndex].cx
				&& x <= pc[camIndex].cx + coef * (pc[camIndex].cw + 2 * paddingX)
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * (pc[camIndex].ch + 2 * paddingY);
		}
		else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
			res =  x >= pc[camIndex].cx - coef * (pc[camIndex].cw + 2 * paddingX) / 2
				&& x <= pc[camIndex].cx + coef * (pc[camIndex].cw + 2 * paddingX) / 2
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * (pc[camIndex].ch + 2 * paddingY);
		}
		else {
			res =  x >= pc[camIndex].cx - coef * (pc[camIndex].cw + 2 * paddingX)
				&& x <= pc[camIndex].cx
				&& y <= pc[camIndex].cy
				&& y >= pc[camIndex].cy - coef * (pc[camIndex].ch + 2 * paddingY);
		}		
		return res;
	}

	public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i) {
		if ((vx >= wb) && (vx <= eb) && (vy >= sb) && (vy <= nb)) { //if glyph hotspot is in the region, it is obviously visible
			return true;
		}
		else {
			// cw and ch actually hold width and height of text *in virtual space*
			if (text_anchor == TEXT_ANCHOR_START) {
				if ((vx <= eb) && ((vx + pc[i].cw + 2 * paddingX) >= wb) && (vy <= nb) && ((vy + pc[i].ch + 2 * paddingY) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
				else return false;   //otherwise the glyph is not visible
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				if ((vx - pc[i].cw / 2 <= eb) && ((vx + (pc[i].cw + 2 * paddingX) / 2) >= wb) && (vy <= nb) && ((vy + pc[i].ch + 2 * paddingY) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
				else return false;   //otherwise the glyph is not visible
			}
			else {//TEXT_ANCHOR_END
				if ((vx - (pc[i].cw + 2 * paddingX) <= eb) && (vx >= wb) && (vy <= nb) && ((vy + pc[i].ch + 2 * paddingY) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
				else return false;   //otherwise the glyph is not visible
			}
		}
	}

	public boolean containedInRegion(long wb, long nb, long eb, long sb, int i) {
		if ((vx >= wb) && (vx <= eb) && (vy >= sb) && (vy <= nb)) {
			/* Glyph hotspot is in the region.
					   There is a good chance the glyph is contained in the region, but this is not sufficient. */
			// cw and ch actually hold width and height of text *in virtual space*
			if (text_anchor == TEXT_ANCHOR_START) {
				if ((vx <= eb) && ((vx + pc[i].cw + 2 * paddingX) >= wb) && (vy <= nb) && ((vy - (pc[i].ch + 2 * paddingY)) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				if ((vx + (pc[i].cw + 2 * paddingX) / 2 <= eb) && ((vx - (pc[i].cw + 2 * paddingX) / 2) >= wb) && (vy <= nb) && ((vy - (pc[i].ch + 2 * paddingY)) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
			}
			else {//TEXT_ANCHOR_END
				if ((vx + (pc[i].cw + 2 * paddingX) <= eb) && (vx >= wb) && (vy <= nb) && ((vy - (pc[i].ch + 2 * paddingY)) >= sb)) {
					//if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
					return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
				}
			}
		}
		return false;
	}

	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		if (coef * fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			if (font != null) {
				g.setFont(font);
			}
			// there is a bug, if next line uncommented...
			//if (!pc[i].valid)
			{
				bounds = g.getFontMetrics().getStringBounds(text, g);
				pc[i].cw = (int) bounds.getWidth();
				pc[i].ch = (int) bounds.getHeight();
				pc[i].valid = true;
				size = (float) Math.sqrt(Math.pow(pc[i].cw, 2) + Math.pow(pc[i].ch, 2));
			}
			if (text_anchor == TEXT_ANCHOR_START) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx, dy + pc[i].cy - 2 * paddingY * coef);
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx - (pc[i].cw + 2 * paddingX) * coef / 2f, dy + pc[i].cy - 2 * paddingY * coef);
			}
			else {
				at = AffineTransform.getTranslateInstance(dx + pc[i].cx - (pc[i].cw + 2 * paddingX) * coef, dy + pc[i].cy - 2 * paddingY * coef);
			}
			if (zoomSensitive) {
				at.concatenate(AffineTransform.getScaleInstance(coef, coef));
			}
			g.setTransform(at);

			g.setColor(fillColor);
			g.fillRect(dx, dy - pc[i].ch, pc[i].cw + paddingX * 2, pc[i].ch + paddingY * 2);

			g.setColor(borderColor);
			g.drawRect(dx, dy - pc[i].ch, pc[i].cw + paddingX * 2, pc[i].ch + paddingY * 2);

			g.setColor(this.color);
			g.drawString(text, paddingX, paddingY);
			if (font != null) {
				g.setFont(VirtualSpaceManager.getMainFont());
			}
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx + pc[i].cx, dy + pc[i].cy, 1, 1);
		}
	}


	public void drawForLens(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
		if (coef * fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive) {
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			if (font != null) {
				g.setFont(font);
			}
			// there is a bug, if next line uncommented...
			//if (!pc[i].valid)
			{
				bounds = g.getFontMetrics().getStringBounds(text, g);
				pc[i].lcw = (int) bounds.getWidth();
				pc[i].lch = (int) bounds.getHeight();
				pc[i].valid = true;
				size = (float) Math.sqrt(Math.pow(pc[i].lcw, 2) + Math.pow(pc[i].lch, 2));
			}
			if (text_anchor == TEXT_ANCHOR_START) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx, dy + pc[i].lcy - 2 * paddingY * coef);
			}
			else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - (pc[i].lcw + 2 * paddingX) * coef / 2f, dy + pc[i].lcy - 2 * paddingY * coef);
			}
			else {
				at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - (pc[i].lcw + 2 * paddingX) * coef, dy + pc[i].lcy - 2 * paddingY * coef);
			}
			if (zoomSensitive) {
				at.concatenate(AffineTransform.getScaleInstance(coef, coef));
			}
			g.setTransform(at);

			g.setColor(fillColor);
			g.fillRect(dx, dy - pc[i].lch, pc[i].lcw + paddingX * 2, pc[i].lch + paddingY * 2);

			g.setColor(borderColor);
			g.drawRect(dx, dy - pc[i].lch, pc[i].lcw + paddingX * 2, pc[i].lch + paddingY * 2);

			g.setColor(this.color);
			g.drawString(text, paddingX, paddingY);
			if (font != null) {
				g.setFont(VirtualSpaceManager.getMainFont());
			}
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
	 * Get the width and height of the bounding box in virtual space.
	 *
	 * @param i index of camera (Camera.getIndex())
	 * @return the width and height of the text's bounding box, as a LongPoint
	 */
	public LongPoint getBounds(int i) {
		return new LongPoint(pc[i].cw + paddingX * 2, pc[i].ch + paddingY * 2);
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
