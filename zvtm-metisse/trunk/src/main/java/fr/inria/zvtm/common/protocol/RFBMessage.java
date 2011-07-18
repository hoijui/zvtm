package fr.inria.zvtm.common.protocol;

/**
 * General wrapping class for rfb messages.
 * @author Julien Altieri
 *
 */
public abstract class RFBMessage {
	protected byte[] waiting_uint;
	protected  int waiting_uint_size = 0;

	/**
	 * Each type of {@link RFBMessage} should be sent by sending the byte[] through the socket.
	 * @return The message's byte[] to send.
	 */
	public byte[] getBytes(){
		return waiting_uint;
	}

	/**
	 * Writes 4 bytes in the byte[] of the message.
	 * @param v the value to write
	 */
	protected void writeUint32(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0xff000000 ) >>> 24 );
		waiting_uint[waiting_uint_size+1] = (byte)( ( v & 0x00ff0000 ) >>> 16 );
		waiting_uint[waiting_uint_size+2] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+3] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 4;
	}

	/**
	 * Writes 2 bytes in the byte[] of the message.
	 * @param v the value to write
	 */
	protected void writeUint16(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+1] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 2;
	}

	/**
	 * Writes 1 byte in the byte[] of the message.
	 * @param v the value to write
	 */
	protected void writeUint8(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( v & 0xff );
		waiting_uint_size += 1;
	}

	/**
	 * Writes a string in the byte[] of the message.
	 * @param s the string to write
	 * @see RFBMessage#writeString(byte[])
	 */
	protected void writeString(String s){
		System.arraycopy(s.getBytes(), 0, waiting_uint, waiting_uint_size, s.length());
		waiting_uint_size+=s.length();
	}

	/**
	 * Writes a byte[] in the byte[] of the message.
	 * @param s the byte[] to write
	 * @see RFBMessage#writeString(String s)
	 */
	protected void writeString(byte[] b) {
		System.arraycopy(b, 0, waiting_uint, waiting_uint_size, b.length);
		waiting_uint_size+=b.length;
	}

}

/**
 * The standard protocol version message.
 * @author Julien Altieri
 *
 */
class ProtocolVersion extends RFBMessage{
	@Override
	public byte[] getBytes() {
		byte[] pv = new byte[16]; // "METISSE 000.000\n"
		int major = Proto.rfbProtocolMajorVersion ;
		int minor = Proto.rfbProtocolMinorVersion ;
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
		return pv;
	}

}

/**
 * The standard FBURequest message.
 * @author Julien Altieri
 *
 */
class FBURequest extends RFBMessage{
	
	/**
	 * Frame buffer update request message on the specified rectangle.
	 * @param x x coordinate of the required rectangle
	 * @param y y coordinate of the required rectangle
	 * @param w width of the required rectangle
	 * @param h height of the required rectangle
	 * @param incremental
	 */
	public FBURequest(int x, int y, int w, int h, boolean incremental) {
		this.waiting_uint = new byte[10];
		writeUint8(Proto.rfbFramebufferUpdateRequest);
		writeUint8(incremental ? 1 : 0);
		writeUint16(x);
		writeUint16(y);
		writeUint16(w);
		writeUint16(h);
	}
}

/**
 * The standard key event message.
 * @author Julien Altieri
 *
 */
class KeyEvent extends RFBMessage{

	/**
	 * Key event message.
	 * @param keysym
	 * @param down (1 for down, 0 for up)
	 */
	public KeyEvent(int keysym, boolean down) {
		this.waiting_uint = new byte[6];
		writeUint8(Proto.rfbKeyEvent);
		writeUint8(down ? 1 : 0);
		writeUint32(keysym);
	}
}

/**
 * The standard pointer event message.
 * @author Julien Altieri
 *
 */
class PointerEvent extends RFBMessage{
	/**
	 * Pointer event message.
	 * @param window
	 * @param x
	 * @param y
	 * @param buttons
	 */
	public PointerEvent(int window, int x, int y, int buttons){
		this.waiting_uint = new byte[15];
		writeUint8(Proto.rfbPointerEvent);
		writeUint32(buttons);
		if(x<0)writeUint8(0);
		else writeUint8(1); /* 0: negative, 1: positive */
		writeUint16(x);
		if(y<0)writeUint8(0);
		else writeUint8(1); /* 0: negative, 1: positive */
		writeUint16(y);
		writeUint32(window);
	}
}

/**
 * The standard configure window message.
 * @author Julien Altieri
 *
 */
class ConfigureMsg extends RFBMessage{
	
	/**
	 * Configure window message.
	 * @param window the id of the window
	 * @param isroot is the given window root frame?
	 * @param x position in the server
	 * @param y position in the server
	 * @param w width in the server
	 * @param h height in the server
	 */
	public ConfigureMsg(int window, boolean isroot, int x, int y, int w, int h) {
		this.waiting_uint = new byte[23];
		writeUint8(Proto.rfbConfigureWindow);
		writeUint32(window);
		if(x<0)writeUint16(0);
		else writeUint16(1);
		writeUint16(Math.abs(x));
		if(y<0)writeUint16(0);
		else writeUint16(1);
		writeUint16(Math.abs(y));
		writeUint32(w);
		writeUint32(h);
		if(isroot)writeUint16(1);
		else writeUint16(0);
	}
}

/**
 * The standard configure wall message.
 * @author Julien Altieri
 *
 */
class ConfigureWallMsg extends RFBMessage{
	/**
	 * Configure wall message.
	 * @param bounds the virtual bounds of the wall
	 */
	public ConfigureWallMsg(double[] bounds) {
		this.waiting_uint = new byte[17];
		int e = Float.floatToIntBits((float) bounds[0]);
		int n = Float.floatToIntBits((float) bounds[1]);
		int w = Float.floatToIntBits((float) bounds[2]);
		int s = Float.floatToIntBits((float) bounds[3]);
		writeUint8(Proto.rfbConfigureWall);
		writeUint32(e);//east
		writeUint32(n);//north
		writeUint32(w);//west
		writeUint32(s);//south
	}
}

/**
 * The standard frame buffer update message.
 * @author Julien Altieri
 *
 */
class FBUMsg extends RFBMessage{
	int w;
	int h;
	int size;
	
	/**
	 * Frame buffer update message.
	 * @param window the related window
	 * @param isroot is this the root window
	 * @param img the byte[] containing raster update information. Its maximum size is 4*w*h since each pixel's color is 4-bytes encoded.
	 * @param x the x where the update rectangle starts
	 * @param y the y where the update rectangle starts
	 * @param w the width of the update rectangle
	 * @param h the height of the update rectangle
	 * @param encoding one of {@link Proto}
	 */
	public FBUMsg(int window, boolean isroot, byte[] img,int x, int y, int w, int h,int encoding){
		this.w = w;
		this.h = h;
		this.size = img.length;
		switch(encoding){
		case Proto.rfbEncodingRaw:
			this.waiting_uint = new byte[img.length+27];		
			break;
		default:
			break;
		}
		writeUint8(Proto.rfbFramebufferUpdate);
		writeUint16(1);//nrects
		writeUint32(window);
		writeUint32(isroot?0:1);
		writeUint32(0);//shmid
		writeUint16(x);
		writeUint16(y);
		writeUint16(w);
		writeUint16(h);
		writeUint32(encoding);
		
		switch(encoding){
		case Proto.rfbEncodingRaw:
			writeString(img);
			break;
		default:
			break;
		}
	}
	@Override
	public String toString() {
		return ("w: "+w+" h: "+h+" size: "+size);

	}
}

/**
 * The standard Restack message.
 * @author Julien Altieri
 *
 */
class RestackMsg extends RFBMessage{
	
	/**
	 * Restack window message.
	 * @param window the id of the window
	 * @param nextWindow the id of the next window in the stack (use for drawing)
	 * @param transientFor potential parent of the frame
	 * @param unmanagedFor potential parent of the frame
	 * @param grabWindow potential parent of the frame
	 * @param duplicateFor facade flag
	 * @param facadeReal facade flag
	 * @param flags facade flag
	 */
	public RestackMsg(int window, int nextWindow,int transientFor, int unmanagedFor, int grabWindow,int duplicateFor, int facadeReal, int flags) {
		this.waiting_uint = new byte[33];
		writeUint8(Proto.rfbRestackWindow);
		writeUint32(window);
		writeUint32(nextWindow);
		writeUint32(transientFor);
		writeUint32(unmanagedFor);
		writeUint32(grabWindow);
		writeUint32(duplicateFor);
		writeUint32(facadeReal);
		writeUint32(flags);
	}
}

/**
 * The standard unmap window message.
 * @author Julien Altieri
 *
 */
class UnmapMsg extends RFBMessage{
	
	/**
	 * Unmap window message.
	 * @param window the window to be unmapped
	 */
	public UnmapMsg(int window) {
		this.waiting_uint = new byte[5];
		writeUint8(Proto.rfbUnmapWindow);
		writeUint32(window);
	}
}

/**
 * The standard remove window message.
 * @author Julien Altieri
 *
 */
class RemoveMsg extends RFBMessage{

	/**
	 * Remove window message.
	 * @param window the window to be removed
	 */
	public RemoveMsg(int window) {
		this.waiting_uint = new byte[5];
		writeUint8(Proto.rfbDestroyWindow);
		writeUint32(window);
	}
}

/**
 * The virtual pointer event message.
 * @author Julien Altieri
 *
 */
class DoublePointerEventMsg extends RFBMessage{
	/**
	 * Pointer event message.
	 * @param x virtual x coordinate of the event
	 * @param y virtual y coordinate of the event
	 * @param buttonMask button mask
	 */
	public DoublePointerEventMsg(double x, double y, int buttonMask) {
		this.waiting_uint = new byte[13];
		writeUint8(Proto.rfbDoublePointerEvent);
		writeUint32(buttonMask);//buttons
		writeUint32(Float.floatToIntBits((float) x));
		writeUint32(Float.floatToIntBits((float) y));
	}
}

/**
 * The server key event message.
 * @author Julien Altieri
 *
 */
class ServKeyEvent extends RFBMessage{
	
	/**
	 * Key event message.
	 * @param keysym
	 * @param down (1 for down, 0 for up)
	 */
	public ServKeyEvent(int keysym, boolean down) {
		this.waiting_uint = new byte[6];
		writeUint8(Proto.rfbServKeyEvent);
		writeUint8(down ? 1 : 0);
		writeUint32(keysym);
	}
}

/**
 * The ping message.
 * @author Julien Altieri
 *
 */
class RFBPing extends RFBMessage{
	
	/**
	 * A ping message.
	 */
	public RFBPing(){
		this.waiting_uint = new byte[1];
		writeUint8(Proto.ping);
	}
}

/**
 * The pong message.
 * @author Julien Altieri
 *
 */
class RFBPong extends RFBMessage{
	/**
	 * A pong message.
	 */
	public RFBPong(){
		this.waiting_uint = new byte[1];
		writeUint8(Proto.pong);
	}
}


