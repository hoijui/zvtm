package fr.inria.zvtm.clustering;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.clustering.ObjIdFactory;

// java -cp target/zvtm-0.10.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar fr.inria.zvtm.clustering.AJTest

/**
 * Put some code to test here.
 * This class will be deleted in the future.
 */
public class AJTest {
	public static void main(String[] args){
		VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		//this is just a test; of course we should 
		//never query a VirtualSpace for a glyph whose
		//identity we just created, it is guaraneed to return null
		vs.getGlyphById(ObjIdFactory.next());	
		VRectangle rect = new VRectangle(10, 10, 0, 300, 400, Color.RED);
		vsm.addGlyph(rect, vs);
		rect.moveTo(40,50);
		rect.move(10,20);	
	}
}

