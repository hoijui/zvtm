package fr.inria.zvtm;

import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		VRectangle rect = new VRectangle(0,0,0,10,10,Color.BLUE);
		rect.setVisible(false);
		rect.setMouseInsideHighlightColor(Color.GREEN);
		rect.setStrokeWidth(4f);
		rect.moveTo(4,4);
    }
}
