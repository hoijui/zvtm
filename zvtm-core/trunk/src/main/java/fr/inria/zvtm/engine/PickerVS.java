/*   Copyright (c) INRIA, 2015. All Rights Reserved
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
import java.util.HashMap;

import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VSegment;

/**
 *<p>A picker that requires VirtualSpace coordinates only.</p>
 *<p>List of Glyphs that can be picked by PickerVS at this time:</p>
 *<ul>
 *<li>AdaptiveText</li>
 *<li>DPath</li>
 *<li>FRectangle</li>
 *<li>MultilineText</li>
 *<li>PCircle</li>
 *<li>PRectangle</li>
 *<li>RImage</li>
 *<li>VCircle</li>
 *<li>VImage</li>
 *<li>VPoint</li>
 *<li>VRectangle</li>
 *<li>VRoundRect</li>
 *<li>VText</li>
 *<li>VTextLayout</li>
 *</ul>
 */

public class PickerVS {

    HashMap<Glyph,Object> prevMouseIn = new HashMap();

    /**coord in virtual space*/
    protected double vx,vy;

    //used in computeMouseOverGlyph
    protected Glyph tmpGlyph;
    //used in computeMouseOverGlyph
    protected short tmpRes;
    //used in computeMouseOverGlyph
    protected int maxIndex = -1;

    /** List of glyphs picked at the pickers coordinates.
        Last entry is last glyph entered.
        IMPORTANT: elements beyond maxIndex might not be up to date. Do not trust the value, especially if not null.
        */
    Glyph[] pickedGlyphs;

    /** Last glyph picked. */
    Glyph lastGlyphEntered = null;

    // listen to glyph enter/exit events
    PickerListener pl;

    /** Picker constructor.
     * Instantiated with a default stack size of 50.
     */
    public PickerVS(){
        this(50);
    }

    /** Picker constructor.
     *@param stackSize start picked glyph stack size. Will double capacity if overflows.
     */
    public PickerVS(int stackSize){
       pickedGlyphs = new Glyph[stackSize];
    }

    public Glyph lastGlyphEntered(){
        return lastGlyphEntered;
    }

    void resetMouseIn(Glyph g){
        // if (prevMouseIn.containsKey(g)){
            prevMouseIn.remove(g);
        // }
    }

    /** Set picker's coordinates  (virtual space coordinates system).
     *@param x x-coordinate, in virtual space coordinates system
     *@param y y-coordinate, in virtual space coordinates system
     */
    public void setVSCoordinates(double x, double y){
        vx = x;
        vy = y;
    }

    public void setListener(PickerListener pl){
        this.pl = pl;
    }

    public PickerListener getListener(){
        return this.pl;
    }

    /* ------------ Pick DPaths -------------- */

    /** Get a list of all DPaths picked at a given set of coordinates.
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@param x picker x-coordinate, in virtual space coordinates system
     *@param y picker y-coordinate, in virtual space coordinates system
  	 *@return an empty vector if none
     *@see #getIntersectingPaths(Camera c)
	 */
    public static Vector<DPath> getIntersectingPaths(Camera c, int tolerance, double x, double y){
        Vector res = new Vector();
        Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        Object glyph;
        Graphics2D g2d = c.getOwningView().getGraphicsContext();
        for (int i=0;i<glyphs.size();i++){
            glyph = glyphs.elementAt(i);
            if ((glyph instanceof DPath) && intersectsPath((DPath)glyph, tolerance, x, y, g2d)){res.add(glyph);}
        }
        return res;
    }

    /** Get a list of all DPaths picked at the picker's current coordinates.
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
  	 *@return an empty vector if none
     *@see #getIntersectingPaths(Camera c, int tolerance, double cursorX, double cursorY)
     */
    public Vector<DPath> getIntersectingPaths(Camera c){
	    return getIntersectingPaths(c, 5, vx, vy);
    }

    /** Get a list of all DPaths picked at the picker's current coordinates.
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
  	 *@return an empty vector if none
     *@see #getIntersectingPaths(Camera c, int tolerance, double cursorX, double cursorY)
     */
    public Vector<DPath> getIntersectingPaths(Camera c, int tolerance){
		return getIntersectingPaths(c, tolerance, vx, vy);
    }

    static final Rectangle pickingWindow = new Rectangle(0,0,1,1);

    /** Tells whether the picker is hovering a particular DPath or not.
     *@param p DPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@param x picker x-coordinate, in virtual space coordinates system
     *@param y picker y-coordinate, in virtual space coordinates system
     *@see #intersectsPath(DPath p, int tolerance, Graphics2D g2d)
     */
	public static boolean intersectsPath(DPath p, int tolerance, double x, double y, Graphics2D g2d){
		if (!p.coordsInsideBoundingBox(x, y)){return false;}
        pickingWindow.setRect(x-tolerance, y-tolerance, 2*tolerance, 2*tolerance);
        return g2d.hit(pickingWindow, p.getJava2DGeneralPath(), true);
	}

    /** Tells whether the picker is hovering a particular DPath or not.
     *@param p DPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@see #intersectsPath(DPath p, int tolerance, double x, double y, Graphics2D g2d)
     */
    public boolean intersectsPath(DPath p, int tolerance, Graphics2D g2d){
		return intersectsPath(p, tolerance, vx, vy, g2d);
    }

    /** Get a list of all Glyphs (including segments and paths) picked.
     * This method is especially useful when the camera of interest is not the active camera for the associated view (i.e. another layer is active).
     * Beware of the fact that this method returns glyphs of any kind, not just ClosedShape instances.
     * It can thus be much more computationaly expensive than getpickedGlyphList()
     *@param c a camera (the active camera can be obtained by VirtualSpaceManager.getActiveCamera())
     *@param type the type of glyph to look for (pass null to look for any type of glyph). Type of glyph as specified with Glyph.setType().
     *@return a list of glyphs under the mouse cursor, sorted by drawing order.
     *@see #getIntersectingGlyphs(Camera c)
     *@see #getPickedGlyphList()
     */
    public Vector<Glyph> getIntersectingGlyphs(Camera c, String type){
        Vector res = new Vector();
        Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        Glyph glyph;
        for (int i=0;i<glyphs.size();i++){
            glyph = (Glyph)glyphs.elementAt(i);
            // ignore glyphs of other types than the one specified (if set)
            if (type != null && !glyph.getType().equals(type)){continue;}
            if (glyph.coordInsideV(vx, vy, c.getIndex())){
                res.add(glyph);
            }
            // else if (glyph instanceof VSegment && intersectsSegment((VSegment)glyph, 2, c.getIndex())){
            //     res.add(glyph);
            // }
            else if (glyph instanceof DPath && intersectsPath((DPath)glyph, 2, c.getOwningView().getGraphicsContext())){
                res.add(glyph);
            }
        }
        return res;
    }

    /** Get a list of all Glyphs (including segments and paths) picked.
     * This method is especially useful when the camera of interest is not the active camera for the associated view (i.e. another layer is active).
     * Beware of the fact that this method returns glyphs of any kind, not just ClosedShape instances.
     * It can thus be much more computationaly expensive than getpickedGlyphList()
     *@param c a camera (the active camera can be obtained by VirtualSpaceManager.getActiveCamera())
     *@return a list of glyphs under the mouse cursor, sorted by drawing order; null if no object under the cursor.
     *@see #getIntersectingGlyphs(Camera c, String type)
     *@see #getPickedGlyphList()
     */
    public Vector<Glyph> getIntersectingGlyphs(Camera c){
        return getIntersectingGlyphs(c, null);
    }

    /** Compute the list of glyphs currently picked.
     *@param c camera observing the glyphs of potential interest in the View.
     */
    public boolean computePickedGlyphList(Camera c){
        boolean res = false;
        Vector<Glyph> drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        try {
            for (int i=0;i<drawnGlyphs.size();i++){
                tmpGlyph = drawnGlyphs.elementAt(i);
                if (tmpGlyph.isSensitive() && checkGlyph(c)){
                    res = true;
                }
            }
        }
        catch (java.util.NoSuchElementException e){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("pickerVS.computePickedGlyphList "+e);
                e.printStackTrace();
            }
        }
        catch (NullPointerException e2){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("pickerVS.computePickedGlyphList null "+e2+
                    " (This might be caused by an error in enterGlyph/exitGlyph in your event handler)");
                e2.printStackTrace();
            }
        }
        return res;
    }

    boolean checkGlyph(Camera c){
        // Test if cursor inside, and fire entry/exit events for a given glyph
        //XXX: temporary fix: sending MIN_VALUE for jpx,jpy as we have no idea what those are.
        // But once all Glyphs implement picking in VirtualSpace coordinates,
        // we can get rid of projected coordinate parameters.
        if (tmpGlyph.coordInside(Integer.MIN_VALUE, Integer.MIN_VALUE, c.getIndex(), vx, vy)){
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

    /** Double capacity of array containing glyphs under the cursor. Mechanism similar to what Vectors do, bu we want to avoid casting. */
    void doubleCapacity(){
        Glyph[] tmpArray = new Glyph[pickedGlyphs.length*2];
        System.arraycopy(pickedGlyphs, 0, tmpArray, 0, pickedGlyphs.length);
        pickedGlyphs = tmpArray;
    }

    /** Reset the list of glyphs under the cursor. */
    void resetPickedGlyphsList(){
        Arrays.fill(pickedGlyphs, null);
        maxIndex = -1;
        lastGlyphEntered = null;
        prevMouseIn.clear();
    }

    /** Get the list of glyphs currently picked. Last entry is last glyph entered.
     * This returns a <em>copy</em> of the actual array managed by the picker at the time the method is called.
     * In other words, the array returned by this method is not synchronized with the actual list over time.
     *@return an empty array if the picker is not over any object.
     *@see #getPickedGlyphList(String type)
	 *@see #getIntersectingGlyphs(Camera c)
     *@see #getIntersectingGlyphs(Camera c, String type)
     */
    public Glyph[] getPickedGlyphList(){
        if (maxIndex >= 0){
            Glyph[] res = new Glyph[maxIndex+1];
            System.arraycopy(pickedGlyphs, 0, res, 0, maxIndex+1);
            return res;
        }
        else return new Glyph[0];
    }

    /** Get the list of glyphs currently picked. Last entry is last glyph entered.
     * This returns a <em>copy</em> of the actual array managed by the picker at the time the method is called.
     * In other words, the array returned by this method is not synchronized with the actual list over time.
     *@param type the type of glyph to look for. Type of glyph as specified with Glyph.setType().
     *@return an empty array if the picker is not over any object.
     *@see #getPickedGlyphList()
     *@see #getIntersectingGlyphs(Camera c)
     *@see #getIntersectingGlyphs(Camera c, String type)
     */
    public Glyph[] getPickedGlyphList(String type){
        if (maxIndex >= 0){
            Vector<Glyph> gV = new Vector(maxIndex+1);
            synchronized(pickedGlyphs){
                for (int i=0;i<=maxIndex;i++){
                    if (pickedGlyphs[i].getType() != null && pickedGlyphs[i].getType().equals(type)){
                        gV.add(pickedGlyphs[i]);
                    }
                }
            }
            return gV.toArray(new Glyph[gV.size()]);
        }
        else return new Glyph[0];
    }

    /** Tells whether a given glyph is under this picker or not. */
    public boolean isPicked(Glyph g){
        for (int i=0;i<=maxIndex;i++){
            if (pickedGlyphs[i] == g){return true;}
        }
        return false;
    }

    /** Remove glyph g in list of glyphs under picker if it is present. Called when destroying a glyph. */
    void removeGlyphFromList(Glyph g){
	    int i = 0;
        boolean present = false;
        while (i<=maxIndex){
            if (pickedGlyphs[i++]==g){present = true;break;}
        }
        while (i<=maxIndex){
            pickedGlyphs[i-1] = pickedGlyphs[i];
            i++;
        }
        if (present){
            maxIndex = maxIndex - 1;
            if (maxIndex<0){lastGlyphEntered = null;maxIndex = -1;}
            else {lastGlyphEntered = pickedGlyphs[maxIndex];}
        }
    }

    /** Print list of glyphs under cursor on System.err for debugging. */
    public void printList(){
        System.err.print("[");
        for (int i=0;i<=maxIndex;i++){
            System.err.print(pickedGlyphs[i].hashCode()+",");
        }
        System.err.println("]");
    }

    /**
     * The list of glyphs under the picker, ordered according to the drawing stack
     */
    public Glyph[] getDrawOrderedPickedGlyphList(VirtualSpace v){
    	Glyph[] tt = getPickedGlyphList();
    	Glyph[] t = new Glyph[tt.length];
		int k = 0;
		Glyph[] list = v.getDrawingList();
		for (int i = 0; i < list.length; i++) {
			if (contains(tt,list[i]) && !contains(t,list[i])){
                t[k++] = list[i];
            }
		}
		return t;
    }

    /**Returns the glyph under the picker (drawing order)*/
    public Glyph pickOnTop(VirtualSpace v){
    	Glyph[] list = getDrawOrderedPickedGlyphList(v);
    	if(list.length == 0){
            return null;
        }
    	return list[list.length-1];
    }

	private boolean contains(Glyph[] tab, Glyph g){
		for (int i = 0; i < tab.length; i++) {
			if(tab[i]==g){
				return true;
			}
		}
		return false;
	}

}
