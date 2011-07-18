package fr.inria.zvtm.master.compositor;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import fr.inria.zvtm.common.compositor.FrameManager;
import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.compositor.ZvtmRfbHandler;
import fr.inria.zvtm.common.kernel.Pair;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.Connector;
import fr.inria.zvtm.master.gui.MasterViewer;
import fr.inria.zvtm.master.gui.PCursorPack;

/**
 * This class acts like "Network Address Translation" (NAT) but here for the ids of Metisse windows.
 * It modifies each received message by replacing the window's id by a unique cross-socket id, as well as in the other sense.
 * Then messages are sent to the {@link MasterViewer}'s {@link FrameManager}.
 * @author Julien Altieri
 *
 */
public class ZvtmRfbHandlerMultiplexer extends ZvtmRfbHandler {

	private HashMap<String, HashMap<Integer, Integer>> translationTable;//<port,<id,translated id>>
	private HashMap<String,Socket> socks;
	private Random rand;
	private Connector owner;

	/**
	 * @param fm The {@link FrameManager} which will handle messages from all (the {@link MasterViewer}'s {@link FrameManager}
	 * @param owner The parent {@link Connector} 
	 */
	public ZvtmRfbHandlerMultiplexer(FrameManager fm, Connector owner) {
		super(fm);
		translationTable = new HashMap<String, HashMap<Integer,Integer>>();
		socks = new HashMap<String, Socket>();
		this.rand = new Random();
		this.owner = owner;
	}


	/**
	 * Does the translation process.
	 * @param sock The {@link Socket} from which comes the window
	 * @param window The original id of the window
	 * @return A unique id for this window 
	 */
	public int get(Socket sock, int window) {
		String client = sock.getInetAddress()+":"+sock.getPort();
		socks.put(client, sock);
		if(!translationTable.containsKey(client))translationTable.put(client,new HashMap<Integer, Integer>());
		if(!translationTable.get(client).containsKey(window))translationTable.get(client).put(window, generateUniqueId());
		return translationTable.get(client).get(window);
	}

	private Integer generateUniqueId() {
		int res = 0;
		boolean checked = false;
		while(!checked ){
			checked = true;
			res = (rand).nextInt();
			for (HashMap<Integer, Integer> hm : translationTable.values()) {
				if(hm.containsKey(res)){
					checked = false;
					break;					
				}
			}
		}
		return res;
	}

	/**
	 * Removes the specified {@link Socket} and all her related entries from all the tables.
	 * @param sock
	 */
	public void remove(Socket sock) {
		String client = "";
		for (String s : socks.keySet()) {
			if(socks.get(s)==sock){
				client = s;
				break;
			}
		}
		
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().unsubscribeClient(sock);
		try {
			for (Integer win : translationTable.get(client).values()) {
				fm.removeWindow(win);
			}
			socks.remove(client);			
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @return The {@link FrameManager} which handles all the Metisse windows.
	 */
	public FrameManager getFrameManager() {
		return fm;
	}

	/**
	 * Sends the cursor event to the virtual cursor associated with the specified {@link Socket}.
	 * @see PCursorPack
	 * @param sock the original {@link Socket}
	 * @param x virtual x coordinate
	 * @param y virtual y coordinate
	 * @param buttons button mask
	 */
	public void handleDoublePointerEvent(Socket sock, double x,double y, int buttons) {
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().handlePointerEvent(sock,x,y,buttons);			
	}

	/**
	 * One {@link Socket} is associated with only one virtual cursor. This method sends the key event to the {@link MetisseWindow} under the {@link Socket}'s virtual cursor.
	 * @see PCursorPack
	 * @param sock the original {@link Socket}
	 * @param keysym
	 * @param i (1 for down, 0 for up)
	 */
	public void handleRemoteKeyEvent(Socket sock,int keysym, int i) {
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().handleRemoteKeyEvent(sock,keysym,i);			
	}


	/**
	 * Given the specified unique id, untranslates into the original {@link Socket} and id.
	 * @param window the unique cross-socket id 
	 * @return A {@link Pair} containing untranslated information ({@link Socket} and id)
	 */
	public Pair find( int window) {
		for (String s : translationTable.keySet()) {			
			for (int id : translationTable.get(s).keySet()) {
				if(translationTable.get(s).get(id)==window){
					return new Pair(id, socks.get(s));
				}
			}
		}
		return null;
	}


	/**
	 * 
	 * @param s a {@link Socket} object
	 * @return The {@link RfbAgent} linked to the specified {@link Socket}.
	 */
	public RfbAgent getRfbAgent(Socket s) {
		return owner.getRfbAgent(s);
	}


	/**
	 * Returns the list of all connections' {@link RfbAgent}.
	 */
	public Collection<RfbAgent> getAllAgents() {
		return owner.getAllAgents();
	}




}
