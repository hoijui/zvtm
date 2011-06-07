package fr.inria.zvtm.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.ViewPanel;


public class DragItem extends DragableItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int lastJPX;
	private int lastJPY;
	private double pvx;
	private double pvy;
	private double vx;
	private double vy;


	public DragItem(PopMenu parent) {
		super(parent); 
	}

	@Override
	protected String getState1ImageName() {
		return "drag.png";
	}

	@Override
	protected String getState2ImageName() {
		return "dragp.png";
	}

	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press1(v, mod, jpxx, jpyy, e);
		if(parent.parentFrame==null)parent.viewer.getCursor().setVisible(false);
		if(parent.parentFrame!=null)setFreedom(false);
		else setFreedom(true);
		lastJPX = parent.viewer.getCursorHandler().getX();
		lastJPY = parent.viewer.getCursorHandler().getY();
		pvx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		pvy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;

	}


	@Override
	public void release1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.release1(v, mod, jpxx, jpyy, e);
		if(parent.parentFrame==null){
			parent.viewer.getCursorHandler().move(jpxx, jpyy, e);
			parent.viewer.getNavigationManager().getCamera().setXspeed(0);
			parent.viewer.getNavigationManager().getCamera().setYspeed(0);
			parent.viewer.getCursor().setVisible(true);
		}
		
	}

	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {
		super.mouseDragged(v, mod, buttonNumber, jpxx, jpyy, e);
		if(buttonNumber!=1)return;		
		double jpx = parent.viewer.getCursorHandler().getX();
		double jpy = parent.viewer.getCursorHandler().getY();
		vx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		vy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;

		if(parent.parentFrame==null){
			parent.viewer.getNavigationManager().getCamera().setXspeed(((jpx-lastJPX)*(parent.getAltFactor()/parent.viewer.getNavigationManager().PAN_SPEED_COEF)));
			parent.viewer.getNavigationManager().getCamera().setYspeed(((lastJPY-jpy)*(parent.getAltFactor()/parent.viewer.getNavigationManager().PAN_SPEED_COEF)));
		}
		else{
			parent.parentFrame.moveGlyphOf(vx-pvx,vy-pvy);
			parent.move(vx-pvx,vy-pvy);
		}
		pvx = vx;
		pvy = vy;
	}
}

