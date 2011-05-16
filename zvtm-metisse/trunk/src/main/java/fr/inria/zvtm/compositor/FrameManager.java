package fr.inria.zvtm.compositor;

import java.util.HashMap;

import fr.inria.zvtm.kernel.Main;


/**
 * this is the meeting point between the ZVTM viewer and the compositor (ZVTMAdapter)
 * @author Julien Altieri
 *
 */

public class FrameManager {
	private static boolean verbose = false;
	private static HashMap<Integer, MetisseWindow> windows;
	private static HashMap<Integer, MetisseWindow> standBy;
	
	
	public static void init() {
		windows = new HashMap<Integer, MetisseWindow>();
		standBy = new HashMap<Integer, MetisseWindow>();
	}
	

	public static void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		if (verbose)System.out.println("try to add "+window);
		if(windows.containsKey(window))return;
		if(standBy.containsKey(window))standBy.remove(window);
		standBy.put(window, new MetisseWindow(isroot,window,x,y,w,h));
		if (verbose)System.out.println("added "+window);
	}

	public static void removeWindow(int window) {
		if (verbose)System.out.println("try to remove "+window);
		if (!windows.containsKey(window))return;
		Main.viewer.remFrame(windows.get(window));
		windows.remove(window);
		if (verbose)System.out.println("removed "+window);
	}

	@SuppressWarnings("unused")
	private static void printState() {
		System.out.println("nombre de fenetres : "+windows.keySet().size());
	}

	public static void frameBufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h) {

		
		if(windows.containsKey(window))windows.get(window).fbUpdate(img,x,y,w,h);
		if(standBy.containsKey(window))standBy.get(window).fbUpdate(img,x,y,w,h);
	}

	public static void restackWindow(int window, int nextWindow, int transientFor,
			int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,
			int flags) {

		if (verbose)System.out.println("try to restack "+window);
		
		
		if(standBy.containsKey(window)){
			windows.put(window,standBy.get(window));
			standBy.remove(window);
			Main.viewer.addFrame(windows.get(window));
			if(grabWindow!=0){
				windows.get(window).refreshMaster(windows.get(grabWindow));
			}
			
			if (verbose)System.out.println("restacked "+window);
		}
	}

	public static void UnmapWindow(int window) {
		if (verbose)System.out.println("try to unmap "+window);
		if(windows.containsKey(window)){
			standBy.put(window,windows.get(window));
			removeWindow(window);
			if (verbose)System.out.println("unmapped "+window);
		}
		
	}

	public static void configure(int window, int x, int y, int w, int h) {
		if(windows.containsKey(window))windows.get(window).configure(x,y,w,h);
	}


	public static MetisseWindow get(int number) {
		if(windows.containsKey(number))return windows.get(number);
		return null;
	}
	
	
	
	/**
	 * **********************************************************************************************************************
	 */

}
