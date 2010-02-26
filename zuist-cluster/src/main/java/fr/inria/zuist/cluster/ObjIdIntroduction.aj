/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.ObjIdFactory;
import fr.inria.zvtm.cluster.Identifiable;

import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

aspect ObjIdIntroduction {
    declare parents: SceneManager implements Identifiable;
    private final ObjId<SceneManager> SceneManager.id = ObjIdFactory.next();
    public final ObjId<SceneManager> SceneManager.getObjId(){ return id; }

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

