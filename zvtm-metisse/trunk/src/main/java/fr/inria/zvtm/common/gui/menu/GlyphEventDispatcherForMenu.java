package fr.inria.zvtm.common.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.common.gui.CursorHandler;
import fr.inria.zvtm.common.gui.GlyphEventDispatcher;
import fr.inria.zvtm.common.gui.MainEventHandler;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * This class must be added to a view as a ViewListener. It dispatches glyph events on the 
 * subscribed glyph which is under the picker
 * @author Julien Altieri
 *
 */
public class GlyphEventDispatcherForMenu extends GlyphEventDispatcher implements ItemActivationListener {


	private boolean active = false;
	private MainEventHandler dominatedListener;
	private PopMenu menu;
	private Viewer viewer;
	private PCursor cursor;


	public GlyphEventDispatcherForMenu(PCursor cu,VirtualSpace vs,Viewer v) {
		super(cu.getPicker2(),vs);
		this.viewer = v;
		this.cursor = cu;
	}
	
	public void setPriorityOn(MainEventHandler v){
		dominatedListener = v;
	}

	public void setMenu(PopMenu menu){
		this.menu = menu;
		for (Item it : menu.getItems()) {
			it.addListener(this);
		}
	}

	private void setActive(boolean active) {
		this.active = active;
		if(active)dominatedListener.lock();
		else dominatedListener.unlock();
	}

	public boolean isActive() {
		return active;
	}

	public boolean testActivity(){
		for (Item it : menu.getItems()) {
			if(it.isActive()){
				setActive(true);
				return true;
			}
		}
		setActive(false);
		return false;
	}
	
	@Override
	public void mouseMoved(double x, double y) {
		super.mouseMoved(x, y);
		menu.mouseMove(x,y);
	}

	@Override
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {
		super.mouseMoved(v, jpx, jpy, e);
		testActivity();
		CursorHandler ch = viewer.getCursorHandler();
		menu.mouseMove(ch.getVX(), ch.getVY());
	}
	
	@Override
	public void press(double x, double y, int i) {
		super.press(x, y, i);
		menu.tryToBanish();
	}
	
	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press1(v, mod, jpxx, jpyy, e);
		menu.tryToBanish();
	}

	@Override
	public void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press2(v, mod, jpxx, jpyy, e);
		menu.tryToBanish();
	}
	public void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press3(v, mod, jpxx, jpyy, e);
		menu.tryToBanish();	
	}
	
	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {
		testActivity();
		super.mouseDragged(v, mod, buttonNumber, jpxx, jpyy, e);
	}
	
	@Override
	public void mouseDragged(double x, double y) {
		testActivity();
		super.mouseDragged(x, y);
	}
	
	@Override
	public void enterGlyph(Glyph g) {
		testActivity();
		super.enterGlyph(g);
	}

	@Override
	public void exitGlyph(Glyph g) {
		testActivity();
		super.exitGlyph(g);
	}

	@Override
	public void activated() {
		testActivity();
	}

	@Override
	public void deactivated() {
		testActivity();
	}

	public PCursor getCursor() {
		return cursor;
	}
	
	
	
}
