/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zuist.engine.SceneManager;

public interface SceneObserver {

    public double[] getVisibleRegion();

    public double getAltitude();

    void setLayerIndex(int li);

    public int getLayerIndex();

    void setPreviousAltitude(double a);

    double getPreviousAltitude();

    public double getX();

    public double getY();

    public VirtualSpace getTargetVirtualSpace();

    void setSceneManager(SceneManager sm);

}
