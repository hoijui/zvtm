package fr.inria.zvtm.gui;

import java.awt.geom.Point2D.Double;


import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.gui.Viewer;

public class HorizonUpdater implements CameraListener{

	private Viewer application;
	public HorizonUpdater(Viewer app){
		this.application = app;
	}
	@Override
	public void cameraMoved(Camera arg0, Double arg1, double arg2) {
		application.updateHorizon();
		application.updateCursor();
	}

}
