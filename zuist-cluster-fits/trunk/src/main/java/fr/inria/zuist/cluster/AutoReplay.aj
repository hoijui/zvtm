/*
 *   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.cluster;

import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.cluster.AbstractAutoReplay;
import fr.inria.zvtm.cluster.Identifiable;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zuist.od.JSkyFitsImageDescription;
import fr.inria.zvtm.glyphs.JSkyFitsHistogram;
import java.awt.Color;
import fr.inria.zvtm.glyphs.VRectangle;


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
         execution(public void JSkyFitsImageDescription.setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm, boolean)) ||
         execution(public void JSkyFitsImageDescription.setColorLookupTable(String, boolean)) ||
         execution(public VirtualSpace JSkyFitsImageDescription.getVirtualSpace()) ||
         execution(public void JSkyFitsImageDescription.setRescaleGlobal(double, double)) ||
         execution(public void JSkyFitsImageDescription.setRescaleGlobal(boolean)) ||
         execution(public void JSkyFitsImageDescription.rescale(double, double, boolean)) ||
         execution(public void JSkyFitsImageDescription.rescaleGlobal()) ||
         execution(public void JSkyFitsImageDescription.rescaleLocal()) ||
         execution(public double[] JSkyFitsImageDescription.getLocalScaleParams()) ||
         execution(public double[] JSkyFitsImageDescription.getGlobalScaleParams()) ||
         execution(public void JSkyFitsImageDescription.setTranslucencyValue(float)) ||
         execution(public double JSkyFitsImageDescription.getWidth()) ||
         execution(public double JSkyFitsImageDescription.getHeight()) ||
         execution(public double JSkyFitsImageDescription.getX()) ||
         execution(public double JSkyFitsImageDescription.getY()) ||
         execution(public boolean JSkyFitsImageDescription.isVisible()) ||
         execution(public URL JSkyFitsImageDescription.getSrc()) ||
         execution(public void JSkyFitsImageDescription.moveTo(double, double)) ||
         execution(public void JSkyFitsImageDescription.orientTo(double)) ||
         execution(public void JSkyFitsImageDescription.setVisible(boolean)) ||
         execution(private void JSkyFitsHistogram.initBars(int[], int, int, Color)) ||
         execution(public JSkyFitsHistogram JSkyFitsHistogram.fromFitsImage(JSkyFitsImage)) ||
         execution(public JSkyFitsHistogram JSkyFitsHistogram.fromFitsImage(JSkyFitsImage, Color)) ||
         execution(public void JSkyFitsHistogram.moveTo(double, double)) ||
         execution(public VRectangle[] JSkyFitsHistogram.getBars())
        );
}
