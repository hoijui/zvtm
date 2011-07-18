package fr.inria.zvtm.common.protocol;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.common.compositor.RfbMessageHandler;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.kernel.RfbInput;


/**
 * The main Metisse protocol reader and writer class.
 * @author Julien Altieri
 *
 */
public class RfbAgent {

	private BufferedInputStream in;
	private BufferedOutputStream out;
	private String password;
	private LinkedList<RfbMessageHandler> listeners;
	private int height;
	private int width;
	private LinkedBlockingQueue<RFBMessage> toSend;
	private boolean alive = true;
	private Owner owner;

	/**
	 * One must provide the owner of the {@link RfbAgent} since it may ask for the connection to be closed.
	 * @param input the {@link InputStream} of the related {@link Socket}
	 * @param outs the {@link OutputStream} of the related {@link Socket}
	 * @param own the object which owns the {@link RfbAgent}
	 */
	public RfbAgent(InputStream input, OutputStream outs,Owner own) {
		this.owner = own;
		this.out = new BufferedOutputStream(outs);
		this.in = new BufferedInputStream(input);
		toSend = new LinkedBlockingQueue<RFBMessage>();
		password = "insitu";
		listeners = new LinkedList<RfbMessageHandler>();
	}

	/**
	 * Only used for a very specific case (zvtm server => zvtm client => Metisse server) to plug directly the streams.
	 * @param input the {@link InputStream} of the related {@link Socket}
	 * @param out the sending queue of the {@link RfbAgent} who shares the stream
	 * @param own the object which owns the {@link RfbAgent}
	 */
	public RfbAgent(InputStream input, LinkedBlockingQueue<RFBMessage> out,Owner own) {
		this.owner = own;
		in  = new BufferedInputStream(input);
		toSend = out;
		password = "insitu";
		listeners = new LinkedList<RfbMessageHandler>();
	}

	/* ****************************************************************************
	 * Functions for sending messages to the server (display => server)
	 ****************************************************************************/
	static int instance = 0;
	/**
	 * Adds a handler for input messages.
	 */
	public void addListener(RfbMessageHandler a){
		listeners.add(a);
	}
	/**
	 * Starts the sending {@link Thread}.
	 */
	public void startSender(){
		Thread t = new Thread(){
			BufferedOutputStream outstr = out;
			@Override
			public void run() {
				RFBMessage msg = null ;
				while(true){
					byte[] b;
					try {
						msg = toSend.take();
						b = msg.getBytes();		
						outstr.write(b);
						outstr.flush();
					} catch (IOException e) {
						System.err.println("Connection closed");
						alive = false;
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				owner.end();
			};
		};
		t.start();
	}

	/**
	 * Welcome message for the Metisse protocol.
	 * @throws IOException
	 */
	public void rfbProtocalVersion() throws IOException{
		byte[] pv = new byte[16]; // "METISSE 000.000\n"
		readString(pv, 16);     // 0123456789012345

		if(pv[0] != 'M' ||
				pv[1] != 'E' ||
				pv[2] != 'T' ||
				pv[3] != 'I' ||
				pv[4] != 'S' ||
				pv[5] != 'S' || 
				pv[6] != 'E' ||
				pv[7] != ' ' ||
				pv[11] != '.' ||
				pv[15] != '\n' ||
				(pv[ 8] < '0' || pv[ 8] > '9') ||
				(pv[ 9] < '0' || pv[ 9] > '9') ||
				(pv[10] < '0' || pv[10] > '9') || 
				(pv[12] < '0' || pv[12] > '9') || 
				(pv[13] < '0' || pv[13] > '9') ||
				(pv[14] < '0' || pv[14] > '9'))
			throw new RuntimeException("Not a valid Metisse server");

		int major = (pv[ 8]-'0') * 100 + (pv[ 9]-'0') * 10 + (pv[10]-'0');
		int minor = (pv[12]-'0') * 100 + (pv[13]-'0') * 10 + (pv[14]-'0');

		major = Proto.rfbProtocolMajorVersion ;
		minor = Proto.rfbProtocolMinorVersion ;
		pv[0] = 'M';
		pv[1] = 'E';
		pv[2] = 'T';
		pv[3] = 'I';
		pv[4] = 'S';
		pv[5] = 'S';
		pv[6] = 'E';
		pv[7] = ' ';
		pv[8] = (byte) ((major/100) + '0'); 
		pv[9] = (byte)(((major/10)%10) + '0'); 
		pv[10] = (byte)(((major)%10) + '0');
		pv[11] = '.';
		pv[12] = (byte)((minor/100) + '0'); 
		pv[13] = (byte)(((minor/10)%10) + '0'); 
		pv[14] = (byte)(((minor)%10) + '0');
		pv[15] = '\n';

		out.write(pv,0,16);
		out.flush();

	}




	/**
	 * Authentication procedure.
	 * @throws IOException
	 */
	public void rfbAuthentification() throws IOException{
		int authScheme = readCard32();

		switch (authScheme){
		case Proto.rfbConnFailed:
			int reasonLen = readCard32();
			byte reason[] = new byte[reasonLen];
			readString(reason, reasonLen);
			throw new RuntimeException("Metisse connection failed (" + new String(reason) + ")");

		case Proto.rfbNoAuth:
			break;

		case Proto.rfbMetisseAuth:
			byte challenge[] = new byte[16];
			readString(challenge, 16);

			if (password.length() > 0){
				Authentication auth = new Authentication(password.getBytes());
				auth.encrypt(challenge, 0, challenge, 0);
				auth.encrypt(challenge, 8, challenge, 8);
			}

			out.write(challenge, 0, 16);
			out.flush();
			int authResult = readCard32();

			switch (authResult){
			case Proto.rfbMetisseAuthOK:
				//    Debug.debug("Metisse authentication succeeded");
				break;
			case Proto.rfbMetisseAuthFailed:
				throw new RuntimeException("Metisse authentication failed") ;
			case Proto.rfbMetisseAuthTooMany:
				throw new RuntimeException("Metisse authentication failed - too many tries");
			default:
				throw new RuntimeException("Unknown Metisse authentication result") ;
			}
			break;

		default:
			throw new RuntimeException("Unknown authentication scheme from Metisse server");
		} /* switch (authScheme) */
	}

	/**
	 * Metisse client initialization.
	 * @throws IOException
	 */
	public void rfbClientInit() throws IOException{
		byte shared[] = new byte[1];
		shared[0] = 1;
		out.write(shared, 0, 1);
		out.flush();
	}

	/**
	 * Metisse server initialization.
	 * @throws IOException
	 */
	public void rfbServerInit() throws IOException{
		width = readCard16();
		height = readCard16();
		/* rfbPixelFormat */
		readCard8(); // bpp
		readCard8(); // depth
		readCard8(); // big endian
		readCard8(); // truecolour
		readCard16(); // Max - red
		readCard16(); // green
		readCard16(); // blue
		readCard8(); // shift - red
		readCard8(); // green
		readCard8(); // blue

		int namelen = readCard32();
		byte[] name = new byte[namelen];
		readString(name, namelen);
	}

	/**
	 * Tells the server which pixel format the client uses.
	 * @throws IOException
	 */
	public void rfbSetPixelFormat() throws IOException{
		writeUint8(Proto.rfbSetPixelFormat); // msg type
		writeUint8(32); // bitsPerPixel
		writeUint8(16); // depth
		writeUint8(0); // bigEndian
		writeUint8(1); // trueColour
		writeUint16(255); // redMax
		writeUint16(255); // greenMax
		writeUint16(255); // blueMax
		writeUint8(0); // redShift
		writeUint8(8); // greenShift
		writeUint8(16); // blueShift
		flushUint();
	}


	/**
	 * Encoding negotiation.
	 * @throws IOException
	 */
	public void rfbSetEncodings() throws IOException{
		writeUint8(Proto.rfbSetEncodings); // Msg type
		writeUint16(4); // number of encodings
		writeUint32(Proto.rfbEncodingRaw);
		writeUint32(Proto.rfbEncodingPointerPos);
		writeUint32(Proto.rfbEncodingXCursor);
		writeUint32(Proto.rfbEncodingWindowShape);
		flushUint();
	}


	/**
	 * Asks the Metisse server to send a frame buffer update.
	 * @param incremental
	 * @throws IOException
	 */
	public void rfbFramebufferUpdateRequest(boolean incremental) throws IOException{
		rfbFramebufferUpdateRequest(0, 0, 10 * width, 10 * height, incremental) ;
	}

	/**
	 * Asks the Metisse server to send a frame buffer update on the specified rectangle.
	 * @param x x coordinate of the required rectangle
	 * @param y y coordinate of the required rectangle
	 * @param w width of the required rectangle
	 * @param h height of the required rectangle
	 * @param incremental
	 * @throws IOException
	 */
	public void rfbFramebufferUpdateRequest(int x, int y, int w, int h, boolean incremental) throws IOException{
		send(new FBURequest(x, y, w, h, incremental));
	}


	/**
	 * Sends a key event to the Metisse server.
	 * @param keysym
	 * @param down (1 for down, 0 for up)
	 */
	public void rfbKeyEvent(int keysym, boolean down){
		send(new KeyEvent(keysym, down));
	}


	/**
	 * Sends a pointer event to the Metisse server.
	 * @param window
	 * @param x
	 * @param y
	 * @param buttons
	 */
	public void rfbPointerEvent(int window, int x, int y, int buttons){
		send(new PointerEvent(window, x, y, buttons));
	}


	/**
	 * Sends a pointer event to the Metisse server.
	 * @param window
	 * @param jpx
	 * @param jpy
	 * @param buttonState the int[] representing the button mask
	 */
	public void rfbPointerEvent(int window, int jpx, int jpy, int[] buttonState){
		int a = 0;
		for (int i = 0; i < 32; i++) {
			a = a | (buttonState[i]<<i);
		}

		rfbPointerEvent(window,jpx,jpy,a);
	}

	protected void readString(byte data[], int length) throws IOException {
		int	read_total = 0;
		int res = 0;

		while (read_total < length) {
			res = in.read(data, read_total, length - read_total);
			if (res <= 0)
				return;
			read_total += res;
		}
	}

	
	protected void writeString(byte[] data, int length) throws IOException {
		out.write(data, 0, length);	
		out.flush();
	}

	/**
	 * Reads 1 byte from the input stream.
	 * @throws IOException
	 */
	public int readCard8() throws IOException{
		return ( (byte)in.read() & 0xff );
	}


	/**
	 * Reads 2 bytes from the input stream.
	 * @throws IOException
	 */
	private int readCard16() throws IOException{
		return ( ( (byte)in.read() << 8 ) & 0xff00 ) 
		| (  (byte)in.read()    & 0x00ff );
	}

	/**
	 * Reads 4 bytes from the input stream.
	 * @throws IOException
	 */
	private int readCard32() throws IOException {
		return ( ( (byte)in.read() << 24 ) & 0xff000000 ) 
		| ( ( (byte)in.read() << 16 ) & 0x00ff0000 ) 
		| ( ( (byte)in.read() << 8 ) & 0x0000ff00 ) 
		| (  (byte)in.read()     & 0x000000ff );
	}


	private  byte[] waiting_uint = new byte[1024];
	private  int waiting_uint_size = 0; 

	/**
	 * Must use one of RFBMessage.
	 * @see RFBMessage
	 * @deprecated
	 * @param v
	 */
	private  void writeUint32(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0xff000000 ) >>> 24 );
		waiting_uint[waiting_uint_size+1] = (byte)( ( v & 0x00ff0000 ) >>> 16 );
		waiting_uint[waiting_uint_size+2] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+3] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 4;
	}

	/**
	 * Must use one of RFBMessage.
	 * @see RFBMessage
	 * @deprecated
	 * @param v
	 */
	private  void writeUint16(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+1] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 2;
	}

	/**
	 * Must use one of RFBMessage.
	 * @see RFBMessage
	 * @deprecated
	 * @param v
	 */
	private  void writeUint8(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( v & 0xff );
		waiting_uint_size += 1;
	}

	/**
	 * @deprecated
	 * Must use the {@link RfbAgent}{@link #send(RFBMessage)} instead.
	 * @throws IOException
	 */
	private void flushUint() throws IOException{
		out.write(waiting_uint, 0, waiting_uint_size);
		waiting_uint_size = 0;
		out.flush();
	}









	/* ****************************************************************************
	 * Functions for reaction to the server => to display
	 ****************************************************************************/

	public  void handle(){

	}

	private  boolean verbose = false;


	/**
	 * Handles configure window messages.
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	public  void handleConfigureWindow(int window, boolean isroot, int x, int y, int w, int h){
		boolean consumed = false;
		if (verbose) System.out.println("configure "+window);
		for(RfbMessageHandler l : listeners){
			consumed = l.handleConfigureWindow(window, isroot, x, y, w, h) || consumed;
		}

		if(!consumed){
			for(RfbMessageHandler l : listeners){
				l.addWindow(window, isroot, x, y, w, h);
			}
			if (verbose) System.out.println("add "+window);
		}
	}


	/**
	 * Handles server's cursor position information.
	 * @param x position in the server
	 * @param y position in the server
	 */
	public void handleCursorPosition(int x, int y) {
		for(RfbMessageHandler l : listeners){
			l.handleCursorPosition(x, y);
		}
	}


	/**
	 * Handles FB Update messages. Updates the related {@link MetisseWindow}'s raster according to the byte[].
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 */
	public  void handleImageFramebufferUpdate(int window, boolean isroot, byte img[], int x, int y, int w, int h){

		for(RfbMessageHandler l : listeners){
			l.handleImageFramebufferUpdate(window, isroot, img, x, y, w, h);
		}
	}

	/**
	 * Handles destroy window messages.
	 * @param window the window to be destroy
	 */
	public  void handleDestroyWindow(int window){
		for(RfbMessageHandler l : listeners){
			l.handleDestroyWindow(window);
		}
		if (verbose) System.out.println("destroy "+window);
	}

	/**
	 * Handles unmap window messages. Hide the given window (applies also for vanishing menus)
	 * @param window the window to be unmapped
	 */
	public  void handleUnmapWindow(int window){
		for(RfbMessageHandler l : listeners){
			l.handleUnmapWindow(window); 
		}
		if (verbose) System.out.println("unmap "+window);
	}

	/**
	 * Handles server cut text messages (not implemented)
	 * @param str the server cut text
	 */
	public  void handleServerCutText(String str){
		for(RfbMessageHandler l : listeners){
			l.handleServerCutText(str);
		}
	}

	/**
	 * Handles restack window messages. Restack the given window. (Make it visible)
	 * @param window the id of the window
	 * @param nextWindow the id of the next window in the stack (use for drawing)
	 * @param transientFor potential parent of the frame
	 * @param unmanagedFor potential parent of the frame
	 * @param grabWindow potential parent of the frame
	 * @param duplicateFor facade flag
	 * @param facadeReal facade flag
	 * @param flags facade flag
	 */
	public  void handleRestackWindow(int window, int nextWindow, int transientFor, int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal, int flags){
		for(RfbMessageHandler l : listeners){
			l.handleRestackWindow(window, nextWindow, transientFor, unmanagedFor, grabWindow, duplicateFor, facadeReal, flags);
		}
		if (verbose) System.out.println("restack "+window);
	}


	/**
	 * Handles key event messages from a client to the server.
	 * @param keysym
	 * @param i (down: 1, up: 0)
	 */
	public void handleRemoteKeyEvent(int keysym, int i) {
		for(RfbMessageHandler l : listeners){
			if(l instanceof RfbInput){
				((RfbInput)l).handleRemoteKeyEvent(keysym, i);
			}
		}
	}

	/**
	 * Reads FB update messages.
	 * @param slave configuration boolean, the zvtm server should set it to true while a simple client should not. If set to false, FBU request will be systematically sent at the end of the message. 
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public  boolean framebufferUpdate(boolean slave) throws IOException{
		int nRects = readCard16();
		int window = readCard32();
		int topWindow = readCard32();

		int shmid = readCard32();

		boolean isRoot = (topWindow == 0);

		for(int i = 0; i < nRects; i++){
			int x = readCard16(); // rfbRectangle
			int y = readCard16(); // rfbRectangle
			int w = readCard16(); // rfbRectangle
			int h = readCard16(); // rfbRectangle
			int encoding = readCard32(); // one of the encoding types rfbEncoding...

			if(encoding == Proto.rfbEncodingXCursor) {
				int bpr = (w + 7) / 8;
				int bd = bpr * h;
				byte buf[] = new byte[bd * 2];

				if (w * h != 0){
					int foreRed = readCard8();
					int foreGreen = readCard8();
					int foreBlue = readCard8();
					int backRed = readCard8();
					int backGreen = readCard8();
					int backBlue = readCard8();

					readString(buf, bd * 2);
				}
				continue;
			}

			if (encoding == Proto.rfbEncodingPointerPos){
				handleCursorPosition(x, y);
				continue;
			}


			if (encoding == Proto.rfbEncodingWindowShape){
				/* rect.r contains the size of the window */
				int nrects = readCard32();
				int[] buf = null;
				if(nrects > 0){
					buf = new int[nrects * 4];
					for(int j = 0; j < nrects; j++){
						buf[j*4 ] = readCard16(); // x
						buf[j*4+1] = readCard16(); // y
						buf[j*4+2] = readCard16(); // w
						buf[j*4+3] = readCard16(); // h
					}
				}
				continue;
			}

			if (h * w == 0){
				continue;
			}

			int rectImgSize = 0;
			byte ptr[] = null;

			switch(encoding){
			case Proto.rfbEncodingARGBCursor:
				int buffer_size = w * h * 4;
				byte buffer[] = new byte[buffer_size];
				readString(buffer, buffer_size);
				continue;

			case Proto.rfbEncodingRaw:
				rectImgSize = w * h * 4 ;
				ptr = new byte[rectImgSize] ;
				readString(ptr, rectImgSize);
				handleImageFramebufferUpdate(window, isRoot, ptr, x, y, w, h);
				break;

			case Proto.rfbEncodingCoRRE:
				int nSubrects = readCard32();
				byte pixel[] = new byte[4];
				readString(pixel, 4);
				for (int j = 0; j < nSubrects; j++){
					readString(pixel, 4);
					int rx = readCard8();
					int ry = readCard8();
					int rw = readCard8();
					int rh = readCard8();
				}
				break;

			default:
				return false;
			}
		}
		if(!slave)rfbFramebufferUpdateRequest(true);
		return true;
	}


	/**
	 * Reads and handles pointer events (in double coordinates) from a client to the server.
	 * @throws IOException
	 */
	public boolean rfbDoublePointerEvent() throws IOException {
		int buttons = readCard32();//buttons
		int lx = readCard32();//length of x
		int ly = readCard32();//length of y
		double x = Float.intBitsToFloat(lx);
		double y = Float.intBitsToFloat(ly);
		for(RfbMessageHandler l : listeners){
			if(l instanceof RfbInput){
				((RfbInput)l).handleDoublePointerEvent(x,y,buttons);
			}
		}
		return true;
	}

	/**
	 * Reads and handles configure wall messages (information of the zvtm bounds of the wall).
	 * @throws IOException
	 */
	public boolean rfbConfigureWall() throws IOException {
		double[] res =new double[4];
		for (int i = 0; i < 4; i++) {
			res[i] = Float.intBitsToFloat(readCard32());
		}
		PCursor.wallBounds = res;
		return true;
	}

	/**
	 * Reads a key event from a client to the zvtm server.
	 * @throws IOException
	 */
	public boolean rfbRemoteKeyEvent() throws IOException {
		int i = readCard8();//down
		int keysym = readCard32();
		handleRemoteKeyEvent(keysym,i);
		return false;
	}

	public  boolean rfbBell(){
		return false;
	}

	/**
	 * Reads a configure message for color map entries.
	 * @throws IOException
	 */
	public boolean rfbSetColourMapEntries() throws IOException {
		readCard16(); // firstColour
		int nColours = readCard16(); // nColours;
		for(int i = 0; i < nColours; i++){
			readCard16(); // red
			readCard16(); // green
			readCard16(); // blue
		}
		return false;
	}

	/**
	 * Reads the server's cut text. (Not handled here)
	 * @throws IOException
	 */
	public boolean rfbServerCutText() throws IOException {
		int length = readCard32(); // length;
		byte str[] = new byte[length];
		readString(str, length);
		return false;
	}

	/**
	 * Reads a configure window message.
	 * @throws IOException
	 */
	public boolean rfbConfigureWindow() throws IOException{
		int window = readCard32(); // window id
		int xsgn = readCard16(); // xsgn : 0: negative, 1: positive
		int x = (xsgn == 0 ? -1 : 1) * readCard16(); // x
		int ysgn = readCard16(); // ysgn : 0: negative, 1: positive
		int y = (ysgn == 0 ? -1 : 1) * readCard16(); // y
		int width = readCard32(); // width
		int height = readCard32(); // height
		int isroot = readCard16(); // root window : 1, not root window : 0 
		handleConfigureWindow(window, isroot != 0, x, y, width, height);
		rfbFramebufferUpdateRequest(true);
		return true;
	}

	/**
	 * Reads and handles a unmap window message.
	 * @throws IOException
	 */
	public boolean rfbUnmapWindow() throws IOException {
		int window = readCard32(); // window id
		handleUnmapWindow(window);
		return true;
	}

	/**
	 * Reads and handles a destroy window message.
	 * @throws IOException
	 */
	public boolean rfbDestroyWindow() throws IOException {
		int window = readCard32(); // window id
		handleDestroyWindow(window);
		return true;
	}

	/**
	 * Reads and handles a restack window message.
	 * @throws IOException
	 */
	public  boolean rfbRestackWindow() throws IOException{
		int window = readCard32(); // window id
		int nextWindow = readCard32(); 
		int transientFor = readCard32();
		int unmanagedFor = readCard32();
		int grabWindow = readCard32();
		int duplicateFor = readCard32();
		int facadeReal = readCard32();
		int flags = readCard32();
		handleRestackWindow(window, nextWindow, transientFor, unmanagedFor, grabWindow, duplicateFor, facadeReal, flags);
		rfbFramebufferUpdateRequest(true);
		return true;  
	}

	/*****************************************************************************
	 * Functions for bouncing messages to a client
	 ****************************************************************************/



	/**
	 * Sends a configure window message to the zvtm client.
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	public void orderConfigure(int window, boolean isroot, int x, int y, int w, int h)   {
		send(new ConfigureMsg(window, isroot, x, y, w, h));
	}

	/**
	 * Sends a configure wall message to the zvtm client.
	 * @param bounds the virtual bounds of the wall
	 */
	public void orderConfigureWall(double[] bounds)   {
		send(new ConfigureWallMsg(bounds));
	}

	/**
	 * Sends a frame buffer update to the zvtm client.
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 */
	public void orderFrameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h,int encoding)   {
		send(new FBUMsg(window, isroot, img, x, y, w, h, encoding));
	}

	/**
	 * Sends a restack window message to the zvtm client.
	 * @param window the id of the window
	 * @param nextWindow the id of the next window in the stack (use for drawing)
	 * @param transientFor potential parent of the frame
	 * @param unmanagedFor potential parent of the frame
	 * @param grabWindow potential parent of the frame
	 * @param duplicateFor facade flag
	 * @param facadeReal facade flag
	 * @param flags facade flag
	 */
	public void orderRestackWindow(int window, int nextWindow,int transientFor, int unmanagedFor, int grabWindow,int duplicateFor, int facadeReal, int flags)   {
		send(new RestackMsg(window, nextWindow, transientFor, unmanagedFor, grabWindow, duplicateFor, facadeReal, flags));
	}

	/**
	 * Sends a unmap window message to the zvtm client.
	 * @param window the window to be unmapped
	 */
	public void orderUnmapWindow(int window)   {
		send(new UnmapMsg(window));
	}

	/**
	 * Sends a add window message to the zvtm client.
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	public void orderAddWindow(int id, boolean root, int x, int y, int w, int h)   {
		orderConfigure(id,root,x, y, w, h);
	}

	/**
	 * Sends a remove window message to the zvtm client.
	 * @param window the window to be removed
	 */
	public void orderRemoveWindow(int window)   {
		send(new RemoveMsg(window));
	}


	/**
	 * Sends a pointer event message to the zvtm client.
	 * @param x virtual x coordinate of the event
	 * @param y virtual y coordinate of the event
	 * @param buttonMask button mask
	 */
	public void orderPointerEvent(double x, double y, int buttonMask)   {
		send(new DoublePointerEventMsg(x, y, buttonMask));
	}

	/**
	 * Sends a key event to the zvtm client.
	 * @param keysym
	 * @param down (1 for down, 0 for up)
	 */
	public void orderKeyEvent(int keysym, boolean down)   {
		send(new ServKeyEvent(keysym, down));
	}


	/**
	 * Protocol version procedure from the server.
	 * @throws IOException
	 */
	public boolean servrfbProtocalVersion() throws IOException{
		byte[] pv = new byte[16]; 

		int major = (pv[ 8]-'0') * 100 + (pv[ 9]-'0') * 10 + (pv[10]-'0');
		int minor = (pv[12]-'0') * 100 + (pv[13]-'0') * 10 + (pv[14]-'0');

		major = Proto.rfbProtocolMajorVersion ;
		minor = Proto.rfbProtocolMinorVersion ;
		pv[0] = 'M';
		pv[1] = 'E';
		pv[2] = 'T';
		pv[3] = 'I';
		pv[4] = 'S';
		pv[5] = 'S';
		pv[6] = 'E';
		pv[7] = ' ';
		pv[11] = '.';
		pv[15] = '\n';
		pv[ 8] = (byte)((major/100) + '0'); pv[ 9] = (byte)(((major/10)%10) + '0'); pv[10] = (byte)(((major)%10) + '0');
		pv[12] = (byte)((minor/100) + '0'); pv[13] = (byte)(((minor/10)%10) + '0'); pv[14] = (byte)(((minor)%10) + '0');

		out.write(pv,0,16);
		out.flush();
		// "METISSE 000.000\n"
		readString(pv, 16);     // 0123456789012345
		if(pv[0] != 'M' ||
				pv[1] != 'E' ||
				pv[2] != 'T' ||
				pv[3] != 'I' ||
				pv[4] != 'S' ||
				pv[5] != 'S' || 
				pv[6] != 'E' ||
				pv[7] != ' ' ||
				pv[11] != '.' ||
				pv[15] != '\n' ||
				(pv[ 8] < '0' || pv[ 8] > '9') ||
				(pv[ 9] < '0' || pv[ 9] > '9') ||
				(pv[10] < '0' || pv[10] > '9') || 
				(pv[12] < '0' || pv[12] > '9') || 
				(pv[13] < '0' || pv[13] > '9') ||
				(pv[14] < '0' || pv[14] > '9'))
		{
			writeUint32(Proto.rfbConnFailed);
			String reason = "not a valid protocol version";
			writeUint32(reason.length());
			flushUint();			
			writeString(reason.getBytes(),reason.length());
			return false;
		}
		return true;
	}




	/**
	 * Authentication process from the server.
	 * @throws IOException
	 */
	public void servrfbAuthentication() throws IOException{
		writeUint32(Proto.rfbNoAuth);
		flushUint();
	}

	/**
	 * Client init process from the server.
	 * @throws IOException
	 */
	public void servrfbClientInit() throws IOException{
		byte shared[] = new byte[1];
		shared[0] = 1;
		in.read(shared,0,1);
	}

	/**
	 * Server init process from the server.
	 * @throws IOException
	 */
	public void servrfbServerInit() throws IOException{
		writeUint16(width);
		writeUint16(height);

		writeUint8(0);
		writeUint8(0);
		writeUint8(0);
		writeUint8(0);
		writeUint16(0);
		writeUint16(0);
		writeUint16(0);
		writeUint8(0);
		writeUint8(0);
		writeUint8(0);
		flushUint();
		String s = "one client";
		int namelen = s.length();
		writeUint32(namelen);
		flushUint();

		writeString(s.getBytes(),namelen);
	}

	/**
	 * Handles the pixel format message sent by the client to the zvtm server.
	 * @throws IOException
	 */
	public void servrfbSetPixelFormat() throws IOException{
		readCard8();// msg type
		readCard8(); // bitsPerPixel
		readCard8(); // depth
		readCard8(); // bigEndian
		readCard8(); // trueColour
		readCard16(); // redMax
		readCard16(); // greenMax
		readCard16(); // blueMax
		readCard8(); // redShift
		readCard8(); // greenShift
		readCard8(); // blueShift
	}


	/**
	 * Handles the set encodings message sent by the client to the zvtm server.
	 * @throws IOException
	 */
	public void servrfbSetEncodings() throws IOException{
		readCard8();
		readCard16();
		readCard32();
		readCard32();
		readCard32();
		readCard32();
	}


	/**
	 * @return The sending blocking queue 
	 */
	public LinkedBlockingQueue<RFBMessage> getOut() {
		return toSend;
	}


	/**
	 *Handles configure wall messages sent by the client to the zvtm server.
	 * @throws IOException
	 */
	public boolean handleConfigureWall() throws IOException {
		rfbConfigureWall();
		return false;
	}

	
	/**
	 * Forwards key event from the zvtm server to the Metisse server of a client.
	 * @throws IOException
	 */
	public boolean fwKeyEvent() throws IOException{
		int down = readCard8();//down
		int keysym = readCard32();
		send(new KeyEvent(keysym, 1==down));
		return false;
	}
	
	
	/**
	 * Forwards pointer event from the zvtm server to the Metisse server of a client.
	 * @throws IOException
	 */
	public boolean fwPointerEvent() throws IOException{
		int buttons = readCard32();
		int xs = readCard8();
		int x = readCard16();
		int ys = readCard8();
		int y = readCard16();
		int window = readCard32();
		send(new PointerEvent(window,(xs==1?1:-1)*x, (ys==1?1:-1)*y, buttons));
		return false;
	}

	private boolean pinged = false;

	/**
	 * Sends a ping to the client
	 */
	public void ping() {
		send(new RFBPing());
		pinged = true;
	}

	/**
	 * Sends a pong to the zvtm server
	 */
	public void pong() {
		send(new RFBPong());
	}

	/**
	 * Adds the specified {@link RFBMessage} to the sending queue. (no concurrency issues because the queue is a blocking queue)
	 * @param msg
	 */
	private void send(RFBMessage msg) {
		try {
			if (alive)
				toSend.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inform the {@link RfbAgent} that the client has responded.
	 */
	public void ponged() {
		pinged = false;
	}

	/**
	 * Has the client responded?
	 */
	public boolean hasResponded(){
		return !pinged;
	}

}
