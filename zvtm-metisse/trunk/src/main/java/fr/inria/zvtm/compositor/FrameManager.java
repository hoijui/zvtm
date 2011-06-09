package fr.inria.zvtm.compositor;

import java.util.HashMap;

import fr.inria.zvtm.gui.Viewer;
import fr.inria.zvtm.protocol.Proto;


/**
 * Keeps in memory the windows, accordingly with the rfb instructions
 * @author Julien Altieri
 *
 */

public class FrameManager {
	private boolean verbose = false;
	protected HashMap<Integer, MetisseWindow> inSpace;
	private HashMap<Integer, MetisseWindow> inWaitingRoom;
	private Viewer viewer;
	
	
	public FrameManager(Viewer application) {
		inSpace = new HashMap<Integer, MetisseWindow>();
		inWaitingRoom = new HashMap<Integer, MetisseWindow>();
		this.viewer = application;
	}
	

	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		if (verbose)System.out.println("try to add "+window);
		if(inSpace.containsKey(window))return;
		if(inWaitingRoom.containsKey(window))inWaitingRoom.remove(window);
		inWaitingRoom.put(window, new MetisseWindow(isroot,window,x,y,w,h));
		if (verbose)System.out.println("added "+window);
	}

	public void removeWindow(int window) {
		if (verbose)System.out.println("try to remove "+window);
		if (!inSpace.containsKey(window))return;
		viewer.remFrame(inSpace.get(window));
		inSpace.remove(window);
		if (verbose)System.out.println("removed "+window);
	}

	public void frameBufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h) {
		if (verbose)System.out.println("fbupdate "+window);
		if(inSpace.containsKey(window))inSpace.get(window).fbUpdate(img,x,y,w,h);
		if(inWaitingRoom.containsKey(window))inWaitingRoom.get(window).fbUpdate(img,x,y,w,h);
	}

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

	public void UnmapWindow(int window) {
		if (verbose)System.out.println("try to unmap "+window);
		if(inSpace.containsKey(window)){
			inWaitingRoom.put(window,inSpace.get(window));
			removeWindow(window);
			if (verbose)System.out.println("unmapped "+window);
		}
		
	}

	public void configure(int window, int x, int y, int w, int h) {
		if(inSpace.containsKey(window))inSpace.get(window).configure(x,y,w,h);
	}


	public MetisseWindow get(int number) {
		if(inSpace.containsKey(number))return inSpace.get(number);
		if(inWaitingRoom.containsKey(number))return inWaitingRoom.get(number);
		return null;
	}


	public void endResize(MetisseWindow mwr) {}

}
