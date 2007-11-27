/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */


package net.claribole.zvtm.lens;


public interface LPDistanceMetrics {

    /**
     *@param lp value of L(P), the Lp-metrics defining the lens' shape, in ]0,+inf[
     */
    public void setDistanceMetrics(float lp);
    
    /**
     *@return value of L(P), the Lp-metrics defining the lens' shape, in ]0,+inf[
     */
    public float getDistanceMetrics();

}
