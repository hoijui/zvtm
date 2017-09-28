/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2016. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.engine.VirtualSpace;

import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.od.ObjectDescription;

/** A region in the scene. Regions contain objects. Regions belong to levels, and can span one or more contiguous level(s).
 *@author Emmanuel Pietriga
 */

public class Region {

    /** Objects appear gradually (animated fade-in transition). */
    public static final short FADE_IN = 0;
    /** Objects disappear gradually (animated fade-out transition). */
    public static final short FADE_OUT = 1;
    /** Objects appear immediately (no animated transition). */
    public static final short APPEAR = 2;
    /** Objects disappear immediately (no animated transition). */
    public static final short DISAPPEAR = 3;
    /** Objects appear gradually (animated fade-in transition), keyword in ZUIST XML vocabulary. */
    public static final String FADE_IN_STR = "fadein";
    /** Objects disappear gradually (animated fade-out transition), keyword in ZUIST XML vocabulary.. */
    public static final String FADE_OUT_STR = "fadeout";
    /** Objects disappear immediately (no animated transition), keyword in ZUIST XML vocabulary. */
    public static final String APPEAR_STR = "appear";
    /** Objects disappear immediately (no animated transition), keyword in ZUIST XML vocabulary. */
    public static final String DISAPPEAR_STR = "disappear";

    /** Entering region: approaching from upper level. */
    public static final short TFUL = 0;
    /** Entering region: approaching from lower level. */
    public static final short TFLL = 1;
    /** Leaving region: going to upper level. */
    public static final short TTUL = 2;
    /** Leaving region: going to lower level. */
    public static final short TTLL = 3;
    /** Entering/leaving region: already in level range (transition at same level).
     Not stored, always appear/disappear as this is always off screen. */
    public static final short TASL = 4;

    /** Default transition when leaving region. */
    static short DEFAULT_T_TRANSITION = DISAPPEAR;
    /** Default transition when entering region. */
    static short DEFAULT_F_TRANSITION = APPEAR;

    /* Set default transition when none specified.
     *@param comingFrom one of {APPEAR, FADE_IN}
     *@param goingTo one of {DISAPPEAR, FADE_OUT}
     */
    public static void setDefaultTransitions(short comingFrom, short goingTo){
        DEFAULT_F_TRANSITION = comingFrom;
        DEFAULT_T_TRANSITION = goingTo;
    }

    public static final short ORDERING_ARRAY = 0;
    public static final short ORDERING_DISTANCE = 1;
    public static final String ORDERING_ARRAY_STR = "decl";
    public static final String ORDERING_DISTANCE_STR = "dist";

    static short parseOrdering(String o){
        if (o.equals(ORDERING_DISTANCE_STR)){
            return ORDERING_DISTANCE;
        }
        else if (o.equals(ORDERING_ARRAY_STR)){
            return ORDERING_ARRAY;
        }
        else {
            if (SceneManager.getDebugMode()){System.err.println("Error: unknown load/unload request ordering declaration: "+o);}
            return -1;
        }
    }

    static short parseTransition(String t){
        if (t.equals(FADE_IN_STR)){return FADE_IN;}
        else if (t.equals(FADE_OUT_STR)){return FADE_OUT;}
        else if (t.equals(APPEAR_STR)){return APPEAR;}
        else if (t.equals(DISAPPEAR_STR)){return DISAPPEAR;}
        else {
            if (SceneManager.getDebugMode()){System.err.println("Error: incorrect transition value: "+t);}
            return -1;
        }
    }

    double x;       // center coordinates of region
    double y;
    double w;       // width and height of region
    double h;
    double[] wnes;  // west, north, east and south bounds of region
    // lowest level index
    int lli = 0;
    // highest level index
    int hli = 0;
    String id;
    String title;

    Region containingRegion = null;
    Region[] containedRegions = new Region[0];

    ObjectDescription[] objects = new ObjectDescription[0];

    String[] tags;

    SceneManager sm;

    boolean isSensitive = false;

    short[] transitions;

    short requestOrder = ORDERING_DISTANCE;

    /** Create a new region.
     *@param x center of region
     *@param y center of region
     *@param w width of region
     *@param h height of region
     *@param highestLevel index of highest level in level span for this region (highestLevel <= lowestLevel)
     *@param lowestLevel index of lowest level in level span for this region (highestLevel <= lowestLevel)
     *@param id region ID
     *@param transitions a 4-element array with values in Region.{FADE_IN, FADE_OUT, APPEAR, DISAPPEAR}, corresponding to
                         transitions from upper level, from lower level, to upper level, to lower level.
     */
    Region(double x, double y, double w, double h, int highestLevel, int lowestLevel,
           String id, String[] tags, short[] trans, short ro, SceneManager sm){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.hli = highestLevel;
        this.lli = lowestLevel;
        wnes = new double[4];
        setGeometry(x, y, w, h);
        this.id = id;
        this.tags = tags;
        this.sm = sm;
        transitions = trans;
        requestOrder = ro;
    }

    /** Get Region ID. */
    public String getID(){
        return id;
    }

    public boolean hasTag(String tag){
        if (tags == null){return false;}
        for (int i=0;i<tags.length;i++){
            if (tags[i].equals(tag)){return true;}
        }
        return false;
    }

    public void addTag(String tag){
        if (tags == null){
            tags = new String[]{tag};
        }
        else if (!hasTag(tag)){
            String[] nta = new String[tags.length+1];
            System.arraycopy(tags, 0, nta, 0, tags.length);
            nta[tags.length] = tag;
            tags = nta;
        }
    }

    public void removeTag(String tag){
        if (tags == null){return;}
        // find tag index
        int tagIndex = -1;
        for (int i=0;i<tags.length;i++){
            if (tags[i].equals(tag)){
                tagIndex = i;
                break;
            }
        }
        // remove from list of tags
        if (tagIndex != -1){
            String[] nta = new String[tags.length-1];
            System.arraycopy(tags, 0, nta, 0, tagIndex);
            System.arraycopy(tags, tagIndex+1, nta, 0, tags.length - tagIndex - 1);
            tags = nta;
        }
    }

    /**
     *@return null if no tags attached
     */
    public String[] getTags(){
        return tags;
    }

    /** Set Region title. */
    public void setTitle(String t){
        this.title = t;
    }

    /** Get Region title. */
    public String getTitle(){
        return title;
    }

    /** Get index of highest and lowest levels this region belongs to.
     */
    public int getHighestLevel(){
        return hli;
    }

    /** Get index of highest and lowest levels this region belongs to.
     */
    public int getLowestLevel(){
        return lli;
    }

    /** Get containing region, if any has been declared.
     *@return null if no containing region declared
     */
    public Region getContainingRegion(){
        return containingRegion;
    }

    /** Get containing regions recursively, if any have been declared.
     *@param cr an initially empty Vector that will be recursively populated with containing regions up to the root.
     *@return cr populated with additional regions if any declared.
     */
    public Vector<Region> getContainingRegions(Vector<Region> cr){
        cr.insertElementAt(this, 0);
        if (containingRegion != null){
            return containingRegion.getContainingRegions(cr);
        }
        else {
            return cr;
        }
    }

    /** Get objects in this region.
     *@return the actual list, not a clone. Do not temper with.
     */
    public ObjectDescription[] getObjectsInRegion(){
        return objects;
    }

    /** Get this region's center x-coordinate.
     */
    public double getX(){
        return x;
    }

    /** Get this region's center y-coordinate.
     */
    public double getY(){
        return y;
    }

    /** Get this region's width.
     */
    public double getWidth(){
        return w;
    }

    /** Get this region's height.
     */
    public double getHeight(){
        return h;
    }

    public double[] getBounds(){
        double[] res = new double[4];
        System.arraycopy(wnes, 0, res, 0, wnes.length);
        return res;
    }

    /** Set this region's center (x,y)-coordinates.
     */
    public void moveTo(double x, double y){
        this.x = x;
        this.y = y;
        updateGeometry();
    }

    /** Set this region's width.
     */
    public void setWidth(double w){
        this.w = w;
        updateGeometry();
    }

    /** Set this region's height.
     */
    public void setHeight(double h){
        this.h = h;
        updateGeometry();
    }

    void updateGeometry(){
        setGeometry(this.x, this.y, this.w, this.h);
    }

    /** Set this region's center (x,y)-coordinates, width and height.
     */
    public void setGeometry(double x, double y, double w, double h){
        wnes[0] = x - w/2;
        wnes[1] = y + h/2;
        wnes[2] = x + w/2;
        wnes[3] = y - h/2;
    }

    public void addObject(ObjectDescription od){
        ObjectDescription[] newObjects = new ObjectDescription[objects.length+1];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);
        newObjects[objects.length] = od;
        objects = newObjects;
    }

    public void setContainingRegion(Region r){
        containingRegion = r;
    }

    public void addContainedRegion(Region r){
        Region[] tmpR = new Region[containedRegions.length+1];
        System.arraycopy(containedRegions, 0, tmpR, 0, containedRegions.length);
        tmpR[containedRegions.length] = r;
        containedRegions = tmpR;
    }

    /*rl can be null*/
    void updateVisibility(SceneObserver so, double[] viewportBounds, int atDepth,
                          short transition, RegionListener rl){
        // is visible in viewport
        boolean iviv = (wnes[0] < viewportBounds[2] && wnes[2] > viewportBounds[0]
            && wnes[3] < viewportBounds[1] && wnes[1] > viewportBounds[3]);
        if (iviv){
            if (so.observedRegions.containsKey(this)){
                // was visible last time we checked, is still visible
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, viewportBounds, atDepth, transition, rl);
                }
            }
            else {
                // was not visible last time we checked, is visible now
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    if (so.isOfInterest(this)){
                        forceShow(so, transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                        so.observedRegions.put(this, SceneObserver.DUMMY_SHORT);
                        if (rl != null){
                            rl.enteredRegion(this);
                        }
                    }
                }
            }
        }
        else {
            if (so.observedRegions.containsKey(this)){
                // was visible last time we checked, is no longer visible
                // contained regions are necessarily invisible
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, false, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    if (so.isOfInterest(this)){
                        forceHide(so, transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                        if (rl != null){
                            rl.exitedRegion(this);
                        }
                    }
                    // Remove it even if supposed not to be of interest.
                    // Should not have been in there in the first place.
                    so.observedRegions.remove(this);
                }
            }
            // else nothing to do: was not visible last time we checked, is still invisible
            // contained regions were not visible, and are still not visible
        }
    }

    /*rl can be null*/
    void updateVisibility(SceneObserver so, boolean visible, double[] viewportBounds, int atDepth,
                          short transition, RegionListener rl){
        if (visible){
            if (so.observedRegions.containsKey(this)){
                // was visible last time we checked, is still visible
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, viewportBounds, atDepth, transition, rl);
                }
            }
            else {
                // was not visible last time we checked, is visible now
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    if (so.isOfInterest(this)){
                        forceShow(so, transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                        so.observedRegions.put(this, SceneObserver.DUMMY_SHORT);
                        if (rl != null){
                            rl.enteredRegion(this);
                        }
                    }
                }
            }
        }
        else {
            if (so.observedRegions.containsKey(this)){
                // was visible last time we checked, is no longer visible
                // contained regions are necessarily invisible
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(so, false, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    if (so.isOfInterest(this)){
                        forceHide(so, transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                        if (rl != null){
                            rl.exitedRegion(this);
                        }
                    }
                    // Remove it even if supposed not to be of interest.
                    // Should not have been in there in the first place.
                    so.observedRegions.remove(this);
                }
            }
            // else nothing to do: was not visible last time we checked, is still invisible
            // contained regions were not visible, and are still not visible
        }
    }

    void show(SceneObserver so, short transition, double  x, double y){
        if (!so.observedRegions.containsKey(this)){
            forceShow(so, transition, x, y);
            so.observedRegions.put(this, SceneObserver.DUMMY_SHORT);
        }
    }

    void forceShow(SceneObserver so, short transition, double x, double y){
        if (requestOrder == ORDERING_DISTANCE){
            Arrays.sort(objects, new DistanceComparator(x, y));
        }
        boolean fade = (transition == TASL) ? false : transitions[transition] == FADE_IN;
        for (int i=0;i<objects.length;i++){
            sm.glyphLoader.addLoadRequest(so.getTargetVirtualSpace(this), objects[i], fade);
        }
    }

    void hide(SceneObserver so, short transition, double x, double y){
        if (so.observedRegions.containsKey(this)){
            forceHide(so, transition, x, y);
            so.observedRegions.remove(this);
        }
    }

    void forceHide(SceneObserver so, short transition, double x, double y){
        if (requestOrder == ORDERING_DISTANCE){
            Arrays.sort(objects, new DistanceComparator(x, y));
        }
        boolean fade = (transition == TASL) ? false : transitions[transition] == FADE_OUT;
        for (int i=0;i<objects.length;i++){
            sm.glyphLoader.addUnloadRequest(so.getTargetVirtualSpace(this), objects[i], fade);
        }
    }

    int getClosestObjectIndex(double x, double y){
        int res = 0;
        if (objects.length > 0){
            // do not take the square root to get the actual distance as we are just comparing values
            double shortestDistance = Math.round((x-objects[0].getX())*(x-objects[0].getX()) + (y-objects[0].getY())*(y-objects[0].getY()));
            double distance;
            for (int i=1;i<objects.length;i++){
                distance = Math.round((x-objects[i].getX())*(x-objects[i].getX()) + (y-objects[i].getY())*(y-objects[i].getY()));
                if (distance < shortestDistance){
                    shortestDistance = distance;
                    res = i;
                }
            }
        }
        return res;
    }

    public boolean coordInside(double vx, double vy){
        return (vx >= wnes[0] && vx <= wnes[2] && vy >= wnes[3] && vy <= wnes[1]);
    }

    public String toString(){
        String res = "Region " + id + " contained in " + ((containingRegion != null) ? containingRegion.id : "NO REGION") + " and containing regions [";
        for (int i=0;i<containedRegions.length;i++){
            res += containedRegions[i].id + ", ";
        }
        res += "]";
        return res;
    }

}

class DistanceComparator implements Comparator<ObjectDescription> {

    double x,y;

    DistanceComparator(double x, double y){
    this.x = x;
    this.y = y;
    }

    public int compare(ObjectDescription od1, ObjectDescription od2){
        double d1 = (x-od1.getX())*(x-od1.getX()) + (y-od1.getY())*(y-od1.getY());
        double d2 = (x-od2.getX())*(x-od2.getX()) + (y-od2.getY())*(y-od2.getY());
        if (d1 < d2){
            return -1;
        }
        else if (d1 > d2){
            return 1;
        }
        else {
            return 0;
        }
    }

}
