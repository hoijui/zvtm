package fr.inria.zuist.cluster;

import fr.inria.zuist.engine.FitsResourceHandler;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;

//set a resource handler for FITS files whenever a SceneManager is
//instantiated
aspect EnableZuistSupport {
    //sceneManagerCreation defined in zuist-cluster aspects
    
    after(SceneManager sceneManager, 
            VirtualSpace[] spaces,
            Camera[] cameras) returning(): 
        SceneManagerReplication.sceneManagerCreation(sceneManager,
                spaces, cameras){
        sceneManager.setResourceHandler(
                FitsResourceHandler.RESOURCE_TYPE_FITS, 
                new FitsResourceHandler()
                );
    }
}

