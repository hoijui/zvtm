package Protocol;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import kernel.Main;
import Compositors.BasicAdapter;
import ZVTMViewer.MetisseWindow;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;

public class InputForwarder {

	private rfbAgent rfb;
	private int[]buttonState;
	int currentWindow;
	private long sendEventDelay = 100;
	private boolean dragging = false;
	private boolean pressed = false;
	private long lastSentEventTime;

	public InputForwarder(rfbAgent rfb) {
		this.rfb = rfb;
		this.buttonState = new int[8];
		this.lastSentEventTime = System.currentTimeMillis();

	}


	public void Kpress(KeyEvent e) {
		rfb.rfbKeyEvent(e.getKeyCode(), true);

	}

	public void Kreles(KeyEvent e) {
		rfb.rfbKeyEvent(e.getKeyCode(), false);
	}


	public void click(int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {	
	}

	public void press(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = true;
		buttonState[e.getButton()-1] = 1;
		currentWindow = detectWindow(v, jpx, jpy);
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		rfb.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}

	public void release(int mod, ViewPanel v, int jpx, int jpy, MouseEvent e) {
		pressed = false;
		dragging = false;
		if(MetisseWindow.beingResized !=null)
			MetisseWindow.beingResized.endResize();
		buttonState[e.getButton()-1] = 0;
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		rfb.rfbPointerEvent(p[2],p[0],p[1], buttonState);
		Main.viewer.dragging = false;
	}


	public int[] unproject(ViewPanel v, double jpx,double jpy){
		int[] res = new int[3];
		double sf = 1;//scale factor
		double x = v.getVCursor().getVSXCoordinate();
		double y = v.getVCursor().getVSYCoordinate();
		MetisseWindow up = MetisseWindow.rootFrame;
		double[] bounds = ((VImage) up).getBounds();//TODO si ce n'est pas une VImage
		int xx,yy;


		if(((BasicAdapter)Main.renderer).winman.windows.containsKey(currentWindow)){
			sf = ((BasicAdapter)Main.renderer).winman.windows.get(currentWindow).scaleFactor;

			MetisseWindow cu = ((BasicAdapter)Main.renderer).winman.windows.get(currentWindow);
			xx = (int) ((x/sf-bounds[0])+cu.getLocation().x*(1-1./sf));
			yy = (int) ((bounds[1]-y/sf)-cu.getLocation().y*(1-1./sf));

		}else{
			xx = (int) ((x/sf-bounds[0]));
			yy = (int) ((bounds[1]-y/sf));
		}

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
			if((System.currentTimeMillis()-lastSentEventTime)<sendEventDelay)return;
			else{
				lastSentEventTime = System.currentTimeMillis();
			}
		}

		currentWindow = detectWindow(v, jpx, jpy);
		int[] p = unproject(v, jpx, jpy);
		if(p == null)return;
		rfb.rfbPointerEvent(p[2],p[0],p[1], buttonState);
	}


	public static int detectWindow(ViewPanel v,double jpx,double jpy){
		Vector<Glyph> t = v.getVCursor().getPicker().getIntersectingGlyphs(Main.viewer.mCamera);
		
		if (t==null) {
			return -1 ;
		}
		Glyph up = t.lastElement() ;
		return ((MetisseWindow)up).windowNumber;
	}
}
