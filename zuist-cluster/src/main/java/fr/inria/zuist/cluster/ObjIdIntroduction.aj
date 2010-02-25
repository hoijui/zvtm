package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.ObjIdFactory;
import fr.inria.zvtm.cluster.Identifiable;

import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.Region;

aspect ObjIdIntroduction {
    //declare parents: SceneManager implements Identifiable;
    declare parents: Region implements Identifiable;
    private final ObjId<Region> Region.id = ObjIdFactory.next();
    public final ObjId<Region> Region.getObjId(){ return id; }

    declare parents: Level implements Identifiable;
    private final ObjId<Level> Level.id = ObjIdFactory.next();
    public final ObjId<Level> Level.getObjId(){ return id; }

    declare parents: ObjectDescription implements Identifiable;
    private final ObjId<ObjectDescription> ObjectDescription.id =
        ObjIdFactory.next();
    public final ObjId<ObjectDescription> ObjectDescription.getObjId(){
        return id;
    }
}

