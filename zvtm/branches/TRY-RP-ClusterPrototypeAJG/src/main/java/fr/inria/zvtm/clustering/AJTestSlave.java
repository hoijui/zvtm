package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.Vector;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class SlaveOptions{
	@Option(name = "-b", aliases = {"--block"}, usage = "metacamera block number (slave index)")
	int blockNumber = 0;

	@Option(name = "-w", aliases = {"--width"}, usage = "slave view width")
	int width = 400;

	@Option(name = "-h", aliases = {"--height"}, usage = "slave view height")
	int height = 300;
	
	@Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows")
	int numRows = 1;

	@Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns")
	int numCols = 1;
}

/**
 * Slave application that observes a cloned VirtualSpace.
 */
public class AJTestSlave {
	private final SlaveOptions options;
	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;

	AJTestSlave(SlaveOptions options) throws Exception {
		this.options = options;

		vsm.setDebug(true);
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		SlaveUpdater updater = new SlaveUpdater(vs);
		Camera c = vsm.addCamera(vs);
		vs.getCameraGroup().offerCamera(c, options.blockNumber,
			options.numRows, options.numCols, 
			options.width, options.height);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(c);
		View view = vsm.addExternalView(vcam, 
				"slaveView " + options.blockNumber, 
				View.STD_VIEW,
				options.width, options.height, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
	}

	public static void main(String[] args) throws Exception {
		SlaveOptions options = new SlaveOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);

		new AJTestSlave(options);
	}
}

