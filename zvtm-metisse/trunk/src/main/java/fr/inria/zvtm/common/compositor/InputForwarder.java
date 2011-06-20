package fr.inria.zvtm.common.compositor;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.kernel.Temporizer;
import fr.inria.zvtm.common.protocol.Keysym;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

public class InputForwarder {

	private int[]buttonState = new int[32];
	private int currentWindow;
	private boolean dragging = false;
	private boolean pressed = false;
	private long lastSentEventTime = System.currentTimeMillis();
	private ClientViewer viewer;
	private RfbAgent rfbAgent;

	public InputForwarder(ClientViewer clientViewer) {
		this.viewer = clientViewer;
	}

	public void setRfbAgent(RfbAgent a){
		this.rfbAgent = a;
	}

	public void Kpress(KeyEvent e) {
		rfbAgent.rfbKeyEvent(getKeysym(e), true);

	}

	public void Kreles(KeyEvent e) {
		rfbAgent.rfbKeyEvent(getKeysym(e), false);
	}


	public void click(int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {	
	}

	public void press(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = true;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			buttonState[0] = 1;
			break;
		case MouseEvent.BUTTON2:
			buttonState[1] = 1;
			break;
		case MouseEvent.BUTTON3:
			buttonState[2] = 1;
			break;
		default:
			break;
		}
		currentWindow = detectWindow(jpx, jpy);
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}

	public void release(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = false;
		dragging = false;
		MetisseWindow  mwr = MetisseWindow.getRezisingFrame();
		if(mwr !=null){
			mwr.endResize();
			this.viewer.getFrameManager().endResize(mwr);			
		}
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			buttonState[0] = 0;
			break;
		case MouseEvent.BUTTON2:
			buttonState[1] = 0;
			break;
		case MouseEvent.BUTTON3:
			buttonState[2] = 0;
			break;
		default:
			break;
		}
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}


	public void wheel(int mod, ViewPanel v, int jpx, int jpy,MouseWheelEvent e) {

		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;

		int i = e.getWheelRotation();
		if(i<0){
			buttonState[3] =1;
			rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
			buttonState[3] =0;
			rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
		}
		else{
			buttonState[4] =1;
			rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
			buttonState[4] =0;
			rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
		}
	}

	public int[] unproject(ViewPanel v, double jpx,double jpy){
		if(viewer==null)return null;
		if(viewer.getFrameManager()==null)return null;
		int[] res = new int[3];
		double sf = 1;//scale factor
		int xx,yy;
		MetisseWindow cu = viewer.getFrameManager().get(currentWindow);
		if(cu==null)return null;
		double[] cubounds = cu.getBounds();
		sf = cu.getScaleFactor();
		xx = (int) (cu.getX()+ (viewer.getCursorHandler().getCursor().getVSXCoordinate()-cubounds[0])/sf);
		yy = (int) (cu.getY()- (viewer.getCursorHandler().getCursor().getVSYCoordinate()-cubounds[1])/sf);
		res[0] = xx;
		res[1] = yy;
		res[2] = currentWindow;
		return res;
	}

	public void move(ViewPanel v, int jpx, int jpy, MouseEvent e) {
		if(pressed&& !dragging){
			dragging = true;
		}
		if(dragging){
			if((System.currentTimeMillis()-lastSentEventTime)<Temporizer.sendEventDelay)return;
			else{
				lastSentEventTime = System.currentTimeMillis();
			}
		}
		currentWindow = detectWindow(jpx, jpy);
		int[] p = unproject(v, jpx, jpy);

		if(p == null)return;
		rfbAgent.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}

	public int detectWindow(double jpx,double jpy){
		if(viewer==null||viewer.getCursorHandler().getCursor().getPicker().getDrawOrderedPickedGlyphList(viewer.getVirtualSpace()).length==0)return -1;
		Glyph up = viewer.getCursorHandler().getCursor().getPicker().pickOnTop(viewer.getVirtualSpace());
		if(up==null || !up.getClass().equals(MetisseWindow.class))return -1;
		return ((MetisseWindow)up).getId();
	}

	private int getKeysym(KeyEvent e){
		return getKeysym(e.getKeyChar(), e.getKeyCode());
	}



	public static int getKeysym(char c, int code) {
		switch (code) {
		case KeyEvent.VK_LEFT:
			return (Keysym.Left);
		case KeyEvent.VK_RIGHT:
			return (Keysym.Right);
		case KeyEvent.VK_UP:
			return (Keysym.Up);
		case KeyEvent.VK_DOWN:
			return (Keysym.Down);
		case KeyEvent.VK_TAB:
			return (Keysym.Tab);
		case KeyEvent.VK_ESCAPE:
			return (Keysym.Escape);
		case KeyEvent.VK_ENTER:
			return (Keysym.KpEnter);
		case KeyEvent.VK_END:
			return (Keysym.End);
		case KeyEvent.VK_PAGE_UP:
			return (Keysym.PageUp);
		case KeyEvent.VK_PAGE_DOWN:
			return (Keysym.PageDown);
		case KeyEvent.VK_BACK_SPACE:
			return (Keysym.BackSpace);
		case KeyEvent.VK_ALT:
			return (Keysym.AltL);
		case KeyEvent.VK_CONTROL:
			return (Keysym.ControlL);

		default:
			break;
		}
		return (Keysym.toCharacter(c));
	}
}
