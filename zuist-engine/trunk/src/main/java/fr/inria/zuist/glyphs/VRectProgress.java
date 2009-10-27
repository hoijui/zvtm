/*   AUTHOR :           Julien Husson
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zuist.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

/**
 * VRectProgress - A progress bar based on VRectangle
 * 
 * @author Julien Husson
 * @see fr.inria.zvtm.glyphs.VRectangle
 */
public class VRectProgress extends VRectangle {

	private VText label;
	// The current VirtualSpace
	private VirtualSpace currentVS;

	final String LABEL_TEXT = "%";
	static int LOADING_LABEL_FONT_SIZE = 12;
	Color bgColor = Color.LIGHT_GRAY;
	Color barColor = Color.BLUE;
	int val = 0;

	public VRectProgress(long x, long y, int z, long w, long h, Color bgC,
			Color barC, VirtualSpace vs) {

		super(x, y, z, w, h, bgC);

		this.setTranslucencyValue(.5f);
		this.bgColor = bgC;
		this.barColor = barC;
		this.currentVS = vs;

		addPercentLabel(x, y, z, w, h);
	}

	/** A message to be displayed to the progress indicator. */
	private void addPercentLabel(long x, long y, int z, long w, long h) {
		label = new VText(x, y, z, Color.black, val + LABEL_TEXT,
				VText.TEXT_ANCHOR_START, h / LOADING_LABEL_FONT_SIZE);
		currentVS.addGlyph(label);
	}

	private void removePercentLabel() {
		currentVS.removeGlyph(label);
		label = null;
	}

	@Override
	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS,
			AffineTransform stdT, int dx, int dy) {

		if (alphaC != null && alphaC.getAlpha() == 0) {
			return;
		}
		g.setColor(bgColor);

		// draw just enough
		super.draw(g, vW - val, vH, i, stdS, stdT, dx, dy);

		g.setColor(barColor);
		g.fillRect(dx + pc[i].cx - (pc[i].cw), dy + pc[i].cy - pc[i].ch,
				(int) ((2 * val) * (double) pc[i].cw / 100), 2 * pc[i].ch);

	}

	// rough percentage calculator
	public void setProgress(int count, int ligne) {

		val = (int) count * 100 / ligne;

		label.setText(val + LABEL_TEXT);

		if (val == 100)
			removePercentLabel();
	}

	public int getProgress() {
		return val;
	}

	public void setBgColor(Color c) {
		this.bgColor = c;
	}

	public void setBarColor(Color c) {
		this.barColor = c;
	}
}
