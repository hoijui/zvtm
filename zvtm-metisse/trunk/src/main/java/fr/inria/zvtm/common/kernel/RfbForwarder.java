package fr.inria.zvtm.common.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import fr.inria.zvtm.client.ClientMain;
import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.protocol.Owner;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.common.protocol.RfbAgent;

/**
 * Redirects input events from the zvtm server to the X server.
 * @author Julien Altieri
 *
 */
public class RfbForwarder implements Owner{

	private Socket sock;
	private InputStream input;
	private OutputStream output;
	private RfbAgent rfbAgent;
	private boolean dead = false;

	public RfbForwarder() {
		connect();
	}

	/**
	 * Connection beetween the client and the zvtm server.
	 * Forwards FB events, window managing events for public windows, and input events when the cursor in virtual mode.
	 */
	private void connect() {
		try {
			sock = new Socket(InetAddress.getByName(ClientMain.Hostip),ClientMain.Hostport);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			rfbAgent = new RfbAgent(input, output,this);
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


	@Override
	public void end() {
		System.out.println("RFB Forwarder Connection ended");
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ListeningThread extends Thread implements Owner{
		@Override
		public void end() {
		}};
	
	private void startListening(){
		
		Thread listen = new ListeningThread(){
			RfbAgent fwagent;

			@Override
			public void run() {
				fwagent  = new RfbAgent(input, ClientMain.connection.getRfbAgent().getOut(),this);
				while(true){
					try {
						receive();
					} catch (IOException e) {
						end();
						break;
					}
				}
			}

			@Override
			public void end() {
				System.out.println("connection "+sock.getInetAddress()+":"+sock.getPort()+" closed.");
			}
			
			protected boolean receive() throws IOException{
				if(sock == null)
					return false;

				boolean something_read = false;
				boolean ret = false;
				something_read = true;
				int type = fwagent.readCard8();
				switch(type){
				case Proto.rfbConfigureWall:
					ret = fwagent.handleConfigureWall();
					((ClientViewer)ClientMain.getViewer()).handleResetCursor();
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
				
				case Proto.ping:
					rfbAgent.pong();
					break;

				default:
					return false;
				} 
				return something_read;
			}
		};
		listen.start();
	}

	/**
	 * Sends a ConfigureWindow message to the zvtm server.
	 * @param window the id of the configured window
	 * @param isroot is the window the root frame?
	 * @param x the new x coordinate in the X server
	 * @param y the new y coordinate in the X server
	 * @param w the new width in the X server
	 * @param h the new height in the X server
	 */
	public void configure(int window,boolean isroot, int x, int y, int w, int h){
		if(dead)return;
		rfbAgent.orderConfigure(window,isroot,x,y,w,h);
	}

	/**
	 * Sends a FrameBufferUpdate message to the zvtm server.
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 */
	public void frameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h){
		if(dead)return;
		rfbAgent.orderFrameBufferUpdate(window,isroot,img,x,y,w,h,Proto.rfbEncodingRaw);
	}

	/**
	 * Sends a RestackWindow message to the zvtm server.
	 * @param window the window to restack
	 * @param nextWindow the next window in the stack 
	 * @param transientFor potential parent of the frame
	 * @param unmanagedFor potential parent of the frame
	 * @param grabWindow potential parent of the frame
	 * @param duplicateFor Facade flag
	 * @param facadeReal Facade flag
	 * @param flags Facade flag
	 */
	public void restackWindow(int window, int nextWindow, int transientFor,int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal,int flags){
		if(dead)return;
		rfbAgent.orderRestackWindow(window,nextWindow,transientFor,unmanagedFor,grabWindow,duplicateFor,facadeReal,flags);
	}

	/**
	 * Sends a UnmapWindow message to the zvtm server.
	 * @param window
	 */
	public void UnmapWindow(int window){
		if(dead)return;
		rfbAgent.orderUnmapWindow(window);
	}

	/**
	 * Sends a AddWindow message to the zvtm server.
	 * @param window The id of the new window
	 * @param isroot is it the root frame?
	 * @param x The window's x position in the X server
	 * @param y The window's y position in the X server
	 * @param w The window's width in the X server
	 * @param h The window's height in the X server
	 */
	public void addWindow(int id, boolean root, int x, int y, int w, int h){
		if(dead)return;
		rfbAgent.orderAddWindow(id,root,x,y,w,h);
	}

	
	/**
	 * Sends a RemoveWindow message to the zvtm server.
	 * @param id
	 */
	public void removeWindow(int id){
		if(dead)return;
		rfbAgent.orderRemoveWindow(id);
	}

	/**
	 * Sends a pointer event to the zvtm server, using double coordinates
	 * @param x The virtual x coordinate of the event
	 * @param y The virtual y coordinate of the event
	 * @param buttonMask The mutton mask
	 */
	public void sendPointerEvent(double x, double y, int buttonMask){
		if(dead)return;
		rfbAgent.orderPointerEvent(x,y,buttonMask);
	}

	/**
	 * Sends a key event to the zvtm server
	 * @param code the virtual key code
	 * @param i the press state of the key (1 for down, 0 for up)
	 */
	public void sendKeyEvent(int code, int i){
		if(dead)return;
		rfbAgent.orderKeyEvent(code, i==1);
	}

	/**
	 * Starts the listening {@link Thread} from the zvtm server.
	 */
	public void startListeningBounces() {
		startListening();
	}

}
