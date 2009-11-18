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
	static int LOADING_LABEL_FONT_SIZE = 12;
	
	int val = 0;
	
	Color bgColor = Color.LIGHT_GRAY;
	Color barColor = Color.BLUE;
	Color percentColor = Color.BLACK;

	public VRectProgress(long x, long y, int z, long w, long h, Color bgC,
			Color barC, VirtualSpace vs) {

		super(x, y, z, w, h, bgC);

		this.bgColor = bgC;
		this.barColor = barC;

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
		
		/** A message to be displayed to the progress indicator. */
		g.setColor(percentColor);
		g.setFont(new Font("Serif",Font.PLAIN,(pc[i].cw >= 0) ? (int)(12 * (double) pc[i].cw / 100) : 12));
		g.drawString(val+LABEL_TEXT, dx + pc[i].cx - (pc[i].cw), dy + pc[i].cy - pc[i].ch);
		
		VirtualSpaceManager.INSTANCE.repaintNow();

	}

	// rough percentage calculator
	public void setProgress(int count, int ligne) {
		val = (int) count * 100 / ligne;
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
