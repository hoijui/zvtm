package fr.inria.zvtm.common.compositor;

import fr.inria.zvtm.common.protocol.RfbAgent;




/**
 * This class is called by the {@link RfbAgent} to handle RBF messages. 
 * @author Julien
 *
 */
public interface RfbMessageHandler {

	/**
	 * Modify the position or size of a window
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	boolean handleConfigureWindow(int window, boolean isroot, int x, int y,	int w, int h);

	/**
	 * Creation of a new window
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	void addWindow(int window, boolean isroot, int x, int y, int w, int h);

	/**
	 * Not implemented, represent the cut text of the server
	 * @param str the cut text
	 */
	void handleServerCutText(String str);

	/**
	 * Restack the given window. (Make it visible)
	 * @param window the id of the window
	 * @param nextWindow the id of the next window in the stack (use for drawing)
	 * @param transientFor potential parent of the frame
	 * @param unmanagedFor potential parent of the frame
	 * @param grabWindow potential parent of the frame
	 * @param duplicateFor facade flag
	 * @param facadeReal facade flag
	 * @param flags facade flag
	 */
	void handleRestackWindow(int window, int nextWindow, int transientFor,int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,int flags);

	/**
	 * Hide the given window (applies also for vanishing menus)
	 * @param window the window to be unmapped
	 */
	void handleUnmapWindow(int window);

	/**
	 * Destroys the given window.
	 * @param window the window to be destroy
	 */
	void handleDestroyWindow(int window);

	/**
	 * Updates the related {@link MetisseWindow}'s raster according to the byte[].
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 */
	void handleImageFramebufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h);

	/**
	 * Server's cursor position information.
	 * @param x position in the server
	 * @param y position in the server
	 */
	void handleCursorPosition(int x, int y);


}
