package fr.inria.zvtm.client.compositor;

import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.compositor.FrameManager;
import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.kernel.RfbForwarder;
import fr.inria.zvtm.common.protocol.Keysym;

/**
 * This is an extension of the basic FrameManager that forwards the rfbMessages (incoming from the Metisse server, to the Zvtm server) concerning public frames.
 * @author Julien Altieri
 * @see RfbForwarder
 * @see FrameManager
 */
public class ForwardingFrameManager extends FrameManager {

	private RfbForwarder rfbfw;

	/**
	 * Must provide the related ZVTM Viewer
	 * @param viewer
	 */
	public ForwardingFrameManager(ClientViewer viewer) {
		super(viewer);
		rfbfw = new RfbForwarder();

	}

	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		super.addWindow(window, isroot, x, y, w, h);
		if(isroot){
			rfbfw.addWindow(window, isroot, x, y, w, h);
		}
	}

	@Override
	public void configure(int window, int x, int y, int w, int h) {
		super.configure(window, x, y, w, h);
		if(get(window)==null)return;
		if(get(window).isPublished())
			rfbfw.configure(window,get(window).isRoot(), x, y, w, h);
	}

	@Override
	public void frameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h) {
		super.frameBufferUpdate(window, isroot, img, x, y, w, h);
		if(get(window)==null)return;
		if(get(window).isPublished()){
			rfbfw.frameBufferUpdate(window, isroot, img, x, y, w, h);
		}
	}

	@Override
	public void restackWindow(int window, int nextWindow, int transientFor,int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,int flags) {
		super.restackWindow(window, nextWindow, transientFor, unmanagedFor, grabWindow,duplicateFor, facadeReal, flags);
		MetisseWindow win = get(window);
		if(win==null)return;
		if(win.isPublished()){
			rfbfw.addWindow(window,false,win.getX(),win.getY(),win.getW(),win.getH());
			rfbfw.restackWindow(window, nextWindow, transientFor,unmanagedFor, grabWindow, duplicateFor, facadeReal,flags);
			rfbfw.frameBufferUpdate(win.getId(), win.isRoot(), win.getRaster(), 0, 0, win.getW(), win.getH());
		}
	}

	@Override
	public void UnmapWindow(int window) {
		super.UnmapWindow(window);
		if(get(window)==null)return;
		if(get(window).isPublished())
			rfbfw.UnmapWindow(window);
	}


	/**
	 * By calling this method, the specified {@link MetisseWindow} will be visible on the wall
	 * @param win the window to publish
	 */
	public void publish(MetisseWindow win) {
		rfbfw.addWindow(win.getId(), win.isRoot(), win.getX(), win.getY(), win.getW(), win.getH());
		if(inSpace.containsValue(win)){
			rfbfw.restackWindow(win.getId(), 0, 0, 0, 0, 0, 0, 0);
			rfbfw.frameBufferUpdate(win.getId(), win.isRoot(), win.getRaster(), 0, 0, win.getW(), win.getH());
		}
		for (MetisseWindow w : win.getChildren().values()) {
			publish(w);
		}
	}

	/**
	 * Unpublishes the specified {@link MetisseWindow}
	 * @param win the window to unpublish
	 */
	public void unpublish(MetisseWindow win) {
		rfbfw.removeWindow(win.getId());
	}

	@Override
	public void removeWindow(int window) {
		super.removeWindow(window);
		rfbfw.removeWindow(window);
	}

	@Override
	public void endResize(MetisseWindow mwr) {//send a message to indicate the end of resizing 
		rfbfw.configure(mwr.getId(), true, mwr.getX(), mwr.getY(), mwr.getW(), mwr.getH());
	}

	/**
	 * Sends a pointer event in double coordinates to the zvtm server. Used for transmitting the zvtm virtual pointer events to the zvtm server.
	 * @param x
	 * @param y
	 * @param buttonMask
	 */
	public void sendDoublePointerEvent(double x, double y,int buttonMask) {
		rfbfw.sendPointerEvent(x,y,buttonMask);	
	}

	/**
	 * Sends a key event to the zvtm server
	 * @param code
	 * @param i 1 for down, 0 for up
	 */
	public void sendKeyEvent(int code, int i) {
		if(i==Keysym.AltL)return;
		rfbfw.sendKeyEvent(code,i);
	}

	/**
	 * Must be called to start listening the zvtm server messages (wall interaction)
	 */
	public void startListeningBounces() {
		this.rfbfw.startListeningBounces();
	}
}
