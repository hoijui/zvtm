/*   FILE: BiManPlugin.java
 *   DATE OF CREATION:  Thu May 26 16:42:33 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: BiManPlugin.java,v 1.8 2005/06/02 12:45:04 skbo Exp $
 */ 

package net.claribole.zgrviewer.mpd;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.swing.JFrame;

import net.claribole.zgrviewer.*;
import net.claribole.zvtm.mpd.MPDManager;
import net.claribole.zvtm.mpd.MPDAppEventHandler;

import domino.Socket;

public class BiManPlugin implements Plugin {
    
    static String _NAME = "Bi-manual Interaction Plugin";
    static String _VERSION = "0.1";
    static String _AUTHOR = "Emmanuel Pietriga";
    static URL _URL;
    static {
	try {_URL = new URL("http://zvtm.sourceforge.net/zgrviewer.html");}
	catch (MalformedURLException ex){}
    }
    
    ZGRViewer application;
    MPDAppEventHandler evH;
    MPDManager mpdMngr;
    Socket nonDominantSocket;


    static String NON_DOMINANT_HAND_DEVICE_NAME_LABEL = "NonDominantHandDeviceName";
    static String NON_DOMINANT_HAND_DEVICE_SENSITIVITY_LABEL = "NonDominantHandDeviceSensitivity";
    String NON_DOMINANT_HAND_DEVICE_NAME = null;
    float NON_DOMINANT_HAND_DEVICE_SENSITIVITY = 1;

    public BiManPlugin(){}
    
    public void setApplication(ZGRViewer app){
	this.application = app;
	evH = new ZgrvBiManHdlr(this.application, this);
	mpdMngr = new MPDManager(evH, this.application.mainView);
	
    }

    protected void listenTo(String deviceName){
	if (mpdMngr.getAvailablePointingDevices() != null){
	    Socket[] sockets = mpdMngr.getAvailablePointingDevices();
	    for (int i = 0; i < sockets.length; i++){
		//add a listener for device whose name is deviceName
		if (sockets[i].getDeviceDescriptor().getName().equals(deviceName)){
            if (sockets[i] != nonDominantSocket){
                if(nonDominantSocket != null)
                    mpdMngr.removeListener(nonDominantSocket);
			mpdMngr.addListener(sockets[i]);
			nonDominantSocket = sockets[i];
		    }
		    break;
		}
	    }
	}
    }

    public void loadPreferences(Hashtable settings){
	if (settings != null){
	    try {
		setNonDominantHandDevice((String)settings.get(NON_DOMINANT_HAND_DEVICE_NAME_LABEL));
	    }
	    catch (Exception ex){}
	    try {
		setNonDominantHandDeviceSensitivity(Float.parseFloat((String)settings.get(NON_DOMINANT_HAND_DEVICE_SENSITIVITY_LABEL)));
	    }
	    catch (Exception ex){}
	}
    }

    protected void setNonDominantHandDevice(String deviceName){
	if (deviceName != null && deviceName.length() > 0){
	    NON_DOMINANT_HAND_DEVICE_NAME = deviceName;
	}
	else {
	    NON_DOMINANT_HAND_DEVICE_NAME = null;
	}
	if (deviceName != null){
	    listenTo(NON_DOMINANT_HAND_DEVICE_NAME);
	}
    }
    
    protected void setNonDominantHandDeviceSensitivity(float f){
	NON_DOMINANT_HAND_DEVICE_SENSITIVITY = f;
    }

    public Hashtable savePreferences(){
	Hashtable res = new Hashtable();
	res.put(NON_DOMINANT_HAND_DEVICE_NAME_LABEL, (NON_DOMINANT_HAND_DEVICE_NAME!=null) ? NON_DOMINANT_HAND_DEVICE_NAME : "");
	res.put(NON_DOMINANT_HAND_DEVICE_SENSITIVITY_LABEL, Float.toString(NON_DOMINANT_HAND_DEVICE_SENSITIVITY));
	return res;
    }

    public void showSettings(){
	   new BiManSettings(this);
    }

    public void terminate(){
	mpdMngr.terminate();
    }
    
    public String getAuthor(){return _AUTHOR;}
    
    public String getName(){return _NAME;}

    public String getVersion(){return _VERSION;}

    public URL getURL(){return _URL;}
    
}
