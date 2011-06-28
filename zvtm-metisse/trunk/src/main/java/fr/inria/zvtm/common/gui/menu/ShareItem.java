package fr.inria.zvtm.common.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.engine.ViewPanel;

public class ShareItem extends ToggleItem {

	private boolean enabled = true;
	
	public ShareItem(PopMenu parent) {
		super(parent);
	}

	@Override
	public void appear() {
		super.appear();
		if(parent.parentFrame==null)setStatus(false);
		else{
			setStatus(parent.parentFrame.isShared());
		}
	}
	
	@Override
	protected String getState1ImageName() {
		return "unshare.png";
	}

	@Override
	protected String getState2ImageName() {
		return "unsharep.png";
	}
	
	@Override
	protected String getState3ImageName() {
		return "share.png";
	}

	@Override
	protected String getState4ImageName() {
		return "sharep.png";
	}

	public void setStatus(boolean s){
		enabled = s;
		setState(enabled?1:0);
	}
	
	@Override
	public void click1(ViewPanel v, int mod, int jpxx, int jpyy,
			int clickNumber, MouseEvent e) {
		super.click1(v, mod, jpxx, jpyy, clickNumber, e);
		if(parent.parentFrame==null)return;
		setStatus(!enabled);
		parent.parentFrame.setShared(enabled);
	}
	
}
