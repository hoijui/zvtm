package fr.inria.zvtm.common.gui.menu;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;

/**
 * This king of {@link Item} can be grabbed and leaves the menu when doing so. When the mouse is released, the menu moves itself and catch the {@link Item} back.
 * @author Julien Altieri
 *
 */
public abstract class DragableItem extends Item {

	private double lastAltFactor;
	private double offsetx;
	private double offsety;
	protected double vx;
	protected double vy;
	private double pvx;
	private double pvy;
	private boolean freedom = false;
	

	public DragableItem(PopMenu parent) {
		super(parent);
	}
	
	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press1(v, mod, jpxx, jpyy, e);
		drawDown();
		offsetx = 0;
		offsety = 0;
		lastAltFactor = parent.getAltFactor();		
		pvx = ((GlyphEventDispatcherForMenu)parent.ged).getCursor().getVSXCoordinate()-parent.viewer.getNavigationManager().getCamera().vx;
		pvy = ((GlyphEventDispatcherForMenu)parent.ged).getCursor().getVSYCoordinate()-parent.viewer.getNavigationManager().getCamera().vy;
	}

	@Override
	public void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.release1(v, mod, jpxx, jpyy, e);
		drawUp();
		parent.refreshAltFactor();
		if(freedom)
		parent.animatedMove(this,offsetx, offsety);
	}
	
	
	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {
		super.mouseDragged(v, mod, buttonNumber, jpxx, jpyy, e);
		if(buttonNumber!=1)return;
		vx = ((GlyphEventDispatcherForMenu)parent.ged).getCursor().getVSXCoordinate()-parent.viewer.getNavigationManager().getCamera().vx;
		vy = ((GlyphEventDispatcherForMenu)parent.ged).getCursor().getVSYCoordinate()-parent.viewer.getNavigationManager().getCamera().vy;
		
		if(freedom){
			lastAltFactor = parent.getAltFactor();
			parent.refreshAltFactor();
			offsetx += (vx/parent.getAltFactor()-pvx/lastAltFactor);
			offsety += (vy/parent.getAltFactor()-pvy/lastAltFactor);
			shape.move((vx/parent.getAltFactor()-pvx/lastAltFactor),(vy/parent.getAltFactor()-pvy/lastAltFactor));
			shape2.move((vx/parent.getAltFactor()-pvx/lastAltFactor),(vy/parent.getAltFactor()-pvy/lastAltFactor));
		}
		pvx = vx;
		pvy = vy;
	}

	/**
	 * When set to true, the {@link DragableItem} can leave the {@link PopMenu}. Otherwise, it is attached and the {@link PopMenu} follows.
	 * @param freedom
	 */
	public void setFreedom(boolean freedom) {
		this.freedom = freedom;
	}

	/**
	 * @see DragableItem#setFreedom(boolean)
	 * @return the freedom state
	 */
	public boolean isFree() {
		return freedom;
	}
	
	/**
	 * Transforms virtual coordinates into JPanel projected coordinates.
	 * @param vx x coordinate in the {@link VirtualSpace}
	 * @param vy y coordinate in the {@link VirtualSpace}
	 * @return int[] as follow: {x,y}
	 */
	public int[] project(double vx,double vy){
		int[] res = new int[2];
		Camera c = parent.getVirtualSpace().getCamera(0);
		Dimension d = parent.getVirtualSpace().getCamera(0).getOwningView().getPanelSize();
		double coef = c.focal / (c.focal+c.altitude);
		int cx = (int)Math.round((d.width/2)+(vx-c.vx)*coef);
		int cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
		res[0] = cx;
		res[1] = cy;
		return res;
	}
	
}
