package fr.inria.zvtm.common.kernel;

import java.net.Socket;

import fr.inria.zvtm.common.compositor.RfbMessageHandler;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;
import fr.inria.zvtm.master.gui.MasterViewer;

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

	private int translate(int window) {
		return multiplexer.get(sock,window);
	}

	public void handleDoublePointerEvent(double x,double y, int buttons) {
		multiplexer.handleDoublePointerEvent(sock, x, y, buttons);
	}

	public void handleRemoteKeyEvent(int keysym, int i) {
		multiplexer.handleRemoteKeyEvent(sock,keysym,i);
	}

}