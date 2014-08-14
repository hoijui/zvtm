/*   AUTHOR :           Julien Husson
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;

/**
 * VRectProgress - A progress bar based on VRectangle.
 * 
 * @author Julien Husson
 * @see fr.inria.zvtm.glyphs.VRectangle
 */
public class VRectProgress extends VRectangle {

    // percentage
	int val = 0;
	
	Color barColor;

	public VRectProgress(double x, double y, int z, double w, double h) {
		super(x, y, z, w, h, Color.WHITE, Color.BLACK);
		this.barColor = Color.BLACK;
	}

	public VRectProgress(double x, double y, int z, double w, double h, Color bkgC, Color borderC, Color barC) {
		super(x, y, z, w, h, bkgC, borderC);
		this.barColor = barC;
	}

	@Override
	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS,
			AffineTransform stdT, int dx, int dy) {
		super.draw(g, vW - val, vH, i, stdS, stdT, dx, dy);
		g.setColor(barColor);
		g.fillRect(dx + pc[i].cx - (pc[i].cw), dy + pc[i].cy - pc[i].ch,
				(int) ((2 * val) * pc[i].cw / 100d), 2 * pc[i].ch);		
	}

	/** Set progress value.
	 *@param count current value
	 *@param total max (completion) value
	 */
	public void setProgress(int count, int total) {
		val = (int) count * 100 / total;
		VirtualSpaceManager.INSTANCE.repaint();
	}

    /** Get current progress.
     *@return current progress as a percentage.
     */
	public int getProgress(){
		return val;
	}
	
}
