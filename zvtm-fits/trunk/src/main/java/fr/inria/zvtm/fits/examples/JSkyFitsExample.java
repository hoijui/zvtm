/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.examples;

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import java.awt.Cursor;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.image.DataBuffer;

import java.io.IOException;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Vector;
import java.util.List;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VCross;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.JSkyFitsImage;

import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.Utils;
import fr.inria.zvtm.fits.filters.Aips0Filter;
import fr.inria.zvtm.fits.filters.BackgrFilter;
import fr.inria.zvtm.fits.filters.BlueFilter;
import fr.inria.zvtm.fits.filters.BlulutFilter;
import fr.inria.zvtm.fits.filters.ColorFilter;
import fr.inria.zvtm.fits.filters.GreenFilter;
import fr.inria.zvtm.fits.filters.HeatFilter;
import fr.inria.zvtm.fits.filters.Idl11Filter;
import fr.inria.zvtm.fits.filters.Idl12Filter;
import fr.inria.zvtm.fits.filters.Idl14Filter;
import fr.inria.zvtm.fits.filters.Idl15Filter;
import fr.inria.zvtm.fits.filters.Idl2Filter;
import fr.inria.zvtm.fits.filters.Idl4Filter;
import fr.inria.zvtm.fits.filters.Idl5Filter;
import fr.inria.zvtm.fits.filters.Idl6Filter;
import fr.inria.zvtm.fits.filters.IsophotFilter;
import fr.inria.zvtm.fits.filters.LightFilter;
import fr.inria.zvtm.fits.filters.ManycolFilter;
import fr.inria.zvtm.fits.filters.PastelFilter;
import fr.inria.zvtm.fits.filters.RainbowFilter;
import fr.inria.zvtm.fits.filters.Rainbow1Filter;
import fr.inria.zvtm.fits.filters.Rainbow2Filter;
import fr.inria.zvtm.fits.filters.Rainbow3Filter;
import fr.inria.zvtm.fits.filters.Rainbow4Filter;
import fr.inria.zvtm.fits.filters.RampFilter;
import fr.inria.zvtm.fits.filters.RandomFilter;
import fr.inria.zvtm.fits.filters.Random1Filter;
import fr.inria.zvtm.fits.filters.Random2Filter;
import fr.inria.zvtm.fits.filters.Random3Filter;
import fr.inria.zvtm.fits.filters.Random4Filter;
// import fr.inria.zvtm.fits.filters.Random5Filter;
// import fr.inria.zvtm.fits.filters.Random6Filter;
import fr.inria.zvtm.fits.filters.RealFilter;
import fr.inria.zvtm.fits.filters.RedFilter;
import fr.inria.zvtm.fits.filters.SmoothFilter;
// import fr.inria.zvtm.fits.filters.Smooth1Filter;
// import fr.inria.zvtm.fits.filters.Smooth2Filter;
// import fr.inria.zvtm.fits.filters.Smooth3Filter;
import fr.inria.zvtm.fits.filters.StaircaseFilter;
import fr.inria.zvtm.fits.filters.Stairs8Filter;
import fr.inria.zvtm.fits.filters.Stairs9Filter;
import fr.inria.zvtm.fits.filters.StandardFilter;
import fr.inria.zvtm.fits.filters.ColorGradient;
import fr.inria.zvtm.fits.simbad.AstroObject;
import fr.inria.zvtm.fits.simbad.SimbadCatQuery;

import jsky.image.ImageLookup;
import jsky.image.ImageProcessor;
import jsky.image.BasicImageReadableProcessor;
import jsky.image.ImageChangeEvent;
import jsky.image.ImageProcessor;
import jsky.coords.WorldCoords;

import javax.media.jai.Histogram;
//import javax.media.jai.ROI;
//import javax.media.jai.ROIShape;

// Options
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Example application loading FITS images using JSky.
 */

public class JSkyFitsExample{

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1280;
    static int VIEW_MAX_H = 800;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    String APP_TITLE = "JSkyFitsImage viewer";

    //shortcut
    VirtualSpaceManager vsm;

    VirtualSpace mSpace, bSpace, mnSpace;
    Camera mCamera, bCamera, mnCamera;

    JSkyFitsImage img;
    // double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;

    View mView;
    JSFEEventHandler eh;

    JSkyFitsMenu menu;

    static final String mSpaceName = "FITS Layer";
    static final String bSpaceName = "Data Layer";
    static final String mnSpaceName = "Menu Layer";

    static final int LAYER_FITS = 0;
    static final int LAYER_DATA = 1;
    static final int LAYER_MENU = 2;

    JSkyFitsExample(FitsOptions options){
        initGUI(options);
        try {
            loadFITSImage(options);
        }
        catch (IOException ex){
            System.err.println("Error while loading FITS image");
            ex.printStackTrace();
        }
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
        if (options.fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setBackgroundColor(Color.GRAY);
        menu = new JSkyFitsMenu(this);
        eh = new JSFEEventHandler(this);
        mView.setListener(eh, LAYER_FITS);
        mView.setListener(eh, LAYER_DATA);
        mView.setListener(menu, LAYER_MENU);
        mView.getCursor().getPicker().setListener(menu);
    }

    void windowLayout(){
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void loadFITSImage(FitsOptions options) throws IOException {
        if(options.url != null){
            img = new JSkyFitsImage(new URL(options.url) );
        }
        else if(options.file != null){
            String path = new File ( options.file ).getAbsolutePath ();
            if ( File.separatorChar != '/' )
            {
                path = path.replace ( File.separatorChar, '/' );
            }
            if ( !path.startsWith ( "/" ) )
            {
                path = "/" + path;
            }
            String retVal =  "file:" + path;
            img = new JSkyFitsImage(new URL(retVal));

        }
        if (img != null){
            img.setColorLookupTable("Standard");
            img.setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm.LINEAR);
            mSpace.addGlyph(img);
            menu.drawHistogram();
        }
    }

    void toggleMenu(){
        mnCamera.setEnabled(!mnCamera.isEnabled());
    }

    void querySimbad(Point2D.Double qp){
        // Point2D.Double vsCenter = selMan.getVsCenter();
        // Point2D.Double wcsCenter = selectedImage.pix2wcs(vsCenter.x, vsCenter.y);
        //compute radius in arcmin
        Point2D.Double vsExt = new Point2D.Double(qp.x+10, qp.y);
        Point2D.Double wcsExt = img.pix2wcs(vsExt.x, vsExt.y);
        final WorldCoords wc = new WorldCoords(qp.getX(), qp.getY());
        //WorldCoords wcDummy = new WorldCoords(wcsExt.getX(), wcsExt.getY());
        final int distArcMin = 1;//(int)(wc.dist(wcDummy)+1);//make sure the query radius is at least 1arcmin
        //perform catalog query
        System.err.println("Querying Simbad at " + wc + " with a radius of " + distArcMin + " arcminutes");
        // symbolSpace.removeAllGlyphs();
        new SwingWorker(){
            @Override public List<AstroObject> construct(){
                List<AstroObject> objs = null;
                try{
                    objs = SimbadCatQuery.makeSimbadCoordQuery(wc.getRaDeg(), wc.getDecDeg(), distArcMin);
                } catch(IOException ioe){
                    ioe.printStackTrace();
                } finally {
                    return objs;
                }
            }
            @Override public void finished(){
                List<AstroObject> objs = (List<AstroObject>)get();
                drawSymbols(objs);
            }
        }.start();
    }

    void drawSymbols(List<AstroObject> objs){
        for(AstroObject obj: objs){
            Point2D.Double p = img.wcs2vs(obj.getRa(), obj.getDec());
            VCross cr = new VCross(p.x, p.y, 100, 10, 10, Color.RED, Color.WHITE, .8f);
            cr.setStroke(AstroObject.AO_STROKE);
            VText lb = new VText(p.x+10, p.y+10, 101, Color.RED, obj.getIdentifier(), VText.TEXT_ANCHOR_START);
            mSpace.addGlyph(cr);
            mSpace.addGlyph(lb);
        }
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
        new JSkyFitsExample(options);
    }

}

class JSFEEventHandler implements ViewListener {

    public static final String T_FILTER = "Fltr";

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    JSkyFitsExample app;

    //private double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;
    //private RangeSelection rs;

    private int lastJPX;
    private int lastJPY;

    boolean panning = false;

    JSFEEventHandler(JSkyFitsExample app){
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
        panning = true;
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

        panning = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        // get coordinates in FITS image space
        Point2D.Double vp = v.getVCursor().getVSCoordinates(app.mCamera);
        // get WCS coordinates
        Point2D.Double wcsp = app.img.vs2wcs(vp.x, vp.y);
        // make query
        app.querySimbad(wcsp);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (app.mnCamera.isEnabled() && app.menu.cursorInside(jpx, jpy)){
            v.parent.setActiveLayer(app.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (panning){
            Camera c = app.mCamera;
            pan(c, lastJPX-jpx, jpy-lastJPY);
            lastJPX = jpx;
            lastJPY = jpy;
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
        */
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c = app.mCamera;
        double mvx = v.getVCursor().getVSXCoordinate();
        double mvy = v.getVCursor().getVSYCoordinate();
        zoom(c, mvx, mvy, wheelDirection);
    }

    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_M){
            app.toggleMenu();
        }
        // else if (code == KeyEvent.VK_MINUS){
        //     //app.scaleBounds[1] -= 100;
        //     //app.img.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        // }
        // else if (code == KeyEvent.VK_PLUS){
        //     //app.scaleBounds[1] += 100;
        //     //app.img.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        // }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    void pan(Camera c, int dx, int dy){
        synchronized(c){
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            c.move(a*dx, a*dy);
        }
    }

    void zoom(Camera c, double vx, double vy, short direction){
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (direction  == WHEEL_UP){
            // zooming out
            c.move(-((vx - c.vx) * WHEEL_ZOOMOUT_FACTOR / c.focal),
                   -((vy - c.vy) * WHEEL_ZOOMOUT_FACTOR / c.focal));
            c.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
        }
        else {
            // direction == WHEEL_DOWN, zooming in
            if (c.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= c.getZoomFloor()){
                // this test to prevent translation when camera is not actually zoming in
                c.move((vx - c.vx) * WHEEL_ZOOMIN_FACTOR / c.focal,
                       ((vy - c.vy) * WHEEL_ZOOMIN_FACTOR / c.focal));
            }
            c.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
        }
    }

}

class JSkyFitsMenu implements ViewListener, PickerListener {


    public static final int WIDTH_MENU = 200;

    private static final String SCALE_DEFAULT = "LINEAR SCALE";
    private static final String COLOR_DEFAULT = "Standard";

    public static final int HEIGHT_BTN = 15;
    public static final Color BORDER_COLOR_BTN = Color.BLACK;
    public static final Color TEXT_COLOR_BTN = Color.BLACK;
    public static final Color BACKGROUND_COLOR_BTN = Color.LIGHT_GRAY;
    public static final int FONT_SIZE_BTN = 10;
    public static final Font FONT_BTN = new Font("Bold", Font.PLAIN, FONT_SIZE_BTN);
    //public static int PIXELS_FONT = 16;//(int)(FONT_BTN.getSize()*1.333997172);
    public static int DISPLACE_TEXT_BTN = 4;

    //public static final Color LINE_COLOR = Color.RED;
    //public static final Stroke LINE_STROKE = new BasicStroke(1f);

    private static final int BORDER = 2;
    private static final int DISPLACE = 5;

    public static final int Z_BTN = 0;

    public static final String T_FILTER = "Fltr";
    public static final String T_SCALE = "Scl";
    public static final String T_SCROLL = "Scrll";
    public static final String T_SCROLL_BAR = "ScrllBr";

    public static final float WHEEL_FACTOR = 15.0f;

    public static final ColorGradient[] COLOR_GRADIENT = {new Aips0Filter(), new BackgrFilter(),
        new BlueFilter(), new BlulutFilter(), new ColorFilter(), new GreenFilter(),
        new HeatFilter(), new Idl11Filter(), new Idl12Filter(),new Idl14Filter(),new Idl15Filter(),
        new Idl2Filter(),new Idl4Filter(), new Idl5Filter(), new Idl6Filter(), new IsophotFilter(),new LightFilter(),
        new ManycolFilter(), new PastelFilter(), new RainbowFilter(), new Rainbow1Filter(),
        new Rainbow2Filter(),new Rainbow3Filter(), new Rainbow4Filter(), new RampFilter(),
        new RandomFilter(), new Random1Filter(), new Random2Filter(), new Random3Filter(),new Random4Filter(),
        /*new Random5Filter(), new Random6Filter(),*/ new RealFilter(), new RedFilter(), new SmoothFilter(),
        /*new Smooth1Filter(),new Smooth2Filter(),new Smooth3Filter(),*/ new StaircaseFilter(), new Stairs8Filter(), new Stairs9Filter(),
        new StandardFilter()};

    public static final String[] COLOR_NAME = {"Aips0", "Background", "Blue", "Blulut", "Color", "Green",
        "Heat", "Idl11", "Idl12","Idl14","Idl15",
        "Idl2","Idl4","Idl5","Idl6","Isophot","Light","Manycolor",
        "Pastel","Rainbow","Rainbow1","Rainbow2","Rainbow3","Rainbow4","Ramp","Random","Random1","Random2",
        "Random3","Random4",/*"Random5","Random6",*/"Real","Red","Smooth",/*"Smooth1","Smooth2","Smooth3",*/
        "Staircase", "Stairs8", "Stairs9", "Standard"};

    //public static final int[] SCALE_METHOD = {ImageLookup.LINEAR_SCALE, ImageLookup.LOG_SCALE, ImageLookup.HIST_EQ, ImageLookup.SQRT_SCALE};
    public static final JSkyFitsImage.ScaleAlgorithm[] SCALE_METHOD = {JSkyFitsImage.ScaleAlgorithm.LINEAR, JSkyFitsImage.ScaleAlgorithm.LOG, JSkyFitsImage.ScaleAlgorithm.HIST_EQ, JSkyFitsImage.ScaleAlgorithm.SQRT};
    public static final String[] SCALE_NAME = {"LINEAR SCALE", "LOGARITHMIC", "HISTOGRAM EQUALIZATION", "SQUARE ROOT"};

    public int BORDER_BOTTOM_FILTER;
    public int BORDER_TOP_FILTER;

    public int BORDER_TOP_HISTOGRAM;
    public int BORDER_BOTTOM_HISTOGRAM;
    public int BORDER_LEFT_HISTOGRAM;
    public int BORDER_RIGHT_HISTOGRAM;

    PRectangle scale_selected;
    PRectangle color_selected;

//  int CONST = 636;

    //public static final FitsImage.ScaleMethod[] SCALE_METHOD = {FitsImage.ScaleMethod.ASINH, FitsImage.ScaleMethod.HISTOGRAM_EQUALIZATION, FitsImage.ScaleMethod.LINEAR, FitsImage.ScaleMethod.LOG, FitsImage.ScaleMethod.SQUARE, FitsImage.ScaleMethod.SQUARE_ROOT};

    //private static Vector<Point2D.Double> ASINH;
    //private static double FACTOR_H_ASINH = (HEIGHT_BUTTON-2)/2/3;
    //private static double FACTOR_W_ASINH = (WIDTH_MENU - 2*BORDER)/4;

    JSkyFitsExample app;
    VirtualSpace mnSpace;

    JSkyFitsHistogram hist;

    int lastJPX;

    VRectangle shadow;
    double shadow_vx;
    double shadow_vy;
    //int shadow_width;

    boolean scroll = false;
    boolean press3_scroll = false;
    boolean press1 = false;

    Glyph lastGlyph = null;

    JSkyFitsMenu(JSkyFitsExample app){
        this.app = app;
        mnSpace = app.mnSpace;
        drawFiltersColor();
    }

    private void drawFiltersColor(){

        BORDER_TOP_FILTER =  0;//app.VIEW_H/2;

        int py = app.VIEW_H/2 - 2*HEIGHT_BTN - BORDER;

        for(int i = 0; i < COLOR_GRADIENT.length; i++){
            MultipleGradientPaint grad = Utils.makeGradient((RGBImageFilter)COLOR_GRADIENT[i]);
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, grad, BORDER_COLOR_BTN);
            filter.setType(T_FILTER);
            filter.setOwner(COLOR_NAME[i]);

            if(COLOR_DEFAULT.equals(COLOR_NAME[i])){
                color_selected = filter;
                color_selected.setWidth(color_selected.getWidth()+DISPLACE*2);
                color_selected.move(DISPLACE, 0);
            }

            mnSpace.addGlyph(filter);
            py -= (HEIGHT_BTN + BORDER);
        }

        for(int i = 0; i < SCALE_METHOD.length; i++){
            PRectangle filter = new PRectangle(-app.VIEW_W/2 + WIDTH_MENU/2, py, Z_BTN, WIDTH_MENU - BORDER, HEIGHT_BTN, BACKGROUND_COLOR_BTN, BORDER_COLOR_BTN);
            filter.setType(T_SCALE);
            filter.setOwner(SCALE_METHOD[i]);
            mnSpace.addGlyph(filter);

            VText scaleText = new VText(-app.VIEW_W/2 + WIDTH_MENU/2, py - DISPLACE_TEXT_BTN, Z_BTN, TEXT_COLOR_BTN, SCALE_NAME[i], VText.TEXT_ANCHOR_MIDDLE);
            scaleText.setFont(FONT_BTN);
            scaleText.setSensitivity(false);
            mnSpace.addGlyph(scaleText);
            filter.stick(scaleText);

            if(SCALE_DEFAULT.equals(SCALE_NAME[i])){
                scale_selected = filter;
                scale_selected.setWidth(scale_selected.getWidth()+DISPLACE*2);
                scale_selected.move(DISPLACE, 0);
            }

            //Glyph ln = drawMethod(SCALE_METHOD[i]);
            //ln.moveTo(-app.VIEW_W/2 + WIDTH_MENU/2 + BORDER , py);
            //mnSpace.addGlyph(ln);
            py -= (HEIGHT_BTN + BORDER);
        }
        BORDER_BOTTOM_FILTER = COLOR_GRADIENT.length * ( HEIGHT_BTN + BORDER*2 ) + SCALE_METHOD.length * ( HEIGHT_BTN + BORDER*2 ) + HEIGHT_BTN + BORDER;
    }

    public void drawHistogram(){

        if(app.img != null){
            hist = JSkyFitsHistogram.fromFitsImage(app.img);
            hist.moveTo(-app.VIEW_W/2 + (app.VIEW_W - hist.getWidth())/2 , -app.VIEW_H/2 + 50);

            BORDER_TOP_HISTOGRAM = (int)(app.VIEW_H - hist.getHeight() - 65 );
            BORDER_BOTTOM_HISTOGRAM = app.VIEW_H - 65;
            BORDER_LEFT_HISTOGRAM = (int)( (app.VIEW_W - hist.getWidth())/2 - JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;
            BORDER_RIGHT_HISTOGRAM = (int)( (app.VIEW_W + hist.getWidth())/2 + JSkyFitsHistogram.DEFAULT_BIN_WIDTH) ;

            VRectangle board = new VRectangle(hist.vx+hist.getWidth()/2, hist.vy+hist.getHeight()/2, Z_BTN, BORDER_RIGHT_HISTOGRAM-BORDER_LEFT_HISTOGRAM, BORDER_BOTTOM_HISTOGRAM-BORDER_TOP_HISTOGRAM+10, Color.WHITE, Color.WHITE, 0.8f);

            mnSpace.addGlyph(board);
            mnSpace.addGlyph(hist);
        }
    }

    private void adaptCutLevels(double left, double right){
        double[] originCutLevels = app.img.getOriginCutLevels();
        double min = originCutLevels[0];
        double max = originCutLevels[1];

        left = (left < 0) ? 0 : left;
        right = (right > 1) ? 1 : right;

        app.img.setCutLevels(min + left*(max - min), min + right*(max - min));

    }

    private void drawShadow(double x1, double x2){
        if(shadow != null){
            double w = Math.abs(x2-x1);
            double x = (x1 > x2) ? x1 - w/2 : x1 + w/2;
            shadow.moveTo(x, shadow.vy);
            shadow.setWidth(w);
        }
    }

    boolean cursorInside(int jpx, int jpy){
        return (jpx < WIDTH_MENU && jpy < BORDER_BOTTOM_FILTER && jpy > BORDER_TOP_FILTER) ||
               (jpy > BORDER_TOP_HISTOGRAM && jpy < BORDER_BOTTOM_HISTOGRAM && jpx > BORDER_LEFT_HISTOGRAM && jpx < BORDER_RIGHT_HISTOGRAM);
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTOM_HISTOGRAM){
            lastJPX = jpx;
            if(shadow != null)
                app.mnSpace.removeGlyph(shadow);

            press1 = true;

            //Point2D.Double point = app.viewToSpace(app.mnCamera, jpx, jpy);

            shadow = new VRectangle( ((lastJPX < jpx)? lastJPX : jpx) + Math.abs(lastJPX - jpx)/2 - app.VIEW_W/2, -app.VIEW_H/2 + 100, Z_BTN, Math.abs(lastJPX - jpx), hist.getHeight() + 10, Color.WHITE, Color.BLACK, .2f);
            shadow.setType(T_SCROLL);
            app.mnSpace.addGlyph(shadow);
            shadow_vx = shadow.vx;
            shadow_vy = shadow.vy;
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.SELECTED_FILL_COLOR);
            }
            */
        }
    }

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        if(jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTOM_HISTOGRAM){

            double left = ( ((lastJPX < jpx)? lastJPX : jpx) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();
            double right = ( ((lastJPX < jpx)? jpx : lastJPX) - app.VIEW_W/2 + hist.getWidth()/2) / hist.getWidth();

            adaptCutLevels(left, right);

            lastJPX = jpx;
            press1 = false;
        }

    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if(clickNumber == 2){
            adaptCutLevels(0, 1);
            if(shadow != null){
                app.mnSpace.removeGlyph(shadow);
                shadow = null;
            }
            /*
            VRectangle[] bars = hist.getBars();
            for(VRectangle b : bars){
                b.setColor(FitsHistogram.DEFAULT_FILL_COLOR);
            }
            */
        }
    }


    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if(scroll){
            press3_scroll = true;
            lastJPX = jpx;
        }
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if(shadow != null && press3_scroll){
            press3_scroll = false;
            lastJPX = 0;

            /*
            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
            adaptCutLevels(left, right);
            */
        }

    }
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (!cursorInside(jpx, jpy)){
            v.parent.setActiveLayer(app.LAYER_FITS);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if(press3_scroll){
            if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2 + (jpx - lastJPX)) < app.menu.BORDER_RIGHT_HISTOGRAM){

                shadow.move(jpx - lastJPX,0);
                lastJPX = jpx;
                double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                adaptCutLevels(left, right);

            } else if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2) > app.menu.BORDER_LEFT_HISTOGRAM &&
                (shadow.vx+shadow.vw/2 + app.VIEW_W/2) < app.menu.BORDER_RIGHT_HISTOGRAM){
                if( (jpx - lastJPX) > 0 ){
                    shadow.move( app.menu.BORDER_RIGHT_HISTOGRAM - (shadow.vx+shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;
                    double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                    double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                    adaptCutLevels(left, right);

                } else if( (jpx - lastJPX) < 0 ){
                    shadow.move( app.menu.BORDER_LEFT_HISTOGRAM - (shadow.vx-shadow.vw/2 + app.VIEW_W/2) ,0);
                    lastJPX = jpx;
                    double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
                    double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
                    adaptCutLevels(left, right);
                }
            }
        }
        if(press1){
            if(shadow != null){
                if(jpx > BORDER_RIGHT_HISTOGRAM) jpx = BORDER_RIGHT_HISTOGRAM;
                if(jpx < BORDER_LEFT_HISTOGRAM) jpx = BORDER_LEFT_HISTOGRAM;
                Point2D.Double point = app.mView.fromPanelToVSCoordinates(jpx, jpy, app.mnCamera, new Point2D.Double());
                drawShadow(shadow_vx, point.getX());
                //scroll = false;
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){

        if(shadow != null){

            double mvx = v.getVCursor().getVSXCoordinate();
            double mvy = v.getVCursor().getVSYCoordinate();

            if(wheelDirection == WHEEL_UP){
                if((shadow.vx+shadow.vw/2 + app.VIEW_W/2 + WHEEL_FACTOR) < BORDER_RIGHT_HISTOGRAM ) shadow.move( WHEEL_FACTOR, 0 );
                else if( (shadow.vx+shadow.vw/2 + app.VIEW_W/2) < BORDER_RIGHT_HISTOGRAM){
                    shadow.move( BORDER_RIGHT_HISTOGRAM - (shadow.vx+shadow.vw/2 + app.VIEW_W/2), 0 );
                }
            } else {
                if((shadow.vx-shadow.vw/2 + app.VIEW_W/2 - WHEEL_FACTOR) > BORDER_LEFT_HISTOGRAM ) shadow.move( -WHEEL_FACTOR, 0 );
                else if( (shadow.vx-shadow.vw/2 + app.VIEW_W/2) > BORDER_LEFT_HISTOGRAM){
                    shadow.move( BORDER_LEFT_HISTOGRAM - (shadow.vx-shadow.vw/2 + app.VIEW_W/2) , 0 );
                }
            }

            double left = ( shadow.vx + hist.getWidth()/2 - shadow.vw/2) / (hist.getWidth());
            double right = ( shadow.vx + hist.getWidth()/2 + shadow.vw/2 ) / (hist.getWidth());
            adaptCutLevels(left, right);
        }

    }

    public void enterGlyph(Glyph g){
        //System.out.println("enter: " + g.getType());
        if(g.getType().equals(T_FILTER)){

            if(color_selected != ((PRectangle)g)){
                app.img.setColorLookupTable((String)g.getOwner());
                if(color_selected != null){
                    color_selected.setWidth(color_selected.getWidth()-DISPLACE*2);
                    color_selected.move(-DISPLACE,0);
                }
                ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+DISPLACE*2);
                g.move(DISPLACE, 0);
                color_selected = (PRectangle)g;
                //redrawHistogram();
            }
        } else if(g.getType().equals(T_SCALE)){
            lastGlyph = g;
            if(scale_selected != (PRectangle)g){
                app.img.setScaleAlgorithm((JSkyFitsImage.ScaleAlgorithm)g.getOwner());
                if(scale_selected != null){
                    scale_selected.setWidth(scale_selected.getWidth()-DISPLACE*2);
                    scale_selected.move(-DISPLACE,0);
                }
                ((PRectangle)g).setWidth(((PRectangle)g).getWidth()+DISPLACE*2);
                g.move(DISPLACE,0);
                scale_selected = (PRectangle)g;
                //redrawHistogram();
            }
        } else if(g.getType().equals(T_SCROLL) && !scroll){
            scroll = true;
            //v.parent.setCursorIcon(Cursor.);
        }
        lastGlyph = g;
    }

    public void exitGlyph(Glyph g){
        //System.out.println("exit: " + g.getType());
        if(g.getType().equals(T_SCROLL) && scroll){
            scroll = false;
            //v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_M){
            app.toggleMenu();
        }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}

class JSkyFitsHistogram extends Composite {

    public static final double DEFAULT_BIN_WIDTH = 6;
    public static final Color DEFAULT_FILL_COLOR = new Color(0,0,255,127);
    public static final Color SELECTED_FILL_COLOR = new Color(0,0,255,210);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0,0,255,180);

    public static final int HISTOGRAM_SIZE = 128;

    double width;
    double height = 100;
    VRectangle[] bars = new VRectangle[128];


    public JSkyFitsHistogram(int[] data, int min, int max, Color fillColor){

        width = DEFAULT_BIN_WIDTH*data.length;
        VRectangle backgrown = new VRectangle(width/2, height/2, JSkyFitsMenu.Z_BTN, width, height, Color.GRAY, Color.BLACK, 0.2f);
        addChild(backgrown);

        int i = 0;
        //int val;
        for(int j = 0; j< data.length; j++){
            //val = data[j];
            double h = (Math.sqrt(data[j]) * height) / Math.sqrt(max - min);
            int hh = (int)(h);
            hh = ( hh % 2 == 0) ? hh : hh + 1;
            VRectangle bar = new VRectangle(i+DEFAULT_BIN_WIDTH/2, (int)(hh/2), JSkyFitsMenu.Z_BTN, DEFAULT_BIN_WIDTH, (int)(hh), fillColor);
            bar.setBorderColor(DEFAULT_BORDER_COLOR);
            addChild(bar);
            bars[j] = bar;
            i += DEFAULT_BIN_WIDTH;
        }

    }

    public VRectangle[] getBars(){
        return bars;
    }

    public JSkyFitsHistogram(int[] data, int min, int max){
        this(data, min, max, DEFAULT_FILL_COLOR);
    }

    //Scale method linear
    public static JSkyFitsHistogram fromFitsImage(JSkyFitsImage image, Color fillColor){
        /*
        Histogram hist = image.getHistogram( 2048 );//HISTOGRAM_SIZE);
        int[] data = new int[HISTOGRAM_SIZE];

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        System.out.println("Num Bands: " + hist.getNumBands());
        System.out.println("Num Bins: " + hist.getNumBins().length);
        for(int i = 0; i < hist.getNumBins().length; i++){
            System.out.println(hist.getNumBins()[i]);
        }
        System.out.println("Num low values: " + hist.getLowValue().length);
        for(int i = 0; i < hist.getLowValue().length; i++){
            System.out.println(hist.getLowValue()[i]);
        }
        System.out.println("Num high values: " + hist.getLowValue().length);
        for(int i = 0; i < hist.getHighValue().length; i++){
            System.out.println(hist.getHighValue()[i]);
        }
        */

    /**
     *
     * Plot a histogram for the image
     */

        double[] cutlevels = image.getCutLevels();

        double lowCut = cutlevels[0];
        double highCut = cutlevels[1];

        int numValues = HISTOGRAM_SIZE;
        int dataType = image.getDataType();
        boolean isFloatingPoint = (dataType == DataBuffer.TYPE_FLOAT || dataType == DataBuffer.TYPE_DOUBLE);
        double n = highCut - lowCut;

        if (n < numValues && !isFloatingPoint) {
            numValues = (int) n;
        }

        if (numValues <= 0) {
            System.out.println("return numValues: " + numValues);
            //chart.getXYPlot().setDataset(new SimpleDataset());
            //return;
        }

        double[] xValues = new double[numValues];
        int[] yValues = new int[numValues];
        double m = lowCut;
        double factor = n / numValues;

        // the X values are the pixel values
        // the Y values are the number of pixels in a given range
        for (int i = 0; i < numValues; i++, m += factor) {
            xValues[i] = m;
            yValues[i] = 0;
        }
        if (factor >= 0.0) {

            Histogram histogram = image.getHistogram(numValues);
            yValues = histogram.getBins(0);
            //chart.getXYPlot().setDataset(new SimpleDataset(xValues, yValues));
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<yValues.length; ++j){
            if (yValues[j] < min){
                min = yValues[j];
            }
            if(yValues[j] > max){
                max = yValues[j];
            }
        }

        /*
        for(int i=0; i<HISTOGRAM_SIZE; ++i){
            //data[i/(hist.getCounts().length / data.length)] += hist.getCounts()[i];
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int j=0; j<data.length; ++j){
            if (data[j] < min){
                min = data[j];
            }
            if(data[j] > max){
                max = data[j];
            }
        }
        */
        return new JSkyFitsHistogram(yValues, min, max, fillColor);
    }


    public double getHeight(){
        return height;
    }
    public double getWidth(){
        return width;
    }

    public static JSkyFitsHistogram fromFitsImage(JSkyFitsImage image){
        return fromFitsImage(image, DEFAULT_FILL_COLOR);
    }

}
