package kernel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
//import java.util.LinkedList;

import Compositors.ZVTMAdapter;
import Protocol.InputForwarder;
import Protocol.Proto;
import Protocol.rfbAgent;





public class Connexion {
	private String ip;
	private int port;
	private Socket sock;
	private InputStream input;
	private OutputStream output;
	private rfbAgent rfb;
	//	private LinkedList<byte[]> writeBuffer;
	private long delay = 20;
	public InputForwarder infw;


	public Connexion(String ip, int port) {
		this.ip = ip;
		this.port = port;
		//		writeBuffer = new LinkedList<byte[]>();
		initConnexion();
		setZVTMAdapter(Main.renderer);
		infw = new InputForwarder(rfb);
		start();
	}

	private void start() {
		Thread listen = new Thread(){
			@Override
			public void run() {
				while(true){

					try {
						Thread.sleep(delay);
						receive();
						//	System.out.println("new rfbMessage");
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

	private void initConnexion() {
		try {
			sock = new Socket(InetAddress.getByName(ip),port);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			rfb = new rfbAgent(input, output);
			rfb.rfbProtocalVersion(); // receive, send
			rfb.rfbAuthentification(); // receive, send & receive
			rfb.rfbClientInit(); // send
			rfb.rfbServerInit(); // receive
			rfb.rfbSetPixelFormat(); // send
			rfb.rfbSetEncodings(); // sens
			rfb.rfbFramebufferUpdateRequest(true); // sens

		} catch (Exception e) {
			System.err.println("Connexion to the metisse server failed, system will exit...");
			Main.end();
		} 

		System.out.println("Connexion established.");
	}

	public void setZVTMAdapter(ZVTMAdapter a){
		rfb.addZVTMAdapter(a);
	}

	public void end() {

		if (sock!=null) try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void requestFrameBufferUpdate(int x,int y,int w, int h){
		try {
			System.out.println("request");
			System.out.println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
			rfb.rfbFramebufferUpdateRequest(x, y, w, h, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean receive() throws IOException {
		if(sock == null)
			return false;

		boolean something_read = false;
		boolean ret = false;
		while(input.available() > 0){
			something_read = true;
			int type = rfb.readCard8();

			switch(type){
			case Proto.rfbBell:
				ret = rfb.rfbBell();
				if(ret)
					return true;
				break ;

			case Proto.rfbSetColourMapEntries:
				ret = rfb.rfbSetColourMapEntries();
				if(ret)
					return true;
				break ;

			case Proto.rfbServerCutText:
				ret = rfb.rfbServerCutText();
				if(ret)
					return true;
				break ;

			case Proto.rfbConfigureWindow:
				ret = rfb.rfbConfigureWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbUnmapWindow:
				ret = rfb.rfbUnmapWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbDestroyWindow:
				ret = rfb.rfbDestroyWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbRestackWindow:
				ret = rfb.rfbRestackWindow();
				if(ret)
					return true;
				break;

			case Proto.rfbFramebufferUpdate:
				ret = rfb.framebufferUpdate();
				if (!ret){
					return false;
				}
				return true;
				//break;

			default:
				//   Debug.warning("unknown message type " + type + " from Metisse server");
				return false;
			} /* switch */
		}

		return something_read;
	}

}


