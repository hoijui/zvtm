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

    public abstract double[] getVisibleRegion();

    public abstract double getAltitude();

    abstract public double getX();

    abstract public double getY();

    public VirtualSpace getTargetVirtualSpace(){
        return vs;
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

}
