package net.claribole.zvtm.cluster;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

import java.awt.Color;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.util.Vector;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class SlaveOptions {
	@Option(name = "-b", aliases = {"--block"}, usage = "metacamera block number")
	int blockNumber = 0;

	@Option(name = "-w", aliases = {"--width"}, usage = "slave view width")
	int width = 400;

	@Option(name = "-h", aliases = {"--height"}, usage = "slave view height")
	int height = 300;

	@Option(name = "-g", aliases = {"--opengl"}, usage = "create OpenGL view (default std view)")
	boolean opengl = false;

	@Option(name = "-f", aliases = {"--fullscreen"}, usage = "set view in fullscreen")
	boolean fullscreen = false;
}

//Generic slave application
// - retrieves a slave camera from a MetaCamera
// - creates a std or opengl view 
// - dispays scene
public class SlaveApp {
	private SlaveOptions slaveOptions;

	VirtualSpaceManager vsm;
	View view;

	SlaveApp(SlaveOptions options){
		this.slaveOptions = options;
		vsm = VirtualSpaceManager.getInstance();
		VirtualSpaceManager.setDebug(true);
		System.out.println("slave (block " + slaveOptions.blockNumber 
				+ "): waiting for master to initialize virtual space");

		//the master program creates the main virtual space
		//and metacamera, among other things 
		vsm.awaitMaster();

		VirtualSpace vs = vsm.getVirtualSpace("protoSpace");

		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(vs.getMetaCamera().retrieveCamera(slaveOptions.blockNumber));
		if(slaveOptions.opengl){ System.out.println("creating openGL view");}
		view = vsm.addExternalView(vcam, "slaveView"  + slaveOptions.blockNumber, 
				slaveOptions.opengl? View.OPENGL_VIEW : View.STD_VIEW, slaveOptions.width, 
				slaveOptions.height, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
		//set refresh rate to 25fps (40ms)
		view.setRefreshRate(40);
		vcam.get(0).setOwningView(view); 

		//set fullscreen if needed
		if(slaveOptions.fullscreen){
			//GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)view.getFrame());
			JFrame vFrame = (JFrame)view.getFrame();
			vFrame.dispose();
			vFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			vFrame.setUndecorated(true); 
			vFrame.setVisible(true);
		}
	}

	public static void main(String[] args) throws CmdLineException{
		SlaveOptions options = new SlaveOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);
		new SlaveApp(options);
	}
}

