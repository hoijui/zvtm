/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObjectPicker.java 5516 2015-04-27 15:09:27Z epietrig $
 */

package fr.inria.zuist.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import fr.inria.zuist.event.PickedObjectListener;

/**
 <p>An object picker that tells what objects overlap the corresponding coordinates.
    The picker will only test for objects that are visible in the given level range.</p>
 <p> An ObjectPicker is instantiated as follows:</p>
<pre>
SceneManager sm = ...;
// create a picker that will only consider objects visible at ZUIST levels 0 through 1 (any of these levels or all of them)
ObjectPicker oPicker = sm.createObjectPicker(0,1);
oPicker.setListener(aListener);
</pre>
<p>with aListener a PickedObjectListener that gets notified whenever the picker enters or exits an Object.</p>
<p>An ObjectPicker is moved programmatically using setVSCoordinates(). Any VirtualSpace coordinates can be given to it.</p>
<p>One way to tie it to a VCursor is simply to call setVSCoordinates() in the mouseMoved listener associated with the corresponding ViewListener.</p>
<pre>
public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
  oPicker.setVSCoordinates(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
}
</pre>
 */

public class ObjectPicker extends ZuistPicker {

    static final short ENTERED_OBJECT = 1;
    static final short NO_EVENT = 0;
    static final short EXITED_OBJECT = -1;

    static final int DEFAULT_STACK_SIZE = 10;

    ObjectDescription[] candidateObjects = new ObjectDescription[0];
    ObjectDescription[] pickedObjects;
    ObjectDescription lastObjectEntered;

    protected int maxIndex = -1;

    HashMap<ObjectDescription,Object> prevPickerIn = new HashMap(DEFAULT_STACK_SIZE);

    // listen to enter/exit events
    PickedObjectListener ol;

    ObjectPicker(SceneManager sm, int tl, int bl){
        this.sm = sm;
        setLevelRange(tl, bl);
        pickedObjects = new ObjectDescription[DEFAULT_STACK_SIZE];
        updateCandidateObjects();
    }

    /** This method should be called whenever ObjectDescriptions are added/removed in the level range considered for picking.*/
    public void updateCandidateObjects(){
        synchronized(candidateObjects){
            Vector<ObjectDescription> v = new Vector();
            for (int i=topLevel;i<=bottomLevel;i++){
                if (i < sm.levels.length){
                    // we know that i >= 0 as topLevel is checked accordingly
                    for (Region r:sm.levels[i].regions){
                        for (ObjectDescription od:r.getObjectsInRegion()){
                            if (!v.contains(od)){
                                v.add(od);
                            }
                        }
                    }
                }
            }
            candidateObjects = v.toArray(new ObjectDescription[v.size()]);
        }
    }

    public void setListener(PickedObjectListener ol){
        this.ol = ol;
    }

    public PickedObjectListener getListener(){
        return this.ol;
    }

    @Override
    public void setVSCoordinates(double x, double y){
        super.setVSCoordinates(x, y);
        computePickedObjectList();
    }

    @Override
    public void setLevelRange(int tl, int bl){
        super.setLevelRange(tl, bl);
        updateCandidateObjects();
    }

    /** Double capacity of array containing ObjectDescriptions picked.
     * Mechanism similar to what Vectors do, bu we want to avoid casting.
     */
    void doubleCapacity(){
        ObjectDescription[] tmpArray = new ObjectDescription[pickedObjects.length*2];
        System.arraycopy(pickedObjects, 0, tmpArray, 0, pickedObjects.length);
        pickedObjects = tmpArray;
    }

    /** Reset the list of glyphs under the cursor. */
    public void reset(){
        Arrays.fill(pickedObjects, null);
        maxIndex = -1;
        lastObjectEntered = null;
        prevPickerIn.clear();
    }


    /** Get the list of ObjectDescriptions currently picked. Last entry is last ObjectDescription entered.
     * This returns a <em>copy</em> of the actual array managed by the picker at the time the method is called.
     * In other words, the array returned by this method is not synchronized with the actual list over time.
     *@return an empty array if the picker is not over any ObjectDescription.
     */
    public ObjectDescription[] getPickedObjectList(){
        if (maxIndex >= 0){
            ObjectDescription[] res = new ObjectDescription[maxIndex+1];
            System.arraycopy(pickedObjects, 0, res, 0, maxIndex+1);
            return res;
        }
        else return new ObjectDescription[0];
    }

    /** Compute the list of ObjectDescriptions currently picked.
     */
    public void computePickedObjectList(){
        boolean res = false;
        try {
            synchronized(candidateObjects){
                for (ObjectDescription od:candidateObjects){
                    checkObject(od);
                }
            }
        }
        catch (java.util.NoSuchElementException e){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("ObjectPicker.compterPickedObjectDescriptionList: "+e);
                e.printStackTrace();
            }
        }
        catch (NullPointerException e2){
            if (VirtualSpaceManager.debugModeON()){
                System.err.println("ObjectPicker.compterPickedObjectDescriptionList: "+e2+
                    " (This might be caused by an error in your ObjectDescription listener)");
                e2.printStackTrace();
            }
        }
    }

    boolean checkObject(ObjectDescription od){
        // test if picker inside, and fire entry/exit events for a given ObjectDescription
        if (od.coordInside(vx, vy)){
            // if the picker is inside the ObjectDescription
            if (!prevPickerIn.containsKey(od)){
                // if it was not inside it last time, picker has entered the ObjectDescription
                prevPickerIn.put(od, null);
                maxIndex = maxIndex + 1;
                if (maxIndex >= pickedObjects.length){doubleCapacity();}
                pickedObjects[maxIndex] = od;
                lastObjectEntered = od;
                if (ol != null){ol.enteredObject(od);}
                return true;
            }
        }
        else {
            // if the picker is not inside the ObjectDescription
            if (prevPickerIn.containsKey(od)){
                // if it was inside it last time, picker has exited the ObjectDescription
                prevPickerIn.remove(od);
                int j = 0;
                while (j <= maxIndex){
                    if (pickedObjects[j++] == od){break;}
                }
                while (j <= maxIndex){
                    pickedObjects[j-1] = pickedObjects[j];
                    j++;
                }
                maxIndex = maxIndex - 1;
                if (maxIndex < 0){
                    lastObjectEntered = null;
                    maxIndex = -1;
                }
                else {
                    lastObjectEntered = pickedObjects[maxIndex];
                }
                if (ol != null){ol.exitedObject(od);}
                return true;
            }
        }
        return false;
    }

}
