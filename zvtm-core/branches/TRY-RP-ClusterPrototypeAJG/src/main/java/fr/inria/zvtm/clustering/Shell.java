package fr.inria.zvtm.clustering;

import bsh.Interpreter;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

// java -cp target/zvtm-0.10.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/bsh-1.3.0.jar fr.inria.zvtm.clustering.Shell

class ShellOptions {
	//start web and telnet server
	@Option(name = "-s", aliases = {"--server"}, usage = "spawn server")
	boolean spawnServer = false;

	//server port (ignored if server not started)
	@Option(name = "-p", aliases = {"--port"}, usage = "web server port")
	int port = -1;

	//file to execute after context setup
	@Option(name = "-f", aliases = {"--file"}, usage = "script to execute")
	String execPath = "";	
}

/**
 * Minimal ZVTM shell based on BeanShell.
 * Does some boilerplate setup, provides a few convenience
 * commands and off you go.
 */
public class Shell {
	private final ShellOptions options;
	private final Interpreter interpreter = new Interpreter();

	private Shell(ShellOptions options){
		this.options = options;

		//setup context
		try{
			//provide options to the context setup script
			interpreter.set("options", options);
			interpreter.source("context.bsh");
		} catch (Exception e){
			//prototype code - not much in the way of error handling
			System.out.println("Could not set up ZVTM context: " + e);
		}
	}

	private final void launch(){
		if(!options.execPath.equals("")){
			try{
				interpreter.source(options.execPath);
			} catch (Exception e){
				//prototype code - not much in the way of error handling
				System.out.println("Could not set up ZVTM context: " + e);
			}
		}

	}

	public static void main(String[] args) throws CmdLineException {
		ShellOptions options = new ShellOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);

		Shell s = new Shell(options); 
		s.launch();
	}
}

