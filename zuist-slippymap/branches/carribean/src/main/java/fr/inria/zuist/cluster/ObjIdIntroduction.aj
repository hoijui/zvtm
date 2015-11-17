/*
 *   Copyright (c) INRIA, 2010-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObjIdIntroduction.aj 5712 2015-08-15 15:25:13Z epietrig $
 */

package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.DefaultIdentifiable;

import fr.inria.zuist.engine.Level;
import fr.inria.zuist.od.ObjectDescription;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.SceneBuilder;

/* copied from zuist-cluster */

aspect ObjIdIntroduction {
    declare parents: SceneManager extends DefaultIdentifiable;
    declare parents: SceneBuilder extends DefaultIdentifiable;
    declare parents: Region extends DefaultIdentifiable;
    declare parents: Level extends DefaultIdentifiable;
    declare parents: ObjectDescription extends DefaultIdentifiable;
}
