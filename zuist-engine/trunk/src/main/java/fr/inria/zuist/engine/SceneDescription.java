/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.engine;

import java.net.URL;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectProgress;

/** Description of a part of the scene that is loaded/unloaded dynamically.
 *@author Emmanuel Pietriga
 */

public class SceneDescription extends ResourceDescription {

    public static final String RESOURCE_TYPE_SCENE = "scn";

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
    SceneDescription(String id, long x, long y, URL p, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.setURL(p);
        this.parentRegion = pr;
    }

    /** Type of resource.
    *@return type of resource.
    */
    public String getType(){
        return RESOURCE_TYPE_SCENE;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(final VirtualSpace vs, final boolean fadeIn){
        // open connection to data
        if (showFeedbackWhenFetching){
        }
        else {
        }                
        loadRequest = null;    
    }

    private synchronized void finishLoadingScene(VirtualSpace vs, VRectProgress vrp, boolean fadeIn){
        
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, boolean fadeOut){
        unloadRequest = null;
    }
    
    public Glyph getGlyph(){
        return null;
    }

}