/*   FILE: MapManager.java
 *   DATE OF CREATION:  Wed Jul 12 15:01:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.zvtm.demo;

import java.awt.Image;
import javax.swing.ImageIcon;

import java.util.Hashtable;
import java.util.Vector;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.VImage;
import net.claribole.zvtm.engine.Utils;

public class MapManager implements Runnable {

    /*get info about map loading/unloading actions*/
    static final boolean DEBUG = true;

    /*conversion between latitude/longitude coordinates expressed with the
      decimal notation and virtual space coordinates (for a world map that is
      64000 pixel wide and 32000 pixel high)*/
    static final double COORDS_CONV = 18.0/3200.0;

    /*various altitudes that trigger changes w.r.t levels of detail*/
    static final short LEVEL_0 = 0;
    static final short LEVEL_1 = 1;
    static final short LEVEL_2 = 2;
    static final short LEVEL_3 = 3;
    
    static final String LEVEL_1_NAME = "Level 1";
    static final String LEVEL_2_NAME = "Level 2";
    static final String LEVEL_3_NAME = "Level 3";

    static final float LEVEL_3_ALT = 400;
    static final float LEVEL_2_ALT = 800;
    static final float LEVEL_1_ALT = 1600;

    /*map manager thread sleeping time*/
    static final int SLEEP_TIME = 50;

    /*static map information access methods*/
    static String getMapPath(String mapID){
	Vector v = (Vector)MapData.mapInfo.get(mapID);
	return (String)v.elementAt(0);
    }

    static LongPoint getMapCenter(String mapID){
	Vector v = (Vector)MapData.mapInfo.get(mapID);
	return (LongPoint)v.elementAt(1);	
    }

    static double getMapScaleFactor(String mapID){
	Vector v = (Vector)MapData.mapInfo.get(mapID);
	return ((Double)v.elementAt(2)).doubleValue();
    }

//     MapApplication application;
    Thread runView;

    boolean adaptMaps = true;

    /* main 8000x4000 map (always displayed) */
    VImage mainMap;
    /* level of detail (0,1,2,3) depending on observation altitude */
    short lod = LEVEL_0; // Level of Details

    /*maps at various levels and their status (loaded, being loaded, being unloaded)*/

    /* VImage objects containing M1N00 maps */
    VImage[] M1N00im = new VImage[8];
    /* status of load request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1N00lrqs = new boolean[8];
    /* status of unload request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1N00urqs = new boolean[8];
    /* load requests for M1N00 maps */
    Request[] M1N00lrq = new Request[8];
    /* unload requests for M1N00 maps */
    Request[] M1N00urq = new Request[8];

    /* VImage objects containing M1NN0 maps */
    VImage[] M1NN0im = new VImage[32];
    /* status of load request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1NN0lrqs = new boolean[32];
    /* status of unload request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1NN0urqs = new boolean[32];
    /* load requests for M1NN0 maps */
    Request[] M1NN0lrq = new Request[32];
    /* unload requests for M1NN0 maps */
    Request[] M1NN0urq = new Request[32];

    /* VImage objects containing M1NNN maps */
    VImage[] M1NNNim = new VImage[128];
    /* status of load request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1NNNlrqs = new boolean[128];
    /* status of unload request M1N00 (being processed right now=true, not yet processed=false)*/
    boolean[] M1NNNurqs = new boolean[128];
    /* load requests for M1NNN maps */
    Request[] M1NNNlrq = new Request[128];
    /* unload requests for M1NNN maps */
    Request[] M1NNNurq = new Request[128];

    /* load/unload requests accessible by ID */
    Hashtable id2request;
    /* pending requests ordered by creation date */
    Vector requestQueue;
    /* give a unique identifier to each request (serves as key in id2request) */
    int nextRequestID = -1;

    /*map manager constructor*/
    MapManager(VirtualSpaceManager vsm, VirtualSpace vs, Camera c, View v){
// 	this.application = app;
	this.vsm = vsm;
	this.vs = vs;
	this.camera = c;
	this.view = v;
	for (int i=0;i<M1N00lrqs.length;i++){
	    M1N00lrqs[i] = false;
	    M1N00urqs[i] = false;
	}
	for (int i=0;i<M1NN0lrqs.length;i++){
	    M1NN0lrqs[i] = false;
	    M1NN0urqs[i] = false;
	}
	for (int i=0;i<M1NNNlrqs.length;i++){
	    M1NNNlrqs[i] = false;
	    M1NNNurqs[i] = false;
	}
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

//     MapMonitor mapMonitor;
    VirtualSpace vs;
    VirtualSpaceManager vsm;
    Camera camera;
    View view;

//     void setMapMonitor(MapMonitor mm){
// 	mapMonitor = mm;
//     }

    public void run(){
	Thread me = Thread.currentThread();
	while (runView == me){
// 	    if  (mapMonitor != null){mapMonitor.updateMaps();}
	    processRequests();
	    try {
		runView.sleep(SLEEP_TIME);
	    }
	    catch(InterruptedException ex){ex.printStackTrace();}
	}
    }

    void switchAdaptMaps(){
	adaptMaps = !adaptMaps;
	System.out.println("Adapt maps: "+((adaptMaps) ? "On\n" : "Off\n"));
    }

    /*called by thread on a regular basis ; pops request from queue in a FIFO manner*/
    void processRequests(){
	if (requestQueue.size() > 0){
	    // (rid is actually an Integer)
	    Object rid = requestQueue.firstElement();
	    requestQueue.removeElementAt(0);
	    Request r = (Request)id2request.get(rid);
	    if (r != null){
		// r might be null if the request got canceled
		r.requestsStatusAtLevel[r.mapIndex] = true;
		id2request.remove(rid);
		processRequest(r);
// 		if  (mapMonitor != null){mapMonitor.updateMaps();}
	    }
	}
    }
    
    /*process a specific request*/
    void processRequest(Request r){
	switch (r.type){
	case Request.TYPE_LOAD:{
	    loadMap(r.mapID, r.mapIndex, r.imagesAtLevel, r.requestsStatusAtLevel, r.requestsAtLevel);
	    break;
	}
	case Request.TYPE_UNLOAD:{
	    unloadMap(r.mapID, r.mapIndex, r.imagesAtLevel, r.requestsStatusAtLevel, r.requestsAtLevel);
	    break;
	}
	}
    }

    /*cancel a pending request*/
    void cancelRequest(Integer requestID, Request[] requests, int mapIndex){
// 	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){
// 	    application.writeOnConsole("Canceling request "+requestID+" for map "+requests[mapIndex].mapID+"\n",
// 				       Console.GRAY_STYLE);
// 	}
	requests[mapIndex] = null;
	id2request.remove(requestID);
    }

    /* remove all detailed maps, leaving only the main map */
    void resetMap(){
	resetLevel(M1N00im, M1N00lrq, M1N00lrqs, M1N00urq, M1N00urqs, LEVEL_1_NAME);
	resetLevel(M1NN0im, M1NN0lrq, M1NN0lrqs, M1NN0urq, M1NN0urqs, LEVEL_2_NAME);
	resetLevel(M1NNNim, M1NNNlrq, M1NNNlrqs, M1NNNurq, M1NNNurqs, LEVEL_3_NAME);
    }

    /* remove detailed maps at a given level */
    void resetLevel(final VImage[] maps, final Request[] lrequests, final boolean[] lrequestsStatus,
		    final Request[] urequests, final boolean[] urequestsStatus, String levelName){
	for (int i=0;i<maps.length;i++){
	    if (maps[i] != null){
		if (urequests[i] == null){
		    // if map is loaded and there is no pending unload request, unload it
		    addUnloadRequest(i, levelName, maps, urequestsStatus, urequests);
		}
		// else do nothing, this will be taken care of by the existing unload request
	    }
	    /* in theory both the above and below branches are mutually exclusive
	       since a load request cannot be created if the map is already loaded */
	    if (lrequests[i] != null){
		if (lrequestsStatus[i]){
		    // a load request is currently being processed for this map
		    // we cannot cancel it, so we add an unload request
		    addUnloadRequest(i, levelName, maps, urequestsStatus, urequests);
		}
		else {
		    // if a load request is pending, cancel it
		    cancelRequest(lrequests[i].ID, lrequests, i);
		}
	    }
	}
    }

    /*manual call to garbage collector*/
    void gc(){
// 	if (ZLWorldTask.SHOW_CONSOLE && DEBUG){
// 	    application.writeOnConsole("gc...", Console.GRAY_STYLE);
// 	    System.gc();
// 	    application.writeOnConsole("done\n", Console.GRAY_STYLE);
// 	}
// 	else {
	    System.gc();
// 	}
    }

    /*request ID generator*/
    Integer incRequestID(){
	nextRequestID += 1;
	return new Integer(nextRequestID);
    }

    /*create a request for loading a map*/
    void addLoadRequest(int mapIndex, String mapID, VImage[] maps,
			boolean[] lrequestsStatus, Request[] lrequests){
	Integer requestID = incRequestID();
	lrequests[mapIndex] = new Request(requestID, Request.TYPE_LOAD, mapIndex,
					  mapID, maps, lrequestsStatus, lrequests);
	id2request.put(requestID, lrequests[mapIndex]);
	requestQueue.addElement(requestID);
    }

    /*create a request for unloading a map*/
    void addUnloadRequest(int mapIndex, String mapID, VImage[] maps,
			  boolean[] urequestsStatus, Request[] urequests){
	Integer requestID = incRequestID();
	urequests[mapIndex] = new Request(requestID, Request.TYPE_UNLOAD, mapIndex,
					  mapID, maps, urequestsStatus, urequests);
	id2request.put(requestID, urequests[mapIndex]);
	requestQueue.addElement(requestID);
    }

    /* load main map (called once at init time) */
    void initMap(String mainMapPath){
// 	if (ZLWorldTask.SHOW_CONSOLE){application.writeOnConsole(ZLWorldTask.LOADING_WORLDMAP_TEXT);}
	mainMap = new VImage(MapData.M1000x, MapData.M1000y, 0,
 			     (new ImageIcon((mainMapPath != null) ? mainMapPath : MapData.M1000path)).getImage(),
			     MapData.MN000factor.doubleValue());
	mainMap.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(mainMap, vs);
	vs.atBottom(mainMap);
// 	if (ZLWorldTask.SHOW_CONSOLE){application.writeOnConsole("done\n");}
    }

    /* display map with id mapID, located at index mapIndex in the corresponding array of maps */
    void loadMap(String mapID, int mapIndex, VImage[] maps, boolean[] lrequestsStatus, Request[] lrequests){
	if (maps[mapIndex] == null){
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("loading map "+mapID+"\n");}
	    LongPoint loc = getMapCenter(mapID);
	    maps[mapIndex] = new VImage(loc.x, loc.y, 0, (new ImageIcon(getMapPath(mapID))).getImage(), getMapScaleFactor(mapID));
	    maps[mapIndex].setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	    vsm.addGlyph(maps[mapIndex], vs);
	    vs.atBottom(maps[mapIndex]);
	    vs.atBottom(mainMap);
	}
	lrequestsStatus[mapIndex] = false;
	lrequests[mapIndex] = null;
    }

    /* remove map at index mapIndex in array of maps at a given level*/
    void unloadMap(String mapID, int mapIndex, VImage[] maps, boolean[] urequestsStatus, Request[] urequests){
	if (maps[mapIndex] != null){
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("unloading map "+mapID+"\n");}
	    vs.destroyGlyph(maps[mapIndex]);
	    maps[mapIndex].getImage().flush();
	    maps[mapIndex] = null;
	}
	urequestsStatus[mapIndex] = false;
	urequests[mapIndex] = null;
    }

    /* the following 3 methods are called only if the map is not yet loaded */

    void showMapL1(final String mapID, final int mapIndex){
	if (M1N00lrq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("load request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addLoadRequest(mapIndex, mapID, M1N00im, M1N00lrqs, M1N00lrq);
	}
    }
    
    void showMapL2(final String mapID, final int mapIndex){
	if (M1NN0lrq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("load request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addLoadRequest(mapIndex, mapID, M1NN0im, M1NN0lrqs, M1NN0lrq);
	}
    }

    void showMapL3(final String mapID, final int mapIndex){
	if (M1NNNlrq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("load request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addLoadRequest(mapIndex, mapID, M1NNNim, M1NNNlrqs, M1NNNlrq);
	}
    }

    /* the following 3 methods are called only if the map is loaded */

    void hideMapL1(final String mapID, final int mapIndex){
	if (M1N00urq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("unload request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addUnloadRequest(mapIndex, mapID, M1N00im, M1N00urqs, M1N00urq);
	}
    }

    void hideMapL2(final String mapID, final int mapIndex){
	if (M1NN0urq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("unload request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addUnloadRequest(mapIndex, mapID, M1NN0im, M1NN0urqs, M1NN0urq);
	}
    }

    void hideMapL3(final String mapID, final int mapIndex){
	if (M1NNNurq[mapIndex] == null){
	    // first check that there is no pending request for the same action
// 	    if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("unload request for "+mapID+"\n", Console.GRAY_STYLE);}
	    addUnloadRequest(mapIndex, mapID, M1NNNim, M1NNNurqs, M1NNNurq);
	}
    }

    /* change level of details (depends on camera altitude) */
    void updateMapLevel(float altitude){
	// map level
	if (altitude < LEVEL_3_ALT){
	    updateMapLevel(LEVEL_3);
	}
	else if (altitude < LEVEL_2_ALT){
	    updateMapLevel(LEVEL_2);
	}
	else if (altitude < LEVEL_1_ALT){
	    updateMapLevel(LEVEL_1);
	}
	else {
	    updateMapLevel(LEVEL_0);
	}
    }

    /* returns the map level of detail corresponding to the provided camera altitude */
    short getMapLevel(float altitude){
	if (altitude < LEVEL_3_ALT){
	    return LEVEL_3;
	}
	else if (altitude < LEVEL_2_ALT){
	    return LEVEL_2;
	}
	else if (altitude < LEVEL_1_ALT){
	    return LEVEL_1;
	}
	else {
	    return LEVEL_0;
	}
    }

    /* change level of details (depends on camera altitude) */
    void updateMapLevel(short level){
	/* do not update maps if user disabled updates explicitly */
	/* update maps only if level-of-detail has changed*/
	if (adaptMaps && level != lod){
	    /* Remember old level-of-detail value as we need to know
	       what level to reset. We first process load requests,
	       and then unload requests in order to avoid glitches
	       when switching from one level to the next. By glitch
	       I mean temporarily seing level 0 during the transition
	       e.g. from level 2 to level 3*/
	    short oldlod = lod;
	    if (level == LEVEL_3){
//  		if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("switch to level 3\n", Console.GRAY_STYLE);}
		lod = LEVEL_3;
		updateVisibleMaps(view.getVisibleRegion(camera),
				  false, (short)0);
		if (oldlod == LEVEL_2){
		    resetLevel(M1NN0im, M1NN0lrq, M1NN0lrqs, M1NN0urq, M1NN0urqs, LEVEL_2_NAME);
		}
		else if (oldlod == LEVEL_1){
		    resetLevel(M1N00im, M1N00lrq, M1N00lrqs, M1N00urq, M1N00urqs, LEVEL_1_NAME);
		}
	    }
	    else if (level == LEVEL_2){
//  		if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("switch to level 2\n", Console.GRAY_STYLE);}
		lod = LEVEL_2;
		updateVisibleMaps(view.getVisibleRegion(camera),
				  false, (short)0);
		if (oldlod == LEVEL_3){
		    resetLevel(M1NNNim, M1NNNlrq, M1NNNlrqs, M1NNNurq, M1NNNurqs, LEVEL_3_NAME);
		}
		else if (oldlod == LEVEL_1){
		    resetLevel(M1N00im, M1N00lrq, M1N00lrqs, M1N00urq, M1N00urqs, LEVEL_1_NAME);
		}
	    }
	    else if (level == LEVEL_1){
//  		if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("switch to level 1\n", Console.GRAY_STYLE);}
		lod = LEVEL_1;
		updateVisibleMaps(view.getVisibleRegion(camera),
				  false, (short)0);
		if (oldlod == LEVEL_3){
		    resetLevel(M1NNNim, M1NNNlrq, M1NNNlrqs, M1NNNurq, M1NNNurqs, LEVEL_3_NAME);
		}
		else if (oldlod == LEVEL_2){
		    resetLevel(M1NN0im, M1NN0lrq, M1NN0lrqs, M1NN0urq, M1NN0urqs, LEVEL_2_NAME);
		}
	    }
	    else {// LEVEL_0
//  		if (ZLWorldTask.SHOW_CONSOLE && DEBUG){application.writeOnConsole("switch to level 0\n", Console.GRAY_STYLE);}
		lod = LEVEL_0;
		updateVisibleMaps(view.getVisibleRegion(camera),
				  false, (short)0);
		if (oldlod == LEVEL_3){
		    resetLevel(M1NNNim, M1NNNlrq, M1NNNlrqs, M1NNNurq, M1NNNurqs, LEVEL_3_NAME);
		}
		else if (oldlod == LEVEL_2){
		    resetLevel(M1NN0im, M1NN0lrq, M1NN0lrqs, M1NN0urq, M1NN0urqs, LEVEL_2_NAME);
		}
		else if (oldlod == LEVEL_1){
		    resetLevel(M1N00im, M1N00lrq, M1N00lrqs, M1N00urq, M1N00urqs, LEVEL_1_NAME);
		}
	    }
	}
    }

    /* returns true if region delimited by wnes intersects a given map*/
    boolean intersectsRegion(long[] wnes, long[] mapRegion){
	return (wnes[0] < mapRegion[2] && wnes[2] > mapRegion[0]
		&& wnes[3] < mapRegion[1] && wnes[1] > mapRegion[3]);
    }


    /*--------- THE REMAINDER OF THIS FILE IS GENERATED AUTOMATICALLY -----------*/
    /*             by a Python script: updVisMapsMethodGeneator.py               */
    
    /* called each time the camera moves - detects what regions are visible depending 
       on the level of details and calls methods to load/unload maps accordingly */
    void updateVisibleMaps(long[] wnes, boolean onlyIfSameLevel, short actualLevel){
	       if ((onlyIfSameLevel && (actualLevel != lod)) || !adaptMaps){
	           /* do not update visible maps if the actual level is different from the
	              level the app believes it is at (happens when a lens is active, as
	              updateMapLevel call are temporarily freezed)*/
	           /*do not update them if user disabled updates explicitly either*/
	           return;
	       }
        if (lod == LEVEL_3){// if dealing with maps at level of detail = 1
            updateVisibleMapsL3(wnes);
        }
        else if (lod == LEVEL_2){// if dealing with maps at level of detail = 1
            updateVisibleMapsL2(wnes);
        }
        else if (lod == LEVEL_1){// if dealing with maps at level of detail = 1
            updateVisibleMapsL1(wnes);
        }
        /* else if lod == LEVEL_0 there is nothing to do as this is the main map
           (always visible);level 1 removal delt with through updateMapLevel */
    }

    void updateVisibleMapsL1(long[] wnes){
        // Level 1 - M1100
        if (M1N00im[0] == null){
            if (intersectsRegion(wnes, MapData.M1100region)){showMapL1(MapData.M1100, 0);}
            else if (M1N00lrq[0] != null && !M1N00lrqs[0]){cancelRequest(M1N00lrq[0].ID, M1N00lrq, 0);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1100region)){hideMapL1(MapData.M1100, 0);}
        }
        // Level 1 - M1200
        if (M1N00im[1] == null){
            if (intersectsRegion(wnes, MapData.M1200region)){showMapL1(MapData.M1200, 1);}
            else if (M1N00lrq[1] != null && !M1N00lrqs[1]){cancelRequest(M1N00lrq[1].ID, M1N00lrq, 1);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1200region)){hideMapL1(MapData.M1200, 1);}
        }
        // Level 1 - M1300
        if (M1N00im[2] == null){
            if (intersectsRegion(wnes, MapData.M1300region)){showMapL1(MapData.M1300, 2);}
            else if (M1N00lrq[2] != null && !M1N00lrqs[2]){cancelRequest(M1N00lrq[2].ID, M1N00lrq, 2);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1300region)){hideMapL1(MapData.M1300, 2);}
        }
        // Level 1 - M1400
        if (M1N00im[3] == null){
            if (intersectsRegion(wnes, MapData.M1400region)){showMapL1(MapData.M1400, 3);}
            else if (M1N00lrq[3] != null && !M1N00lrqs[3]){cancelRequest(M1N00lrq[3].ID, M1N00lrq, 3);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1400region)){hideMapL1(MapData.M1400, 3);}
        }
        // Level 1 - M1500
        if (M1N00im[4] == null){
            if (intersectsRegion(wnes, MapData.M1500region)){showMapL1(MapData.M1500, 4);}
            else if (M1N00lrq[4] != null && !M1N00lrqs[4]){cancelRequest(M1N00lrq[4].ID, M1N00lrq, 4);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1500region)){hideMapL1(MapData.M1500, 4);}
        }
        // Level 1 - M1600
        if (M1N00im[5] == null){
            if (intersectsRegion(wnes, MapData.M1600region)){showMapL1(MapData.M1600, 5);}
            else if (M1N00lrq[5] != null && !M1N00lrqs[5]){cancelRequest(M1N00lrq[5].ID, M1N00lrq, 5);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1600region)){hideMapL1(MapData.M1600, 5);}
        }
        // Level 1 - M1700
        if (M1N00im[6] == null){
            if (intersectsRegion(wnes, MapData.M1700region)){showMapL1(MapData.M1700, 6);}
            else if (M1N00lrq[6] != null && !M1N00lrqs[6]){cancelRequest(M1N00lrq[6].ID, M1N00lrq, 6);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1700region)){hideMapL1(MapData.M1700, 6);}
        }
        // Level 1 - M1800
        if (M1N00im[7] == null){
            if (intersectsRegion(wnes, MapData.M1800region)){showMapL1(MapData.M1800, 7);}
            else if (M1N00lrq[7] != null && !M1N00lrqs[7]){cancelRequest(M1N00lrq[7].ID, M1N00lrq, 7);}
        }
        else {
            if (!intersectsRegion(wnes, MapData.M1800region)){hideMapL1(MapData.M1800, 7);}
        }
    }

    void updateVisibleMapsL2(long[] wnes){
        // Level 2 - M11N0
        if (intersectsRegion(wnes, MapData.M1100region)){
            if (M1NN0im[0] == null){
                if (intersectsRegion(wnes, MapData.M1110region)){showMapL2(MapData.M1110, 0);}
                else if (M1NN0lrq[0] != null && !M1NN0lrqs[0]){cancelRequest(M1NN0lrq[0].ID, M1NN0lrq, 0);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1110region)){hideMapL2(MapData.M1110, 0);}
            }
            if (M1NN0im[1] == null){
                if (intersectsRegion(wnes, MapData.M1120region)){showMapL2(MapData.M1120, 1);}
                else if (M1NN0lrq[1] != null && !M1NN0lrqs[1]){cancelRequest(M1NN0lrq[1].ID, M1NN0lrq, 1);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1120region)){hideMapL2(MapData.M1120, 1);}
            }
            if (M1NN0im[2] == null){
                if (intersectsRegion(wnes, MapData.M1130region)){showMapL2(MapData.M1130, 2);}
                else if (M1NN0lrq[2] != null && !M1NN0lrqs[2]){cancelRequest(M1NN0lrq[2].ID, M1NN0lrq, 2);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1130region)){hideMapL2(MapData.M1130, 2);}
            }
            if (M1NN0im[3] == null){
                if (intersectsRegion(wnes, MapData.M1140region)){showMapL2(MapData.M1140, 3);}
                else if (M1NN0lrq[3] != null && !M1NN0lrqs[3]){cancelRequest(M1NN0lrq[3].ID, M1NN0lrq, 3);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1140region)){hideMapL2(MapData.M1140, 3);}
            }
        }
        else {
            if (M1NN0im[0] != null){hideMapL2(MapData.M1110, 0);}
            else if (M1NN0lrq[0] != null && !M1NN0lrqs[0]){cancelRequest(M1NN0lrq[0].ID, M1NN0lrq, 0);}
            if (M1NN0im[1] != null){hideMapL2(MapData.M1120, 1);}
            else if (M1NN0lrq[1] != null && !M1NN0lrqs[1]){cancelRequest(M1NN0lrq[1].ID, M1NN0lrq, 1);}
            if (M1NN0im[2] != null){hideMapL2(MapData.M1130, 2);}
            else if (M1NN0lrq[2] != null && !M1NN0lrqs[2]){cancelRequest(M1NN0lrq[2].ID, M1NN0lrq, 2);}
            if (M1NN0im[3] != null){hideMapL2(MapData.M1140, 3);}
            else if (M1NN0lrq[3] != null && !M1NN0lrqs[3]){cancelRequest(M1NN0lrq[3].ID, M1NN0lrq, 3);}
        }
        // Level 2 - M12N0
        if (intersectsRegion(wnes, MapData.M1200region)){
            if (M1NN0im[4] == null){
                if (intersectsRegion(wnes, MapData.M1210region)){showMapL2(MapData.M1210, 4);}
                else if (M1NN0lrq[4] != null && !M1NN0lrqs[4]){cancelRequest(M1NN0lrq[4].ID, M1NN0lrq, 4);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1210region)){hideMapL2(MapData.M1210, 4);}
            }
            if (M1NN0im[5] == null){
                if (intersectsRegion(wnes, MapData.M1220region)){showMapL2(MapData.M1220, 5);}
                else if (M1NN0lrq[5] != null && !M1NN0lrqs[5]){cancelRequest(M1NN0lrq[5].ID, M1NN0lrq, 5);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1220region)){hideMapL2(MapData.M1220, 5);}
            }
            if (M1NN0im[6] == null){
                if (intersectsRegion(wnes, MapData.M1230region)){showMapL2(MapData.M1230, 6);}
                else if (M1NN0lrq[6] != null && !M1NN0lrqs[6]){cancelRequest(M1NN0lrq[6].ID, M1NN0lrq, 6);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1230region)){hideMapL2(MapData.M1230, 6);}
            }
            if (M1NN0im[7] == null){
                if (intersectsRegion(wnes, MapData.M1240region)){showMapL2(MapData.M1240, 7);}
                else if (M1NN0lrq[7] != null && !M1NN0lrqs[7]){cancelRequest(M1NN0lrq[7].ID, M1NN0lrq, 7);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1240region)){hideMapL2(MapData.M1240, 7);}
            }
        }
        else {
            if (M1NN0im[4] != null){hideMapL2(MapData.M1210, 4);}
            else if (M1NN0lrq[4] != null && !M1NN0lrqs[4]){cancelRequest(M1NN0lrq[4].ID, M1NN0lrq, 4);}
            if (M1NN0im[5] != null){hideMapL2(MapData.M1220, 5);}
            else if (M1NN0lrq[5] != null && !M1NN0lrqs[5]){cancelRequest(M1NN0lrq[5].ID, M1NN0lrq, 5);}
            if (M1NN0im[6] != null){hideMapL2(MapData.M1230, 6);}
            else if (M1NN0lrq[6] != null && !M1NN0lrqs[6]){cancelRequest(M1NN0lrq[6].ID, M1NN0lrq, 6);}
            if (M1NN0im[7] != null){hideMapL2(MapData.M1240, 7);}
            else if (M1NN0lrq[7] != null && !M1NN0lrqs[7]){cancelRequest(M1NN0lrq[7].ID, M1NN0lrq, 7);}
        }
        // Level 2 - M13N0
        if (intersectsRegion(wnes, MapData.M1300region)){
            if (M1NN0im[8] == null){
                if (intersectsRegion(wnes, MapData.M1310region)){showMapL2(MapData.M1310, 8);}
                else if (M1NN0lrq[8] != null && !M1NN0lrqs[8]){cancelRequest(M1NN0lrq[8].ID, M1NN0lrq, 8);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1310region)){hideMapL2(MapData.M1310, 8);}
            }
            if (M1NN0im[9] == null){
                if (intersectsRegion(wnes, MapData.M1320region)){showMapL2(MapData.M1320, 9);}
                else if (M1NN0lrq[9] != null && !M1NN0lrqs[9]){cancelRequest(M1NN0lrq[9].ID, M1NN0lrq, 9);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1320region)){hideMapL2(MapData.M1320, 9);}
            }
            if (M1NN0im[10] == null){
                if (intersectsRegion(wnes, MapData.M1330region)){showMapL2(MapData.M1330, 10);}
                else if (M1NN0lrq[10] != null && !M1NN0lrqs[10]){cancelRequest(M1NN0lrq[10].ID, M1NN0lrq, 10);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1330region)){hideMapL2(MapData.M1330, 10);}
            }
            if (M1NN0im[11] == null){
                if (intersectsRegion(wnes, MapData.M1340region)){showMapL2(MapData.M1340, 11);}
                else if (M1NN0lrq[11] != null && !M1NN0lrqs[11]){cancelRequest(M1NN0lrq[11].ID, M1NN0lrq, 11);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1340region)){hideMapL2(MapData.M1340, 11);}
            }
        }
        else {
            if (M1NN0im[8] != null){hideMapL2(MapData.M1310, 8);}
            else if (M1NN0lrq[8] != null && !M1NN0lrqs[8]){cancelRequest(M1NN0lrq[8].ID, M1NN0lrq, 8);}
            if (M1NN0im[9] != null){hideMapL2(MapData.M1320, 9);}
            else if (M1NN0lrq[9] != null && !M1NN0lrqs[9]){cancelRequest(M1NN0lrq[9].ID, M1NN0lrq, 9);}
            if (M1NN0im[10] != null){hideMapL2(MapData.M1330, 10);}
            else if (M1NN0lrq[10] != null && !M1NN0lrqs[10]){cancelRequest(M1NN0lrq[10].ID, M1NN0lrq, 10);}
            if (M1NN0im[11] != null){hideMapL2(MapData.M1340, 11);}
            else if (M1NN0lrq[11] != null && !M1NN0lrqs[11]){cancelRequest(M1NN0lrq[11].ID, M1NN0lrq, 11);}
        }
        // Level 2 - M14N0
        if (intersectsRegion(wnes, MapData.M1400region)){
            if (M1NN0im[12] == null){
                if (intersectsRegion(wnes, MapData.M1410region)){showMapL2(MapData.M1410, 12);}
                else if (M1NN0lrq[12] != null && !M1NN0lrqs[12]){cancelRequest(M1NN0lrq[12].ID, M1NN0lrq, 12);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1410region)){hideMapL2(MapData.M1410, 12);}
            }
            if (M1NN0im[13] == null){
                if (intersectsRegion(wnes, MapData.M1420region)){showMapL2(MapData.M1420, 13);}
                else if (M1NN0lrq[13] != null && !M1NN0lrqs[13]){cancelRequest(M1NN0lrq[13].ID, M1NN0lrq, 13);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1420region)){hideMapL2(MapData.M1420, 13);}
            }
            if (M1NN0im[14] == null){
                if (intersectsRegion(wnes, MapData.M1430region)){showMapL2(MapData.M1430, 14);}
                else if (M1NN0lrq[14] != null && !M1NN0lrqs[14]){cancelRequest(M1NN0lrq[14].ID, M1NN0lrq, 14);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1430region)){hideMapL2(MapData.M1430, 14);}
            }
            if (M1NN0im[15] == null){
                if (intersectsRegion(wnes, MapData.M1440region)){showMapL2(MapData.M1440, 15);}
                else if (M1NN0lrq[15] != null && !M1NN0lrqs[15]){cancelRequest(M1NN0lrq[15].ID, M1NN0lrq, 15);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1440region)){hideMapL2(MapData.M1440, 15);}
            }
        }
        else {
            if (M1NN0im[12] != null){hideMapL2(MapData.M1410, 12);}
            else if (M1NN0lrq[12] != null && !M1NN0lrqs[12]){cancelRequest(M1NN0lrq[12].ID, M1NN0lrq, 12);}
            if (M1NN0im[13] != null){hideMapL2(MapData.M1420, 13);}
            else if (M1NN0lrq[13] != null && !M1NN0lrqs[13]){cancelRequest(M1NN0lrq[13].ID, M1NN0lrq, 13);}
            if (M1NN0im[14] != null){hideMapL2(MapData.M1430, 14);}
            else if (M1NN0lrq[14] != null && !M1NN0lrqs[14]){cancelRequest(M1NN0lrq[14].ID, M1NN0lrq, 14);}
            if (M1NN0im[15] != null){hideMapL2(MapData.M1440, 15);}
            else if (M1NN0lrq[15] != null && !M1NN0lrqs[15]){cancelRequest(M1NN0lrq[15].ID, M1NN0lrq, 15);}
        }
        // Level 2 - M15N0
        if (intersectsRegion(wnes, MapData.M1500region)){
            if (M1NN0im[16] == null){
                if (intersectsRegion(wnes, MapData.M1510region)){showMapL2(MapData.M1510, 16);}
                else if (M1NN0lrq[16] != null && !M1NN0lrqs[16]){cancelRequest(M1NN0lrq[16].ID, M1NN0lrq, 16);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1510region)){hideMapL2(MapData.M1510, 16);}
            }
            if (M1NN0im[17] == null){
                if (intersectsRegion(wnes, MapData.M1520region)){showMapL2(MapData.M1520, 17);}
                else if (M1NN0lrq[17] != null && !M1NN0lrqs[17]){cancelRequest(M1NN0lrq[17].ID, M1NN0lrq, 17);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1520region)){hideMapL2(MapData.M1520, 17);}
            }
            if (M1NN0im[18] == null){
                if (intersectsRegion(wnes, MapData.M1530region)){showMapL2(MapData.M1530, 18);}
                else if (M1NN0lrq[18] != null && !M1NN0lrqs[18]){cancelRequest(M1NN0lrq[18].ID, M1NN0lrq, 18);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1530region)){hideMapL2(MapData.M1530, 18);}
            }
            if (M1NN0im[19] == null){
                if (intersectsRegion(wnes, MapData.M1540region)){showMapL2(MapData.M1540, 19);}
                else if (M1NN0lrq[19] != null && !M1NN0lrqs[19]){cancelRequest(M1NN0lrq[19].ID, M1NN0lrq, 19);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1540region)){hideMapL2(MapData.M1540, 19);}
            }
        }
        else {
            if (M1NN0im[16] != null){hideMapL2(MapData.M1510, 16);}
            else if (M1NN0lrq[16] != null && !M1NN0lrqs[16]){cancelRequest(M1NN0lrq[16].ID, M1NN0lrq, 16);}
            if (M1NN0im[17] != null){hideMapL2(MapData.M1520, 17);}
            else if (M1NN0lrq[17] != null && !M1NN0lrqs[17]){cancelRequest(M1NN0lrq[17].ID, M1NN0lrq, 17);}
            if (M1NN0im[18] != null){hideMapL2(MapData.M1530, 18);}
            else if (M1NN0lrq[18] != null && !M1NN0lrqs[18]){cancelRequest(M1NN0lrq[18].ID, M1NN0lrq, 18);}
            if (M1NN0im[19] != null){hideMapL2(MapData.M1540, 19);}
            else if (M1NN0lrq[19] != null && !M1NN0lrqs[19]){cancelRequest(M1NN0lrq[19].ID, M1NN0lrq, 19);}
        }
        // Level 2 - M16N0
        if (intersectsRegion(wnes, MapData.M1600region)){
            if (M1NN0im[20] == null){
                if (intersectsRegion(wnes, MapData.M1610region)){showMapL2(MapData.M1610, 20);}
                else if (M1NN0lrq[20] != null && !M1NN0lrqs[20]){cancelRequest(M1NN0lrq[20].ID, M1NN0lrq, 20);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1610region)){hideMapL2(MapData.M1610, 20);}
            }
            if (M1NN0im[21] == null){
                if (intersectsRegion(wnes, MapData.M1620region)){showMapL2(MapData.M1620, 21);}
                else if (M1NN0lrq[21] != null && !M1NN0lrqs[21]){cancelRequest(M1NN0lrq[21].ID, M1NN0lrq, 21);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1620region)){hideMapL2(MapData.M1620, 21);}
            }
            if (M1NN0im[22] == null){
                if (intersectsRegion(wnes, MapData.M1630region)){showMapL2(MapData.M1630, 22);}
                else if (M1NN0lrq[22] != null && !M1NN0lrqs[22]){cancelRequest(M1NN0lrq[22].ID, M1NN0lrq, 22);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1630region)){hideMapL2(MapData.M1630, 22);}
            }
            if (M1NN0im[23] == null){
                if (intersectsRegion(wnes, MapData.M1640region)){showMapL2(MapData.M1640, 23);}
                else if (M1NN0lrq[23] != null && !M1NN0lrqs[23]){cancelRequest(M1NN0lrq[23].ID, M1NN0lrq, 23);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1640region)){hideMapL2(MapData.M1640, 23);}
            }
        }
        else {
            if (M1NN0im[20] != null){hideMapL2(MapData.M1610, 20);}
            else if (M1NN0lrq[20] != null && !M1NN0lrqs[20]){cancelRequest(M1NN0lrq[20].ID, M1NN0lrq, 20);}
            if (M1NN0im[21] != null){hideMapL2(MapData.M1620, 21);}
            else if (M1NN0lrq[21] != null && !M1NN0lrqs[21]){cancelRequest(M1NN0lrq[21].ID, M1NN0lrq, 21);}
            if (M1NN0im[22] != null){hideMapL2(MapData.M1630, 22);}
            else if (M1NN0lrq[22] != null && !M1NN0lrqs[22]){cancelRequest(M1NN0lrq[22].ID, M1NN0lrq, 22);}
            if (M1NN0im[23] != null){hideMapL2(MapData.M1640, 23);}
            else if (M1NN0lrq[23] != null && !M1NN0lrqs[23]){cancelRequest(M1NN0lrq[23].ID, M1NN0lrq, 23);}
        }
        // Level 2 - M17N0
        if (intersectsRegion(wnes, MapData.M1700region)){
            if (M1NN0im[24] == null){
                if (intersectsRegion(wnes, MapData.M1710region)){showMapL2(MapData.M1710, 24);}
                else if (M1NN0lrq[24] != null && !M1NN0lrqs[24]){cancelRequest(M1NN0lrq[24].ID, M1NN0lrq, 24);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1710region)){hideMapL2(MapData.M1710, 24);}
            }
            if (M1NN0im[25] == null){
                if (intersectsRegion(wnes, MapData.M1720region)){showMapL2(MapData.M1720, 25);}
                else if (M1NN0lrq[25] != null && !M1NN0lrqs[25]){cancelRequest(M1NN0lrq[25].ID, M1NN0lrq, 25);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1720region)){hideMapL2(MapData.M1720, 25);}
            }
            if (M1NN0im[26] == null){
                if (intersectsRegion(wnes, MapData.M1730region)){showMapL2(MapData.M1730, 26);}
                else if (M1NN0lrq[26] != null && !M1NN0lrqs[26]){cancelRequest(M1NN0lrq[26].ID, M1NN0lrq, 26);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1730region)){hideMapL2(MapData.M1730, 26);}
            }
            if (M1NN0im[27] == null){
                if (intersectsRegion(wnes, MapData.M1740region)){showMapL2(MapData.M1740, 27);}
                else if (M1NN0lrq[27] != null && !M1NN0lrqs[27]){cancelRequest(M1NN0lrq[27].ID, M1NN0lrq, 27);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1740region)){hideMapL2(MapData.M1740, 27);}
            }
        }
        else {
            if (M1NN0im[24] != null){hideMapL2(MapData.M1710, 24);}
            else if (M1NN0lrq[24] != null && !M1NN0lrqs[24]){cancelRequest(M1NN0lrq[24].ID, M1NN0lrq, 24);}
            if (M1NN0im[25] != null){hideMapL2(MapData.M1720, 25);}
            else if (M1NN0lrq[25] != null && !M1NN0lrqs[25]){cancelRequest(M1NN0lrq[25].ID, M1NN0lrq, 25);}
            if (M1NN0im[26] != null){hideMapL2(MapData.M1730, 26);}
            else if (M1NN0lrq[26] != null && !M1NN0lrqs[26]){cancelRequest(M1NN0lrq[26].ID, M1NN0lrq, 26);}
            if (M1NN0im[27] != null){hideMapL2(MapData.M1740, 27);}
            else if (M1NN0lrq[27] != null && !M1NN0lrqs[27]){cancelRequest(M1NN0lrq[27].ID, M1NN0lrq, 27);}
        }
        // Level 2 - M18N0
        if (intersectsRegion(wnes, MapData.M1800region)){
            if (M1NN0im[28] == null){
                if (intersectsRegion(wnes, MapData.M1810region)){showMapL2(MapData.M1810, 28);}
                else if (M1NN0lrq[28] != null && !M1NN0lrqs[28]){cancelRequest(M1NN0lrq[28].ID, M1NN0lrq, 28);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1810region)){hideMapL2(MapData.M1810, 28);}
            }
            if (M1NN0im[29] == null){
                if (intersectsRegion(wnes, MapData.M1820region)){showMapL2(MapData.M1820, 29);}
                else if (M1NN0lrq[29] != null && !M1NN0lrqs[29]){cancelRequest(M1NN0lrq[29].ID, M1NN0lrq, 29);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1820region)){hideMapL2(MapData.M1820, 29);}
            }
            if (M1NN0im[30] == null){
                if (intersectsRegion(wnes, MapData.M1830region)){showMapL2(MapData.M1830, 30);}
                else if (M1NN0lrq[30] != null && !M1NN0lrqs[30]){cancelRequest(M1NN0lrq[30].ID, M1NN0lrq, 30);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1830region)){hideMapL2(MapData.M1830, 30);}
            }
            if (M1NN0im[31] == null){
                if (intersectsRegion(wnes, MapData.M1840region)){showMapL2(MapData.M1840, 31);}
                else if (M1NN0lrq[31] != null && !M1NN0lrqs[31]){cancelRequest(M1NN0lrq[31].ID, M1NN0lrq, 31);}
            }
            else {
                if (!intersectsRegion(wnes, MapData.M1840region)){hideMapL2(MapData.M1840, 31);}
            }
        }
        else {
            if (M1NN0im[28] != null){hideMapL2(MapData.M1810, 28);}
            else if (M1NN0lrq[28] != null && !M1NN0lrqs[28]){cancelRequest(M1NN0lrq[28].ID, M1NN0lrq, 28);}
            if (M1NN0im[29] != null){hideMapL2(MapData.M1820, 29);}
            else if (M1NN0lrq[29] != null && !M1NN0lrqs[29]){cancelRequest(M1NN0lrq[29].ID, M1NN0lrq, 29);}
            if (M1NN0im[30] != null){hideMapL2(MapData.M1830, 30);}
            else if (M1NN0lrq[30] != null && !M1NN0lrqs[30]){cancelRequest(M1NN0lrq[30].ID, M1NN0lrq, 30);}
            if (M1NN0im[31] != null){hideMapL2(MapData.M1840, 31);}
            else if (M1NN0lrq[31] != null && !M1NN0lrqs[31]){cancelRequest(M1NN0lrq[31].ID, M1NN0lrq, 31);}
        }
    }

    void updateVisibleMapsL3(long[] wnes){
        if (intersectsRegion(wnes, MapData.M1100region)){
            if (intersectsRegion(wnes, MapData.M1110region)){
		if (M1NNNim[0] == null){
		    if (intersectsRegion(wnes, MapData.M1111region)){showMapL3(MapData.M1111, 0);}
                 else if (M1NNNlrq[0] != null && !M1NNNlrqs[0]){cancelRequest(M1NNNlrq[0].ID, M1NNNlrq, 0);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1111region)){hideMapL3(MapData.M1111, 0);}
		}
		if (M1NNNim[1] == null){
		    if (intersectsRegion(wnes, MapData.M1112region)){showMapL3(MapData.M1112, 1);}
                 else if (M1NNNlrq[1] != null && !M1NNNlrqs[1]){cancelRequest(M1NNNlrq[1].ID, M1NNNlrq, 1);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1112region)){hideMapL3(MapData.M1112, 1);}
		}
		if (M1NNNim[2] == null){
		    if (intersectsRegion(wnes, MapData.M1113region)){showMapL3(MapData.M1113, 2);}
                 else if (M1NNNlrq[2] != null && !M1NNNlrqs[2]){cancelRequest(M1NNNlrq[2].ID, M1NNNlrq, 2);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1113region)){hideMapL3(MapData.M1113, 2);}
		}
		if (M1NNNim[3] == null){
		    if (intersectsRegion(wnes, MapData.M1114region)){showMapL3(MapData.M1114, 3);}
                 else if (M1NNNlrq[3] != null && !M1NNNlrqs[3]){cancelRequest(M1NNNlrq[3].ID, M1NNNlrq, 3);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1114region)){hideMapL3(MapData.M1114, 3);}
		}
            }
            else {
              if (M1NNNim[0] != null){hideMapL3(MapData.M1111, 0);}
              else if (M1NNNlrq[0] != null && !M1NNNlrqs[0]){cancelRequest(M1NNNlrq[0].ID, M1NNNlrq, 0);}
              if (M1NNNim[1] != null){hideMapL3(MapData.M1112, 1);}
              else if (M1NNNlrq[1] != null && !M1NNNlrqs[1]){cancelRequest(M1NNNlrq[1].ID, M1NNNlrq, 1);}
              if (M1NNNim[2] != null){hideMapL3(MapData.M1113, 2);}
              else if (M1NNNlrq[2] != null && !M1NNNlrqs[2]){cancelRequest(M1NNNlrq[2].ID, M1NNNlrq, 2);}
              if (M1NNNim[3] != null){hideMapL3(MapData.M1114, 3);}
              else if (M1NNNlrq[3] != null && !M1NNNlrqs[3]){cancelRequest(M1NNNlrq[3].ID, M1NNNlrq, 3);}
            }
            if (intersectsRegion(wnes, MapData.M1120region)){
		if (M1NNNim[4] == null){
		    if (intersectsRegion(wnes, MapData.M1121region)){showMapL3(MapData.M1121, 4);}
                 else if (M1NNNlrq[4] != null && !M1NNNlrqs[4]){cancelRequest(M1NNNlrq[4].ID, M1NNNlrq, 4);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1121region)){hideMapL3(MapData.M1121, 4);}
		}
		if (M1NNNim[5] == null){
		    if (intersectsRegion(wnes, MapData.M1122region)){showMapL3(MapData.M1122, 5);}
                 else if (M1NNNlrq[5] != null && !M1NNNlrqs[5]){cancelRequest(M1NNNlrq[5].ID, M1NNNlrq, 5);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1122region)){hideMapL3(MapData.M1122, 5);}
		}
		if (M1NNNim[6] == null){
		    if (intersectsRegion(wnes, MapData.M1123region)){showMapL3(MapData.M1123, 6);}
                 else if (M1NNNlrq[6] != null && !M1NNNlrqs[6]){cancelRequest(M1NNNlrq[6].ID, M1NNNlrq, 6);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1123region)){hideMapL3(MapData.M1123, 6);}
		}
		if (M1NNNim[7] == null){
		    if (intersectsRegion(wnes, MapData.M1124region)){showMapL3(MapData.M1124, 7);}
                 else if (M1NNNlrq[7] != null && !M1NNNlrqs[7]){cancelRequest(M1NNNlrq[7].ID, M1NNNlrq, 7);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1124region)){hideMapL3(MapData.M1124, 7);}
		}
            }
            else {
              if (M1NNNim[4] != null){hideMapL3(MapData.M1121, 4);}
              else if (M1NNNlrq[4] != null && !M1NNNlrqs[4]){cancelRequest(M1NNNlrq[4].ID, M1NNNlrq, 4);}
              if (M1NNNim[5] != null){hideMapL3(MapData.M1122, 5);}
              else if (M1NNNlrq[5] != null && !M1NNNlrqs[5]){cancelRequest(M1NNNlrq[5].ID, M1NNNlrq, 5);}
              if (M1NNNim[6] != null){hideMapL3(MapData.M1123, 6);}
              else if (M1NNNlrq[6] != null && !M1NNNlrqs[6]){cancelRequest(M1NNNlrq[6].ID, M1NNNlrq, 6);}
              if (M1NNNim[7] != null){hideMapL3(MapData.M1124, 7);}
              else if (M1NNNlrq[7] != null && !M1NNNlrqs[7]){cancelRequest(M1NNNlrq[7].ID, M1NNNlrq, 7);}
            }
            if (intersectsRegion(wnes, MapData.M1130region)){
		if (M1NNNim[8] == null){
		    if (intersectsRegion(wnes, MapData.M1131region)){showMapL3(MapData.M1131, 8);}
                 else if (M1NNNlrq[8] != null && !M1NNNlrqs[8]){cancelRequest(M1NNNlrq[8].ID, M1NNNlrq, 8);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1131region)){hideMapL3(MapData.M1131, 8);}
		}
		if (M1NNNim[9] == null){
		    if (intersectsRegion(wnes, MapData.M1132region)){showMapL3(MapData.M1132, 9);}
                 else if (M1NNNlrq[9] != null && !M1NNNlrqs[9]){cancelRequest(M1NNNlrq[9].ID, M1NNNlrq, 9);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1132region)){hideMapL3(MapData.M1132, 9);}
		}
		if (M1NNNim[10] == null){
		    if (intersectsRegion(wnes, MapData.M1133region)){showMapL3(MapData.M1133, 10);}
                 else if (M1NNNlrq[10] != null && !M1NNNlrqs[10]){cancelRequest(M1NNNlrq[10].ID, M1NNNlrq, 10);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1133region)){hideMapL3(MapData.M1133, 10);}
		}
		if (M1NNNim[11] == null){
		    if (intersectsRegion(wnes, MapData.M1134region)){showMapL3(MapData.M1134, 11);}
                 else if (M1NNNlrq[11] != null && !M1NNNlrqs[11]){cancelRequest(M1NNNlrq[11].ID, M1NNNlrq, 11);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1134region)){hideMapL3(MapData.M1134, 11);}
		}
            }
            else {
              if (M1NNNim[8] != null){hideMapL3(MapData.M1131, 8);}
              else if (M1NNNlrq[8] != null && !M1NNNlrqs[8]){cancelRequest(M1NNNlrq[8].ID, M1NNNlrq, 8);}
              if (M1NNNim[9] != null){hideMapL3(MapData.M1132, 9);}
              else if (M1NNNlrq[9] != null && !M1NNNlrqs[9]){cancelRequest(M1NNNlrq[9].ID, M1NNNlrq, 9);}
              if (M1NNNim[10] != null){hideMapL3(MapData.M1133, 10);}
              else if (M1NNNlrq[10] != null && !M1NNNlrqs[10]){cancelRequest(M1NNNlrq[10].ID, M1NNNlrq, 10);}
              if (M1NNNim[11] != null){hideMapL3(MapData.M1134, 11);}
              else if (M1NNNlrq[11] != null && !M1NNNlrqs[11]){cancelRequest(M1NNNlrq[11].ID, M1NNNlrq, 11);}
            }
            if (intersectsRegion(wnes, MapData.M1140region)){
		if (M1NNNim[12] == null){
		    if (intersectsRegion(wnes, MapData.M1141region)){showMapL3(MapData.M1141, 12);}
                 else if (M1NNNlrq[12] != null && !M1NNNlrqs[12]){cancelRequest(M1NNNlrq[12].ID, M1NNNlrq, 12);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1141region)){hideMapL3(MapData.M1141, 12);}
		}
		if (M1NNNim[13] == null){
		    if (intersectsRegion(wnes, MapData.M1142region)){showMapL3(MapData.M1142, 13);}
                 else if (M1NNNlrq[13] != null && !M1NNNlrqs[13]){cancelRequest(M1NNNlrq[13].ID, M1NNNlrq, 13);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1142region)){hideMapL3(MapData.M1142, 13);}
		}
		if (M1NNNim[14] == null){
		    if (intersectsRegion(wnes, MapData.M1143region)){showMapL3(MapData.M1143, 14);}
                 else if (M1NNNlrq[14] != null && !M1NNNlrqs[14]){cancelRequest(M1NNNlrq[14].ID, M1NNNlrq, 14);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1143region)){hideMapL3(MapData.M1143, 14);}
		}
		if (M1NNNim[15] == null){
		    if (intersectsRegion(wnes, MapData.M1144region)){showMapL3(MapData.M1144, 15);}
                 else if (M1NNNlrq[15] != null && !M1NNNlrqs[15]){cancelRequest(M1NNNlrq[15].ID, M1NNNlrq, 15);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1144region)){hideMapL3(MapData.M1144, 15);}
		}
            }
            else {
              if (M1NNNim[12] != null){hideMapL3(MapData.M1141, 12);}
              else if (M1NNNlrq[12] != null && !M1NNNlrqs[12]){cancelRequest(M1NNNlrq[12].ID, M1NNNlrq, 12);}
              if (M1NNNim[13] != null){hideMapL3(MapData.M1142, 13);}
              else if (M1NNNlrq[13] != null && !M1NNNlrqs[13]){cancelRequest(M1NNNlrq[13].ID, M1NNNlrq, 13);}
              if (M1NNNim[14] != null){hideMapL3(MapData.M1143, 14);}
              else if (M1NNNlrq[14] != null && !M1NNNlrqs[14]){cancelRequest(M1NNNlrq[14].ID, M1NNNlrq, 14);}
              if (M1NNNim[15] != null){hideMapL3(MapData.M1144, 15);}
              else if (M1NNNlrq[15] != null && !M1NNNlrqs[15]){cancelRequest(M1NNNlrq[15].ID, M1NNNlrq, 15);}
            }
        }
        else {
            if (M1NNNim[0] != null){hideMapL3(MapData.M1111, 0);}
            else if (M1NNNlrq[0] != null && !M1NNNlrqs[0]){cancelRequest(M1NNNlrq[0].ID, M1NNNlrq, 0);}
            if (M1NNNim[1] != null){hideMapL3(MapData.M1112, 1);}
            else if (M1NNNlrq[1] != null && !M1NNNlrqs[1]){cancelRequest(M1NNNlrq[1].ID, M1NNNlrq, 1);}
            if (M1NNNim[2] != null){hideMapL3(MapData.M1113, 2);}
            else if (M1NNNlrq[2] != null && !M1NNNlrqs[2]){cancelRequest(M1NNNlrq[2].ID, M1NNNlrq, 2);}
            if (M1NNNim[3] != null){hideMapL3(MapData.M1114, 3);}
            else if (M1NNNlrq[3] != null && !M1NNNlrqs[3]){cancelRequest(M1NNNlrq[3].ID, M1NNNlrq, 3);}
            if (M1NNNim[4] != null){hideMapL3(MapData.M1121, 4);}
            else if (M1NNNlrq[4] != null && !M1NNNlrqs[4]){cancelRequest(M1NNNlrq[4].ID, M1NNNlrq, 4);}
            if (M1NNNim[5] != null){hideMapL3(MapData.M1122, 5);}
            else if (M1NNNlrq[5] != null && !M1NNNlrqs[5]){cancelRequest(M1NNNlrq[5].ID, M1NNNlrq, 5);}
            if (M1NNNim[6] != null){hideMapL3(MapData.M1123, 6);}
            else if (M1NNNlrq[6] != null && !M1NNNlrqs[6]){cancelRequest(M1NNNlrq[6].ID, M1NNNlrq, 6);}
            if (M1NNNim[7] != null){hideMapL3(MapData.M1124, 7);}
            else if (M1NNNlrq[7] != null && !M1NNNlrqs[7]){cancelRequest(M1NNNlrq[7].ID, M1NNNlrq, 7);}
            if (M1NNNim[8] != null){hideMapL3(MapData.M1131, 8);}
            else if (M1NNNlrq[8] != null && !M1NNNlrqs[8]){cancelRequest(M1NNNlrq[8].ID, M1NNNlrq, 8);}
            if (M1NNNim[9] != null){hideMapL3(MapData.M1132, 9);}
            else if (M1NNNlrq[9] != null && !M1NNNlrqs[9]){cancelRequest(M1NNNlrq[9].ID, M1NNNlrq, 9);}
            if (M1NNNim[10] != null){hideMapL3(MapData.M1133, 10);}
            else if (M1NNNlrq[10] != null && !M1NNNlrqs[10]){cancelRequest(M1NNNlrq[10].ID, M1NNNlrq, 10);}
            if (M1NNNim[11] != null){hideMapL3(MapData.M1134, 11);}
            else if (M1NNNlrq[11] != null && !M1NNNlrqs[11]){cancelRequest(M1NNNlrq[11].ID, M1NNNlrq, 11);}
            if (M1NNNim[12] != null){hideMapL3(MapData.M1141, 12);}
            else if (M1NNNlrq[12] != null && !M1NNNlrqs[12]){cancelRequest(M1NNNlrq[12].ID, M1NNNlrq, 12);}
            if (M1NNNim[13] != null){hideMapL3(MapData.M1142, 13);}
            else if (M1NNNlrq[13] != null && !M1NNNlrqs[13]){cancelRequest(M1NNNlrq[13].ID, M1NNNlrq, 13);}
            if (M1NNNim[14] != null){hideMapL3(MapData.M1143, 14);}
            else if (M1NNNlrq[14] != null && !M1NNNlrqs[14]){cancelRequest(M1NNNlrq[14].ID, M1NNNlrq, 14);}
            if (M1NNNim[15] != null){hideMapL3(MapData.M1144, 15);}
            else if (M1NNNlrq[15] != null && !M1NNNlrqs[15]){cancelRequest(M1NNNlrq[15].ID, M1NNNlrq, 15);}
        }
        if (intersectsRegion(wnes, MapData.M1200region)){
            if (intersectsRegion(wnes, MapData.M1210region)){
		if (M1NNNim[16] == null){
		    if (intersectsRegion(wnes, MapData.M1211region)){showMapL3(MapData.M1211, 16);}
                 else if (M1NNNlrq[16] != null && !M1NNNlrqs[16]){cancelRequest(M1NNNlrq[16].ID, M1NNNlrq, 16);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1211region)){hideMapL3(MapData.M1211, 16);}
		}
		if (M1NNNim[17] == null){
		    if (intersectsRegion(wnes, MapData.M1212region)){showMapL3(MapData.M1212, 17);}
                 else if (M1NNNlrq[17] != null && !M1NNNlrqs[17]){cancelRequest(M1NNNlrq[17].ID, M1NNNlrq, 17);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1212region)){hideMapL3(MapData.M1212, 17);}
		}
		if (M1NNNim[18] == null){
		    if (intersectsRegion(wnes, MapData.M1213region)){showMapL3(MapData.M1213, 18);}
                 else if (M1NNNlrq[18] != null && !M1NNNlrqs[18]){cancelRequest(M1NNNlrq[18].ID, M1NNNlrq, 18);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1213region)){hideMapL3(MapData.M1213, 18);}
		}
		if (M1NNNim[19] == null){
		    if (intersectsRegion(wnes, MapData.M1214region)){showMapL3(MapData.M1214, 19);}
                 else if (M1NNNlrq[19] != null && !M1NNNlrqs[19]){cancelRequest(M1NNNlrq[19].ID, M1NNNlrq, 19);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1214region)){hideMapL3(MapData.M1214, 19);}
		}
            }
            else {
              if (M1NNNim[16] != null){hideMapL3(MapData.M1211, 16);}
              else if (M1NNNlrq[16] != null && !M1NNNlrqs[16]){cancelRequest(M1NNNlrq[16].ID, M1NNNlrq, 16);}
              if (M1NNNim[17] != null){hideMapL3(MapData.M1212, 17);}
              else if (M1NNNlrq[17] != null && !M1NNNlrqs[17]){cancelRequest(M1NNNlrq[17].ID, M1NNNlrq, 17);}
              if (M1NNNim[18] != null){hideMapL3(MapData.M1213, 18);}
              else if (M1NNNlrq[18] != null && !M1NNNlrqs[18]){cancelRequest(M1NNNlrq[18].ID, M1NNNlrq, 18);}
              if (M1NNNim[19] != null){hideMapL3(MapData.M1214, 19);}
              else if (M1NNNlrq[19] != null && !M1NNNlrqs[19]){cancelRequest(M1NNNlrq[19].ID, M1NNNlrq, 19);}
            }
            if (intersectsRegion(wnes, MapData.M1220region)){
		if (M1NNNim[20] == null){
		    if (intersectsRegion(wnes, MapData.M1221region)){showMapL3(MapData.M1221, 20);}
                 else if (M1NNNlrq[20] != null && !M1NNNlrqs[20]){cancelRequest(M1NNNlrq[20].ID, M1NNNlrq, 20);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1221region)){hideMapL3(MapData.M1221, 20);}
		}
		if (M1NNNim[21] == null){
		    if (intersectsRegion(wnes, MapData.M1222region)){showMapL3(MapData.M1222, 21);}
                 else if (M1NNNlrq[21] != null && !M1NNNlrqs[21]){cancelRequest(M1NNNlrq[21].ID, M1NNNlrq, 21);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1222region)){hideMapL3(MapData.M1222, 21);}
		}
		if (M1NNNim[22] == null){
		    if (intersectsRegion(wnes, MapData.M1223region)){showMapL3(MapData.M1223, 22);}
                 else if (M1NNNlrq[22] != null && !M1NNNlrqs[22]){cancelRequest(M1NNNlrq[22].ID, M1NNNlrq, 22);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1223region)){hideMapL3(MapData.M1223, 22);}
		}
		if (M1NNNim[23] == null){
		    if (intersectsRegion(wnes, MapData.M1224region)){showMapL3(MapData.M1224, 23);}
                 else if (M1NNNlrq[23] != null && !M1NNNlrqs[23]){cancelRequest(M1NNNlrq[23].ID, M1NNNlrq, 23);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1224region)){hideMapL3(MapData.M1224, 23);}
		}
            }
            else {
              if (M1NNNim[20] != null){hideMapL3(MapData.M1221, 20);}
              else if (M1NNNlrq[20] != null && !M1NNNlrqs[20]){cancelRequest(M1NNNlrq[20].ID, M1NNNlrq, 20);}
              if (M1NNNim[21] != null){hideMapL3(MapData.M1222, 21);}
              else if (M1NNNlrq[21] != null && !M1NNNlrqs[21]){cancelRequest(M1NNNlrq[21].ID, M1NNNlrq, 21);}
              if (M1NNNim[22] != null){hideMapL3(MapData.M1223, 22);}
              else if (M1NNNlrq[22] != null && !M1NNNlrqs[22]){cancelRequest(M1NNNlrq[22].ID, M1NNNlrq, 22);}
              if (M1NNNim[23] != null){hideMapL3(MapData.M1224, 23);}
              else if (M1NNNlrq[23] != null && !M1NNNlrqs[23]){cancelRequest(M1NNNlrq[23].ID, M1NNNlrq, 23);}
            }
            if (intersectsRegion(wnes, MapData.M1230region)){
		if (M1NNNim[24] == null){
		    if (intersectsRegion(wnes, MapData.M1231region)){showMapL3(MapData.M1231, 24);}
                 else if (M1NNNlrq[24] != null && !M1NNNlrqs[24]){cancelRequest(M1NNNlrq[24].ID, M1NNNlrq, 24);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1231region)){hideMapL3(MapData.M1231, 24);}
		}
		if (M1NNNim[25] == null){
		    if (intersectsRegion(wnes, MapData.M1232region)){showMapL3(MapData.M1232, 25);}
                 else if (M1NNNlrq[25] != null && !M1NNNlrqs[25]){cancelRequest(M1NNNlrq[25].ID, M1NNNlrq, 25);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1232region)){hideMapL3(MapData.M1232, 25);}
		}
		if (M1NNNim[26] == null){
		    if (intersectsRegion(wnes, MapData.M1233region)){showMapL3(MapData.M1233, 26);}
                 else if (M1NNNlrq[26] != null && !M1NNNlrqs[26]){cancelRequest(M1NNNlrq[26].ID, M1NNNlrq, 26);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1233region)){hideMapL3(MapData.M1233, 26);}
		}
		if (M1NNNim[27] == null){
		    if (intersectsRegion(wnes, MapData.M1234region)){showMapL3(MapData.M1234, 27);}
                 else if (M1NNNlrq[27] != null && !M1NNNlrqs[27]){cancelRequest(M1NNNlrq[27].ID, M1NNNlrq, 27);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1234region)){hideMapL3(MapData.M1234, 27);}
		}
            }
            else {
              if (M1NNNim[24] != null){hideMapL3(MapData.M1231, 24);}
              else if (M1NNNlrq[24] != null && !M1NNNlrqs[24]){cancelRequest(M1NNNlrq[24].ID, M1NNNlrq, 24);}
              if (M1NNNim[25] != null){hideMapL3(MapData.M1232, 25);}
              else if (M1NNNlrq[25] != null && !M1NNNlrqs[25]){cancelRequest(M1NNNlrq[25].ID, M1NNNlrq, 25);}
              if (M1NNNim[26] != null){hideMapL3(MapData.M1233, 26);}
              else if (M1NNNlrq[26] != null && !M1NNNlrqs[26]){cancelRequest(M1NNNlrq[26].ID, M1NNNlrq, 26);}
              if (M1NNNim[27] != null){hideMapL3(MapData.M1234, 27);}
              else if (M1NNNlrq[27] != null && !M1NNNlrqs[27]){cancelRequest(M1NNNlrq[27].ID, M1NNNlrq, 27);}
            }
            if (intersectsRegion(wnes, MapData.M1240region)){
		if (M1NNNim[28] == null){
		    if (intersectsRegion(wnes, MapData.M1241region)){showMapL3(MapData.M1241, 28);}
                 else if (M1NNNlrq[28] != null && !M1NNNlrqs[28]){cancelRequest(M1NNNlrq[28].ID, M1NNNlrq, 28);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1241region)){hideMapL3(MapData.M1241, 28);}
		}
		if (M1NNNim[29] == null){
		    if (intersectsRegion(wnes, MapData.M1242region)){showMapL3(MapData.M1242, 29);}
                 else if (M1NNNlrq[29] != null && !M1NNNlrqs[29]){cancelRequest(M1NNNlrq[29].ID, M1NNNlrq, 29);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1242region)){hideMapL3(MapData.M1242, 29);}
		}
		if (M1NNNim[30] == null){
		    if (intersectsRegion(wnes, MapData.M1243region)){showMapL3(MapData.M1243, 30);}
                 else if (M1NNNlrq[30] != null && !M1NNNlrqs[30]){cancelRequest(M1NNNlrq[30].ID, M1NNNlrq, 30);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1243region)){hideMapL3(MapData.M1243, 30);}
		}
		if (M1NNNim[31] == null){
		    if (intersectsRegion(wnes, MapData.M1244region)){showMapL3(MapData.M1244, 31);}
                 else if (M1NNNlrq[31] != null && !M1NNNlrqs[31]){cancelRequest(M1NNNlrq[31].ID, M1NNNlrq, 31);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1244region)){hideMapL3(MapData.M1244, 31);}
		}
            }
            else {
              if (M1NNNim[28] != null){hideMapL3(MapData.M1241, 28);}
              else if (M1NNNlrq[28] != null && !M1NNNlrqs[28]){cancelRequest(M1NNNlrq[28].ID, M1NNNlrq, 28);}
              if (M1NNNim[29] != null){hideMapL3(MapData.M1242, 29);}
              else if (M1NNNlrq[29] != null && !M1NNNlrqs[29]){cancelRequest(M1NNNlrq[29].ID, M1NNNlrq, 29);}
              if (M1NNNim[30] != null){hideMapL3(MapData.M1243, 30);}
              else if (M1NNNlrq[30] != null && !M1NNNlrqs[30]){cancelRequest(M1NNNlrq[30].ID, M1NNNlrq, 30);}
              if (M1NNNim[31] != null){hideMapL3(MapData.M1244, 31);}
              else if (M1NNNlrq[31] != null && !M1NNNlrqs[31]){cancelRequest(M1NNNlrq[31].ID, M1NNNlrq, 31);}
            }
        }
        else {
            if (M1NNNim[16] != null){hideMapL3(MapData.M1211, 16);}
            else if (M1NNNlrq[16] != null && !M1NNNlrqs[16]){cancelRequest(M1NNNlrq[16].ID, M1NNNlrq, 16);}
            if (M1NNNim[17] != null){hideMapL3(MapData.M1212, 17);}
            else if (M1NNNlrq[17] != null && !M1NNNlrqs[17]){cancelRequest(M1NNNlrq[17].ID, M1NNNlrq, 17);}
            if (M1NNNim[18] != null){hideMapL3(MapData.M1213, 18);}
            else if (M1NNNlrq[18] != null && !M1NNNlrqs[18]){cancelRequest(M1NNNlrq[18].ID, M1NNNlrq, 18);}
            if (M1NNNim[19] != null){hideMapL3(MapData.M1214, 19);}
            else if (M1NNNlrq[19] != null && !M1NNNlrqs[19]){cancelRequest(M1NNNlrq[19].ID, M1NNNlrq, 19);}
            if (M1NNNim[20] != null){hideMapL3(MapData.M1221, 20);}
            else if (M1NNNlrq[20] != null && !M1NNNlrqs[20]){cancelRequest(M1NNNlrq[20].ID, M1NNNlrq, 20);}
            if (M1NNNim[21] != null){hideMapL3(MapData.M1222, 21);}
            else if (M1NNNlrq[21] != null && !M1NNNlrqs[21]){cancelRequest(M1NNNlrq[21].ID, M1NNNlrq, 21);}
            if (M1NNNim[22] != null){hideMapL3(MapData.M1223, 22);}
            else if (M1NNNlrq[22] != null && !M1NNNlrqs[22]){cancelRequest(M1NNNlrq[22].ID, M1NNNlrq, 22);}
            if (M1NNNim[23] != null){hideMapL3(MapData.M1224, 23);}
            else if (M1NNNlrq[23] != null && !M1NNNlrqs[23]){cancelRequest(M1NNNlrq[23].ID, M1NNNlrq, 23);}
            if (M1NNNim[24] != null){hideMapL3(MapData.M1231, 24);}
            else if (M1NNNlrq[24] != null && !M1NNNlrqs[24]){cancelRequest(M1NNNlrq[24].ID, M1NNNlrq, 24);}
            if (M1NNNim[25] != null){hideMapL3(MapData.M1232, 25);}
            else if (M1NNNlrq[25] != null && !M1NNNlrqs[25]){cancelRequest(M1NNNlrq[25].ID, M1NNNlrq, 25);}
            if (M1NNNim[26] != null){hideMapL3(MapData.M1233, 26);}
            else if (M1NNNlrq[26] != null && !M1NNNlrqs[26]){cancelRequest(M1NNNlrq[26].ID, M1NNNlrq, 26);}
            if (M1NNNim[27] != null){hideMapL3(MapData.M1234, 27);}
            else if (M1NNNlrq[27] != null && !M1NNNlrqs[27]){cancelRequest(M1NNNlrq[27].ID, M1NNNlrq, 27);}
            if (M1NNNim[28] != null){hideMapL3(MapData.M1241, 28);}
            else if (M1NNNlrq[28] != null && !M1NNNlrqs[28]){cancelRequest(M1NNNlrq[28].ID, M1NNNlrq, 28);}
            if (M1NNNim[29] != null){hideMapL3(MapData.M1242, 29);}
            else if (M1NNNlrq[29] != null && !M1NNNlrqs[29]){cancelRequest(M1NNNlrq[29].ID, M1NNNlrq, 29);}
            if (M1NNNim[30] != null){hideMapL3(MapData.M1243, 30);}
            else if (M1NNNlrq[30] != null && !M1NNNlrqs[30]){cancelRequest(M1NNNlrq[30].ID, M1NNNlrq, 30);}
            if (M1NNNim[31] != null){hideMapL3(MapData.M1244, 31);}
            else if (M1NNNlrq[31] != null && !M1NNNlrqs[31]){cancelRequest(M1NNNlrq[31].ID, M1NNNlrq, 31);}
        }
        if (intersectsRegion(wnes, MapData.M1300region)){
            if (intersectsRegion(wnes, MapData.M1310region)){
		if (M1NNNim[32] == null){
		    if (intersectsRegion(wnes, MapData.M1311region)){showMapL3(MapData.M1311, 32);}
                 else if (M1NNNlrq[32] != null && !M1NNNlrqs[32]){cancelRequest(M1NNNlrq[32].ID, M1NNNlrq, 32);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1311region)){hideMapL3(MapData.M1311, 32);}
		}
		if (M1NNNim[33] == null){
		    if (intersectsRegion(wnes, MapData.M1312region)){showMapL3(MapData.M1312, 33);}
                 else if (M1NNNlrq[33] != null && !M1NNNlrqs[33]){cancelRequest(M1NNNlrq[33].ID, M1NNNlrq, 33);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1312region)){hideMapL3(MapData.M1312, 33);}
		}
		if (M1NNNim[34] == null){
		    if (intersectsRegion(wnes, MapData.M1313region)){showMapL3(MapData.M1313, 34);}
                 else if (M1NNNlrq[34] != null && !M1NNNlrqs[34]){cancelRequest(M1NNNlrq[34].ID, M1NNNlrq, 34);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1313region)){hideMapL3(MapData.M1313, 34);}
		}
		if (M1NNNim[35] == null){
		    if (intersectsRegion(wnes, MapData.M1314region)){showMapL3(MapData.M1314, 35);}
                 else if (M1NNNlrq[35] != null && !M1NNNlrqs[35]){cancelRequest(M1NNNlrq[35].ID, M1NNNlrq, 35);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1314region)){hideMapL3(MapData.M1314, 35);}
		}
            }
            else {
              if (M1NNNim[32] != null){hideMapL3(MapData.M1311, 32);}
              else if (M1NNNlrq[32] != null && !M1NNNlrqs[32]){cancelRequest(M1NNNlrq[32].ID, M1NNNlrq, 32);}
              if (M1NNNim[33] != null){hideMapL3(MapData.M1312, 33);}
              else if (M1NNNlrq[33] != null && !M1NNNlrqs[33]){cancelRequest(M1NNNlrq[33].ID, M1NNNlrq, 33);}
              if (M1NNNim[34] != null){hideMapL3(MapData.M1313, 34);}
              else if (M1NNNlrq[34] != null && !M1NNNlrqs[34]){cancelRequest(M1NNNlrq[34].ID, M1NNNlrq, 34);}
              if (M1NNNim[35] != null){hideMapL3(MapData.M1314, 35);}
              else if (M1NNNlrq[35] != null && !M1NNNlrqs[35]){cancelRequest(M1NNNlrq[35].ID, M1NNNlrq, 35);}
            }
            if (intersectsRegion(wnes, MapData.M1320region)){
		if (M1NNNim[36] == null){
		    if (intersectsRegion(wnes, MapData.M1321region)){showMapL3(MapData.M1321, 36);}
                 else if (M1NNNlrq[36] != null && !M1NNNlrqs[36]){cancelRequest(M1NNNlrq[36].ID, M1NNNlrq, 36);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1321region)){hideMapL3(MapData.M1321, 36);}
		}
		if (M1NNNim[37] == null){
		    if (intersectsRegion(wnes, MapData.M1322region)){showMapL3(MapData.M1322, 37);}
                 else if (M1NNNlrq[37] != null && !M1NNNlrqs[37]){cancelRequest(M1NNNlrq[37].ID, M1NNNlrq, 37);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1322region)){hideMapL3(MapData.M1322, 37);}
		}
		if (M1NNNim[38] == null){
		    if (intersectsRegion(wnes, MapData.M1323region)){showMapL3(MapData.M1323, 38);}
                 else if (M1NNNlrq[38] != null && !M1NNNlrqs[38]){cancelRequest(M1NNNlrq[38].ID, M1NNNlrq, 38);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1323region)){hideMapL3(MapData.M1323, 38);}
		}
		if (M1NNNim[39] == null){
		    if (intersectsRegion(wnes, MapData.M1324region)){showMapL3(MapData.M1324, 39);}
                 else if (M1NNNlrq[39] != null && !M1NNNlrqs[39]){cancelRequest(M1NNNlrq[39].ID, M1NNNlrq, 39);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1324region)){hideMapL3(MapData.M1324, 39);}
		}
            }
            else {
              if (M1NNNim[36] != null){hideMapL3(MapData.M1321, 36);}
              else if (M1NNNlrq[36] != null && !M1NNNlrqs[36]){cancelRequest(M1NNNlrq[36].ID, M1NNNlrq, 36);}
              if (M1NNNim[37] != null){hideMapL3(MapData.M1322, 37);}
              else if (M1NNNlrq[37] != null && !M1NNNlrqs[37]){cancelRequest(M1NNNlrq[37].ID, M1NNNlrq, 37);}
              if (M1NNNim[38] != null){hideMapL3(MapData.M1323, 38);}
              else if (M1NNNlrq[38] != null && !M1NNNlrqs[38]){cancelRequest(M1NNNlrq[38].ID, M1NNNlrq, 38);}
              if (M1NNNim[39] != null){hideMapL3(MapData.M1324, 39);}
              else if (M1NNNlrq[39] != null && !M1NNNlrqs[39]){cancelRequest(M1NNNlrq[39].ID, M1NNNlrq, 39);}
            }
            if (intersectsRegion(wnes, MapData.M1330region)){
		if (M1NNNim[40] == null){
		    if (intersectsRegion(wnes, MapData.M1331region)){showMapL3(MapData.M1331, 40);}
                 else if (M1NNNlrq[40] != null && !M1NNNlrqs[40]){cancelRequest(M1NNNlrq[40].ID, M1NNNlrq, 40);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1331region)){hideMapL3(MapData.M1331, 40);}
		}
		if (M1NNNim[41] == null){
		    if (intersectsRegion(wnes, MapData.M1332region)){showMapL3(MapData.M1332, 41);}
                 else if (M1NNNlrq[41] != null && !M1NNNlrqs[41]){cancelRequest(M1NNNlrq[41].ID, M1NNNlrq, 41);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1332region)){hideMapL3(MapData.M1332, 41);}
		}
		if (M1NNNim[42] == null){
		    if (intersectsRegion(wnes, MapData.M1333region)){showMapL3(MapData.M1333, 42);}
                 else if (M1NNNlrq[42] != null && !M1NNNlrqs[42]){cancelRequest(M1NNNlrq[42].ID, M1NNNlrq, 42);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1333region)){hideMapL3(MapData.M1333, 42);}
		}
		if (M1NNNim[43] == null){
		    if (intersectsRegion(wnes, MapData.M1334region)){showMapL3(MapData.M1334, 43);}
                 else if (M1NNNlrq[43] != null && !M1NNNlrqs[43]){cancelRequest(M1NNNlrq[43].ID, M1NNNlrq, 43);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1334region)){hideMapL3(MapData.M1334, 43);}
		}
            }
            else {
              if (M1NNNim[40] != null){hideMapL3(MapData.M1331, 40);}
              else if (M1NNNlrq[40] != null && !M1NNNlrqs[40]){cancelRequest(M1NNNlrq[40].ID, M1NNNlrq, 40);}
              if (M1NNNim[41] != null){hideMapL3(MapData.M1332, 41);}
              else if (M1NNNlrq[41] != null && !M1NNNlrqs[41]){cancelRequest(M1NNNlrq[41].ID, M1NNNlrq, 41);}
              if (M1NNNim[42] != null){hideMapL3(MapData.M1333, 42);}
              else if (M1NNNlrq[42] != null && !M1NNNlrqs[42]){cancelRequest(M1NNNlrq[42].ID, M1NNNlrq, 42);}
              if (M1NNNim[43] != null){hideMapL3(MapData.M1334, 43);}
              else if (M1NNNlrq[43] != null && !M1NNNlrqs[43]){cancelRequest(M1NNNlrq[43].ID, M1NNNlrq, 43);}
            }
            if (intersectsRegion(wnes, MapData.M1340region)){
		if (M1NNNim[44] == null){
		    if (intersectsRegion(wnes, MapData.M1341region)){showMapL3(MapData.M1341, 44);}
                 else if (M1NNNlrq[44] != null && !M1NNNlrqs[44]){cancelRequest(M1NNNlrq[44].ID, M1NNNlrq, 44);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1341region)){hideMapL3(MapData.M1341, 44);}
		}
		if (M1NNNim[45] == null){
		    if (intersectsRegion(wnes, MapData.M1342region)){showMapL3(MapData.M1342, 45);}
                 else if (M1NNNlrq[45] != null && !M1NNNlrqs[45]){cancelRequest(M1NNNlrq[45].ID, M1NNNlrq, 45);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1342region)){hideMapL3(MapData.M1342, 45);}
		}
		if (M1NNNim[46] == null){
		    if (intersectsRegion(wnes, MapData.M1343region)){showMapL3(MapData.M1343, 46);}
                 else if (M1NNNlrq[46] != null && !M1NNNlrqs[46]){cancelRequest(M1NNNlrq[46].ID, M1NNNlrq, 46);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1343region)){hideMapL3(MapData.M1343, 46);}
		}
		if (M1NNNim[47] == null){
		    if (intersectsRegion(wnes, MapData.M1344region)){showMapL3(MapData.M1344, 47);}
                 else if (M1NNNlrq[47] != null && !M1NNNlrqs[47]){cancelRequest(M1NNNlrq[47].ID, M1NNNlrq, 47);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1344region)){hideMapL3(MapData.M1344, 47);}
		}
            }
            else {
              if (M1NNNim[44] != null){hideMapL3(MapData.M1341, 44);}
              else if (M1NNNlrq[44] != null && !M1NNNlrqs[44]){cancelRequest(M1NNNlrq[44].ID, M1NNNlrq, 44);}
              if (M1NNNim[45] != null){hideMapL3(MapData.M1342, 45);}
              else if (M1NNNlrq[45] != null && !M1NNNlrqs[45]){cancelRequest(M1NNNlrq[45].ID, M1NNNlrq, 45);}
              if (M1NNNim[46] != null){hideMapL3(MapData.M1343, 46);}
              else if (M1NNNlrq[46] != null && !M1NNNlrqs[46]){cancelRequest(M1NNNlrq[46].ID, M1NNNlrq, 46);}
              if (M1NNNim[47] != null){hideMapL3(MapData.M1344, 47);}
              else if (M1NNNlrq[47] != null && !M1NNNlrqs[47]){cancelRequest(M1NNNlrq[47].ID, M1NNNlrq, 47);}
            }
        }
        else {
            if (M1NNNim[32] != null){hideMapL3(MapData.M1311, 32);}
            else if (M1NNNlrq[32] != null && !M1NNNlrqs[32]){cancelRequest(M1NNNlrq[32].ID, M1NNNlrq, 32);}
            if (M1NNNim[33] != null){hideMapL3(MapData.M1312, 33);}
            else if (M1NNNlrq[33] != null && !M1NNNlrqs[33]){cancelRequest(M1NNNlrq[33].ID, M1NNNlrq, 33);}
            if (M1NNNim[34] != null){hideMapL3(MapData.M1313, 34);}
            else if (M1NNNlrq[34] != null && !M1NNNlrqs[34]){cancelRequest(M1NNNlrq[34].ID, M1NNNlrq, 34);}
            if (M1NNNim[35] != null){hideMapL3(MapData.M1314, 35);}
            else if (M1NNNlrq[35] != null && !M1NNNlrqs[35]){cancelRequest(M1NNNlrq[35].ID, M1NNNlrq, 35);}
            if (M1NNNim[36] != null){hideMapL3(MapData.M1321, 36);}
            else if (M1NNNlrq[36] != null && !M1NNNlrqs[36]){cancelRequest(M1NNNlrq[36].ID, M1NNNlrq, 36);}
            if (M1NNNim[37] != null){hideMapL3(MapData.M1322, 37);}
            else if (M1NNNlrq[37] != null && !M1NNNlrqs[37]){cancelRequest(M1NNNlrq[37].ID, M1NNNlrq, 37);}
            if (M1NNNim[38] != null){hideMapL3(MapData.M1323, 38);}
            else if (M1NNNlrq[38] != null && !M1NNNlrqs[38]){cancelRequest(M1NNNlrq[38].ID, M1NNNlrq, 38);}
            if (M1NNNim[39] != null){hideMapL3(MapData.M1324, 39);}
            else if (M1NNNlrq[39] != null && !M1NNNlrqs[39]){cancelRequest(M1NNNlrq[39].ID, M1NNNlrq, 39);}
            if (M1NNNim[40] != null){hideMapL3(MapData.M1331, 40);}
            else if (M1NNNlrq[40] != null && !M1NNNlrqs[40]){cancelRequest(M1NNNlrq[40].ID, M1NNNlrq, 40);}
            if (M1NNNim[41] != null){hideMapL3(MapData.M1332, 41);}
            else if (M1NNNlrq[41] != null && !M1NNNlrqs[41]){cancelRequest(M1NNNlrq[41].ID, M1NNNlrq, 41);}
            if (M1NNNim[42] != null){hideMapL3(MapData.M1333, 42);}
            else if (M1NNNlrq[42] != null && !M1NNNlrqs[42]){cancelRequest(M1NNNlrq[42].ID, M1NNNlrq, 42);}
            if (M1NNNim[43] != null){hideMapL3(MapData.M1334, 43);}
            else if (M1NNNlrq[43] != null && !M1NNNlrqs[43]){cancelRequest(M1NNNlrq[43].ID, M1NNNlrq, 43);}
            if (M1NNNim[44] != null){hideMapL3(MapData.M1341, 44);}
            else if (M1NNNlrq[44] != null && !M1NNNlrqs[44]){cancelRequest(M1NNNlrq[44].ID, M1NNNlrq, 44);}
            if (M1NNNim[45] != null){hideMapL3(MapData.M1342, 45);}
            else if (M1NNNlrq[45] != null && !M1NNNlrqs[45]){cancelRequest(M1NNNlrq[45].ID, M1NNNlrq, 45);}
            if (M1NNNim[46] != null){hideMapL3(MapData.M1343, 46);}
            else if (M1NNNlrq[46] != null && !M1NNNlrqs[46]){cancelRequest(M1NNNlrq[46].ID, M1NNNlrq, 46);}
            if (M1NNNim[47] != null){hideMapL3(MapData.M1344, 47);}
            else if (M1NNNlrq[47] != null && !M1NNNlrqs[47]){cancelRequest(M1NNNlrq[47].ID, M1NNNlrq, 47);}
        }
        if (intersectsRegion(wnes, MapData.M1400region)){
            if (intersectsRegion(wnes, MapData.M1410region)){
		if (M1NNNim[48] == null){
		    if (intersectsRegion(wnes, MapData.M1411region)){showMapL3(MapData.M1411, 48);}
                 else if (M1NNNlrq[48] != null && !M1NNNlrqs[48]){cancelRequest(M1NNNlrq[48].ID, M1NNNlrq, 48);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1411region)){hideMapL3(MapData.M1411, 48);}
		}
		if (M1NNNim[49] == null){
		    if (intersectsRegion(wnes, MapData.M1412region)){showMapL3(MapData.M1412, 49);}
                 else if (M1NNNlrq[49] != null && !M1NNNlrqs[49]){cancelRequest(M1NNNlrq[49].ID, M1NNNlrq, 49);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1412region)){hideMapL3(MapData.M1412, 49);}
		}
		if (M1NNNim[50] == null){
		    if (intersectsRegion(wnes, MapData.M1413region)){showMapL3(MapData.M1413, 50);}
                 else if (M1NNNlrq[50] != null && !M1NNNlrqs[50]){cancelRequest(M1NNNlrq[50].ID, M1NNNlrq, 50);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1413region)){hideMapL3(MapData.M1413, 50);}
		}
		if (M1NNNim[51] == null){
		    if (intersectsRegion(wnes, MapData.M1414region)){showMapL3(MapData.M1414, 51);}
                 else if (M1NNNlrq[51] != null && !M1NNNlrqs[51]){cancelRequest(M1NNNlrq[51].ID, M1NNNlrq, 51);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1414region)){hideMapL3(MapData.M1414, 51);}
		}
            }
            else {
              if (M1NNNim[48] != null){hideMapL3(MapData.M1411, 48);}
              else if (M1NNNlrq[48] != null && !M1NNNlrqs[48]){cancelRequest(M1NNNlrq[48].ID, M1NNNlrq, 48);}
              if (M1NNNim[49] != null){hideMapL3(MapData.M1412, 49);}
              else if (M1NNNlrq[49] != null && !M1NNNlrqs[49]){cancelRequest(M1NNNlrq[49].ID, M1NNNlrq, 49);}
              if (M1NNNim[50] != null){hideMapL3(MapData.M1413, 50);}
              else if (M1NNNlrq[50] != null && !M1NNNlrqs[50]){cancelRequest(M1NNNlrq[50].ID, M1NNNlrq, 50);}
              if (M1NNNim[51] != null){hideMapL3(MapData.M1414, 51);}
              else if (M1NNNlrq[51] != null && !M1NNNlrqs[51]){cancelRequest(M1NNNlrq[51].ID, M1NNNlrq, 51);}
            }
            if (intersectsRegion(wnes, MapData.M1420region)){
		if (M1NNNim[52] == null){
		    if (intersectsRegion(wnes, MapData.M1421region)){showMapL3(MapData.M1421, 52);}
                 else if (M1NNNlrq[52] != null && !M1NNNlrqs[52]){cancelRequest(M1NNNlrq[52].ID, M1NNNlrq, 52);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1421region)){hideMapL3(MapData.M1421, 52);}
		}
		if (M1NNNim[53] == null){
		    if (intersectsRegion(wnes, MapData.M1422region)){showMapL3(MapData.M1422, 53);}
                 else if (M1NNNlrq[53] != null && !M1NNNlrqs[53]){cancelRequest(M1NNNlrq[53].ID, M1NNNlrq, 53);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1422region)){hideMapL3(MapData.M1422, 53);}
		}
		if (M1NNNim[54] == null){
		    if (intersectsRegion(wnes, MapData.M1423region)){showMapL3(MapData.M1423, 54);}
                 else if (M1NNNlrq[54] != null && !M1NNNlrqs[54]){cancelRequest(M1NNNlrq[54].ID, M1NNNlrq, 54);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1423region)){hideMapL3(MapData.M1423, 54);}
		}
		if (M1NNNim[55] == null){
		    if (intersectsRegion(wnes, MapData.M1424region)){showMapL3(MapData.M1424, 55);}
                 else if (M1NNNlrq[55] != null && !M1NNNlrqs[55]){cancelRequest(M1NNNlrq[55].ID, M1NNNlrq, 55);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1424region)){hideMapL3(MapData.M1424, 55);}
		}
            }
            else {
              if (M1NNNim[52] != null){hideMapL3(MapData.M1421, 52);}
              else if (M1NNNlrq[52] != null && !M1NNNlrqs[52]){cancelRequest(M1NNNlrq[52].ID, M1NNNlrq, 52);}
              if (M1NNNim[53] != null){hideMapL3(MapData.M1422, 53);}
              else if (M1NNNlrq[53] != null && !M1NNNlrqs[53]){cancelRequest(M1NNNlrq[53].ID, M1NNNlrq, 53);}
              if (M1NNNim[54] != null){hideMapL3(MapData.M1423, 54);}
              else if (M1NNNlrq[54] != null && !M1NNNlrqs[54]){cancelRequest(M1NNNlrq[54].ID, M1NNNlrq, 54);}
              if (M1NNNim[55] != null){hideMapL3(MapData.M1424, 55);}
              else if (M1NNNlrq[55] != null && !M1NNNlrqs[55]){cancelRequest(M1NNNlrq[55].ID, M1NNNlrq, 55);}
            }
            if (intersectsRegion(wnes, MapData.M1430region)){
		if (M1NNNim[56] == null){
		    if (intersectsRegion(wnes, MapData.M1431region)){showMapL3(MapData.M1431, 56);}
                 else if (M1NNNlrq[56] != null && !M1NNNlrqs[56]){cancelRequest(M1NNNlrq[56].ID, M1NNNlrq, 56);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1431region)){hideMapL3(MapData.M1431, 56);}
		}
		if (M1NNNim[57] == null){
		    if (intersectsRegion(wnes, MapData.M1432region)){showMapL3(MapData.M1432, 57);}
                 else if (M1NNNlrq[57] != null && !M1NNNlrqs[57]){cancelRequest(M1NNNlrq[57].ID, M1NNNlrq, 57);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1432region)){hideMapL3(MapData.M1432, 57);}
		}
		if (M1NNNim[58] == null){
		    if (intersectsRegion(wnes, MapData.M1433region)){showMapL3(MapData.M1433, 58);}
                 else if (M1NNNlrq[58] != null && !M1NNNlrqs[58]){cancelRequest(M1NNNlrq[58].ID, M1NNNlrq, 58);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1433region)){hideMapL3(MapData.M1433, 58);}
		}
		if (M1NNNim[59] == null){
		    if (intersectsRegion(wnes, MapData.M1434region)){showMapL3(MapData.M1434, 59);}
                 else if (M1NNNlrq[59] != null && !M1NNNlrqs[59]){cancelRequest(M1NNNlrq[59].ID, M1NNNlrq, 59);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1434region)){hideMapL3(MapData.M1434, 59);}
		}
            }
            else {
              if (M1NNNim[56] != null){hideMapL3(MapData.M1431, 56);}
              else if (M1NNNlrq[56] != null && !M1NNNlrqs[56]){cancelRequest(M1NNNlrq[56].ID, M1NNNlrq, 56);}
              if (M1NNNim[57] != null){hideMapL3(MapData.M1432, 57);}
              else if (M1NNNlrq[57] != null && !M1NNNlrqs[57]){cancelRequest(M1NNNlrq[57].ID, M1NNNlrq, 57);}
              if (M1NNNim[58] != null){hideMapL3(MapData.M1433, 58);}
              else if (M1NNNlrq[58] != null && !M1NNNlrqs[58]){cancelRequest(M1NNNlrq[58].ID, M1NNNlrq, 58);}
              if (M1NNNim[59] != null){hideMapL3(MapData.M1434, 59);}
              else if (M1NNNlrq[59] != null && !M1NNNlrqs[59]){cancelRequest(M1NNNlrq[59].ID, M1NNNlrq, 59);}
            }
            if (intersectsRegion(wnes, MapData.M1440region)){
		if (M1NNNim[60] == null){
		    if (intersectsRegion(wnes, MapData.M1441region)){showMapL3(MapData.M1441, 60);}
                 else if (M1NNNlrq[60] != null && !M1NNNlrqs[60]){cancelRequest(M1NNNlrq[60].ID, M1NNNlrq, 60);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1441region)){hideMapL3(MapData.M1441, 60);}
		}
		if (M1NNNim[61] == null){
		    if (intersectsRegion(wnes, MapData.M1442region)){showMapL3(MapData.M1442, 61);}
                 else if (M1NNNlrq[61] != null && !M1NNNlrqs[61]){cancelRequest(M1NNNlrq[61].ID, M1NNNlrq, 61);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1442region)){hideMapL3(MapData.M1442, 61);}
		}
		if (M1NNNim[62] == null){
		    if (intersectsRegion(wnes, MapData.M1443region)){showMapL3(MapData.M1443, 62);}
                 else if (M1NNNlrq[62] != null && !M1NNNlrqs[62]){cancelRequest(M1NNNlrq[62].ID, M1NNNlrq, 62);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1443region)){hideMapL3(MapData.M1443, 62);}
		}
		if (M1NNNim[63] == null){
		    if (intersectsRegion(wnes, MapData.M1444region)){showMapL3(MapData.M1444, 63);}
                 else if (M1NNNlrq[63] != null && !M1NNNlrqs[63]){cancelRequest(M1NNNlrq[63].ID, M1NNNlrq, 63);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1444region)){hideMapL3(MapData.M1444, 63);}
		}
            }
            else {
              if (M1NNNim[60] != null){hideMapL3(MapData.M1441, 60);}
              else if (M1NNNlrq[60] != null && !M1NNNlrqs[60]){cancelRequest(M1NNNlrq[60].ID, M1NNNlrq, 60);}
              if (M1NNNim[61] != null){hideMapL3(MapData.M1442, 61);}
              else if (M1NNNlrq[61] != null && !M1NNNlrqs[61]){cancelRequest(M1NNNlrq[61].ID, M1NNNlrq, 61);}
              if (M1NNNim[62] != null){hideMapL3(MapData.M1443, 62);}
              else if (M1NNNlrq[62] != null && !M1NNNlrqs[62]){cancelRequest(M1NNNlrq[62].ID, M1NNNlrq, 62);}
              if (M1NNNim[63] != null){hideMapL3(MapData.M1444, 63);}
              else if (M1NNNlrq[63] != null && !M1NNNlrqs[63]){cancelRequest(M1NNNlrq[63].ID, M1NNNlrq, 63);}
            }
        }
        else {
            if (M1NNNim[48] != null){hideMapL3(MapData.M1411, 48);}
            else if (M1NNNlrq[48] != null && !M1NNNlrqs[48]){cancelRequest(M1NNNlrq[48].ID, M1NNNlrq, 48);}
            if (M1NNNim[49] != null){hideMapL3(MapData.M1412, 49);}
            else if (M1NNNlrq[49] != null && !M1NNNlrqs[49]){cancelRequest(M1NNNlrq[49].ID, M1NNNlrq, 49);}
            if (M1NNNim[50] != null){hideMapL3(MapData.M1413, 50);}
            else if (M1NNNlrq[50] != null && !M1NNNlrqs[50]){cancelRequest(M1NNNlrq[50].ID, M1NNNlrq, 50);}
            if (M1NNNim[51] != null){hideMapL3(MapData.M1414, 51);}
            else if (M1NNNlrq[51] != null && !M1NNNlrqs[51]){cancelRequest(M1NNNlrq[51].ID, M1NNNlrq, 51);}
            if (M1NNNim[52] != null){hideMapL3(MapData.M1421, 52);}
            else if (M1NNNlrq[52] != null && !M1NNNlrqs[52]){cancelRequest(M1NNNlrq[52].ID, M1NNNlrq, 52);}
            if (M1NNNim[53] != null){hideMapL3(MapData.M1422, 53);}
            else if (M1NNNlrq[53] != null && !M1NNNlrqs[53]){cancelRequest(M1NNNlrq[53].ID, M1NNNlrq, 53);}
            if (M1NNNim[54] != null){hideMapL3(MapData.M1423, 54);}
            else if (M1NNNlrq[54] != null && !M1NNNlrqs[54]){cancelRequest(M1NNNlrq[54].ID, M1NNNlrq, 54);}
            if (M1NNNim[55] != null){hideMapL3(MapData.M1424, 55);}
            else if (M1NNNlrq[55] != null && !M1NNNlrqs[55]){cancelRequest(M1NNNlrq[55].ID, M1NNNlrq, 55);}
            if (M1NNNim[56] != null){hideMapL3(MapData.M1431, 56);}
            else if (M1NNNlrq[56] != null && !M1NNNlrqs[56]){cancelRequest(M1NNNlrq[56].ID, M1NNNlrq, 56);}
            if (M1NNNim[57] != null){hideMapL3(MapData.M1432, 57);}
            else if (M1NNNlrq[57] != null && !M1NNNlrqs[57]){cancelRequest(M1NNNlrq[57].ID, M1NNNlrq, 57);}
            if (M1NNNim[58] != null){hideMapL3(MapData.M1433, 58);}
            else if (M1NNNlrq[58] != null && !M1NNNlrqs[58]){cancelRequest(M1NNNlrq[58].ID, M1NNNlrq, 58);}
            if (M1NNNim[59] != null){hideMapL3(MapData.M1434, 59);}
            else if (M1NNNlrq[59] != null && !M1NNNlrqs[59]){cancelRequest(M1NNNlrq[59].ID, M1NNNlrq, 59);}
            if (M1NNNim[60] != null){hideMapL3(MapData.M1441, 60);}
            else if (M1NNNlrq[60] != null && !M1NNNlrqs[60]){cancelRequest(M1NNNlrq[60].ID, M1NNNlrq, 60);}
            if (M1NNNim[61] != null){hideMapL3(MapData.M1442, 61);}
            else if (M1NNNlrq[61] != null && !M1NNNlrqs[61]){cancelRequest(M1NNNlrq[61].ID, M1NNNlrq, 61);}
            if (M1NNNim[62] != null){hideMapL3(MapData.M1443, 62);}
            else if (M1NNNlrq[62] != null && !M1NNNlrqs[62]){cancelRequest(M1NNNlrq[62].ID, M1NNNlrq, 62);}
            if (M1NNNim[63] != null){hideMapL3(MapData.M1444, 63);}
            else if (M1NNNlrq[63] != null && !M1NNNlrqs[63]){cancelRequest(M1NNNlrq[63].ID, M1NNNlrq, 63);}
        }
        if (intersectsRegion(wnes, MapData.M1500region)){
            if (intersectsRegion(wnes, MapData.M1510region)){
		if (M1NNNim[64] == null){
		    if (intersectsRegion(wnes, MapData.M1511region)){showMapL3(MapData.M1511, 64);}
                 else if (M1NNNlrq[64] != null && !M1NNNlrqs[64]){cancelRequest(M1NNNlrq[64].ID, M1NNNlrq, 64);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1511region)){hideMapL3(MapData.M1511, 64);}
		}
		if (M1NNNim[65] == null){
		    if (intersectsRegion(wnes, MapData.M1512region)){showMapL3(MapData.M1512, 65);}
                 else if (M1NNNlrq[65] != null && !M1NNNlrqs[65]){cancelRequest(M1NNNlrq[65].ID, M1NNNlrq, 65);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1512region)){hideMapL3(MapData.M1512, 65);}
		}
		if (M1NNNim[66] == null){
		    if (intersectsRegion(wnes, MapData.M1513region)){showMapL3(MapData.M1513, 66);}
                 else if (M1NNNlrq[66] != null && !M1NNNlrqs[66]){cancelRequest(M1NNNlrq[66].ID, M1NNNlrq, 66);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1513region)){hideMapL3(MapData.M1513, 66);}
		}
		if (M1NNNim[67] == null){
		    if (intersectsRegion(wnes, MapData.M1514region)){showMapL3(MapData.M1514, 67);}
                 else if (M1NNNlrq[67] != null && !M1NNNlrqs[67]){cancelRequest(M1NNNlrq[67].ID, M1NNNlrq, 67);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1514region)){hideMapL3(MapData.M1514, 67);}
		}
            }
            else {
              if (M1NNNim[64] != null){hideMapL3(MapData.M1511, 64);}
              else if (M1NNNlrq[64] != null && !M1NNNlrqs[64]){cancelRequest(M1NNNlrq[64].ID, M1NNNlrq, 64);}
              if (M1NNNim[65] != null){hideMapL3(MapData.M1512, 65);}
              else if (M1NNNlrq[65] != null && !M1NNNlrqs[65]){cancelRequest(M1NNNlrq[65].ID, M1NNNlrq, 65);}
              if (M1NNNim[66] != null){hideMapL3(MapData.M1513, 66);}
              else if (M1NNNlrq[66] != null && !M1NNNlrqs[66]){cancelRequest(M1NNNlrq[66].ID, M1NNNlrq, 66);}
              if (M1NNNim[67] != null){hideMapL3(MapData.M1514, 67);}
              else if (M1NNNlrq[67] != null && !M1NNNlrqs[67]){cancelRequest(M1NNNlrq[67].ID, M1NNNlrq, 67);}
            }
            if (intersectsRegion(wnes, MapData.M1520region)){
		if (M1NNNim[68] == null){
		    if (intersectsRegion(wnes, MapData.M1521region)){showMapL3(MapData.M1521, 68);}
                 else if (M1NNNlrq[68] != null && !M1NNNlrqs[68]){cancelRequest(M1NNNlrq[68].ID, M1NNNlrq, 68);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1521region)){hideMapL3(MapData.M1521, 68);}
		}
		if (M1NNNim[69] == null){
		    if (intersectsRegion(wnes, MapData.M1522region)){showMapL3(MapData.M1522, 69);}
                 else if (M1NNNlrq[69] != null && !M1NNNlrqs[69]){cancelRequest(M1NNNlrq[69].ID, M1NNNlrq, 69);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1522region)){hideMapL3(MapData.M1522, 69);}
		}
		if (M1NNNim[70] == null){
		    if (intersectsRegion(wnes, MapData.M1523region)){showMapL3(MapData.M1523, 70);}
                 else if (M1NNNlrq[70] != null && !M1NNNlrqs[70]){cancelRequest(M1NNNlrq[70].ID, M1NNNlrq, 70);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1523region)){hideMapL3(MapData.M1523, 70);}
		}
		if (M1NNNim[71] == null){
		    if (intersectsRegion(wnes, MapData.M1524region)){showMapL3(MapData.M1524, 71);}
                 else if (M1NNNlrq[71] != null && !M1NNNlrqs[71]){cancelRequest(M1NNNlrq[71].ID, M1NNNlrq, 71);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1524region)){hideMapL3(MapData.M1524, 71);}
		}
            }
            else {
              if (M1NNNim[68] != null){hideMapL3(MapData.M1521, 68);}
              else if (M1NNNlrq[68] != null && !M1NNNlrqs[68]){cancelRequest(M1NNNlrq[68].ID, M1NNNlrq, 68);}
              if (M1NNNim[69] != null){hideMapL3(MapData.M1522, 69);}
              else if (M1NNNlrq[69] != null && !M1NNNlrqs[69]){cancelRequest(M1NNNlrq[69].ID, M1NNNlrq, 69);}
              if (M1NNNim[70] != null){hideMapL3(MapData.M1523, 70);}
              else if (M1NNNlrq[70] != null && !M1NNNlrqs[70]){cancelRequest(M1NNNlrq[70].ID, M1NNNlrq, 70);}
              if (M1NNNim[71] != null){hideMapL3(MapData.M1524, 71);}
              else if (M1NNNlrq[71] != null && !M1NNNlrqs[71]){cancelRequest(M1NNNlrq[71].ID, M1NNNlrq, 71);}
            }
            if (intersectsRegion(wnes, MapData.M1530region)){
		if (M1NNNim[72] == null){
		    if (intersectsRegion(wnes, MapData.M1531region)){showMapL3(MapData.M1531, 72);}
                 else if (M1NNNlrq[72] != null && !M1NNNlrqs[72]){cancelRequest(M1NNNlrq[72].ID, M1NNNlrq, 72);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1531region)){hideMapL3(MapData.M1531, 72);}
		}
		if (M1NNNim[73] == null){
		    if (intersectsRegion(wnes, MapData.M1532region)){showMapL3(MapData.M1532, 73);}
                 else if (M1NNNlrq[73] != null && !M1NNNlrqs[73]){cancelRequest(M1NNNlrq[73].ID, M1NNNlrq, 73);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1532region)){hideMapL3(MapData.M1532, 73);}
		}
		if (M1NNNim[74] == null){
		    if (intersectsRegion(wnes, MapData.M1533region)){showMapL3(MapData.M1533, 74);}
                 else if (M1NNNlrq[74] != null && !M1NNNlrqs[74]){cancelRequest(M1NNNlrq[74].ID, M1NNNlrq, 74);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1533region)){hideMapL3(MapData.M1533, 74);}
		}
		if (M1NNNim[75] == null){
		    if (intersectsRegion(wnes, MapData.M1534region)){showMapL3(MapData.M1534, 75);}
                 else if (M1NNNlrq[75] != null && !M1NNNlrqs[75]){cancelRequest(M1NNNlrq[75].ID, M1NNNlrq, 75);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1534region)){hideMapL3(MapData.M1534, 75);}
		}
            }
            else {
              if (M1NNNim[72] != null){hideMapL3(MapData.M1531, 72);}
              else if (M1NNNlrq[72] != null && !M1NNNlrqs[72]){cancelRequest(M1NNNlrq[72].ID, M1NNNlrq, 72);}
              if (M1NNNim[73] != null){hideMapL3(MapData.M1532, 73);}
              else if (M1NNNlrq[73] != null && !M1NNNlrqs[73]){cancelRequest(M1NNNlrq[73].ID, M1NNNlrq, 73);}
              if (M1NNNim[74] != null){hideMapL3(MapData.M1533, 74);}
              else if (M1NNNlrq[74] != null && !M1NNNlrqs[74]){cancelRequest(M1NNNlrq[74].ID, M1NNNlrq, 74);}
              if (M1NNNim[75] != null){hideMapL3(MapData.M1534, 75);}
              else if (M1NNNlrq[75] != null && !M1NNNlrqs[75]){cancelRequest(M1NNNlrq[75].ID, M1NNNlrq, 75);}
            }
            if (intersectsRegion(wnes, MapData.M1540region)){
		if (M1NNNim[76] == null){
		    if (intersectsRegion(wnes, MapData.M1541region)){showMapL3(MapData.M1541, 76);}
                 else if (M1NNNlrq[76] != null && !M1NNNlrqs[76]){cancelRequest(M1NNNlrq[76].ID, M1NNNlrq, 76);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1541region)){hideMapL3(MapData.M1541, 76);}
		}
		if (M1NNNim[77] == null){
		    if (intersectsRegion(wnes, MapData.M1542region)){showMapL3(MapData.M1542, 77);}
                 else if (M1NNNlrq[77] != null && !M1NNNlrqs[77]){cancelRequest(M1NNNlrq[77].ID, M1NNNlrq, 77);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1542region)){hideMapL3(MapData.M1542, 77);}
		}
		if (M1NNNim[78] == null){
		    if (intersectsRegion(wnes, MapData.M1543region)){showMapL3(MapData.M1543, 78);}
                 else if (M1NNNlrq[78] != null && !M1NNNlrqs[78]){cancelRequest(M1NNNlrq[78].ID, M1NNNlrq, 78);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1543region)){hideMapL3(MapData.M1543, 78);}
		}
		if (M1NNNim[79] == null){
		    if (intersectsRegion(wnes, MapData.M1544region)){showMapL3(MapData.M1544, 79);}
                 else if (M1NNNlrq[79] != null && !M1NNNlrqs[79]){cancelRequest(M1NNNlrq[79].ID, M1NNNlrq, 79);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1544region)){hideMapL3(MapData.M1544, 79);}
		}
            }
            else {
              if (M1NNNim[76] != null){hideMapL3(MapData.M1541, 76);}
              else if (M1NNNlrq[76] != null && !M1NNNlrqs[76]){cancelRequest(M1NNNlrq[76].ID, M1NNNlrq, 76);}
              if (M1NNNim[77] != null){hideMapL3(MapData.M1542, 77);}
              else if (M1NNNlrq[77] != null && !M1NNNlrqs[77]){cancelRequest(M1NNNlrq[77].ID, M1NNNlrq, 77);}
              if (M1NNNim[78] != null){hideMapL3(MapData.M1543, 78);}
              else if (M1NNNlrq[78] != null && !M1NNNlrqs[78]){cancelRequest(M1NNNlrq[78].ID, M1NNNlrq, 78);}
              if (M1NNNim[79] != null){hideMapL3(MapData.M1544, 79);}
              else if (M1NNNlrq[79] != null && !M1NNNlrqs[79]){cancelRequest(M1NNNlrq[79].ID, M1NNNlrq, 79);}
            }
        }
        else {
            if (M1NNNim[64] != null){hideMapL3(MapData.M1511, 64);}
            else if (M1NNNlrq[64] != null && !M1NNNlrqs[64]){cancelRequest(M1NNNlrq[64].ID, M1NNNlrq, 64);}
            if (M1NNNim[65] != null){hideMapL3(MapData.M1512, 65);}
            else if (M1NNNlrq[65] != null && !M1NNNlrqs[65]){cancelRequest(M1NNNlrq[65].ID, M1NNNlrq, 65);}
            if (M1NNNim[66] != null){hideMapL3(MapData.M1513, 66);}
            else if (M1NNNlrq[66] != null && !M1NNNlrqs[66]){cancelRequest(M1NNNlrq[66].ID, M1NNNlrq, 66);}
            if (M1NNNim[67] != null){hideMapL3(MapData.M1514, 67);}
            else if (M1NNNlrq[67] != null && !M1NNNlrqs[67]){cancelRequest(M1NNNlrq[67].ID, M1NNNlrq, 67);}
            if (M1NNNim[68] != null){hideMapL3(MapData.M1521, 68);}
            else if (M1NNNlrq[68] != null && !M1NNNlrqs[68]){cancelRequest(M1NNNlrq[68].ID, M1NNNlrq, 68);}
            if (M1NNNim[69] != null){hideMapL3(MapData.M1522, 69);}
            else if (M1NNNlrq[69] != null && !M1NNNlrqs[69]){cancelRequest(M1NNNlrq[69].ID, M1NNNlrq, 69);}
            if (M1NNNim[70] != null){hideMapL3(MapData.M1523, 70);}
            else if (M1NNNlrq[70] != null && !M1NNNlrqs[70]){cancelRequest(M1NNNlrq[70].ID, M1NNNlrq, 70);}
            if (M1NNNim[71] != null){hideMapL3(MapData.M1524, 71);}
            else if (M1NNNlrq[71] != null && !M1NNNlrqs[71]){cancelRequest(M1NNNlrq[71].ID, M1NNNlrq, 71);}
            if (M1NNNim[72] != null){hideMapL3(MapData.M1531, 72);}
            else if (M1NNNlrq[72] != null && !M1NNNlrqs[72]){cancelRequest(M1NNNlrq[72].ID, M1NNNlrq, 72);}
            if (M1NNNim[73] != null){hideMapL3(MapData.M1532, 73);}
            else if (M1NNNlrq[73] != null && !M1NNNlrqs[73]){cancelRequest(M1NNNlrq[73].ID, M1NNNlrq, 73);}
            if (M1NNNim[74] != null){hideMapL3(MapData.M1533, 74);}
            else if (M1NNNlrq[74] != null && !M1NNNlrqs[74]){cancelRequest(M1NNNlrq[74].ID, M1NNNlrq, 74);}
            if (M1NNNim[75] != null){hideMapL3(MapData.M1534, 75);}
            else if (M1NNNlrq[75] != null && !M1NNNlrqs[75]){cancelRequest(M1NNNlrq[75].ID, M1NNNlrq, 75);}
            if (M1NNNim[76] != null){hideMapL3(MapData.M1541, 76);}
            else if (M1NNNlrq[76] != null && !M1NNNlrqs[76]){cancelRequest(M1NNNlrq[76].ID, M1NNNlrq, 76);}
            if (M1NNNim[77] != null){hideMapL3(MapData.M1542, 77);}
            else if (M1NNNlrq[77] != null && !M1NNNlrqs[77]){cancelRequest(M1NNNlrq[77].ID, M1NNNlrq, 77);}
            if (M1NNNim[78] != null){hideMapL3(MapData.M1543, 78);}
            else if (M1NNNlrq[78] != null && !M1NNNlrqs[78]){cancelRequest(M1NNNlrq[78].ID, M1NNNlrq, 78);}
            if (M1NNNim[79] != null){hideMapL3(MapData.M1544, 79);}
            else if (M1NNNlrq[79] != null && !M1NNNlrqs[79]){cancelRequest(M1NNNlrq[79].ID, M1NNNlrq, 79);}
        }
        if (intersectsRegion(wnes, MapData.M1600region)){
            if (intersectsRegion(wnes, MapData.M1610region)){
		if (M1NNNim[80] == null){
		    if (intersectsRegion(wnes, MapData.M1611region)){showMapL3(MapData.M1611, 80);}
                 else if (M1NNNlrq[80] != null && !M1NNNlrqs[80]){cancelRequest(M1NNNlrq[80].ID, M1NNNlrq, 80);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1611region)){hideMapL3(MapData.M1611, 80);}
		}
		if (M1NNNim[81] == null){
		    if (intersectsRegion(wnes, MapData.M1612region)){showMapL3(MapData.M1612, 81);}
                 else if (M1NNNlrq[81] != null && !M1NNNlrqs[81]){cancelRequest(M1NNNlrq[81].ID, M1NNNlrq, 81);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1612region)){hideMapL3(MapData.M1612, 81);}
		}
		if (M1NNNim[82] == null){
		    if (intersectsRegion(wnes, MapData.M1613region)){showMapL3(MapData.M1613, 82);}
                 else if (M1NNNlrq[82] != null && !M1NNNlrqs[82]){cancelRequest(M1NNNlrq[82].ID, M1NNNlrq, 82);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1613region)){hideMapL3(MapData.M1613, 82);}
		}
		if (M1NNNim[83] == null){
		    if (intersectsRegion(wnes, MapData.M1614region)){showMapL3(MapData.M1614, 83);}
                 else if (M1NNNlrq[83] != null && !M1NNNlrqs[83]){cancelRequest(M1NNNlrq[83].ID, M1NNNlrq, 83);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1614region)){hideMapL3(MapData.M1614, 83);}
		}
            }
            else {
              if (M1NNNim[80] != null){hideMapL3(MapData.M1611, 80);}
              else if (M1NNNlrq[80] != null && !M1NNNlrqs[80]){cancelRequest(M1NNNlrq[80].ID, M1NNNlrq, 80);}
              if (M1NNNim[81] != null){hideMapL3(MapData.M1612, 81);}
              else if (M1NNNlrq[81] != null && !M1NNNlrqs[81]){cancelRequest(M1NNNlrq[81].ID, M1NNNlrq, 81);}
              if (M1NNNim[82] != null){hideMapL3(MapData.M1613, 82);}
              else if (M1NNNlrq[82] != null && !M1NNNlrqs[82]){cancelRequest(M1NNNlrq[82].ID, M1NNNlrq, 82);}
              if (M1NNNim[83] != null){hideMapL3(MapData.M1614, 83);}
              else if (M1NNNlrq[83] != null && !M1NNNlrqs[83]){cancelRequest(M1NNNlrq[83].ID, M1NNNlrq, 83);}
            }
            if (intersectsRegion(wnes, MapData.M1620region)){
		if (M1NNNim[84] == null){
		    if (intersectsRegion(wnes, MapData.M1621region)){showMapL3(MapData.M1621, 84);}
                 else if (M1NNNlrq[84] != null && !M1NNNlrqs[84]){cancelRequest(M1NNNlrq[84].ID, M1NNNlrq, 84);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1621region)){hideMapL3(MapData.M1621, 84);}
		}
		if (M1NNNim[85] == null){
		    if (intersectsRegion(wnes, MapData.M1622region)){showMapL3(MapData.M1622, 85);}
                 else if (M1NNNlrq[85] != null && !M1NNNlrqs[85]){cancelRequest(M1NNNlrq[85].ID, M1NNNlrq, 85);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1622region)){hideMapL3(MapData.M1622, 85);}
		}
		if (M1NNNim[86] == null){
		    if (intersectsRegion(wnes, MapData.M1623region)){showMapL3(MapData.M1623, 86);}
                 else if (M1NNNlrq[86] != null && !M1NNNlrqs[86]){cancelRequest(M1NNNlrq[86].ID, M1NNNlrq, 86);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1623region)){hideMapL3(MapData.M1623, 86);}
		}
		if (M1NNNim[87] == null){
		    if (intersectsRegion(wnes, MapData.M1624region)){showMapL3(MapData.M1624, 87);}
                 else if (M1NNNlrq[87] != null && !M1NNNlrqs[87]){cancelRequest(M1NNNlrq[87].ID, M1NNNlrq, 87);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1624region)){hideMapL3(MapData.M1624, 87);}
		}
            }
            else {
              if (M1NNNim[84] != null){hideMapL3(MapData.M1621, 84);}
              else if (M1NNNlrq[84] != null && !M1NNNlrqs[84]){cancelRequest(M1NNNlrq[84].ID, M1NNNlrq, 84);}
              if (M1NNNim[85] != null){hideMapL3(MapData.M1622, 85);}
              else if (M1NNNlrq[85] != null && !M1NNNlrqs[85]){cancelRequest(M1NNNlrq[85].ID, M1NNNlrq, 85);}
              if (M1NNNim[86] != null){hideMapL3(MapData.M1623, 86);}
              else if (M1NNNlrq[86] != null && !M1NNNlrqs[86]){cancelRequest(M1NNNlrq[86].ID, M1NNNlrq, 86);}
              if (M1NNNim[87] != null){hideMapL3(MapData.M1624, 87);}
              else if (M1NNNlrq[87] != null && !M1NNNlrqs[87]){cancelRequest(M1NNNlrq[87].ID, M1NNNlrq, 87);}
            }
            if (intersectsRegion(wnes, MapData.M1630region)){
		if (M1NNNim[88] == null){
		    if (intersectsRegion(wnes, MapData.M1631region)){showMapL3(MapData.M1631, 88);}
                 else if (M1NNNlrq[88] != null && !M1NNNlrqs[88]){cancelRequest(M1NNNlrq[88].ID, M1NNNlrq, 88);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1631region)){hideMapL3(MapData.M1631, 88);}
		}
		if (M1NNNim[89] == null){
		    if (intersectsRegion(wnes, MapData.M1632region)){showMapL3(MapData.M1632, 89);}
                 else if (M1NNNlrq[89] != null && !M1NNNlrqs[89]){cancelRequest(M1NNNlrq[89].ID, M1NNNlrq, 89);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1632region)){hideMapL3(MapData.M1632, 89);}
		}
		if (M1NNNim[90] == null){
		    if (intersectsRegion(wnes, MapData.M1633region)){showMapL3(MapData.M1633, 90);}
                 else if (M1NNNlrq[90] != null && !M1NNNlrqs[90]){cancelRequest(M1NNNlrq[90].ID, M1NNNlrq, 90);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1633region)){hideMapL3(MapData.M1633, 90);}
		}
		if (M1NNNim[91] == null){
		    if (intersectsRegion(wnes, MapData.M1634region)){showMapL3(MapData.M1634, 91);}
                 else if (M1NNNlrq[91] != null && !M1NNNlrqs[91]){cancelRequest(M1NNNlrq[91].ID, M1NNNlrq, 91);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1634region)){hideMapL3(MapData.M1634, 91);}
		}
            }
            else {
              if (M1NNNim[88] != null){hideMapL3(MapData.M1631, 88);}
              else if (M1NNNlrq[88] != null && !M1NNNlrqs[88]){cancelRequest(M1NNNlrq[88].ID, M1NNNlrq, 88);}
              if (M1NNNim[89] != null){hideMapL3(MapData.M1632, 89);}
              else if (M1NNNlrq[89] != null && !M1NNNlrqs[89]){cancelRequest(M1NNNlrq[89].ID, M1NNNlrq, 89);}
              if (M1NNNim[90] != null){hideMapL3(MapData.M1633, 90);}
              else if (M1NNNlrq[90] != null && !M1NNNlrqs[90]){cancelRequest(M1NNNlrq[90].ID, M1NNNlrq, 90);}
              if (M1NNNim[91] != null){hideMapL3(MapData.M1634, 91);}
              else if (M1NNNlrq[91] != null && !M1NNNlrqs[91]){cancelRequest(M1NNNlrq[91].ID, M1NNNlrq, 91);}
            }
            if (intersectsRegion(wnes, MapData.M1640region)){
		if (M1NNNim[92] == null){
		    if (intersectsRegion(wnes, MapData.M1641region)){showMapL3(MapData.M1641, 92);}
                 else if (M1NNNlrq[92] != null && !M1NNNlrqs[92]){cancelRequest(M1NNNlrq[92].ID, M1NNNlrq, 92);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1641region)){hideMapL3(MapData.M1641, 92);}
		}
		if (M1NNNim[93] == null){
		    if (intersectsRegion(wnes, MapData.M1642region)){showMapL3(MapData.M1642, 93);}
                 else if (M1NNNlrq[93] != null && !M1NNNlrqs[93]){cancelRequest(M1NNNlrq[93].ID, M1NNNlrq, 93);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1642region)){hideMapL3(MapData.M1642, 93);}
		}
		if (M1NNNim[94] == null){
		    if (intersectsRegion(wnes, MapData.M1643region)){showMapL3(MapData.M1643, 94);}
                 else if (M1NNNlrq[94] != null && !M1NNNlrqs[94]){cancelRequest(M1NNNlrq[94].ID, M1NNNlrq, 94);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1643region)){hideMapL3(MapData.M1643, 94);}
		}
		if (M1NNNim[95] == null){
		    if (intersectsRegion(wnes, MapData.M1644region)){showMapL3(MapData.M1644, 95);}
                 else if (M1NNNlrq[95] != null && !M1NNNlrqs[95]){cancelRequest(M1NNNlrq[95].ID, M1NNNlrq, 95);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1644region)){hideMapL3(MapData.M1644, 95);}
		}
            }
            else {
              if (M1NNNim[92] != null){hideMapL3(MapData.M1641, 92);}
              else if (M1NNNlrq[92] != null && !M1NNNlrqs[92]){cancelRequest(M1NNNlrq[92].ID, M1NNNlrq, 92);}
              if (M1NNNim[93] != null){hideMapL3(MapData.M1642, 93);}
              else if (M1NNNlrq[93] != null && !M1NNNlrqs[93]){cancelRequest(M1NNNlrq[93].ID, M1NNNlrq, 93);}
              if (M1NNNim[94] != null){hideMapL3(MapData.M1643, 94);}
              else if (M1NNNlrq[94] != null && !M1NNNlrqs[94]){cancelRequest(M1NNNlrq[94].ID, M1NNNlrq, 94);}
              if (M1NNNim[95] != null){hideMapL3(MapData.M1644, 95);}
              else if (M1NNNlrq[95] != null && !M1NNNlrqs[95]){cancelRequest(M1NNNlrq[95].ID, M1NNNlrq, 95);}
            }
        }
        else {
            if (M1NNNim[80] != null){hideMapL3(MapData.M1611, 80);}
            else if (M1NNNlrq[80] != null && !M1NNNlrqs[80]){cancelRequest(M1NNNlrq[80].ID, M1NNNlrq, 80);}
            if (M1NNNim[81] != null){hideMapL3(MapData.M1612, 81);}
            else if (M1NNNlrq[81] != null && !M1NNNlrqs[81]){cancelRequest(M1NNNlrq[81].ID, M1NNNlrq, 81);}
            if (M1NNNim[82] != null){hideMapL3(MapData.M1613, 82);}
            else if (M1NNNlrq[82] != null && !M1NNNlrqs[82]){cancelRequest(M1NNNlrq[82].ID, M1NNNlrq, 82);}
            if (M1NNNim[83] != null){hideMapL3(MapData.M1614, 83);}
            else if (M1NNNlrq[83] != null && !M1NNNlrqs[83]){cancelRequest(M1NNNlrq[83].ID, M1NNNlrq, 83);}
            if (M1NNNim[84] != null){hideMapL3(MapData.M1621, 84);}
            else if (M1NNNlrq[84] != null && !M1NNNlrqs[84]){cancelRequest(M1NNNlrq[84].ID, M1NNNlrq, 84);}
            if (M1NNNim[85] != null){hideMapL3(MapData.M1622, 85);}
            else if (M1NNNlrq[85] != null && !M1NNNlrqs[85]){cancelRequest(M1NNNlrq[85].ID, M1NNNlrq, 85);}
            if (M1NNNim[86] != null){hideMapL3(MapData.M1623, 86);}
            else if (M1NNNlrq[86] != null && !M1NNNlrqs[86]){cancelRequest(M1NNNlrq[86].ID, M1NNNlrq, 86);}
            if (M1NNNim[87] != null){hideMapL3(MapData.M1624, 87);}
            else if (M1NNNlrq[87] != null && !M1NNNlrqs[87]){cancelRequest(M1NNNlrq[87].ID, M1NNNlrq, 87);}
            if (M1NNNim[88] != null){hideMapL3(MapData.M1631, 88);}
            else if (M1NNNlrq[88] != null && !M1NNNlrqs[88]){cancelRequest(M1NNNlrq[88].ID, M1NNNlrq, 88);}
            if (M1NNNim[89] != null){hideMapL3(MapData.M1632, 89);}
            else if (M1NNNlrq[89] != null && !M1NNNlrqs[89]){cancelRequest(M1NNNlrq[89].ID, M1NNNlrq, 89);}
            if (M1NNNim[90] != null){hideMapL3(MapData.M1633, 90);}
            else if (M1NNNlrq[90] != null && !M1NNNlrqs[90]){cancelRequest(M1NNNlrq[90].ID, M1NNNlrq, 90);}
            if (M1NNNim[91] != null){hideMapL3(MapData.M1634, 91);}
            else if (M1NNNlrq[91] != null && !M1NNNlrqs[91]){cancelRequest(M1NNNlrq[91].ID, M1NNNlrq, 91);}
            if (M1NNNim[92] != null){hideMapL3(MapData.M1641, 92);}
            else if (M1NNNlrq[92] != null && !M1NNNlrqs[92]){cancelRequest(M1NNNlrq[92].ID, M1NNNlrq, 92);}
            if (M1NNNim[93] != null){hideMapL3(MapData.M1642, 93);}
            else if (M1NNNlrq[93] != null && !M1NNNlrqs[93]){cancelRequest(M1NNNlrq[93].ID, M1NNNlrq, 93);}
            if (M1NNNim[94] != null){hideMapL3(MapData.M1643, 94);}
            else if (M1NNNlrq[94] != null && !M1NNNlrqs[94]){cancelRequest(M1NNNlrq[94].ID, M1NNNlrq, 94);}
            if (M1NNNim[95] != null){hideMapL3(MapData.M1644, 95);}
            else if (M1NNNlrq[95] != null && !M1NNNlrqs[95]){cancelRequest(M1NNNlrq[95].ID, M1NNNlrq, 95);}
        }
        if (intersectsRegion(wnes, MapData.M1700region)){
            if (intersectsRegion(wnes, MapData.M1710region)){
		if (M1NNNim[96] == null){
		    if (intersectsRegion(wnes, MapData.M1711region)){showMapL3(MapData.M1711, 96);}
                 else if (M1NNNlrq[96] != null && !M1NNNlrqs[96]){cancelRequest(M1NNNlrq[96].ID, M1NNNlrq, 96);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1711region)){hideMapL3(MapData.M1711, 96);}
		}
		if (M1NNNim[97] == null){
		    if (intersectsRegion(wnes, MapData.M1712region)){showMapL3(MapData.M1712, 97);}
                 else if (M1NNNlrq[97] != null && !M1NNNlrqs[97]){cancelRequest(M1NNNlrq[97].ID, M1NNNlrq, 97);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1712region)){hideMapL3(MapData.M1712, 97);}
		}
		if (M1NNNim[98] == null){
		    if (intersectsRegion(wnes, MapData.M1713region)){showMapL3(MapData.M1713, 98);}
                 else if (M1NNNlrq[98] != null && !M1NNNlrqs[98]){cancelRequest(M1NNNlrq[98].ID, M1NNNlrq, 98);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1713region)){hideMapL3(MapData.M1713, 98);}
		}
		if (M1NNNim[99] == null){
		    if (intersectsRegion(wnes, MapData.M1714region)){showMapL3(MapData.M1714, 99);}
                 else if (M1NNNlrq[99] != null && !M1NNNlrqs[99]){cancelRequest(M1NNNlrq[99].ID, M1NNNlrq, 99);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1714region)){hideMapL3(MapData.M1714, 99);}
		}
            }
            else {
              if (M1NNNim[96] != null){hideMapL3(MapData.M1711, 96);}
              else if (M1NNNlrq[96] != null && !M1NNNlrqs[96]){cancelRequest(M1NNNlrq[96].ID, M1NNNlrq, 96);}
              if (M1NNNim[97] != null){hideMapL3(MapData.M1712, 97);}
              else if (M1NNNlrq[97] != null && !M1NNNlrqs[97]){cancelRequest(M1NNNlrq[97].ID, M1NNNlrq, 97);}
              if (M1NNNim[98] != null){hideMapL3(MapData.M1713, 98);}
              else if (M1NNNlrq[98] != null && !M1NNNlrqs[98]){cancelRequest(M1NNNlrq[98].ID, M1NNNlrq, 98);}
              if (M1NNNim[99] != null){hideMapL3(MapData.M1714, 99);}
              else if (M1NNNlrq[99] != null && !M1NNNlrqs[99]){cancelRequest(M1NNNlrq[99].ID, M1NNNlrq, 99);}
            }
            if (intersectsRegion(wnes, MapData.M1720region)){
		if (M1NNNim[100] == null){
		    if (intersectsRegion(wnes, MapData.M1721region)){showMapL3(MapData.M1721, 100);}
                 else if (M1NNNlrq[100] != null && !M1NNNlrqs[100]){cancelRequest(M1NNNlrq[100].ID, M1NNNlrq, 100);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1721region)){hideMapL3(MapData.M1721, 100);}
		}
		if (M1NNNim[101] == null){
		    if (intersectsRegion(wnes, MapData.M1722region)){showMapL3(MapData.M1722, 101);}
                 else if (M1NNNlrq[101] != null && !M1NNNlrqs[101]){cancelRequest(M1NNNlrq[101].ID, M1NNNlrq, 101);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1722region)){hideMapL3(MapData.M1722, 101);}
		}
		if (M1NNNim[102] == null){
		    if (intersectsRegion(wnes, MapData.M1723region)){showMapL3(MapData.M1723, 102);}
                 else if (M1NNNlrq[102] != null && !M1NNNlrqs[102]){cancelRequest(M1NNNlrq[102].ID, M1NNNlrq, 102);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1723region)){hideMapL3(MapData.M1723, 102);}
		}
		if (M1NNNim[103] == null){
		    if (intersectsRegion(wnes, MapData.M1724region)){showMapL3(MapData.M1724, 103);}
                 else if (M1NNNlrq[103] != null && !M1NNNlrqs[103]){cancelRequest(M1NNNlrq[103].ID, M1NNNlrq, 103);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1724region)){hideMapL3(MapData.M1724, 103);}
		}
            }
            else {
              if (M1NNNim[100] != null){hideMapL3(MapData.M1721, 100);}
              else if (M1NNNlrq[100] != null && !M1NNNlrqs[100]){cancelRequest(M1NNNlrq[100].ID, M1NNNlrq, 100);}
              if (M1NNNim[101] != null){hideMapL3(MapData.M1722, 101);}
              else if (M1NNNlrq[101] != null && !M1NNNlrqs[101]){cancelRequest(M1NNNlrq[101].ID, M1NNNlrq, 101);}
              if (M1NNNim[102] != null){hideMapL3(MapData.M1723, 102);}
              else if (M1NNNlrq[102] != null && !M1NNNlrqs[102]){cancelRequest(M1NNNlrq[102].ID, M1NNNlrq, 102);}
              if (M1NNNim[103] != null){hideMapL3(MapData.M1724, 103);}
              else if (M1NNNlrq[103] != null && !M1NNNlrqs[103]){cancelRequest(M1NNNlrq[103].ID, M1NNNlrq, 103);}
            }
            if (intersectsRegion(wnes, MapData.M1730region)){
		if (M1NNNim[104] == null){
		    if (intersectsRegion(wnes, MapData.M1731region)){showMapL3(MapData.M1731, 104);}
                 else if (M1NNNlrq[104] != null && !M1NNNlrqs[104]){cancelRequest(M1NNNlrq[104].ID, M1NNNlrq, 104);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1731region)){hideMapL3(MapData.M1731, 104);}
		}
		if (M1NNNim[105] == null){
		    if (intersectsRegion(wnes, MapData.M1732region)){showMapL3(MapData.M1732, 105);}
                 else if (M1NNNlrq[105] != null && !M1NNNlrqs[105]){cancelRequest(M1NNNlrq[105].ID, M1NNNlrq, 105);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1732region)){hideMapL3(MapData.M1732, 105);}
		}
		if (M1NNNim[106] == null){
		    if (intersectsRegion(wnes, MapData.M1733region)){showMapL3(MapData.M1733, 106);}
                 else if (M1NNNlrq[106] != null && !M1NNNlrqs[106]){cancelRequest(M1NNNlrq[106].ID, M1NNNlrq, 106);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1733region)){hideMapL3(MapData.M1733, 106);}
		}
		if (M1NNNim[107] == null){
		    if (intersectsRegion(wnes, MapData.M1734region)){showMapL3(MapData.M1734, 107);}
                 else if (M1NNNlrq[107] != null && !M1NNNlrqs[107]){cancelRequest(M1NNNlrq[107].ID, M1NNNlrq, 107);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1734region)){hideMapL3(MapData.M1734, 107);}
		}
            }
            else {
              if (M1NNNim[104] != null){hideMapL3(MapData.M1731, 104);}
              else if (M1NNNlrq[104] != null && !M1NNNlrqs[104]){cancelRequest(M1NNNlrq[104].ID, M1NNNlrq, 104);}
              if (M1NNNim[105] != null){hideMapL3(MapData.M1732, 105);}
              else if (M1NNNlrq[105] != null && !M1NNNlrqs[105]){cancelRequest(M1NNNlrq[105].ID, M1NNNlrq, 105);}
              if (M1NNNim[106] != null){hideMapL3(MapData.M1733, 106);}
              else if (M1NNNlrq[106] != null && !M1NNNlrqs[106]){cancelRequest(M1NNNlrq[106].ID, M1NNNlrq, 106);}
              if (M1NNNim[107] != null){hideMapL3(MapData.M1734, 107);}
              else if (M1NNNlrq[107] != null && !M1NNNlrqs[107]){cancelRequest(M1NNNlrq[107].ID, M1NNNlrq, 107);}
            }
            if (intersectsRegion(wnes, MapData.M1740region)){
		if (M1NNNim[108] == null){
		    if (intersectsRegion(wnes, MapData.M1741region)){showMapL3(MapData.M1741, 108);}
                 else if (M1NNNlrq[108] != null && !M1NNNlrqs[108]){cancelRequest(M1NNNlrq[108].ID, M1NNNlrq, 108);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1741region)){hideMapL3(MapData.M1741, 108);}
		}
		if (M1NNNim[109] == null){
		    if (intersectsRegion(wnes, MapData.M1742region)){showMapL3(MapData.M1742, 109);}
                 else if (M1NNNlrq[109] != null && !M1NNNlrqs[109]){cancelRequest(M1NNNlrq[109].ID, M1NNNlrq, 109);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1742region)){hideMapL3(MapData.M1742, 109);}
		}
		if (M1NNNim[110] == null){
		    if (intersectsRegion(wnes, MapData.M1743region)){showMapL3(MapData.M1743, 110);}
                 else if (M1NNNlrq[110] != null && !M1NNNlrqs[110]){cancelRequest(M1NNNlrq[110].ID, M1NNNlrq, 110);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1743region)){hideMapL3(MapData.M1743, 110);}
		}
		if (M1NNNim[111] == null){
		    if (intersectsRegion(wnes, MapData.M1744region)){showMapL3(MapData.M1744, 111);}
                 else if (M1NNNlrq[111] != null && !M1NNNlrqs[111]){cancelRequest(M1NNNlrq[111].ID, M1NNNlrq, 111);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1744region)){hideMapL3(MapData.M1744, 111);}
		}
            }
            else {
              if (M1NNNim[108] != null){hideMapL3(MapData.M1741, 108);}
              else if (M1NNNlrq[108] != null && !M1NNNlrqs[108]){cancelRequest(M1NNNlrq[108].ID, M1NNNlrq, 108);}
              if (M1NNNim[109] != null){hideMapL3(MapData.M1742, 109);}
              else if (M1NNNlrq[109] != null && !M1NNNlrqs[109]){cancelRequest(M1NNNlrq[109].ID, M1NNNlrq, 109);}
              if (M1NNNim[110] != null){hideMapL3(MapData.M1743, 110);}
              else if (M1NNNlrq[110] != null && !M1NNNlrqs[110]){cancelRequest(M1NNNlrq[110].ID, M1NNNlrq, 110);}
              if (M1NNNim[111] != null){hideMapL3(MapData.M1744, 111);}
              else if (M1NNNlrq[111] != null && !M1NNNlrqs[111]){cancelRequest(M1NNNlrq[111].ID, M1NNNlrq, 111);}
            }
        }
        else {
            if (M1NNNim[96] != null){hideMapL3(MapData.M1711, 96);}
            else if (M1NNNlrq[96] != null && !M1NNNlrqs[96]){cancelRequest(M1NNNlrq[96].ID, M1NNNlrq, 96);}
            if (M1NNNim[97] != null){hideMapL3(MapData.M1712, 97);}
            else if (M1NNNlrq[97] != null && !M1NNNlrqs[97]){cancelRequest(M1NNNlrq[97].ID, M1NNNlrq, 97);}
            if (M1NNNim[98] != null){hideMapL3(MapData.M1713, 98);}
            else if (M1NNNlrq[98] != null && !M1NNNlrqs[98]){cancelRequest(M1NNNlrq[98].ID, M1NNNlrq, 98);}
            if (M1NNNim[99] != null){hideMapL3(MapData.M1714, 99);}
            else if (M1NNNlrq[99] != null && !M1NNNlrqs[99]){cancelRequest(M1NNNlrq[99].ID, M1NNNlrq, 99);}
            if (M1NNNim[100] != null){hideMapL3(MapData.M1721, 100);}
            else if (M1NNNlrq[100] != null && !M1NNNlrqs[100]){cancelRequest(M1NNNlrq[100].ID, M1NNNlrq, 100);}
            if (M1NNNim[101] != null){hideMapL3(MapData.M1722, 101);}
            else if (M1NNNlrq[101] != null && !M1NNNlrqs[101]){cancelRequest(M1NNNlrq[101].ID, M1NNNlrq, 101);}
            if (M1NNNim[102] != null){hideMapL3(MapData.M1723, 102);}
            else if (M1NNNlrq[102] != null && !M1NNNlrqs[102]){cancelRequest(M1NNNlrq[102].ID, M1NNNlrq, 102);}
            if (M1NNNim[103] != null){hideMapL3(MapData.M1724, 103);}
            else if (M1NNNlrq[103] != null && !M1NNNlrqs[103]){cancelRequest(M1NNNlrq[103].ID, M1NNNlrq, 103);}
            if (M1NNNim[104] != null){hideMapL3(MapData.M1731, 104);}
            else if (M1NNNlrq[104] != null && !M1NNNlrqs[104]){cancelRequest(M1NNNlrq[104].ID, M1NNNlrq, 104);}
            if (M1NNNim[105] != null){hideMapL3(MapData.M1732, 105);}
            else if (M1NNNlrq[105] != null && !M1NNNlrqs[105]){cancelRequest(M1NNNlrq[105].ID, M1NNNlrq, 105);}
            if (M1NNNim[106] != null){hideMapL3(MapData.M1733, 106);}
            else if (M1NNNlrq[106] != null && !M1NNNlrqs[106]){cancelRequest(M1NNNlrq[106].ID, M1NNNlrq, 106);}
            if (M1NNNim[107] != null){hideMapL3(MapData.M1734, 107);}
            else if (M1NNNlrq[107] != null && !M1NNNlrqs[107]){cancelRequest(M1NNNlrq[107].ID, M1NNNlrq, 107);}
            if (M1NNNim[108] != null){hideMapL3(MapData.M1741, 108);}
            else if (M1NNNlrq[108] != null && !M1NNNlrqs[108]){cancelRequest(M1NNNlrq[108].ID, M1NNNlrq, 108);}
            if (M1NNNim[109] != null){hideMapL3(MapData.M1742, 109);}
            else if (M1NNNlrq[109] != null && !M1NNNlrqs[109]){cancelRequest(M1NNNlrq[109].ID, M1NNNlrq, 109);}
            if (M1NNNim[110] != null){hideMapL3(MapData.M1743, 110);}
            else if (M1NNNlrq[110] != null && !M1NNNlrqs[110]){cancelRequest(M1NNNlrq[110].ID, M1NNNlrq, 110);}
            if (M1NNNim[111] != null){hideMapL3(MapData.M1744, 111);}
            else if (M1NNNlrq[111] != null && !M1NNNlrqs[111]){cancelRequest(M1NNNlrq[111].ID, M1NNNlrq, 111);}
        }
        if (intersectsRegion(wnes, MapData.M1800region)){
            if (intersectsRegion(wnes, MapData.M1810region)){
		if (M1NNNim[112] == null){
		    if (intersectsRegion(wnes, MapData.M1811region)){showMapL3(MapData.M1811, 112);}
                 else if (M1NNNlrq[112] != null && !M1NNNlrqs[112]){cancelRequest(M1NNNlrq[112].ID, M1NNNlrq, 112);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1811region)){hideMapL3(MapData.M1811, 112);}
		}
		if (M1NNNim[113] == null){
		    if (intersectsRegion(wnes, MapData.M1812region)){showMapL3(MapData.M1812, 113);}
                 else if (M1NNNlrq[113] != null && !M1NNNlrqs[113]){cancelRequest(M1NNNlrq[113].ID, M1NNNlrq, 113);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1812region)){hideMapL3(MapData.M1812, 113);}
		}
		if (M1NNNim[114] == null){
		    if (intersectsRegion(wnes, MapData.M1813region)){showMapL3(MapData.M1813, 114);}
                 else if (M1NNNlrq[114] != null && !M1NNNlrqs[114]){cancelRequest(M1NNNlrq[114].ID, M1NNNlrq, 114);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1813region)){hideMapL3(MapData.M1813, 114);}
		}
		if (M1NNNim[115] == null){
		    if (intersectsRegion(wnes, MapData.M1814region)){showMapL3(MapData.M1814, 115);}
                 else if (M1NNNlrq[115] != null && !M1NNNlrqs[115]){cancelRequest(M1NNNlrq[115].ID, M1NNNlrq, 115);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1814region)){hideMapL3(MapData.M1814, 115);}
		}
            }
            else {
              if (M1NNNim[112] != null){hideMapL3(MapData.M1811, 112);}
              else if (M1NNNlrq[112] != null && !M1NNNlrqs[112]){cancelRequest(M1NNNlrq[112].ID, M1NNNlrq, 112);}
              if (M1NNNim[113] != null){hideMapL3(MapData.M1812, 113);}
              else if (M1NNNlrq[113] != null && !M1NNNlrqs[113]){cancelRequest(M1NNNlrq[113].ID, M1NNNlrq, 113);}
              if (M1NNNim[114] != null){hideMapL3(MapData.M1813, 114);}
              else if (M1NNNlrq[114] != null && !M1NNNlrqs[114]){cancelRequest(M1NNNlrq[114].ID, M1NNNlrq, 114);}
              if (M1NNNim[115] != null){hideMapL3(MapData.M1814, 115);}
              else if (M1NNNlrq[115] != null && !M1NNNlrqs[115]){cancelRequest(M1NNNlrq[115].ID, M1NNNlrq, 115);}
            }
            if (intersectsRegion(wnes, MapData.M1820region)){
		if (M1NNNim[116] == null){
		    if (intersectsRegion(wnes, MapData.M1821region)){showMapL3(MapData.M1821, 116);}
                 else if (M1NNNlrq[116] != null && !M1NNNlrqs[116]){cancelRequest(M1NNNlrq[116].ID, M1NNNlrq, 116);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1821region)){hideMapL3(MapData.M1821, 116);}
		}
		if (M1NNNim[117] == null){
		    if (intersectsRegion(wnes, MapData.M1822region)){showMapL3(MapData.M1822, 117);}
                 else if (M1NNNlrq[117] != null && !M1NNNlrqs[117]){cancelRequest(M1NNNlrq[117].ID, M1NNNlrq, 117);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1822region)){hideMapL3(MapData.M1822, 117);}
		}
		if (M1NNNim[118] == null){
		    if (intersectsRegion(wnes, MapData.M1823region)){showMapL3(MapData.M1823, 118);}
                 else if (M1NNNlrq[118] != null && !M1NNNlrqs[118]){cancelRequest(M1NNNlrq[118].ID, M1NNNlrq, 118);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1823region)){hideMapL3(MapData.M1823, 118);}
		}
		if (M1NNNim[119] == null){
		    if (intersectsRegion(wnes, MapData.M1824region)){showMapL3(MapData.M1824, 119);}
                 else if (M1NNNlrq[119] != null && !M1NNNlrqs[119]){cancelRequest(M1NNNlrq[119].ID, M1NNNlrq, 119);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1824region)){hideMapL3(MapData.M1824, 119);}
		}
            }
            else {
              if (M1NNNim[116] != null){hideMapL3(MapData.M1821, 116);}
              else if (M1NNNlrq[116] != null && !M1NNNlrqs[116]){cancelRequest(M1NNNlrq[116].ID, M1NNNlrq, 116);}
              if (M1NNNim[117] != null){hideMapL3(MapData.M1822, 117);}
              else if (M1NNNlrq[117] != null && !M1NNNlrqs[117]){cancelRequest(M1NNNlrq[117].ID, M1NNNlrq, 117);}
              if (M1NNNim[118] != null){hideMapL3(MapData.M1823, 118);}
              else if (M1NNNlrq[118] != null && !M1NNNlrqs[118]){cancelRequest(M1NNNlrq[118].ID, M1NNNlrq, 118);}
              if (M1NNNim[119] != null){hideMapL3(MapData.M1824, 119);}
              else if (M1NNNlrq[119] != null && !M1NNNlrqs[119]){cancelRequest(M1NNNlrq[119].ID, M1NNNlrq, 119);}
            }
            if (intersectsRegion(wnes, MapData.M1830region)){
		if (M1NNNim[120] == null){
		    if (intersectsRegion(wnes, MapData.M1831region)){showMapL3(MapData.M1831, 120);}
                 else if (M1NNNlrq[120] != null && !M1NNNlrqs[120]){cancelRequest(M1NNNlrq[120].ID, M1NNNlrq, 120);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1831region)){hideMapL3(MapData.M1831, 120);}
		}
		if (M1NNNim[121] == null){
		    if (intersectsRegion(wnes, MapData.M1832region)){showMapL3(MapData.M1832, 121);}
                 else if (M1NNNlrq[121] != null && !M1NNNlrqs[121]){cancelRequest(M1NNNlrq[121].ID, M1NNNlrq, 121);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1832region)){hideMapL3(MapData.M1832, 121);}
		}
		if (M1NNNim[122] == null){
		    if (intersectsRegion(wnes, MapData.M1833region)){showMapL3(MapData.M1833, 122);}
                 else if (M1NNNlrq[122] != null && !M1NNNlrqs[122]){cancelRequest(M1NNNlrq[122].ID, M1NNNlrq, 122);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1833region)){hideMapL3(MapData.M1833, 122);}
		}
		if (M1NNNim[123] == null){
		    if (intersectsRegion(wnes, MapData.M1834region)){showMapL3(MapData.M1834, 123);}
                 else if (M1NNNlrq[123] != null && !M1NNNlrqs[123]){cancelRequest(M1NNNlrq[123].ID, M1NNNlrq, 123);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1834region)){hideMapL3(MapData.M1834, 123);}
		}
            }
            else {
              if (M1NNNim[120] != null){hideMapL3(MapData.M1831, 120);}
              else if (M1NNNlrq[120] != null && !M1NNNlrqs[120]){cancelRequest(M1NNNlrq[120].ID, M1NNNlrq, 120);}
              if (M1NNNim[121] != null){hideMapL3(MapData.M1832, 121);}
              else if (M1NNNlrq[121] != null && !M1NNNlrqs[121]){cancelRequest(M1NNNlrq[121].ID, M1NNNlrq, 121);}
              if (M1NNNim[122] != null){hideMapL3(MapData.M1833, 122);}
              else if (M1NNNlrq[122] != null && !M1NNNlrqs[122]){cancelRequest(M1NNNlrq[122].ID, M1NNNlrq, 122);}
              if (M1NNNim[123] != null){hideMapL3(MapData.M1834, 123);}
              else if (M1NNNlrq[123] != null && !M1NNNlrqs[123]){cancelRequest(M1NNNlrq[123].ID, M1NNNlrq, 123);}
            }
            if (intersectsRegion(wnes, MapData.M1840region)){
		if (M1NNNim[124] == null){
		    if (intersectsRegion(wnes, MapData.M1841region)){showMapL3(MapData.M1841, 124);}
                 else if (M1NNNlrq[124] != null && !M1NNNlrqs[124]){cancelRequest(M1NNNlrq[124].ID, M1NNNlrq, 124);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1841region)){hideMapL3(MapData.M1841, 124);}
		}
		if (M1NNNim[125] == null){
		    if (intersectsRegion(wnes, MapData.M1842region)){showMapL3(MapData.M1842, 125);}
                 else if (M1NNNlrq[125] != null && !M1NNNlrqs[125]){cancelRequest(M1NNNlrq[125].ID, M1NNNlrq, 125);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1842region)){hideMapL3(MapData.M1842, 125);}
		}
		if (M1NNNim[126] == null){
		    if (intersectsRegion(wnes, MapData.M1843region)){showMapL3(MapData.M1843, 126);}
                 else if (M1NNNlrq[126] != null && !M1NNNlrqs[126]){cancelRequest(M1NNNlrq[126].ID, M1NNNlrq, 126);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1843region)){hideMapL3(MapData.M1843, 126);}
		}
		if (M1NNNim[127] == null){
		    if (intersectsRegion(wnes, MapData.M1844region)){showMapL3(MapData.M1844, 127);}
                 else if (M1NNNlrq[127] != null && !M1NNNlrqs[127]){cancelRequest(M1NNNlrq[127].ID, M1NNNlrq, 127);}
		}
		else {
		    if (!intersectsRegion(wnes, MapData.M1844region)){hideMapL3(MapData.M1844, 127);}
		}
            }
            else {
              if (M1NNNim[124] != null){hideMapL3(MapData.M1841, 124);}
              else if (M1NNNlrq[124] != null && !M1NNNlrqs[124]){cancelRequest(M1NNNlrq[124].ID, M1NNNlrq, 124);}
              if (M1NNNim[125] != null){hideMapL3(MapData.M1842, 125);}
              else if (M1NNNlrq[125] != null && !M1NNNlrqs[125]){cancelRequest(M1NNNlrq[125].ID, M1NNNlrq, 125);}
              if (M1NNNim[126] != null){hideMapL3(MapData.M1843, 126);}
              else if (M1NNNlrq[126] != null && !M1NNNlrqs[126]){cancelRequest(M1NNNlrq[126].ID, M1NNNlrq, 126);}
              if (M1NNNim[127] != null){hideMapL3(MapData.M1844, 127);}
              else if (M1NNNlrq[127] != null && !M1NNNlrqs[127]){cancelRequest(M1NNNlrq[127].ID, M1NNNlrq, 127);}
            }
        }
        else {
            if (M1NNNim[112] != null){hideMapL3(MapData.M1811, 112);}
            else if (M1NNNlrq[112] != null && !M1NNNlrqs[112]){cancelRequest(M1NNNlrq[112].ID, M1NNNlrq, 112);}
            if (M1NNNim[113] != null){hideMapL3(MapData.M1812, 113);}
            else if (M1NNNlrq[113] != null && !M1NNNlrqs[113]){cancelRequest(M1NNNlrq[113].ID, M1NNNlrq, 113);}
            if (M1NNNim[114] != null){hideMapL3(MapData.M1813, 114);}
            else if (M1NNNlrq[114] != null && !M1NNNlrqs[114]){cancelRequest(M1NNNlrq[114].ID, M1NNNlrq, 114);}
            if (M1NNNim[115] != null){hideMapL3(MapData.M1814, 115);}
            else if (M1NNNlrq[115] != null && !M1NNNlrqs[115]){cancelRequest(M1NNNlrq[115].ID, M1NNNlrq, 115);}
            if (M1NNNim[116] != null){hideMapL3(MapData.M1821, 116);}
            else if (M1NNNlrq[116] != null && !M1NNNlrqs[116]){cancelRequest(M1NNNlrq[116].ID, M1NNNlrq, 116);}
            if (M1NNNim[117] != null){hideMapL3(MapData.M1822, 117);}
            else if (M1NNNlrq[117] != null && !M1NNNlrqs[117]){cancelRequest(M1NNNlrq[117].ID, M1NNNlrq, 117);}
            if (M1NNNim[118] != null){hideMapL3(MapData.M1823, 118);}
            else if (M1NNNlrq[118] != null && !M1NNNlrqs[118]){cancelRequest(M1NNNlrq[118].ID, M1NNNlrq, 118);}
            if (M1NNNim[119] != null){hideMapL3(MapData.M1824, 119);}
            else if (M1NNNlrq[119] != null && !M1NNNlrqs[119]){cancelRequest(M1NNNlrq[119].ID, M1NNNlrq, 119);}
            if (M1NNNim[120] != null){hideMapL3(MapData.M1831, 120);}
            else if (M1NNNlrq[120] != null && !M1NNNlrqs[120]){cancelRequest(M1NNNlrq[120].ID, M1NNNlrq, 120);}
            if (M1NNNim[121] != null){hideMapL3(MapData.M1832, 121);}
            else if (M1NNNlrq[121] != null && !M1NNNlrqs[121]){cancelRequest(M1NNNlrq[121].ID, M1NNNlrq, 121);}
            if (M1NNNim[122] != null){hideMapL3(MapData.M1833, 122);}
            else if (M1NNNlrq[122] != null && !M1NNNlrqs[122]){cancelRequest(M1NNNlrq[122].ID, M1NNNlrq, 122);}
            if (M1NNNim[123] != null){hideMapL3(MapData.M1834, 123);}
            else if (M1NNNlrq[123] != null && !M1NNNlrqs[123]){cancelRequest(M1NNNlrq[123].ID, M1NNNlrq, 123);}
            if (M1NNNim[124] != null){hideMapL3(MapData.M1841, 124);}
            else if (M1NNNlrq[124] != null && !M1NNNlrqs[124]){cancelRequest(M1NNNlrq[124].ID, M1NNNlrq, 124);}
            if (M1NNNim[125] != null){hideMapL3(MapData.M1842, 125);}
            else if (M1NNNlrq[125] != null && !M1NNNlrqs[125]){cancelRequest(M1NNNlrq[125].ID, M1NNNlrq, 125);}
            if (M1NNNim[126] != null){hideMapL3(MapData.M1843, 126);}
            else if (M1NNNlrq[126] != null && !M1NNNlrqs[126]){cancelRequest(M1NNNlrq[126].ID, M1NNNlrq, 126);}
            if (M1NNNim[127] != null){hideMapL3(MapData.M1844, 127);}
            else if (M1NNNlrq[127] != null && !M1NNNlrqs[127]){cancelRequest(M1NNNlrq[127].ID, M1NNNlrq, 127);}
        }
    }


}
