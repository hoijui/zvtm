package Compositors;

import java.util.HashMap;

import kernel.Main;

import Protocol.Proto;
import ZVTMViewer.Viewer;
import ZVTMViewer.MetisseWindow;



public class BasicAdapter implements ZVTMAdapter {


	public FrameManager winman = new FrameManager();


	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		winman.addWindow(window,isroot,x,y,w,h);		

	}

	@Override
	public boolean handleConfigureWindow(int window, boolean isroot, int x,
			int y, int w, int h) {
		if(Main.viewer.dragging)return false;
		if(winman.windows.containsKey(window))
			winman.configure(window,x,y,w,h);
		
		return false;
	}

	@Override
	public void handleCursorPosition(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDestroyWindow(int window) {
		winman.removeWindow(window);
		refresh();
	}

	@Override
	public void handleImageFramebufferUpdate(int window, boolean isroot,
			byte[] img, int x, int y, int w, int h) {
		winman.frameBufferUpdate(window,isroot,img,x,y,w,h);
		refresh();
	}

	@Override
	public void handleRestackWindow(int window, int nextWindow,
			int transientFor, int unmanagedFor, int grabWindow,
			int duplicateFor, int facadeReal, int flags) {
		winman.restackWindow(window, nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	@Override
	public void handleServerCutText(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUnmapWindow(int window) {
		winman.UnmapWindow(window);
		refresh();

	}

	private void refresh(){
		Main.viewer.refresh();
	}

}
