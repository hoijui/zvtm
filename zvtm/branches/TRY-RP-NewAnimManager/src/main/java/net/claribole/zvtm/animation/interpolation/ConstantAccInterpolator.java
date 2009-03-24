package net.claribole.zvtm.animation.interpolation;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.interpolation.SplineInterpolator;

/**
 * A class that implements constant acceleration interpolation.
 */
@Immutable
public class ConstantAccInterpolator{
    //Disallow instanciation
    private ConstantAccInterpolator(){}

    /**
     * Returns Interpolator instance
     * @return an Interpolator that implements constant acceleration interpolation
     */
    public static Interpolator getInstance(){
    //interpolator has no state, hence we share it among all animations
    //XXX I'm assuming that the underlying SplineInterpolator is thread-safe.
    //This is almost self-evident, but may be worth checking
	
	return INSTANCE;
    }

    private static Interpolator INSTANCE = new SplineInterpolator(.5f, 0f, 1f, .5f);
}
