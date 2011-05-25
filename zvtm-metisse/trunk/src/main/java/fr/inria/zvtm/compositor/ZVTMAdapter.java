package fr.inria.zvtm.compositor;

import fr.inria.zvtm.gui.Viewer;



public class ZVTMAdapter implements fr.inria.zvtm.compositor.GenericAdapter {
	public Viewer application;
	private FrameManager fm;

	@Override
	public void init() {
	}
	
	public void setClient(Viewer clientViewer) {		
		this.application = clientViewer;
		fm = new FrameManager(application);
	}

	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		fm.addWindow(window,isroot,x,y,w,h);		

	}

	@Override
	public boolean handleConfigureWindow(int window, boolean isroot, int x,
			int y, int w, int h) {
		if(application.dragging)return false;
		fm.configure(window,x,y,w,h);
		return false;
	}

	@Override
	public void handleCursorPosition(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDestroyWindow(int window) {
		fm.removeWindow(window);
		refresh();
	}

	@Override
	public void handleImageFramebufferUpdate(int window, boolean isroot,
			byte[] img, int x, int y, int w, int h) {
		fm.frameBufferUpdate(window,isroot,img,x,y,w,h);
		refresh();
	}

	@Override
	public void handleRestackWindow(int window, int nextWindow,
			int transientFor, int unmanagedFor, int grabWindow,
			int duplicateFor, int facadeReal, int flags) {
		fm.restackWindow(window, nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	@Override
	public void handleServerCutText(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUnmapWindow(int window) {
		fm.UnmapWindow(window);
		refresh();

	}

	private void refresh(){
		application.refresh();
	}

	public MetisseWindow get(int currentWindow) {
		if(fm==null)return null;
		return fm.get(currentWindow);
	}



}
