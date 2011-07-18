package fr.inria.zvtm.master;

import fr.inria.zvtm.master.compositor.MasterCompositor;
import fr.inria.zvtm.master.gui.MasterViewer;

/**
 * The main class for the zvtm server.
 * @author Julien Altieri
 *
 */
public class MasterMain {
	
	/**
	 * Set to true, the related {@link MasterViewer} will generate a clustered view. (Specified by the arguments of {@link MasterMain#main(String[])})
	 */
	public static boolean CLUSTERMODE = false;
	public static boolean SMALLMODE = false;
	protected static MasterCompositor compositor;//wall compositor
	protected static int listeningPort = 5700;
	protected static Connector connector;
	
	/**
	 * 
	 * Use: MasterMain 1 1 <br>
	 * <b>Clustermode on</b> (set to 0 to deactivate) <br>
	 * <b>Cmallmode on</b> (idem)
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new MasterCompositor(new MasterViewer());
		connector = new Connector(compositor);
		((MasterViewer)compositor.getViewer()).getBouncer().setRFBInputMultiplexer(connector.getMultiplexer());
		connector.init(listeningPort);
	}

	
	protected static void handleArgs(String[] args) {

		if(args.length==0){
			return;
		}
		if(args.length!=2){
			diplayUse();
			System.exit(0);
		}


		CLUSTERMODE = (args[0].equals("1"));
		SMALLMODE = (args[1].equals("1"));
		
	}
	
	protected static void diplayUse() {
		System.out.println("Use: Main 1 1 \nclustermode on (set to 0 to deactivate)\nsmallmode on (idem)");

	}
}