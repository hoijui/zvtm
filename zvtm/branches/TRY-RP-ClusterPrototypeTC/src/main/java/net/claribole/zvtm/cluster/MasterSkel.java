package net.claribole.zvtm.cluster;

import net.claribole.zvtm.cluster.MetaCamera;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.Vector;

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
		//TODO which camera to use, if any? metacam[0]?

		//add a few glyphs to the virtual space
		Glyph rect = new VRectangle(0,0,0,60,40,Color.GREEN);
		Glyph anotherRect = new VRectangle(30,50,0,60,70,Color.RED);

		vsm.addGlyph(rect, vs);
		vsm.addGlyph(anotherRect, vs);

		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(vs.getMetaCamera().retrieveCamera(0));
		View view = vsm.addExternalView(vcam, "masterView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
	}

	public static void main(String[] args){
		new MasterSkel();
	}
}

