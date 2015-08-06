/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.geom.Point2D;

import java.util.HashMap;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zuist.engine.SceneManager;

/** Same as ViewSceneObserver, but ignores Regions that are not tagged with one
    of the tags declared in a mapping from tags to target VirtualSpaces.
    ObjectDescriptions in Regions tagged with one of the tags declared in this
    SceneObserver get instantiated in the VirtualSpace mapped to the tag,
    as declared in the SceneObserver's constructor.
    */

public class TaggedViewSceneObserver extends ViewSceneObserver {

    HashMap<String,VirtualSpace> tag2space;

    /**
     *@param observingView view that observes the scene
     *@param observingCamera camera in view that observes the scene
     *@param t2s tag -> corresponding VirtualSpace mapping.
     */
    public TaggedViewSceneObserver(View observingView, Camera observingCamera, HashMap<String,VirtualSpace> t2s){
        super(observingView, observingCamera, null);
        tag2space = t2s;
    }

    @Override
    public VirtualSpace[] getTargetVirtualSpaces(){
        return tag2space.values().toArray(new VirtualSpace[tag2space.size()]);
    }

    /** Returns the VirtualSpace corresponding to the first tag that matches.
        If multiple tags in the Region match those declared in this SceneObserver,
        there is no guarantee as to which one will be returned.*/
    @Override
    public VirtualSpace getTargetVirtualSpace(Region r){
        if (r.getTags() == null){
            return null;
        }
        else {
            for (String tag:r.getTags()){
                if (tag2space.containsKey(tag)){
                    return tag2space.get(tag);
                }
            }
            return null;
        }
    }

    /** Tells the SceneManager whether a given region is of interest to this particular SceneObserver.
     * If it is, the SceneObserver crossing this region will trigger the loading of the ObjectDescriptions it contains.
     * If it is not, the Region will be ignored.
     * The default behaviour is to return true. Application-specific subclasses of SceneObserver can override this method
     * to return true/false based, e.g., on the tags declared for the region.
     */
    @Override
    public boolean isOfInterest(Region r){
        if (r.getTags() == null){
            return false;
        }
        else {
            for (String tag:r.getTags()){
                if (tag2space.containsKey(tag)){
                    return true;
                }
            }
            return false;
        }
    }

    public HashMap<String,VirtualSpace> getTagVirtualSpaceMapping(){
        return tag2space;
    }

}
