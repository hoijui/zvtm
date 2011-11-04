package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.AgileGLJPanelFactory;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
 * The generic slave application display the contents of one block
 * of a clustered view (i.e. one slave should be instantiated for
 * every block of the clustered view).
 * @see ClusteredView
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

    protected String getViewType(){
        //return options.openGl ? View.OPENGL_VIEW : View.STD_VIEW;
        return AgileGLJPanelFactory.AGILE_GLJ_VIEW;
    }

	void createLocalView(ClusteredView cv){
		if(!cv.ownsBlock(options.blockNumber)){
			return;
		}

		if(view != null){
			throw new IllegalStateException("local view already exists");
		}

		clusteredView = cv;

		view = vsm.addFrameView(cv.getCameras(), 
				"slaveView " + options.blockNumber, 
				getViewType(),
				cv.getClusterGeometry().getBlockWidth(), 
				cv.getClusterGeometry().getBlockHeight(), 
                false, false, !options.undecorated, null);
        view.setBackgroundColor(cv.getBackgroundColor());
        view.setListener(new SlaveEventHandler());
        view.getPanel().setRefreshRate(options.refreshPeriod);

        //move cameras to their 'proper' location
        for(Camera cam: clusteredView.getCameras()){
            setCameraLocation(cam.getLocation(), cam);
        }
        
        if (options.antialiasing){
            view.setAntialiasing(true);
        }

        if(!options.fullscreen){
            ((JFrame)view.getFrame()).setLocation(
            options.xOffset,
            options.yOffset);
        }

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
		view.setVisible(true);
	}

	void setCameraLocation(Location masterLoc,
			Camera slaveCamera){
		if(clusteredView == null || (!clusteredView.ownsCamera(slaveCamera))){
			slaveCamera.setLocation(masterLoc);
			return;
		}

		double focal = slaveCamera.getFocal();
		double altCoef = (focal + masterLoc.alt) / focal;
		//block (screen) width in virtualspace coords
		double virtBlockWidth = clusteredView.getClusterGeometry().getBlockWidth() * altCoef;
		//block (screen) height in virtualspace coords
		double virtBlockHeight = clusteredView.getClusterGeometry().getBlockHeight() * altCoef;	

		//row and col take origin block into account
		int viewRows = clusteredView.getViewRows();
		int viewCols = clusteredView.getViewCols();
		int row = clusteredView.rowNum(options.blockNumber) - clusteredView.rowNum(clusteredView.getOrigin()); 
		int col = clusteredView.colNum(options.blockNumber) - clusteredView.colNum(clusteredView.getOrigin()); 

		double xOffset = -((viewCols-1)*virtBlockWidth)/2; 
		double yOffset = ((viewRows-1)*virtBlockHeight)/2;

		double newX = xOffset + masterLoc.vx + col*virtBlockWidth;
		double newY = -yOffset + masterLoc.vy - row*virtBlockHeight;

		slaveCamera.setLocation(new Location(newX, newY, masterLoc.alt));
	}

    void stop(){
        System.exit(0);
    }

    void setBackgroundColor(ClusteredView cv, Color bgColor){
        //find if cv owns the local view. 	
        if(!cv.ownsBlock(options.blockNumber)){
			return;
		}
        if(view == null){
            return;
        }
        view.setBackgroundColor(bgColor);
    }

    //Simple event handler. All callbacks are no-ops except viewClosing
    //which should ensure graceful application closure.
    protected static class SlaveEventHandler extends ViewAdapter {
        @Override
        public void viewClosing(View v){System.exit(0);}
    }
}

class SlaveOptions {
	@Option(name = "-b", aliases = {"--block"}, usage = "clustered view block number (slave index)")
		int blockNumber = 0;

	@Option(name = "-n", aliases = {"--app-name"}, usage = "application name (should match master program)")
		String appName = "zvtmApplication";

	@Option(name = "-f", aliases = {"--fullscreen"}, usage = "open in full screen mode")
		boolean fullscreen = false;		

	@Option(name = "-a", aliases = {"--antialiasing"}, usage = "enable antialiased rendering")
		boolean antialiasing = false;
    
    @Option(name = "-o", usage = "enable OpenGL acceleration")
		boolean openGl = false;

	@Option(name = "-d", aliases = {"--device-name"}, usage = "use chosen device (fullscreen only)")
		String device = "";

	@Option(name = "-g", aliases = {"--debug"}, usage = "show ZVTM debug information")
		boolean debug = false;

	@Option(name = "-h", aliases = {"--help"}, usage = "print this help message and exit")
		boolean help = false;

    @Option(name = "-u", aliases = {"--undecorated"}, usage = "remove window decorations (borders)")
        boolean undecorated = false;

    @Option(name = "-x", aliases = {"--x-offset"}, usage = "window x offset (ignored if fullscreen set)")
        int xOffset = 0;

    @Option(name = "-y", aliases = {"--y-offset"}, usage = "window y offset (ignored if fullscreen set)")
        int yOffset = 0;

    @Option(name = "-r", aliases = {"--refresh-period"}, usage = "time between two scene repaints (milliseconds)")
        int refreshPeriod = 25;
}

