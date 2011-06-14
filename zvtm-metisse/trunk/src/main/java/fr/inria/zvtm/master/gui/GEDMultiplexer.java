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

public class GEDMultiplexer extends GlyphEventDispatcherForMenu {

	private HashMap<PopMenu, GlyphEventDispatcherForMenu> geds;
	
	public GEDMultiplexer() {
		super(new PCursor(), null,null);
		geds = new HashMap<PopMenu, GlyphEventDispatcherForMenu>();
	}

	public void subscribe(PopMenu menu, GlyphEventDispatcherForMenu ged) {
		geds.put(menu, ged);
		
	}

	public void unsubscribe(PopMenu menu) {
		geds.remove(menu);
	}
	
	public void subscribe(Glyph g,GlyphListener gl){
		
	//	geds.get(((Item)gl).getParent()).subscribe(g, gl);
	}
	
	public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {}
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {}
	public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpxx,int jpyy, MouseWheelEvent e) {}
	protected Glyph pick() {return null;}
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	public void press2(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	public void press3(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {}
	public boolean testActivity() {return false;}

}
