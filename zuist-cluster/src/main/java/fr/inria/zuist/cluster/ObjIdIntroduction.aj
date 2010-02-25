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

aspect ObjIdIntroduction {
    //SceneManager does not implement Identifiable:
    //we assume the presence of a single SceneManager in
    //the application, which is the zuist entry point
    //(i.e. Levels, Regions and ObjectDescriptions are created
    //there)

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

