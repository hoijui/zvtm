/*
 *   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObjIdIntroduction.aj 781 2015-01-21 12:59:34Z epietrig $
 */

package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.DefaultIdentifiable;

import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

/* copied from zuist-cluster */

aspect ObjIdIntroduction {
    declare parents: SceneManager extends DefaultIdentifiable;
    declare parents: Region extends DefaultIdentifiable;
    declare parents: Level extends DefaultIdentifiable;
    declare parents: ObjectDescription extends DefaultIdentifiable;
}

