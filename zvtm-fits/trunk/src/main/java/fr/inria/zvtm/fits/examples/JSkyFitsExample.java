/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */ 
package fr.inria.zvtm.fits.examples;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.Utils;

import fr.inria.zvtm.glyphs.VRectangle;

import fr.inria.zvtm.glyphs.PRectangle;

import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Point2D;

import java.io.IOException;
import java.util.Vector;
import java.io.File;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.net.MalformedURLException;
import java.net.URL;

// Options
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.awt.Toolkit;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;


/**
 * Sample FITS application.
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
    double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;

    private View mView;
    private PanZoomEventHandler eh;

    public JSkyFitsMenu menu;

    static final String mSpaceName = "FITS Layer";
    static final String bSpaceName = "Data Layer";
	static final String mnSpaceName = "Menu Layer";

	static final int LAYER_FITS = 0;
	static final int LAYER_DATA = 1;
	static final int LAYER_MENU = 2;


	JSkyFitsExample(FitsOptions options) throws IOException {
		
		initGUI(options);

		if(options.url != null){
			img = new JSkyFitsImage(new URL(options.url) );

        } else if(options.file != null){
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

        } else {
            System.err.println("usage: JSkyFitsExample -file image_File or -url image_URL");
            System.exit(0);
            return;
        }

        /* DEFAULT */
        img.setColorLookupTable("Standard");
        img.setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm.LINEAR);

        mSpace.addGlyph(img);

        menu.drawHistogram();


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


        mView.setBackgroundColor(Color.GRAY);

        menu = new JSkyFitsMenu(this);

        eh = new PanZoomEventHandler(this);
        mView.setListener(eh, LAYER_FITS);
        mView.setListener(eh, LAYER_DATA);
        mView.setListener(menu, LAYER_MENU);

        
        
    }

    public Point2D.Double viewToSpace(Camera cam, int jpx, int jpy){
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

	public JSkyFitsMenu getMenu(){
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

