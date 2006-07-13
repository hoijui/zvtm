/*   FILE: LInfFSFresnelLens.java
 *   DATE OF CREATION:  Thu Jul 13 10:19:04 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LInfFSFresnelLens.java,v 1.5 2005/12/05 15:29:08 epietrig Exp $
 */ 


package net.claribole.zvtm.lens;


/**Profile: Fresnel-like (discrete steps) - Distance metric: L(Inf) (square shape)<br>Size expressed as an absolute value in pixels*/

public class LInfFSFresnelLens extends FSFresnelLens {

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public LInfFSFresnelLens(){
	super();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param ns number of discrete transition steps between context and focus
     */
    public LInfFSFresnelLens(float mm, int ns){
	super(mm, ns);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param ns number of discrete transition steps between context and focus
     */
    public LInfFSFresnelLens(float mm, int outerRadius, int innerRadius, int ns){
	super(mm, outerRadius, innerRadius, ns);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param ns number of discrete transition steps between context and focus
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public LInfFSFresnelLens(float mm, int outerRadius, int innerRadius, int ns, int x, int y){
	super(mm, outerRadius, innerRadius, ns, x, y);
    }

    public void gf(float x, float y, float[] g){
	d = Math.max(Math.abs(x-sw-lx), Math.abs(y-sh-ly));
	if (d <= LR2)
	    g[0] = g[1] = MM;
	else if (d <= LR1){
	    for (int i=0;i<Ri.length;i++){
		if (d <= Ri[i]){
		    g[0] = g[1] = MMi[i];
		    break;
		}
	    }
	}
	else
	    g[0] = g[1] = 1;
    }

}