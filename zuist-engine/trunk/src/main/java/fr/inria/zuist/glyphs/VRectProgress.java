/*   AUTHOR :           Julien Husson
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zuist.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
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
	static int LOADING_LABEL_FONT_SIZE = 10;
	static final String FONT_FAMILY = "Arial";
	static Font PROGRESS_DONT = new Font(FONT_FAMILY, Font.PLAIN, LOADING_LABEL_FONT_SIZE);
	
	int val = 0;
	String valStr = "";
	
	Color bgColor;
	Color barColor;
	Color percentColor;

	public VRectProgress(long x, long y, int z, long w, long h, Color bgC,
			Color barC, Color percentC, VirtualSpace vs) {

		super(x, y, z, w, h, bgC);

		this.bgColor = bgC;
		this.barColor = barC;
		this.percentColor = percentC;

	}

	@Override
	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS,
			AffineTransform stdT, int dx, int dy) {

		if (alphaC != null && alphaC.getAlpha() == 0) {
			return;
		}

		// draw just enough
		super.draw(g, vW - val, vH, i, stdS, stdT, dx, dy);

		g.setColor(barColor);
		g.fillRect(dx + pc[i].cx - (pc[i].cw), dy + pc[i].cy - pc[i].ch,
				(int) ((2 * val) * (double) pc[i].cw / 100), 2 * pc[i].ch);
		
		/** A message to be displayed to the progress indicator. */
		g.setColor(percentColor);
		AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx, dy+pc[i].cy+pc[i].ch/2);
		at.concatenate(AffineTransform.getScaleInstance(pc[i].cw / 100.0, pc[i].cw / 100.0));
		g.setTransform(at);
		g.drawString(valStr, 0, 0);
		g.setTransform(stdT);
	}

	// rough percentage calculator
	public void setProgress(int count, int ligne) {
		val = (int) count * 100 / ligne;
		valStr = String.valueOf(val) + LABEL_TEXT;
	}

	public int getProgress() {
		return val;
	}
	
}
