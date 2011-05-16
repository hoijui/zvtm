package fr.inria.zvtm.compositor;

import fr.inria.zvtm.kernel.Main;



public class ZVTMAdapter implements fr.inria.zvtm.compositor.GenericAdapter {


	@Override
	public void init() {
		FrameManager.init();
	}
	
	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		FrameManager.addWindow(window,isroot,x,y,w,h);		

	}

	@Override
	public boolean handleConfigureWindow(int window, boolean isroot, int x,
			int y, int w, int h) {
		if(Main.viewer.dragging)return false;
		FrameManager.configure(window,x,y,w,h);
		return false;
	}

	@Override
	public void handleCursorPosition(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDestroyWindow(int window) {
		FrameManager.removeWindow(window);
		refresh();
	}

	@Override
	public void handleImageFramebufferUpdate(int window, boolean isroot,
			byte[] img, int x, int y, int w, int h) {
		FrameManager.frameBufferUpdate(window,isroot,img,x,y,w,h);
		refresh();
	}

	@Override
	public void handleRestackWindow(int window, int nextWindow,
			int transientFor, int unmanagedFor, int grabWindow,
			int duplicateFor, int facadeReal, int flags) {
		FrameManager.restackWindow(window, nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	@Override
	public void handleServerCutText(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUnmapWindow(int window) {
		FrameManager.UnmapWindow(window);
		refresh();

	}

	private void refresh(){
		Main.viewer.refresh();
	}



}
