package fr.inria.zvtm.master.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;

import fr.inria.zvtm.common.gui.GlyphListener;
import fr.inria.zvtm.common.gui.PCursor;
import fr.inria.zvtm.common.gui.menu.GlyphEventDispatcherForMenu;
import fr.inria.zvtm.common.gui.menu.PopMenu;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * This class is a structure for keeping the relation between a {@link GlyphEventDispatcherForMenu} and a {@link PopMenu}. Its type is for compatibility.
 * @see GlyphEventDispatcherForMenu
 * @author Julien Altieri
 *
 */
public class GEDMultiplexer extends GlyphEventDispatcherForMenu {

	private HashMap<PopMenu, GlyphEventDispatcherForMenu> geds;
	
	public GEDMultiplexer() {
		super(new PCursor(), null,null);
		geds = new HashMap<PopMenu, GlyphEventDispatcherForMenu>();
	}

	/**
	 * Adds the specified {@link PopMenu} 
	 * @param menu
	 * @param ged
	 */
	public void subscribe(PopMenu menu, GlyphEventDispatcherForMenu ged) {
		geds.put(menu, ged);
		
	}

	/**
	 * Removes the specified {@link PopMenu} from the dispatch list.
	 * @param menu
	 */
	public void unsubscribe(PopMenu menu) {
		geds.remove(menu);
	}
	 
	/**
	 * This does nothing.
	 */
	public void subscribe(Glyph g,GlyphListener gl){
		
	//	geds.get(((Item)gl).getParent()).subscribe(g, gl);
	}
	
	/**
	 * This does nothing.
	 */
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {}
	/**
	 * This does nothing.
	 */
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {}
	/**
	 * This does nothing.
	 */
	public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpxx,int jpyy, MouseWheelEvent e) {}
	/**
	 * This does nothing.
	 */
	protected Glyph pick() {return null;}
	/**
	 * This does nothing.
	 */
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	/**
	 * This does nothing.
	 */
	public void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	/**
	 * This does nothing.
	 */
	public void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	/**
	 * This does nothing.
	 */
	public boolean testActivity() {return false;}

}
