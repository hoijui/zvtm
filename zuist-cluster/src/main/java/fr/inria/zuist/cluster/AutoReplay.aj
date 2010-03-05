package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

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

