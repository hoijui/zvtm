package fr.inria.zvtm.common.compositor;

import fr.inria.zvtm.client.compositor.ForwardingFrameManager;
import fr.inria.zvtm.client.gui.ClientViewer;
import fr.inria.zvtm.common.gui.Viewer;

/**
 * Basic structure a zvtm compositor.
 * It requires a {@link Viewer} and a {@link FrameManager}
 * @author Julien Altieri
 *
 */
public abstract class ZvtmCompositor {

	protected FrameManager framemanager;
	private Viewer viewer;
	
	/**
	 * Create the structure (including the {@link FrameManager}) for the compositor. Based on a zvtm {@link Viewer};
	 * @param viewer
	 */
	public ZvtmCompositor(Viewer viewer) {
		this.setViewer(viewer);
		if(viewer instanceof ClientViewer)this.framemanager = new ForwardingFrameManager((ClientViewer) viewer);
		else this.framemanager = new FrameManager(viewer);
		this.getViewer().setFrameManager(this.framemanager);
	}


	private void setViewer(Viewer viewer) {
		this.viewer = viewer;
	}


	public Viewer getViewer() {
		return viewer;
	}
}
