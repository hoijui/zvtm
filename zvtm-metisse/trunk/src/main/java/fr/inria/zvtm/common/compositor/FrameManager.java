package fr.inria.zvtm.common.compositor;

import java.util.HashMap;

import fr.inria.zvtm.client.compositor.ForwardingFrameManager;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.engine.VirtualSpace;


/**
 * Keeps in memory the windows, accordingly with the Metisse server's instructions.
 * @author Julien Altieri
 * @see ForwardingFrameManager
 */
public class FrameManager {
	private boolean verbose = false;
	protected HashMap<Integer, MetisseWindow> inSpace;
	private HashMap<Integer, MetisseWindow> inWaitingRoom;
	private Viewer viewer;
	
	/**
	 * The FrameManager is linked to a Zvtm {@link Viewer}.
	 * @param application the related Viewer
	 */
	public FrameManager(Viewer application) {
		inSpace = new HashMap<Integer, MetisseWindow>();
		inWaitingRoom = new HashMap<Integer, MetisseWindow>();
		this.viewer = application;
	}
	
	/**
	 * Called by the RFB listener. Creates a {@link MetisseWindow} object that represents the specified id (window) given by the Metisse server.
	 * The new {@link MetisseWindow} is put in the "waiting room" until it is restacked
	 * @param window
	 * @param isroot
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @see FrameManager#restackWindow(int, int, int, int, int, int, int, int)
	 */
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		if (verbose)System.out.println("try to add "+window);
		if(inSpace.containsKey(window))return;
		if(inWaitingRoom.containsKey(window))inWaitingRoom.remove(window);
		inWaitingRoom.put(window, new MetisseWindow(isroot,window,x,y,w,h));
		if (verbose)System.out.println("added "+window);
	}

	/**
	 * Called by the RFB listener. Remove the specified window if it exists.
	 * @param window
	 */
	public void removeWindow(int window) {
		if (verbose)System.out.println("try to remove "+window);
		if (!inSpace.containsKey(window))return;
		viewer.remFrame(inSpace.get(window));
		inSpace.remove(window);
		if (verbose)System.out.println("removed "+window);
	}

	/**
	 * Called by the RFB listener. Updates the related {@link MetisseWindow}'s raster according to the byte[].
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 */
	public void frameBufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h) {
		if (verbose)System.out.println("fbupdate "+window);
		if(inSpace.containsKey(window))inSpace.get(window).fbUpdate(img,x,y,w,h);
		if(inWaitingRoom.containsKey(window))inWaitingRoom.get(window).fbUpdate(img,x,y,w,h);
	}

	/**
	 * Called by the RFB listener. Transfers the given window from the waiting room to the in-space room, and adds it to the {@link VirtualSpace}.
	 * The given window will now be visible (mapped) in the virtual space.
	 * @param window the window to restack
	 * @param nextWindow the next window in the stack 
	 * @param transientFor Facade Parametter (dont mind)
	 * @param unmanagedFor Facade Parametter (dont mind)
	 * @param grabWindow Facade Parametter (dont mind)
	 * @param duplicateFor Facade Parametter (dont mind)
	 * @param facadeReal Facade Parametter (dont mind)
	 * @param flags Facade Parametter (dont mind)
	 */
	public void restackWindow(int window, int nextWindow, int transientFor,
			int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,
			int flags) {
		if((flags & Proto.rfbWindowFlagsNetChecking) != 0)return;
		if (verbose)System.out.println("try to restack "+window);
		
		if(inWaitingRoom.containsKey(window)){
			inSpace.put(window,inWaitingRoom.get(window));
			inWaitingRoom.remove(window);
			viewer.addFrame(inSpace.get(window));
			if(transientFor!=0){
				inSpace.get(window).refreshMaster(inSpace.get(transientFor));
			}
			else if(unmanagedFor!=0){
				inSpace.get(window).refreshMaster(inSpace.get(unmanagedFor));
			}
			else if(grabWindow!=0){
				inSpace.get(window).refreshMaster(inSpace.get(grabWindow));
			}
			else if((flags & Proto.rfbWindowFlagsUnmanaged) != 0){
				if (verbose)System.out.println("restacking a window which as an a parent but whose identity is untransmitted");
			}
			
			if (verbose)System.out.println("restacked "+window);
		}
	}

	/**
	 * Called by the RFB listener. Hide the specified window if not already on the waiting list. 
	 * @param window
	 */
	public void UnmapWindow(int window) {
		if (verbose)System.out.println("try to unmap "+window);
		if(inSpace.containsKey(window)){
			inWaitingRoom.put(window,inSpace.get(window));
			removeWindow(window);
			if (verbose)System.out.println("unmapped "+window);
		}
		
	}

	/**
	 * Called by the RFB listener. This Message concerns the specified window size and position in the X server.
	 * @param window the id of the configured window
	 * @param x the new x coordinate in the X server
	 * @param y the new y coordinate in the X server
	 * @param w the new width in the X server
	 * @param h the new height in the X server
	 */
	public void configure(int window, int x, int y, int w, int h) {
		if(inSpace.containsKey(window))inSpace.get(window).configure(x,y,w,h);
	}

	/**
	 * This method is a shortcut for accessing the specified window
	 * @param id the id of the window to find
	 * @return the {@link MetisseWindow} object registered with this ID (null if does not exist neither in the VirtualSpace nor in the waiting room.
	 */
	public MetisseWindow get(int id) {
		if(inSpace.containsKey(id))return inSpace.get(id);
		if(inWaitingRoom.containsKey(id))return inWaitingRoom.get(id);
		return null;
	}

	/**
	 * This method is called when the left mouse button is released. Triggers the process of updating the size and content of the specified {@link MetisseWindow}.
	 * @param mwr
	 */
	public void endResize(MetisseWindow mwr) {}
	
	/**
	 * 
	 * @return the viewer associated to this {@link FrameManager}
	 */
	public Viewer getViewer(){
		return viewer;
	}

}
