package fr.inria.zvtm.clustering;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.clustering.ObjIdFactory;

//java -cp target/zvtm-0.10.0-SNAPSHOT.jar:targetimingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar fr.inria.zvtm.clustering.AJTest
/**
 * Basic test class
 */
public class AJTest {
	public static void main(String[] args){
		VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");

		Camera c = vsm.addCamera(vs);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(c);
		View view = vsm.addExternalView(vcam, "masterView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);

		VRectangle rect = new VRectangle(10, 10, 0, 100, 150, Color.RED);
		vs.addGlyph(rect);
		rect.moveTo(40,50);
		rect.move(10,20);	
		//vs.removeGlyph(rect);
	}
}

