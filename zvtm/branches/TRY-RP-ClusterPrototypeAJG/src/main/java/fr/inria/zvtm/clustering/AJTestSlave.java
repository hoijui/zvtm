package fr.inria.zvtm.clustering;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.Vector;

/**
 * Slave application that observes a VS replica.
 * We don't bother about cameras yet.
 */
public class AJTestSlave {
	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;

	AJTestSlave() throws Exception {
		vsm.setDebug(true);
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		SlaveUpdater updater = new SlaveUpdater(vs);
		Camera c = vsm.addCamera(vs);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(c);
		View view = vsm.addExternalView(vcam, "slaveView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);

	}

	public static void main(String[] args) throws Exception {
		new AJTestSlave();
	}
}

