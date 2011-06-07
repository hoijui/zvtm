package fr.inria.zvtm.compositor;

import fr.inria.zvtm.gui.client.ClientViewer;

public class ForwardingFrameManager extends FrameManager {

	private RfbForwarder rfbfw;

	public ForwardingFrameManager(ClientViewer viewer) {
		super(viewer);
		rfbfw = new RfbForwarder();
	}

	@Override
	public void configure(int window, int x, int y, int w, int h) {
		super.configure(window, x, y, w, h);
		if(get(window)==null)return;
		if(get(window).isPublished())rfbfw.configure(window,get(window).isRoot(), x, y, w, h);
	}

	@Override
	public void frameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h) {
		super.frameBufferUpdate(window, isroot, img, x, y, w, h);
		if(get(window)==null)return;
		if(get(window).isPublished())rfbfw.frameBufferUpdate(window, isroot, img, x, y, w, h);
	}

	@Override
	public void restackWindow(int window, int nextWindow, int transientFor,int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,int flags) {
		super.restackWindow(window, nextWindow, transientFor, unmanagedFor, grabWindow,duplicateFor, facadeReal, flags);
		if(get(window)==null)return;
		if(get(window).isPublished())rfbfw.restackWindow(window, nextWindow, transientFor,unmanagedFor, grabWindow, duplicateFor, facadeReal,flags);
	}

	@Override
	public void UnmapWindow(int window) {
		super.UnmapWindow(window);
		if(get(window)==null)return;
		if(get(window).isPublished())rfbfw.UnmapWindow(window);
	}

	public void publish(MetisseWindow win) {
		rfbfw.addWindow(win.getId(), win.isRoot(), win.getX(), win.getY(), win.getW(), win.getH());
		rfbfw.restackWindow(win.getId(), 0, 0, 0, 0, 0, 0, 0);
	}

	public void unpublish(MetisseWindow win) {
		rfbfw.removeWindow(win.getId());
	}
	
	@Override
	public void removeWindow(int window) {
		super.removeWindow(window);
		rfbfw.removeWindow(window);
	}
}
