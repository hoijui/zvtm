/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.Timer;

import java.io.File;
import java.net.URL;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.engine.Location;
import fr.inria.zuist.event.ProgressListener;
import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.event.ObjectListener;

import org.w3c.dom.Document;

/** <strong>Multi-scale scene manager: main ZUIST class instantiated by client application.</strong>
 *@author Emmanuel Pietriga
 */

public class SceneManager {

    public static final short TAKES_TO_OBJECT = 0;
    public static final short TAKES_TO_REGION = 1;

    SceneBuilder sb;

    final GlyphLoader glyphLoader;

    Level[] levels = new Level[0];

    final LinkedHashMap<String,VirtualSpace> layers = new LinkedHashMap(5, .8f, false);
    SceneObserver[] sceneObservers;
    // final double[] prevAlts; //previous altitudes
    final RegionUpdater regUpdater;

    /** Contains a mapping from region IDs to actual Region objects. */
    Hashtable<String,Region> id2region;
    /** Contains a mapping from object IDs to actual objects. */
    Hashtable<String,ObjectDescription> id2object;

    LevelListener levelListener;
    RegionListener regionListener;
    ObjectListener objectListener;

    /** Set to something else than 0,0 to translate a scene to another location than that defined originally. */
    Point2D.Double origin = new Point2D.Double(0, 0);

    HashMap<String,Object> sceneAttrs;

    HashMap<String, ResourceHandler> RESOURCE_HANDLERS;

    class RegionUpdater {
        private final HashMap<SceneObserver, Location> toUpdate;
        private boolean active;
        private static final int DEFAULT_PERIOD = 200; //milliseconds
        private int period;

        private boolean enabled = true;

        RegionUpdater(){
            toUpdate = new HashMap<SceneObserver, Location>(sceneObservers.length,1);
            active = false;
            period = DEFAULT_PERIOD;
        }

        /**
         * Sets the RegionUpdater period.
         * Region updates will be spaced by at least <code>period</code>
         * milliseconds.
         * @param period the new period, in milliseconds.
         */
        void setPeriod(int period){
            this.period = period;
        }

        void setEnabled(boolean b){
            enabled = b;
        }

        void addEntry(SceneObserver so, Location loc){
            if (!enabled){return;}
            //add or overwrite update target
            toUpdate.put(so, loc);

            //if not active, create timer task and start it
            if(active) return;
            ActionListener action = new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    for(Map.Entry<SceneObserver, Location> entry: toUpdate.entrySet()){
                        // Camera cam = entry.getKey();
                        SceneObserver so = entry.getKey();
                        double alt = entry.getValue().alt;
                        VirtualSpace layer = so.getTargetVirtualSpace();
                        if(!layers.containsKey(layer.getName())){
                            if (DEBUG_MODE){System.err.println("SceneObserver " + so + "is not tracked by ZUIST");}
                            return;
                        }
                        //update regions
                        if(alt != so.getPreviousAltitude()){
                            so.setPreviousAltitude(alt);
                            updateLevel(so);
                        } else {
                            updateVisibleRegions(layer, so.getVisibleRegion());
                        }

                    }
                    active = false;
                    toUpdate.clear();
                 }
            };
            active = true;
            Timer t = new Timer(period, action);
            t.setRepeats(false);
            t.start();
        }
    }

    /** Scene Manager: Main ZUIST class instantiated by client application.
     *@param sos scene observers through which the scene will be observed.
     *@param properties properties that can be set on this scene manager.
     *@see #setProperties(HashMap properties)
     */
    public SceneManager(SceneObserver[] sos, HashMap<String,String> properties){
        sb = new SceneBuilder(this);
        this.sceneObservers = sos;
        for (int i=0;i<sceneObservers.length;i++){
            VirtualSpace vs = sceneObservers[i].getTargetVirtualSpace();
            layers.put(vs.getName(), vs);
        }
        this.setProperties(properties);
        regUpdater = new RegionUpdater();
        glyphLoader = new GlyphLoader(this);
        id2region = new Hashtable<String,Region>();
        id2object = new Hashtable<String,ObjectDescription>();
        sceneAttrs = new HashMap(5,1);
        RESOURCE_HANDLERS = new HashMap<String, ResourceHandler>(5);
        for(SceneObserver so:sceneObservers){
            so.setSceneManager(this);
        }
    }

    public static SceneObserver[] buildObservers(VirtualSpace[] spaces, Camera[] cameras){
        ViewSceneObserver[] sos = new ViewSceneObserver[cameras.length];
        for (int i=0;i<cameras.length;i++){
            sos[i] = new ViewSceneObserver(cameras[i].getOwningView(), cameras[i], spaces[i]);
        }
        return sos;
    }

    public SceneManager(VirtualSpace[] targetSpaces, Camera[] observingCameras, HashMap<String,String> properties){
        this(buildObservers(targetSpaces, observingCameras), properties);
    }

    public SceneBuilder getSceneBuilder(){
        return sb;
    }

    /* -------------- Properties -------------------- */

    public static final String HTTP_AUTH_USER = "user";
    public static final String HTTP_AUTH_PASSWORD = "password";

    static String httpUser = null;
    static String httpPassword = null;

    /** Set properties on this scene manager.
     * <ul>
     *  <li>HTTP_AUTH_USER: username as a string for HTTP authentication when fetching resources from the Web.</li>
     *  <li>HTTP_AUTH_PASSWORD: password as a string for HTTP authentication when fetching resources from the Web.</li>
     * </ul>
     */
    public void setProperties(HashMap<String,String> properties){
        for (String prop:properties.keySet()){
            if (prop.equals(HTTP_AUTH_USER)){
                setHTTPUser(properties.get(prop));
            }
            else if (prop.equals(HTTP_AUTH_PASSWORD)){
                setHTTPPassword(properties.get(prop));
            }
            else {
                System.out.println("Warning: trying to set unknown property on ZUIST SceneManager: "+prop);
            }
        }
    }

    /* HTTPS authentication */
    public static void setHTTPUser(String u){
        SceneManager.httpUser = u;
    }

    public static void setHTTPPassword(String p){
        SceneManager.httpPassword = p;
    }

    public static String getHTTPUser(){
        return SceneManager.httpUser;
    }

    public static String getHTTPPassword(){
        return SceneManager.httpPassword;
    }

    /* ------------------- Observers   -------------------- */

    public void addSceneObserver(SceneObserver so){
        // check that so is not already registered
        for (int i=0;i<sceneObservers.length;i++){
            if (so == sceneObservers[i]){
                return;
            }
        }
        // if not, register it
        VirtualSpace vs = so.getTargetVirtualSpace();
        if (!layers.containsKey(vs.getName())){
            layers.put(vs.getName(), vs);
        }
        so.setSceneManager(this);
        SceneObserver[] nsos = new SceneObserver[sceneObservers.length+1];
        synchronized(sceneObservers){
            System.arraycopy(sceneObservers, 0, nsos, 0, sceneObservers.length);
            nsos[sceneObservers.length] = so;
            sceneObservers = nsos;
        }
    }

    public void removeSceneObserver(SceneObserver so){
        // remove layer declaration if not observed by any other SceneObserver
        String layer = so.getTargetVirtualSpace().getName();
        if (layers.containsKey(layer)){
            boolean layerObservedByAnotherSO = false;
            for (SceneObserver so2:sceneObservers){
                if (so2 != so && so2.getTargetVirtualSpace().getName().equals(layer)){
                    layerObservedByAnotherSO = true;
                    break;
                }
            }
            if (!layerObservedByAnotherSO){
                layers.remove(layer);
            }
        }
        // find SceneObserver index
        int soIndex = -1;
        for (int i=0;i<sceneObservers.length;i++){
            if (so == sceneObservers[i]){
                soIndex = i;
                break;
            }
        }
        // remove from list of SceneObservers
        if (soIndex != -1){
            SceneObserver[] nsos = new SceneObserver[sceneObservers.length-1];
            synchronized(sceneObservers){
                System.arraycopy(sceneObservers, 0, nsos, 0, soIndex);
                System.arraycopy(sceneObservers, soIndex+1, nsos, 0, sceneObservers.length - soIndex - 1);
                sceneObservers = nsos;
            }
        }
    }

    /* -------------- Scene Management -------------------- */

    /**
     * Sets the RegionUpdater period.
     * Region updates will be spaced by at least <code>period</code>
     * milliseconds.
     * @param period the new period, in milliseconds.
     */
    public void setRegionUpdatePeriod(int period){
        regUpdater.setPeriod(period);
    }

    /**
     * Shuts down this SceneManager. The SceneManager should not be used after invoking shutdown.
     */
    public void shutdown(){
        glyphLoader.shutdown();
    }

    /** Declare a ResourceHandler for a given type of resource.
     *@param rType type of resource to be handled, e.g., "pdf", "img", ...
     *@param rh class implementing ResourceHandler for that type of resource
     */
    public void setResourceHandler(String rType, ResourceHandler rh){
        RESOURCE_HANDLERS.put(rType, rh);
    }

    /** Get the class handling a given type of resource.
     *@param rType type of resource to handled, e.g., "pdf", "img", ...
     *@return instance of class implementing ResourceHandler for that type of resource. Null if none associated with rType.
     */
    public ResourceHandler getResourceHandler(String rType){
        return RESOURCE_HANDLERS.get(rType);
    }


    /**
     * Gets an unmodifiable view of every ObjectDescription known
     * to this SceneManager
     */
    public Collection<ObjectDescription> getObjectDescriptions(){
        return Collections.unmodifiableCollection(id2object.values());
    }

    /** Set to something else than 0,0 to translate a scene to another location than that defined originally. */
    public void setOrigin(Point2D.Double p){
        origin = p;
    }

    /** Is set to something else than 0,0 when translating a scene to another location than that defined originally. */
    public Point2D.Double getOrigin(){
        return origin;
    }

    /**
        *@return the actual hashmap used internally to store scene attributes.
        */
    public HashMap getSceneAttributes(){
        return sceneAttrs;
    }


    public Set<String> getRegionIDs(){
        return id2region.keySet();
    }

    /** Get a region knowing its ID.
     *@return null if no region associated with this ID.
     */
    public Region getRegion(String id){
        return id2region.get(id);
    }

    /** Get a list of all object IDs, at any level and in any region.
     *@return sequence of object IDs in no particular order
     */
    public Set<String> getObjectIDs(){
        return id2object.keySet();
    }

    /** Get an object knowing its ID.
     *@return null if no object associated with this ID.
     */
    public ObjectDescription getObject(String id){
    return id2object.get(id);
    }

    /** Get the total number of objects (at any level and in any region) in the scene. */
    public int getObjectCount(){
        return id2object.size();
    }

    /** Get the total number of regions (at any level) in the scene. */
    public int getRegionCount(){
        return id2region.size();
    }

    /** Get the total number of levels in the scene. */
    public int getLevelCount(){
        return levels.length;
    }

    /** Get a level.
     *@param index index of level.
     *@return null if level index does not correspond to an actual level.
     */
    public Level getLevel(int index){
        return (index < levels.length) ? levels[index] : null;
    }

    /** Get all regions that belong to a given level.
     *@param level index of level.
     *@return sequence of regions at this level, in no particular order.
     *        Returns null if level index does not correspond to an actual level.
     */
    public Region[] getRegionsAtLevel(int level){
        if (level < levels.length){
            Region[] res = new Region[levels[level].regions.length];
            System.arraycopy(levels[level].regions, 0, res, 0, levels[level].regions.length);
            return res;
        }
        return null;
    }

    /*--------------------------------------------------------------------*/

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     *@param reset reset scene (default is true) ; if false, append regions and objects to existing scene, new levels are ignored (as they would most likely conflict).
     */
    public Region[] loadScene(Document scene, File sceneFileDirectory, boolean reset){
        return sb.loadScene(scene, sceneFileDirectory, reset, null);
    }

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     *@param reset reset scene (default is true) ; if false, append regions and objects to existing scene, new levels are ignored (as they would most likely conflict).
     */
    public Region[] loadScene(Document scene, File sceneFileDirectory, boolean reset, ProgressListener pl){
        return sb.loadScene(scene, sceneFileDirectory, reset, pl);
    }

    /* ----------- ZUIST events ----------- */

    public RegionPicker createRegionPicker(int tl, int bl){
        return new RegionPicker(this, tl, bl);
    }

    public ObjectPicker createObjectPicker(int tl, int bl){
        return new ObjectPicker(this, tl, bl);
    }

    public void setLevelListener(LevelListener ll){
        levelListener = ll;
    }

    public LevelListener getLevelListener(){
        return levelListener;
    }

    public void setRegionListener(RegionListener rl){
        regionListener = rl;
    }

    public RegionListener getRegionListener(){
        return regionListener;
    }

    public int getPendingRequestQueueSize(){
        return glyphLoader.getPendingRequestQueueSize();
    }

    public void setObjectListener(ObjectListener ol){
        objectListener = ol;
    }

    public ObjectListener getObjectListener(){
        return objectListener;
    }

    /** For internal use. */
    public void objectCreated(ObjectDescription od){
        if (objectListener != null){
            objectListener.objectCreated(od);
        }
    }

    /** For internal use. Made public for outside package subclassing  */
    public void objectDestroyed(ObjectDescription od){
        if (objectListener != null){
            objectListener.objectDestroyed(od);
        }
    }

    /* ----------- level / region / object creation (API and XML) ----------- */

    public void reset(){
        id2region.clear();
        id2object.clear();
        sceneAttrs.clear();
        levels = new Level[0];
    }


    /* ----------- level / region visibility update ----------- */

    public void enableRegionUpdater(boolean b){
        regUpdater.setEnabled(b);
    }

    int previousLevel = -2;
    int currentLevel = -1;
    boolean updateLevel = false;

    /** Enable/disable level updating.
     * Calls to updateLevel(altitude) have no effect if level updating is disabled.
     *@see #updateLevel(SceneObserver so)
     */
    public void setUpdateLevel(boolean b){
    updateLevel = b;
    //update level for every camera
    if(updateLevel){
        synchronized(sceneObservers){
            for(SceneObserver so: sceneObservers){
                updateLevel(so);
            }
        }
    }
    }

    /** Notify altitude changes.
     *@param vr region visible through a SceneObserver
     *@param altitude the new SceneObserver's altitude
     */
    private void updateLevel(SceneObserver so){
        if (!updateLevel){return;}
        double soa = so.getAltitude();
        // find out new level
        for (int i=0;i<levels.length;i++){
            if (levels[i].inRange(soa)){currentLevel = i;break;}
        }
        double[] vr = so.getVisibleRegion();
        // compare to current level
        if (previousLevel != currentLevel){
            // it is important that exitLevel() gets called before enterLevel()
            // because of regions spanning multiple levels that get checked in exitLevel()
            if (previousLevel >= 0){
                exitLevel(so, previousLevel, currentLevel);
            }
            enterLevel(so.getTargetVirtualSpace(), vr, currentLevel, previousLevel);
            previousLevel = currentLevel;
        }
        else {
            // if level hasn't changed, it is still necessary to update
            // visible regions as some of them might have become (in)visible
            updateVisibleRegions(so.getTargetVirtualSpace(), vr);
        }
    }

    /** Get the current level.
     *@return index of level at which camera is right now (highest level is 0)
     */
    public int getCurrentLevel(){
        return currentLevel;
    }

    private void enterLevel(VirtualSpace layer, double[] vr, int depth, int prev_depth){
        boolean arrivingFromHigherAltLevel = depth > prev_depth;
        updateVisibleRegions(layer, vr, depth, (arrivingFromHigherAltLevel) ? Region.TFUL : Region.TFLL);
        if (levelListener != null){
            levelListener.enteredLevel(depth);
        }
    }

    private void exitLevel(SceneObserver so, int depth, int new_depth){
        boolean goingToLowerAltLevel = new_depth > depth;
        Region r;
        for (int i=0;i<levels[depth].regions.length;i++){
            r = levels[depth].regions[i];
            // hide only if region does not span the level where we are going
            if ((goingToLowerAltLevel && !levels[new_depth].contains(r))
                || (!goingToLowerAltLevel && !levels[new_depth].contains(r))){
                    r.hide((goingToLowerAltLevel) ? Region.TTLL : Region.TTUL,
                           so.getX(),
                           so.getY());
            }
        }
        if (levelListener != null){
            levelListener.exitedLevel(depth);
        }
    }

    /** Get region whose center is closest to a given location at the current level. */
    public Region getClosestRegionAtCurrentLevel(Point2D.Double lp){
        return levels[currentLevel].getClosestRegion(lp);
    }

    /** Notify camera translations. It is up to the client application to notify the scene manager each time the position of the camera used to observe the scene changes.
     *
     */
    private void updateVisibleRegions(VirtualSpace layer, double[] vr){
        //called when an x-y movement occurs but no altitude change
        updateVisibleRegions(layer, vr, currentLevel, Region.TASL);
    }


    private void updateVisibleRegions(VirtualSpace layer, double[] vr, int level, short transition){
        try {
            for (int i=0;i<levels[level].regions.length;i++){
                if(layer != levels[level].regions[i].layer){
                    continue;
                }
                levels[level].regions[i].updateVisibility(vr, currentLevel, transition, regionListener);
            }
        }
        catch ( Exception e) {
            if (DEBUG_MODE){
                System.err.println("ZUIST: Error: failed to update visible region. Possible causes:\n\t- the camera's current altitude is not in the range of any scene level.");
                e.printStackTrace();
            }
        }
    }

    /** Update visible regions for all cameras. */
    public void updateVisibleRegions(){
        synchronized(sceneObservers){
            for (SceneObserver so:sceneObservers){
                updateVisibleRegions(so.getTargetVirtualSpace(), so.getVisibleRegion());
            }
        }
    }

    public void setFadeInDuration(int d){
        glyphLoader.FADE_IN_DURATION = d;
    }

    public void setFadeOutDuration(int d){
        glyphLoader.FADE_OUT_DURATION = d;
    }

    // debug
//     void printLevelInfo(){
//  for (int i=0;i<levels.length;i++){
//      System.out.println("-------------------------------- Level "+i);
//      System.out.println(levels[i].toString());
//  }
//     }

//     void printRegionInfo(){
//  for (int i=0;i<levels.length;i++){
//      System.out.println("-------------------------------- Level "+i);
//      for (int j=0;j<levels[i].regions.length;j++){
//      System.out.println(levels[i].regions[j].toString());
//      }
//  }
//     }

    /* -------- Navigation ----------------- */

    /** Get the bounding box of all regions in this scene.
     *@return bounds in virtual space
     */
    public double[] findFarmostRegionCoords(){
        int l = 0;
        while (getRegionsAtLevel(l) == null){
            l++;
            if (l > getLevelCount()){
                l = -1;
                break;
            }
        }
        if (l > -1){
            return getLevel(l).getBounds();
        }
        else return new double[]{0,0,0,0};
    }

    /** Get a global view of the scene.
     *@param c camera that should show a global view
     *@param d duration of animation from current location to global view
     *@param ea action to be perfomed after camera has reached its new position (can be null)
     @return bounds in virtual space
     */
    public double[] getGlobalView(Camera c, int d, EndAction ea){
        double[] wnes = findFarmostRegionCoords();
        c.getOwningView().centerOnRegion(c, d, wnes[0], wnes[1], wnes[2], wnes[3], ea);
        return wnes;
    }


    /* ------------------ DEBUGGING --------------------- */
    private static boolean DEBUG_MODE = true;

    public static void setDebugMode(boolean b){
        DEBUG_MODE = b;
    }

    public static boolean getDebugMode(){
        return DEBUG_MODE;
    }

}
