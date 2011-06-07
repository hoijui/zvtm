package fr.inria.zvtm.compositor;

import java.util.HashMap;

import fr.inria.zvtm.gui.Viewer;


/**
 * this is the meeting point between the ZVTM viewer and the compositor (ZVTMAdapter)
 * @author Julien Altieri
 *
 */

public class FrameManager {
	private boolean verbose = false;
	private HashMap<Integer, MetisseWindow> inSpace;
	private HashMap<Integer, MetisseWindow> inWaitingRoom;
	private Viewer application;
	
	
	public FrameManager(Viewer application) {
		inSpace = new HashMap<Integer, MetisseWindow>();
		inWaitingRoom = new HashMap<Integer, MetisseWindow>();
		this.application = application;
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
	
		application.remFrame(inSpace.get(window));
		inSpace.remove(window);
		if (verbose)System.out.println("removed "+window);
	}

	@SuppressWarnings("unused")
	private void printState() {
		System.out.println("nombre de fenetres : "+inSpace.keySet().size());
	}

	public void frameBufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h) {

		
		if(inSpace.containsKey(window))inSpace.get(window).fbUpdate(img,x,y,w,h);
		if(inWaitingRoom.containsKey(window))inWaitingRoom.get(window).fbUpdate(img,x,y,w,h);
	}

	public void restackWindow(int window, int nextWindow, int transientFor,
			int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,
			int flags) {

		if (verbose)System.out.println("try to restack "+window);
		
		
		if(inWaitingRoom.containsKey(window)){
			inSpace.put(window,inWaitingRoom.get(window));
			inWaitingRoom.remove(window);
			application.addFrame(inSpace.get(window));
			if(grabWindow!=0){
				inSpace.get(window).refreshMaster(inSpace.get(grabWindow));
				if(inSpace.containsKey(grabWindow)&& inSpace.get(grabWindow).isOnWall()) application.teleport(inSpace.get(window));
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
		return null;
	}
	
	
	
	/**
	 * **********************************************************************************************************************
	 */

}
