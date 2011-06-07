package fr.inria.zvtm.kernel;

import fr.inria.zvtm.compositor.client.ClientCompositor;
import fr.inria.zvtm.gui.client.ClientViewer;

public class ClientMain {
	
	public static boolean CLUSTERMODE = false;
	public static boolean SMALLMODE = false;
	protected static String Xip = "127.0.0.1";
	protected static int Xport = 5901;
	public static String Hostip = "127.0.0.1";
	public static int Hostport = 5700;
	protected static ClientCompositor compositor;
	protected static RFBConnection connection;//connection with the X server
	
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new ClientCompositor();
		connection = new RFBConnection(Xip,Xport,compositor.getRFBInput());
		((ClientViewer)compositor.getViewer()).getInputForwarder().setRfbAgent(connection.getRfbAgent());
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
		SMALLMODE = (args[1].equals("1"));
		if(args.length == 2) return;
		Xip = args[2];
		Xport = Integer.parseInt(args[3]);

	}
	
	protected static void diplayUse() {
		System.out.println("Use: Main 1 1 ip port\nclustermode on (set to 0 to deactivate)\nsmallmode on (idem)\nip: the adress of the machine on which runs the metisse server (default 127.0.0.1)\nport: the port (default is 5901");

	}
}