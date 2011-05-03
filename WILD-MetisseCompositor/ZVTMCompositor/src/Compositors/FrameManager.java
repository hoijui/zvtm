package Compositors;

import java.util.HashMap;

import kernel.Main;

import Protocol.Proto;
import ZVTMViewer.MetisseWindow;

/**
 * this is the meeting point between the ZVTM viewer and the compositor (Basic adapter)
 * @author insitu
 *
 */

public class FrameManager {
	boolean verbose = false;
	public HashMap<Integer, MetisseWindow> windows;
	protected HashMap<Integer, MetisseWindow> standBy;
	
	public FrameManager() {
		this.windows = new HashMap<Integer, MetisseWindow>();
		this.standBy = new HashMap<Integer, MetisseWindow>();
	}

	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		if (verbose)System.out.println("try to add "+window);
		if(windows.containsKey(window))return;
		if(standBy.containsKey(window))standBy.remove(window);
		standBy.put(window, new MetisseWindow(isroot,window,x,y,w,h));
		if (verbose)System.out.println("added "+window);
	}

	public void removeWindow(int window) {
		if (verbose)System.out.println("try to remove "+window);
		if (!windows.containsKey(window))return;
		Main.viewer.remFrame(windows.get(window));
		windows.remove(window);
		if (verbose)System.out.println("removed "+window);
	}

	@SuppressWarnings("unused")
	private void printState() {
		System.out.println("nombre de fenetres : "+windows.keySet().size());
	}

	public void frameBufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h) {

		
		if(windows.containsKey(window))windows.get(window).fbUpdate(img,x,y,w,h);
		if(standBy.containsKey(window))standBy.get(window).fbUpdate(img,x,y,w,h);
	}

	public void restackWindow(int window, int nextWindow, int transientFor,
			int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,
			int flags) {

		if (verbose)System.out.println("try to restack "+window);
	//	System.out.println("restack "+window);
//		boolean stop = false;
//		
//		if((flags & Proto.rfbWindowFlagsNetChecking) >0){
//			System.out.println("netchecking "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowFlagsInputOnly) >0){
//			System.out.println("inputOnly "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowFlagsEwmhDesktop) >0){
//			System.out.println("ewmhdesk "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowFlagsOverrideRedirect) >0){
//			System.out.println("override redirect "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowFlagsTransient) >0){
//			System.out.println("transcient "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowUpdateRequest) >0){
//			System.out.println("request "+window);
//		//	stop = stop||true;
//		}
//		if((flags & Proto.rfbWindowFlagsUnmanaged) >0){
//			System.out.println("unmanaged "+window);
//		//	stop = stop||true;
//		}
//		
//		if (stop) return;
		
		if(standBy.containsKey(window)){
			windows.put(window,standBy.get(window));
			standBy.remove(window);
			Main.viewer.addFrame(windows.get(window));
			if (verbose)System.out.println("restacked "+window);
		}
		Main.viewer.mSpace.below(windows.get(window), windows.get(nextWindow));
		//je ne comprends pas pourquoi below marche ....
		//echantillonner les event souris
	}

	public void UnmapWindow(int window) {
		if (verbose)System.out.println("try to unmap "+window);
		if(windows.containsKey(window)){
			standBy.put(window,windows.get(window));
			removeWindow(window);
			if (verbose)System.out.println("unmapped "+window);
		}
		
	}

	public void configure(int window, int x, int y, int w, int h) {
	
		if(windows.containsKey(window))windows.get(window).configure(x,y,w,h);
	//	if(ZVTMFrame.rootFrame!=null)Main.viewer.mSpace.above(windows.get(window), windows.get(ZVTMFrame.rootFrame));
	}
	
	

}
