package utils.settings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TechniqueSettingsManager {
	
	public enum Hands { One, Two }
	public enum GestureType { Linear, Circular }
	public enum Dimensions { OneD, TwoD, ThreeD }
	
	public static final String SettingsFile = "src/TechniqueSettings";
	
	protected TechniqueSettings[][][] settings = new TechniqueSettings[Hands.values().length][GestureType.values().length][Dimensions.values().length];
	protected TechniqueSettings currentSettings;
	
	public TechniqueSettingsManager() {
		
		String line;
		
		//lecture du fichier texte	
		try{
			
			InputStream ips=new FileInputStream(SettingsFile); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);

			while ((line=br.readLine())!=null){
				
				// Start of a technique settings description
				if ( line.contains("[") ) {
					
					Hands hands = null;
					GestureType gestureType = null;
					Dimensions dimensions = null;
					
					Pattern p = Pattern.compile("\\[([^\\]]+)\\]"); 
					Matcher m = p.matcher(line);
					
					// For each instance of "[...]"
					m.find();
					String h = m.group(1); 
					m.find();
					String gt = m.group(1);
					m.find();
					String d = m.group(1);
					
					if (h.equalsIgnoreCase("One")) {
						hands = Hands.One;
					} else if (h.equalsIgnoreCase("Two")) {
						hands = Hands.Two;
					} else {
						System.err.println("Unknown Hands !");
					}
					
					if (gt.equalsIgnoreCase("Linear")) {
						gestureType = GestureType.Linear;
					} else if (gt.equalsIgnoreCase("Circular")) {
						gestureType = GestureType.Circular;
					} else {
						System.err.println("Unknown Gesture Type !");
					}
					
					if (d.equalsIgnoreCase("OneD")) {
						dimensions = Dimensions.OneD;
					} else if (d.equalsIgnoreCase("TwoD")) {
						dimensions = Dimensions.TwoD;
					} else if (d.equalsIgnoreCase("ThreeD")) {
						dimensions = Dimensions.ThreeD;
					} else {
						System.err.println("Unknown Dimensions !");
					}
					
					// The data about this configuration are in the 6 following lines
					
					String zoomTech = null;
					String pointTech = null;
					String panTech = null;
					
					String wisConfFile = "";
					String[] viconObjects = null;
					String[] xmlFiles = null;
					
					
					// 0 : Zoom
					line = br.readLine();
					p = Pattern.compile("Zoom class\\s*:\\s([^\\s]+)");
					m = p.matcher(line);
					// m.find(); System.out.println(m.group(1));
					
					if (m.find()) {
						zoomTech = m.group(1); 
					} else {
						System.err.println("No Zoom class found !");
					}
					
					// 1 : Point
					line = br.readLine();
					p = Pattern.compile("\\s*Point class\\s*:\\s([^\\s]+)");
					m = p.matcher(line);
					// m.find();
					
					if (m.find()) {
						pointTech = m.group(1); 
					} else {
						System.err.println("No Point class found !");
					}
					
					// 2 : Pan
					line = br.readLine();
					p = Pattern.compile("\\s*Pan class\\s*:\\s([^\\s]+)");
					m = p.matcher(line);
					// m.find();
					
					if (m.find()) {
						panTech = m.group(1); 
					} else {
						System.err.println("No Pan class found !");
					}
					
					// 3 : WIS Configuration File
					line = br.readLine();
					p = Pattern.compile("\\s*WildInputServer conf\\s:\\s([^\\s]+)");
					m = p.matcher(line);
					// m.find();
					
					if (m.find()) {
						wisConfFile = m.group(1);
					} else {
						System.err.println("No WIS conf. file found !");
					}
					
					// 4 : VICON tracked object
					line = br.readLine();
					p = Pattern.compile("Tracked object.*:\\s*([^\n]+)");
					m = p.matcher(line);
					// m.find();
					
					if (m.find()) {
						viconObjects = m.group(1).split(", ");
					} else {
						System.err.println("No VICON objects found !");
					}
					
					// 5 : XML files
					line = br.readLine();
					p = Pattern.compile("xml file.*:\\s*([^\n]+)");
					m = p.matcher(line);
					// m.find();
					
					if (m.find()) {
						xmlFiles = m.group(1).split(", ");
					} else {
						System.err.println("No XML files found !");
					}
					
					// Creation of the settings object and filling the array
					
					System.out.println("Creating " + hands.toString() + " " + gestureType.toString() + " " + dimensions.toString());
					System.out.println("(" + hands.ordinal() + ", " + gestureType.ordinal() + ", " + dimensions.ordinal() + ")");
					System.out.println("\tWith " + zoomTech + ", " + pointTech + ", " + panTech + " and " + wisConfFile + ", " + viconObjects.length + " objects and " + xmlFiles.length + " xml files.");
					
					settings[hands.ordinal()][gestureType.ordinal()][dimensions.ordinal()] = new TechniqueSettings(
							hands, gestureType, dimensions,
							zoomTech, pointTech, panTech,
							wisConfFile, viconObjects, xmlFiles,
							xmlFiles.length > 1
					);
					
					// System.out.println("Tracker command : " + settings[hands.ordinal()][gestureType.ordinal()][dimensions.ordinal()].getTrackerCommand());
					// System.out.println("Instructions : \n" + settings[hands.ordinal()][gestureType.ordinal()][dimensions.ordinal()].getOperatorInstructions());
					// System.out.println();
					
				}
				
			}
			
			br.close(); 
		}		
		catch (Exception e){
			e.printStackTrace();
		}

		
	}
	
	public TechniqueSettings getSetting(Hands h, GestureType gt, Dimensions d) {
		return currentSettings = settings[h.ordinal()][gt.ordinal()][d.ordinal()];
	}
	
	public TechniqueSettings getSetting(int h, int gt, int d) {
		return currentSettings = settings[h][gt][d];
	}
	
	public TechniqueSettings getCurrentSettings() {
		return currentSettings;
	}
	
}
