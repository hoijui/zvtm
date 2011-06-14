package fr.inria.zvtm.client;

import fr.inria.zvtm.client.compositor.ClientCompositor;
import fr.inria.zvtm.client.compositor.ForwardingFrameManager;
import fr.inria.zvtm.client.gui.ClientViewer;

public class ClientMain {
	
	public static boolean CLUSTERMODE = false;
	public static boolean SMALLMODE = false;
	protected static String Xip = "127.0.0.1";
	protected static int Xport = 5901;
	public static String Hostip = "127.0.0.1";
	public static int Hostport = 5700;
	protected static ClientCompositor compositor;
	public static RFBConnection connection;//connection with the X server
	
	public static void main(String[] args) {
		handleArgs(args);
		System.out.println("X server: "+ Xip+":"+Xport);
		System.out.println("Zvtm server: "+ Hostip+":"+Hostport);
		compositor = new ClientCompositor();
		connection = new RFBConnection(Xip,Xport,compositor.getRFBInput());
		((ClientViewer)compositor.getViewer()).getInputForwarder().setRfbAgent(connection.getRfbAgent());
		((ForwardingFrameManager)compositor.getViewer().getFrameManager()).startListeningBounces();
	}


	protected static void handleArgs(String[] args) {

		if(args.length!=1 && args.length!=3){
			diplayUse();
			System.exit(0);
		}

		Hostip = args[0];
		
		if(args.length == 1) return;
		Xip = args[1];
		Xport = Integer.parseInt(args[2]);

	}
	
	protected static void diplayUse() {
		System.out.println("Use: ClientMain [ZVTM server address] \nor   ClientMain [ZVTM server address] [X server address] [X server port]");
	}
}