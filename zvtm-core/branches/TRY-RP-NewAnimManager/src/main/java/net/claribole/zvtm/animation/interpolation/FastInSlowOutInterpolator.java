/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package net.claribole.zvtm.animation.interpolation;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.interpolation.SplineInterpolator;

/**
 * A class that implements fast in, slow out interpolation.
 * @author Romain Primet
 */
@Immutable
public class FastInSlowOutInterpolator{
    //Disallow instanciation
    private FastInSlowOutInterpolator(){}

    /**
     * Returns Interpolator instance
     * @return an Interpolator that implements fast in, slow out interpolation
     */
    public static Interpolator getInstance(){
    //interpolator has no state, hence we share it among all animations
    //XXX I'm assuming that the underlying SplineInterpolator is thread-safe.
    //This is almost self-evident, but may be worth checking
	
	return INSTANCE;
    }

    private static Interpolator INSTANCE = new SplineInterpolator(0f, 1f, 0f, 1f);
}
