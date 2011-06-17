package fr.inria.zvtm.master.compositor;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import fr.inria.zvtm.common.compositor.FrameManager;
import fr.inria.zvtm.common.compositor.ZvtmRfbHandler;
import fr.inria.zvtm.common.kernel.Pair;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.Connector;
import fr.inria.zvtm.master.gui.MasterViewer;

public class ZvtmRfbHandlerMultiplexer extends ZvtmRfbHandler {

	private HashMap<String, HashMap<Integer, Integer>> translationTable;//<port,<id,translated id>>
	private HashMap<String,Socket> socks;
	private Random rand;
	private Connector owner;

	public ZvtmRfbHandlerMultiplexer(FrameManager fm, Connector owner) {
		super(fm);
		translationTable = new HashMap<String, HashMap<Integer,Integer>>();
		socks = new HashMap<String, Socket>();
		this.rand = new Random();
		this.owner = owner;
	}


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

	public void remove(Socket sock) {
		String client = "";
		for (String s : socks.keySet()) {
			if(socks.get(s)==sock){
				client = s;
				break;
			}
		}
		
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().unsubscribeClient(sock);
		for (Integer win : translationTable.get(client).values()) {
			fm.removeWindow(win);
		}
		socks.remove(client);
	}

	public FrameManager getFrameManager() {
		return fm;
	}

	public void handleDoublePointerEvent(Socket sock, double x,double y, int buttons) {
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().handlePointerEvent(sock,x,y,buttons);			
	}

	public void handleRemoteKeyEvent(Socket sock,int keysym, int i) {
		((MasterViewer)fm.getViewer()).getCursorMultiplexer().handleRemoteKeyEvent(sock,keysym,i);			
	}


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


	public RfbAgent getRfbAgent(Socket s) {
		return owner.getRfbAgent(s);
	}


	public Collection<RfbAgent> getAllAgents() {
		return owner.getAllAgents();
	}



}
