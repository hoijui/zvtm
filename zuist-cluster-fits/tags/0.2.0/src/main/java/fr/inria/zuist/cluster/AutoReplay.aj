/*
 *   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AutoReplay.aj 5261 2014-12-29 19:36:34Z fdelcampo $
 */

package fr.inria.zuist.cluster;

import java.awt.geom.Point2D;

import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.FitsImageDescription;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zuist.engine.JSkyFitsImageDescription;

import java.net.URL;

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
         execution(public URL FitsImageDescription.getSrc()) ||
         execution(public void FitsImageDescription.moveTo(double, double)) ||
         execution(public void FitsImageDescription.orientTo(double)) ||
         execution(public void FitsImageDescription.setVisible(boolean)) ||
         execution(public void JSkyFitsImageDescription.setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm)) ||
         execution(public void JSkyFitsImageDescription.setColorLookupTable(String)) ||
         execution(public VirtualSpace JSkyFitsImageDescription.getVirtualSpace()) ||
         execution(public void JSkyFitsImageDescription.setRescaleGlobal(double, double)) ||
         execution(public void JSkyFitsImageDescription.setRescaleGlobal(boolean)) || 
         execution(public void JSkyFitsImageDescription.rescale(double, double)) ||
         execution(public void JSkyFitsImageDescription.rescaleGlobal()) ||
         execution(public void JSkyFitsImageDescription.rescaleLocal()) ||
         execution(public double[] JSkyFitsImageDescription.getLocalScaleParams()) ||
         execution(public double[] JSkyFitsImageDescription.getGlobalScaleParams()) ||
         execution(public void JSkyFitsImageDescription.setTranslucency(float)) ||
         execution(public double JSkyFitsImageDescription.getWidth()) ||
         execution(public double JSkyFitsImageDescription.getHeight()) ||
         execution(public double JSkyFitsImageDescription.getX()) ||
         execution(public double JSkyFitsImageDescription.getY()) ||
         execution(public boolean JSkyFitsImageDescription.isVisible()) ||
         execution(public URL JSkyFitsImageDescription.getSrc()) ||
         execution(public void JSkyFitsImageDescription.moveTo(double, double)) ||
         execution(public void JSkyFitsImageDescription.orientTo(double)) ||
         execution(public void JSkyFitsImageDescription.setVisible(boolean))
        );
}

