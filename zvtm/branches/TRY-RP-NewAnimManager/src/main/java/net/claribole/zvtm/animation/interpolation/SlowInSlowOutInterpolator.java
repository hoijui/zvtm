package net.claribole.zvtm.animation.interpolation;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.interpolation.SplineInterpolator;

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

    private static Interpolator INSTANCE = new SplineInterpolator(1f, 0f, 0f, 1f);
}

