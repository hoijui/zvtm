/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */


package fr.inria.zvtm.lens;

import java.awt.Graphics2D;

/**Profile: linear - Distance metric: L(1) (diamond shape)<br>Size expressed as an absolute value in pixels*/

public class L1FSGaussianLens extends FSGaussianLens {

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public L1FSGaussianLens(){
	super();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public L1FSGaussianLens(float mm){
	super(mm);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public L1FSGaussianLens(float mm, int outerRadius, int innerRadius){
	super(mm, outerRadius, innerRadius);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public L1FSGaussianLens(float mm, int outerRadius, int innerRadius, int x, int y){
	super(mm, outerRadius, innerRadius, x, y);
    }

    public void gf(float x, float y, float[] g){
	d = Math.abs(x-sw-lx) + Math.abs(y-sh-ly);
	if (d <= LR2){
	    g[0] = g[1] = MM;
	}
	else if (d <= LR1){
	    g[0] = g[1] = (float)(c * Math.cos(a*d+b) + e);
	}
	else {
	    g[0] = g[1] = 1;
	}
    }
    
    /**for internal use*/
    public void drawBoundary(Graphics2D g2d){
        if (r1Color != null){
            g2d.setColor(r1Color);
            g2d.drawLine(lx+w/2, ly+h/2-LR1, lx+w/2+LR1, ly+h/2);
            g2d.drawLine(lx+w/2+LR1, ly+h/2, lx+w/2, ly+h/2+LR1);
            g2d.drawLine(lx+w/2, ly+h/2+LR1, lx+w/2-LR1, ly+h/2);
            g2d.drawLine(lx+w/2-LR1, ly+h/2, lx+w/2, ly+h/2-LR1);
        }
        if (r2Color != null){
            g2d.setColor(r2Color);
            g2d.drawLine(lx+w/2, ly+h/2-LR2, lx+w/2+LR2, ly+h/2);
            g2d.drawLine(lx+w/2+LR2, ly+h/2, lx+w/2, ly+h/2+LR2);
            g2d.drawLine(lx+w/2, ly+h/2+LR2, lx+w/2-LR2, ly+h/2);
            g2d.drawLine(lx+w/2-LR2, ly+h/2, lx+w/2, ly+h/2-LR2);
        }
    }

}
