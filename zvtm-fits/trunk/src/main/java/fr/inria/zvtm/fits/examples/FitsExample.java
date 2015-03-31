/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.examples;

import java.awt.Toolkit;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.fits.FitsHistogram;
import fr.inria.zvtm.fits.filters.HeatFilter;
import fr.inria.zvtm.fits.filters.NopFilter;
import fr.inria.zvtm.fits.filters.RainbowFilter;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.Utils;
import fr.inria.zvtm.fits.ZScale;

import java.awt.image.ImageFilter;

import edu.jhu.pha.sdss.fits.FITSImage;

import fr.inria.zvtm.glyphs.PRectangle;

import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Point2D;

import java.io.IOException;
import java.io.File;

import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import fr.inria.zvtm.fits.Grid;

// Options
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

//Menu
import java.awt.Cursor;


/**
 * Sample FITS application.
 */
public class FitsExample {


    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1280;
    static int VIEW_MAX_H = 800;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    static final Color BACKGROUND_COLOR = Color.GRAY;

	//shortcut
	VirtualSpaceManager vsm;
    FitsImage hi;
    double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;
    private RangeSelection rs;
    private View mView;

    static final String mSpaceName = "FITS Layer";
    static final String bSpaceName = "Data Layer";
    static final String mnSpaceName = "Menu Layer";

    static final int LAYER_FITS = 0;
    static final int LAYER_DATA = 1;
    static final int LAYER_MENU = 2;

    VirtualSpace mSpace, bSpace, mnSpace;
    Camera mCamera, bCamera, mnCamera;

    IFEEventHandler eh;
    public FitsMenu menu;

    static final String APP_TITLE = "FITS Example";


	FitsExample(FitsOptions options) throws IOException {

/*
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		Camera cam = vs.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(cam);

        mView = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, options.blockWidth, options.blockHeight, false, true, !options.fullscreen, null);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        mView.setListener(new PanZoomEventHandler());
*/

        initGUI(options);

        if(options.url != null){
            hi = new FitsImage(0,0,0,new URL(options.url));

        } else if(options.file != null){
            hi = new FitsImage(0,0,0,new File(options.file));

        } else {
            System.err.println("usage: FitsExample -file image_File or -url image_URL");
            System.exit(0);
            return;
        }

        //hi.setScaleMethod(FitsImage.ScaleMethod.LINEAR);//ASINH);
        //hi.setColorFilter(FitsImage.ColorFilter.RAINBOW);
        mSpace.addGlyph(hi, false);


/*
        scaleBounds = ZScale.computeScale(hi.getUnderlyingImage());
        hi.rescale(scaleBounds[0], scaleBounds[1], 1);
        //System.out.println(scaleBounds[0] + ", " + scaleBounds[1]);
*/

        menu.drawHistogram();

        //FitsHistogram hist = FitsHistogram.fromFitsImage(hi);

        //hist.reSize(0.8f);
        //mnSpace.addGlyph(hist);
        /*
        rs = new RangeSelection();
        double min = hi.getUnderlyingImage().getHistogram().getMin();
        double max = hi.getUnderlyingImage().getHistogram().getMax();
        rs.setTicksVal((scaleBounds[0]-min)/(max-min), (scaleBounds[1]-min)/(max-min));

        mSpace.addGlyph(rs);
        rs.move(0, -30);

        // example fake gradient
        Point2D start = new Point2D.Float(0,0);
        Point2D end = new Point2D.Float(200,0);
        */


    }

    void initGUI(FitsOptions options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mnSpace = vsm.addVirtualSpace(mnSpaceName);
        mCamera = mSpace.addCamera();
        bCamera = bSpace.addCamera();
        mnCamera = mnSpace.addCamera();
        Vector<Camera> cameras = new Vector<Camera>();
        cameras.add(mCamera);
        cameras.add(bCamera);
        cameras.add(mnCamera);
        mView = vsm.addFrameView(cameras, APP_TITLE, View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, null);
        // fullscreen or not
        if (options.fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }

        menu = new FitsMenu(this);

        eh = new IFEEventHandler(this);
        //mCamera.addListener(eh);
        mView.setListener(eh, LAYER_FITS);
        mView.setListener(eh, LAYER_DATA);
        mView.setListener(menu, LAYER_MENU);
        mView.getCursor().getPicker().setListener(menu);
        mView.setBackgroundColor(BACKGROUND_COLOR);

        //mView.setActiveLayer(2);

    }

    public void setColorFilter(ImageFilter filter){

        hi.setColorFilter(filter);
    }

    /*
    public void setScaleMethod(int scale){
        switch(scale){
            case FITSImage.SCALE_ASINH:
                hi.setScaleMethod(FitsImage.ScaleMethod.ASINH);
                break;
            case FITSImage.SCALE_HISTOGRAM_EQUALIZATION:
                hi.setScaleMethod(FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION);
                break;
            case FITSImage.SCALE_LINEAR:
                hi.setScaleMethod(FitsImage.ScaleMethod.LINEAR);
                break;
            case FITSImage.SCALE_LOG:
                hi.setScaleMethod(FitsImage.ScaleMethod.LOG);
                break;
            case FITSImage.SCALE_SQUARE:
                hi.setScaleMethod(FitsImage.ScaleMethod.SQUARE);
                break;
            case FITSImage.SCALE_SQUARE_ROOT:
                hi.setScaleMethod(FitsImage.ScaleMethod.SQUARE_ROOT);
                break;
            default:
                hi.setScaleMethod(FitsImage.ScaleMethod.LINEAR);
                break;
        }
        //menu.redrawHistogram();
    }
    */

    public boolean isRunningOnCluster(){
        return false;
    }

    int getDisplayWidth(){
        return (SCREEN_WIDTH > VIEW_MAX_W) ? VIEW_MAX_W : SCREEN_WIDTH;
    }

    int getDisplayHeight(){
        return (SCREEN_HEIGHT > VIEW_MAX_H) ? VIEW_MAX_H : SCREEN_HEIGHT;
    }

    int getColumnCount(){
        return 1;
    }

    int getRowCount(){
        return 1;
    }

    void windowLayout(){

        VIEW_X = 80;
        SCREEN_WIDTH -= 80;

        /*
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        */
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }



    private Point2D.Double viewToSpace(Camera cam, int jpx, int jpy){
        Location camLoc = cam.getLocation();
        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = mView.getPanelSize();

        //find coords of view origin in the virtual space
        double viewOrigX = camLoc.vx - (0.5*viewSize.width*altCoef);
        double viewOrigY = camLoc.vy + (0.5*viewSize.height*altCoef);

        return new Point2D.Double(
                viewOrigX + (altCoef*jpx),
                viewOrigY - (altCoef*jpy));
    }

    public VirtualSpace getMSpace(){
        return mSpace;
    }
    public VirtualSpace getMnSpace(){
        return mnSpace;
    }
    public FitsMenu getMenu(){
        return menu;
    }

    public int getViewW(){
        return VIEW_W;
    }
    public int getViewH(){
        return VIEW_H;
    }

    public Camera getMCamera(){
        return mCamera;
    }
    public Camera getMnCamera(){
        return mnCamera;
    }
    public FitsImage getImage(){
        return hi;
    }

	public static void main(String[] args) throws IOException{

        FitsOptions options = new FitsOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }

        new FitsExample(options);
	}



}

class IFEEventHandler implements ViewListener {

    public static final String T_FILTER = "Fltr";

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;


    FitsExample app;

    //private double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;
    //private RangeSelection rs;

    private int lastJPX;
    private int lastJPY;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
    static final short ZERO_ORDER = 0;
    static final short FIRST_ORDER = 1;
    short navMode = ZERO_ORDER;

    double angle = 0;


    IFEEventHandler(FitsExample app){
        this.app = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
        Point2D.Double cursorPos = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
        if(app.rs.overLeftTick(cursorPos.x, cursorPos.y)){
            dragLeft = true;
        } else if(app.rs.overRightTick(cursorPos.x, cursorPos.y)){
            dragRight = true;
        }
        */

        lastJPX = jpx;
        lastJPY = jpy;
        if (navMode == FIRST_ORDER){
            first_order_dragging = true;
            v.setDrawDrag(true);
        }
        else {
            // ZERO_ORDER
            zero_order_dragging = true;
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
        dragLeft = false;
        dragRight = false;
        double min = hi.getUnderlyingImage().getHistogram().getMin();
        double max = hi.getUnderlyingImage().getHistogram().getMax();
        app.hi.rescale(min + app.rs.getLeftValue()*(max - min),
                min + app.rs.getRightValue()*(max - min),
                1);
        */
        //v.parent.setActiveLayer(0);

        zero_order_dragging = false;
        if (first_order_dragging){
            Camera c = app.mCamera;
            c.setXspeed(0);
            c.setYspeed(0);
            c.setZspeed(0);
            v.setDrawDrag(false);
            first_order_dragging = false;
        }

        //System.out.println("panzoomEH release1");

    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

        Point2D.Double cur = new Point2D.Double(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        Point2D.Double fi = new Point2D.Double(0,0);
        System.out.println( "[" + app.hi.getFitsWidth()/2 + ", " + app.hi.getFitsHeight()/2 + "]");
        double x = cur.getX() - fi.getX() + app.hi.getFitsWidth()/2;
        double y = cur.getY() - fi.getY() + app.hi.getFitsHeight()/2;
        System.out.println( x + " - " + y );
        Point2D.Double wcs = app.hi.pix2wcs( x, y );
        System.out.println("pix2wcs("+ x+", "+y+" )");
        System.out.println("wcs: (" + wcs.getX() + ", " + wcs.getY() + ")");

    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //v.parent.setActiveLayer(2);
        /*
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        app.vsm.getActiveView().mouse.setSensitivity(false);
        */
        //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
        v.cams[0].setXspeed(0);
        v.cams[0].setYspeed(0);
        v.cams[0].setZspeed(0);
        v.setDrawDrag(false);
        app.vsm.getActiveView().mouse.setSensitivity(true);
        */
        //v.parent.setActiveLayer(0);
        //System.out.println("panzoomEH release3");
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){

        /*
        app.setCursorCoords(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        VirtualSpaceManager.INSTANCE.repaint();
        */

        //System.out.println(app.menu.BORDER_BOTTON_HISTOGRAM + " > " + jpy + " > " + app.menu.BORDER_TOP_HISTOGRAM);
        //System.out.println(app.menu.BORDER_LEFT_HISTOGRAM + " < " + jpx + " < " + app.menu.BORDER_RIGHT_HISTOGRAM);

        if((jpx < app.menu.WIDTH_MENU && jpy > app.menu.BORDER_BOTTON_FILTER && jpy < app.menu.BORDER_TOP_FILTER) ||
                    (jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM && jpx > app.menu.BORDER_LEFT_HISTOGRAM &&
                    jpx < app.menu.BORDER_RIGHT_HISTOGRAM )){
            v.parent.setActiveLayer(app.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        } else {
            v.parent.setActiveLayer(app.LAYER_FITS);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }


        /*
        Point2D.Double cur = new Point2D.Double(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        Point2D.Double fi = new Point2D.Double(0,0);
        System.out.println( "[" + app.hi.getFitsWidth()/2 + ", " + app.hi.getFitsHeight()/2 + "]");
        double x = cur.getX() - fi.getX() + app.hi.getFitsWidth()/2;
        double y = cur.getY() - fi.getY() + app.hi.getFitsHeight()/2;
        System.out.println( x + " - " + y );
        Point2D.Double wcs = app.hi.pix2wcs( x, y );
        System.out.println("pix2wcs("+ x+", "+y+" )");
        System.out.println("wcs: (" + wcs.getX() + ", " + wcs.getY() + ")");
        */


    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){

        Camera c = app.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (zero_order_dragging){
            c.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (first_order_dragging){
            if (mod == SHIFT_MOD){
                c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed( (c.altitude>0) ? ((lastJPY-jpy)*(ZOOM_SPEED_COEF)) : ((lastJPY-jpy)/(ZOOM_SPEED_COEF)));
            }
            else {
                c.setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/PAN_SPEED_COEF) : (jpx-lastJPX)/(a*PAN_SPEED_COEF));
                c.setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/PAN_SPEED_COEF) : (lastJPY-jpy)/(a*PAN_SPEED_COEF));
                c.setZspeed(0);
            }
        }

        /*

        if(buttonNumber == 1){
            /*
            if(dragLeft) {
                //rs.setLeftTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
            } else if(dragRight){
                //rs.setRightTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
            }
            *
        }

        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c = app.vsm.getActiveCamera();
            double a = (c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                v.cams[0].setXspeed(0);
                v.cams[0].setYspeed(0);
                v.cams[0].setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));

            }
            else {
                v.cams[0].setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/4.0) : (jpx-lastJPX)/(a*4));
                v.cams[0].setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));
                v.cams[0].setZspeed(0);
            }
        }
        */
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){

        if(app instanceof FitsExample){
            Camera c = app.mCamera;

            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            double mvx = v.getVCursor().getVSXCoordinate();
            double mvy = v.getVCursor().getVSYCoordinate();
            if (wheelDirection  == WHEEL_UP){
                // zooming out
                c.move(-((mvx - c.vx) * WHEEL_ZOOMOUT_FACTOR / c.focal),
                                         -((mvy - c.vy) * WHEEL_ZOOMOUT_FACTOR / c.focal));
                c.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
                app.vsm.repaint();
            }
            else {
                //wheelDirection == WHEEL_DOWN, zooming in
                if (c.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= c.getZoomFloor()){
                    // this test to prevent translation when camera is not actually zoming in
                    c.move((mvx - c.vx) * WHEEL_ZOOMIN_FACTOR / c.focal,
                                             ((mvy - c.vy) * WHEEL_ZOOMIN_FACTOR / c.focal));
                }
                c.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
                app.vsm.repaint();
            }


        }

    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
        //System.out.println("Ktype:" + c);
        if(app instanceof FitsExample){
            if(c == '-'){
                app.scaleBounds[1] -= 100;
                app.hi.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
            } else if (c == '+'){
                app.scaleBounds[1] += 100;
                app.hi.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
            } else if(c == 'g'){
                System.out.println("Grid");
                Camera cam = app.mCamera;
                double a = (cam.focal+Math.abs(cam.altitude)) / cam.focal;
                System.out.println("a: " + a);
                Grid grid = Grid.makeGrid( app.hi, 100 );
                app.hi.setGrid(grid);
                app.mSpace.addGlyph(grid);

            } else if(c == 'r'){
                app.hi.orientTo(angle);
                angle+=Math.PI/5;
            }
        }
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    void toggleNavMode(){
        switch(navMode){
            case FIRST_ORDER:{navMode = ZERO_ORDER;break;}
            case ZERO_ORDER:{navMode = FIRST_ORDER;break;}
        }
    }

}
