package fr.inria.zvtm.compositor;

import fr.inria.zvtm.gui.Viewer;
import fr.inria.zvtm.gui.client.ClientViewer;

public abstract class ZvtmCompositor {

	protected FrameManager framemanager;
	private Viewer viewer;
	
	
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
