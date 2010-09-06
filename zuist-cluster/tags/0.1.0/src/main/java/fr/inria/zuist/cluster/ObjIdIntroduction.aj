/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.DefaultIdentifiable;

import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

aspect ObjIdIntroduction {
    declare parents: SceneManager extends DefaultIdentifiable;
    declare parents: Region extends DefaultIdentifiable;
    declare parents: Level extends DefaultIdentifiable;
    declare parents: ObjectDescription extends DefaultIdentifiable;
}

