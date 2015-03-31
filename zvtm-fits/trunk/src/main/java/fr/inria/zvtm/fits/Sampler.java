/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
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

