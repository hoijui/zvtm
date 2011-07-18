package fr.inria.zvtm.common.compositor;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.common.kernel.Temporizer;
import fr.inria.zvtm.common.protocol.Keysym;
import fr.inria.zvtm.common.protocol.RfbAgent;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * This class redirects the Zvtm {@link Viewer}'s input to the real windows in the X server.
 * Since a {@link MetisseWindow} has both a X server location / size and a Zvtm location / size (as {@link Glyph}), the {@link InputForwarder} calculates the inverse Zvtm transformation in order to transmit the correct coordinates of an event to the X server.
 * @author Julien Altieri
 *
 */
public class InputForwarder {

	private int[]buttonState = new int[32];
	private int currentWindow;
	private boolean dragging = false;
	private boolean pressed = false;
	private long lastSentEventTime = System.currentTimeMillis();
	private ClientViewer viewer;
	private RfbAgent rfbAgent;

	/**
	 * Obviously, the {@link InputForwarder} is related to a Zvtm {@link Viewer}. However, We only need to redirect inputs from the client.
	 * @param clientViewer The {@link ClientViewer} that needs to be handled
	 */
	public InputForwarder(ClientViewer clientViewer) {
		this.viewer = clientViewer;
	}

	/**
	 * In the overall architecture, the interesting {@link RfbAgent} is instantiated after the creation of the {@link InputForwarder}. We need to set it after.
	 * @param a
	 */
	public void setRfbAgent(RfbAgent a){
		this.rfbAgent = a;
	}

	public void Kpress(KeyEvent e) {
		rfbAgent.rfbKeyEvent(getKeysym(e), true);

	}

	public void Kreles(KeyEvent e) {
		rfbAgent.rfbKeyEvent(getKeysym(e), false);
	}


	/**
	 * Forwards a click event
	 * @param mod the modifier mask
	 * @param jpx the x position in the X server
	 * @param jpy the y position in the X server
	 * @param clickNumber 
	 * @param e
	 */
	public void click(int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {	
	}

	/**
	 * Forwards a MousePressed event
	 * @param mod the modifier mask
	 * @param v the origin ViewPanel of the event
	 * @param jpx the x position in the X server
	 * @param jpy the y position in the X server
	 * @param e
	 */
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

	/**
	 * Forwards a MouseReleased Event
	 * @param mod the modifier mask
	 * @param v the origin ViewPanel of the event
	 * @param jpx the x position in the X server
	 * @param jpy the y position in the X server
	 * @param e
	 */
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

	/**
	 * Forwards a MouseWheel event
	 * @param mod the modifier mask
	 * @param v the origin ViewPanel of the event
	 * @param jpx the x position in the X server
	 * @param jpy the y position in the X server
	 * @param e
	 */
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

	/**
	 * Transforms the coordinates from the Zvtm {@link Viewer} to the coordinates in X server.
	 * @param v
	 * @param jpx
	 * @param jpy
	 * @return a int[] according to the following format: {server x, server y, picked window id}
	 */
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

	/**
	 * Forwards a move event.
	 * @param v the origin ViewPanel of the event
	 * @param jpx the x position in the X server
	 * @param jpy the y position in the X server
	 * @param e
	 */
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

	/**
	 * Pick the window which is under the cursor at position jpx, jpy in the Zvtm {@link Viewer}.
	 * @param jpx 
	 * @param jpy
	 * @return The picked window's id
	 */
	public int detectWindow(double jpx,double jpy){
		if(viewer==null||viewer.getCursorHandler().getCursor().getPicker().getDrawOrderedPickedGlyphList(viewer.getVirtualSpace()).length==0)return -1;
		Glyph up = viewer.getCursorHandler().getCursor().getPicker().pickOnTop(viewer.getVirtualSpace());
		if(up==null || !up.getClass().equals(MetisseWindow.class))return -1;
		return ((MetisseWindow)up).getId();
	}

	private int getKeysym(KeyEvent e){
		return getKeysym(e.getKeyChar(), e.getKeyCode());
	}


	/**
	 * Converts Swing virtual keys into keysyms
	 * @param c the related character. Works for all the regular characters, else the method will use the code for the convertion.
	 * @param code
	 * @return the keysym
	 */
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
