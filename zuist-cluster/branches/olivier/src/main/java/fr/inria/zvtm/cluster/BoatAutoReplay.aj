/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id:  $
 */


package fr.inria.zvtm.cluster;

import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zvtm.glyphs.BoatInfoG;

/**
 * Add methods that should be replay by the generic Delta here.
 * See the AbstractAutoReplay aspect in ZVTM-cluster for more details.
 * @see fr.inria.zvtm.AbstractAutoReplay
 */
aspect BoatAutoReplay extends AbstractAutoReplay {

    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         execution(public void BoatInfoG.setInfo(String, String, String, String, boolean))
        );

}
