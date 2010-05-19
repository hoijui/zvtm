package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.FitsImageDescription;

aspect FitsAutoReplay extends AbstractAutoReplay {
     public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         execution(public void FitsImageDescription.setScaleMethod(FitsImage.ScaleMethod)) ||
         execution(public void FitsImageDescription.setColorFilter(FitsImage.ColorFilter))
        );
}

