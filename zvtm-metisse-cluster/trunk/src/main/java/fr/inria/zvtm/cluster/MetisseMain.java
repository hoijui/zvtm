package fr.inria.zvtm.cluster;

import fr.inria.zvtm.master.Connector;
import fr.inria.zvtm.master.MasterMain;
import fr.inria.zvtm.master.compositor.MasterCompositor;
import fr.inria.zvtm.master.gui.MasterViewer;







public class MetisseMain extends MasterMain {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		handleArgs(args);
		compositor = new MasterCompositor(new MetisseViewer());
		connector = new Connector(compositor);
		initViewers();
		connector.init(listeningPort);
	}


	private static void initViewers() {
		((MasterViewer)compositor.getViewer()).getBouncer().setRFBInputMultiplexer(connector.getMultiplexer());

	}
}