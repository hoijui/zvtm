/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.util.concurrent.ConcurrentHashMap;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.RegionListener;

public abstract class SceneObserver {

    SceneManager sm;

    static final Short DUMMY_SHORT = new Short((short)0);
    ConcurrentHashMap<Region,Short> observedRegions = new ConcurrentHashMap(20, .75f, 4);

    VirtualSpace vs;
    Camera c;

    double prevAlt;
    int previousLevel = -2;
    int currentLevel = -1;

    LevelListener levelListener;
    RegionListener regionListener;

    double hpf = 1d;
    double vpf = 1d;

    public abstract double[] getVisibleRegion();

    public abstract double getAltitude();

    abstract public double getX();

    abstract public double getY();

    /** Region is irrelevant for basic SceneObservers. It matters only when a decision has to be made about which VirtualSpace
        to return depending on the region's properties. For instance, it matters for TagggedViewSceneObserver, but it does not matter for ViewSceneObserver.*/
    public VirtualSpace getTargetVirtualSpace(Region r){
        return vs;
    }

    /** This is overridden by more elaborate SceneObservers such as TagggedViewSceneObserver. */
    public VirtualSpace[] getTargetVirtualSpaces(){
        return new VirtualSpace[]{vs};
    }

    /** Tells the SceneManager whether a given region is of interest to this particular SceneObserver.
     * If it is, the SceneObserver crossing this region will trigger the loading of the ObjectDescriptions it contains.
     * If it is not, the Region will be ignored.
     * The default behaviour is to return true. Application-specific subclasses of SceneObserver can override this method
     * to return true/false based, e.g., on the tags declared for the region.
     */
    public boolean isOfInterest(Region r){
        return true;
    }

    void setSceneManager(SceneManager sm){
        this.sm = sm;
    }

    public Camera getCamera(){
        return c;
    }

    public void setPreviousAltitude(double a){
        this.prevAlt = a;
    }

    public double getPreviousAltitude(){
        return this.prevAlt;
    }

    /** Get the current level.
     *@return index of level at which camera is right now (highest level is 0)
     */
    public int getCurrentLevel(){
        return currentLevel;
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


    /** Multiply the SceneObserver's observed region width by hpf for the only purpose of computing what is visible through it.
     *@param hpf horizontal preload factor. Default is 1.
     */
    public void setHorizontalPreloadFactor(double hpf){
        this.hpf = hpf;
    }

    /** Multiply the SceneObserver's observed region height by vpf for the only purpose of computing what is visible through it.
     *@param vpf vertical preload factor. Default is 1.
     */
    public void setVerticalPreloadFactor(double vpf){
        this.vpf = vpf;
    }

}
