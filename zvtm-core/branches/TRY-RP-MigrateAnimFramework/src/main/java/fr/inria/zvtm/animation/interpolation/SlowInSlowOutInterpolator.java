/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$ 
 */
package fr.inria.zvtm.animation.interpolation;

import net.jcip.annotations.*;

import org.jdesktop.core.animation.timing.Interpolator;
import org.jdesktop.core.animation.timing.interpolators.SplineInterpolator;

/**
 * A class that implements slow in, slow out interpolation.
 * @author Romain Primet
 */
@Immutable
public class SlowInSlowOutInterpolator{
    //Disallow instanciation
    private SlowInSlowOutInterpolator(){}

    /**
     * Returns Interpolator instance
     * @return an Interpolator that implements slow in, slow out interpolation
     */
    public static Interpolator getInstance(){
	//interpolator has no state, hence we share it among all animations
	//XXX I'm assuming that the underlying SplineInterpolator is thread-safe.
	//This is almost self-evident, but may be worth checking
	
	return INSTANCE;
    }

    private static final Interpolator INSTANCE = new SplineInterpolator(1f, 0f, 0f, 1f);
}

