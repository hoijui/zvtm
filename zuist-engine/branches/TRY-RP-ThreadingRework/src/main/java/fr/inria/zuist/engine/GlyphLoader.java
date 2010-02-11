/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.util.Hashtable;
import java.util.LinkedList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.animation.EndAction;

/** Thread dedicated to the processing of load/unload requests.
 *@author Emmanuel Pietriga
 */

class GlyphLoader implements Runnable {

    static int FADE_IN_DURATION = 300;
    static int FADE_OUT_DURATION = 300;

    private int NUMBER_OF_REQUESTS_PER_CYCLE = 5;

    /* thread sleeping time */
    private static final int SLEEP_TIME = 5;

    static boolean DEBUG = false;

    private Thread runView;
    private boolean enabled = false;

    private SceneManager sm;

    /* load/unload requests accessible by ID */
    private Hashtable id2request;
    /* pending requests ordered by creation date */
    LinkedList<Integer> requestQueue;
    /* give a unique identifier to each request (serves as key in id2request) */
    private int nextRequestID = -1;

    GlyphLoader(SceneManager sm){
	this.sm = sm;
	id2request = new Hashtable();
	requestQueue = new LinkedList<Integer>();
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
	    if (!requestQueue.isEmpty()){
		    Integer rid = requestQueue.remove();
		    Request r = (Request)id2request.get(rid);
		    if (r != null){// r might be null if the request got canceled
			    id2request.remove(rid);
			    processRequest(r);
		    }
	    }
    }
    
    /*process a specific request*/
    void processRequest(Request r){
	switch (r.type){
	case Request.TYPE_LOAD:{
  	    showObject(r.od, r.transition);
	    break;
	}
	case Request.TYPE_UNLOAD:{
  	    hideObject(r.od, r.transition);
	    break;
	}
	}
    }

    /* request ID generator */
    private Integer incRequestID(){
	nextRequestID += 1;
	return new Integer(nextRequestID);
    }

    /* create a request for loading an object */
    void addLoadRequest(ObjectDescription od, boolean transition){
	    if (DEBUG){
		    System.out.println("Considering adding a load request for " + od.toString());
	    }
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
	    Request r = new Request(requestID, Request.TYPE_LOAD, od, transition);
	    od.loadRequest = requestID;
	    id2request.put(requestID, r);
	    requestQueue.offer(requestID);
    }

    /*create a request for unloading a map*/
    void addUnloadRequest(ObjectDescription od, boolean transition){
	    if (DEBUG){
		    System.out.println("Considering adding an unload request for " + od.toString());
	    }
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
	    Request r = new Request(requestID, Request.TYPE_UNLOAD, od, transition);
	    od.unloadRequest = requestID;
	    id2request.put(requestID, r);
	    requestQueue.offer(requestID);
    }

    /*cancel a pending request*/
    private void cancelRequest(Integer requestID){
	if (DEBUG){
	    System.out.println("Canceling request to " + id2request.get(requestID).toString());
	}
	id2request.remove(requestID);
    }


    void showObject(ObjectDescription od, short transition){
        if (DEBUG){
            System.out.println("Actually loading "+od);
        }
        od.createObject(sm.sceneLayers[od.getParentRegion().getLayerIndex()], transition==Request.TRANSITION_FADE);
    }


    void hideObject(ObjectDescription od, short transition){
        if (DEBUG){
            System.out.println("Actually unloading "+od);
        }
        od.destroyObject(sm.sceneLayers[od.getParentRegion().getLayerIndex()], transition==Request.TRANSITION_FADE);
    }


}
