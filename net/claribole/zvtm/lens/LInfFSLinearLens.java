/*   FILE: LInfFSLinearLens.java
 *   DATE OF CREATION:  Wed Nov 03 11:51:04 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LInfFSLinearLens.java,v 1.5 2005/12/05 15:29:08 epietrig Exp $
 */ 


package net.claribole.zvtm.lens;


/**Profile: linear - Distance metric: L(Inf) (square shape)<br>Size expressed as an absolute value in pixels*/

public class LInfFSLinearLens extends FSLinearLens {

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public LInfFSLinearLens(){
	super();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public LInfFSLinearLens(float mm){
	super(mm);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public LInfFSLinearLens(float mm, int outerRadius, int innerRadius){
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
    public LInfFSLinearLens(float mm, int outerRadius, int innerRadius, int x, int y){
	super(mm, outerRadius, innerRadius, x, y);
    }

    public void gf(float x, float y, float[] g){
	d = Math.max(Math.abs(x-sw-lx), Math.abs(y-sh-ly));
	if (d <= LR2)
	    g[0] = g[1] = MM;
	else if (d <= LR1)
	    g[0] = g[1] = a * (float)d + b;
	else
	    g[0] = g[1] = 1;
    }

}