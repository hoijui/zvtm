/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
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
     *@param from one of {APPEAR, FADE_IN}
     *@param to one of {DISAPPEAR, FADE_OUT}
     */
    public static void setDefaultTransitions(short from, short to){
        DEFAULT_F_TRANSITION = from;
        DEFAULT_T_TRANSITION = to;
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
            System.err.println("Error: unknown load/unload request ordering declaration: "+o);
            return -1;
        }
    }
    
    static short parseTransition(String t){
        if (t.equals(FADE_IN_STR)){return FADE_IN;}
        else if (t.equals(FADE_OUT_STR)){return FADE_OUT;}
        else if (t.equals(APPEAR_STR)){return APPEAR;}
        else if (t.equals(DISAPPEAR_STR)){return DISAPPEAR;}
        else {
            System.err.println("Error: incorrect transition value: "+t);
            return -1;
        }
    }

    long x;       // center coordinates of region
    long y;
    long w;       // width and height of region
    long h;
    long[] wnes;  // west, north, east and south bounds of region
    // lowest level index
    int lli = 0;
    // highest level index
    int hli = 0;
    String id;
    String title;

    // virtual space/layer index (in SceneManager)
    int li = 0;

    Region containingRegion = null;
    Region[] containedRegions = new Region[0];

    // rectangle representing region
    VRectangle bounds;

    ObjectDescription[] objects = new ObjectDescription[0];

    // was visible in viewport
    boolean wviv = false;

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
     *@param li layer index (information layer/space in which objects will be put)
     *@param transitions a 4-element array with values in Region.{FADE_IN, FADE_OUT, APPEAR, DISAPPEAR}, corresponding to
                         transitions from upper level, from lower level, to upper level, to lower level.
     */
    Region(long x, long y, long w, long h, int highestLevel, int lowestLevel,
           String id, int li, short[] trans, short ro, SceneManager sm){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.hli = highestLevel;
        this.lli = lowestLevel;
        this.li = li;
        wnes = new long[4];
        wnes[0] = x - w/2;
        wnes[1] = y + h/2;
        wnes[2] = x + w/2;
        wnes[3] = y - h/2;
        this.id = id;
        this.sm = sm;
        transitions = trans;
        requestOrder = ro;
    }

    public String getID(){
	return id;
    }
    
    public void setTitle(String t){
        this.title = t;
    }
    
    public String getTitle(){
        return title;
    }

    void setLayerIndex(int i){
        li = i;
    }

    int getLayerIndex(){
        return li;
    }

    /** Get index of highest and lowest level this region belongs to.
     */
    public int getHighestLevel(){
	    return hli;
    }

    /** Get index of highest and lowest level this region belongs to.
     */
    public int getLowestLevel(){
	    return lli;
    }

    public Region getContainingRegion(){
	return containingRegion;
    }

//    public Region getContainingRegionAtLevel(int level){
//	      if (lli <= level){return this;}
//	      else if (containingRegion != null){return containingRegion.getContainingRegionAtLevel(level);}
//	      else return null;
//    }
    
    public Vector getContainingRegions(Vector cr){
        cr.insertElementAt(this, 0);
        if (containingRegion != null){
            return containingRegion.getContainingRegions(cr);
        }
        else {
            return cr;
        }
    }
    
    /** Get objects in this region.
     *@return returns the actual list, not a clone. Do not temper with.
     */
    public ObjectDescription[] getObjectsInRegion(){
        return objects;
    }

    void setGlyph(VRectangle r){
        bounds = r;
        bounds.setSensitivity(isSensitive);
    }

    public VRectangle getBounds(){
	return bounds;
    }
	
	public long getX(){
		return x;
	}
	
	public long getY(){
		return y;
	}

	public long getWidth(){
		return w;
	}
	
	public long getHeight(){
		return h;
	}

    public void setSensitive(boolean b){
        isSensitive = b;
        if (bounds != null){bounds.setSensitivity(b);}
    }
    
    public boolean isSensitive(){
        return isSensitive;
    }

    void addObject(ObjectDescription od){
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
    void updateVisibility(long[] viewportBounds, int atDepth, short transition, RegionListener rl){
        // is visible in viewport
        boolean iviv = (wnes[0] < viewportBounds[2] && wnes[2] > viewportBounds[0]
            && wnes[3] < viewportBounds[1] && wnes[1] > viewportBounds[3]);
        if (iviv){
            if (wviv){
                // was visible last time we checked, is still visible
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(viewportBounds, atDepth, transition, rl);
                }
            }
            else {
                // was not visible last time we checked, is visible now
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    forceShow(transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                    if (rl != null){
                        rl.enteredRegion(this);
                    }
                }
            }
        }
        else {
            if (wviv){
                // was visible last time we checked, is no longer visible
                // contained regions are necessarily invisible
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(false, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    forceHide(transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                    if (rl != null){
                        rl.exitedRegion(this);
                    }
                }

            }
            // else nothing to do: was not visible last time we checked, is still invisible
            // contained regions were not visible, and are still not visible
        }
    }

    /*rl can be null*/
    void updateVisibility(boolean visible, long[] viewportBounds, int atDepth, short transition, RegionListener rl){
        if (visible){
            if (wviv){
                // was visible last time we checked, is still visible
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(viewportBounds, atDepth, transition, rl);
                }
            }
            else {
                // was not visible last time we checked, is visible now
                // visibility status of contained regions might have changed
                // we have to compute intersections to find out
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    forceShow(transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                    if (rl != null){
                        rl.enteredRegion(this);
                    }
                }

            }
        }
        else {
            if (wviv){
                // was visible last time we checked, is no longer visible
                // contained regions are necessarily invisible
                for (int i=0;i<containedRegions.length;i++){
                    containedRegions[i].updateVisibility(false, viewportBounds, atDepth, transition, rl);
                }
                if (atDepth >= hli && atDepth <= lli){
                    forceHide(transition, (wnes[2]+wnes[0])/2, (wnes[1]+wnes[3])/2);
                    if (rl != null){
                        rl.exitedRegion(this);
                    }
                }
            }
            // else nothing to do: was not visible last time we checked, is still invisible
            // contained regions were not visible, and are still not visible
        }
    }

    void show(short transition, long x, long y){
	if (!wviv){
	    forceShow(transition, x, y);
	}
    }

    void forceShow(short transition, long x, long y){
	if (requestOrder == ORDERING_DISTANCE){
	    Arrays.sort(objects, new DistanceComparator(x, y));
	}
	boolean fade = (transition == TASL) ? false : transitions[transition] == FADE_IN;
	for (int i=0;i<objects.length;i++){
	    sm.glyphLoader.addLoadRequest(li, objects[i], fade);
	}
	wviv = true;
    }

    void hide(short transition, long x, long y){
	if (wviv){
	    forceHide(transition, x, y);
	}
    }
    
    void forceHide(short transition, long x, long y){
	if (requestOrder == ORDERING_DISTANCE){
	    Arrays.sort(objects, new DistanceComparator(x, y));
	}
	boolean fade = (transition == TASL) ? false : transitions[transition] == FADE_OUT;
	for (int i=0;i<objects.length;i++){
	    sm.glyphLoader.addUnloadRequest(li, objects[i], fade);
	}
	wviv = false;
    }

    int getClosestObjectIndex(long x, long y){
	int res = 0;
	if (objects.length > 0){
	    // do not take the square root to get the actual distance as we are just comparing values
	    long shortestDistance = Math.round(Math.pow(x-objects[0].getX(),2) + Math.pow(y-objects[0].getY(),2));
	    long distance;
	    for (int i=1;i<objects.length;i++){
		distance = Math.round(Math.pow(x-objects[i].getX(),2) + Math.pow(y-objects[i].getY(),2));
		if (distance < shortestDistance){
		    shortestDistance = distance;
		    res = i;
		}
	    }
	}
	return res;
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

    long x,y;

    DistanceComparator(long x, long y){
	this.x = x;
	this.y = y;
    }
    
	public int compare(ObjectDescription od1, ObjectDescription od2){
		double d1 = Math.pow(x-od1.getX(), 2) + Math.pow(y-od1.getY(), 2);
		double d2 = Math.pow(x-od2.getX(), 2) + Math.pow(y-od2.getY(), 2);
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
