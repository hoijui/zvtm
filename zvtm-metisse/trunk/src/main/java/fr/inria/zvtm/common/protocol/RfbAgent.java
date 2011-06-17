package fr.inria.zvtm.common.protocol;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import fr.inria.zvtm.common.compositor.RfbMessageHandler;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.kernel.RfbInput;



public class RfbAgent {

	private InputStream in;
	private OutputStream out;
	private String password;
	private LinkedList<RfbMessageHandler> listeners;
	private int height;
	private int width;
	private LinkedBlockingQueue<RFBMessage> toSend;

	public RfbAgent(InputStream in, OutputStream out) {
		this.setIn(in);
		this.out = out;
		toSend = new LinkedBlockingQueue<RFBMessage>();
		password = "insitu";
		listeners = new LinkedList<RfbMessageHandler>();
	}

	/*****************************************************************************
	 * Functions for sending messages to the server (display => server)
	 ****************************************************************************/

	public void addListener(RfbMessageHandler a){
		listeners.add(a);
	}

	public void startSender(){
		Thread t = new Thread(){
			OutputStream outstr = out;
			@Override
			public void run() {
				while(true){
					try {
						byte[] b = toSend.take().getBytes();
						outstr.write(b);
						outstr.flush();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		t.start();
	}

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
		pv[8] = (byte)((major/100) + '0'); 
		pv[9] = (byte)(((major/10)%10) + '0'); 
		pv[10] = (byte)(((major)%10) + '0');
		pv[11] = '.';
		pv[12] = (byte)((minor/100) + '0'); 
		pv[13] = (byte)(((minor/10)%10) + '0'); 
		pv[14] = (byte)(((minor)%10) + '0');
		pv[15] = '\n';
	
	 	out.write(pv,0,16);
	}





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


	public void rfbClientInit() throws IOException{
		byte shared[] = new byte[1];
		shared[0] = 1;
		out.write(shared, 0, 1);
		out.flush();
	}


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


	public void rfbSetPixelFormat() throws IOException{
		writeUint8(Proto.rfbSetPixelFormat); // msg type
		writeUint8(32); // bitsPerPixel
		writeUint8(24); // depth
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


	public void rfbSetEncodings() throws IOException{
		writeUint8(Proto.rfbSetEncodings); // Msg type
		writeUint16(4); // number of encodings
		writeUint32(Proto.rfbEncodingRaw);
		writeUint32(Proto.rfbEncodingPointerPos);
		writeUint32(Proto.rfbEncodingXCursor);
		writeUint32(Proto.rfbEncodingWindowShape);
		flushUint();
	}


	public void rfbFramebufferUpdateRequest(boolean incremental) throws IOException {
		rfbFramebufferUpdateRequest(0, 0, 10 * width, 10 * height, incremental) ;
	}


	public void rfbFramebufferUpdateRequest(int x, int y, int w, int h, boolean incremental) throws IOException {
	try {
		toSend.put(new FBURequest(x, y, w, h, incremental));
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	}


	public void rfbKeyEvent(int keysym, boolean down) {
		try {
			toSend.put(new KeyEvent(keysym, down));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	public void rfbPointerEvent(int window, int x, int y, int buttons) {
	try {
		toSend.put(new PointerEvent(window, x, y, buttons));
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	}



	public void rfbPointerEvent(int window, int jpx, int jpy, int[] buttonState) {
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
			res = getIn().read(data, read_total, length - read_total);
			if (res <= 0)
				return;
			read_total += res;
		}
	}


	protected void writeString(byte[] data, int length) throws IOException {
		out.write(data, 0, length);	
		out.flush();
	}

	public int readCard8() throws IOException{
		return ( (byte)getIn().read() & 0xff );
	}


	private int readCard16() throws IOException{
		return ( ( (byte)getIn().read() << 8 ) & 0xff00 ) 
		| (  (byte)getIn().read()    & 0x00ff );
	}


	private int readCard32() throws IOException {
		return ( ( (byte)getIn().read() << 24 ) & 0xff000000 ) 
		| ( ( (byte)getIn().read() << 16 ) & 0x00ff0000 ) 
		| ( ( (byte)getIn().read() << 8 ) & 0x0000ff00 ) 
		| (  (byte)getIn().read()     & 0x000000ff );
	}


	private  byte waiting_uint[] = new byte[1024];
	private  int waiting_uint_size = 0; 


	private  void writeUint32(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0xff000000 ) >>> 24 );
		waiting_uint[waiting_uint_size+1] = (byte)( ( v & 0x00ff0000 ) >>> 16 );
		waiting_uint[waiting_uint_size+2] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+3] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 4;
	}


	private  void writeUint16(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+1] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 2;
	}


	private  void writeUint8(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( v & 0xff );
		waiting_uint_size += 1;
	}


	private void flushUint() throws IOException{
		out.write(waiting_uint, 0, waiting_uint_size);
		waiting_uint_size = 0;
		out.flush();
	}









	/*****************************************************************************
	 * Functions for reaction to the server => to display
	 ****************************************************************************/

	public  void handle(){

	}

	private  boolean verbose = false;


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


	public void handleCursorPosition(int x, int y) {
		for(RfbMessageHandler l : listeners){
			l.handleCursorPosition(x, y);
		}
	}


	public  void handleImageFramebufferUpdate(int window, boolean isroot, byte img[], int x, int y, int w, int h){

		for(RfbMessageHandler l : listeners){
			l.handleImageFramebufferUpdate(window, isroot, img, x, y, w, h);
		}
	}


	public  void handleDestroyWindow(int window){
		for(RfbMessageHandler l : listeners){
			l.handleDestroyWindow(window);
		}
		if (verbose) System.out.println("destroy "+window);
	}


	public  void handleUnmapWindow(int window){
		for(RfbMessageHandler l : listeners){
			l.handleUnmapWindow(window); 
		}
		if (verbose) System.out.println("unmap "+window);
	}


	public  void handleServerCutText(String str){
		for(RfbMessageHandler l : listeners){
			l.handleServerCutText(str);
		}
	}


	public  void handleRestackWindow(int window, int nextWindow, int transientFor, int unmanagedFor, int grabWindow, int duplicateFor, int facadeReal, int flags){
		for(RfbMessageHandler l : listeners){
			l.handleRestackWindow(window, nextWindow, transientFor, unmanagedFor, grabWindow, duplicateFor, facadeReal, flags);
		}
		if (verbose) System.out.println("restack "+window);
	}


	public void handleRemoteKeyEvent(int keysym, int i) {
		for(RfbMessageHandler l : listeners){
			if(l instanceof RfbInput){
				((RfbInput)l).handleRemoteKeyEvent(keysym, i);
			}
		}
	}

	public void rootPointerEvent(int x, int y, int buttons){
		rfbPointerEvent(0, x, y, buttons);
	}


	public void rootKeyEvent(int key, boolean down_flag){
		rfbKeyEvent(key, down_flag);
	}

	@SuppressWarnings("unused")
	public  boolean framebufferUpdate() throws IOException {
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
		rfbFramebufferUpdateRequest(true);
		return true;
	}


	public boolean rfbDoublePointerEvent() {
		try{
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
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}

	public boolean rfbConfigureWall() {
		double[] res =new double[4];
		try{
			for (int i = 0; i < 4; i++) {
				res[i] = Float.intBitsToFloat(readCard32());
			}
			PCursor.wallBounds = res;
			return true;
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}

	public boolean rfbRemoteKeyEvent() {
		try {
			int i = readCard8();//down
			int keysym = readCard32();
			handleRemoteKeyEvent(keysym,i);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public  boolean rfbBell(){
		return false;
	}


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


	public boolean rfbServerCutText() throws IOException {
		int length = readCard32(); // length;
		byte str[] = new byte[length];
		readString(str, length);
		return false;
	}


	public boolean rfbConfigureWindow() throws IOException {
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


	public boolean rfbUnmapWindow() throws IOException {
		int window = readCard32(); // window id
		handleUnmapWindow(window);
		return true;
	}


	public boolean rfbDestroyWindow() throws IOException {
		int window = readCard32(); // window id
		handleDestroyWindow(window);
		return true;
	}


	public  boolean rfbRestackWindow() throws IOException {
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
	 * Functions for sending to a client
	 ****************************************************************************/




	public void orderConfigure(int window, boolean isroot, int x, int y, int w, int h) {
		try {
			toSend.put(new ConfigureMsg(window, isroot, x, y, w, h));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderConfigureWall(double[] bounds) {
		try {
			toSend.put(new ConfigureWallMsg(bounds));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderFrameBufferUpdate(int window, boolean isroot, byte[] img,int x, int y, int w, int h,int encoding) {
		try {
			toSend.put(new FBUMsg(window, isroot, img, x, y, w, h, encoding));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderRestackWindow(int window, int nextWindow,int transientFor, int unmanagedFor, int grabWindow,int duplicateFor, int facadeReal, int flags) {
		try {
			toSend.put(new RestackMsg(window, nextWindow, transientFor, unmanagedFor, grabWindow, duplicateFor, facadeReal, flags));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderUnmapWindow(int window) {
		try {
			toSend.put(new UnmapMsg(window));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderAddWindow(int id, boolean root, int x, int y, int w, int h) {
		orderConfigure(id,root,x, y, w, h);
	}

	public void orderRemoveWindow(int window) {
		try {
			toSend.put(new RemoveMsg(window));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void orderPointerEvent(double x, double y, int buttonMask) {
		try {
			toSend.put(new DoublePointerEventMsg(x, y, buttonMask));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void orderKeyEvent(int keysym, boolean down) {
		try {
			toSend.put(new ServKeyEvent(keysym, down));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


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
			writeString(reason.getBytes(), reason.length());
			return false;
		}
		return true;
	}





	public void servrfbAuthentication() throws IOException{
		writeUint32(Proto.rfbNoAuth);
		flushUint();
	}


	public void servrfbClientInit() throws IOException{
		byte shared[] = new byte[1];
		shared[0] = 1;
		getIn().read(shared,0,1);
	}


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
		byte[] name = s.getBytes();
		int namelen = name.length;
		writeUint32(namelen);
		flushUint();

		writeString(name, namelen);
	}

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


	public void servrfbSetEncodings() throws IOException{
		readCard8();
		readCard16();
		readCard32();
		readCard32();
		readCard32();
		readCard32();
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public InputStream getIn() {
		return in;
	}

	public OutputStream getOut() {
		return out;
	}


	public boolean handleConfigureWall() {
		rfbConfigureWall();
		return false;
	}
	
	public boolean fwKeyEvent() {

		try {
			int down = readCard8();//down
			int keysym = readCard32();
			toSend.put(new KeyEvent(keysym, 1==down));
		}catch(IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean fwPointerEvent() {
		try{
			int buttons = readCard32();
			int xs = readCard8();
			int x = readCard16();
			int ys = readCard8();
			int y = readCard16();
			int window = readCard32();
			toSend.put(new PointerEvent(window,(xs==1?1:-1)*x, (ys==1?1:-1)*y, buttons));
			
		}catch(IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}


}