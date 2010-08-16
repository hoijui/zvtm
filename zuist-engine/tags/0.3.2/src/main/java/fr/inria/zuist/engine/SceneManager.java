/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
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
import java.util.Map;
import java.util.Enumeration;

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
import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.svg.SVGReader;
import fr.inria.zvtm.engine.Location;

/** <strong>Multi-scale scene manager: main ZUIST class instantiated by client application.</strong>
 * Used to parse XML descriptions of multi-scale scene configurations and manage them once instantiated.
 *@author Emmanuel Pietriga
 */

public class SceneManager implements CameraListener {
    
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
    static final String FILE_PROTOCOL_HEAD = "file://";

    final GlyphLoader glyphLoader;

    Level[] levels = new Level[0];

    final VirtualSpace[] sceneLayers;
    final Camera[] sceneCameras;
    final float[] prevAlts; //previous altitudes
    private final RegionUpdater regUpdater = new RegionUpdater();

    /** Contains a mapping from region IDs to actual Region objects. */
    Hashtable id2region;
    /** Contains a mapping from object IDs to actual objects. */
    Hashtable<String, ObjectDescription> id2object;
    
    LevelListener levelListener;
    RegionListener regionListener;
    ObjectListener objectListener;

    /** Set to something else than 0,0 to translate a scene to another location than that defined originally. */
    LongPoint origin = new LongPoint(0, 0);
    
    HashMap sceneAttrs;
    
    HashMap<String, ResourceHandler> RESOURCE_HANDLERS;

    private class RegionUpdater {
        private final HashMap<Camera, Location> toUpdate;
        private boolean active;
        private static final int DEFAULT_PERIOD = 200; //milliseconds
        private int period;
        
        private boolean enabled = true;

        RegionUpdater(){
            toUpdate = new HashMap<Camera, Location>();
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

        void addEntry(Camera cam, Location loc){
            if (!enabled){return;}
            //add or overwrite update target 
            toUpdate.put(cam, loc);

            //if not active, create timer task and start it
            if(active) return;
            ActionListener action = new ActionListener(){
                public void actionPerformed(ActionEvent event){
                    for(Map.Entry<Camera, Location> entry: toUpdate.entrySet()){
                        Camera cam = entry.getKey();
                        float alt = entry.getValue().alt;
                        int layerIndex = getLayerIndex(cam);
                        if(layerIndex == -1){
                            if (DEBUG_MODE){System.err.println("Camera " + cam + "is not tracked by ZUIST");}
                            return;
                        }
                        long[] cameraBounds = cam.getOwningView().getVisibleRegion(cam);
                        //update regions
                        if(alt != prevAlts[layerIndex]){
                            prevAlts[layerIndex] = alt;
                            updateLevel(layerIndex, cameraBounds, alt);
                        } else {
                            updateVisibleRegions(layerIndex, cameraBounds);
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
     *@param vss virtual spaces in which the scene will be loaded
     *@param cs cameras associated to those virtual spaces, through which the scene will be observed
     */
    public SceneManager(VirtualSpace[] vss, Camera[] cs){
        this.sceneLayers = vss;
        this.sceneCameras = cs;
        prevAlts = new float[sceneCameras.length];
        glyphLoader = new GlyphLoader(this);
        id2region = new Hashtable();
        id2object = new Hashtable<String, ObjectDescription>();
        sceneAttrs = new HashMap();
        RESOURCE_HANDLERS = new HashMap<String, ResourceHandler>();

        for(Camera cam: sceneCameras){
            cam.addListener(this);
        }
    }

    /**
     * Sets the RegionUpdater period.
     * Region updates will be spaced by at least <code>period</code>
     * milliseconds.
     * @param period the new period, in milliseconds.
     */
    public void setRegionUpdatePeriod(int period){
        regUpdater.setPeriod(period);
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
    public void setOrigin(LongPoint p){
        origin = p;
    }
    
    /** Is set to something else than 0,0 when translating a scene to another location than that defined originally. */
    public LongPoint getOrigin(){
        return origin;
    }
    
    /**
        *@return the actual hashmap used internally to store scene attributes.
        */
    public HashMap getSceneAttributes(){
        return sceneAttrs;
    }


    public Enumeration getRegionIDs(){
	return id2region.keys();
    }

    /** Get a region knowing its ID.
     *@return null if no region associated with this ID.
     */
    public Region getRegion(String id){
	return (Region)id2region.get(id);
    }

	/** Get a list of all object IDs, at any level and in any region.
	 *@return sequence of object IDs in no particular order
	 */
	public Enumeration getObjectIDs(){
		return id2object.keys();
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
        //return glyphLoader.requestQueue.size();
	    return 0; //XXX fix or drop the method
    }
    
    public void setObjectListener(ObjectListener ol){
        objectListener = ol;
    }

    public ObjectListener getObjectListener(){
        return objectListener;
    }
    
    void objectCreated(ObjectDescription od){
        if (objectListener != null){
            objectListener.objectCreated(od);
        }
    }

    void objectDestroyed(ObjectDescription od){
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
        NodeList nl = root.getChildNodes();
        Node n;
        Element e;
        if (reset || levels.length == 0){
            // process new levels only if resetting and loading a new scene, or if appending but no level was ever created
            if (pl != null){
                pl.setLabel("Creating levels...");
                pl.setValue(10);
            }
            for (int i=0;i<nl.getLength();i++){
                n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE){
                    e = (Element)n;
                    if (e.getTagName().equals(_level)){
                        processLevel(e);
                    }
                }
            }            
        }
        if (pl != null){
            pl.setLabel("Creating regions, loading object descriptions...");
        }
        // temporary hashtable used to build structure
        Hashtable regionName2containerRegionName = new Hashtable();
        Vector regions = new Vector();
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                e = (Element)n;
                if (e.getTagName().equals(_region)){
                    if (pl != null){
                        pl.setValue(Math.round(10+i/((float)nl.getLength())*50.0f));
                    }
                    regions.add(processRegion(e, regionName2containerRegionName, sceneFileDirectory));
                }
            }
        }
        for (Enumeration en=regionName2containerRegionName.keys();en.hasMoreElements();){
            String rn = (String)en.nextElement();
            if (rn != null){
                // region is contained in another region
                Region r = (Region)id2region.get(rn);
                Region cr = (Region)id2region.get(regionName2containerRegionName.get(rn));
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
        nl = root.getChildNodes();
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                e = (Element)n;
                if (e.getTagName().equals(_include)){
                    if (pl != null){
                        pl.setValue(Math.round(60+i/((float)nl.getLength())*30.0f));
                    }
                    processInclude(e, sceneFileDirectory);
                }
            }
        }
        if (pl != null){
            pl.setLabel("Cleaning up temporary resources...");
            pl.setValue(95);
        }
        regionName2containerRegionName.clear();
        //    	printLevelInfo();
        //   	printRegionInfo();
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
    
    public SceneFragmentDescription createSceneFragmentDescription(long x, long y, String id, Region region, URL resourceURL){
        //System.out.println("Creating scene fragment "+resourceURL);
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
    public Level createLevel(int depth, float calt, float falt){
        if (depth >= levels.length){
            Level[] tmpL = new Level[depth+1];
            System.arraycopy(levels, 0, tmpL, 0, levels.length);
            levels = tmpL;
        }
        levels[depth] = new Level(calt, falt);
        return levels[depth];
    }
    
    Level processLevel(Element levelEL){
        return createLevel(Integer.parseInt(levelEL.getAttribute(_depth)), Float.parseFloat(levelEL.getAttribute(_ceiling)), Float.parseFloat(levelEL.getAttribute(_floor)));
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
     *@param li layer index (information layer/space in which objects will be put)
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
    public Region createRegion(long x, long y, long w, long h, int highestLevel, int lowestLevel,
                               String id, String title, int li, short[] transitions, short requestOrdering,
                               boolean sensitivity, Color fill, Color stroke){
        Region region = new Region(x+origin.x, y+origin.y, w, h, highestLevel, lowestLevel, id, li, transitions, requestOrdering, this);
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
        VRectangle r = new VRectangle(x+origin.x, y+origin.y, 0, w/2, h/2, Color.WHITE, Color.BLACK);
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
            sceneLayers[li].addGlyph(r);
        }
        region.setGlyph(r);
        r.setOwner(region);
        return region;
    }

    Region processRegion(Element regionEL, Hashtable rn2crn, File sceneFileDirectory){
        long x = Long.parseLong(regionEL.getAttribute(_x));
        long y = Long.parseLong(regionEL.getAttribute(_y));
        long w = Long.parseLong(regionEL.getAttribute(_w));
        long h = Long.parseLong(regionEL.getAttribute(_h));
        Color fill = SVGReader.getColor(regionEL.getAttribute(_fill));
        Color stroke = SVGReader.getColor(regionEL.getAttribute(_stroke));
        String id = regionEL.getAttribute(_id);
        short[] transitions = {regionEL.hasAttribute(_tful) ? Region.parseTransition(regionEL.getAttribute(_tful)) : Region.DEFAULT_F_TRANSITION,
            regionEL.hasAttribute(_tfll) ? Region.parseTransition(regionEL.getAttribute(_tfll)) : Region.DEFAULT_F_TRANSITION,
            regionEL.hasAttribute(_ttul) ? Region.parseTransition(regionEL.getAttribute(_ttul)) : Region.DEFAULT_T_TRANSITION,
            regionEL.hasAttribute(_ttll) ? Region.parseTransition(regionEL.getAttribute(_ttll)) : Region.DEFAULT_T_TRANSITION};
        int li = getLayerIndex(regionEL.getAttribute(_layer));
        if (li == -1){
            // put region in first virtual space (assumed to be the only one if yields -1) corresponding to a layer
            li = 0;
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
        Region region = createRegion(x, y, w, h, highestLevel, lowestLevel, id, title, li, transitions,
                                     (regionEL.hasAttribute(_ro)) ? Region.parseOrdering(regionEL.getAttribute(_ro)) : Region.ORDERING_DISTANCE,
                                     sensitivity, fill, stroke);
        String containerID = (regionEL.hasAttribute(_containedIn)) ? regionEL.getAttribute(_containedIn) : null;
        if (containerID != null){
            rn2crn.put(id, containerID);
        }
        Node n;
        Element e;
        NodeList nl = regionEL.getChildNodes();
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                processObject((Element)n, region, sceneFileDirectory);
            }
        }
        return region;
    }
    
    public void destroyRegionsAtLevel(int l){
        Region[] ral = getRegionsAtLevel(l);
        for (int i=0;i<ral.length;i++){
            destroyRegion(ral[i]);
        }
    }
    
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
        long x = Long.parseLong(resourceEL.getAttribute(_x));
        long y = Long.parseLong(resourceEL.getAttribute(_y));
        String src = resourceEL.getAttribute(_src);
        String params = resourceEL.getAttribute(_params);
        Color stroke = SVGReader.getColor(resourceEL.getAttribute(_stroke));
        boolean sensitivity = (resourceEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(resourceEL.getAttribute(_sensitive)) : true;
		URL absoluteSrc = SceneManager.getAbsoluteURL(src, sceneFileDirectory);
        if (type.equals(ImageDescription.RESOURCE_TYPE_IMG)){
    		long w = Long.parseLong(resourceEL.getAttribute(_w));
            long h = Long.parseLong(resourceEL.getAttribute(_h));
            return createImageDescription(x+origin.x, y+origin.y, w, h, id, zindex, region, absoluteSrc, sensitivity, stroke, params);
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
    public ResourceDescription createResourceDescription(long x, long y, String id, int zindex, Region region,
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
    public ImageDescription createImageDescription(long x, long y, long w, long h, String id, int zindex, Region region,
                                                   URL imageURL, boolean sensitivity, Color stroke, String params){
        Object interpolation = (params != null && params.startsWith(_im)) ? parseInterpolation(params.substring(3)) : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        ImageDescription imd = new ImageDescription(id, x, y, zindex, w, h, imageURL, stroke, interpolation, region);
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

    /** Process XML description of a rectangle object. */
    ClosedShapeDescription processRectangle(Element rectEL, String id, int zindex, Region region){
        long x = Long.parseLong(rectEL.getAttribute(_x));
        long y = Long.parseLong(rectEL.getAttribute(_y));
        long w = Long.parseLong(rectEL.getAttribute(_w));
        long h = Long.parseLong(rectEL.getAttribute(_h));
        Color stroke = SVGReader.getColor(rectEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(rectEL.getAttribute(_fill));
        boolean sensitivity = (rectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(rectEL.getAttribute(_sensitive)) : true;
        ClosedShape g = new VRectangle(x+origin.x, y+origin.y, zindex, w/2, h/2, (fill!=null) ? fill : Color.BLACK, (stroke!=null) ? stroke : Color.WHITE, 1.0f);
        if (fill == null){g.setFilled(false);}
        if (stroke == null){g.setDrawBorder(false);}
        return createClosedShapeDescription(g, id, zindex, region, sensitivity);
    }

    /** Process XML description of a polygon object. */
    ClosedShapeDescription processPolygon(Element polygonEL, String id, int zindex, Region region){
        LongPoint[] vertices = parseVertexCoordinates(polygonEL.getAttribute(_points), origin);
        Color stroke = SVGReader.getColor(polygonEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(polygonEL.getAttribute(_fill));
        boolean sensitivity = (polygonEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(polygonEL.getAttribute(_sensitive)) : true;
        ClosedShape g = new VPolygon(vertices, zindex, (fill!=null) ? fill : Color.BLACK, (stroke!=null) ? stroke : Color.WHITE, 1.0f);
        if (fill == null){g.setFilled(false);}
        if (stroke == null){g.setDrawBorder(false);}
        return createClosedShapeDescription(g, id, zindex, region, sensitivity);
    }
    
    public static LongPoint[] parseVertexCoordinates(String s, LongPoint orig){
        String[] points = s.split(PARAM_SEPARATOR);
        LongPoint[] res = new LongPoint[points.length];
        String[] xy;
        for (int i=0;i<points.length;i++){
            xy = points[i].split(COORD_SEPARATOR);
            res[i] = new LongPoint(SVGReader.getLong(xy[0])+orig.x, SVGReader.getLong(xy[1])+orig.y);
        }
        return res;
    }
    
    /** Process XML description of a text object. */
    TextDescription processText(Element textEL, String id, int zindex, Region region){
        long x = Long.parseLong(textEL.getAttribute(_x));
        long y = Long.parseLong(textEL.getAttribute(_y));
        float scale = Float.parseFloat(textEL.getAttribute(_scale));
        String text = textEL.getFirstChild().getNodeValue();
        Color fill = SVGReader.getColor(textEL.getAttribute(_fill));
        String ff = (textEL.hasAttribute(_fontFamily)) ? textEL.getAttribute(_fontFamily) : null;
        int fst = (textEL.hasAttribute(_fontStyle)) ? getFontStyle(textEL.getAttribute(_fontStyle)) : Font.PLAIN;
        int fsz = (textEL.hasAttribute(_fontSize)) ? Integer.parseInt(textEL.getAttribute(_fontSize)) : 12;        
        boolean sensitivity = (textEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(textEL.getAttribute(_sensitive)) : true;
        short anchor = (textEL.hasAttribute(_anchor)) ? TextDescription.getAnchor(textEL.getAttribute(_anchor)) : VText.TEXT_ANCHOR_MIDDLE;
        TextDescription od = createTextDescription(x+origin.x, y+origin.y, id, zindex, region, scale, text,
                                                   anchor, fill,
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
    public TextDescription createTextDescription(long x, long y, String id, int zindex, Region region, float scale, String text,
                                                 short anchor, Color fill, 
                                                 String family, int style, int size,
                                                 boolean sensitivity){
        TextDescription td = new TextDescription(id, x, y, zindex, scale, text,
                                                 (fill != null) ? fill : Color.BLACK,
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
        long x = Long.parseLong(includeEL.getAttribute(_x));
        long y = Long.parseLong(includeEL.getAttribute(_y));
        String src = includeEL.getAttribute(_src);
        String absoluteSrc = ((new File(src)).isAbsolute()) ? src : sceneFileDirectory.getAbsolutePath() + File.separatorChar + src;
        File f = new File(absoluteSrc);
        setOrigin(new LongPoint(x, y));
        loadScene(parseXML(f), f.getParentFile(), false, null);
        setOrigin(new LongPoint(0, 0));
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
     *@see #updateLevel(int layerIndex, long[] cameraBounds, float altitude)
     */
    public void setUpdateLevel(boolean b){
	updateLevel = b;
    //update level for every camera
    if(updateLevel){
        for(Camera cam: sceneCameras){
            updateLevel(getLayerIndex(cam), 
                    cam.getOwningView().getVisibleRegion(cam),
                    cam.getAltitude());
        }
    }
    }

    /** Notify altitude changes.
     *@param altitude the new camera's altitude
     */
    private void updateLevel(int layerIndex, long[] cameraBounds, float altitude){
        if (!updateLevel){return;}
        // find out new level
        for (int i=0;i<levels.length;i++){
            if (levels[i].inRange(altitude)){currentLevel = i;break;}
        }
        // compare to current level
        if (previousLevel != currentLevel){
            // it is important that exitLevel() gets called before enterLevel()
            // because of regions spanning multiple levels that get checked in exitLevel()
            if (previousLevel >= 0){
                exitLevel(previousLevel, currentLevel);
            }
            enterLevel(layerIndex, cameraBounds, currentLevel, previousLevel);
            previousLevel = currentLevel;
        }
        else {
            // if level hasn't changed, it is still necessary to update
            // visible regions as some of them might have become (in)visible
            updateVisibleRegions(layerIndex, cameraBounds);
        }
    }

    /** Get the current level. 
     *@return index of level at which camera is right now (highest level is 0)
     */
    public int getCurrentLevel(){
	return currentLevel;
    }

    private void enterLevel(int layerIndex, long[] cameraBounds, int depth, int prev_depth){
        boolean arrivingFromHigherAltLevel = depth > prev_depth;
	    updateVisibleRegions(layerIndex, cameraBounds, depth, (arrivingFromHigherAltLevel) ? Region.TFUL : Region.TFLL);
	    if (levelListener != null){
	        levelListener.enteredLevel(depth);
	    }
    }

    private void exitLevel(int depth, int new_depth){
        boolean goingToLowerAltLevel = new_depth > depth;
        for (int i=0;i<levels[depth].regions.length;i++){            
            // hide only if region does not span the level where we are going
            if ((goingToLowerAltLevel && !levels[new_depth].contains(levels[depth].regions[i]))
                || (!goingToLowerAltLevel && !levels[new_depth].contains(levels[depth].regions[i]))){
                    levels[depth].regions[i].hide((goingToLowerAltLevel) ? Region.TTLL : Region.TTUL,
                        sceneCameras[levels[depth].regions[i].li].posx, sceneCameras[levels[depth].regions[i].li].posy);
            }
        }
        if (levelListener != null){
	        levelListener.exitedLevel(depth);
	    }
    }

	/** Get region whose center is closest to a given location at the current level. */
	public Region getClosestRegionAtCurrentLevel(LongPoint lp){
		return levels[currentLevel].getClosestRegion(lp);
	}

    /** Notify camera translations. It is up to the client application to notify the scene manager each time the position of the camera used to observe the scene changes.
     *
     */
    private void updateVisibleRegions(int layerIndex, long[] cameraBounds){
        //called when an x-y movement occurs but no altitude change 
        updateVisibleRegions(layerIndex, cameraBounds, currentLevel, Region.TASL);
    }

   
    private void updateVisibleRegions(int layerIndex, long[] cameraBounds, int level, short transition){
        try {
	        for (int i=0;i<levels[level].regions.length;i++){
                if(layerIndex != levels[level].regions[i].li){
                    continue;
                }
	            levels[level].regions[i].updateVisibility(cameraBounds, currentLevel, transition, regionListener);
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
        for (Camera cam:sceneCameras){
            updateVisibleRegions(getLayerIndex(cam), cam.getOwningView().getVisibleRegion(cam));
        }
    }
    
    public void setFadeInDuration(int d){
	glyphLoader.FADE_IN_DURATION = d;
    }

    public void setFadeOutDuration(int d){
	glyphLoader.FADE_OUT_DURATION = d;
    }

    int getLayerIndex(String spaceName){
        for (int i=0;i<sceneLayers.length;i++){
            if (sceneLayers[i].getName().equals(spaceName)){
                return i;
            }
        }
        return -1;
    }

    VirtualSpace getSpaceByIndex(int layerIndex){
	if((layerIndex < 0) || (layerIndex > sceneLayers.length)){
	    return null;
	}
        return sceneLayers[layerIndex];
    } 

    // debug
//     void printLevelInfo(){
// 	for (int i=0;i<levels.length;i++){
// 	    System.out.println("-------------------------------- Level "+i);
// 	    System.out.println(levels[i].toString());
// 	}
//     }

//     void printRegionInfo(){
// 	for (int i=0;i<levels.length;i++){
// 	    System.out.println("-------------------------------- Level "+i);
// 	    for (int j=0;j<levels[i].regions.length;j++){
// 		System.out.println(levels[i].regions[j].toString());
// 	    }
// 	}
//     }

    /* -------- Navigation ----------------- */
    
    /** Get a global view of the scene.
     *@param c camera that should show a global view
     *@param d duration of animation from current location to global view
     *@param ea action to be perfomed after camera has reached its new position (can be null)
     @return bounds in virtual space, null if none
     */
    public long[] getGlobalView(Camera c, int d, EndAction ea){
		int l = 0;
		while (getRegionsAtLevel(l) == null){
			l++;
			if (l > getLevelCount()){
				l = -1;
				break;
			}
		}
		if (l > -1){
			long[] wnes = getLevel(l).getBounds();
	        c.getOwningView().centerOnRegion(c, d, wnes[0], wnes[1], wnes[2], wnes[3], ea);
	        return wnes;
		}
		else {
		    return null;
		}
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
        if (src.indexOf(URL_PROTOCOL_SEQ) != -1){
    		try {
    			return new URL(src);
    		}
    		catch(MalformedURLException ex){if (DEBUG_MODE)System.err.println("Error: malformed resource URL: "+src);}
		}
    	else {
    		// probably a local file URL
    		try {
    			return new URL(FILE_PROTOCOL_HEAD +
    			               (((new File(src)).isAbsolute()) ? src
    			                                               : sceneFileDir.getAbsolutePath() + File.separatorChar + src));
    		}
    		catch(MalformedURLException ex){if (DEBUG_MODE){System.err.println("Error: malformed local resource URL: "+src);ex.printStackTrace();}}		
    	}
    	return null;
    }
        
    /* Camera events handling */
    public void cameraMoved(Camera cam, LongPoint loc, float alt){
        regUpdater.addEntry(cam, new Location(loc.x, loc.y, alt));
    }

    /** 
     * returns the layer index (0-based)
     * of camera 'cam', or -1 if 'cam' does not belong
     * to cameras tracked by this ZUIST instance.
     */
    private int getLayerIndex(Camera cam){
        for(int i=0; i<sceneCameras.length; ++i){
            if(sceneCameras[i] == cam){
                return i;
            }
        }
        return -1;
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
