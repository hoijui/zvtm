package fr.inria.zvtm.client.compositor;

import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.compositor.ZvtmCompositor;
import fr.inria.zvtm.common.compositor.ZvtmRfbHandler;

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
