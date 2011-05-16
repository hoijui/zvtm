package fr.inria.zvtm.compositor;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.kernel.Main;
import fr.inria.zvtm.kernel.Temporizer;
import fr.inria.zvtm.protocol.RfbAgent;

public class InputForwarder {

	private static int[]buttonState;
	private static int currentWindow;
	private static boolean dragging = false;
	private static boolean pressed = false;
	private static long lastSentEventTime;

	public static void init() {
		buttonState = new int[8];
		lastSentEventTime = System.currentTimeMillis();
	}


	public static void Kpress(KeyEvent e) {
		RfbAgent.rfbKeyEvent(getKeysym(e), true);

	}

	public static void Kreles(KeyEvent e) {
		RfbAgent.rfbKeyEvent(getKeysym(e), false);
	}


	public static void click(int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {	
	}

	public static void press(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = true;
		buttonState[e.getButton()-1] = 1;
		currentWindow = detectWindow(v, jpx, jpy);
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		RfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}

	public static void release(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = false;
		dragging = false;
		if(MetisseWindow.getRezisingFrame() !=null)
			MetisseWindow.getRezisingFrame().endResize();
		buttonState[e.getButton()-1] = 0;
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		RfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
		Main.viewer.dragging = false;
	}


	public static int[] unproject(ViewPanel v, double jpx,double jpy){
		int[] res = new int[3];
		double sf = 1;//scale factor
		int xx,yy;

		MetisseWindow cu = FrameManager.get(currentWindow);
		if(cu==null)return null;
		double[] cubounds = cu.getBounds();
		sf = cu.getScaleFactor();
		xx = (int) (cu.getX()+ (v.getVCursor().getVSXCoordinate()-cubounds[0])/sf);
		yy = (int) (cu.getY()- (v.getVCursor().getVSYCoordinate()-cubounds[1])/sf);
		res[0] = xx;
		res[1] = yy;
		res[2] = currentWindow;
		return res;
	}

	public static void move(ViewPanel v, int jpx, int jpy, MouseEvent e) {
		if(pressed&& !dragging){
			dragging = true;
		}
		if(dragging){
			if((System.currentTimeMillis()-lastSentEventTime)<Temporizer.sendEventDelay)return;
			else{
				lastSentEventTime = System.currentTimeMillis();
			}
		}

		currentWindow = detectWindow(v, jpx, jpy);
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		RfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}


	public static int detectWindow(ViewPanel v,double jpx,double jpy){
		if(v==null)return -1;
		if(v.getVCursor()==null)return -1;
		if(v.getVCursor().getPicker()==null)return -1;
		if(Main.viewer==null)return-1;
		Glyph[] t = v.getVCursor().getPicker().getPickedGlyphList();

		if(t.length<=0)return -1;
		Glyph up = t[t.length-1];
		if(!up.getClass().equals(MetisseWindow.class))return -1;
		return ((MetisseWindow)up).getId();
	}


	private static int getKeysym(KeyEvent e){

		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			return (0xff51);
		case KeyEvent.VK_RIGHT:
			return (0xff53);
		case KeyEvent.VK_UP:
			return (0xff52);
		case KeyEvent.VK_DOWN:
			return (0xff54);
		case KeyEvent.VK_TAB:
			return (0xff09);
		case KeyEvent.VK_ESCAPE:
			return (0xff1b);
		case KeyEvent.VK_END:
			return (0xff57);
		case KeyEvent.VK_PAGE_UP:
			return (0xff55);
		case KeyEvent.VK_PAGE_DOWN:
			return (0xff56);

		default:
			break;
		}



		return (e.getKeyChar());
	}
}
