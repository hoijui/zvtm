package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;

aspect AutoReplay extends AbstractAutoReplay {
    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        (
         execution(public void SceneManager.setUpdateLevel(boolean))	
        );
}

