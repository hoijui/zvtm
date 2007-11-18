/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zslideshow;

import java.util.Hashtable;
import java.util.Vector;

import java.io.File;
import java.io.FilenameFilter;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.engine.PostAnimationAction;

/** Thread dedicated to the processing of load/unload requests.
 *@author Emmanuel Pietriga
 */

class GlyphLoader implements Runnable {

    static final float[] FADE_OUT_ANIM_DATA = {0, 0, 0, 0, 0, 0, -1.0f};
    static final float[] FADE_IN_ANIM_DATA = {0, 0, 0, 0, 0, 0, 1.0f};
    static int FADE_IN_DURATION = 300;
    static int FADE_OUT_DURATION = 300;

    int NUMBER_OF_REQUESTS_PER_CYCLE = 5;

    /* thread sleeping time */
    static final int SLEEP_TIME = 5;

    static boolean DEBUG = true;

    Thread runView;
    boolean enabled = false;

    /* load/unload requests accessible by ID */
    Hashtable id2request;
    /* pending requests ordered by creation date */
    Vector requestQueue;
    /* give a unique identifier to each request (serves as key in id2request) */
    int nextRequestID = -1;
    
    ZSlideShow application;
    
    /* List of picture files in the current directory. */
    ImageDescription[] images = new ImageDescription[0];
    int currentIndex = -1;
    
    GlyphLoader(ZSlideShow app){
        this.application = app;
        id2request = new Hashtable();
        requestQueue = new Vector();
        start();
    }

    public void start(){
        runView = new Thread(this);
        runView.setPriority(Thread.NORM_PRIORITY);
        runView.start();
    }

    public void stop(){
        runView = null;
        notify();
    }

    public void run(){
        Thread me = Thread.currentThread();
        int processedRequestCount = 0;
        while (runView == me){
            if (enabled){
                processRequests();
                processedRequestCount++;
                if (processedRequestCount >= NUMBER_OF_REQUESTS_PER_CYCLE || requestQueue.isEmpty()){
                    processedRequestCount = 0;
                    goToSleep();
                }
            }
            else {
                goToSleep();
            }
        }
    }

    void goToSleep(){
        try {
            runView.sleep(SLEEP_TIME);
        }
        catch(InterruptedException ex){ex.printStackTrace();}
    }

    void setNumberOfRequestsHandledPerCycle(int i){
        NUMBER_OF_REQUESTS_PER_CYCLE = i;
    }

    void setEnabled(boolean b){
        enabled = b;
    }

    /*called by thread on a regular basis ; pops request from queue in a FIFO manner*/
    void processRequests(){
        synchronized(requestQueue){
            if (requestQueue.size() > 0){
                // (rid is actually an Integer)
                Object rid = requestQueue.firstElement();
                requestQueue.removeElementAt(0);
                Request r = (Request)id2request.get(rid);
                if (r != null){
                    // r might be null if the request got canceled
                    id2request.remove(rid);
                    processRequest(r);
                }
            }
        }
    }
    
    /*process a specific request*/
    void processRequest(Request r){
        switch (r.type){
            case Request.TYPE_LOAD:{
                loadObject(r.od, r.transition, r.showImmediately);
                break;
            }
            case Request.TYPE_UNLOAD:{
                unloadObject(r.od, r.transition);
                break;
            }
        }
    }

    /* request ID generator */
    Integer incRequestID(){
        nextRequestID += 1;
        return new Integer(nextRequestID);
    }

    /* create a request for loading an object */
    void addLoadRequest(int i, boolean transition, boolean si){
        ImageDescription od = images[i];
        if (DEBUG){
            System.out.println("Considering adding a load request for " + od.toString());
        }
        synchronized(requestQueue){
            synchronized(od){
                if (od.unloadRequest != null){
                    cancelRequest(od.unloadRequest);
                    od.unloadRequest = null;
                    return;
                }
                if (od.loadRequest != null){return;}
                if (DEBUG){
                    System.out.println("Adding load request for " + od.toString());
                }
                Integer requestID = incRequestID();
                Request r = new Request(requestID, Request.TYPE_LOAD, od, transition, si);
                od.loadRequest = requestID;
                id2request.put(requestID, r);
                requestQueue.addElement(requestID);
            }
        }
    }

    /*create a request for unloading a map*/
    void addUnloadRequest(int i, boolean transition){
        ImageDescription od = images[i];
        if (DEBUG){
            System.out.println("Considering adding an unload request for " + od.toString());
        }
        synchronized(requestQueue){
            synchronized(od){
                if (od.loadRequest != null){
                    cancelRequest(od.loadRequest);
                    od.loadRequest = null;
                    return;
                }
                if (od.unloadRequest != null){return;}
                if (DEBUG){
                    System.out.println("Adding unload request for " + od.toString());
                }
                Integer requestID = incRequestID();
                Request r = new Request(requestID, Request.TYPE_UNLOAD, od, transition, false);
                od.unloadRequest = requestID;
                id2request.put(requestID, r);
                requestQueue.addElement(requestID);
            }
        }
    }

    /*cancel a pending request*/
    void cancelRequest(Integer requestID){
        if (DEBUG){
            System.out.println("Canceling request to " + id2request.get(requestID).toString());
        }
        id2request.remove(requestID);
    }

    void loadObject(ImageDescription od, short transition, boolean showImmediately){
        if (DEBUG){
            System.out.println("Actually loading "+od);
        }
        od.loadObject(application.mSpace, application.vsm, transition==Request.TRANSITION_FADE, showImmediately);
    }


    void unloadObject(ImageDescription od, short transition){
        if (DEBUG){
            System.out.println("Actually unloading "+od);
        }
        od.unloadObject(application.mSpace, application.vsm, transition==Request.TRANSITION_FADE);
    }

    void openDirectory(File dir){
         if (currentIndex != -1){
             reset();
         }
         File[] files = dir.listFiles(new ImageFileFilter());
         images = new ImageDescription[files.length];
         for (int i=0;i<images.length;i++){
             images[i] = new ImageDescription(files[i], i);
         }
         if (images.length > 0){
             displayPicture(0);
         }
         else {
             //XXX: SIGNAL NO FILE THAT CAN BE DISPLAYED IN SLIDESHOW
             System.out.println("Directory " + dir.getAbsolutePath() + " does not contain any file that can be displayed");

         }
     }
     
     void reset(){
         //XXX:TBW have to write a special request type that erases everything
     }

     void displayPicture(int i){
         currentIndex = i;
         addLoadRequest(i, false, true);
         if (currentIndex > 0){
             addLoadRequest(i-1, false, false);
             if (currentIndex > 1){
                 addUnloadRequest(i-2, false);
             }
         }
         if (currentIndex < images.length-1){
             addLoadRequest(i+1, false, false);
             if (currentIndex < images.length-2){
                 addUnloadRequest(i+2, false);
             }
         }
     }

     void displayPreviousPicture(){
         if (currentIndex > 0){
             displayPicture(currentIndex-1);
         }
     }

     void displayNextPicture(){
         if (currentIndex < images.length-1){
             displayPicture(currentIndex+1);
         }
     }

}

class ImageFileFilter implements FilenameFilter {
    
    static final String[] EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif"};
    
    ImageFileFilter(){}
    
    public boolean accept(File dir, String name){
        for (int i=0;i<EXTENSIONS.length;i++){
            if (name.endsWith(EXTENSIONS[i])){
                return true;
            }
        }
        return false;
    }
    
}
