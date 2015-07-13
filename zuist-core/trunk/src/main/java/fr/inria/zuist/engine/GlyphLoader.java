/*   AUTHOR: Romain Primet (romain.primet@inria.fr)
 *   Copyright (c) INRIA, 2007-2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpace;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import fr.inria.zuist.od.ObjectDescription;

/**
 * Thread safety: GlyphLoader public methods should be invoked
 * from the same thread, normally the Swing EDT.
 *@author Romain Primet, Emmanuel Pietriga
 */

public class GlyphLoader {

    private final SceneManager sceneManager;
    private final ConcurrentHashMap<ObjectDescription, LoadAction> tasks;
    private final ExecutorService loader;

    private enum LoadAction {LOAD, UNLOAD};

    public static int FADE_IN_DURATION = 300; //milliseconds
    public static int FADE_OUT_DURATION = 300; //milliseconds

    GlyphLoader(SceneManager sm){
        this.sceneManager = sm;
        loader = Executors.newSingleThreadExecutor(new LoaderThreadFactory());
        tasks = new ConcurrentHashMap<ObjectDescription, LoadAction>();
    }

    int getPendingRequestQueueSize(){
        return tasks.size();
    }

    /** Add a request to load an object in the queue.
     *@param od description of object to be loaded.
     *@param transition one of Region.{APPEAR,FADE_IN}
     */
    public void addLoadRequest(VirtualSpace tvs, ObjectDescription od, boolean transition){
        if(tasks.remove(od, LoadAction.UNLOAD)){
            return;
        }
        tasks.put(od, LoadAction.LOAD);

        if(tvs == null){
            if (SceneManager.getDebugMode()){System.err.println("addLoadRequest: could not retrieve virtual space "+tvs);}
            return;
        }
        loader.submit(new Request(this.sceneManager, tvs, od, transition));
    }

    /**
     * Shuts down this GlyphLoader. The loader should not be used after this method has been invoked.
     */
    public void shutdown(){
        loader.shutdownNow();
    }

    /** Add a request to unload an object in the queue.
     *@param od description of object to be loaded.
     *@param transition one of Region.{DISAPPEAR,FADE_OUT}
     */
    public void addUnloadRequest(VirtualSpace tvs, ObjectDescription od, boolean transition){
        if(tasks.remove(od, LoadAction.LOAD)){
            return;
        }

        tasks.put(od, LoadAction.UNLOAD);

        if(tvs == null){
            if (SceneManager.getDebugMode()){System.err.println("addLoadRequest: could not retrieve virtual space"+tvs);}
            return;
        }
        loader.submit(new Request(this.sceneManager, tvs, od, transition));
    }

    private class Request implements Runnable {
        final VirtualSpace target;
        final ObjectDescription od;
        final boolean transition;
        final SceneManager sm;

        Request(SceneManager sm, VirtualSpace target, ObjectDescription od, boolean transition){
            this.sm = sm;
            this.target = target;
            this.od = od;
            this.transition = transition;
        }

        public void run(){
            LoadAction action = tasks.remove(od);
           if(action == null){
               return;
           } else if(action.equals(LoadAction.LOAD)){
               od.createObject(sm, target, transition);
           } else {
               od.destroyObject(sm, target, transition);
           }
        }
    }

    private static class LoaderThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r){
            Thread retval = new Thread(r, "sceneLoader");
            retval.setDaemon(true);
            return retval;
        }
    }
}
