package kernel;
import ZVTMViewer.Viewer;
import Compositors.BasicAdapter;
import Compositors.ZVTMAdapter;



public class Main {

	private static String ip = "127.0.0.1";
	private static int port = 5901;
	public static Connexion conn;
	public static ZVTMAdapter renderer;
	public static Viewer viewer;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		renderer = new BasicAdapter();
		viewer = new Viewer(false, false, true);
		initConnexion(ip, port);//will use the settled ZVTMAdapter (renderer)
		
	}

	private static void initConnexion(String ip, int port) {
		conn = new Connexion(ip,port);

	}

	private static void handleArgs(String[] args) {
		int validNumberOfArgs = 2;

		if(args.length==0){
			System.out.println("Starting with default values: 127.0.0.1:5901");
			return;
			
		}
		if(args.length!=validNumberOfArgs){
			diplayUse();
			System.exit(0);
		}

		ip = args[0];
		port = Integer.parseInt(args[1]);

	}

	private static void diplayUse() {
		System.out.println("Use: Main ip port\nip: the adress of the machine on which runs the metisse server (default 127.0.0.1)\nport: the port (default is 5901");

	}

	public static void end() {
		if(conn!=null){
			conn.end();
			conn = null;
		}
		System.exit(0);

	}

}
