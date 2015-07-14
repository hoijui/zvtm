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
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zuist.engine.SceneManager;

public class PortalSceneObserver extends SceneObserver implements CameraListener {

    CameraPortal cp;

    /**
     *@param observingPortal portal that observes the scene
     *@param observingCamera camera in portal that observes the scene
     *param targetVirtualSpace virtual space in which the scene objects should be put
     */
    public PortalSceneObserver(CameraPortal observingPortal, Camera observingCamera, VirtualSpace targetVirtualSpace){
        this.cp = observingPortal;
        this.c = observingCamera;
        this.c.addListener(this);
        this.vs = targetVirtualSpace;
    }

    public double[] getVisibleRegion(){
        return cp.getVisibleRegion();
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

    public void cameraMoved(){
        sm.regUpdater.addEntry(this, new Location(c.vx, c.vy, c.altitude));
    }

    /* Camera events handling */
    public void cameraMoved(Camera cam, Point2D.Double loc, double alt){
        cameraMoved();
    }

}
