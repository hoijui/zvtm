package fr.inria.zuist.cluster;

import java.util.ArrayList;
import java.util.List;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.cluster.Delta;
import fr.inria.zvtm.cluster.Identifiables;
import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.SlaveUpdater;
import fr.inria.zuist.engine.SceneManager;

aspect SceneManagerReplication {
    //instrument createLevel, createRegion, destroyRegion,
    //createImageDescription, createTextDescription
    
    //instrument SceneManager creation: create remote SceneManager
    //and register it in the SlaveUpdater.

    private static class SceneManagerCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ArrayList<ObjId<VirtualSpace>> spaceRefs;
        private final ArrayList<ObjId<Camera>> cameraRefs;

        SceneManagerCreateDelta(ObjId<SceneManager> smId, List<VirtualSpace> spaces, List<Camera> cameras){
            this.smId = smId;
            this.spaceRefs = Identifiables.getRefList(spaces);
            this.cameraRefs = Identifiables.getRefList(cameras);
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = 
                new SceneManager(su.getSlaveObjectArrayList(spaceRefs).toArray(new VirtualSpace[0]),
                        su.getSlaveObjectArrayList(cameraRefs).toArray(new Camera[0]));
            su.putSlaveObject(smId, sm);
        }
    }
}

