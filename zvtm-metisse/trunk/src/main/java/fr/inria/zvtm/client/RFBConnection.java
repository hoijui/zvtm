package fr.inria.zvtm.client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import fr.inria.zvtm.common.compositor.ZvtmRfbHandler;
import fr.inria.zvtm.common.kernel.Temporizer;
import fr.inria.zvtm.common.protocol.Owner;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.common.protocol.RfbAgent;

/**
 * This class handles the Remote Frame Buffer based (RFB) connection between the Metisse server provided and the client.
 * It handles the reception process but each action calls the handling methods of a ZvtmRfbHandler
 * @author Julien Altieri
 *
 */
public class RFBConnection implements Owner{
	private String ip;
	private int port;
	private Socket sock;
	private InputStream input;
	private OutputStream output;
	private ZvtmRfbHandler rfbInput;
	private RfbAgent rfbAgent;
	
	/**
	 * 
	 * @param ip the address of the Metisse server
	 * @param port the port of the Metisse server
	 * @param rfbInput the ZvtmRFBHandler who will handle the rfb actions
	 * @see #ZvtmRfbHandler
	 */
	public RFBConnection(String ip,int port,ZvtmRfbHandler rfbInput) {
		this.rfbInput = rfbInput;
		this.init(ip, port);
	}

	private void init(String ip, int port) {
		this.ip = ip;
		this.port = port;
		connect();
		startListenning();
	}
	
	/**
	 * 
	 * @return the RfbAgent related to the connection with the Metisse server
	 * @see #RfbAgent
	 */
	public RfbAgent getRfbAgent(){
		return rfbAgent;
	}

	private void startListenning() {
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

	private void connect() {
		try {
			sock = new Socket(InetAddress.getByName(ip),port);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			rfbAgent = new RfbAgent(input, output,this);
			rfbAgent.rfbProtocalVersion(); // receive, send
			rfbAgent.rfbAuthentification(); // receive, send & receive
			rfbAgent.rfbClientInit(); // send
			rfbAgent.rfbServerInit(); // receive
			rfbAgent.rfbSetPixelFormat(); // send
			rfbAgent.rfbSetEncodings(); // sens
			rfbAgent.rfbFramebufferUpdateRequest(true); // sens
			rfbAgent.addListener(rfbInput);//connect the compositor to the rfb socket			
			rfbAgent.startSender();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.err.println("Connexion to the metisse server failed, system will exit...");
			System.exit(1);
		} 

		System.out.println("Connexion established.");
	}

	/**
	 * to close the connection properly
	 */
	public void end() {

		if (sock!=null) try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean receive() throws IOException, InterruptedException {
		if(sock == null)
			return false;

		boolean something_read = false;
		boolean ret = false;
		something_read = true;
		
		int type = rfbAgent.readCard8();

		switch(type){
		case Proto.rfbBell:
			ret = rfbAgent.rfbBell();
			if(ret)
				return true;
			break ;

		case Proto.rfbSetColourMapEntries:
			ret = rfbAgent.rfbSetColourMapEntries();
			if(ret)
				return true;
			break ;

		case Proto.rfbServerCutText:
			ret = rfbAgent.rfbServerCutText();
			if(ret)
				return true;
			break ;

		case Proto.rfbConfigureWindow:
			ret = rfbAgent.rfbConfigureWindow();
			if(ret)
				return true;
			break;

		case Proto.rfbUnmapWindow:
			ret = rfbAgent.rfbUnmapWindow();
			if(ret)
				return true;
			break;

		case Proto.rfbDestroyWindow:
			ret = rfbAgent.rfbDestroyWindow();
			if(ret)
				return true;
			break;

		case Proto.rfbRestackWindow:
			ret = rfbAgent.rfbRestackWindow();
			if(ret)
				return true;
			break;

		case Proto.rfbFramebufferUpdate:
			ret = rfbAgent.framebufferUpdate(false);
			if (!ret){
				return false;
			}
			return true;

		default:
			return false;
		} 
		return something_read;
	}

}


