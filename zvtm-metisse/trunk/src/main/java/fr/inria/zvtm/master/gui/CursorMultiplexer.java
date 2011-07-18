package fr.inria.zvtm.master.gui;

import java.net.Socket;
import java.util.HashMap;

/**
 * This class retrieves input events from the network and sends them to the right {@link PCursorPack}. 
 * @author Julien Altieri
 */
public class CursorMultiplexer {

	private HashMap<Socket, PCursorPack> cursors;
	private MasterViewer viewer;

	/**
	 * Must specify the related {@link MasterViewer}
	 * @param v
	 */
	public CursorMultiplexer(MasterViewer v) {
		this.viewer = v;
		cursors = new HashMap<Socket, PCursorPack>();
	}

	/**
	 * Creates a {@link PCursorPack} for the specified client's {@link Socket}.
	 * @param s the {@link Socket} representing the client
	 */
	public void subscribeClient(Socket s){
		cursors.put(s, new PCursorPack(viewer));
	}

	/**
	 * Properly removes the client from the list (and all it's Metisse windows)
	 * @param sock
	 */
	public void unsubscribeClient(Socket sock) {
		if(cursors.containsKey(sock)){
			cursors.get(sock).end();
			cursors.remove(sock);
		}
	}

	/**
	 * Transmit a pointer event coming from the network to the specified {@link Socket}'s {@link PCursorPack}.
	 * @param sock represents the client
	 * @param x pointer x coordinate
	 * @param y pointer y coordinate
	 * @param buttons button mask
	 */
	public void handlePointerEvent(Socket sock, double x, double y,int buttons) {
		cursors.get(sock).handlePointerEvent(x,y,buttons);
	}

	/**
	 * Transmit a key event coming from the network to the specified {@link Socket}'s {@link PCursorPack}.
	 * @param sock represents the client
	 * @param keysym 
	 * @param i (1 for down, 0 for up)
	 */
	public void handleRemoteKeyEvent(Socket sock,int keysym, int i) {
		cursors.get(sock).handleRemoteKeyEvent(keysym,i);
	}

	/**
	 * Translate a {@link PCursorPack} into the related {@link Socket}
	 * @param p the {@link PCursorPack} that concerns the client you are looking for
	 * @return his {@link Socket}
	 */
	public Socket find(PCursorPack p){
		for (Socket s : cursors.keySet()) {
			if (p==cursors.get(s))return s;
		}
		return null;
	}

}
