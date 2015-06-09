/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import fr.inria.zuist.event.RegionListener;

/**
 <p>A region picker that tells what regions overlap the corresponding coordinates.
    The picker will only test for regions that are visible in the given level range.</p>
 <p> A RegionPicker is instantiated as follows:</p>
<pre>
SceneManager sm = ...;
// create a picker that will only consider regions visible at ZUIST levels 3 through 5 (any of these levels or all of them)
RegionPicker rPicker = sm.createRegionPicker(3,5);
rPicker.setListener(aListener);
</pre>
<p>with aListener a RegionListener that gets notified whenever the picker enters or exits a Region.</p>
<p>A RegionPicker is moved programmatically using setVSCoordinates(). Any VirtualSpace coordinates can be given to it.</p>
<p>One way to tie it to a VCursor is simply to call setVSCoordinates() in the mouseMoved listener associated with the corresponding ViewListener.</p>
<pre>
public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
  rPicker.setVSCoordinates(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
}
</pre>
 */

public class RegionPicker extends ZuistPicker {

    static final short ENTERED_REGION = 1;
    static final short NO_EVENT = 0;
    static final short EXITED_REGION = -1;

    static final int DEFAULT_STACK_SIZE = 10;

    Region[] candidateRegions = new Region[0];
    Region[] pickedRegions;
    Region lastRegionEntered;

    protected int maxIndex = -1;

    HashMap<Region,Object> prevPickerIn = new HashMap(DEFAULT_STACK_SIZE);

    // listen to enter/exit events
    RegionListener rl;

    RegionPicker(SceneManager sm, int tl, int bl){
        this.sm = sm;
        setLevelRange(tl, bl);
        pickedRegions = new Region[DEFAULT_STACK_SIZE];
        updateCandidateRegions();
    }

    /** This method should be called whenever regions are added/removed in the level range considered for picking.*/
    public void updateCandidateRegions(){
        synchronized(candidateRegions){
            Vector<Region> v = new Vector();
            for (int i=topLevel;i<=bottomLevel;i++){
                if (i < sm.levels.length){
                    // we know that i >= 0 as topLevel is checked accordingly
                    for (Region r:sm.levels[i].regions){
                        if (!v.contains(r)){
                            v.add(r);
                        }
                    }
                }
            }
            candidateRegions = v.toArray(new Region[v.size()]);
        }
    }

    public void setListener(RegionListener rl){
        this.rl = rl;
    }

    public RegionListener getListener(){
        return this.rl;
    }

    @Override
    public void setVSCoordinates(double x, double y){
        super.setVSCoordinates(x, y);
        computePickedRegionList();
    }

    @Override
    public void setLevelRange(int tl, int bl){
        super.setLevelRange(tl, bl);
        updateCandidateRegions();
    }

    /** Double capacity of array containing regions picked.
     * Mechanism similar to what Vectors do, bu we want to avoid casting.
     */
    void doubleCapacity(){
        Region[] tmpArray = new Region[pickedRegions.length*2];
        System.arraycopy(pickedRegions, 0, tmpArray, 0, pickedRegions.length);
        pickedRegions = tmpArray;
    }

    /** Reset the list of glyphs under the cursor. */
    public void reset(){
        Arrays.fill(pickedRegions, null);
        maxIndex = -1;
        lastRegionEntered = null;
        prevPickerIn.clear();
    }


    /** Get the list of regions currently picked. Last entry is last region entered.
     * This returns a <em>copy</em> of the actual array managed by the picker at the time the method is called.
     * In other words, the array returned by this method is not synchronized with the actual list over time.
     *@return an empty array if the picker is not over any region.
     */
    public Region[] getPickedRegionList(){
        if (maxIndex >= 0){
            Region[] res = new Region[maxIndex+1];
            System.arraycopy(pickedRegions, 0, res, 0, maxIndex+1);
            return res;
        }
        else return new Region[0];
    }

    /** Compute the list of Regions currently picked.
     */
    public void computePickedRegionList(){
        boolean res = false;
        try {
            synchronized(candidateRegions){
                for (Region r:candidateRegions){
                    checkRegion(r);
                }
            }
        }
        catch (java.util.NoSuchElementException e){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("RegionPicker.compterPickedRegionList: "+e);
                e.printStackTrace();
            }
        }
        catch (NullPointerException e2){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("RegionPicker.compterPickedRegionList: "+e2+
                    " (This might be caused by an error in your region listener)");
                e2.printStackTrace();
            }
        }
    }

    boolean checkRegion(Region r){
        // test if picker inside, and fire entry/exit events for a given Region
        if (r.coordInside(vx, vy)){
            // if the picker is inside the region
            if (!prevPickerIn.containsKey(r)){
                // if it was not inside it last time, picker has entered the region
                prevPickerIn.put(r, null);
                maxIndex = maxIndex + 1;
                if (maxIndex >= pickedRegions.length){doubleCapacity();}
                pickedRegions[maxIndex] = r;
                lastRegionEntered = r;
                if (rl != null){rl.enteredRegion(r);}
                return true;
            }
        }
        else {
            // if the picker is not inside the region
            if (prevPickerIn.containsKey(r)){
                // if it was inside it last time, picker has exited the region
                prevPickerIn.remove(r);
                int j = 0;
                while (j <= maxIndex){
                    if (pickedRegions[j++] == r){break;}
                }
                while (j <= maxIndex){
                    pickedRegions[j-1] = pickedRegions[j];
                    j++;
                }
                maxIndex = maxIndex - 1;
                if (maxIndex < 0){
                    lastRegionEntered = null;
                    maxIndex = -1;
                }
                else {
                    lastRegionEntered = pickedRegions[maxIndex];
                }
                if (rl != null){rl.exitedRegion(r);}
                return true;
            }
        }
        return false;
    }

}
