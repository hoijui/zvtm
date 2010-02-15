/*   AUTHOR: Romain Primet (romain.primet@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */
package fr.inria.zuist.engine;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

import fr.inria.zvtm.engine.VirtualSpace;

class GlyphLoader {

    private final SceneManager sceneManager;
    private final int NTHREADS = 40;
    private final int CAPACITY = 200;
    private final ThreadPoolExecutor executor;
    private volatile boolean enabled = true;

    static int FADE_IN_DURATION = 300; //milliseconds
    static int FADE_OUT_DURATION = 300; //milliseconds

    GlyphLoader(SceneManager sm){
        this.sceneManager = sm;
        executor = new ThreadPoolExecutor(NTHREADS, NTHREADS, 0L,
                TimeUnit.MILLISECONDS, 
                new LinkedBlockingQueue<Runnable>(CAPACITY));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //layerIndex maps to a VirtualSpace
    public void addLoadRequest(int layerIndex, ObjectDescription od, boolean transition){
        if(!enabled){
            return;
        }

        final VirtualSpace target = sceneManager.getSpaceByIndex(layerIndex);
        if(target == null){
            System.err.println("addLoadRequest: could not retrieve virtual space");
            return;
        }
        executor.submit(new Request(target, Request.TYPE_LOAD, od, transition));	
    }

    //layerIndex maps to a VirtualSpace
    public void addUnloadRequest(int layerIndex, ObjectDescription od, boolean transition){
        if(!enabled){
            return;
        }

        final VirtualSpace target = sceneManager.getSpaceByIndex(layerIndex);
        if(target == null){
            System.err.println("addLoadRequest: could not retrieve virtual space");
            return;
        }
        executor.submit(new Request(target, Request.TYPE_UNLOAD, od, transition));
    }

    /**
     * Enables or disables this GlyphLoader.
     * A disabled GlyphLoader silently discards requests.
     * (i.e. addLoadRequest and addUnloadRequest have no effect).
     */
    public void setEnabled(boolean enable){
        this.enabled = enable;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted 
     * tasks are executed, but no new tasks will be accepted.
     */
    public void shutdown(){ executor.shutdown(); }

}

