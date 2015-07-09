/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Image;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.svg.SVGReader;
import fr.inria.zvtm.engine.Location;

import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.event.ObjectListener;
import fr.inria.zuist.event.ProgressListener;

/** <strong>Multi-scale scene manager: main ZUIST class instantiated by client application.</strong>
 * Used to parse XML descriptions of multi-scale scene configurations and manage them once instantiated.
 *@author Emmanuel Pietriga
 */

public class SceneManager {

    public static final String _none = "none";
    public static final String _level = "level";
    public static final String _region = "region";
    public static final String _object = "object";
    public static final String _id = "id";
    public static final String _title = "title";
    public static final String _containedIn = "containedIn";
    public static final String _resource = "resource";
    public static final String _type = "type";
    public static final String _text = "text";
    public static final String _rect = "rect";
    public static final String _polygon = "polygon";
    public static final String _include = "include";
    public static final String _x = "x";
    public static final String _y = "y";
    public static final String _w = "w";
    public static final String _h = "h";
    public static final String _points = "points";
    public static final String _alpha = "alpha";
    public static final String _fill = "fill";
    public static final String _stroke = "stroke";
    public static final String _background = "background";
    public static final String _scale = "scale";
    public static final String _src = "src";
    public static final String _onClick = "onClick";
    public static final String _focusOnObject = "focusOnObject";
    public static final String _tful = "tful"; // transition from upper level
    public static final String _tfll = "tfll"; // transition from lower level
    public static final String _ttul = "ttul"; // transition to upper level
    public static final String _ttll = "ttll"; // transition to lower level
    public static final String _appear = "appear";
    public static final String _diappear = "disappear";
    public static final String _fadein = "fadein";
    public static final String _fadeout = "fadeout";
    public static final String _levels = "levels";
    public static final String _depth = "depth";
    public static final String _ceiling = "ceiling";
    public static final String _floor = "floor";
    public static final String _ro = "ro";
    public static final String _takesToR = "ttr";
    public static final String _takesToO = "tto";
    public static final String _sensitive = "sensitive";
    public static final String _anchor = "anchor";
    public static final String _layer = "layer";
    public static final String _zindex = "z-index";
    public static final String _params = "params";
    public static final String _im = "im=";
    public static final String _nearestNeighbor = "nearestNeighbor";
    public static final String _bilinear = "bilinear";
    public static final String _bicubic = "bicubic";
    public static final String _fontFamily = "font-family";
    public static final String _fontStyle = "font-style";
    public static final String _fontSize = "font-size";
    public static final String _plain = "plain";
    public static final String _italic = "italic";
    public static final String _bold = "bold";
    public static final String _boldItalic = "boldItalic";

    public static final String PARAM_SEPARATOR = ";";
    public static final String COORD_SEPARATOR = ",";

    public static final short TAKES_TO_OBJECT = 0;
    public static final short TAKES_TO_REGION = 1;


    static final String URL_PROTOCOL_SEQ = ":/";
    static final String JAR_PROTOCOL_SEQ = ":!/";
    static final String FILE_PROTOCOL_HEAD = "file://";

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

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     *@param reset reset scene (default is true) ; if false, append regions and objects to existing scene, new levels are ignored (as they would most likely conflict).
     */
    public Region[] loadScene(Document scene, File sceneFileDirectory, boolean reset){
        return loadScene(scene, sceneFileDirectory, reset, null);
    }

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     *@param reset reset scene (default is true) ; if false, append regions and objects to existing scene, new levels are ignored (as they would most likely conflict).
     */
    public Region[] loadScene(Document scene, File sceneFileDirectory, boolean reset, ProgressListener pl){
        if (reset){
            reset();
        }
        Element root = scene.getDocumentElement();
        // scene attributes
        processSceneAttributes(root);
        NodeList nl = root.getElementsByTagName(_level);
        if (reset || levels.length == 0){
            // process new levels only if resetting and loading a new scene, or if appending but no level was ever created
            if (pl != null){
                pl.setLabel("Creating levels...");
                pl.setValue(10);
            }
            for (int i=0;i<nl.getLength();i++){
                processLevel((Element)nl.item(i));
            }
        }
        if (pl != null){
            pl.setLabel("Creating regions, loading object descriptions...");
        }
        // temporary hashtable used to build structure
        nl = root.getElementsByTagName(_region);
        HashMap<String,String> regionName2containerRegionName = new HashMap<String,String>(nl.getLength(),1);
        Vector<Region> regions = new Vector(nl.getLength());
        for (int i=0;i<nl.getLength();i++){
            if (pl != null){
                pl.setValue(Math.round(10+i/((float)nl.getLength())*50.0f));
            }
            regions.add(processRegion((Element)nl.item(i), regionName2containerRegionName, sceneFileDirectory));
        }
        for (String rn:regionName2containerRegionName.keySet()){
            if (rn != null){
                // region is contained in another region
                Region r = id2region.get(rn);
                Region cr = id2region.get(regionName2containerRegionName.get(rn));
                if (r != null && cr != null){
                    cr.addContainedRegion(r);
                    r.setContainingRegion(cr);
                }
                else {
                    if (DEBUG_MODE){
                        System.err.println("Warning: trying to set a containedIn relationship between:\n" +
                        rn + " => " + r + "\n" +
                        regionName2containerRegionName.get(rn) + " => " + cr);
                    }
                }
            }
        }
        if (pl != null){
            pl.setLabel("Processing inclusions...");
        }
        nl = root.getElementsByTagName(_include);
        for (int i=0;i<nl.getLength();i++){
            if (pl != null){
                pl.setValue(Math.round(60+i/((float)nl.getLength())*30.0f));
            }
            processInclude((Element)nl.item(i), sceneFileDirectory);
        }
        if (pl != null){
            pl.setLabel("Cleaning up temporary resources...");
            pl.setValue(95);
        }
        regionName2containerRegionName.clear();
        //      printLevelInfo();
        //      printRegionInfo();
        System.gc();
        if (pl != null){
            pl.setLabel("Scene file loaded successfully");
            pl.setValue(100);
        }
        return (Region[])regions.toArray(new Region[regions.size()]);
    }

    void processSceneAttributes(Element sceneEL){
        if (sceneEL.hasAttribute(_background)){
            Color bkg = SVGReader.getColor(sceneEL.getAttribute(_background));
            if (bkg != null){
                sceneAttrs.put(_background, bkg);
            }
        }
    }

    public SceneFragmentDescription createSceneFragmentDescription(double x, double y, String id, Region region, URL resourceURL){
        SceneFragmentDescription sd = new SceneFragmentDescription(id, x, y, resourceURL, region, this);
        region.addObject(sd);
        return sd;
    }

    public void destroySceneFragment(SceneFragmentDescription sd){
        //System.out.println("Destroying fragment "+sd.getID());
    }

    /** Create a new level in the scene.
     *@param depth of this level (0 corresponds to the highest level in terms of altitude range)
     *@param calt ceiling altitude
     *@param falt floor altitude
     */
    public Level createLevel(int depth, double calt, double falt){
        if (depth >= levels.length){
            Level[] tmpL = new Level[depth+1];
            System.arraycopy(levels, 0, tmpL, 0, levels.length);
            levels = tmpL;
        }
        levels[depth] = new Level(calt, falt);
        return levels[depth];
    }

    Level processLevel(Element levelEL){
        return createLevel(Integer.parseInt(levelEL.getAttribute(_depth)),
            Double.parseDouble(levelEL.getAttribute(_ceiling)), Double.parseDouble(levelEL.getAttribute(_floor)));
    }

    /** Create a new region.
     * Important: when called directly from the client application, Region.setContainingRegion() should also be called manually (if there is any such containing region).
     * Also important: if the region is neither visible nor sensitive at instantiation time, its associated glyph is not added to the virtual space.
     * Note that containment relationships between regions have to be set manually through calls to Region instance methods.
     *@param x center of region
     *@param y center of region
     *@param w width of region
     *@param h height of region
     *@param highestLevel index of highest level in level span for this region (highestLevel <= lowestLevel)
     *@param lowestLevel index of lowest level in level span for this region (highestLevel <= lowestLevel)
     *@param id region ID
     *@param title region's title (metadata)
     *@param layer name of VirtualSpace in which objects will be put
     *@param transitions a 4-element array with values in Region.{FADE_IN, FADE_OUT, APPEAR, DISAPPEAR}, corresponding to
                         transitions from upper level, from lower level, to upper level, to lower level.
     *@param requestOrdering how requests for loading / unloading objects should be ordered when
                             entering / leaving this region; one of Region.{ORDERING_ARRAY, ORDERING_DISTANCE}.
     *@param sensitivity should the rectangle symbolizing the region itself be sensitive to mouse events or not.
     *@param fill fill color of the rectangle symbolizing the region itself
     *@param stroke border color of the rectangle symbolizing the region itself
     *@see Region#setContainingRegion(Region r)
     *@see Region#addContainedRegion(Region r)
     */
    public Region createRegion(double x, double y, double w, double h, int highestLevel, int lowestLevel,
                               String id, String title, String layer, short[] transitions, short requestOrdering,
                               boolean sensitivity, Color fill, Color stroke){
        VirtualSpace vs = layers.get(layer);
        Region region = new Region(x+origin.x, y+origin.y, w, h, highestLevel, lowestLevel, id, vs, transitions, requestOrdering, this);
        if (!id2region.containsKey(id)){
            id2region.put(id, region);
        }
        else {
            if (DEBUG_MODE){System.err.println("Error: ID "+id+" used to identify more than one region.");}
            return null;
        }
        for (int i=highestLevel;i<=lowestLevel;i++){
            levels[i].addRegion(region);
        }
        if (sensitivity){region.setSensitive(true);}
        if (title != null && title.length() > 0){
            region.setTitle(title);
        }
        VRectangle r = new VRectangle(x+origin.x, y+origin.y, 0, w, h, Color.WHITE, Color.BLACK);
        if (fill != null){
            r.setColor(fill);
        }
        else {
            r.setFilled(false);
        }
        if (stroke != null){
            r.setBorderColor(stroke);
        }
        else {
            r.setDrawBorder(false);
        }
        if (fill != null || stroke != null || sensitivity){
            // add the rectangle representing the region only if it is visible or sensitive
            layers.get(layer).addGlyph(r);
        }
        region.setGlyph(r);
        r.setOwner(region);
        return region;
    }

    Region processRegion(Element regionEL, HashMap<String,String> rn2crn, File sceneFileDirectory){
        double x = Double.parseDouble(regionEL.getAttribute(_x));
        double y = Double.parseDouble(regionEL.getAttribute(_y));
        double w = Double.parseDouble(regionEL.getAttribute(_w));
        double h = Double.parseDouble(regionEL.getAttribute(_h));
        Color fill = SVGReader.getColor(regionEL.getAttribute(_fill));
        Color stroke = SVGReader.getColor(regionEL.getAttribute(_stroke));
        String id = regionEL.getAttribute(_id);
        short[] transitions = {regionEL.hasAttribute(_tful) ? Region.parseTransition(regionEL.getAttribute(_tful)) : Region.DEFAULT_F_TRANSITION,
            regionEL.hasAttribute(_tfll) ? Region.parseTransition(regionEL.getAttribute(_tfll)) : Region.DEFAULT_F_TRANSITION,
            regionEL.hasAttribute(_ttul) ? Region.parseTransition(regionEL.getAttribute(_ttul)) : Region.DEFAULT_T_TRANSITION,
            regionEL.hasAttribute(_ttll) ? Region.parseTransition(regionEL.getAttribute(_ttll)) : Region.DEFAULT_T_TRANSITION};
        String layer = regionEL.getAttribute(_layer);
        if (!layers.containsKey(layer)){
            // if no layer information is provided,
            // put the region in the first layer that was declared to the SceneManager
            layer = layers.keySet().iterator().next();
        }
        boolean sensitivity = (regionEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(regionEL.getAttribute(_sensitive)) : false;
        String title = regionEL.getAttribute(_title);
        int lowestLevel = 0;
        int highestLevel = 0;
        String levelStr = regionEL.getAttribute(_levels);
        int scIndex = levelStr.indexOf(PARAM_SEPARATOR);
        if (scIndex != -1){// level information given as, e.g., 2;4 (region spans multiple levels)
            highestLevel = Integer.parseInt(levelStr.substring(0, scIndex));
            lowestLevel = Integer.parseInt(levelStr.substring(scIndex+1));
        }
        else {// level information given as, e.g., 2, short for 2;2 (single level)
            lowestLevel = highestLevel = Integer.parseInt(levelStr);
        }
        Region region = createRegion(x, y, w, h, highestLevel, lowestLevel, id, title, layer, transitions,
                                     (regionEL.hasAttribute(_ro)) ? Region.parseOrdering(regionEL.getAttribute(_ro)) : Region.ORDERING_DISTANCE,
                                     sensitivity, fill, stroke);
        String containerID = (regionEL.hasAttribute(_containedIn)) ? regionEL.getAttribute(_containedIn) : null;
        if (containerID != null){
            rn2crn.put(id, containerID);
        }
        NodeList nl = regionEL.getChildNodes();
        Node n;
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                processObject((Element)nl.item(i), region, sceneFileDirectory);
            }
        }
        return region;
    }

    /** Destroy all regions at a given level.
     * Destroying a region destroys all object descriptions it contains.
     *@param l level index
     */
    public void destroyRegionsAtLevel(int l){
        Region[] ral = getRegionsAtLevel(l);
        for (int i=0;i<ral.length;i++){
            destroyRegion(ral[i]);
        }
    }

    /** Destroy a region.
     * Destroying a region destroys all object descriptions it contains.
     *@param r region to be destroyed
     */
    public void destroyRegion(Region r){
        r.forceHide(Region.DISAPPEAR, r.x, r.y);
        ObjectDescription[] ods = r.getObjectsInRegion();
        for (int i=0;i<ods.length;i++){
            id2object.remove(ods[i].getID());
        }
        id2region.remove(r.getID());
        for (int i=r.getLowestLevel();i<=r.getHighestLevel();i++){
            levels[i].removeRegion(r);
        }
        if (r.getBounds() != null){
            layers.get(r.layer).removeGlyph(r.getBounds());
        }
    }

    ObjectDescription processObject(Element e, Region region, File sceneFileDirectory){
        ObjectDescription od = null;
        // extract info shared by all types of elements
        String id = e.getAttribute(_id);
        int zindex = (e.hasAttribute(_zindex)) ? Integer.parseInt(e.getAttribute(_zindex)) : 0;
        if (id == null || id.length() == 0){
            if (DEBUG_MODE){System.err.println("Warning: object "+e+" has no ID");}
        }
        // process element-specific attributes
        if (e.getTagName().equals(_resource)){
            od = processResource(e, id, zindex, region, sceneFileDirectory);
        }
        else if (e.getTagName().equals(_text)){
            od = processText(e, id, zindex, region);
        }
        else if (e.getTagName().equals(_rect)){
            od = processRectangle(e, id, zindex, region);
        }
        else if (e.getTagName().equals(_polygon)){
            od = processPolygon(e, id, zindex, region);
        }
        else {
            if (DEBUG_MODE){System.err.println("Error: failed to process object declaration: "+id);}
            return null;
        }
        String tto = e.getAttribute(_takesToO);
        String ttr = e.getAttribute(_takesToR);
        if (tto != null && tto.length() > 0){
            od.setTakesTo(tto, TAKES_TO_OBJECT);
        }
        else if (ttr != null && ttr.length() > 0){
            od.setTakesTo(ttr, TAKES_TO_REGION);
        }
        return od;
    }

    /** Process XML description of a resource (image, pdf) object. */
    ResourceDescription processResource(Element resourceEL, String id, int zindex, Region region, File sceneFileDirectory){
        String type = resourceEL.getAttribute(_type);
        double x = Double.parseDouble(resourceEL.getAttribute(_x));
        double y = Double.parseDouble(resourceEL.getAttribute(_y));
        String src = resourceEL.getAttribute(_src);
        String params = resourceEL.getAttribute(_params);
        Color stroke = SVGReader.getColor(resourceEL.getAttribute(_stroke));
        boolean sensitivity = (resourceEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(resourceEL.getAttribute(_sensitive)) : true;
        float alpha = (resourceEL.hasAttribute(_alpha)) ? Float.parseFloat(resourceEL.getAttribute(_alpha)) : 1f;
        URL absoluteSrc = SceneManager.getAbsoluteURL(src, sceneFileDirectory);
        if (type.equals(ImageDescription.RESOURCE_TYPE_IMG)){
            double w = Double.parseDouble(resourceEL.getAttribute(_w));
            double h = Double.parseDouble(resourceEL.getAttribute(_h));
            return createImageDescription(x+origin.x, y+origin.y, w, h, id, zindex, region, absoluteSrc, sensitivity, stroke, alpha, params);
        }
        else if (type.equals(SceneFragmentDescription.RESOURCE_TYPE_SCENE)){
            return createSceneFragmentDescription(x+origin.x, y+origin.y, id, region, absoluteSrc);
        }
        else {
            return createResourceDescription(x+origin.x, y+origin.y, id, zindex, region, absoluteSrc, type, sensitivity, stroke, params);
        }
    }

    /** Creates a resource and adds it to a region.
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param zindex z-index (layer)
        *@param resourceURL path to resource (should be absolute)
        *@param type resource type ("img", "pdf", ...)
        *@param sensitivity should the object be sensitive to mouse events or not.
        *@param stroke border color
        *@param region parent Region in scene
        *@param params custom parameters for a given type of resource
     */
    public ResourceDescription createResourceDescription(double x, double y, String id, int zindex, Region region,
                                                         URL resourceURL, String type, boolean sensitivity, Color stroke, String params){
        if (RESOURCE_HANDLERS.containsKey(type)){
            ResourceDescription rd = (RESOURCE_HANDLERS.get(type)).createResourceDescription(x, y, id, zindex, region,
                                                                                             resourceURL, sensitivity, stroke, params);
            if (!id2object.containsKey(id)){
                id2object.put(id, rd);
            }
            else {
                if (DEBUG_MODE)System.err.println("Warning: ID: "+id+" used to identify more than one object.");
            }
            return rd;
        }
        else {
            if (DEBUG_MODE){System.err.println("Error: failed to process resource declaration "+id+" : no appropriate handler for this type of resource");}
            return null;
        }
    }

    /** Creates an image and adds it to a region.
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param zindex z-index (layer)
        *@param w width in scene
        *@param h height in scene
        *@param imageURL path to bitmap resource (should be absolute)
        *@param stroke border color
        *@param sensitivity should the object be sensitive to mouse events or not.
        *@param params allowed parameters: "im=nearestNeighbor", "im=bilinear", "im=bicubic"
        *@param region parent Region in scene
     */
    public ImageDescription createImageDescription(double x, double y, double w, double h, String id, int zindex, Region region,
                                                   URL imageURL, boolean sensitivity, Color stroke, float alpha, String params){
        Object interpolation = (params != null && params.startsWith(_im)) ? parseInterpolation(params.substring(3)) : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        ImageDescription imd = new ImageDescription(id, x, y, zindex, w, h, imageURL, stroke, alpha, interpolation, region);
        imd.setSensitive(sensitivity);
        region.addObject(imd);
        if (!id2object.containsKey(id)){
            id2object.put(id, imd);
        }
        else {
            if (DEBUG_MODE)System.err.println("Warning: ID: "+id+" used to identify more than one object.");
        }
        return imd;
    }

    protected static Object parseInterpolation(String im){
        if (im.equals(_bilinear)){
            return RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }
        else if (im.equals(_bicubic)){
            return RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        }
        else {
            return RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
    }

    /**
     *@param g any ClosedShape. It must implement fr.inria.zvtm.glyphs.Translucent if fade in/out transitions are used in the parent region.
     */
    public ClosedShapeDescription createClosedShapeDescription(ClosedShape g, String id, int zindex, Region region, boolean sensitivity){
        ClosedShapeDescription gd = new ClosedShapeDescription(id, g, zindex, region, sensitivity);
        region.addObject(gd);
        if (!id2object.containsKey(id)){
            id2object.put(id, gd);
        }
        else {
            if (DEBUG_MODE)System.err.println("Warning: ID: "+id+" used to identify more than one object.");
        }
        return gd;
    }

    public GlyphDescription createGlyphDescription(Glyph g, String id,
            int zindex, Region region, boolean sensitivity){
        GlyphDescription gd = new GlyphDescription(id, g, zindex, region, sensitivity);
        region.addObject(gd);
        if (!id2object.containsKey(id)){
            id2object.put(id, gd);
        } else {
            if(DEBUG_MODE){
                System.err.println("Warning: ID: "+id+" used to identify more than one object.");
            }
        }
        return gd;
    }

    /** Process XML description of a rectangle object. */
    ClosedShapeDescription processRectangle(Element rectEL, String id, int zindex, Region region){
        double x = Double.parseDouble(rectEL.getAttribute(_x));
        double y = Double.parseDouble(rectEL.getAttribute(_y));
        double w = Double.parseDouble(rectEL.getAttribute(_w));
        double h = Double.parseDouble(rectEL.getAttribute(_h));
        Color stroke = SVGReader.getColor(rectEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(rectEL.getAttribute(_fill));
        boolean sensitivity = (rectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(rectEL.getAttribute(_sensitive)) : true;
        ClosedShape g = new VRectangle(x+origin.x, y+origin.y, zindex, w, h, (fill!=null) ? fill : Color.BLACK, (stroke!=null) ? stroke : Color.WHITE, 1.0f);
        if (fill == null){g.setFilled(false);}
        if (stroke == null){g.setDrawBorder(false);}
        return createClosedShapeDescription(g, id, zindex, region, sensitivity);
    }

    /** Process XML description of a polygon object. */
    ClosedShapeDescription processPolygon(Element polygonEL, String id, int zindex, Region region){
        Point2D.Double[] vertices = parseVertexCoordinates(polygonEL.getAttribute(_points), origin);
        Color stroke = SVGReader.getColor(polygonEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(polygonEL.getAttribute(_fill));
        boolean sensitivity = (polygonEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(polygonEL.getAttribute(_sensitive)) : true;
        ClosedShape g = new VPolygon(vertices, zindex, (fill!=null) ? fill : Color.BLACK, (stroke!=null) ? stroke : Color.WHITE, 1.0f);
        if (fill == null){g.setFilled(false);}
        if (stroke == null){g.setDrawBorder(false);}
        return createClosedShapeDescription(g, id, zindex, region, sensitivity);
    }

    public static Point2D.Double[] parseVertexCoordinates(String s, Point2D.Double orig){
        String[] points = s.split(PARAM_SEPARATOR);
        Point2D.Double[] res = new Point2D.Double[points.length];
        String[] xy;
        for (int i=0;i<points.length;i++){
            xy = points[i].split(COORD_SEPARATOR);
            res[i] = new Point2D.Double(Double.parseDouble(xy[0])+orig.x, Double.parseDouble(xy[1])+orig.y);
        }
        return res;
    }

    /** Process XML description of a text object. */
    TextDescription processText(Element textEL, String id, int zindex, Region region){
        double x = Double.parseDouble(textEL.getAttribute(_x));
        double y = Double.parseDouble(textEL.getAttribute(_y));
        float scale = Float.parseFloat(textEL.getAttribute(_scale));
        String text = textEL.getFirstChild().getNodeValue();
        Color fill = SVGReader.getColor(textEL.getAttribute(_fill));
        String ff = (textEL.hasAttribute(_fontFamily)) ? textEL.getAttribute(_fontFamily) : null;
        int fst = (textEL.hasAttribute(_fontStyle)) ? getFontStyle(textEL.getAttribute(_fontStyle)) : Font.PLAIN;
        int fsz = (textEL.hasAttribute(_fontSize)) ? Integer.parseInt(textEL.getAttribute(_fontSize)) : 12;
        float alpha = (textEL.hasAttribute(_alpha)) ? Float.parseFloat(textEL.getAttribute(_alpha)) : 1f;
        boolean sensitivity = (textEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(textEL.getAttribute(_sensitive)) : true;
        short anchor = (textEL.hasAttribute(_anchor)) ? TextDescription.getAnchor(textEL.getAttribute(_anchor)) : VText.TEXT_ANCHOR_MIDDLE;
        TextDescription od = createTextDescription(x+origin.x, y+origin.y, id, zindex, region, scale, text,
                                                   anchor, fill, alpha,
                                                   ff, fst, fsz,
                                                   sensitivity);
        return od;
    }

    public static int getFontStyle(String style){
        if (style.equals(_italic)){
            return Font.ITALIC;
        }
        else if (style.equals(_bold)){
            return Font.BOLD;
        }
        else if (style.equals(_boldItalic)){
            return Font.BOLD + Font.ITALIC;
        }
        else {
            return Font.PLAIN;
        }
    }

    /** Creates a text object and adds it to a region.
     *
     */
    public TextDescription createTextDescription(double x, double y, String id, int zindex, Region region, float scale, String text,
                                                 short anchor, Color fill, float alpha,
                                                 String family, int style, int size,
                                                 boolean sensitivity){
        TextDescription td = new TextDescription(id, x, y, zindex, scale, text,
                                                 (fill != null) ? fill : Color.BLACK, alpha,
                                                 anchor, region);
        if (family != null){
            td.setFont(SVGReader.getFont(family, style, size));
        }
        td.setSensitive(sensitivity);
        region.addObject(td);
        if (!id2object.containsKey(id)){
            id2object.put(id, td);
        }
        else {
            if (DEBUG_MODE)System.err.println("Warning: ID: "+id+" used to identify more than one object.");
        }
        return td;
    }

    /* ---------- inclusions (of other scene files) ----------- */

    void processInclude(Element includeEL, File sceneFileDirectory){
        double x = Double.parseDouble(includeEL.getAttribute(_x));
        double y = Double.parseDouble(includeEL.getAttribute(_y));
        String src = includeEL.getAttribute(_src);
        String absoluteSrc = ((new File(src)).isAbsolute()) ? src : sceneFileDirectory.getAbsolutePath() + File.separatorChar + src;
        File f = new File(absoluteSrc);
        setOrigin(new Point2D.Double(x, y));
        loadScene(parseXML(f), f.getParentFile(), false, null);
        setOrigin(new Point2D.Double(0, 0));
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
     *@see #updateLevel(int layerIndex, double[] cameraBounds, double altitude)
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

    /* -------------- Utils ------------------- */

    public static Document parseXML(File f){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document res = builder.parse(f);
            return res;
        }
        catch (FactoryConfigurationError e){e.printStackTrace();return null;}
        catch (ParserConfigurationException e){e.printStackTrace();return null;}
        catch (SAXException e){e.printStackTrace();return null;}
        catch (IOException e){e.printStackTrace();return null;}
    }

    public static URL getAbsoluteURL(String src, File sceneFileDir){
        if (src.indexOf(URL_PROTOCOL_SEQ) != -1 || src.indexOf(JAR_PROTOCOL_SEQ) != -1){
            try {
                return new URL(src);
            }
            catch(MalformedURLException ex){if (DEBUG_MODE)System.err.println("Error: malformed resource URL: "+src);}
        }
        else {
            // probably a local file URL
            try {

                File f = new File(src);
                if (!f.isAbsolute()){
                    f = new File(sceneFileDir.getCanonicalPath() + File.separator + src);
                }
                return f.toURI().toURL();
            }
            catch(IOException ex){if (DEBUG_MODE){System.err.println("Error: unable to make URL from path to: "+src);ex.printStackTrace();}}
        }
        return null;
    }

    /* ------------------ DEBUGGING --------------------- */
    private static boolean DEBUG_MODE = false;

    public static void setDebugMode(boolean b){
        DEBUG_MODE = b;
    }

    public static boolean getDebugMode(){
        return DEBUG_MODE;
    }

}
