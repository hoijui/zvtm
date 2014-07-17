/*
 *   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.cluster;

import java.awt.geom.Point2D;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.FitsImageDescription;

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
         execution(public void Region.addContainedRegion(Region)) ||
         execution(public void FitsImageDescription.setScaleMethod(FitsImage.ScaleMethod)) ||
         execution(public void FitsImageDescription.setColorFilter(FitsImage.ColorFilter)) ||
         execution(public VirtualSpace FitsImageDescription.getVirtualSpace()) ||
         execution(public void FitsImageDescription.setRescaleGlobal(double, double)) ||
         execution(public void FitsImageDescription.setRescaleGlobal(boolean)) || 
         execution(public void FitsImageDescription.rescale(double, double, double)) ||
         execution(public void FitsImageDescription.rescaleGlobal()) ||
         execution(public void FitsImageDescription.rescaleLocal()) ||
         execution(public double[] FitsImageDescription.getLocalScaleParams()) ||
         execution(public void FitsImageDescription.setTranslucency(float)) ||
         execution(public String FitsImageDescription.getObjectName()) ||
         execution(public double FitsImageDescription.getWidth()) ||
         execution(public double FitsImageDescription.getHeight()) ||
         execution(public double FitsImageDescription.getX()) ||
         execution(public double FitsImageDescription.getY()) ||
         execution(public boolean FitsImageDescription.isVisible()) ||
         execution(public void FitsImageDescription.setVisible(boolean))
        );
}

