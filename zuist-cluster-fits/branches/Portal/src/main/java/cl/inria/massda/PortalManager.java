
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

    static final String PORTAL_SPACE_NAME = "portalSpace_";
    String portalSpaceName;

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
	//public static final Color DM_COLOR = Color.RED;


    public Camera portalCamera;
	DraggableCameraPortal dmPortal;
	PortalSceneObserver pso;
	VRectangle magWindow;
    int magWindowW, magWindowN, magWindowE, magWindowS;

    double[] dmwnes = new double[4];


    //Camera ovCamera;
    Camera[] cameras;
    Camera mCamera;
	
	View mView;
	ClusteredView clView;
	VirtualSpace mSpace;
	JSkyFitsViewer app;

	PortalListener prtlEvntHndlr;

	Color dmColor;


	public PortalManager(JSkyFitsViewer app, Color color){
		this(app, app.getMainView(), app.getClusterView(), color);
	}

	public PortalManager(JSkyFitsViewer app, View mView, ClusteredView clView){
		this(app, mView, clView, Color.RED);
	}

	/*public PortalManager(JSkyFitsViewer app, View mView, ClusteredView clView, Color color){
		this(app, mView, clView, color, app.getPanelWidth()/2, app.getPanelHeight()/2);
		//this(app, mView, clView, color, app.getPanelWidth()/2-DM_PORTAL_WIDTH-1, app.getPanelHeight()/2-DM_PORTAL_HEIGHT-1);
	}
	*/

	public PortalManager(JSkyFitsViewer app, View mView, ClusteredView clView, Color color){
		
		System.out.println("PortalManager");

		this.app = app;
		this.mView = mView;
		this.clView = clView;
		this.mSpace = app.mSpace;
		this.mCamera = app.mCamera;
		/*this.portalSpace = portalSpace;*/
		dmColor = color;

		vsm = VirtualSpaceManager.INSTANCE;

		long epoch = System.currentTimeMillis();
		portalSpaceName = PORTAL_SPACE_NAME+epoch;
		portalSpace = vsm.addVirtualSpace(portalSpaceName);

		this.portalCamera = portalSpace.addCamera();
		portalCamera.moveTo(0,0);

		animator = vsm.getAnimationManager();

		prtlEvntHndlr = app.getPortalListener();

        initDM();
        //createDM(dmPosX, dmPosY, l);

	}

	void initDM(){
		System.out.println("initDM");
	    magWindow = new VRectangle(0, 0, 0, 1, 1, dmColor);
	    magWindow.setFilled(false);
	    magWindow.setBorderColor(dmColor);
	    mSpace.addGlyph(magWindow);
	    mSpace.hide(magWindow);
    }

	public void createDM(int x, int y, Location l){
		//if(dmPortal != null) killDM();
		System.out.println("createDM");

		dmPortal = new DraggableCameraPortal(x, y, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera);
		pso = new PortalSceneObserver(dmPortal, portalCamera, portalSpace);
		dmPortal.setPortalListener(prtlEvntHndlr);
		
		vsm.addPortal(dmPortal, mView);
		vsm.addClusteredPortal(dmPortal, clView);
		dmPortal.setBorder(dmColor);
		dmPortal.setDragBarColor(dmColor.darker());
		dmPortal.setBackgroundColor(dmColor);
		//dmPortal.setTranslucencyValue(0.8f);

		app.sm.addSceneObserver(pso);
        //Location l = dmPortal.getSeamlessView(mCamera);
        System.out.println("location: " + l.vx + ", " + l.vy);
        portalCamera.moveTo(l.vx, l.vy);
        double alt = ((mCamera.getAltitude()+mCamera.getFocal())/(DEFAULT_MAG_FACTOR)-mCamera.getFocal());
        System.out.println("alt: " + alt);
        if(alt > 1) portalCamera.setAltitude(alt);
        else portalCamera.setAltitude(1);
        updateMagWindow();

        int w = (int)Math.round(magWindow.getWidth() * mCamera.getFocal() / ((float)(mCamera.getFocal()+mCamera.getAltitude())));
        int h = (int)Math.round(magWindow.getHeight() * mCamera.getFocal() / ((float)(mCamera.getFocal()+mCamera.getAltitude())));
        dmPortal.sizeTo(w, h);
        mSpace.onTop(magWindow);
        mSpace.show(magWindow);
        //animator.createPortalAnimation(GraphicsManager.DM_PORTAL_ANIM_TIME, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
        //Animation as = animator.getAnimationFactory().createPortalSizeAnim(DM_PORTAL_ANIM_TIME, dmPortal,
        //    0, 0, true,

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
        
		/*
		Animation at = animator.getAnimationFactory().createPortalTranslation(DM_PORTAL_ANIM_TIME, dmPortal,
            new Point(-w/2, -h/2), true,
            IdentityInterpolator.getInstance(), null);
		*/
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

    public void moveTo(double vx, double vy){
    	portalCamera.moveTo(vx, vy);
    	updateMagWindow();
    }

    public Camera getCamera(){
    	return portalCamera;
    }

    //public void changeZoom(double vx, double vy, short direction){
    public void changeZoom(short direction){
    	double a = (portalCamera.focal+Math.abs(portalCamera.altitude)) / portalCamera.focal;
    	System.out.println("a: " + a);
    	System.out.println("wheel: " + direction);

    	switch(direction)
    	{
    		case ViewListener.WHEEL_UP:
				System.out.println("zoom in");
	            portalCamera.altitudeOffset(a*WHEEL_ZOOMIN_FACTOR);
	    		break;
	    	case ViewListener.WHEEL_DOWN:
	            portalCamera.altitudeOffset(-a*WHEEL_ZOOMOUT_FACTOR);
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
        vsm.destroyVirtualSpace(portalSpaceName);
        //magWindow = null;
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

