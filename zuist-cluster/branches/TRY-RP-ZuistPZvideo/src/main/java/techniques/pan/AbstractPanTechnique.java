package techniques.pan;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.AbstractTechnique;

public abstract class AbstractPanTechnique extends AbstractTechnique {
	
	public AbstractPanTechnique(String id, ORDER o) {
		super(id, o, false);
	}
	
	public AbstractPanTechnique(String id, ORDER o, boolean c) {
		super(id, o, c);
	}
	
	public static AbstractPanTechnique createTechnique(String name)
	{

		if (name.equals("DesktopMouse"))
		{
			return new DesktopMouse("DesktopMouse", ORDER.ZERO);
			
		} else if (name.equals("MouseButtonLaserPan")) {
			
			return new MouseButtonLaserPan("MouseButtonLaserPan", ORDER.ZERO, Zoom.DEFAULT_PAN_OSC_LISTENING_PORT);
			
		} else if (name.equals("MouseButtonLaserPanWithoutWIS")) {
			
			return new MouseButtonLaserPanWithoutWIS("MouseButtonLaserPanWithoutWIS", ORDER.ZERO);
			
		} else if (name.equals("MouseButtonZLaserPanWithoutWIS")) {
			
			return new MouseButtonZLaserPanWithoutWIS("MouseButtonZLaserPanWithoutWIS", ORDER.ZERO);
			
		} else if (name.equals("IPodPressLaserPan")) {
			
			return new IPodPressLaserPan("IPodPressLaserPan", ORDER.ZERO);
			
		} else if (name.equals("IPodZoneLaserPan")) {
			
			return new IPodZoneLaserPan("IPodZoneLaserPan", ORDER.ZERO);
			
		}

		System.err.println(
			"WARNING : unknown PAN technique name. Couldn't create technique.");

		return null;
		
	}

	@Override
       public void deleteStatLabels() {
	}
}
