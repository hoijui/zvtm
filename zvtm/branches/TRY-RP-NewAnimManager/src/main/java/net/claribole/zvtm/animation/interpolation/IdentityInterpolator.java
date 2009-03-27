package net.claribole.zvtm.animation.interpolation;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.interpolation.LinearInterpolator;

/**
 * A class that implements an identity ('no-op') interpolation.
 * It simply returns the singleton instance of LinearInterpolator.
 */
@Immutable
public class IdentityInterpolator{
    //Disallow instanciation
    private IdentityInterpolator(){}

    /**
     * Returns Interpolator instance
     * @return an Interpolator that implements identity interpolation
     */
    public static Interpolator getInstance(){
	return LinearInterpolator.getInstance();
    }
}
