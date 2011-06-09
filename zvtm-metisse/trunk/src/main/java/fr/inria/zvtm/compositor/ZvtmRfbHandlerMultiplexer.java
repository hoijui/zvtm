package fr.inria.zvtm.compositor;

import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class ZvtmRfbHandlerMultiplexer extends ZvtmRfbHandler {

	private HashMap<String, HashMap<Integer, Integer>> translationTable;//<port,<id,translated id>>
	private Random rand;

	public ZvtmRfbHandlerMultiplexer(FrameManager fm) {
		super(fm);
		translationTable = new HashMap<String, HashMap<Integer,Integer>>();
		this.rand = new Random();
	}

	public void handleCursorPosition(int port, int x, int y) {

	}

	public int get(Socket sock, int window) {
		String client = sock.getInetAddress()+":"+sock.getPort();
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
		String client = sock.getInetAddress()+":"+sock.getPort();
		for (Integer win : translationTable.get(client).values()) {
			fm.removeWindow(win);
		}
	}
}
