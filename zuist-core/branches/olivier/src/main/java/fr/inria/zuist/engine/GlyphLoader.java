/*   AUTHOR: Romain Primet (romain.primet@inria.fr)
 *   Copyright (c) INRIA, 2007-2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: GlyphLoader.java 5315 2015-01-29 08:34:19Z epietrig $
 */

package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpace;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread safety: GlyphLoader public methods should be invoked
 * from the same thread, normally the Swing EDT.
 *@author Romain Primet, Emmanuel Pietriga
 */

class GlyphLoader {

    
    private final SceneManager sceneManager;
    private final ConcurrentLinkedQueue<GlyphLoaderAction> tasks;
    private final ExecutorService loader;

    private enum LoadAction {LOAD, UNLOAD};

    static int FADE_IN_DURATION = 300; //milliseconds
    static int FADE_OUT_DURATION = 300; //milliseconds

    class GlyphLoaderAction {
        ObjectDescription od;
        PseudoView pv;
        LoadAction loadAction;
        GlyphLoaderAction(ObjectDescription od, PseudoView pv, LoadAction loadAction) {
            this.od = od; this.pv = pv; this.loadAction = loadAction;
        }
    }

    GlyphLoader(SceneManager sm){
        this.sceneManager = sm;
        loader = Executors.newSingleThreadExecutor(new LoaderThreadFactory());
        //tasks = new ConcurrentHashMap<ObjectDescription, LoadAction>();
        tasks = new ConcurrentLinkedQueue<GlyphLoaderAction>();
    }

    int getPendingRequestQueueSize(){
        return tasks.size();
    }

    /** Add a request to load an object in the queue.
     *@param od description of object to be loaded.
     *@param transition one of Region.{APPEAR,FADE_IN}
     */
    public void addLoadRequest(PseudoView pv, ObjectDescription od, boolean transition){
        //if(tasks.remove(od, LoadAction.UNLOAD)){
        //    return;
        //}
        //tasks.put(od, LoadAction.LOAD);
        for (GlyphLoaderAction gfa : tasks) {
            if (gfa.od == od && gfa.pv == pv){
                if (gfa.loadAction == LoadAction.UNLOAD){
                    tasks.remove(gfa);
                    return;
                }
                else{ // already in the queue
                    return;
                }
            }
        }
        tasks.add(new GlyphLoaderAction(od, pv, LoadAction.LOAD));

        final VirtualSpace target = pv.vs; //sceneManager.getSpaceByIndex(layerIndex);
        if(target == null){
            if (SceneManager.getDebugMode()){System.err.println("addLoadRequest: could not retrieve virtual space ");}
            return;
        }
        loader.submit(new Request(this.sceneManager, target, od, transition));
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
    public void addUnloadRequest(PseudoView pv, ObjectDescription od, boolean transition){
        //if(tasks.remove(od, LoadAction.LOAD)){
        //    return;
        //}
        //tasks.put(od, LoadAction.UNLOAD);
        for (GlyphLoaderAction gfa : tasks) {
            if (gfa.od == od && gfa.pv == pv){
                if(gfa.loadAction == LoadAction.LOAD){
                    tasks.remove(gfa);
                    return;
                }
                else{// already in the queue
                    return;
                }
            }
        }
        tasks.add(new GlyphLoaderAction(od, pv, LoadAction.UNLOAD));

        final VirtualSpace target = pv.vs;
        if(target == null){
            if (SceneManager.getDebugMode()){System.err.println("addUnLoadRequest: could not retrieve virtual space");}
            return;
        }
        loader.submit(new Request(this.sceneManager, target, od, transition));
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
            //GlyphLoaderAction action = tasks.remove(od);
            GlyphLoaderAction action = tasks.poll();
           if(action == null){
               return;
           } else if(action.loadAction.equals(LoadAction.LOAD)){
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

