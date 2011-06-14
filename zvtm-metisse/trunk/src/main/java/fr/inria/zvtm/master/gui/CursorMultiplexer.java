package fr.inria.zvtm.master.gui;

import java.net.Socket;
import java.util.HashMap;

public class CursorMultiplexer {

	private HashMap<Socket, PCursorPack> cursors;
	private MasterViewer viewer;
	
	
	public CursorMultiplexer(MasterViewer v) {
		this.viewer = v;
		cursors = new HashMap<Socket, PCursorPack>();
	}
	
	public void subscribeClient(Socket s){
		cursors.put(s, new PCursorPack(viewer));
	}

	public void unsubscribeClient(Socket sock) {
		cursors.get(sock).end();
		cursors.remove(sock);
	}

	public void handlePointerEvent(Socket sock, double x, double y,int buttons) {
		cursors.get(sock).handlePointerEvent(x,y,buttons);
	}

	public void handleRemoteKeyEvent(Socket sock,int keysym, int i) {
		cursors.get(sock).handleRemoteKeyEvent(keysym,i);
	}

}
