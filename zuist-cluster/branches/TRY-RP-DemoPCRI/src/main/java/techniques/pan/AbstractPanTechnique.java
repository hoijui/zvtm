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
        if (name.equals("IPodPressLaserPan")) {

            return new IPodPressLaserPan("IPodPressLaserPan", ORDER.ZERO);
        }

            System.err.println(
                    "WARNING : unknown PAN technique name. Couldn't create technique.");

            return null;
        }

	@Override
       public void deleteStatLabels() {
	}
}
