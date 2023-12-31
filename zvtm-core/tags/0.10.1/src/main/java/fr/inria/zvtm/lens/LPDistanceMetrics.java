/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */


package fr.inria.zvtm.lens;


public interface LPDistanceMetrics {

    public static float LP_FLOOR = 0.01f;
    
    /**
     *@param lp value of L(P), the Lp-metrics defining the lens' shape, in ]0,+inf[
     */
    public void setDistanceMetrics(float lp);
    
    /**
     *@return value of L(P), the Lp-metrics defining the lens' shape, in ]0,+inf[
     */
    public float getDistanceMetrics();

}
