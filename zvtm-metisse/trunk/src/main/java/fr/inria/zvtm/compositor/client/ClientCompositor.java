package fr.inria.zvtm.compositor.client;

import fr.inria.zvtm.compositor.ZvtmCompositor;
import fr.inria.zvtm.compositor.ZvtmRfbHandler;
import fr.inria.zvtm.gui.client.ClientViewer;

public class ClientCompositor extends ZvtmCompositor {

	private ZvtmRfbHandler rfbInput;
	
	
	public ClientCompositor() {	
		super(new ClientViewer());
		rfbInput = new ZvtmRfbHandler(framemanager);		
	}

	public ZvtmRfbHandler getRFBInput() {
		return rfbInput;
	}

}
