package fr.inria.zvtm.cluster;
import fr.inria.zvtm.compositor.ZVTMAdapter;
import fr.inria.zvtm.kernel.Connexion;



public class MetisseMain {

	private static String ip = "127.0.0.1";
	private static int port = 5901;
	public static ZVTMAdapter compositor;
	public static MetisseViewer viewer;
	public static MetisseViewer clientViewer;

	public static boolean CLUSTERMODE = false;
	public static boolean SMALLMODE = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new ZVTMAdapter();
		compositor.init();
		viewer = new MetisseViewer(false);
		viewer.init(false, false, true, null);

		clientViewer = new MetisseViewer(true);
		clientViewer.init(false, false, true, viewer.mSpace);	
		Connexion.init(ip,port);
	}

	private static void handleArgs(String[] args) {

		if(args.length==0){
			System.out.println("Starting with default values: 127.0.0.1:5901");
			return;
			
		}
		if(args.length!=2 && args.length!=4){
			diplayUse();
			System.exit(0);
		}

	
		CLUSTERMODE = (args[0].equals("1"));
		SMALLMODE = (args[1].equals("1"));
		if(args.length == 2) return;
		ip = args[2];
		port = Integer.parseInt(args[3]);

	}

	private static void diplayUse() {
		System.out.println("Use: MetisseMain 1 1 ip port\nclustermode on (set to 0 to deactivate)\nsmallmode on (idem)\nip: the adress of the machine on which runs the metisse server (default 127.0.0.1)\nport: the port (default is 5901");

	}

	public static void end() {
		Connexion.end();
		System.exit(0);

	}

}
