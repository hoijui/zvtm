package fr.inria.zvtm.master.gui;

import java.net.Socket;

import fr.inria.zvtm.common.kernel.Pair;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;

/**
 * The {@link Bouncer} is a redirecting tool for input events on the wall. When an event occurs, it detects which window is under the cursor, and transmits the event through the correct {@link Socket}.
 * @author Julien Altieri
 *
 */
public class Bouncer {
	private ZvtmRfbHandlerMultiplexer multiplexer;

	/**
	 * The {@link ZvtmRfbHandlerMultiplexer} must be specified after the instantiation of the {@link Bouncer}.
	 * @param multiplexer
	 */
	public void setRFBInputMultiplexer(ZvtmRfbHandlerMultiplexer multiplexer) {
		this.multiplexer = multiplexer;
	}


	/**
	 * Forwards the mouse event to the correct {@link Socket}.
	 * @param x server x coordinate
	 * @param y server y coordinate
	 * @param buttonMask
	 * @param window the unique id (it will be reversed and sent to the appropriate {@link Socket}.
	 */
	public void handleMouse(int x, int y, int buttonMask, int window) {
		Pair p = getDestPack(window);
		if (p==null)return;
		multiplexer.getRfbAgent(p.dest).rfbPointerEvent(p.detranslatedWindow, x, y,buttonMask);
	}

	/**
	 * Forwards the key event to the correct {@link Socket}.
	 * @param keysym 
	 * @param down (1 for down, 0 for up)
	 * @param window the unique id (it will be reversed and sent to the appropriate {@link Socket}.
	 */
	public void handleKey(int keysym,boolean down, int window) {
		Pair p = getDestPack(window);
		if (p==null)return;
		multiplexer.getRfbAgent(p.dest).orderKeyEvent(keysym, down);
	}

	/**
	 * Reverse translate the specified window's unique id
	 * @param window the unique id
	 * @return a {@link Pair} containing the true id and the related {@link Socket}
	 */
	public Pair getDestPack(int window){
		return multiplexer.find(window);
	}

	/**
	 * Broadcast a view upgrade message (wall bounds) to all clients.
	 * @param bounds the current wall bounds
	 */
	public void sendViewUpgrade(double[] bounds) {
		for (RfbAgent rfb : multiplexer.getAllAgents()) {
			rfb.orderConfigureWall(bounds);
		}
	}

	/** Tests if the cursor's owner is also the owner of the window
	 *@param cursor The potential owner of the window
	 *@param window The window to test 
	 */
	public boolean testOwnership(PCursorPack cursor, int window) {
		Socket s = ((MasterViewer)multiplexer.getFrameManager().getViewer()).getCursorMultiplexer().find(cursor);
		Pair p = multiplexer.find(window);
		return ((p!=null) && (p.dest==s));
	}


}

