/*
 *   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.cluster;

import java.awt.geom.Point2D;

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
        if(replayTarget.isReplicated()) &&
        (
         execution(public void SceneManager.reset()) ||
         execution(public void SceneManager.setUpdateLevel(boolean)) ||
         execution(public void SceneManager.setOrigin(Point2D.Double)) ||
         execution(public void SceneManager.enableRegionUpdater(boolean)) ||
         execution(public void SceneManager.updateVisibleRegions()) ||
         execution(public void Region.setContainingRegion(Region)) ||
         execution(public void Region.addContainedRegion(Region)) 
        );
}

