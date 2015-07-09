/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zuist.engine.SceneManager;

public class ViewSceneObserver implements SceneObserver, CameraListener {

    SceneManager sm;
    double prevAlt;

    View v;
    Camera c;
    VirtualSpace vs;

    /**
     *@param observingView view that observes the scene
     *@param observingCamera camera in view that observes the scene
     *param targetVirtualSpace virtual space in which the scene objects should be put
     */
    public ViewSceneObserver(View observingView, Camera observingCamera, VirtualSpace targetVirtualSpace){
        this.v = observingView;
        this.c = observingCamera;
        this.c.addListener(this);
        this.vs = targetVirtualSpace;
    }

    public Camera getCamera(){
        return c;
    }

    public double[] getVisibleRegion(){
        return v.getVisibleRegion(c);
    }

    public double getX(){
        return c.vx;
    }

    public double getY(){
        return c.vy;
    }

    public double getAltitude(){
        return c.getAltitude();
    }

    public VirtualSpace getTargetVirtualSpace(){
        return vs;
    }

    /* Camera events handling */
    public void cameraMoved(Camera cam, Point2D.Double loc, double alt){
        sm.regUpdater.addEntry(this, new Location(loc.x, loc.y, alt));
    }

    public void setSceneManager(SceneManager sm){
        this.sm = sm;
    }

    public void setPreviousAltitude(double a){
        this.prevAlt = a;
    }

    public double getPreviousAltitude(){
        return this.prevAlt;
    }

}
