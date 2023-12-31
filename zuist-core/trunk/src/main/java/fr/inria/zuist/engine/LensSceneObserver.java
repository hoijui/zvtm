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
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.engine.Location;

import fr.inria.zvtm.event.CameraListener;
import fr.inria.zuist.engine.SceneManager;

public class LensSceneObserver extends SceneObserver implements CameraListener {

    View v;
    Lens l;

    /**
     *@param observingView view that observes the scene
     *@param observingCamera camera in view that observes the scene
     *@param targetVirtualSpace virtual space in which the scene objects should be put
     */
    public LensSceneObserver(View observingView, Camera observingCamera,
                             Lens observingLens, VirtualSpace targetVirtualSpace){
        this(observingView, observingCamera, observingLens, targetVirtualSpace, 1, 1);
    }

    /**
     *@param observingView view that observes the scene
     *@param observingCamera camera in view that observes the scene
     *@param targetVirtualSpace virtual space in which the scene objects should be put
     *@param hpf horizontal preload factor. Multiply the SceneObserver's observed region width by hpf for the only purpose of computing what is visible through it. Default is 1.
     *@param vpf vertical preload factor. Multiply the SceneObserver's observed region height by vpf for the only purpose of computing what is visible through it. Default is 1.
     */
    public LensSceneObserver(View observingView, Camera observingCamera,
                             Lens observingLens, VirtualSpace targetVirtualSpace, double hpf, double vpf){
        this.v = observingView;
        this.c = observingCamera;
        this.l = observingLens;
        this.c.addListener(this);
        this.vs = targetVirtualSpace;
        this.hpf = hpf;
        this.vpf = vpf;
    }

    public double[] getVisibleRegion(){
        if (hpf != 1 || vpf != 1){
            double[] res = l.getVisibleRegion(c, new double[4]);
            double cvx = getX();
            double cvy = getY();
            res[0] = cvx - (cvx-res[0]) * hpf;
            res[1] = cvy + (res[1]-cvy) * vpf;
            res[2] = cvx + (res[2]-cvx) * hpf;
            res[3] = cvy - (cvy-res[3]) * vpf;
            return res;
        }
        else {
            return l.getVisibleRegion(c, new double[4]);
        }
    }

    // x-coord of the conceptual camera that would be observing the region
    // corresponding to the lens (taking its mag factor into account)
    public double getX(){
        return c.vx + l.lx * ((c.focal+c.altitude)/c.focal);
    }

    // altitude of the conceptual camera that would be observing the region
    // corresponding to the lens (taking its mag factor into account)
    public double getY(){
        return c.vy - l.ly * ((c.focal+c.altitude)/c.focal);
    }

    // altitude of the conceptual camera that would be observing the region
    // corresponding to the lens (taking its mag factor into account)
    public double getAltitude(){
        double deltAlt = (c.getAltitude() + c.getFocal())
                         * (1-l.getMaximumMagnification()) / l.getMaximumMagnification();
        return c.getAltitude() + deltAlt;
    }

    /* Camera events handling */
    public void cameraMoved(Camera cam, Point2D.Double loc, double alt){
        sm.regUpdater.addEntry(this, new Location(getX(), getY(), getAltitude()));
    }

    /** This method has to be called whenever the lens moves within the view.
       We have no way to detect such changes currently, so the application
       has to trigger such calls. */
    public void lensMoved(){
        sm.regUpdater.addEntry(this, new Location(getX(), getY(), getAltitude()));
    }

    /** This method has to be called whenever the lens' magnification factor changes.
       We have no way to detect such changes currently, so the application
       has to trigger such calls. */
    public void lensMagnified(){
        sm.regUpdater.addEntry(this, new Location(getX(), getY(), getAltitude()));
    }

}
