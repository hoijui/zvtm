package fr.inria.zvtm.common.compositor;





public class ZvtmRfbHandler implements fr.inria.zvtm.common.compositor.RfbMessageHandler {
	protected FrameManager fm;

	public ZvtmRfbHandler(FrameManager fm) {		
		this.fm = fm;
	}

	@Override
	public void addWindow(int window, boolean isroot, int x, int y, int w, int h) {
		fm.addWindow(window,isroot,x,y,w,h);		

	}

	@Override
	public boolean handleConfigureWindow(int window, boolean isroot, int x,int y, int w, int h) {
		if(fm!=null)
		if(fm.get(window)!=null)
		if(isroot&&!fm.get(window).isRoot()){
			fm.get(window).endResize();
		}
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
	}

	@Override
	public void handleImageFramebufferUpdate(int window, boolean isroot,byte[] img, int x, int y, int w, int h) {
		fm.frameBufferUpdate(window,isroot,img,x,y,w,h);
	}

	@Override
	public void handleRestackWindow(int window, int nextWindow,int transientFor, int unmanagedFor, int grabWindow,int duplicateFor, int facadeReal, int flags) {
		fm.restackWindow(window, nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	@Override
	public void handleServerCutText(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUnmapWindow(int window) {
		fm.UnmapWindow(window);
	}


	public MetisseWindow get(int currentWindow) {
		if(fm==null)return null;
		return fm.get(currentWindow);
	}


}
