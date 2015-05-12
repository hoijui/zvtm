/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2011-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import java.util.Vector;
import java.util.Arrays;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VSegment;

/**
 *<p>A picker that requires both VirtualSpace coordinates and View-projected coordinates.</p>
 *
 *<p>All glyphs can be picked by this type of picker. It is the default picker associated with VCursor.</p>
 *
 <p>When instantiating a Picker manually, that picker should be registered with the VirtualSpace
  in which it is going to perform picking operations so that it gets notified whenever glyphs get
  removed from the VirtualSpace and updates itself accordingly. This is not necessary for the
  picker associated with a View's VCursor, that gets created automatically.</p>
 */

public class Picker extends PickerVS {

    /**coords in JPanel*/
    protected int jpx, jpy;

    /** Picker constructor.
     * Instantiated with a default stack size of 50.
     */
    public Picker(){
        this(50);
    }

    /** Picker constructor.
     *@param stackSize start picked glyph stack size. Will double capacity if overflows.
     */
    public Picker(int stackSize){
        super(stackSize);
    }

    /* ------------ Picker location -------------- */

    /** Set picker's coordinates  (JPanel coordinates system).
     *@param x x-coordinate, in JPanel coordinates system
     *@param y y-coordinate, in JPanel coordinates system
     */
    public void setJPanelCoordinates(int x, int y){
        jpx = x;
        jpy = y;
    }

    /* ------------ Picker VSegments -------------- */

    /** Get a list of all VSegments picked at the picker's current coordinates.
     * Cursor coordinates are taken from the active layer's camera space.
     *@param c camera observing the segments of interest
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
  	 *@return an empty vector if none
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     */
    public Vector<VSegment> getIntersectingSegments(Camera c, int tolerance){
	    return getIntersectingSegments(c, jpx, jpy, tolerance);
    }

    /** Get a list of all VSegments picked at a given set of coordinates.
     *@param c camera observing the segments of interest
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@param x cursor x-coordinate in JPanel coordinates system
     *@param y cursor y-coordinate in JPanel coordinates system
  	 *@return an empty vector if none
     *@see #getIntersectingSegments(Camera c, int tolerance)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     */
    public static Vector<VSegment> getIntersectingSegments(Camera c, int x, int y, int tolerance){
        Vector res = new Vector();
        int index = c.getIndex();
        Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        Object glyph;
        for (int i=0;i<glyphs.size();i++){
            glyph = glyphs.elementAt(i);
            if ((glyph instanceof VSegment) && (intersectsSegment((VSegment)glyph, x, y, tolerance, index))){res.add(glyph);}
        }
        return res;
    }

    /** Tells whether the picker is hovering a particular VSegment or not.
     *@param camIndex indes of camera observing the segments of interest (available through Camera.getIndex())
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@see fr.inria.zvtm.engine.Camera#getIndex()
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #getIntersectingSegments(Camera c, int tolerance)
  	 *@return an empty vector if none
     */
    public boolean intersectsSegment(VSegment s, int tolerance, int camIndex){
	    return intersectsSegment(s, jpx, jpy, camIndex, tolerance);
    }

    /** Tells whether the picker is hovering a particular VSegment or not.
     *@param camIndex index of camera observing the segments of interest (available through Camera.getIndex())
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #getIntersectingSegments(Camera c, int tolerance)
  	 *@return an empty vector if none
     */
    public static boolean intersectsSegment(VSegment s, int x, int y, int tolerance, int camIndex){
	    return s.intersects(x, y, tolerance, camIndex);
    }

    @Override
	public Vector<Glyph> getIntersectingGlyphs(Camera c, boolean onlyGlyphsInViewport, String type){
        Vector res = new Vector();
        Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        Glyph glyph;
        for (int i=0;i<glyphs.size();i++){
            glyph = (Glyph)glyphs.elementAt(i);
            // ignore glyphs of other types than the one specified (if set)
            if (type != null && !glyph.getType().equals(type)){continue;}
            if (glyph.coordInside(jpx, jpy, c, vx, vy)){
                res.add(glyph);
            }
            else if (glyph instanceof VSegment && intersectsSegment((VSegment)glyph, 2, c.getIndex())){
                res.add(glyph);
            }
            else if (glyph instanceof DPath && intersectsPath((DPath)glyph, 2, c.getOwningView().getGraphicsContext())){
                res.add(glyph);
            }
        }
        return res;
    }

    public boolean computePickedGlyphList(Camera c){
        return this.computePickedGlyphList(c, jpx, jpy);
    }

    /** Compute the list of glyphs currently picked. */
    boolean computePickedGlyphList(Camera c, ViewPanel v){
//        if (v.lens != null){
//            // following use of cx,cy implies that VCursor.unProject() has been called before this method
//            return this.computePickedGlyphList(c, Math.round(cx + v.size.width/2), Math.round(v.size.height/2 - cy));
//        }
//        else {
            return this.computePickedGlyphList(c, jpx, jpy);
//        }
    }

    /** Compute the list of glyphs currently picked. */
    boolean computePickedGlyphList(Camera c, int x, int y){
        boolean res=false;
        Vector<Glyph> drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        try {
            for (int i=0;i<drawnGlyphs.size();i++){
                tmpGlyph = drawnGlyphs.elementAt(i);
                if (tmpGlyph.isSensitive() && checkGlyph(c, x, y)){
                    res = true;
                }
            }
        }
        catch (java.util.NoSuchElementException e){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("picker.computePickedGlyphList "+e);
                e.printStackTrace();
            }
        }
        catch (NullPointerException e2){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("picker.computePickedGlyphList null "+e2+
                    " (This might be caused by an error in enterGlyph/exitGlyph in your event handler)");
                e2.printStackTrace();
            }
        }
        return res;
    }

    boolean checkGlyph(Camera c, int x, int y){
        // Test if cursor inside, and fire entry/exit events for a given glyph
        if (tmpGlyph.coordInside(x, y, c, vx, vy)){
            //if the mouse is inside the glyph
            if (!prevMouseIn.containsKey(tmpGlyph)){
                //if it was not inside it last time, mouse has entered the glyph
                prevMouseIn.put(tmpGlyph, null);
                tmpRes = Glyph.ENTERED_GLYPH;
            }
            //if it was inside last time, nothing has changed
            else {tmpRes = Glyph.NO_CURSOR_EVENT;}
        }
        else {
            //if the mouse is not inside the glyph
            if (prevMouseIn.containsKey(tmpGlyph)){
                //if it was inside it last time, mouse has exited the glyph
                prevMouseIn.remove(tmpGlyph);
                tmpRes = Glyph.EXITED_GLYPH;
            }//if it was not inside last time, nothing has changed
            else {tmpRes = Glyph.NO_CURSOR_EVENT;}
        }
        if (tmpRes == Glyph.ENTERED_GLYPH){
            //we've entered this glyph
            maxIndex = maxIndex + 1;
            if (maxIndex >= pickedGlyphs.length){doubleCapacity();}
            pickedGlyphs[maxIndex] = tmpGlyph;
            lastGlyphEntered = tmpGlyph;
            if (pl != null){pl.enterGlyph(tmpGlyph);}
            return true;
        }
        else if (tmpRes == Glyph.EXITED_GLYPH){
            //we've exited it
            int j = 0;
            while (j <= maxIndex){
                if (pickedGlyphs[j++] == tmpGlyph){break;}
            }
            while (j <= maxIndex){
                pickedGlyphs[j-1] = pickedGlyphs[j];
                j++;
            }
            maxIndex = maxIndex - 1;
            /*required because list can be reset because we change layer and then we exit a glyph*/
            if (maxIndex<0){lastGlyphEntered = null;maxIndex = -1;}
            else {lastGlyphEntered = pickedGlyphs[maxIndex];}
            if (pl != null){pl.exitGlyph(tmpGlyph);}
            return true;
        }
        return false;
    }

}
