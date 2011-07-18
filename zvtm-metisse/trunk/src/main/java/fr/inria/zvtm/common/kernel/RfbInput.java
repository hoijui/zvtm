package fr.inria.zvtm.common.kernel;

import java.net.Socket;

import fr.inria.zvtm.common.compositor.RfbMessageHandler;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;
import fr.inria.zvtm.master.gui.MasterViewer;

/**
 * All input messages of a client are handled by a single {@link RfbInput} object. This object transmits then the event to the {@link ZvtmRfbHandlerMultiplexer} after replacing the id of the related window by a cross-client unique one.
 * @author Julien Altieri
 *
 */
public class RfbInput implements RfbMessageHandler {

	private ZvtmRfbHandlerMultiplexer multiplexer;
	private Socket sock;

	public RfbInput(Socket sock, ZvtmRfbHandlerMultiplexer multiplexer) {
		this.sock = sock;
		this.multiplexer = multiplexer;
		((MasterViewer)this.multiplexer.getFrameManager().getViewer()).getCursorMultiplexer().subscribeClient(sock);
	}

	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		multiplexer.addWindow(translate(window), isroot, x, y, w, h);
	}

	@Override
	public boolean handleConfigureWindow(int window, boolean isroot, int x,int y, int w, int h) {
		return multiplexer.handleConfigureWindow(translate(window), isroot, x, y, w, h);
	}

	@Override
	public void handleCursorPosition(int x, int y) {
		
	}

	@Override
	public void handleDestroyWindow(int window) {
		multiplexer.handleDestroyWindow(translate(window));
	}

	@Override
	public void handleImageFramebufferUpdate(int window, boolean isroot,byte[] img, int x, int y, int w, int h) {
		multiplexer.handleImageFramebufferUpdate(translate(window), isroot, img, x, y, w, h);
	}

	@Override
	public void handleRestackWindow(int window, int nextWindow,int transientFor, int unmanagedFor, int grabWindow,int duplicateFor, int facadeReal, int flags) {
		multiplexer.handleRestackWindow(translate(window), translate(nextWindow), translate(transientFor), translate(unmanagedFor), translate(grabWindow), translate(duplicateFor), translate(facadeReal), flags);
	}

	@Override
	public void handleServerCutText(String str) {
		multiplexer.handleServerCutText(str);
	}

	@Override
	public void handleUnmapWindow(int window) {
		multiplexer.handleUnmapWindow(translate(window));

	}

	/*
	 * returns the general id of the window (this unique id comes from the multiplexer)
	 */
	private int translate(int window) {
		return multiplexer.get(sock,window);
	}

	/**
	 * Handles pointer events coming from the network.
	 * @param x virtual x coordinate
	 * @param y virtual y coordinate
	 * @param buttons button mask
	 */
	public void handleDoublePointerEvent(double x,double y, int buttons) {
		multiplexer.handleDoublePointerEvent(sock, x, y, buttons);
	}

	/**
	 * Handles key events coming from the network.
	 * @param keysym
	 * @param i (1 for down, 0 for up)
	 */
	public void handleRemoteKeyEvent(int keysym, int i) {
		multiplexer.handleRemoteKeyEvent(sock,keysym,i);
	}

}
