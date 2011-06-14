package fr.inria.zvtm.master.gui;

import fr.inria.zvtm.common.kernel.Pair;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;

public class Bouncer {
	private ZvtmRfbHandlerMultiplexer multiplexer;


	public void setRFBInputMultiplexer(ZvtmRfbHandlerMultiplexer multiplexer) {
		this.multiplexer = multiplexer;
	}


	public void handleMouse(int x, int y, int buttonMask, int window) {
		Pair p = getDestPack(window);
		if (p==null)return;
		multiplexer.getRfbAgent(p.dest).rfbPointerEvent(p.detranslatedWindow, x, y,buttonMask);
	}


	public void handleKey(int keysym,boolean down, int window) {
		Pair p = getDestPack(window);
		if (p==null)return;
		multiplexer.getRfbAgent(p.dest).orderKeyEvent(keysym, down);
	}


	public Pair getDestPack(int window){
		return multiplexer.find(window);
	}


	public void sendViewUpgrade(double[] bounds) {
		for (RfbAgent rfb : multiplexer.getAllAgents()) {
			rfb.orderConfigureWall(bounds);
		}
	}
	
	
}

