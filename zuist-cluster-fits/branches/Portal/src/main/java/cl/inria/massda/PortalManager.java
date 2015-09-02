
package cl.inria.massda;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;

import fr.inria.zvtm.event.PortalListener;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.SIRectangle;


import fr.inria.zvtm.cluster.ClusteredView;

import fr.inria.zuist.viewer.JSkyFitsViewer;

import java.awt.Color;


public class PortalManager{
	
	public static final int DM_PORTAL_WIDTH = 200;
	public static final int DM_PORTAL_HEIGHT = 200;
	public static final int DM_PORTAL_INITIAL_X_OFFSET = 150;
	public static final int DM_PORTAL_INITIAL_Y_OFFSET = 150;
	public static final int DM_PORTAL_ANIM_TIME = 150;
	public static final Color DM_COLOR = Color.RED;

	public static final int OV_PORTAL_WIDTH = 200;
	public static final int OV_PORTAL_HEIGHT = 200;
	static final Color OBSERVED_REGION_CROSSHAIR_COLOR = new Color(115, 83, 115);
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color BACKGROUND_COLOR = Color.GRAY;

	public VirtualSpaceManager vsm;

	CameraPortal ovPortal;
	
	//virtual space containing rectangle representing region seen through main camera (used in overview)
	VirtualSpace rSpace;

	//name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view
    static final String rdRegionVirtualSpaceName = "radarSpace";

    View rView;
    static final String RADAR_VIEW_NAME = "Overview";
    

    //represents the region seen by main view in the radar view
    VRectangle observedRegion;

    public Camera dmCamera;
	DraggableCameraPortal dmPortal;
	VRectangle magWindow;
    int magWindowW, magWindowN, magWindowE, magWindowS;

    //Camera ovCamera;
    Camera[] cameras;
	
	View mView;
	ClusteredView clView;
	VirtualSpace mSpace;
	JSkyFitsViewer app;

	PortalListener prtlEvntHndlr;


	public PortalManager(JSkyFitsViewer app, Camera[] cameras, View mView, ClusteredView clView){
		
		System.out.println("PortalManager");

		this.app = app;
		this.mSpace = app.mSpace;
		this.mView = mView;
		this.clView = clView;

		this.cameras = cameras;
		//ovCamera = mSpace.addCamera();

		vsm = VirtualSpaceManager.INSTANCE;

		prtlEvntHndlr = app.getPortalListener();

		/*
		dmCamera = mSpace.addCamera();

		DraggableCameraPortal portal = new DraggableCameraPortal(250 ,250, 200, 200, dmCamera);

		vsm.addPortal(portal, mView);
		vsm.addClusteredPortal(portal, clView);

		portal.setBorder(Color.GREEN);
		*/

		rSpace = vsm.addVirtualSpace(rdRegionVirtualSpaceName);
        // camera for rectangle representing region seen in main viewport (in overview)
        rSpace.addCamera();
		// DragMag portal camera (camera #2)
		dmCamera = mSpace.addCamera();
		SIRectangle seg1;
        SIRectangle seg2;
        observedRegion = new VRectangle(0, 0, 0, 10, 10, OBSERVED_REGION_COLOR, OBSERVED_REGION_CROSSHAIR_COLOR, 0.5f);
        //500 should be sufficient as the radar window is
        seg1 = new SIRectangle(0, 0, 0, 0, 500, OBSERVED_REGION_CROSSHAIR_COLOR);
        //not resizable and is 300x200 (see rdW,rdH below)
        seg2 = new SIRectangle(0, 0, 0, 500, 0, OBSERVED_REGION_CROSSHAIR_COLOR);
        rSpace.addGlyph(observedRegion);
        rSpace.addGlyph(seg1);
        rSpace.addGlyph(seg2);
        Glyph.stickToGlyph(seg1, observedRegion);
        Glyph.stickToGlyph(seg2, observedRegion);
        observedRegion.setSensitivity(false);

		

        //createOverview();

        createDraggable(app.getPanelWidth()/2-DM_PORTAL_WIDTH-1, app.getPanelHeight()/2-DM_PORTAL_HEIGHT-1);

	}

	void createOverview(){

		System.out.println("Overview");

		ovPortal = new CameraPortal(app.getPanelWidth()-OV_PORTAL_WIDTH-1, app.getPanelHeight()-OV_PORTAL_HEIGHT-1, OV_PORTAL_WIDTH, OV_PORTAL_HEIGHT, cameras, OBSERVED_REGION_ALPHA);
		ovPortal.setPortalListener(prtlEvntHndlr);
		ovPortal.setBackgroundColor(BACKGROUND_COLOR);
		vsm.addPortal(ovPortal, mView);
		vsm.addClusteredPortal(ovPortal, clView);
		ovPortal.setBorder(Color.GREEN);

	}

	void createDraggable(int x, int y){

		System.out.println("Draggable");

		dmPortal = new DraggableCameraPortal(x, y, OV_PORTAL_WIDTH, OV_PORTAL_HEIGHT, cameras);
		dmPortal.setPortalListener(prtlEvntHndlr);
		//ovPortal.setBackgroundColor(BACKGROUND_COLOR);
		vsm.addPortal(dmPortal, mView);
		vsm.addClusteredPortal(dmPortal, clView);
		dmPortal.setBorder(DM_COLOR);

		

	}



}

