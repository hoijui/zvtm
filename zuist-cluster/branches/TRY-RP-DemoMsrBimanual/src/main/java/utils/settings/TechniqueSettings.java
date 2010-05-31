package utils.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.lri.insitu.wild.wildinputserver.remote.WILDInputServerRemoteClient;

import utils.settings.TechniqueSettingsManager.Dimensions;
import utils.settings.TechniqueSettingsManager.GestureType;
import utils.settings.TechniqueSettingsManager.Hands;

public class TechniqueSettings {
	
	public static final String SINGLE_TRACKER_COMMAND = "/Users/wild/Applications/VICON/screen_calibration2/tracking/single_track ";
	public static final String SINGLE_TRACKER_OBJECTS = "/Users/wild/Applications/VICON/screen_calibration2/objects/";
	
	public static final String TRACKER_END = "";
	
	// Problmes avec l'ancien traqueur. Si on laisse tout en adresses absolues, il n'envoie pas ˆ la bonne adresse OSC.
	public static final String MULTI_TRACKER_COMMAND = "./tracking/multiple_track ";
	public static final String MULTI_TRACKER_OBJECTS = "old_objects/";
	
	// public static final String WIS_LAUNCH = "./runWILDInputServer.sh -f 'IC Files/";
	public static final String WIS_START = "IC Files/zoom&pan/";
	public static final String WIS_END = ".ic";
	
	public static WILDInputServerRemoteClient remoteClient;
	
	static {
		
		try {
			remoteClient = new WILDInputServerRemoteClient(InetAddress.getByName("192.168.0.1"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	protected Hands hands;
	protected GestureType gestureType;
	protected Dimensions dimensions;
	
	protected String zoomTechnique;
	protected String pointTechnique;
	protected String panTechnique;
	
	protected String wildInputServerConfFile;
	protected String viconTrackedObjects[];
	protected String trackerXmlFiles[];
	
	protected boolean multiTrack;
	
	protected String trackerCommand;//, previousTrackerCommand = "";
	protected Process trackerProcess;
	
	protected String wisCommand;
	protected Process wisProcess;
	
	
	public TechniqueSettings(
			Hands h, GestureType gt, Dimensions d, 
			String zoomTech, String pointTech, String panTech,
			String wisConfFile, String[] viconObjects, String[] xmlFiles, boolean multi) {
		
		hands = h;
		dimensions = d;
		gestureType = gt;
		
		zoomTechnique = zoomTech;
		pointTechnique = pointTech;
		panTechnique = panTech;
		
		wildInputServerConfFile = wisConfFile;
		viconTrackedObjects = viconObjects;
		trackerXmlFiles = xmlFiles;
		
		multiTrack = multi;
		
		if (multiTrack) {
			
			trackerCommand = MULTI_TRACKER_COMMAND;
			
			for (String file : trackerXmlFiles) {
				
				trackerCommand += MULTI_TRACKER_OBJECTS + file + " ";
				
			}
			
		} else {
			
			trackerCommand = SINGLE_TRACKER_COMMAND + SINGLE_TRACKER_OBJECTS + trackerXmlFiles[0];
			
		}
		
		trackerCommand += TRACKER_END;
		
		wisCommand = WIS_START + wisConfFile + WIS_END;
		
	}
	
	public String getOperatorInstructions() {
		
		String result = "============ Instructions =============\n";
			
		result += "VICON objects : \n";
		
		for (String vo : viconTrackedObjects) {
			
			result += "\t- " + vo + "\n";
			
		}
		
//		result += "XML file(s) : \n";
//		
//		for (String xf : trackerXmlFiles) {
//			
//			result += "\t- " + xf + "\n";
//			
//		}
		
		// result += "Configuration : " + wildInputServerConfFile + "\n";
		
		// if (trackerCommand.equalsIgnoreCase(previousTrackerCommand)) {
			result += "Tracker command : " + trackerCommand + "\n";
		// } else {
		// 	result += ">>>>>>>>>>>>>     Tracker command : " + trackerCommand + "      <<<<<<<<<<<<<<<\n";
		// }
																 
			result += "=======================================\n";
		
		
		return result;
		
	}
	
	public String getTrackerCommand() {
		return trackerCommand;
	}
	
	public String getWISCommand() {
		return wisCommand;
	}
	
	public boolean callCommands() {
		
		/*
		try {
		
			Runtime rt = Runtime.getRuntime();
	        trackerProcess = rt.exec(trackerCommand);
			
			
			System.out.println("Calling " + trackerCommand);
	        
			/*
	        InputStream stderr = trackerProcess.getErrorStream();
	        InputStreamReader isr = new InputStreamReader(stderr);
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        System.out.println("<TrackerError>");
	        while ( (line = br.readLine()) != null)
	            System.out.println(line);
	        System.out.println("</TrackerError>");
			*/
	        
        /*
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		*/
		
		return remoteClient.loadLocal(wisCommand, true);
		
	}
	
	public void killProcesses() {

		if (trackerProcess != null) {
//			trackerProcess.destroy();
//			System.out.println("Destroying the tracker process");
		} else {
//			System.out.println("Tracker process null");
		}
		
		remoteClient.stop();
		
	}
	
	public String getZoomTechnique() {
		return zoomTechnique;
	}
	
	public String getPanTechnique() {
		return panTechnique;
	}
	
	public String getPointTechnique() {
		return pointTechnique;
	}
	
	public String getWildInputServerConfFile() {
		return wildInputServerConfFile;
	}
	
	public String[] getViconTrackedObjects() {
		return viconTrackedObjects;
	}
	
	public String[] getTrackerXmlFiles() {
		return trackerXmlFiles;
	}
	
	
}
