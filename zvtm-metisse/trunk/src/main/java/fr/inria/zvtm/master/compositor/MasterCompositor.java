package fr.inria.zvtm.master.compositor;

import fr.inria.zvtm.common.compositor.ZvtmCompositor;
import fr.inria.zvtm.common.gui.Viewer;
import fr.inria.zvtm.master.gui.MasterViewer;

/**
 * Basic structure for the zvtm server's version of the {@link ZvtmCompositor}.
 * @author Julien Altieri
 *
 */
public class MasterCompositor extends ZvtmCompositor {

	/**
	 * This server version must use a {@link MasterViewer} as {@link Viewer}.
	 * @param v a {@link MasterViewer} object
	 */
	public MasterCompositor(MasterViewer v) {
		super(v);

	}

}
