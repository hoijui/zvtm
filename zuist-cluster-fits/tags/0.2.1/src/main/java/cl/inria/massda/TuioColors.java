/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TuioColors.java 2014-03-04 13:04:22Z fdelcampo $
 */

package cl.inria.massda;

import java.awt.Color;

public class TuioColors{

	static private final int[] cursorsRGBColors = {
		0xFF9900, 0x91FF00, 0x00F7FF, 0xFF0000, 0xFF00AA, 0x9100FF, 0x1100FF,
		0x00CCFF, 0x00FF4D, 0xDDFF00};

	static public Color getCursorColorById(int id) {
		int c = id;
		if (c >= cursorsRGBColors.length) {
			c = c % cursorsRGBColors.length;
		}
	    return new Color(cursorsRGBColors[c]);
	}
}