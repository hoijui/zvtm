
package cl.inria.massda;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;

import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.PortalListener;

import fr.inria.zuist.engine.PortalSceneObserver;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.SIRectangle;


import fr.inria.zvtm.cluster.ClusteredView;

import fr.inria.zuist.viewer.JSkyFitsViewer;

import java.awt.Color;
import java.awt.Point;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;


public class PortalManager{
	

	public static final int OV_PORTAL_WIDTH = 200;
	public static final int OV_PORTAL_HEIGHT = 200;
	static final Color OBSERVED_REGION_CROSSHAIR_COLOR = new Color(115, 83, 115);
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color BACKGROUND_COLOR = Color.GRAY;

	static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static final short WHEEL_DOWN = '0';
    static final short WHEEL_UP = '1';

	public VirtualSpaceManager vsm;

	CameraPortal ovPortal;
	
	//virtual space containing rectangle representing region seen through main camera (used in overview)
	VirtualSpace portalSpace;

	//name of the VTM virtual space holding the rectangle delimiting the region seen by main view in radar view
    //static final String rdRegionVirtualSpaceName = "radarSpace";

    //View rView;
    //static final String RADAR_VIEW_NAME = "Overview";
    

    //represents the region seen by main view in the radar view
    VRectangle observedRegion;

    AnimationManager animator;

    /* DragMag */
    public static final double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
	static double INV_MAG_FACTOR = 1/MAG_FACTOR;
	public static final float MAX_MAG_FACTOR = 12.0f;

    public static final int DM_PORTAL_WIDTH = 200;
	public static final int DM_PORTAL_HEIGHT = 200;
	public static final int DM_PORTAL_INITIAL_X_OFFSET = 150;
	public static final int DM_PORTAL_INITIAL_Y_OFFSET = 150;
	public static final int DM_PORTAL_ANIM_TIME = 150;
	public static final Color DM_COLOR = Color.RED;

    public Camera portalCamera;
	DraggableCameraPortal dmPortal;
	PortalSceneObserver pso;
	VRectangle magWindow;
    int magWindowW, magWindowN, magWindowE, magWindowS;
    boolean paintLinks = false;

    double[] dmwnes = new double[4];


    //Camera ovCamera;
    Camera[] cameras;
    Camera mCamera;
	
	View mView;
	ClusteredView clView;
	VirtualSpace mSpace;
	JSkyFitsViewer app;

	PortalListener prtlEvntHndlr;


	public PortalManager(JSkyFitsViewer app, View mView, ClusteredView clView){
		
		System.out.println("PortalManager");

		this.app = app;
		this.mSpace = app.mSpace;
		this.mView = mView;
		this.clView = clView;
		this.portalSpace = app.portalSpace;
		this.portalCamera = app.portalCamera;
		this.mCamera = app.mCamera;

		//rSpace = vsm.addVirtualSpace(rdRegionVirtualSpaceName);
        // camera for rectangle representing region seen in main viewport (in overview)
        //rSpace.addCamera();
		// DragMag portal camera (camera #2)
		//dmCamera = portalSpace.addCamera();
		//dmCamera = vss[0].addCamera();
		
		/*
		cameras = new Camera[vss.length];

		for( int i = 0; i < vss.length; i++ ){
			cameras[i] = vss[i].addCamera();
			dmCamera.stick(cameras[i]);
		}
		*/

		//ovCamera = mSpace.addCamera();

		vsm = VirtualSpaceManager.INSTANCE;

		animator = vsm.getAnimationManager();

		prtlEvntHndlr = app.getPortalListener();

		/*
		dmCamera = mSpace.addCamera();

		DraggableCameraPortal portal = new DraggableCameraPortal(250 ,250, 200, 200, dmCamera);

		vsm.addPortal(portal, mView);
		vsm.addClusteredPortal(portal, clView);

		portal.setBorder(Color.GREEN);
		
		SIRectangle seg1;
        SIRectangle seg2;
        observedRegion = new VRectangle(0, 0, 0, 10, 10, OBSERVED_REGION_COLOR, OBSERVED_REGION_CROSSHAIR_COLOR, 0.5f);
        //500 should be sufficient as the radar window is
        seg1 = new SIRectangle(0, 0, 0, 0, 500, OBSERVED_REGION_CROSSHAIR_COLOR);
        //not resizable and is 300x200 (see rdW,rdH below)
        seg2 = new SIRectangle(0, 0, 0, 500, 0, OBSERVED_REGION_CROSSHAIR_COLOR);
        space.addGlyph(observedRegion);
        space.addGlyph(seg1);
        space.addGlyph(seg2);
        Glyph.stickToGlyph(seg1, observedRegion);
        Glyph.stickToGlyph(seg2, observedRegion);
        observedRegion.setSensitivity(false);
		*/

        //createOverview();

        initDM();
        createDM(app.getPanelWidth()/2-DM_PORTAL_WIDTH-1, app.getPanelHeight()/2-DM_PORTAL_HEIGHT-1);

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

	void initDM(){
		System.out.println("initDM");
	    magWindow = new VRectangle(0, 0, 0, 1, 1, DM_COLOR);
	    magWindow.setFilled(false);
	    magWindow.setBorderColor(DM_COLOR);
	    mSpace.addGlyph(magWindow);
	    mSpace.hide(magWindow);
    }

	void createDM(int x, int y){
		System.out.println("createDM");

		dmPortal = new DraggableCameraPortal(x, y, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera); //dmCamera); //
		pso = new PortalSceneObserver(dmPortal, portalCamera, portalSpace);
		dmPortal.setPortalListener(prtlEvntHndlr);
		
		vsm.addPortal(dmPortal, mView);
		vsm.addClusteredPortal(dmPortal, clView);
		dmPortal.setBorder(DM_COLOR);
		dmPortal.setBackgroundColor(DM_COLOR);
		dmPortal.setTranslucencyValue(0.8f);

		app.sm.addSceneObserver(pso);
        Location l = dmPortal.getSeamlessView(mCamera);
        portalCamera.moveTo(l.vx, l.vy);
        portalCamera.setAltitude(((mCamera.getAltitude()+mCamera.getFocal())/(DEFAULT_MAG_FACTOR)-mCamera.getFocal()));
        updateMagWindow();

        int w = (int)Math.round(magWindow.getWidth() * mCamera.getFocal() / ((float)(mCamera.getFocal()+mCamera.getAltitude())));
        int h = (int)Math.round(magWindow.getHeight() * mCamera.getFocal() / ((float)(mCamera.getFocal()+mCamera.getAltitude())));
        dmPortal.sizeTo(w, h);
        mSpace.onTop(magWindow);
        mSpace.show(magWindow);
        paintLinks = true;
        //animator.createPortalAnimation(GraphicsManager.DM_PORTAL_ANIM_TIME, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
        Animation as = animator.getAnimationFactory().createPortalSizeAnim(DM_PORTAL_ANIM_TIME, dmPortal,
            DM_PORTAL_WIDTH-w, DM_PORTAL_HEIGHT-h, true,
            IdentityInterpolator.getInstance(), new EndAction(){
                public void execute(Object subject, Animation.Dimension dimension){
                    pso.cameraMoved();
                }
            });
        Animation at = animator.getAnimationFactory().createPortalTranslation(DM_PORTAL_ANIM_TIME, dmPortal,
            new Point(DM_PORTAL_INITIAL_X_OFFSET-w/2, DM_PORTAL_INITIAL_Y_OFFSET-h/2), true,
            IdentityInterpolator.getInstance(), null);
        animator.startAnimation(as, false);
        animator.startAnimation(at, false);

	}

	public void updateMagWindow(){
        System.out.println("updateMagWindow");
        if (dmPortal == null){return;}
        dmPortal.getVisibleRegion(dmwnes);
        magWindow.moveTo(portalCamera.vx, portalCamera.vy);

        magWindow.setWidth((dmwnes[2]-dmwnes[0]) + 1);
        magWindow.setHeight((dmwnes[1]-dmwnes[3]) + 1);
    }

    public void updateZoomWindow(){
        System.out.println("updateZoomWindow");
        portalCamera.moveTo(magWindow.vx, magWindow.vy);
    }

    public void changeZoom(double vx, double vy, short direction){
    	double a = (portalCamera.focal+Math.abs(portalCamera.altitude)) / portalCamera.focal;
    	System.out.println("a: " + a);
    	System.out.println("wheel: " + direction);

    	switch(direction)
    	{
    		case ViewListener.WHEEL_UP:
				System.out.println("zoom in");
	            portalCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
	    		break;
	    	case ViewListener.WHEEL_DOWN:
	            portalCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
	    		break;
	    	default:
	    		break;
    	}
    	updateMagWindow();
    	
    	
    }

    public void killDM(){
    	app.sm.removeSceneObserver(pso);
        pso = null;
        vsm.destroyPortal(dmPortal);
        dmPortal = null;
        mSpace.hide(magWindow);
        paintLinks = false;
        //prtlEvntHndlr.resetDragMagInteraction();
    }


    /*
    public void cameraMoved(Camera cam, Point2D.Double coord, double alt){
        System.out.println("cameraMoved");
        if (rView!=null){
            Camera c0=mSpace.getCamera(1);
            Camera c1=rSpace.getCamera(0);
            c1.vx=c0.vx;
            c1.vy=c0.vy;
            c1.focal=c0.focal;
            c1.altitude=c0.altitude;
            double[] wnes = mainView.getVisibleRegion(mSpace.getCamera(0));
            observedRegion.moveTo((wnes[0]+wnes[2])/2,(wnes[3]+wnes[1])/2);
            observedRegion.setWidth((wnes[2]-wnes[0]));
            observedRegion.setHeight((wnes[1]-wnes[3]));
        }
        vsm.repaint();
    }
    */



}

