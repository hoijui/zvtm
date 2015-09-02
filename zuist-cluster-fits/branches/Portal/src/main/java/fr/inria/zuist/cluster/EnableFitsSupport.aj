/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: EnableFitsSupport.aj 5709 2015-08-15 00:03:47Z fdelcampo $
 */

package fr.inria.zuist.cluster;

import fr.inria.zuist.engine.JSkyFitsResourceHandler;
// import fr.inria.zuist.engine.FitsResourceHandler;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.SceneObserver;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;

import java.util.HashMap;

//set a resource handler for FITS files whenever a SceneManager is
//instantiated
aspect EnableZuistSupport {
    pointcut sceneManagerCreation(SceneManager sceneManager,
            SceneObserver[] observers, HashMap<String,String> properties) :
        execution(public SceneManager.new(SceneObserver[], HashMap<String,String>)) &&
        this(sceneManager) &&
        args(observers, properties);

    after(SceneManager sceneManager,
            SceneObserver[] observers, HashMap<String,String> properties) returning():
        sceneManagerCreation(sceneManager,
                observers, properties){
        sceneManager.setResourceHandler(
                JSkyFitsResourceHandler.RESOURCE_TYPE_FITS,
                new JSkyFitsResourceHandler()
                );
        // sceneManager.setResourceHandler(
        //         FitsResourceHandler.RESOURCE_TYPE_FITS,
        //         new FitsResourceHandler()
        //         );

    }
}
