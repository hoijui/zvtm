/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: EnableFitsSupport.aj 5262 2014-12-31 13:20:31Z fdelcampo $
 */

package fr.inria.zuist.cluster;

import fr.inria.zuist.engine.JSkyFitsResourceHandler;
import fr.inria.zuist.engine.FitsResourceHandler;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;

//set a resource handler for FITS files whenever a SceneManager is
//instantiated
aspect EnableZuistSupport {
    pointcut sceneManagerCreation(SceneManager sceneManager,
            VirtualSpace[] spaces, Camera[] cameras) :
        execution(public SceneManager.new(VirtualSpace[], Camera[])) &&
        this(sceneManager) &&
        args(spaces, cameras);

    after(SceneManager sceneManager,
            VirtualSpace[] spaces,
            Camera[] cameras) returning():
        sceneManagerCreation(sceneManager,
                spaces, cameras){
        sceneManager.setResourceHandler(
                JSkyFitsResourceHandler.RESOURCE_TYPE_FITS,
                new JSkyFitsResourceHandler()
                );
        sceneManager.setResourceHandler(
                FitsResourceHandler.RESOURCE_TYPE_FITS,
                new FitsResourceHandler()
                );

    }
}

