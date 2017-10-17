/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RectangularShape;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VEclipse;

import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;

/**
 * Define methods that will be replayed automatically
 * on replicated objects.
 * Autoreplay is a quick way of propagating changes to
 * remote objects without writing Delta classes.
 * Use only for "atomic" operations (change one attribute at a time).
 */
public aspect AutoReplay extends AbstractAutoReplay {
    //Rules to observe in order to modify this pointcut:
    // - only add execution join points
    // - every parameter of every method join point must be
    // serializable (primitive types are okay)
    // - exercise caution when adding non-public methods to the
    // join points, because these methods will be invoked reflectively.
    public pointcut autoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(replayTarget.isReplicated()) &&
        (
         //Glyph methods

         //Glyph.move and Glyph.moveTo moved to a static Delta (see GlyphReplication)
         //execution(public void Glyph.move(double, double))	||
         //execution(public void Glyph.moveTo(double, double))	||
         execution(public void Glyph.reSize(double))	||
         execution(public void Glyph.sizeTo(double))	||
         execution(public void Glyph.setBorderColor(Color))	||
         execution(public void Glyph.setColor(Color))	||
         execution(public void Glyph.highlight(boolean, Color))	||
         execution(public void Glyph.setTranslucencyValue(float)) ||
         execution(public void Glyph.setMouseInsideHighlightColor(Color)) ||
         execution(public void Glyph.setVisible(boolean)) ||
         execution(public void Glyph.orientTo(double)) ||
         execution(public void Glyph.setSensitivity(boolean)) ||
         //Glyph.setStroke moved to a static Delta that performs wrapping if possible
         //execution(public void Glyph.setStroke(Stroke)) ||
         //VSegment.setEndPoints moved to a static delta for performence...
         //execution(public void VSegment.setEndPoints(double, double, double, double)) ||
         execution(public void VText.setFont(Font)) ||
         execution(public void VText.setText(String)) ||
         execution(public void VText.setScale(float)) ||
         execution(public void VText.setScaleIndependent(boolean)) ||
         execution(public void ClosedShape.setDrawBorder(boolean)) ||
         execution(public void ClosedShape.setFilled(boolean)) ||
         //DPath.addSegment moved  to a static Delta (see GlyphReplication)
         //execution(public void DPath.addSegment(double, double, boolean)) ||
         execution(public void DPath.addCbCurve(double, double, double, double, double, double, boolean)) ||
         execution(public void DPath.addQdCurve(double, double, double, double, boolean)) ||
         execution(public void DPath.setOutline(Color,int)) ||
         execution(public void DPath.setDrawingMethod(short)) ||
         //DPath.edit moved to a static Delta (see GlyphReplication)
         //execution(public void DPath.edit(Point2D.Double[], boolean)) ||
         execution(public void RectangularShape.setHeight(double)) ||
         execution(public void RectangularShape.setWidth(double)) ||
         execution(public void VRoundRect.setArcWidth(double)) ||
         execution(public void VRoundRect.setArcHeight(double)) ||
         execution(public void VImage.setZoomSensitive(boolean)) ||
         execution(public void VEclipse.setFraction(float)) ||
         execution(public void VirtualSpace.show(Glyph)) ||
         execution(public void VirtualSpace.hide(Glyph)) ||

        //Camera methods
         execution(public void Camera.setZoomFloor(double)) ||

         // Portal
         execution(public void Portal.setVisible(boolean)) ||
         execution(public void CameraPortal.setTranslucencyValue(float)) ||
         execution(public void CameraPortal.setBorder(Color)) ||
         execution(public void CameraPortal.setBackgroundColor(Color)) ||
         execution(public void CameraPortal.setBorderWidth(float)) ||
         execution(public void CameraPortal.setMaxBufferSizeRatios(double, double)) ||
         execution(public void DraggableCameraPortal.setDragBarHeight(int)) ||
         execution(public void DraggableCameraPortal.setDragBarColor(Color)) ||
         execution(public void OverviewPortal.drawObservedRegionLocator(boolean)) ||
         execution(public void OverviewPortal.setObservedRegionColor(Color)) ||
         execution(public void OverviewPortal.setObservedRegionColor(int, Color)) ||
         execution(public void OverviewPortal.setObservedRegionTranslucency(float)) ||
         execution(public void OverviewPortal.setObservedRegionBorderWidth(float))
        );
    }
