/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
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

    public static final int WIDTH_MENU = 200;

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
    VirtualSpace mSpace, bSpace, mnSpace;
    Camera mCamera, bCamera, mnCamera;

    PanZoomEventHandler eh;
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

        hi.setScaleMethod(FitsImage.ScaleMethod.LINEAR);//ASINH);//.LINEAR);
        //hi.setColorFilter(FitsImage.ColorFilter.RAINBOW);
        mSpace.addGlyph(hi, false); 

        menu.drawHistogram();


        scaleBounds = ZScale.computeScale(hi.getUnderlyingImage());
        //hi.rescale(scaleBounds[0], scaleBounds[1], 1);
        System.out.println(scaleBounds[0] + ", " + scaleBounds[1]);

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
        
        eh = new PanZoomEventHandler(this);
        //mCamera.addListener(eh);
        mView.setListener(eh, 0);
        mView.setListener(eh, 1);
        mView.setListener(menu, 2);

        mView.setBackgroundColor(BACKGROUND_COLOR);

        //mView.setActiveLayer(2);

    }

    public void setColorFilter(ImageFilter filter){

        hi.setColorFilter(filter);
    }

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



