package fr.inria.zvtm.common.compositor;




/**
 * Is called by the Rfb handler. 
 * @author insitu
 *
 */
public interface RfbMessageHandler {

	
	boolean handleConfigureWindow(int window, boolean isroot, int x, int y,
			int w, int h);

	void addWindow(int window, boolean isroot, int x, int y, int w, int h);

	void handleServerCutText(String str);

	void handleRestackWindow(int window, int nextWindow, int transientFor,
			int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,
			int flags);

	void handleUnmapWindow(int window);

	void handleDestroyWindow(int window);

	void handleImageFramebufferUpdate(int window, boolean isroot, byte[] img,
			int x, int y, int w, int h);

	void handleCursorPosition(int x, int y);


}
