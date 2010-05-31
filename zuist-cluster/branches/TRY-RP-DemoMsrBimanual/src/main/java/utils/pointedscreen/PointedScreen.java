package utils.pointedscreen;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

public class PointedScreen {

	public final static String CONF_FILE = "pointed screen configuration file";
	public final static String NETWORK = ".wild.lri.fr";
	
	String confFile = "";
	LinkedList<Display> displays = new LinkedList<Display>();
	
	public PointedScreen(String confFile) {
		
		this.confFile = confFile;
		loadConfFile();
		
	}

	private void loadConfFile() {
		if (confFile != null && !confFile.equals("")) {
			File f = new File(confFile);
			if (f.exists()) {
				try {
					displays.clear();
					BufferedReader in = new BufferedReader(new FileReader(f));
					String line = null;
					int nl = 0;
					String name = "", host = "";
					int w = 0, h = 0;
					Vector3D orig = null, ox = null, oy = null, oz = null;
					String[] s;
					while ((line = in.readLine()) != null) {
						if (nl == 0) {
							s = line.split(" ");
							host = s[0]; name = s[1];
							nl++;
						} else if (nl == 1) {
							s = line.split(" ");
							w = Integer.parseInt(s[0]); h = Integer.parseInt(s[1]);
							nl++;
						} else if (nl == 2) {
							s = line.split(" ");
							orig = new Vector3D(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
							nl++;
						} else if (nl == 3) {
							s = line.split(" ");
							ox = new Vector3D(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
							nl++;
						} else if (nl == 4) {
							s = line.split(" ");
							oy = new Vector3D(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
							nl++;
						} else if (nl == 5) {
							s = line.split(" ");
							oz = new Vector3D(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
							nl = 0;
							DisplayType apple = new DisplayType("Apple HD 30", 0, 0, 0, 0, new Dimension(w, h));
							LocalSystem calib = new LocalSystem(orig, ox, oy, oz);
							displays.add(new Display(host+name, apple, 0, 0, 0, 0, calib));
						}
					}
				} catch (Exception e) {
					System.out.println("***ERROR (PLUGIN POINTED SCREEN): ERROR WHILE READING CONFIGURATION FILE.");	
				}
			} else {
				System.out.println("***WARNING (PLUGIN POINTED SCREEN): CONFIGURATION FILE DOES NOT EXIST.");
			}
		} else {
			System.out.println("***WARNING (PLUGIN POINTED SCREEN): NO CONFIGURATION FILE.");
		}
		System.out.println("PLUGIN pointed screen added " + displays.size() + " displays");		
	}
	
	public double[] computeProjectedCoordinates(double x, double y, double z, double dx, double dy, double dz) {
		
		double[] result = new double[3];
		
		//compute right screen
		Vector3D position = new Vector3D(x, y, z);
		Vector3D direction = new Vector3D(dx, dy, dz);
		
		Display fDisplay = null;
		Vector3D pointed = null;
		
		for (Display display : displays) {
			
			//Vector3D screenPosition = display.getLocalSystem().globalPointToLocal(position);
			//Vector3D screenDirection = display.getLocalSystem().globalVectorToLocal(direction);
			
			LocalSystem ls = new LocalSystem(display.getLocalSystem().getOrigin(), display.getLocalSystem().getOx(), display.getLocalSystem().getOy(), direction);
			pointed = ls.globalPointToLocal(position);
			
			if (pointed.getX() >= 0 && pointed.getX() < display.getType().getResolution().width
					&& pointed.getY() >= 0 && pointed.getY() < display.getType().getResolution().height) {
				fDisplay = display;
				break;
			}
			
		}
		
		if (fDisplay != null && pointed != null) {
			
//			Screen oEvent = new Screen();
//			oEvent.setSlotHost(fDisplay.getMachine().getAddress().getHostName() + PointedScreen.NETWORK);
//			oEvent.setSlotName(fDisplay.getMachine().getName() + fDisplay.getName());
//			oEvent.setSlotX((int)Math.round(pointed.getX()));
//			oEvent.setSlotY((int)Math.round(pointed.getY()));
//			oEvent.setSlotZ(pointed.getZ());
//			fireEvent(oEvent);
			
			//norm = new Double(message.getArguments()[DIST].toString()).doubleValue();
			
			String screenName = fDisplay.getName();
			
			// System.out.println(screenName);
			
			double screenX = pointed.getX();
			double screenY = pointed.getY();
			
			int screenCoordsX = -1;
			int screenCoordsY = -1;
			
			if (screenName.contains("A"))
				screenCoordsX = 0;
			else if (screenName.contains("B"))
				screenCoordsX = 2;
			else if (screenName.contains("C"))
				screenCoordsX = 4;
			else if (screenName.contains("D"))
				screenCoordsX = 6;
			
			if (screenName.contains("R"))
				screenCoordsX ++;
			
			screenCoordsY = new Integer(""+screenName.charAt(1)).intValue();
			
			result[0] = screenCoordsX * fDisplay.getLocalBounds().width + screenX;
			result[1] = screenCoordsY * fDisplay.getLocalBounds().height + screenY;
			result[2] = pointed.getZ();
			
			return result;
			
		}
		
		return null;
	}

}
