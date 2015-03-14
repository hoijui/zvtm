/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.io.File;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.HashMap;

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

    /** Constructs the description of a scene fragment.
    *@param id ID of object in scene
    *@param x x-coordinate in scene
    *@param y y-coordinate in scene
    *@param p path to bitmap resource (any valid absolute URL)
    *@param pr parent Region in scene
    */
    SceneFragmentDescription(String id, double x, double y, URL p, Region pr, SceneManager sm){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.setURL(p);
        this.parentRegion = pr;
        this.sm = sm;
    }

    @Override
    public String getType(){
        return RESOURCE_TYPE_SCENE;
    }

    @Override
    public void createObject(SceneManager sm, final VirtualSpace vs, final boolean fadeIn){
        if (regions == null){
            try {
                File sceneFile = new File(src.toURI());
                File sceneFileDirectory = sceneFile.getParentFile();
                Document scene = SceneManager.parseXML(sceneFile);
                Element root = scene.getDocumentElement();
                NodeList nl = root.getElementsByTagName(SceneManager._region);
                Node n;
                HashMap<String,String> regionName2containerRegionName = new HashMap<String,String>(nl.getLength());
                regions = new Vector<Region>(nl.getLength());
                for (int i=0;i<nl.getLength();i++){
                    n = nl.item(i);
                    regions.add(sm.processRegion((Element)n, regionName2containerRegionName, sceneFileDirectory));
                }
                sm.updateVisibleRegions();
            }
            catch (URISyntaxException ex){if (SceneManager.getDebugMode()){ex.printStackTrace();}}
        }
    }

    @Override
    public void destroyObject(SceneManager sm, VirtualSpace vs, boolean fadeOut){
        if (regions != null){
            for (Region region:regions){
                sm.destroyRegion(region);
            }
            regions = null;
        }
        sm.destroySceneFragment(this);
    }

    /** Does not return anything since this is a scene fragment. */
    @Override
    public Glyph getGlyph(){
        return null;
    }

}
