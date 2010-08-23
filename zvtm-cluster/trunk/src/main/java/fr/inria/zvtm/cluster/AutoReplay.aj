/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RectangularShape;
import fr.inria.zvtm.glyphs.VSlice;
import fr.inria.zvtm.glyphs.VText;

import java.awt.Color;
import java.awt.Font;
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
         execution(public void Glyph.move(double, double))	||
         execution(public void Glyph.moveTo(double, double))	||
         execution(public void Glyph.setColor(Color))	||
         execution(public void Glyph.setStrokeWidth(float))	||
         execution(public void Glyph.setTranslucencyValue(float)) || 
         execution(public void Glyph.setMouseInsideHighlightColor(Color)) ||
         execution(public void Glyph.setVisible(boolean)) ||
         execution(public void Glyph.stick(Glyph)) ||
         execution(public static void Glyph.stickToGlyph(Glyph, Glyph)) ||
         execution(public void Glyph.orientTo(float)) ||
         execution(public void Glyph.setSensitivity(boolean)) ||
         execution(public void VSlice.setAngle(double)) || 
         execution(public void VText.setSpecialFont(Font)) || 
         execution(public void VText.setText(String)) || 
         execution(public void VText.setScale(float)) || 
         execution(public void ClosedShape.setDrawBorder(boolean)) || 
         execution(public void ClosedShape.setFilled(boolean)) || 
         execution(public void DPath.addSegment(double, double, boolean)) ||  
         execution(public void DPath.addCbCurve(double, double, double, double, double, double, boolean)) ||  
         execution(public void DPath.addQdCurve(double, double, double, double, boolean)) ||  
         execution(public void DPath.edit(Point2D.Double[], boolean)) ||  
         execution(public void RectangularShape.setHeight(double)) ||  
         execution(public void RectangularShape.setWidth(double)) ||
         execution(public void VirtualSpace.show(Glyph)) ||
         execution(public void VirtualSpace.hide(Glyph)) ||

        //Camera methods
         execution(public void Camera.setZoomFloor(double))
        );
    }

