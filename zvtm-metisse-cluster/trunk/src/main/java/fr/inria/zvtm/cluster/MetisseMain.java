package fr.inria.zvtm.cluster;


import fr.inria.zvtm.compositor.ZVTMAdapter;
import fr.inria.zvtm.gui.Viewer;
import fr.inria.zvtm.kernel.Connexion;
import fr.inria.zvtm.kernel.Main;



public class MetisseMain extends Main{

	public static boolean SMALLMODE = false;

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
		viewer = new MetisseViewer(false);
		viewer.init(false, false, true, null);		
		clientViewer = new MetisseViewer(true);
		clientViewer.init(false, false, true, viewer.mSpace);	
	}
}
