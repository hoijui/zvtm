/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FitsViewer.java 5249 2014-12-11 19:33:30Z fdelcampo $
 */

package fr.inria.zuist.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.util.Vector;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;

//import fr.inria.zvtm.engine.LongPoint;
//import fr.inria.zvtm.engine.Utilities;
import java.awt.geom.Point2D;
import fr.inria.zvtm.engine.Utils;

import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.JSkyFitsImage;

//import fr.inria.zvtm.glyphs.PieMenu;
//import fr.inria.zvtm.glyphs.PieMenuFactory;
import fr.inria.zvtm.widgets.PieMenu;
import fr.inria.zvtm.widgets.PieMenuFactory;

import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.RegionListener;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.JSkyFitsImageDescription;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

// filter
import java.awt.image.ImageFilter;

import cl.inria.massda.SmartiesManager;
import cl.inria.massda.TuioEventHandler;

import edu.jhu.pha.sdss.fits.FITSImage; 
import jsky.coords.WCSTransform;
import nom.tam.fits.FitsException;
import fr.inria.zvtm.fits.NomWcsKeywordProvider;

import cl.inria.massda.PythonWCS;
import cl.inria.massda.SmartiesManager.MyCursor;


/**
 * @author Emmanuel Pietriga, Fernando del Campo
 */

public class JSkyFitsViewer extends FitsViewer implements Java2DPainter, RegionListener, LevelListener {
    
    //@Override
    JSkyFitsViewerEventHandler eh;

    //@Override
    String cfilter = "Standard";
    //@Override
    JSkyFitsImage.ScaleAlgorithm scaleMethod = JSkyFitsImage.ScaleAlgorithm.LINEAR;

    //@Override
    public JSkyFitsMenu menu;
    
    //@Override
    public JSkyFitsImageDescription fitsImageDescRef;
    
    public JSkyFitsViewer(Options options){
        super(options);
        /*
		ovm = new FitsOverlayManager(this);
		//initGUI(fullscreen, opengl, antialiased);
		initGUI(options);
        VirtualSpace[]  sceneSpaces = {mSpace, mSpaceKs, mSpaceH, mSpaceJ};
        Camera[] sceneCameras = {mCamera, mCameraKs, mCameraH, mCameraJ};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setRegionListener(this);
        sm.setLevelListener(this);
		previousLocations = new Vector();
		ovm.initConsole();
        if (options.xmlSceneFile != null){
            sm.enableRegionUpdater(false);
            File xmlSceneFile = new File(options.xmlSceneFile);
            System.out.println("load scene: " + options.xmlSceneFile);
			loadScene(xmlSceneFile);
			EndAction ea  = new EndAction(){
                   public void execute(Object subject, Animation.Dimension dimension){
                       sm.setUpdateLevel(true);
                       sm.enableRegionUpdater(true);
                       
                   }
               };
			getGlobalView(ea);
		}
		ovm.toggleConsole();
        //System.out.println("setActiveLayer(LAYER_SCENE)");
        //mView.setActiveLayer(LAYER_SCENE);
        

		//menu.drawHistogram();
        smartiesMngr = new SmartiesManager(this);
        teh = new TuioEventHandler(this);

        reference = options.reference;

        pythonWCS = new PythonWCS();
        
        //pythonWCS.sendCoordinate(0,0, null);
        */

    }
    //void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
    @Override
    void initGUI(Options options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("JSkyFitsViewer");
        mSpace = vsm.addVirtualSpace(mSpaceName);

        mSpaceKs = vsm.addVirtualSpace(mSpaceKsName);
        mSpaceH = vsm.addVirtualSpace(mSpaceHName);
        mSpaceJ = vsm.addVirtualSpace(mSpaceJName);
        cursorSpace = vsm.addVirtualSpace(cursorSpaceName);

        VirtualSpace mnSpace = vsm.addVirtualSpace(mnSpaceName);

        mCamera = mSpace.addCamera();

        mCameraKs = mSpaceKs.addCamera();
        mCameraH = mSpaceH.addCamera();
        mCameraJ = mSpaceJ.addCamera();
        cursorCamera = cursorSpace.addCamera();

        mCamera.stick(mCameraKs);
        mCamera.stick(mCameraH);
        mCamera.stick(mCameraJ);
        //mCamera.stick(cursorCamera);

		mnSpace.addCamera().setAltitude(10);

        ovSpace = vsm.addVirtualSpace(ovSpaceName);
		ovSpace.addCamera();

		menuSpace = vsm.addVirtualSpace(menuSpaceName);
		menuCamera = menuSpace.addCamera();

        Vector cameras = new Vector();

        cameras.add(mCamera);
        cameras.add(mCameraKs);
        cameras.add(mCameraH);
        cameras.add(mCameraJ);
		cameras.add(vsm.getVirtualSpace(mnSpaceName).getCamera(0));
		cameras.add(vsm.getVirtualSpace(ovSpaceName).getCamera(0));
		cameras.add(menuCamera);
        cameras.add(cursorCamera);

        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, initMenu());
        Vector<Camera> sceneCam = new Vector<Camera>();

        sceneCam.add(mCamera);
        sceneCam.add(mCameraKs);
        sceneCam.add(mCameraH);
        sceneCam.add(mCameraJ);
        sceneCam.add(cursorCamera);

        ClusterGeometry clGeom = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
		clusteredView = new ClusteredView(clGeom, options.numRows-1, options.numCols, options.numRows, sceneCam);
        clusteredView.setBackgroundColor(Color.GRAY);
        vsm.addClusteredView(clusteredView);
        if (options.fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();

		//gp = new VWGlassPane(this);
		//((JFrame)mView.getFrame()).setGlassPane(gp);
        //gp.setValue(0);
        //if(options.xmlSceneFile != null) gp.setVisible(true);
        //else gp.setVisible(false);

        eh = new JSkyFitsViewerEventHandler(this);

        menu = new JSkyFitsMenu(this);
        
        /*
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(eh, 1);
        mView.setEventHandler(ovm, 2);
        */
        mView.setListener(eh, LAYER_SCENE);
        mView.setListener(eh, LAYER_PIEMENU);
        mView.setListener(ovm, LAYER_OVERLAY);
        mView.setListener(menu, LAYER_MENU);
        
		mCamera.addListener(eh);

        //mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.setAntialiasing(!options.noaa);//antialiased);
		mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
		//mView.getPanel().addComponentListener(eh);
		mView.getPanel().getComponent().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
		
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

    int ordinal = 0;
	@Override
    void toggleColorFilter(){
        String next;
        if(ordinal == (JSkyFitsImage.COLORFILTER.length - 1)){
            next = JSkyFitsImage.COLORFILTER[0];
            ordinal = 0;
        } else {
            next = JSkyFitsImage.COLORFILTER[ordinal++];
        }
        cfilter = next;
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).setColorLookupTable(next);
            }
        } 
    }

    @Override
    void toggleTransferFun(){
        JSkyFitsImage.ScaleAlgorithm next = 
            scaleMethod.ordinal() == (JSkyFitsImage.ScaleAlgorithm.values().length - 1) ? JSkyFitsImage.ScaleAlgorithm.values()[0] : JSkyFitsImage.ScaleAlgorithm.values()[scaleMethod.ordinal() + 1];
        scaleMethod = next;
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).setScaleAlgorithm(next);
            }
        } 
    }

    //@Override
    //public void setColorFilter(JSkyFitsImage.ColorFilter filter){
    public void setColorFilter(String filter){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).setColorLookupTable(filter);
            }
        } 
    }

    //@Override
    public void setScaleMethod(JSkyFitsImage.ScaleAlgorithm method){
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).setScaleAlgorithm(method);
            }
        } 
    }

    //@Override
    public void rescale(double min, double max){
    	//System.out.println("rescale");
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).rescale(min, max);
            }
        }
    }

    @Override
    public void rescaleGlobal(boolean global){
        //System.out.println("rescaleGlobal("+global+")");

        boolean globalData = false;
        if(!globalData)
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                if(((JSkyFitsImageDescription)desc).isCreatedWithGlobalData()){
                    globalData = true;
                    //break;
                }
                double[] localScaleParams = ((JSkyFitsImageDescription)desc).getLocalScaleParams();
                if(localScaleParams[0] < globalScaleParams[0]) globalScaleParams[0] = localScaleParams[0];
                if(localScaleParams[1] > globalScaleParams[1]) globalScaleParams[1] = localScaleParams[1];
            }
        }
        if(globalData)
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){ 
                ((JSkyFitsImageDescription)desc).setRescaleGlobal(globalScaleParams[0], globalScaleParams[1]);
                ((JSkyFitsImageDescription)desc).setRescaleGlobal(global);
                if(global) ((JSkyFitsImageDescription)desc).rescaleGlobal();
                else ((JSkyFitsImageDescription)desc).rescaleLocal();
            }
        }
        //System.out.println(" min: " + globalScaleParams[0] + " max: " + globalScaleParams[1] );
        /*
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
                if(global) ((FitsImageDescription)desc).rescaleGlobal();
                else ((FitsImageDescription)desc).rescaleLocal();
            }
        }
        */
    }

    public double[] getGlobalScale(){

        if (fitsImageDescRef != null)
            return fitsImageDescRef.getGlobalScaleParams();
        else
            return new double[2];
    }

    @Override
    public void hideLayer(int layerIndex){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                if( ((JSkyFitsImageDescription)desc).getLayerIndex() == layerIndex){
                    ((JSkyFitsImageDescription)desc).setVisible(false);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.hide(g);
        }*/
    }

    @Override
    public void showLayer(int layerIndex, float alpha){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                if( ((JSkyFitsImageDescription)desc).getLayerIndex() == layerIndex){
                    ((JSkyFitsImageDescription)desc).setVisible(true);
                    ((JSkyFitsImageDescription)desc).setTranslucencyValue(alpha);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.show(g);
            g.setTranslucencyValue(alpha);
        }
        */
    }

    @Override
    public void orientTo(double angle){
        System.out.println("angle: "+angle);
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof JSkyFitsImageDescription){
                ((JSkyFitsImageDescription)desc).orientTo(angle);
            }
        }
    }

    @Override
    void loadScene(File xmlSceneFile){
        System.out.print("this instanceof FitsViewer: ");
        System.out.println( this instanceof FitsViewer );
        try {
            ovm.sayInConsole("Loading "+xmlSceneFile.getCanonicalPath()+"\n");
            System.out.println("Loading "+xmlSceneFile.getCanonicalPath()+"\n");
            mView.setTitle(mViewName + " - " + xmlSceneFile.getCanonicalPath());
            System.out.println(mViewName + " - " + xmlSceneFile.getCanonicalPath());    
        }
        catch (IOException ex){}
        //gp.setValue(0);
        //gp.setVisible(true);
        SCENE_FILE = xmlSceneFile;
        SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        System.out.println("dir: "+SCENE_FILE_DIR + " - file: " + SCENE_FILE);
        System.out.println("loadScene...");
        //sm.loadScene(SceneManager.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
        sm.loadScene(SceneManager.parseXML(SCENE_FILE), SCENE_FILE_DIR, true);
        HashMap sceneAttributes = sm.getSceneAttributes();
        if (sceneAttributes.containsKey(SceneManager._background)){
            mView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
            clusteredView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
        }


        MAX_NB_REQUESTS = sm.getObjectCount() / 100;
        //gp.setVisible(false);
        //gp.setLabel(VWGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);

        loadFitsReference();
    }

    @Override
    public void coordinateWCS(Point2D.Double xy, MyCursor mc){

        try {

            System.out.println("coordinateWCS(" + xy.getX() + ", " + xy.getY() + ")");
            double[] coord = windowToViewCoordinateFromSmarties(xy.getX(), xy.getY());
            System.out.println("windowToViewCoordinateFromSmarties: (" + coord[0] + ", " + coord[1] + ")");
            System.out.println("windowToViewCoordinateFromSmarties Ref: (" + fitsImageDescRef.getX() + ", " + fitsImageDescRef.getY() + ")");

            double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

            System.out.println("coordinateWCS("+coord[0]+", "+coord[1]+")");
            System.out.println("reference("+fitsImageDescRef.getX()+", "+fitsImageDescRef.getY()+")");
            System.out.println("a: " + a);
            System.out.println("factor: " + fitsImageDescRef.getFactor());

            
            System.out.println( "(" + ((coord[0]-fitsImageDescRef.getX()+fitsImageDescRef.getWidthWithFactor()/2)/a  ) + ", " + (( coord[1]-fitsImageDescRef.getY()+fitsImageDescRef.getHeightWithFactor()/2)/a ) + ")");


            System.out.println( "width: " +fitsImageDescRef.getWidth() + " height:" +fitsImageDescRef.getHeight());

            System.out.println( "width/factor: " +(fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor() ) + " height/a:" + (fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()) ) ;

           

            double x = (coord[0] - fitsImageDescRef.getX())/fitsImageDescRef.getFactor() + fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor()/2 ;
            double y = (coord[1] - fitsImageDescRef.getY())/fitsImageDescRef.getFactor() + fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()/2 ;


            System.out.println( "(" + x + ", " + y + ")");

            System.out.println("size: " + fitsImageDescRef.getWidthWithFactor() + ", " +  fitsImageDescRef.getHeightWithFactor());

            pythonWCS.changeCoordinateSystem(galacticalSystem);
            pythonWCS.sendCoordinate(x, y, mc);

        } catch (NullPointerException e){
            e.printStackTrace(System.out);
        }

        
    }

    @Override
    public void loadFitsReference(){
        if(reference == null){
            for(ObjectDescription desc: sm.getObjectDescriptions()){
                if(desc instanceof JSkyFitsImageDescription){
                    if( ((JSkyFitsImageDescription)desc).isReference()){
                        System.out.println("Reference");
                        System.out.println(desc);

                        fitsImageDescRef = (JSkyFitsImageDescription)desc;
                        /*
                        try{
                            fitsImageDescRef = (FitsImageDescription)desc;
                            //FITSImage fitsImage = new FITSImage( fitsImageDescRef.getSrc() );
                            //wcsKeyProviderRef = new NomWcsKeywordProvider( fitsImage.getFits().getHDU(0).getHeader());
                            //wcsTransformRef = new WCSTransform(wcsKeyProviderRef);

                        } catch(IOException ioe){
                            wcsTransformRef = null;
                        } catch (FitsException fe){
                            wcsTransformRef = null;
                        } catch(FITSImage.NoImageDataFoundException nidfe){
                            wcsTransformRef = null;
                        } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                            wcsTransformRef = null;
                        }
                        */

                        break;
                    }
                }
            }
        } else {

            System.out.println("ref: " + reference);

            fitsImageDescRef = null;
            /*
            try{
                
                
                FITSImage fitsImage = new FITSImage( reference );
                wcsKeyProviderRef = new NomWcsKeywordProvider( fitsImage.getFits().getHDU(0).getHeader());
                wcsTransformRef = new WCSTransform(wcsKeyProviderRef);

            } catch(IOException ioe){
                wcsTransformRef = null;
            } catch (FitsException fe){
                wcsTransformRef = null;
            } catch(FITSImage.NoImageDataFoundException nidfe){
                wcsTransformRef = null;
            } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                wcsTransformRef = null;
            }
            */

        }
        
        
        System.out.println("loaded JSky Fits Reference");
    }


    public static void main(String[] args){

    	Options options = new Options();
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
        new JSkyFitsViewer(options);

    }

    
}
/*
class VWGlassPane extends JComponent implements ProgressListener {
    
    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);    
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    static final String EMPTY_STRING = "";
    String msg = EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;
    
    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;
    
    FitsViewer application;
    
    VWGlassPane(FitsViewer app){
        super();
        this.application = app;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }
    
    public void setValue(int c){
        completion = c;
        prX = application.panelWidth/2-BAR_WIDTH/2;
        prY = application.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
    public void setLabel(String m){
        msg = m;
        msgX = application.panelWidth/2-BAR_WIDTH/2;
        msgY = application.panelHeight/2-BAR_HEIGHT/2 - 10;
        repaint(msgX, msgY-50, 400, 70);
    }
    
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (msg != EMPTY_STRING){
            g2.setColor(MSG_COLOR);
            g2.setFont(ConfigManager.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class ConfigManager {

	static Color PIEMENU_FILL_COLOR = Color.BLACK;
	static Color PIEMENU_BORDER_COLOR = Color.WHITE;
	static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
	
	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    static final Font PIEMENU_FONT = DEFAULT_FONT;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

}
*/
