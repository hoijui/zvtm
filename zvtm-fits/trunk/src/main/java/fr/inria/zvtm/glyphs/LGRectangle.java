/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.glyphs;

import java.awt.LinearGradientPaint;
import java.awt.Color;
import fr.inria.zvtm.glyphs.PRectangle;

public class LGRectangle extends PRectangle {

    /**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param w width in virtual space
		*@param h height in virtual space
		*@param p gradient or texture paint
		*/
	public LGRectangle(double x, double y, int z, double w, double h, LinearGradientPaint p){
		super(x, y, z, w, h, p);
	}
    
    /**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param w width in virtual space
		*@param h height in virtual space
		*@param p gradient or texture paint
		*@param bc border color
		*/
	public LGRectangle(double x, double y, int z, double w, double h, LinearGradientPaint p, Color bc){
		super(x, y, z, w, h, p, bc);
	}
	
	public LinearGradientPaint getGradient(){
	    return (LinearGradientPaint)gp;
	}
    
}
