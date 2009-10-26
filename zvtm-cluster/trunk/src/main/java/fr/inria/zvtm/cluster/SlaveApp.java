package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Generic slave application.
 * Instantiates a View, then hands off control to a SlaveUpdater.
 */
public class SlaveApp {
	private final SlaveOptions options;
	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; //shortcut
	private View view = null; //local view
	private ClusteredView clusteredView = null;

	SlaveApp(SlaveOptions options){
		this.options = options;
		vsm.setDebug(options.debug);
	}

	public static void main(String[] args){
		SlaveOptions options = new SlaveOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		if(options.help){
			System.err.println("Usage: SlaveApp [options] where options are: ");
			parser.printUsage(System.err);
			return;
		}

		SlaveApp app = new SlaveApp(options);
		SlaveUpdater updater = new SlaveUpdater(options.appName,
				options.blockNumber);
		updater.setAppDelegate(app);
		updater.startOperation();
	}

	void createLocalView(ClusteredView cv){
		if(view != null){
			throw new IllegalStateException("local view already exists");
		}

		if(!cv.ownsBlock(options.blockNumber)){
			System.out.println("clusteredView does NOT own block " + options.blockNumber);
			return;
		}

		clusteredView = cv;

		view = vsm.addFrameView(cv.getCameras(), 
				"slaveView " + options.blockNumber, 
				View.STD_VIEW,
				cv.getBlockWidth(), 
				cv.getBlockHeight(), false, false, true, null);

		// inputs: block width, block height, fullscreen
		if(options.fullscreen){
			GraphicsEnvironment ge = 
				GraphicsEnvironment.getLocalGraphicsEnvironment();

			GraphicsDevice[] devices = ge.getScreenDevices();
			if(devices.length == 0){
				throw new Error("no screen devices found");
			}
			Map<String, GraphicsDevice> devMap = 
				new HashMap<String, GraphicsDevice>();

			System.out.print("available devices: ");
			for(GraphicsDevice d: devices) {
				devMap.put(d.getIDstring(), d);	
				System.out.print(d.getIDstring());
			}
			System.out.println("");
			GraphicsDevice device = null;
			if(!options.device.equals("")){
				device = devMap.get(options.device);
				if(null == device){
					System.out.println("Warning: could not find device named " + options.device);
				}
			}
			if(null == device){
				device = ge.getDefaultScreenDevice();
			}

			if (device.isFullScreenSupported()) {
				((JFrame)view.getFrame()).removeNotify();
				((JFrame)view.getFrame()).setUndecorated(true);
				device.setFullScreenWindow((JFrame)view.getFrame());
				((JFrame)view.getFrame()).addNotify();
			}
		}

		//XXX fix bgcolor
		view.setBackgroundColor(Color.BLACK);
		view.setVisible(true);
	}

	void setCameraLocation(Location masterLoc,
			Camera slaveCamera){
		if(clusteredView == null){
			slaveCamera.setLocation(masterLoc);
			return;
		}

		float focal = slaveCamera.getFocal();
		float altCoef = (focal + masterLoc.alt) / focal;
		//block (screen) width in virtualspace coords
		long virtBlockWidth = (long)(clusteredView.getBlockWidth() * altCoef);
		//block (screen) height in virtualspace coords
		long virtBlockHeight = (long)(clusteredView.getBlockHeight() * altCoef);	

		//row and col take origin block into account
		int viewRows = clusteredView.getViewRows();
		int viewCols = clusteredView.getViewCols();
		int row = clusteredView.rowNum(options.blockNumber) - clusteredView.rowNum(clusteredView.getOrigin()); 
		int col = clusteredView.colNum(options.blockNumber) - clusteredView.colNum(clusteredView.getOrigin()); 

		long xOffset = -((viewCols-1)*virtBlockWidth)/2; 
		long yOffset = ((viewRows-1)*virtBlockHeight)/2;

		long newX = xOffset + masterLoc.vx + col*virtBlockWidth;
		long newY = yOffset + masterLoc.vy - row*virtBlockHeight;

		slaveCamera.setLocation(new Location(newX, newY, masterLoc.alt));
	}
}

class SlaveOptions {
	@Option(name = "-b", aliases = {"--block"}, usage = "metacamera block number (slave index)")
		int blockNumber = 0;

	@Option(name = "-n", aliases = {"--app-name"}, usage = "application name (should match master program)")
		String appName = "zvtmApplication";

	@Option(name = "-f", aliases = {"--fullscreen"}, usage = "open in full screen mode")
		boolean fullscreen = false;	

	@Option(name = "-d", aliases = {"--device-name"}, usage = "use chosen device (fullscreen only)")
		String device = "";

	@Option(name = "-g", aliases = {"--debug"}, usage = "show ZVTM debug information")
		boolean debug = false;

	@Option(name = "-h", aliases = {"--help"}, usage = "print this help message and exit")
		boolean help = false;
}

