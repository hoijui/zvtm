package techniques.point;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.AbstractTechnique;

public abstract class AbstractPointTechnique extends AbstractTechnique {
	
	public AbstractPointTechnique(String id, ORDER o) {
		super(id, o, false);
	}
	
	public AbstractPointTechnique(String id, ORDER o, boolean c) {
		super(id, o, c);
	}
	
	public static AbstractPointTechnique createTechnique(String name) {
		
		// TODO
		if (name.equals("OneHandedPushPoint"))
		{
			return new OneHandedPushPoint("OneHandedPushPoint", ORDER.ZERO, 57109);
			
		} else if (name.equals("DesktopMouse"))
		{
			return new DesktopMouse("DesktopMouse", ORDER.ZERO);
			
		} else if (name.equals("DefaultLaserPoint")) {
			
			return new DefaultLaserPoint("DefaultLaserPoint", ORDER.ZERO, Viewer.DEFAULT_POINT_OSC_LISTENING_PORT);
			
		}else if (name.equals("OneHandedCircularLaserPoint")) {
			
			return new DefaultLaserPoint("OneHandedCircularLaserPoint", ORDER.ZERO, Viewer.DEFAULT_POINT_OSC_LISTENING_PORT);
			
		}
		

		System.err.println(
			"WARNING : unknown PAN technique name. Couldn't create technique.");

		return null;
		
	}

}
