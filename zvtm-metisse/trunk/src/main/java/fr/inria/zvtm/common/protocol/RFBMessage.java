package fr.inria.zvtm.common.protocol;


public abstract class RFBMessage {
	protected byte[] waiting_uint;
	protected  int waiting_uint_size = 0;

	public byte[] getBytes(){
		return waiting_uint;
	}

	protected void writeUint32(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0xff000000 ) >>> 24 );
		waiting_uint[waiting_uint_size+1] = (byte)( ( v & 0x00ff0000 ) >>> 16 );
		waiting_uint[waiting_uint_size+2] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+3] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 4;
	}


	protected void writeUint16(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( ( v & 0x0000ff00 ) >>> 8 );
		waiting_uint[waiting_uint_size+1] = (byte)(  v & 0x000000ff );
		waiting_uint_size += 2;
	}


	protected void writeUint8(int v) {
		waiting_uint[waiting_uint_size+0] = (byte)( v & 0xff );
		waiting_uint_size += 1;
	}

	protected void writeString(String s){
		System.arraycopy(s.getBytes(), 0, waiting_uint, waiting_uint_size, s.length());
		waiting_uint_size+=s.length();
	}


	protected void writeString(byte[] b) {
		System.arraycopy(b, 0, waiting_uint, waiting_uint_size, b.length);
		waiting_uint_size+=b.length;
	}

}

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


class FBURequest extends RFBMessage{
	
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

class KeyEvent extends RFBMessage{
	public KeyEvent(int keysym, boolean down) {
		this.waiting_uint = new byte[6];
		writeUint8(Proto.rfbKeyEvent);
		writeUint8(down ? 1 : 0);
		writeUint32(keysym);
	}
}

class PointerEvent extends RFBMessage{
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

class ConfigureMsg extends RFBMessage{
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

class ConfigureWallMsg extends RFBMessage{
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

class FBUMsg extends RFBMessage{
	public FBUMsg(int window, boolean isroot, byte[] img,int x, int y, int w, int h,int encoding){
		switch(encoding){
		case Proto.rfbEncodingPointerPos:
			this.waiting_uint = new byte[27];
			break;
		case Proto.rfbEncodingRaw:
			this.waiting_uint = new byte[img.length*4+27];		
			break;
		default:
			break;
		}
		writeUint8(Proto.rfbFramebufferUpdate);
		writeUint16(1);
		writeUint32(window);
		if(isroot)writeUint32(0);
		else writeUint32(1);
		writeUint32(0);//shmid
		writeUint16(x);
		writeUint16(y);
		writeUint16(w);
		writeUint16(h);
		writeUint32(encoding);
		switch(encoding){
		case Proto.rfbEncodingPointerPos:
			break;
		case Proto.rfbEncodingRaw:
			writeString(img);
			break;
		default:
			break;
		}
	}
}

class RestackMsg extends RFBMessage{
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

class UnmapMsg extends RFBMessage{
	public UnmapMsg(int window) {
		this.waiting_uint = new byte[5];
		writeUint8(Proto.rfbUnmapWindow);
		writeUint32(window);
	}
}

class RemoveMsg extends RFBMessage{
	public RemoveMsg(int window) {
		this.waiting_uint = new byte[5];
		writeUint8(Proto.rfbDestroyWindow);
		writeUint32(window);
	}
}


class DoublePointerEventMsg extends RFBMessage{
	public DoublePointerEventMsg(double x, double y, int buttonMask) {
		this.waiting_uint = new byte[13];
		writeUint8(Proto.rfbDoublePointerEvent);
		writeUint32(buttonMask);//buttons
		writeUint32(Float.floatToIntBits((float) x));
		writeUint32(Float.floatToIntBits((float) y));
	}
}

class ServKeyEvent extends RFBMessage{
	public ServKeyEvent(int keysym, boolean down) {
		this.waiting_uint = new byte[6];
		writeUint8(Proto.rfbServKeyEvent);
		writeUint8(down ? 1 : 0);
		writeUint32(keysym);
	}
}




