
package fr.inria.zvtm.cluster.tuio;

import java.awt.Color;

public class TuioColors{

	static private final int[] cursorsRGBColors = {
	0xFF9900, 0x91FF00, 0x00F7FF, 0xFF0000, 0xFF00AA, 0x9100FF, 0x1100FF,
	0x00CCFF, 0x00FF4D, 0xDDFF00};

	static public Color getCursorColorById(int id) 
	{
		int c = id;
		if (c >= cursorsRGBColors.length)
		{
			c =  c % cursorsRGBColors.length;
		}
	    	return new Color(cursorsRGBColors[c]);
	}

}