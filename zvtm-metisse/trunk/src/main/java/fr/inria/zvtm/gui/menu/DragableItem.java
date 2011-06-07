package fr.inria.zvtm.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.ViewPanel;

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
		pvx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		pvy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;
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
		vx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		vy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;
		
		if(freedom){
			lastAltFactor = parent.getAltFactor();
			parent.refreshAltFactor();
			offsetx += (vx/parent.getAltFactor()-pvx/lastAltFactor);
			offsety += (vy/parent.getAltFactor()-pvy/lastAltFactor);
			shape.move((vx/parent.getAltFactor()-pvx/lastAltFactor),(vy/parent.getAltFactor()-pvy/lastAltFactor));
		}
		pvx = vx;
		pvy = vy;
	}

	public void setFreedom(boolean freedom) {
		this.freedom = freedom;
	}

	public boolean isFree() {
		return freedom;
	}
}
