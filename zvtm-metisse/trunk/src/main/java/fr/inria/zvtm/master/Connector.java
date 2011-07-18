package fr.inria.zvtm.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.kernel.RfbInput;
import fr.inria.zvtm.common.protocol.Owner;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.compositor.MasterCompositor;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;

/**
 * This is the hub class which accepts and handles all the connections from the clients.
 * @author Julien Altieri
 *
 */
public class Connector {
	private MasterCompositor compositor;
	private int listeningPort;
	private HashMap<Socket, RfbAgent> connections;
	private ServerSocket server;
	private ZvtmRfbHandlerMultiplexer rfbInputmultiplex;
	private Timer monitor = new Timer();

	/**
	 * You must provide the {@link MasterCompositor}.
	 * @param compositor
	 */
	public Connector(MasterCompositor compositor) {
		this.compositor = compositor;
		this.connections = new HashMap<Socket, RfbAgent>();
		this.rfbInputmultiplex = new ZvtmRfbHandlerMultiplexer(this.compositor.getViewer().getFrameManager(),this);
	}

	/**
	 * Starts the connection accepter thread. It creates a {@link ServerSocket} on the specified port.
	 * @param listeningPort the listening port of the {@link ServerSocket}.
	 */
	public void init(int listeningPort) {
		this.listeningPort = listeningPort;
		try {
			server = new ServerSocket(this.listeningPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		listenIncomingConnections();
	}

	private void listenIncomingConnections() {
		Thread listConn = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						System.out.println("Server ready\nWaiting for incoming connection...");
						Socket s = server.accept();
						System.out.println("Connection accepted from "+s.getInetAddress()+":"+s.getPort());
						startListening(s).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

		listConn.start();
	}

	/**
	 * The basic listening {@link Thread} which sets up a rfb connection and waits for incoming messages.
	 * @author Julien Altieri
	 *
	 */
	class ListeningThread extends Thread implements Owner{
		@Override
		public void end() {
		}};

		private Thread startListening(final Socket s){

			Thread listen = new ListeningThread(){
				private RfbAgent rfbAgent;
				private Socket sock;
				private boolean consumed = false;

				@Override
				public void run() {
					sock = s;
					try {
						connections.put(sock, new RfbAgent(sock.getInputStream(), sock.getOutputStream(),this));
						rfbAgent = connections.get(sock);
						rfbAgent.rfbProtocalVersion();
						rfbAgent.rfbAuthentification(); // receive, send & receive
						rfbAgent.rfbClientInit(); // send
						rfbAgent.rfbServerInit(); // receive
						rfbAgent.rfbSetPixelFormat(); // send
						rfbAgent.rfbSetEncodings(); // sens
						rfbAgent.rfbFramebufferUpdateRequest(true); // sens
						rfbAgent.addListener(new RfbInput(sock,rfbInputmultiplex));//connect the compositor to the rfb socket
						startMonitoring(rfbAgent);
						rfbAgent.startSender();
					} catch (IOException e1) {
						System.err.println("Connection "+sock.getInetAddress()+":"+sock.getPort()+" closing...");
						end();
						return;
					}

					//	int count = 0;
					while(true){
						if(sock.isClosed())return;
						try {
							receive();
							//		System.out.println(count++);
						} catch (IOException e) {
							System.err.println("connection "+sock.getInetAddress()+":"+sock.getPort()+" closed.");
							end();
						}
					}
				}

				/**
				 * Checks the aliveness of the socket. 
				 * Regularly tries to write in the socket, the IO Exception thrown in case of failure is catch and calls the end() methods. 
				 * @param rfbAgent The rfbAgent that will be checked.
				 */
				private void startMonitoring(final RfbAgent rfbAgent) {
					TimerTask t = new TimerTask() {
						@Override
						public void run() {
							rfbAgent.ping();
						}
					};
					monitor.scheduleAtFixedRate(t, 500, 500);
				}

				public void end() {
					if (sock!=null) try {
						rfbInputmultiplex.remove(sock);
						sock.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				protected boolean receive() throws IOException{
					if(sock == null)
						return false;


					boolean something_read = false;
					boolean ret = false;
					something_read = true;
					int type = rfbAgent.readCard8();
					if(!consumed){
						rfbAgent.orderConfigureWall(PCursor.wallBounds);//we send it now because we are sure that the client is ready
						consumed = true;
					}
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

					case Proto.rfbDoublePointerEvent:
						ret = rfbAgent.rfbDoublePointerEvent();
						if(ret)
							return true;
						break;

					case Proto.rfbFramebufferUpdate:
						ret = rfbAgent.framebufferUpdate(true);
						if (!ret){
							return false;
						}
						return true;

					case Proto.rfbConfigureWall:
						ret = rfbAgent.rfbConfigureWall();
						if (!ret){
							return false;
						}
						return true;

					case Proto.rfbServKeyEvent:
						ret = rfbAgent.rfbRemoteKeyEvent();
						if (!ret){
							return false;
						}
						return true;
					case Proto.pong:
						rfbAgent.ponged();
						return true;

					default:
						return false;
					} 
					return something_read;
				}
			};
			return listen;
		}

		/**
		 * 
		 * @return The linked {@link ZvtmRfbHandlerMultiplexer}
		 */
		public ZvtmRfbHandlerMultiplexer getMultiplexer() {
			return this.rfbInputmultiplex;
		}

		/**
		 * Returns the {@link RfbAgent} linked to the specified {@link Socket}.
		 * @param s a {@link Socket} object.
		 */
		public RfbAgent getRfbAgent(Socket s){
			return connections.get(s);
		}

		/**
		 * Returns the list of all connections' {@link RfbAgent}.
		 */
		public Collection<RfbAgent> getAllAgents() {
			return connections.values();
		}

}
