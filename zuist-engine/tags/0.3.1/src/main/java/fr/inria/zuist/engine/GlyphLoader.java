/*   AUTHOR: Romain Primet (romain.primet@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
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

/**
 * Thread safety: GlyphLoader public methods should be invoked
 * from the same thread, normally the Swing EDT.
 *@author Romain Primet, Emmanuel Pietriga
 */
 
class GlyphLoader {

    private final SceneManager sceneManager;
    private final ConcurrentHashMap<ObjectDescription, LoadAction> tasks;
    private final ExecutorService loader;

    private enum LoadAction {LOAD, UNLOAD};

    static int FADE_IN_DURATION = 300; //milliseconds
    static int FADE_OUT_DURATION = 300; //milliseconds

    GlyphLoader(SceneManager sm){
        this.sceneManager = sm;
        loader = Executors.newSingleThreadExecutor(new LoaderThreadFactory());
        tasks = new ConcurrentHashMap<ObjectDescription, LoadAction>();
    }

    //layerIndex maps to a VirtualSpace
    public void addLoadRequest(int layerIndex, ObjectDescription od, boolean transition){
        if(tasks.remove(od, LoadAction.UNLOAD)){
            return;
        }
        tasks.put(od, LoadAction.LOAD);

        final VirtualSpace target = sceneManager.getSpaceByIndex(layerIndex);
        if(target == null){
            System.err.println("addLoadRequest: could not retrieve virtual space");
            return;
        }
        loader.submit(new Request(target, od, transition));	
    }

    //layerIndex maps to a VirtualSpace
    public void addUnloadRequest(int layerIndex, ObjectDescription od, boolean transition){
        if(tasks.remove(od, LoadAction.LOAD)){
            return;
        }

        tasks.put(od, LoadAction.UNLOAD);

        final VirtualSpace target = sceneManager.getSpaceByIndex(layerIndex);
        if(target == null){
            System.err.println("addLoadRequest: could not retrieve virtual space");
            return;
        }
        loader.submit(new Request(target, od, transition));
    }

    private class Request implements Runnable {
        final VirtualSpace target;
        final ObjectDescription od;
        final boolean transition;

        Request(VirtualSpace target, ObjectDescription od, boolean transition){
            this.target = target;
            this.od = od;
            this.transition = transition;
        }

        public void run(){
            LoadAction action = tasks.remove(od);
           if(action == null){
               return;
           } else if(action.equals(LoadAction.LOAD)){
               od.createObject(target, transition);
           } else {
               od.destroyObject(target, transition);
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

