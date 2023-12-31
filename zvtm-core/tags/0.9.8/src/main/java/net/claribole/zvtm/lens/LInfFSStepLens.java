/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.lens;

import java.awt.Color;
import java.awt.Graphics2D;

/**Profile: Step-like (flat steps) - Distance metric: L(Inf) (square shape)<br>Size expressed as an absolute value in pixels*/

public class LInfFSStepLens extends FSStepLens {

    /**
        * create a lens with a maximum magnification factor of 2.0
        */
    public LInfFSStepLens(){
        super();
    }

    /**
        * create a lens with a given maximum magnification factor
        *
        *@param mm maximum magnification factor, mm in [0,+inf[
        *@param ns number of flat steps between context and focus
        */
    public LInfFSStepLens(float mm, int ns){
        super(mm, ns);
    }

    /**
        * create a lens with a given maximum magnification factor, inner and outer radii
        *
        *@param mm maximum magnification factor, mm in [0,+inf[
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        *@param ns number of flat steps between context and focus
        */
    public LInfFSStepLens(float mm, int outerRadius, int innerRadius, int ns){
        super(mm, outerRadius, innerRadius, ns);
    }

    /**
        * create a lens with a given maximum magnification factor, inner and outer radii
        *
        *@param mm maximum magnification factor, mm in [0,+inf[
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        *@param ns number of flat steps between context and focus
        *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
        *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
        */
    public LInfFSStepLens(float mm, int outerRadius, int innerRadius, int ns, int x, int y){
        super(mm, outerRadius, innerRadius, ns, x, y);
    }

    public void gf(float x, float y, float[] g){
        d = Math.max(Math.abs(x-sw-lx), Math.abs(y-sh-ly));
        if (d <= LR2)
            g[0] = g[1] = MM;
        else if (d <= LR1){
            for (int i=0;i<Ri.length;i++){
                if (d <= Ri[i]){
                    g[0] = g[1] = Ai[i]*(float)d + Bi[i];
                    break;
                }
            }
        }
        else
            g[0] = g[1] = 1;
    }

}