package fr.inria.zvtm.common.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Picker;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.master.MasterMain;

/**
 * This class must be added to a view as a ViewListener. It dispatches glyph events on the 
 * subscribed glyph which is under the picker
 * @author Julien Altieri
 *
 */
public class GlyphEventDispatcher implements ViewListener {
	protected HashMap<Glyph, GlyphListener> dispatchTable;
	private Picker picker;
	private VirtualSpace virtualSpace;
	private ViewPanel vp ;
	private Glyph lastPicked;
	private boolean alwaysRepick = true;

	public GlyphEventDispatcher(Picker p,VirtualSpace vs) {
		dispatchTable = new HashMap<Glyph, GlyphListener>();
		this.picker = p;
		this.virtualSpace = vs;
	

	}


	public void subscribe(Glyph g,GlyphListener gl){
		if (vp == null)this.vp = virtualSpace.getCamera(0).getOwningView().getPanel();
		dispatchTable.put(g, gl);
	}

	protected Glyph pick(){
		if(alwaysRepick){	
			picker.computePickedGlyphList(this, virtualSpace.getCamera(0));
			Glyph pp = picker.pickOnTop(virtualSpace);
			if(pp!=lastPicked){
				if(lastPicked!=null)exitGlyph(lastPicked);
				if(pp!=null)enterGlyph(pp);
			}
			lastPicked = pp;
		}
		return lastPicked;
	}


	public void press1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){		
			dispatchTable.get(lastPicked).press1(v,mod,jpxx,jpyy,e);
		}
	}

	public void release1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).release1(v,mod,jpxx,jpyy,e);
		}
	}

	public void click1(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).click1(v,mod,jpxx,jpyy,clickNumber,e);
		}
	}

	public void press2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).press2(v,mod,jpxx,jpyy,e);
		}
	}

	public void release2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).release2(v,mod,jpxx,jpyy,e);
		}
	}

	public void click2(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).click2(v,mod,jpxx,jpyy,clickNumber,e);
		}
	}

	public void press3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).press3(v,mod,jpxx,jpyy,e);
		}
	}

	public void release3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).release3(v,mod,jpxx,jpyy,e);
		}
	}

	public void click3(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).click3(v,mod,jpxx,jpyy,clickNumber,e);
		}
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).mouseMoved(v,jpx,jpy,e);
		}
	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).mouseDragged(v,mod,buttonNumber,jpxx,jpyy,e);
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).mouseWheelMoved(v,wheelDirection,jpxx,jpyy,e);
		}
	}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).Kpress(v,c,code,mod,e);
		}
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).Krelease(v,c,code,mod,e);
		}
	}

	public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
		lastPicked = pick();
		if(dispatchTable.containsKey(lastPicked)){	
			dispatchTable.get(lastPicked).Ktype(v,c,code,mod,e);
		}
	}

	public void enterGlyph(Glyph g) {
		if(dispatchTable.containsKey(g)){
			dispatchTable.get(g).glyphEntered();
		}
	}

	public void exitGlyph(Glyph g) {
		if(dispatchTable.containsKey(g)){
			dispatchTable.get(g).glyphExited();		
		}
	}

	public void viewActivated(View v) {}

	public void viewClosing(View v) {}

	public void viewDeactivated(View v) {}

	public void viewDeiconified(View v) {}

	public void viewIconified(View v) {}


	public void setAlwaysRepick(boolean alwaysRepick) {
		this.alwaysRepick = alwaysRepick;
	}


	public boolean isAlwaysRepick() {
		return alwaysRepick;
	}

	

	public void press(double x, double y, int i) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			if(i==1)dispatchTable.get(lastPicked).press1(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON1));
			if(i==2)dispatchTable.get(lastPicked).press2(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON2));
			if(i==3)dispatchTable.get(lastPicked).press3(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON3));
		}
	}


	public void release(double x, double y, int i) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			if(i==1)dispatchTable.get(lastPicked).release1(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON1));
			if(i==2)dispatchTable.get(lastPicked).release2(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON2));
			if(i==3)dispatchTable.get(lastPicked).release3(vp,0,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON3));
		}
	}


	public void click(double x, double y, int i) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			if(i==1)dispatchTable.get(lastPicked).click1(vp,0,jpxx,jpyy,1,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON1));
			if(i==2)dispatchTable.get(lastPicked).click2(vp,0,jpxx,jpyy,1,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON2));
			if(i==3)dispatchTable.get(lastPicked).click3(vp,0,jpxx,jpyy,1,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON3));
		}
	}


	public void mouseMoved(double x, double y) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			dispatchTable.get(lastPicked).mouseMoved(vp,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.NOBUTTON));
		}
	}


	public void mouseDragged(double x, double y) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			dispatchTable.get(lastPicked).mouseDragged(vp,0,1,jpxx,jpyy,new MouseEvent(vp.getComponent(), MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, jpxx, jpyy, jpxx, jpyy, 0, false, MouseEvent.BUTTON1));
		}
	}


	public void mouseWheelMove(double x, double y, int i) {
		lastPicked = pick();
		int[] co = unproject(x, y);
		int jpxx = co[0];
		int jpyy = co[1];
		if(dispatchTable.containsKey(lastPicked)){
			dispatchTable.get(lastPicked).mouseWheelMoved(vp,(short)i,jpxx,jpyy,new MouseWheelEvent(vp.getComponent(), MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(), 0, jpxx, jpyy, 1, false, MouseEvent.BUTTON1,0,0));
		}
	}
	
	
	public int[] unproject(double vx,double vy){
		int[] res = new int[2];
		Camera c = virtualSpace.getCamera(0);
		java.awt.geom.Point2D.Double d = null;
		if(MasterMain.SMALLMODE){
			Dimension q = virtualSpace.getCamera(0).getOwningView().getPanelSize();
			d = new java.awt.geom.Point2D.Double(q.getWidth(),q.getHeight());
		}
		else d = new java.awt.geom.Point2D.Double(PCursor.wallBounds[2]-PCursor.wallBounds[0], PCursor.wallBounds[1]-PCursor.wallBounds[3]);
		double coef = c.focal / (c.focal+c.altitude);
		int cx = (int)Math.round((d.x/2)+(vx-c.vx)*coef);
		int cy = (int)Math.round((d.y/2)-(vy-c.vy)*coef);
		res[0] = cx;
		res[1] = cy;
		return res;
	}
	

	


}
