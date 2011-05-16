package fr.inria.zvtm.kernel;

import fr.inria.zvtm.gui.Viewer;

import fr.inria.zvtm.compositor.ZVTMAdapter;



public class Main {

	protected static String ip = "127.0.0.1";
	protected static int port = 5901;
	public static ZVTMAdapter compositor;
	public static Viewer viewer;
	public static Viewer clientViewer;

	public static boolean CLUSTERMODE = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new ZVTMAdapter();
		compositor.init();
		initViewers();
		Connexion.init(ip,port);
	}

	private static void initViewers() {
		viewer = new Viewer(false);
		viewer.init(false, false, true, null);		
		clientViewer = new Viewer(true);
		clientViewer.init(false, false, true, viewer.mSpace);	
	}

	protected static void handleArgs(String[] args) {

		if(args.length==0){
			System.out.println("Starting with default values: 127.0.0.1:5901");
			return;

		}
		if(args.length!=2 && args.length!=4){
			diplayUse();
			System.exit(0);
		}


		CLUSTERMODE = (args[0].equals("1"));
		//		SMALLMODE = (args[1].equals("1"));
		if(args.length == 2) return;
		ip = args[2];
		port = Integer.parseInt(args[3]);

	}

	protected static void diplayUse() {
		System.out.println("Use: Main 1 1 ip port\nclustermode on (set to 0 to deactivate)\nsmallmode on (idem)\nip: the adress of the machine on which runs the metisse server (default 127.0.0.1)\nport: the port (default is 5901");

	}

	public static void end() {
		Connexion.end();
		System.exit(0);

	}

}
