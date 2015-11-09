/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: EnableFitsSupport.aj 5488 2015-04-15 20:36:58Z fdelcampo $
 */

package fr.inria.zuist.cluster;

import fr.inria.zuist.engine.JSkyFitsResourceHandler;
import fr.inria.zuist.engine.FitsResourceHandler;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;

import java.util.HashMap;


//set a resource handler for FITS files whenever a SceneManager is
//instantiated
aspect EnableZuistSupport {
    pointcut sceneManagerCreation(SceneManager sceneManager,
            VirtualSpace[] spaces, Camera[] cameras , HashMap<String,String> properties) :
        execution(public SceneManager.new(VirtualSpace[], Camera[], HashMap<String,String>)) &&
        this(sceneManager) &&
        args(spaces, cameras, properties);

    after(SceneManager sceneManager,
            VirtualSpace[] spaces,
            Camera[] cameras, HashMap<String,String> properties) returning():
        sceneManagerCreation(sceneManager,
                spaces, cameras, properties){
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

