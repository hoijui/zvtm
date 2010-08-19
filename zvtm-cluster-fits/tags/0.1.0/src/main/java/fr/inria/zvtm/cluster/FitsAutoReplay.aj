package fr.inria.zvtm.cluster;

import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zvtm.glyphs.FitsImage;

/**
 * Add methods that should be replay by the generic Delta here.
 * See the AbstractAutoReplay aspect in ZVTM-cluster for more details.
 * @see fr.inria.zvtm.AbstractAutoReplay
 */
aspect FitsAutoReplay extends AbstractAutoReplay {
    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         execution(public void FitsImage.rescale(double, double, double)) ||
         execution(public void FitsImage.zRescale()) ||
         execution(public void FitsImage.setColorFilter(FitsImage.ColorFilter)) 
        );
}

