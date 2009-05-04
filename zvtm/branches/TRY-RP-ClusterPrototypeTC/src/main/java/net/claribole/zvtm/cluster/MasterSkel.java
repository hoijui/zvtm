package net.claribole.zvtm.cluster;

import net.claribole.zvtm.cluster.MetaCamera;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

//master application skeleton
public class MasterSkel {
	public MasterSkel(){
		//create virtualspace "protoSpace"
		VirtualSpaceManager vsm = VirtualSpaceManager.getInstance();
		VirtualSpace vs = vsm.addVirtualSpace("protoSpace");
		//create metacamera
		MetaCamera metacam = new MetaCamera(3,2,200,120,vs);
		vs.setMetaCamera(metacam);
		//signal that the space and metacamera are ready
		//to unfreeze the slaves
		vsm.signalMasterReady();
		//add an external view for interaction (scrolling) purposes
		//add a few glyphs to the virtual space
	}

	public static void main(String[] args){
		new MasterSkel();
	}
}
