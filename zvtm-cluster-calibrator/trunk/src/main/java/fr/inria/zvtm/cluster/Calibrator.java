/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.Image;
import javax.swing.ImageIcon;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VImage;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import TUIO.TuioListener;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioTime;
import TUIO.TuioPoint;

public class Calibrator {

    static final Color HV_LINE_COLOR = Color.GRAY;
    static final Color D_LINE_COLOR = Color.DARK_GRAY;
    static final Color CIRCLE_COLOR = Color.GREEN;

    static final int VIEW_W = 1280;
    static final int VIEW_H = 1024;

    double SCENE_W = 12000;
    double SCENE_H = 4500;

    VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    VirtualSpace mSpace;
    Camera mCamera;
    View mView;
    ViewListener eh;

    ClusteredView cv;
    ClusterGeometry cg;

    TuioListener tl;

    public static final Image CALIBRATION_IMG = (new ImageIcon(Calibrator.class.getResource("/images/calibration_img_2048x768.png"))).getImage();

    public Calibrator(WEOptions options){
        vsm.setMaster("Calibrator");
        mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        mCamera = mSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        // local view (on master)
        mView = vsm.addFrameView(cameras, "zvtm-cluster-calibrator", View.STD_VIEW, VIEW_W, VIEW_H, true);
        mView.setAntialiasing(true);
        eh = new CalibratorListener(this);
        mView.setListener(eh, 0);
        mView.setBackgroundColor(Color.BLACK);
        mView.getCursor().setColor(Color.WHITE);
        mView.getCursor().setHintColor(Color.WHITE);
        cg = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
        Vector ccameras = new Vector();
        ccameras.add(mCamera);
        cv = new ClusteredView(cg, options.numRows-1, options.numCols, options.numRows, ccameras);
        cv.setBackgroundColor(Color.BLACK);
        vsm.addClusteredView(cv);
        initTUIO(options.tuioPort);
        initScene(options.sceneWidth, options.sceneHeight, options.sceneStep);
    }

    void initTUIO(int port){
        tl = new TUIOListener(this);
        TuioClient client = new TuioClient(port);
        client.addTuioListener(tl);
        client.connect();
        System.out.println("Listening to TUIO events on port "+port);
    }

    void initScene(int w, int h, int step){
        SCENE_W = w;
        SCENE_H = h;
        // vertical lines
        int i = -w/2;
        while (i <= w/2){
            VSegment s = new VSegment(i, 0, 5, HV_LINE_COLOR, 1, 0);
            s.setEndPoints(i, -h/2, i, h/2);
            mSpace.addGlyph(s);
            i += step;
        }
        // horizontal lines
        i = -h/2;
        while (i <= h/2){
            VSegment s = new VSegment(0, i, 5, HV_LINE_COLOR, 1, 0);
            s.setEndPoints(-w/2, i, w/2, i);
            mSpace.addGlyph(s);
            i += step;
        }
        // diagonal lines
        i = -w;
        while (i <= w){
            VSegment s = new VSegment(i, 0, 5, D_LINE_COLOR, 1, 0);
            s.setEndPoints(i+h/2, -h/2, i-h/2, h/2);
            mSpace.addGlyph(s);
            s = new VSegment(i, 0, 5, D_LINE_COLOR, 1, 0);
            s.setEndPoints(i+h/2, h/2, i-h/2, -h/2);
            mSpace.addGlyph(s);
            i += step;
        }
        // circles
        i = step;
        while (i<=w/2 && i<=h/2){
            VCircle c = new VCircle(0, 0, 10, 2*i, Color.BLACK, CIRCLE_COLOR);
            c.setFilled(false);
            mSpace.addGlyph(c);
            i += step;
        }

        int[] x = {-w/4, w/4};
        int[] y = {-h/4, h/4};
        for(int ii : x)
            for(int jj : y){
                VCircle c = new VCircle(ii, jj, 10, step, Color.GREEN, Color.GREEN);
                c.setFilled(false);
                mSpace.addGlyph(c);
                VSegment s = new VSegment(ii-step/2, jj, ii+step/2, jj, 5, Color.GREEN, 1);
                mSpace.addGlyph(s);
                s = new VSegment(ii, jj-step/2, ii, jj+step/2, 5, Color.GREEN, 1);
                mSpace.addGlyph(s);
            }
/*
        VImage img = new VImage(CALIBRATION_IMG);
        img.setWidth(SCENE_W);
        img.setHeight(SCENE_H);
        mSpace.addGlyph(img);
*/
    }

    void addObject(TuioPoint p){
        Point2D.Double np = normalize(p);
        VCircle c = new VCircle(np.x*SCENE_W-SCENE_W/2, np.y*SCENE_H-SCENE_H/2, 100, 20, Color.RED, Color.RED);
        c.setDrawBorder(false);
        mSpace.addGlyph(c);
    }

    Point2D.Double normalize(TuioPoint p){
        return new Point2D.Double(p.getX(), 1-p.getY());
    }

    public static void main(String[] args){
        WEOptions options = new WEOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }
        if (!options.fullscreen && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        new Calibrator(options);
    }

}

class TUIOListener implements TuioListener {

    Calibrator application;

    TUIOListener(Calibrator app){
        this.application = app;
    }

    public void addTuioCursor(TuioCursor tcur){
        System.out.println("A C "+tcur.getPosition().getX()+" "+tcur.getPosition().getY());
        application.addObject(tcur.getPosition());
    }

    public void addTuioObject(TuioObject tobj){
        System.out.println("A O "+tobj.getPosition());
        application.addObject(tobj.getPosition());
    }

    public void refresh(TuioTime btime){
        // System.out.println("R at "+btime);
    }

    public void removeTuioCursor(TuioCursor tcur){
        // System.out.println("R C "+tcur);
    }

    public void removeTuioObject(TuioObject tobj){
        // System.out.println("R O "+tobj);
    }

    public void updateTuioCursor(TuioCursor tcur){
        // System.out.println("U C "+tcur.getPosition().getX()+" "+tcur.getPosition().getY());
    }

    public void updateTuioObject(TuioObject tobj){
        // System.out.println("U O "+tobj.getPosition());
    }

}

class CalibratorListener extends ViewAdapter {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_COEF = 21.0f;
    static final float WHEEL_ZOOMOUT_COEF = 22.0f;
    static float WHEEL_MM_STEP = 1.0f;

    //remember last mouse coords
    int lastJPX,lastJPY;

    Calibrator application;

    boolean panning = false;

    CalibratorListener(Calibrator app){
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
        // g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        // g.highlight(false, null);
    }

}
