package fr.inria.zvtm.common.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import fr.inria.zvtm.client.ClientMain;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.common.protocol.RfbAgent;

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
				System.exit(0);
			}
			rfbAgent.servrfbAuthentication();
			rfbAgent.servrfbClientInit(); // receive
			rfbAgent.servrfbServerInit(); // send
			rfbAgent.servrfbSetPixelFormat(); // receive
			rfbAgent.servrfbSetEncodings(); // send
			
			rfbAgent.startSender();
		} catch (Exception e) {
			System.err.println("Connexion to the frontal server failed, system will run on local...");
			dead = true;
		} 
	}


	private void startListening(){
		Thread listen = new Thread(){
			RfbAgent fwagent;

			@Override
			public void run() {
				fwagent  = new RfbAgent(input, ClientMain.connection.getRfbAgent().getOut());
				while(true){
					try {
						receive();
					} catch (IOException e) {
						System.out.println("connection "+sock.getInetAddress()+":"+sock.getPort()+" closed.");
					}
				}
			}


			protected boolean receive() throws IOException {
				if(sock == null)
					return false;

				boolean something_read = false;
				boolean ret = false;
				something_read = true;
				int type = fwagent.readCard8();
				switch(type){
				case Proto.rfbConfigureWall:
					ret = fwagent.handleConfigureWall();
					if(ret)
						return true;
					break ;
					
				case Proto.rfbPointerEvent:
					ret = fwagent.fwPointerEvent();
					if(ret)
						return true;
					break;
					
				case Proto.rfbServKeyEvent:
					ret = fwagent.fwKeyEvent();
					if(ret)
						return true;
					break;

				default:
					return false;
				} 
				return something_read;
			}
		};
		listen.start();
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


	public void sendPointerEvent(double x, double y, int buttonMask) {
		if(dead)return;
		rfbAgent.orderPointerEvent(x,y,buttonMask);
	}

	public void sendKeyEvent(int code, int i) {
		if(dead)return;
		rfbAgent.orderKeyEvent(code, i==1);
	}

	public void startListeningBounces() {
		startListening();
	}

}
