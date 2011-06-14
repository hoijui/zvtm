
package fr.inria.zvtm.common.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;


public abstract class MainEventHandler implements ViewListener{

	protected boolean locked = false;
	protected Viewer viewer;

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

	public void upStack(MetisseWindow f){
		if(locked)return;
		if(f==null)return;
		if(f.isRoot())return;
		viewer.getVirtualSpace().onTop(f);
		f.setZindex(0);
	}

	public void lock(){
		locked = true;
	}

	public void unlock(){
		locked = false;
	}

	public boolean isLocked(){
		return locked;
	}

	public abstract void click(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e);
	public abstract void press(ViewPanel v,int mod,int jpxx,int jpyy,MouseEvent e);
	public abstract void release(ViewPanel v,int mod,int jpxx,int jpyy,MouseEvent e);

}
