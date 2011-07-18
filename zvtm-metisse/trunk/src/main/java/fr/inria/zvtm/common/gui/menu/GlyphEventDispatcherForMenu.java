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
 * This is a specific version of {@link GlyphEventDispatcher} for the {@link PopMenu}.
 * @author Julien Altieri
 *
 */
public class GlyphEventDispatcherForMenu extends GlyphEventDispatcher implements ItemActivationListener {


	private boolean active = false;
	private MainEventHandler dominatedListener;
	private PopMenu menu;
	private Viewer viewer;
	private PCursor cursor;

	/**
	 * 
	 * @param cu The {@link PCursor} that will trigger events
	 * @param vs The {@link VirtualSpace} in which Metisse windows are drawn
	 * @param v The owner {@link Viewer}
	 */
	public GlyphEventDispatcherForMenu(PCursor cu,VirtualSpace vs,Viewer v) {
		super(cu.getPicker2(),vs);
		this.viewer = v;
		this.cursor = cu;
	}

	/**
	 * {@link GlyphEventDispatcherForMenu} has priority on the specified {@link MainEventHandler}. No event will be transmitted to the specified {@link MainEventHandler} if one {@link Item} of the {@link PopMenu} is active.
	 * @param v a {@link MainEventHandler}
	 */
	public void setPriorityOn(MainEventHandler v){
		dominatedListener = v;
	}

	/**
	 * Attach the specified {@link PopMenu} to this dispatcher. It also automatically registers each {@link Item} of the {@link PopMenu}.
	 * @param menu a {@link PopMenu} object
	 */
	public void setMenu(PopMenu menu){
		this.menu = menu;
		for (Item it : menu.getItems()) {
			it.addListener(this);
		}
	}

	/**
	 * When set to true, no event will be transmitted to elsewhere than in the {@link PopMenu} layer.
	 * @param active
	 */
	private void setActive(boolean active) {
		this.active = active;
		if(active)dominatedListener.lock();
		else dominatedListener.unlock();
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * Activates the {@link GlyphEventDispatcherForMenu} if at least one {@link Item} is active, deactivates it otherwise.
	 * @return the activity state 
	 */
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
	
	@Override
	public void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press3(v, mod, jpxx, jpyy, e);
		menu.tryToBanish();	
	}

	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {
		super.mouseDragged(v, mod, buttonNumber, jpxx, jpyy, e);
		testActivity();
	}

	@Override
	public void mouseDragged(double x, double y) {
		super.mouseDragged(x, y);
		testActivity();
	}

	@Override
	public void enterGlyph(Glyph g) {
		super.enterGlyph(g);
		testActivity();
	}

	@Override
	public void exitGlyph(Glyph g) {
		super.exitGlyph(g);
		testActivity();
	}

	@Override
	public void activated() {
		testActivity();
	}

	@Override
	public void deactivated() {
		testActivity();
	}

	/**
	 * 
	 * @return The {@link PCursor} attached to this {@link GlyphEventDispatcherForMenu}
	 */
	public PCursor getCursor() {
		return cursor;
	}



}
