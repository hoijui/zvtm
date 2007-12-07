/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: SceneManager.java,v 1.24 2007/10/03 06:37:20 pietriga Exp $
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.svg.SVGReader;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.glyphs.VImageST;
import net.claribole.zvtm.glyphs.VTextST;

/** <strong>Multi-scale scene manager: main ZUIST class instantiated by client application.</strong>
 * Used to parse XML descriptions of multi-scale scene configurations and manage them once instantiated.
 *@author Emmanuel Pietriga
 */

public class SceneManager {
    
    static final String _none = "none";
    static final String _level = "level";
    static final String _region = "region";
    static final String _object = "object";
    static final String _id = "id";
    static final String _title = "title";
    static final String _containedIn = "containedIn";
    static final String _image = "image";
    static final String _type = "type";
    static final String _text = "text";
    static final String _rect = "rect";
    static final String _polygon = "polygon";
    static final String _x = "x";
    static final String _y = "y";
    static final String _w = "w";
    static final String _h = "h";
    static final String _points = "points";
    static final String _fill = "fill";
    static final String _stroke = "stroke";
    static final String _scale = "scale";
    static final String _src = "src";
    static final String _onClick = "onClick";
    static final String _focusOnObject = "focusOnObject";
    static final String _tful = "tful"; // transition from upper level
    static final String _tfll = "tfll"; // transition from lower level
    static final String _ttul = "ttul"; // transition to upper level
    static final String _ttll = "ttll"; // transition to lower level
    static final String _appear = "appear";
    static final String _diappear = "disappear";
    static final String _fadein = "fadein";
    static final String _fadeout = "fadeout";
    static final String _depth = "depth";
    static final String _ceiling = "ceiling";
    static final String _floor = "floor";
    static final String _ro = "ro";
    static final String _takesToR = "takesToRegion";
    static final String _takesToO = "takesToObject";
    static final String _sensitive = "sensitive";
    static final String _anchor = "anchor";
    static final String _layer = "layer";

    public static final short TAKES_TO_OBJECT = 0;
    public static final short TAKES_TO_REGION = 1;

    GlyphLoader glyphLoader;

    Level[] levels = new Level[0];

    VirtualSpaceManager vsm;
    
    VirtualSpace[] sceneLayers;
    Camera[] sceneCameras;
    long[][] sceneCameraBounds;

    /** Contains a mapping from region IDs to actual Region objects. */
    Hashtable id2region;
    /** Contains a mapping from object IDs to actual objects. */
    Hashtable id2object;
    
    LevelListener levelListener;
    RegionListener regionListener;

    /** Scene Manager: Main ZUIST class instantiated by client application.
     *@param vsm ZVTM virtual space manager instantiated by client application
     *@param vss virtual spaces in which the scene will be loaded
     *@param cs cameras associated to those virtual spaces, through which the scene will be observed
     */
    public SceneManager(VirtualSpaceManager vsm, VirtualSpace[] vss, Camera[] cs){
        this.vsm = vsm;
        this.sceneLayers = vss;
        this.sceneCameras = cs;
        sceneCameraBounds = new long[cs.length][];
        glyphLoader = new GlyphLoader(this);
        id2region = new Hashtable();
        id2object = new Hashtable();
    }

    /** Set the array containing information about the bounds of the region of virtual space seen through the camera observing the scene.
     *@param bounds array containing information about the bounds of the region of virtual space seen through the camera observing the scene. It is up to the client application to update the values in this array whenever the camera is moved (through any mean).
     *@see com.xerox.VTM.engine.View#getVisibleRegion(Camera c, long[] res)
     *@see com.xerox.VTM.engine.View#getVisibleRegion(Camera c)
     */
    public void setSceneCameraBounds(Camera c, long[] bounds){
	    for (int i=0;i<sceneCameras.length;i++){
	        if (sceneCameras[i] == c){
	            sceneCameraBounds[i] = bounds;
	            break;
            }
        }
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

    public Enumeration getObjectIDs(){
	return id2object.keys();
    }

    /** Get an object knowing its ID.
     *@return null if no object associated with this ID.
     */
    public ObjectDescription getObject(String id){
	return (ObjectDescription)id2object.get(id);
    }

    public int getObjectCount(){
        return id2object.size();
    }

    public int getRegionCount(){
        return id2region.size();
    }
    
    public int getLevelCount(){
        return levels.length;
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
        return glyphLoader.requestQueue.size();
    }

    /* ----------- level / region / object creation (API and XML) ----------- */

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     */
    public void loadScene(Document scene, String sceneFileDirectory){
	loadScene(scene, sceneFileDirectory, null);
    }

    /** Load a multi-scale scene configuration described in an XML document.
     *@param scene XML document (DOM) containing the scene description
     *@param sceneFileDirectory absolute or relative (w.r.t exec dir) path to the directory containing that XML file (required only if the scene contains image objects whose location is indicated as relative paths to the bitmap files)
     */
    public void loadScene(Document scene, String sceneFileDirectory, ProgressListener pl){
	id2region.clear();
	id2object.clear();
	Element root = scene.getDocumentElement();
	NodeList nl = root.getChildNodes();
	Node n;
	Element e;
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
	// temporary hashtable used to build structure
	if (pl != null){
	    pl.setLabel("Creating regions, loading object descriptions...");
	}
	Hashtable regionName2containerRegionName = new Hashtable();
	for (int i=0;i<nl.getLength();i++){
	    n = nl.item(i);
	    if (n.getNodeType() == Node.ELEMENT_NODE){
		e = (Element)n;
		if (e.getTagName().equals(_region)){
		    if (pl != null){
			pl.setValue(Math.round(10+i/((float)nl.getLength())*90.0f));
		    }
		    processRegion(e, regionName2containerRegionName, sceneFileDirectory);
		}
	    }
	}
	for (Enumeration en=regionName2containerRegionName.keys();en.hasMoreElements();){
	    String rn = (String)en.nextElement();
	    if (rn != null){
		// region is contained in another region
		Region r = (Region)id2region.get(rn);
		Region cr = (Region)id2region.get(regionName2containerRegionName.get(rn));
		cr.addContainedRegion(r);
		r.setContainingRegion(cr);
	    }
	}
	if (pl != null){
	    pl.setLabel("Cleaning up temporary resources...");
	    pl.setValue(95);
	}
	regionName2containerRegionName.clear();
//    	printLevelInfo();
//   	printRegionInfo();
	updateLevel = true;
	System.gc();
	glyphLoader.setEnabled(true);
	if (pl != null){
	    pl.setLabel("Scene file loaded successfully");
	    pl.setValue(100);
	}
    }
    
    public Level createLevel(int depth, float calt, float falt){
        if (depth >= levels.length){
            Level[] tmpL = new Level[depth+1];
            System.arraycopy(levels, 0, tmpL, 0, levels.length);
            levels = tmpL;
            levels[depth] = new Level(calt, falt);
            return levels[depth];
        }
        else {
            return null;
        }
    }
    
    void processLevel(Element levelEL){
        createLevel(Integer.parseInt(levelEL.getAttribute(_depth)), Float.parseFloat(levelEL.getAttribute(_ceiling)), Float.parseFloat(levelEL.getAttribute(_floor)));
    }
    
    /** Create a new region.
     * Important: when called directly from the client application, Region.setContainingRegion() should also be called manually (if there is any such containing region).
     * Also important: if the region is neither visible nor sensitive at instantiation time, its associated glyph is not added to the virtual space.
     */
    public Region createRegion(long x, long y, long w, long h,
                               int depth, String id, int li, String[] transitions, String requestOrdering,
                               boolean sensitivity, String title, Color fill, Color stroke){
        Region region = new Region(x, y, w, h, depth, id, li, transitions, requestOrdering, this);
        if (!id2region.containsKey(id)){
            id2region.put(id, region);
        }
        else {
            System.err.println("Error: ID "+id+" used to identify more than one region.");
            return null;
        }
        levels[depth].addRegion(region);
        if (sensitivity){region.setSensitive(true);}
        if (title != null && title.length() > 0){
            region.setTitle(title);
        }
        VRectangle r = new VRectangle(x, y, 0, w/2, h/2, Color.WHITE, Color.BLACK);
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
            vsm.addGlyph(r, sceneLayers[li]);
        }
        region.setGlyph(r);
        r.setOwner(region);
        return region;
    }

    void processRegion(Element regionEL, Hashtable rn2crn, String sceneFileDirectory){
        long x = Long.parseLong(regionEL.getAttribute(_x));
        long y = Long.parseLong(regionEL.getAttribute(_y));
        long w = Long.parseLong(regionEL.getAttribute(_w));
        long h = Long.parseLong(regionEL.getAttribute(_h));
        Color fill = SVGReader.getColor(regionEL.getAttribute(_fill));
        Color stroke = SVGReader.getColor(regionEL.getAttribute(_stroke));
        int depth = Integer.parseInt(regionEL.getAttribute(_depth));
        String id = regionEL.getAttribute(_id);
        String[] transitions = {regionEL.getAttribute(_tful),
            regionEL.getAttribute(_tfll),
            regionEL.getAttribute(_ttul),
            regionEL.getAttribute(_ttll)};
        int li = getLayerIndex(regionEL.getAttribute(_layer));
        if (li == -1){
            // put region in first (assumed to be the only if yields -1) virtual space corresponding to a layer
            li = 0;
        }
        boolean sensitivity = (regionEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(regionEL.getAttribute(_sensitive)) : false;
        String title = regionEL.getAttribute(_title);
        Region region = createRegion(x, y, w, h, depth, id, li, transitions, regionEL.getAttribute(_ro), sensitivity, title, fill, stroke);
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
                e = (Element)n;
                if (e.getTagName().equals(_object)){
                    processObject(e, levels[depth], region, sceneFileDirectory);
                }
            }
        }
    }

    void processObject(Element objectEL, Level level, Region region, String sceneFileDirectory){
        String type = objectEL.getAttribute(_type);
        String id = objectEL.getAttribute(_id);
        if (id == null || id.length() == 0){
            System.err.println("Warning: object "+objectEL+" has no ID");
        }
        ObjectDescription res = null;
        if (type.equals(_image)){
            res = processImage(objectEL, id, region, sceneFileDirectory);
        }
        else if (type.equals(_rect)){
            res = processRectangle(objectEL, id, region);
        }
        else if (type.equals(_text)){
            res = processText(objectEL, id, region);
        }
        else if (type.equals(_polygon)){
            res = processPolygon(objectEL, id, region);
        }
        else {
            System.err.println("Error: failed to process object declaration: "+id);
            return;
        }
        String tto = objectEL.getAttribute(_takesToO);
        String ttr = objectEL.getAttribute(_takesToR);
        if (tto != null && tto.length() > 0){
            res.setTakesTo(tto, TAKES_TO_OBJECT);
        }
        else if (ttr != null && ttr.length() > 0){
            res.setTakesTo(ttr, TAKES_TO_REGION);
        }
        if (!id2object.containsKey(id)){
            id2object.put(id, res);
        }
        else {
            System.err.println("Warning: ID: "+id+" used to identify more than one object.");
        }
    }

    /** Process XML description of an image object. */
    ImageDescription processImage(Element objectEL, String id, Region region, String sceneFileDirectory){
        long x = Long.parseLong(objectEL.getAttribute(_x));
        long y = Long.parseLong(objectEL.getAttribute(_y));
        long w = Long.parseLong(objectEL.getAttribute(_w));
        long h = Long.parseLong(objectEL.getAttribute(_h));
        String src = objectEL.getAttribute(_src);
        Color stroke = SVGReader.getColor(objectEL.getAttribute(_stroke));
        boolean sensitivity = (objectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(objectEL.getAttribute(_sensitive)) : true;
        ImageDescription od = createImage(x, y, w, h, id, region, sceneFileDirectory+"/"+src, sensitivity, stroke);
        return od;
    }

    /** Creates an image and adds it to a region.
     *
     */
    public ImageDescription createImage(long x, long y, long w, long h, String id, Region region,
                                        String imagePath, boolean sensitivity, Color stroke){
        ImageDescription od = new ImageDescription(id, x, y, w, h, imagePath, stroke, region);
        od.setSensitive(sensitivity);
        region.addObject(od);
        return od;
    }

    /** Process XML description of a rectangle object. */
    RectangleDescription processRectangle(Element objectEL, String id, Region region){
        long x = Long.parseLong(objectEL.getAttribute(_x));
        long y = Long.parseLong(objectEL.getAttribute(_y));
        long w = Long.parseLong(objectEL.getAttribute(_w));
        long h = Long.parseLong(objectEL.getAttribute(_h));
        Color stroke = SVGReader.getColor(objectEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(objectEL.getAttribute(_fill));
        boolean sensitivity = (objectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(objectEL.getAttribute(_sensitive)) : true;
        RectangleDescription od = createRectangle(x, y, w, h, id, region, sensitivity, fill, stroke);
        return od;
    }
    
    /** Creates a rectangle and adds it to a region.
     *
     */
    public RectangleDescription createRectangle(long x, long y, long w, long h, String id, Region region,
                                                boolean sensitivity, Color fill, Color stroke){
        RectangleDescription od = new RectangleDescription(id, x, y, w, h, fill, stroke, region);
        od.setSensitive(sensitivity);
        region.addObject(od);
        return od;
    }

    /** Process XML description of a text object. */
    TextDescription processText(Element objectEL, String id, Region region){
        long x = Long.parseLong(objectEL.getAttribute(_x));
        long y = Long.parseLong(objectEL.getAttribute(_y));
        float scale = Float.parseFloat(objectEL.getAttribute(_scale));
        String text = objectEL.getFirstChild().getNodeValue();
        Color fill = SVGReader.getColor(objectEL.getAttribute(_fill));
        boolean sensitivity = (objectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(objectEL.getAttribute(_sensitive)) : true;
        short anchor = (objectEL.hasAttribute(_anchor)) ? TextDescription.getAnchor(objectEL.getAttribute(_anchor)) : VText.TEXT_ANCHOR_MIDDLE;
        TextDescription od = createText(x, y, id, region, scale, text, anchor, fill, sensitivity);
        return od;
    }
    
    /** Creates a text object and adds it to a region.
     *
     */
    public TextDescription createText(long x, long y, String id, Region region, float scale, String text,
                                      short anchor, Color fill, boolean sensitivity){
        TextDescription od = new TextDescription(id, x, y, scale, text, (fill != null) ? fill : Color.BLACK, anchor, region);
        od.setSensitive(sensitivity);
        region.addObject(od);
        return od;
    }

    /** Process XML description of a polygon object. */
    PolygonDescription processPolygon(Element objectEL, String id, Region region){
        LongPoint[] vertices = parseVertexCoordinates(objectEL.getAttribute(_points));
        Color stroke = SVGReader.getColor(objectEL.getAttribute(_stroke));
        Color fill = SVGReader.getColor(objectEL.getAttribute(_fill));
        boolean sensitivity = (objectEL.hasAttribute(_sensitive)) ? Boolean.parseBoolean(objectEL.getAttribute(_sensitive)) : true;
        PolygonDescription od = createPolygon(vertices, id, region, sensitivity, fill, stroke);
        return od;
    }
    
    /** Creates a polygon and adds it to a region.
     *
     */
    public PolygonDescription createPolygon(LongPoint[] vertices, String id, Region region,
                                            boolean sensitivity, Color fill, Color stroke){
        PolygonDescription od = new PolygonDescription(id, vertices, fill, stroke, region);
        od.setSensitive(sensitivity);
        region.addObject(od);
        return od;
    }
    
    public static LongPoint[] parseVertexCoordinates(String s){
        String[] points = s.split(";");
        LongPoint[] res = new LongPoint[points.length];
        String[] xy;
        for (int i=0;i<points.length;i++){
            xy = points[i].split(",");
            res[i] = new LongPoint(SVGReader.getLong(xy[0]), SVGReader.getLong(xy[1]));
        }
        return res;
    }

    /* ----------- level / region visibility update ----------- */

    int previousLevel = -2;
    int currentLevel = -1;
    boolean updateLevel = false;
    
    /** Enable/disable level updating.
     * Calls to updateLevel(altitude) have no effect if level updating is disabled.
     *@see #updateLevel(float altitude)
     */
    public void setUpdateLevel(boolean b){
	updateLevel = b;
    }

    /** Notify altitude changes.
     * It is up to the client application to notify the scene manager each time the altitude of the camera used to observe the scene changes.
     *@param altitude the new camera's altitude
     */
    public void updateLevel(float altitude){
        if (!updateLevel){return;}
        // find out new level
        for (int i=0;i<levels.length;i++){
            if (levels[i].inRange(altitude)){currentLevel = i;break;}
        }
        // compare to current level
        if (previousLevel != currentLevel){
            if (previousLevel >= 0){
                exitLevel(previousLevel, currentLevel > previousLevel);
            }
            enterLevel(currentLevel, currentLevel > previousLevel);
            previousLevel = currentLevel;
        }
        else {
            // if level hasn't changed, it is still necessary to update
            // visible regions as some of them might have become (in)visible
            updateVisibleRegions();
        }
    }

    /** Get the current level. 
     *@return index of level at which camera is right now (highest level is 0)
     */
    public int getCurrentLevel(){
	return currentLevel;
    }

    void enterLevel(int depth, boolean arrivingFromHigherAltLevel){
	    updateVisibleRegions(depth, (arrivingFromHigherAltLevel) ? Region.TFUL : Region.TFLL);
	    if (levelListener != null){
	        levelListener.enteredLevel(depth);
	    }
    }

    void exitLevel(int depth, boolean goingToLowerAltLevel){
        for (int i=0;i<levels[depth].regions.length;i++){
            levels[depth].regions[i].hide((goingToLowerAltLevel) ? Region.TTLL : Region.TTUL,
                                          sceneCameras[levels[depth].regions[i].li].posx, sceneCameras[levels[depth].regions[i].li].posy);
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
    public void updateVisibleRegions(){
	updateVisibleRegions(currentLevel, Region.TASL);
    }

    void updateVisibleRegions(int level, short transition){
        for (int i=0;i<levels[level].regions.length;i++){
            levels[level].regions[i].updateVisibility(sceneCameraBounds[levels[level].regions[i].li], currentLevel, transition, regionListener);
        }
    }

    /** Set the number of queued requests processed by the load/unload request handling thread before going to sleep.
     *@param nbRequests number of queued requests processed by the load/unload request handling thread before going to sleep (default is 5)
     */
    public void setNumberOfRequestsHandledPerCycle(int nbRequests){
	glyphLoader.setNumberOfRequestsHandledPerCycle(nbRequests);
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

}
