package fr.inria.zvtm.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;

import fr.inria.zvtm.engine.Picker;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;

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
	public static GlyphEventDispatcher INSTANCE;
	private Glyph lastPicked;
	private boolean alwaysRepick = true;

	public GlyphEventDispatcher(Picker p,VirtualSpace vs) {
		INSTANCE = this;
		dispatchTable = new HashMap<Glyph, GlyphListener>();
		this.picker = p;
		this.virtualSpace = vs;
	}


	public void subscribe(Glyph g,GlyphListener gl){
		dispatchTable.put(g, gl);
	}

	private Glyph pick(){
		if(alwaysRepick){			
			lastPicked = picker.pickOnTop(virtualSpace);
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



}
