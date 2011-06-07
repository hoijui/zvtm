package fr.inria.zvtm.compositor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import fr.inria.zvtm.kernel.ClientMain;
import fr.inria.zvtm.kernel.Main;
import fr.inria.zvtm.protocol.Proto;
import fr.inria.zvtm.protocol.RfbAgent;

public class RfbForwarder{

	private Socket sock;
	private InputStream input;
	private OutputStream output;
	private RfbAgent rfbAgent;
	private boolean dead = false;

	public RfbForwarder() {
		connect();
	}

	private void connect() {
		try {
			sock = new Socket(InetAddress.getByName(ClientMain.Hostip),ClientMain.Hostport);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			rfbAgent = new RfbAgent(input, output);
			if(!rfbAgent.servrfbProtocalVersion()){
				System.err.println("the server is not valid, system will exit");
				Main.end();
			}
			rfbAgent.servrfbAuthentication();
			rfbAgent.servrfbClientInit(); // receive
			rfbAgent.servrfbServerInit(); // send
			rfbAgent.servrfbSetPixelFormat(); // receive
			rfbAgent.servrfbSetEncodings(); // send
		} catch (Exception e) {
			System.err.println("Connexion to the frontal server failed, system will run on local...");
			dead = true;
		} 
	}


	public void configure(int window,boolean isroot, int x, int y, int w, int h) {
		if(dead)return;
		rfbAgent.orderConfigure(window,isroot,x,y,w,h);
	}

	public void frameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h) {
		if(dead)return;
		rfbAgent.orderFrameBufferUpdate(window,isroot,img,x,y,w,h,Proto.rfbEncodingRaw);
	}

	public void restackWindow(int window, int nextWindow, int transientFor,int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,int flags) {
		if(dead)return;
		rfbAgent.orderRestackWindow(window,nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	public void UnmapWindow(int window) {
		if(dead)return;
		rfbAgent.orderUnmapWindow(window);
	}

	public void addWindow(int id, boolean root, int x, int y, int w, int h) {
		if(dead)return;
		rfbAgent.orderAddWindow(id,root,x,y,w,h);
	}

	public void removeWindow(int id) {
		if(dead)return;
		rfbAgent.orderRemoveWindow(id);
	}

}
