package fr.inria.zvtm.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.common.kernel.RfbInput;
import fr.inria.zvtm.common.protocol.Proto;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.master.compositor.MasterCompositor;
import fr.inria.zvtm.master.compositor.ZvtmRfbHandlerMultiplexer;

public class Connector {
	private MasterCompositor compositor;
	private int listeningPort;
	private HashMap<Socket, RfbAgent> connections;
	private ServerSocket server;
	private ZvtmRfbHandlerMultiplexer rfbInputmultiplex;
	private Timer monitor = new Timer();

	public Connector(MasterCompositor compositor) {
		this.compositor = compositor;
		this.connections = new HashMap<Socket, RfbAgent>();
		this.rfbInputmultiplex = new ZvtmRfbHandlerMultiplexer(this.compositor.getViewer().getFrameManager(),this);
	}

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
						s.setKeepAlive(true);
						connections.put(s, new RfbAgent(s.getInputStream(), s.getOutputStream()));
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


	private Thread startListening(final Socket s){
		Thread listen = new Thread(){
			RfbAgent rfbAgent;
			Socket sock;

			@Override
			public void run() {
				sock = s;
				rfbAgent = connections.get(sock);
				try {
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
					e1.printStackTrace();
				} 

				while(true){
					if(sock.isClosed())return;
					try {
						receive();
					} catch (IOException e) {
						System.out.println("connection "+sock.getInetAddress()+":"+sock.getPort()+" closed.");
						end();
					}
				}
			}

			private void startMonitoring(final RfbAgent rfbAgent) {
				TimerTask t = new TimerTask() {
					RfbAgent rfb = rfbAgent;
					@Override
					public void run() {
						rfb.ping();
						try {
							sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(!rfb.hasResponded()){
							end();
							this.cancel();
						}
					}
				};
				monitor.scheduleAtFixedRate(t, 2000, 2000);
			}

			public void end() {
				if (sock!=null) try {
					rfbInputmultiplex.remove(sock);
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			protected boolean receive() throws IOException {
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
					
				case Proto.rfbDoublePointerEvent:
					ret = rfbAgent.rfbDoublePointerEvent();
					if(ret)
						return true;
					break;
					
				case Proto.rfbFramebufferUpdate:
					ret = rfbAgent.framebufferUpdate();
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

	public ZvtmRfbHandlerMultiplexer getMultiplexer() {
		return this.rfbInputmultiplex;
	}

	public RfbAgent getRfbAgent(Socket s){
		return connections.get(s);
	}

	public Collection<RfbAgent> getAllAgents() {
		return connections.values();
	}
	
}
