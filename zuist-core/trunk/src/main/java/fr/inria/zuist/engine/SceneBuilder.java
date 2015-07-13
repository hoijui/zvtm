/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.Image;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.ImageIcon;

import java.util.HashMap;
import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.svg.SVGReader;
import fr.inria.zuist.event.ProgressListener;

import fr.inria.zuist.od.ResourceDescription;
import fr.inria.zuist.od.ObjectDescription;
import fr.inria.zuist.od.SceneFragmentDescription;
import fr.inria.zuist.od.TextDescription;
import fr.inria.zuist.od.ImageDescription;
import fr.inria.zuist.od.GlyphDescription;
import fr.inria.zuist.od.ClosedShapeDescription;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

/** Scene Builder.
 * Provides the API to build a scene: levels, regions, object descriptions.
 * Also Used to parse XML descriptions of ZUIST scenes and manage them once instantiated.
 * Each SceneManager instantiates one SceneBuilder.
 *@see SceneManager#getSceneBuilder()
 *@author Emmanuel Pietriga
 */

public class SceneBuilder {

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
    public static final String _tags = "tags";
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
    public static final String TAG_SEPARATOR = ",";

    static final String URL_PROTOCOL_SEQ = ":/";
    static final String JAR_PROTOCOL_SEQ = ":!/";
    static final String FILE_PROTOCOL_HEAD = "file://";

    SceneManager sm;

    SceneBuilder(SceneManager sm){
        this.sm = sm;
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
            sm.reset();
        }
        Element root = scene.getDocumentElement();
        // scene attributes
        processSceneAttributes(root);
        NodeList nl = root.getElementsByTagName(_level);
        if (reset || sm.levels.length == 0){
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
                Region r = sm.id2region.get(rn);
                Region cr = sm.id2region.get(regionName2containerRegionName.get(rn));
                if (r != null && cr != null){
                    cr.addContainedRegion(r);
                    r.setContainingRegion(cr);
                }
                else {
                    if (SceneManager.getDebugMode()){
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
                sm.sceneAttrs.put(_background, bkg);
            }
        }
    }

    public SceneFragmentDescription createSceneFragmentDescription(double x, double y, String id, Region region, URL resourceURL){
        SceneFragmentDescription sd = new SceneFragmentDescription(id, x, y, resourceURL, region, sm);
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
        if (depth >= sm.levels.length){
            Level[] tmpL = new Level[depth+1];
            System.arraycopy(sm.levels, 0, tmpL, 0, sm.levels.length);
            sm.levels = tmpL;
        }
        sm.levels[depth] = new Level(calt, falt);
        return sm.levels[depth];
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
     *@param tags tags associated with region (can be used to filter regions in SceneObservers)
     *@param transitions a 4-element array with values in Region.{FADE_IN, FADE_OUT, APPEAR, DISAPPEAR}, corresponding to
                         transitions from upper level, from lower level, to upper level, to lower level.
     *@param requestOrdering how requests for loading / unloading objects should be ordered when
                             entering / leaving this region; one of Region.{ORDERING_ARRAY, ORDERING_DISTANCE}.
     *@see Region#setContainingRegion(Region r)
     *@see Region#addContainedRegion(Region r)
     */
    public Region createRegion(double x, double y, double w, double h, int highestLevel, int lowestLevel,
                               String id, String title, String[] tags,
                               short[] transitions, short requestOrdering){
        Region region = new Region(x+sm.origin.x, y+sm.origin.y, w, h, highestLevel, lowestLevel, id, tags, transitions, requestOrdering, sm);
        if (!sm.id2region.containsKey(id)){
            sm.id2region.put(id, region);
        }
        else {
            if (SceneManager.getDebugMode()){System.err.println("Error: ID "+id+" used to identify more than one region.");}
            return null;
        }
        for (int i=highestLevel;i<=lowestLevel;i++){
            sm.levels[i].addRegion(region);
        }
        if (title != null && title.length() > 0){
            region.setTitle(title);
        }
        return region;
    }

    public Region processRegion(Element regionEL, HashMap<String,String> rn2crn, File sceneFileDirectory){
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
        String[] tags = null;
        if (regionEL.hasAttribute(_tags)){
            tags = regionEL.getAttribute(_tags).split(TAG_SEPARATOR);
        }
        Region region = createRegion(x, y, w, h, highestLevel, lowestLevel, id, title,
                                     tags, transitions,
                                     (regionEL.hasAttribute(_ro)) ? Region.parseOrdering(regionEL.getAttribute(_ro)) : Region.ORDERING_DISTANCE);
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
        Region[] ral = sm.getRegionsAtLevel(l);
        for (int i=0;i<ral.length;i++){
            destroyRegion(ral[i]);
        }
    }

    /** Destroy a region.
     * Destroying a region destroys all object descriptions it contains.
     *@param r region to be destroyed
     */
    public void destroyRegion(Region r){
        //XXX should reinstate commented out code, calling it for all VirtualSpace involved
        //r.forceHide(Region.DISAPPEAR, r.x, r.y);
        ObjectDescription[] ods = r.getObjectsInRegion();
        for (int i=0;i<ods.length;i++){
            sm.id2object.remove(ods[i].getID());
        }
        sm.id2region.remove(r.getID());
        for (int i=r.getLowestLevel();i<=r.getHighestLevel();i++){
            sm.levels[i].removeRegion(r);
        }
    }

    ObjectDescription processObject(Element e, Region region, File sceneFileDirectory){
        ObjectDescription od = null;
        // extract info shared by all types of elements
        String id = e.getAttribute(_id);
        int zindex = (e.hasAttribute(_zindex)) ? Integer.parseInt(e.getAttribute(_zindex)) : 0;
        if (id == null || id.length() == 0){
            if (SceneManager.getDebugMode()){System.err.println("Warning: object "+e+" has no ID");}
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
            if (SceneManager.getDebugMode()){System.err.println("Error: failed to process object declaration: "+id);}
            return null;
        }
        String tto = e.getAttribute(_takesToO);
        String ttr = e.getAttribute(_takesToR);
        if (tto != null && tto.length() > 0){
            od.setTakesTo(tto, SceneManager.TAKES_TO_OBJECT);
        }
        else if (ttr != null && ttr.length() > 0){
            od.setTakesTo(ttr, SceneManager.TAKES_TO_REGION);
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
        URL absoluteSrc = SceneBuilder.getAbsoluteURL(src, sceneFileDirectory);
        if (type.equals(ImageDescription.RESOURCE_TYPE_IMG)){
            double w = Double.parseDouble(resourceEL.getAttribute(_w));
            double h = Double.parseDouble(resourceEL.getAttribute(_h));
            return createImageDescription(x+sm.origin.x, y+sm.origin.y, w, h, id, zindex, region, absoluteSrc, sensitivity, stroke, alpha, params);
        }
        else if (type.equals(SceneFragmentDescription.RESOURCE_TYPE_SCENE)){
            return createSceneFragmentDescription(x+sm.origin.x, y+sm.origin.y, id, region, absoluteSrc);
        }
        else {
            return createResourceDescription(x+sm.origin.x, y+sm.origin.y, id, zindex, region, absoluteSrc, type, sensitivity, stroke, params);
        }
    }

    /** Creates a resource and adds it to a region.
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param zindex z-index
        *@param resourceURL path to resource (should be absolute)
        *@param type resource type ("img", "pdf", ...)
        *@param sensitivity should the object be sensitive to mouse events or not.
        *@param stroke border color
        *@param region parent Region in scene
        *@param params custom parameters for a given type of resource
     */
    public ResourceDescription createResourceDescription(double x, double y, String id, int zindex, Region region,
                                                         URL resourceURL, String type, boolean sensitivity, Color stroke, String params){
        if (sm.RESOURCE_HANDLERS.containsKey(type)){
            ResourceDescription rd = (sm.RESOURCE_HANDLERS.get(type)).createResourceDescription(x, y, id, zindex, region,
                                                                                             resourceURL, sensitivity, stroke, params);
            if (!sm.id2object.containsKey(id)){
                sm.id2object.put(id, rd);
            }
            else {
                if (SceneManager.getDebugMode())System.err.println("Warning: ID: "+id+" used to identify more than one object.");
            }
            return rd;
        }
        else {
            if (SceneManager.getDebugMode()){System.err.println("Error: failed to process resource declaration "+id+" : no appropriate handler for this type of resource");}
            return null;
        }
    }

    /** Creates an image and adds it to a region.
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param zindex z-index
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
        if (!sm.id2object.containsKey(id)){
            sm.id2object.put(id, imd);
        }
        else {
            if (SceneManager.getDebugMode())System.err.println("Warning: ID: "+id+" used to identify more than one object.");
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
        if (!sm.id2object.containsKey(id)){
            sm.id2object.put(id, gd);
        }
        else {
            if (SceneManager.getDebugMode())System.err.println("Warning: ID: "+id+" used to identify more than one object.");
        }
        return gd;
    }

    public GlyphDescription createGlyphDescription(Glyph g, String id,
            int zindex, Region region, boolean sensitivity){
        GlyphDescription gd = new GlyphDescription(id, g, zindex, region, sensitivity);
        region.addObject(gd);
        if (!sm.id2object.containsKey(id)){
            sm.id2object.put(id, gd);
        } else {
            if(SceneManager.getDebugMode()){
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
        ClosedShape g = new VRectangle(x+sm.origin.x, y+sm.origin.y, zindex, w, h, (fill!=null) ? fill : Color.BLACK, (stroke!=null) ? stroke : Color.WHITE, 1.0f);
        if (fill == null){g.setFilled(false);}
        if (stroke == null){g.setDrawBorder(false);}
        return createClosedShapeDescription(g, id, zindex, region, sensitivity);
    }

    /** Process XML description of a polygon object. */
    ClosedShapeDescription processPolygon(Element polygonEL, String id, int zindex, Region region){
        Point2D.Double[] vertices = parseVertexCoordinates(polygonEL.getAttribute(_points), sm.origin);
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
        TextDescription od = createTextDescription(x+sm.origin.x, y+sm.origin.y, id, zindex, region, scale, text,
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
        if (!sm.id2object.containsKey(id)){
            sm.id2object.put(id, td);
        }
        else {
            if (SceneManager.getDebugMode())System.err.println("Warning: ID: "+id+" used to identify more than one object.");
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
        sm.setOrigin(new Point2D.Double(x, y));
        loadScene(parseXML(f), f.getParentFile(), false, null);
        sm.setOrigin(new Point2D.Double(0, 0));
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
            catch(MalformedURLException ex){if (SceneManager.getDebugMode())System.err.println("Error: malformed resource URL: "+src);}
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
            catch(IOException ex){if (SceneManager.getDebugMode()){System.err.println("Error: unable to make URL from path to: "+src);ex.printStackTrace();}}
        }
        return null;
    }

}
