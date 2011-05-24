package fr.inria.zvtm.cluster;


import fr.inria.zvtm.compositor.ZVTMAdapter;
import fr.inria.zvtm.kernel.Connection;
import fr.inria.zvtm.kernel.Main;



public class MetisseMain extends Main{


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new ZVTMAdapter();
		compositor.init();
		initViewers();
		Connection.init(ip,port);
		ViconInput vi = new ViconInput();
		vi.initOSC(52511);
	}


	private static void initViewers() {
		viewer = new MetisseViewer(false);
		viewer.init(false, false, true, null);		
		clientViewer = new MetisseViewer(true);
		clientViewer.init(false, false, true, viewer.mSpace);	
	}
}
