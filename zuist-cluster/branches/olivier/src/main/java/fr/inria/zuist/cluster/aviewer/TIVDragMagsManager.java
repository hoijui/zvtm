package fr.inria.zuist.cluster.aviewer;

import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;
import fr.inria.zvtm.event.CameraListener;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.PseudoView;
import fr.inria.zuist.engine.SceneManager;

import fr.inria.zvtm.cluster.ClusteredView;

class TIVDragMagsManager  {

	public static int DDM_WIDTH = 150;
    public static int DDM_HEIGHT = 150;
    public static double DDM_SCALE_FAC = 4.0;
    public static Color  DDM_BAR_COLOR = Color.GRAY;
    public static Color  DDM_INSIDE_BAR_COLOR = Color.RED;
    public static Color  DDM_BORDER_COLOR = Color.BLUE;
    public static Color  DDM_INSIDE_BORDER_COLOR = Color.RED;

	Viewer application;
	Camera mCamera;
    VirtualSpaceManager vsm;
    HashMap<DragMag, PseudoView> dmset = new HashMap<DragMag, PseudoView>();

	TIVDragMagsManager(Viewer app){
        this.application = app;
        this.vsm = VirtualSpaceManager.INSTANCE;
        this.mCamera = app.mCamera;
	}

	int count = 0;

	void addDragMag(int type)
	{
		count++;
		VirtualSpace vs = vsm.addVirtualSpace("dragmag "+count);
        Camera cam = vs.addCamera();
        int width = DDM_WIDTH;
        int height = DDM_HEIGHT;
		DragMag dg = new DragMag(
			//application.panelWidth/2 - DDM_WIDTH/2, 
			//application.panelHeight/2 - DDM_HEIGHT/2,
			(int)application.getDisplayWidth()/2, 
			(int)application.getDisplayHeight()/2, 
        	DDM_WIDTH, DDM_HEIGHT,
			cam, mCamera, null, DDM_SCALE_FAC, type);
		//vsm.addPortal(dg, application.mView);
		vsm.addClusteredPortal(dg, application.clusteredView);
		dg.updatePanelSize((int)application.getDisplayWidth(), (int)application.getDisplayHeight());
		dg.setBackgroundColor(Color.BLACK);
		//dg.setDragBarColor(DDM_BAR_COLOR);
        dg.setBorder(DDM_BORDER_COLOR);
        //dg.setPortalListener(application.eh);
     	PseudoView pv = new PseudoView(vs, cam, width, height);
        application.sm.addPseudoView(pv);
        dg.setupCamera();
        dmset.put(dg, pv);
	}

	void addDragMag()
	{
		addDragMag(DragMag.DM_TYPE_DRAGMAG);
	}

	public void dragMagResized(DragMag dm){
		PseudoView pv = dmset.get(dm);
		if (pv != null){
			pv.sizeTo(dm.w, dm.h);
		}
	}

	public DragMag checkVis(int jpx, int jpy){
		DragMag ret = null;
		for(Map.Entry<DragMag, PseudoView> entry: dmset.entrySet()) {
			DragMag dm = entry.getKey();
			if (dm.getType() != DragMag.DM_TYPE_DRAGMAG) continue;
			if (dm.coordInsideVis(jpx, jpy)) { ret = dm; }
		}
		return ret;
	}

}