/*
 *   Copyright (c) INRIA, 2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package fr.inria.zvtm.basicui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;

public class WildViewer {

	static final int VIEW_W = 1024;
	static final int VIEW_H = 768;

	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	VirtualSpace mSpace;
	Camera mCamera;
	View mView;
	ViewListener eh;

	static final short CONFIG_WALL = 0;
	static final short CONFIG_SIMULATOR = 1;
	static final short CONFIG_SINGLE = 2;

	public WildViewer(short config){
        vsm.setMaster("WildViewer");
        mSpace = vsm.addVirtualSpace("mainSpace");
        mCamera = mSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
		// local view (on master)
        mView = vsm.addFrameView(cameras, "zvtm-cluster-basicui", View.STD_VIEW, VIEW_W, VIEW_H, true);
		mView.setAntialiasing(true);
        eh = new WildViewerEvtHld(this);
        mView.setListener(eh, 0);
        mView.setBackgroundColor(Color.BLACK);
		mView.getCursor().setColor(Color.WHITE);
		mView.getCursor().setHintColor(Color.WHITE);
        // clustered view (wall)
		if (config == CONFIG_WALL){
		    // wall made of 6 x 4 32" displays (1920x1080 each, with approx 100px bezels -- overlay mode)
			ClusterGeometry cg = new ClusterGeometry(2120, 1280, 6, 4);
	        Vector ccameras = new Vector();
	        ccameras.add(mCamera);
	        ClusteredView cv = new ClusteredView(cg, 3, 6, 4, ccameras);
	        vsm.addClusteredView(cv);
		}
		else if (config == CONFIG_SIMULATOR){
			// simulator on a single machine, with 6x4 small windows pretending to be different tiles
			ClusterGeometry cg = new ClusterGeometry(200, 112, 6, 4);
	        Vector ccameras = new Vector();
	        ccameras.add(mCamera);
	        ClusteredView cv = new ClusteredView(cg, 3, 6, 4, ccameras);
	        vsm.addClusteredView(cv);
		}
		else if (config == CONFIG_SINGLE){
	        ClusterGeometry cg = new ClusterGeometry(800, 600, 1, 1);
	        Vector ccameras = new Vector();
	        ccameras.add(mCamera);
	        ClusteredView cv = new ClusteredView(cg, 0, 1, 1, ccameras);
	        vsm.addClusteredView(cv);
		}
	}

	void init(){
		mSpace.addGlyph(new VCircle(0, 0 , 0, 400, Color.RED));
		for (float angle=0;angle<2*Math.PI;){
			mSpace.addGlyph(new VRectangle(400*Math.cos(angle), 400*Math.sin(angle), 10, 20, 20, Color.WHITE));
			angle += Math.PI/12d;
		}
	}

	public static void main(String[] args){
		short cfg = CONFIG_WALL;
		for (String arg:args){
			if (arg.equals("-single")){cfg = CONFIG_SINGLE;}
			else if (arg.equals("-simulator")){cfg = CONFIG_SIMULATOR;}
			else if (arg.equals("-wall")){cfg = CONFIG_WALL;}
		}
		(new WildViewer(cfg)).init();
	}

}

class WildViewerEvtHld extends ViewAdapter {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_COEF = 21.0f;
    static final float WHEEL_ZOOMOUT_COEF = 22.0f;
    static float WHEEL_MM_STEP = 1.0f;

    //remember last mouse coords
    int lastJPX,lastJPY;

    WildViewer application;

    boolean panning = false;

    WildViewerEvtHld(WildViewer app){
        this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        panning = true;
        v.setDrawDrag(true);
    }

	public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	    application.mCamera.setXspeed(0);
        application.mCamera.setYspeed(0);
        application.mCamera.setZspeed(0);
        v.setDrawDrag(false);
        panning = false;
	}

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
		if (panning){
            Camera c = v.cams[0];
			double a = (c.focal+Math.abs(c.altitude))/c.focal;
			application.mCamera.setXspeed((long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)));
			application.mCamera.setYspeed((long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)));
			application.mCamera.setZspeed(0);
		}
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
        double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
        if (wheelDirection  == WHEEL_UP){
            // zooming in
            application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_COEF);
            VirtualSpaceManager.INSTANCE.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN, zooming out
            application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_COEF);
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

	public void enterGlyph(Glyph g){
	    g.highlight(true, null);
	}

	public void exitGlyph(Glyph g){
	    g.highlight(false, null);
	}

}
