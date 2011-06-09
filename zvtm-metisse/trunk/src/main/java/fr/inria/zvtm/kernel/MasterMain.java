package fr.inria.zvtm.kernel;

import fr.inria.zvtm.compositor.master.MasterCompositor;

public class MasterMain {
	
	public static boolean CLUSTERMODE = false;
	public static boolean SMALLMODE = false;
	protected static MasterCompositor compositor;//wall compositor
	protected static int listeningPort = 5700;
	private static Connector connector;
	
	
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new MasterCompositor();
		connector = new Connector(compositor);
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