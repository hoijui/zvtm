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
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.EndAction;

import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VText;

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

    static final String T_POINT_TUIO = "Pt";
    static final int Z_GLYPH = 100;
    static final int Z_LINE = 5;
    static final int Z_CIRCLE = 10;

    static final int VIEW_W = 1280;
    static final int VIEW_H = 1024;

    public static double SCENE_W = 11520;//12000;
    public static double SCENE_H = 4320;//4500;

    VirtualSpaceManager vsm;
    static final String mSpaceName = "Scene Space";
    static final String cursorSpaceName = "Cursor Space";
    VirtualSpace mSpace, cursorSpace;
    Camera mCamera, cursorCamera;
    View mView;
    ViewListener eh;

    ClusteredView cv;
    ClusterGeometry cg;

    //TuioListener tl;

    public Calibrator(WEOptions options){
        
        initGUI(options);
        TUIOEventHandler teh = new TUIOEventHandler(this);

        initScene(options.sceneWidth, options.sceneHeight, options.sceneStep);
    }

    public void initGUI(WEOptions options){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("Calibrator");

        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = mSpace.addCamera();
        cursorSpace = vsm.addVirtualSpace(cursorSpaceName);
        cursorCamera = cursorSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(cursorCamera);
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
        ccameras.add(cursorCamera);
        cv = new ClusteredView(cg, options.numRows-1, options.numCols, options.numRows, ccameras);
        cv.setBackgroundColor(Color.BLACK);
        vsm.addClusteredView(cv);

    }

    void initScene(int w, int h, int step){
        SCENE_W = w;
        SCENE_H = h;
        // vertical lines
        int i = -w/2;
        while (i <= w/2){
            VSegment s = new VSegment(i, 0, Z_LINE, HV_LINE_COLOR, 1, 0);
            s.setEndPoints(i, -h/2, i, h/2);
            mSpace.addGlyph(s);
            i += step;
        }
        // horizontal lines
        i = -h/2;
        while (i <= h/2){
            VSegment s = new VSegment(0, i, Z_LINE, HV_LINE_COLOR, 1, 0);
            s.setEndPoints(-w/2, i, w/2, i);
            mSpace.addGlyph(s);
            i += step;
        }
        // diagonal lines
        i = -w;
        while (i <= w){
            VSegment s = new VSegment(i, 0, Z_LINE, D_LINE_COLOR, 1, 0);
            s.setEndPoints(i+h/2, -h/2, i-h/2, h/2);
            mSpace.addGlyph(s);
            s = new VSegment(i, 0, Z_LINE, D_LINE_COLOR, 1, 0);
            s.setEndPoints(i+h/2, h/2, i-h/2, -h/2);
            mSpace.addGlyph(s);
            i += step;
        }
        // circles
        i = step;
        while (i<=w/2 && i<=h/2){
            VCircle c = new VCircle(0, 0, Z_CIRCLE, 2*i, Color.BLACK, CIRCLE_COLOR);
            c.setFilled(false);
            mSpace.addGlyph(c);
            i += step;
        }

        int[] x = {-w/4, w/4};
        int[] y = {-h/4, h/4};
        for(int ii : x)
            for(int jj : y){
                VCircle c = new VCircle(ii, jj, Z_CIRCLE, step, Color.GREEN, Color.GREEN);
                c.setFilled(false);
                mSpace.addGlyph(c);
                VSegment s = new VSegment(ii-step/2, jj, ii+step/2, jj, Z_CIRCLE, Color.GREEN, 1);
                mSpace.addGlyph(s);
                s = new VSegment(ii, jj-step/2, ii, jj+step/2, Z_CIRCLE, Color.GREEN, 1);
                mSpace.addGlyph(s);
            }

        //enumeration
        String[] letters = {"A", "B", "C"};
        String[] numbers = {"1", "2", "3", "4"};
        String[] orientations = {"left", "right"};

        int incrX = w/6;
        int incrY = h/4;
        int j = h/4 + h/8;
        for(String number: numbers){
            i = -w/3 - w/12;
            for(String letter: letters){
                for (String orientation: orientations ) {
                    VText labelPC = new VText(i, j, Z_CIRCLE, CIRCLE_COLOR, letter+number+"_"+orientation, VText.TEXT_ANCHOR_MIDDLE, 0.5f);
                    labelPC.setScale(20);
                    mSpace.addGlyph(labelPC);
                    i += incrX;               
                }
            }
            j -= incrY;
        }
    }

    void addObject(TuioPoint p){
        Point2D.Double np = normalize(p);
        VCircle c = new VCircle(np.x*SCENE_W-SCENE_W/2, np.y*SCENE_H-SCENE_H/2, Z_GLYPH, 20, Color.RED, Color.RED, 0.3f);
        c.setDrawBorder(false);
        c.setType(T_POINT_TUIO);
        mSpace.addGlyph(c);
    }

    Point2D.Double normalize(TuioPoint p){
        System.out.println("    " + "( " + p.getX() + ", " + p.getY() + " )");
        return new Point2D.Double(p.getX(), p.getY());
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



    void centeredZoom(double f, double x, double y){
        Location l = mCamera.getLocation();
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        double newz = mCamera.focal * a * f - mCamera.focal;
        if (newz < 0){
            newz = 0;
            f = mCamera.focal / (a*mCamera.focal);
        }

        double xx = (long)((double)x - (SCENE_W/2.0))*a + l.getX();
        double yy = (long)(-(double)y + (SCENE_H/2.0))*a + l.getY();

        double dx = l.getX() - xx;
        double dy = l.getY() - yy;

        double newx = l.getX() + (f*dx - dx); // *a/(mCamera.altitude+ mCamera.focal));
        double newy = l.getY() + (f*dy - dy);

        mCamera.setLocation(new Location(newx, newy, newz));
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

    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        VCircle c = new VCircle(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate(), Calibrator.Z_GLYPH, 20, Color.RED, Color.RED);
        c.setDrawBorder(false);
        c.setType(Calibrator.T_POINT_TUIO);
        application.mSpace.addGlyph(c);
    }

    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        System.out.println("release3");
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
        if(g.getType().equals(Calibrator.T_POINT_TUIO)){
            Point2D.Double l = g.getLocation();
            VText label = new VText(l.getX() + 100, l.getY(), Calibrator.Z_GLYPH, Color.RED, Color.BLUE, "Hello", (short)100, 1.0f, 0.9f);
            g.setOwner((Object)label);
            application.mSpace.addGlyph(label);
        }
    }

    public void exitGlyph(Glyph g){
        // g.highlight(false, null);
        if(g.getType().equals(Calibrator.T_POINT_TUIO)){
            application.mSpace.removeGlyph((Glyph)g.getOwner());
        }
    }

}
