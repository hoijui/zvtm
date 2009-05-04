package net.claribole.zvtm.cluster;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.Vector;

//Generic slave application
// - retrieves a slave camera from a MetaCamera
// - creates a std view 
// - dispays scene
public class SlaveApp {

	int blockNumber;
	VirtualSpaceManager vsm;
	View view;

	public SlaveApp(int blockNumber){
		vsm = VirtualSpaceManager.getInstance();
		VirtualSpaceManager.setDebug(true);
		System.out.println("slave (block " + blockNumber 
				+ "): waiting for master to initialize virtual space");

		vsm.awaitMaster();

		VirtualSpace vs = vsm.getVirtualSpace("protoSpace");

		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(vs.getMetaCamera().retrieveCamera(blockNumber));
		vsm.addExternalView(vcam, "slaveView", View.STD_VIEW,
				400, 300, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
	}

	public static void main(String[] args){
		new SlaveApp(0);
	}
}

