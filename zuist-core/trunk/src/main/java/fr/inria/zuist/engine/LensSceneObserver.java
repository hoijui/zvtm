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
     *param targetVirtualSpace virtual space in which the scene objects should be put
     */
    public LensSceneObserver(View observingView, Camera observingCamera,
                             Lens observingLens, VirtualSpace targetVirtualSpace){
        this.v = observingView;
        this.c = observingCamera;
        this.l = observingLens;
        this.c.addListener(this);
        this.vs = targetVirtualSpace;
    }

    public double[] getVisibleRegion(){
        return l.getVisibleRegion(c, new double[4]);
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

    // XXX
    // probably have to do something about mag factor in relation with altitude

}
