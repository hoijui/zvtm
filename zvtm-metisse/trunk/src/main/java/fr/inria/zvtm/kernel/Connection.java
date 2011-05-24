package fr.inria.zvtm.kernel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import fr.inria.zvtm.compositor.GenericAdapter;
import fr.inria.zvtm.compositor.InputForwarder;

import fr.inria.zvtm.protocol.Proto;
import fr.inria.zvtm.protocol.RfbAgent;


public class Connection {
	private static String ip;
	private static int port;
	private static Socket sock;
	private static InputStream input;
	private static OutputStream output;
	public static void init(String ip, int port) {
		Connection.ip = ip;
		Connection.port = port;
		connect();
		InputForwarder.init();
		startListenning();
	}

	private static void startListenning() {
		System.out.println("start listening");
		Thread listen = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(Temporizer.connectionProcessingDelay);
						receive();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		listen.start();
	}

	private static void connect() {
		try {
			sock = new Socket(InetAddress.getByName(ip),port);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			RfbAgent.init(input, output);
			RfbAgent.rfbProtocalVersion(); // receive, send
			RfbAgent.rfbAuthentification(); // receive, send & receive
			RfbAgent.rfbClientInit(); // send
			RfbAgent.rfbServerInit(); // receive
			RfbAgent.rfbSetPixelFormat(); // send
			RfbAgent.rfbSetEncodings(); // sens
			RfbAgent.rfbFramebufferUpdateRequest(true); // sens
			System.out.println("addAdapter");
			RfbAgent.addZVTMAdapter((GenericAdapter) Main.compositor);//connect the compositor to the rfb socket
		} catch (Exception e) {
			System.err.println("Connexion to the metisse server failed, system will exit...");
			Main.end();
		} 

		System.out.println("Connexion established.");
	}

	public static void end() {

		if (sock!=null) try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static boolean receive() throws IOException {
		if(sock == null)
			return false;

		boolean something_read = false;
		boolean ret = false;
		while(input.available() > 0){
			something_read = true;
			int type = RfbAgent.readCard8();

			switch(type){
			case Proto.rfbBell:
				ret = RfbAgent.rfbBell();
				if(ret)
					return true;
				break ;

			case Proto.rfbSetColourMapEntries:
				ret = RfbAgent.rfbSetColourMapEntries();
				if(ret)
					return true;
				break ;

			case Proto.rfbServerCutText:
				ret = RfbAgent.rfbServerCutText();
				if(ret)
					return true;
				break ;

			case Proto.rfbConfigureWindow:
				ret = RfbAgent.rfbConfigureWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbUnmapWindow:
				ret = RfbAgent.rfbUnmapWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbDestroyWindow:
				ret = RfbAgent.rfbDestroyWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbRestackWindow:
				ret = RfbAgent.rfbRestackWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbFramebufferUpdate:
				ret = RfbAgent.framebufferUpdate();
				if (!ret){
					return false;
				}
				return true;

			default:
				return false;
			} 
		}
		return something_read;
	}

}


