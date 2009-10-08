package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

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

	SlaveApp(SlaveOptions options){
		this.options = options;
		vsm.setDebug(options.debug);
		SlaveUpdater updater = new SlaveUpdater(options.appName,
				options.blockNumber);
		updater.startOperation();
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

		new SlaveApp(options);
	}
}

class SlaveOptions {
	@Option(name = "-b", aliases = {"--block"}, usage = "metacamera block number (slave index)")
	int blockNumber = 0; 

	String appName = "zvtmApplication";

	@Option(name = "-f", aliases = {"--fullscreen"}, usage = "open in full screen mode")
	boolean fullscreen = false;	

	@Option(name = "-d", aliases = {"--device-name"}, usage = "use chosen device (fullscreen only)")
	String device = "";

	@Option(name = "-g", aliases = {"--debug"}, usage = "show ZVTM debug information")
	boolean debug = false;
}

