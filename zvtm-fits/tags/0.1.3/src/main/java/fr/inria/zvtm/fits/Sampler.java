/*   AUTHOR :          Romain Primet (romain.primet@inria.fr) 
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits;

public interface Sampler {
    /**
     * returns up to 'nmax' samples.
     */
    public double[] getSample(int nmax);
}

