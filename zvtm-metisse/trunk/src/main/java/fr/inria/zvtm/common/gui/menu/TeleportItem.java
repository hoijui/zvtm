package fr.inria.zvtm.common.gui.menu;

import java.awt.event.MouseEvent;

import fr.inria.zvtm.client.compositor.ForwardingFrameManager;
import fr.inria.zvtm.common.compositor.MetisseWindow;
import fr.inria.zvtm.engine.ViewPanel;

/**
 * Specification for {@link Item} to control the visibility of the {@link MetisseWindow} on which it is invoked on the wall.
 * @author Julien Altieri
 *
 */
public class TeleportItem extends ToggleItem {

	private boolean enabled = false;
	
	public TeleportItem(PopMenu parent) {
		super(parent);
	}

	@Override
	public void appear() {
		super.appear();
		if(parent.parentFrame==null)setStatus(false);
		else{
			setStatus(parent.parentFrame.isPublished());
		}
	}
	
	@Override
	protected String getState1ImageName() {
		return "towall.png";
	}

	@Override
	protected String getState2ImageName() {
		return "towallp.png";
	}
	
	@Override
	protected String getState3ImageName() {
		return "toscreen.png";
	}

	@Override
	protected String getState4ImageName() {
		return "toscreenp.png";
	}

	/**
	 * Set the status of the {@link Item} true for configuration 1 and 2 and false for 3 and 4
	 * @param s
	 */
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
		parent.parentFrame.setPublished(enabled);
		if (parent.viewer.getFrameManager() instanceof ForwardingFrameManager) {
			if(enabled){
				((ForwardingFrameManager)parent.viewer.getFrameManager()).publish(parent.parentFrame);
			}
			else {
				((ForwardingFrameManager)parent.viewer.getFrameManager()).unpublish(parent.parentFrame);
			}
		}
	}
	
}
