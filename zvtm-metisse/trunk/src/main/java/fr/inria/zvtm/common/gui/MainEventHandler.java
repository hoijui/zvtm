
package fr.inria.zvtm.common.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * This class is a hub for all possible input events from the zvtm {@link ViewListener}
 * @author Julien Altieri
 *
 */
public abstract class MainEventHandler implements ViewListener{

	protected boolean locked = false;
	protected Viewer viewer;

	/**
	 * The related {@link Viewer} must be set after instantiation.
	 * @param v the related {@link Viewer}
	 */
	public void setViewer(Viewer v){
		this.viewer = v;
	}

	public void viewActivated(View v){if(locked)return;}

	public void viewDeactivated(View v){if(locked)return;}

	public void viewIconified(View v){if(locked)return;}

	public void viewDeiconified(View v){if(locked)return;}

	public void viewClosing(View v){System.exit(0);}

	public void press1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		press(v,mod,jpxx,jpyy,e);
	}

	public void release1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		release(v,mod,jpxx,jpyy,e);
	}

	public void click1(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		if(locked)return;
		click(v, mod, jpxx, jpyy, clickNumber, e);
	}

	public void press2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		press(v,mod,jpxx,jpyy,e);
	}

	public void release2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		release(v,mod,jpxx,jpyy,e);
	}

	public void click2(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		if(locked)return;
		click(v, mod, jpxx, jpyy, clickNumber, e);
	}	
	
	public void press3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		press(v,mod,jpxx,jpyy,e);
	}

	public void release3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(locked)return;
		release(v,mod,jpxx,jpyy,e);
	}

	public void click3(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		if(locked)return;
		click(v, mod, jpxx, jpyy, clickNumber, e);
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){if(locked)return;}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){if(locked)return;}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){if(locked)return;}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){if(locked)return;}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){if(locked)return;}

	public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {if(locked)return;}

	public void enterGlyph(Glyph g) {
		if(locked)return;
		g.highlight(true, null);
	}

	public void exitGlyph(Glyph g) {
		if(locked)return;
		g.highlight(false, null);
	}

	/**
	 * Put the specified {@link MetisseWindow} at the top of the drawing stack.
	 * @param f the {@link MetisseWindow} to be put at the top.
	 */
	public void upStack(MetisseWindow f){
		if(locked)return;
		if(f==null)return;
		if(f.isRoot())return;
		viewer.getVirtualSpace().onTop(f);
		f.setZindex(0);
	}

	/**
	 * Disable all the events. Used by the PopMenu to prevent buttons events from being transmitted to the Metisse server.
	 */
	public void lock(){
		locked = true;
	}

	/**
	 * Enable all the events.
	 * @see MainEventHandler#lock()
	 */
	public void unlock(){
		locked = false;
	}

	/**
	 * @see MainEventHandler#lock()
	 * @see MainEventHandler#unlock()
	 * @return The lock state
	 */
	public boolean isLocked(){
		return locked;
	}

	/**
	 * Hub for all click events. 
	 * @param v {@link ViewPanel} in which glyphs are drawn.
	 * @param mod Modifier Mask
	 * @param jpxx x-coordinates of cursor in JPanel coordinates when event occurred
	 * @param jpyy y-coordinates of cursor in JPanel coordinates when event occurred
	 * @param clickNumber 
	 * @param e The related original MouseEvent
	 */
	public abstract void click(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e);
	
	/**
	 * Hub for all press events. 
	 * @param v {@link ViewPanel} in which glyphs are drawn.
	 * @param mod Modifier Mask
	 * @param jpxx x-coordinates of cursor in JPanel coordinates when event occurred
	 * @param jpyy y-coordinates of cursor in JPanel coordinates when event occurred
	 * @param clickNumber 
	 * @param e The related original MouseEvent
	 */
	public abstract void press(ViewPanel v,int mod,int jpxx,int jpyy,MouseEvent e);
	
	/**
	 * Hub for all release events. 
	 * @param v {@link ViewPanel} in which glyphs are drawn.
	 * @param mod Modifier Mask
	 * @param jpxx x-coordinates of cursor in JPanel coordinates when event occurred
	 * @param jpyy y-coordinates of cursor in JPanel coordinates when event occurred
	 * @param clickNumber 
	 * @param e The related original MouseEvent
	 */
	public abstract void release(ViewPanel v,int mod,int jpxx,int jpyy,MouseEvent e);

}
