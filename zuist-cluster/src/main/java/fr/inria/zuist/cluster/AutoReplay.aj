package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

/**
 * Add methods that should be replay by the generic Delta here.
 * See the AbstractAutoReplay aspect in ZVTM-cluster for more details.
 * @see fr.inria.zvtm.AbstractAutoReplay
 */
aspect AutoReplay extends AbstractAutoReplay {
    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        (
         execution(public void SceneManager.setUpdateLevel(boolean)) ||
         execution(public void SceneManager.enableRegionUpdater(boolean)) ||
         execution(public void Region.setContainingRegion(Region)) ||
         execution(public void Region.addContainedRegion(Region))
        );
}

