package fr.inria.zvtm.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.ViewPanel;

public class ScaleItem extends DragableItem {

	private double lastScaleFactor;
	private double psvx;
	private double refAlt;
	private int lastJPX;
	private double vx;
	private double pvx;
	private double vy;
	private double pvy;


	public ScaleItem(PopMenu parent) {
		super(parent);
	}

	@Override
	protected String getState1ImageName() {
		return "scale.png";
	}
	
	@Override
	protected String getState2ImageName() {
		return "scalep.png";
	}

	@Override
	public void press1(ViewPanel v, int mod, int jpxx, int jpyy, MouseEvent e) {
		super.press1(v, mod, jpxx, jpyy, e);
		if(parent.parentFrame!=null){
			lastScaleFactor = parent.parentFrame.getScaleFactor();
			setFreedom(false);
		}
		else setFreedom(true);
		psvx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		refAlt = parent.viewer.getNavigationManager().getCamera().altitude;
		lastJPX = parent.viewer.getCursorHandler().getX();
		pvx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		pvy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;
	}
	
	@Override
	public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpxx,int jpyy, MouseEvent e) {
		super.mouseDragged(v, mod, buttonNumber, jpxx, jpyy, e);
		if(buttonNumber!=1)return;		
		double jpx = parent.viewer.getCursorHandler().getX();
		vx = parent.viewer.getCursorHandler().getVX()-parent.viewer.getNavigationManager().getCamera().vx;
		vy = parent.viewer.getCursorHandler().getVY()-parent.viewer.getNavigationManager().getCamera().vy;
		
		if(parent.parentFrame ==null){
			double alt = refAlt+ (0.03*Math.abs(jpx-lastJPX)*(jpx-lastJPX));
			parent.viewer.getNavigationManager().getCamera().setAltitude(alt);
		}
		else{
			parent.move((vx-pvx),(vy-pvy));
			parent.parentFrame.setScaleFactor(lastScaleFactor+(vx-psvx)*1./100);
		}
		System.out.println(parent.refVX);

		pvx = vx;
		pvy = vy;
	}
}
