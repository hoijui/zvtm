/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.io.File;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.Hashtable;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectProgress;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/** Description of a part of the scene that is loaded/unloaded dynamically.
 *@author Emmanuel Pietriga
 */

public class SceneFragmentDescription extends ResourceDescription {

    public static final String RESOURCE_TYPE_SCENE = "scn";
    SceneManager sm;
    
    Vector<Region> regions;

    /** Constructs the description of an image (VImageST).
    *@param id ID of object in scene
    *@param x x-coordinate in scene
    *@param y y-coordinate in scene
    *@param z z-index (layer). Feed 0 if you don't know.
    *@param w width in scene
    *@param h height in scene
    *@param p path to bitmap resource (any valid absolute URL)
    *@param sc border color
    *@param pr parent Region in scene
    */
    SceneFragmentDescription(String id, long x, long y, URL p, Region pr, SceneManager sm){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.setURL(p);
        this.parentRegion = pr;
        this.sm = sm;
    }

    /** Type of resource.
    *@return type of resource.
    */
    public String getType(){
        return RESOURCE_TYPE_SCENE;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(final VirtualSpace vs, final boolean fadeIn){
        if (regions == null){
            try {
                File sceneFile = new File(src.toURI());
                File sceneFileDirectory = sceneFile.getParentFile();
                Document scene = SceneManager.parseXML(sceneFile);
                Element root = scene.getDocumentElement();
                NodeList nl = root.getChildNodes();
                Node n;
                Element e;
                Hashtable regionName2containerRegionName = new Hashtable();
                regions = new Vector<Region>();
                for (int i=0;i<nl.getLength();i++){
                    n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE){
                        e = (Element)n;
                        if (e.getTagName().equals(SceneManager._region)){
                            regions.add(sm.processRegion(e, regionName2containerRegionName, sceneFileDirectory));
                        }
                    }
                }            
            }
            catch (URISyntaxException ex){System.err.println();ex.printStackTrace();}            
        }
        loadRequest = null;    
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, boolean fadeOut){
        if (regions != null){
            for (Region region:regions){
                sm.destroyRegion(region);
            }
            regions = null;            
        }
        unloadRequest = null;
    }
    
    public Glyph getGlyph(){
        return null;
    }

}