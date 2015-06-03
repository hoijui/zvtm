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


import java.util.List;
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
import fr.inria.zvtm.glyphs.VCross;
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
import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.ProgressListener;
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
//import cl.inria.massda.SmartiesManager.MyCursor;

import java.util.Observer;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import fr.inria.zvtm.fits.simbad.SimbadCatQuery;
import fr.inria.zvtm.fits.simbad.AstroObject;

import jsky.coords.WorldCoords;

import java.awt.BasicStroke;

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

    private DrawSymbol draw;
    private Query query;

    
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
        teh = new TuioEventHandler(this);

        reference = options.reference;
        */
        smartiesMngr = new SmartiesManager(this);

        // create a picker that will only consider regions visible at ZUIST levels 3 through 5 (any of these levels or all of them)
        System.out.println("levelCount: " + sm.getLevelCount());
        rPicker = sm.createRegionPicker(0, sm.getLevelCount());
        rPicker.setListener(new RegionPickerListener(this));

        draw = new DrawSymbol();
        query = new Query();

    }
    //void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
    @Override
    void initGUI(Options options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("JSkyFitsViewer");

        mSpaceKs = vsm.addVirtualSpace(mSpaceKsName);
        mSpaceH = vsm.addVirtualSpace(mSpaceHName);
        mSpaceJ = vsm.addVirtualSpace(mSpaceJName);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        pMnSpace = vsm.addVirtualSpace(pMnSpaceName);
        mnSpace = vsm.addVirtualSpace(mnSpaceName);
        cursorSpace = vsm.addVirtualSpace(cursorSpaceName);
        ovSpace = vsm.addVirtualSpace(ovSpaceName);
        

        mCameraKs = mSpaceKs.addCamera();
        mCameraH = mSpaceH.addCamera();
        mCameraJ = mSpaceJ.addCamera();
        mCamera = mSpace.addCamera();

        mCamera.stick(mCameraKs);
        mCamera.stick(mCameraH);
        mCamera.stick(mCameraJ);
        //mCamera.stick(cursorCamera);

        pMnSpace.addCamera().setAltitude(10);
        mnCamera = mnSpace.addCamera();
        cursorCamera = cursorSpace.addCamera();
        ovSpace.addCamera();

        Vector cameras = new Vector();

        cameras.add(mCameraKs);
        cameras.add(mCameraH);
        cameras.add(mCameraJ);
        cameras.add(mCamera);
        cameras.add(vsm.getVirtualSpace(pMnSpaceName).getCamera(0));
        cameras.add(mnCamera);
        cameras.add(cursorCamera);
        cameras.add(vsm.getVirtualSpace(ovSpaceName).getCamera(0));

        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, initMenu());
        Vector<Camera> sceneCam = new Vector<Camera>();

        sceneCam.add(mCameraKs);
        sceneCam.add(mCameraH);
        sceneCam.add(mCameraJ);
        sceneCam.add(mCamera);
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

        mView.setListener(eh, LAYER_SCENE_KS);
        mView.setListener(eh, LAYER_SCENE_H);
        mView.setListener(eh, LAYER_SCENE_J);
        mView.setListener(eh, LAYER_SCENE);
        mView.setListener(eh, LAYER_PIEMENU);
        mView.setListener(menu, LAYER_MENU);
        mView.setListener(eh, LAYER_CURSOR);
        mView.setListener(ovm, LAYER_OVERLAY);

        mView.getCursor().getPicker().setListener(menu);
        
		//mCamera.addListener(eh);

        //mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.setAntialiasing(!options.noaa);//antialiased);
		mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
		//mView.getPanel().addComponentListener(eh);
		//mView.getPanel().getComponent().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		//mView.getFrame().addComponentListener(ca0);
		
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

    void toggleMenu(){
        mnCamera.setEnabled(!mnCamera.isEnabled());
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
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((JSkyFitsImageDescription)desc).setColorLookupTable(next, true);
            }
        } 
    }

    @Override
    void toggleTransferFun(){
        JSkyFitsImage.ScaleAlgorithm next = 
            scaleMethod.ordinal() == (JSkyFitsImage.ScaleAlgorithm.values().length - 1) ? JSkyFitsImage.ScaleAlgorithm.values()[0] : JSkyFitsImage.ScaleAlgorithm.values()[scaleMethod.ordinal() + 1];
        scaleMethod = next;
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((JSkyFitsImageDescription)desc).setScaleAlgorithm(next, true);
            }
        } 
    }

    //@Override
    //public void setColorFilter(JSkyFitsImage.ColorFilter filter){
    public void setColorLookupTable(String filter){
        System.out.println("app.setColorLookupTable(" + filter + ")");
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((JSkyFitsImageDescription)desc).setColorLookupTable(filter, true);
            }
        } 
    }

    //@Override
    public void setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm method){
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((JSkyFitsImageDescription)desc).setScaleAlgorithm(method, true);
            }
        } 
    }

    //@Override
    public void rescale(double min, double max){
    	//System.out.println("rescale");
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((JSkyFitsImageDescription)desc).rescale(min, max, true);
            }
        }
    }

    @Override
    public void rescaleGlobal(boolean global){
        System.out.println("rescaleGlobal("+global+")");

        boolean globalData = false;
        if(!globalData)
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE) ){
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
            System.out.println(getLayerScene() + " == " + ((JSkyFitsImageDescription)desc).getLayerIndex());
            if(desc instanceof JSkyFitsImageDescription && (getLayerScene() == ((JSkyFitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){ 
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
                //System.out.println("getLayerIndex: " + ((JSkyFitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
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
                //System.out.println("getLayerIndex: " + ((JSkyFitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
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

        //loadFitsReference();
    }

    public void coordinateWCS(Point2D.Double xy, String id){

        try {

            double[] coord = windowToViewCoordinateFromSmarties(xy.getX(), xy.getY());
            double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

            /*
            System.out.println("coordinateWCS(" + xy.getX() + ", " + xy.getY() + ")");
            
            System.out.println("windowToViewCoordinateFromSmarties: (" + coord[0] + ", " + coord[1] + ")");
            System.out.println("windowToViewCoordinateFromSmarties Ref: (" + fitsImageDescRef.getX() + ", " + fitsImageDescRef.getY() + ")");

            

            System.out.println("coordinateWCS("+coord[0]+", "+coord[1]+")");
            System.out.println("reference("+fitsImageDescRef.getX()+", "+fitsImageDescRef.getY()+")");
            System.out.println("a: " + a);
            System.out.println("factor: " + fitsImageDescRef.getFactor());

            
            System.out.println( "(" + ((coord[0]-fitsImageDescRef.getX()+fitsImageDescRef.getWidthWithFactor()/2)/a  ) + ", " + (( coord[1]-fitsImageDescRef.getY()+fitsImageDescRef.getHeightWithFactor()/2)/a ) + ")");


            System.out.println( "width: " +fitsImageDescRef.getWidth() + " height:" +fitsImageDescRef.getHeight());

            System.out.println( "width/factor: " +(fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor() ) + " height/a:" + (fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()) ) ;

           

            //double x = (coord[0] - fitsImageDescRef.getX())/fitsImageDescRef.getFactor() + fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor()/2 ;
            //double y = (coord[1] - fitsImageDescRef.getY())/fitsImageDescRef.getFactor() + fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()/2 ;

            */

            if(fitsImageDescRef != null){
                System.out.print("xy: ");
                System.out.print(xy);
                System.out.print(" -- coord: ");
                System.out.print(coord);
                double x = (coord[0] - fitsImageDescRef.getX()) + fitsImageDescRef.getWidth()/2 ;
                double y = (coord[1] - fitsImageDescRef.getY()) + fitsImageDescRef.getHeight()/2 ;

                System.out.println( "(" + x + ", " + y + ")");

                System.out.println("size: " + fitsImageDescRef.getWidthWithFactor() + ", " +  fitsImageDescRef.getHeightWithFactor());

                //pythonWCS.changeCoordinateSystem(galacticalSystem);
                //pythonWCS.sendCoordinate(x, y, mc);
                pythonWCS.sendPix2World(x, y, id);
            }

        } catch (NullPointerException e){
            e.printStackTrace(System.out);
        }

        
    }

    public double[] windowToViewCoordinateFromCoordinateWCS(double x, double y){

        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        Location lc = cursorCamera.getLocation();

        Location l = mCamera.getLocation();

        System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());
        System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        System.out.println("fromCoordinateWCS: " + x + " " + y);
        System.out.println("fitsReference: " + fitsImageDescRef.getX() + " " + fitsImageDescRef.getY() + " -- " + (fitsImageDescRef.getWidth()/2) + " " + (fitsImageDescRef.getHeight()/2) );

        x = x + fitsImageDescRef.getX() - fitsImageDescRef.getWidth()/2;
        y = y + fitsImageDescRef.getY() - fitsImageDescRef.getHeight()/2;

        System.out.println(x + ", " + y);

        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        //double xx = (long)((double)x - ((double)SCENE_W/2.0));
        //double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        //xx = l.getX()+ a*xx;
        //yy = l.getY()+ a*yy;

        double xx = x;//x;//l.getX() + a*x;//x/a;//a*x;
        double yy = y;//y;//l.getY() + a*y;//y/a;//a*y;

        //System.out.println("xx: "+ xx + " - yy: " + yy);

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
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

                        if(pythonWCS != null){
                            System.out.println("pythonWCS.setReference("+fitsImageDescRef.getSrc().getPath()+")");

                            pythonWCS.setReference(fitsImageDescRef.getSrc().getPath());
                        } else {
                            System.out.println("pythonWCS == null. setReference() failed");
                        }

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

    

    
    class Query implements Observer{
        public static final String T_QUERY = "Query";
        Point2D.Double center;
        Point2D.Double onCircle;
        public Query(){
            pythonWCS.addObserver(this);
        }

        public void callQuery(Point2D.Double center, Point2D.Double onCircle){
            this.center = null;
            this.onCircle = null;
            pythonWCS.sendPix2World(center.x, center.y, T_QUERY+"_CENTER");
            pythonWCS.sendPix2World(onCircle.x, onCircle.y, T_QUERY+"_ONCIRCLE");
        }

        @Override
        public void update(Observable obs, Object obj){
            if(obj instanceof JSONObject){
                try{

                    System.out.println(obj);

                    JSONObject json = (JSONObject) obj;

                    String name = json.getString("name");

                    if(name.equals("pix2world")){

                        String id = json.getString("id");
                        final double ra = json.getDouble("ra");
                        final double dec = json.getDouble("dec");

                        if(id.equals(T_QUERY+"_CENTER")){
                            center = new Point2D.Double(ra, dec);
                        } else if(id.equals(T_QUERY+"_ONCIRCLE")) {
                            onCircle = new Point2D.Double(ra, dec);
                        }

                        if(isDone()){

                            //compute radius in arcmin
                            final WorldCoords wc = new WorldCoords(center.getX(), center.getY());
                            System.out.println("center: ("+center.getX()+", "+center.getY()+")");
                            WorldCoords wcDummy = new WorldCoords(onCircle.getX(), onCircle.getY());
                            System.out.println("onCircle: ("+onCircle.getX()+", "+onCircle.getY()+")");


                            final double distArcMin = wc.dist(wcDummy);
                            //perform catalog query
                            System.err.println("Querying Simbad at " + wc + " with a radius of " + distArcMin + " arcminutes");
                            // symbolSpace.removeAllGlyphs();
                            new SwingWorker(){
                                @Override public List<AstroObject> construct(){
                                    List<AstroObject> objs = null;
                                    try{
                                        //objs = SimbadCatQuery.makeSimbadCoordQuery(wc.getRaDeg(), wc.getDecDeg(), distArcMin);
                                        System.out.println("SimbadCatQuery.makeSimbadCoordQuery("+wc.getRaDeg()+", "+wc.getDecDeg()+", "+distArcMin+")");
                                        objs = SimbadCatQuery.makeSimbadCoordQuery(wc.getRaDeg(), wc.getDecDeg(), distArcMin);
                                    } catch(IOException ioe){
                                        ioe.printStackTrace();
                                    } finally {
                                        return objs;
                                    }
                                }
                                @Override public void finished(){
                                    List<AstroObject> objs = (List<AstroObject>)get();
                                    System.out.println("drawSymbols("+objs+")");
                                    drawSymbols(objs);
                                    eh.fadeOutRightClickSelection();

                                }
                            }.start();

                            center = null;
                            onCircle = null;
                            //pythonWCS.deleteObserver(this);

                        }
                    }

                    System.out.println("updateQuery()");
                } catch(JSONException e){
                    System.out.println(e);
                }
            }
        }

        public boolean isDone(){
            return center != null && onCircle != null;
        }
    }




    void querySimbad(Point2D.Double center, Point2D.Double onCircle){

        System.out.println("center");
        System.out.println(center);
        System.out.println("onCircle");
        System.out.println(onCircle);

        /*
        double x = (center.x); //+ fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
        double y = (center.y);// + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

        System.out.println( "(" + center.x + " - " + fitsImageDescRef.getX() + ") + " + fitsImageDescRef.getWidth() + "/2 = " + x  );
        System.out.println( "(" + center.y + " - " + fitsImageDescRef.getY() + ") + " + fitsImageDescRef.getHeight() + "/2 = " + y );

        center.x = x;
        center.y = y;

        VCross cr = new VCross(x, y, 1000, 200, 200, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        x = (onCircle.x + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
        y = (onCircle.y + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

        System.out.println( "(" + onCircle.x + " - " + fitsImageDescRef.getX() + ") + " + fitsImageDescRef.getWidth() + "/2 = " + x  );
        System.out.println( "(" + onCircle.y + " - " + fitsImageDescRef.getY() + ") + " + fitsImageDescRef.getHeight() + "/2 = " + y );

        onCircle.x = x;
        onCircle.y = y;

        */

        VCross cr;


        /*
        VCross cr = new VCross(center.x, center.y, 1000, 400, 400, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        cr = new VCross(onCircle.x, onCircle.y, 1000, 400, 400, Color.BLUE, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);


        cr = new VCross(onCircle.x, onCircle.y, 1000, 200, 200, Color.BLUE, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);
        */

        center.x = center.x - fitsImageDescRef.getX() + fitsImageDescRef.getWidth()/2;
        center.y = center.y - fitsImageDescRef.getY() + fitsImageDescRef.getHeight()/2;

        onCircle.x = onCircle.x - fitsImageDescRef.getX() + fitsImageDescRef.getWidth()/2;
        onCircle.y = onCircle.y - fitsImageDescRef.getY() + fitsImageDescRef.getHeight()/2;


        /*
        cr = new VCross(center.x, center.y, 1000, 200, 200, Color.RED, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);


        cr = new VCross(fitsImageDescRef.getX(), fitsImageDescRef.getY(), 1000, fitsImageDescRef.getWidth(), fitsImageDescRef.getHeight(), Color.GRAY, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        cr = new VCross(0, 0, 1000, 1000000, 1000000, Color.BLACK, Color.WHITE, .8f);
        cr.setStroke(new BasicStroke(2f));
        mSpace.addGlyph(cr);

        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        */
        
        query.callQuery(center, onCircle);

        // XXX
        //Point2D.Double centerWCS = coordinateWCS(xy); //new Point2D.Double();//img.vs2wcs(center.x, center.y);
        //Point2D.Double onCircleWCS = new Point2D.Double();//img.vs2wcs(onCircle.x, onCircle.y);

        
    }


    class DrawSymbol implements Observer{

        //String id;
        //AstroObject obj;

        public static final String T_DRAW = "Draw";

        public DrawSymbol(){
            pythonWCS.addObserver(this);
        }

        public void drawSymbol(double ra, double dec, String id){
            pythonWCS.sendWorld2Pix(ra, dec, id);
        }

        /*
        public DrawSymbol(double[] ras, double[] decs, String[] ids){
            pythonWCS.addObserver(this);
            pythonWCS.sendWorld2Pix(ras, decs, ids);
        }
        */

        @Override
        public void update(Observable obs, Object obj){
            if(obj instanceof JSONObject){
                try{

                    System.out.println(obj);

                    JSONObject json = (JSONObject) obj;

                    String name = json.getString("name");
                    

                    if(name.equals("world2pix")){

                        System.out.println("Draw Symbols: update");
                        System.out.println(json);
                        String id = json.getString("id");
                        //double x = (json.getDouble("x") + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
                        //double y = (json.getDouble("y") + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;
                        double[] l = windowToViewCoordinateFromCoordinateWCS(json.getDouble("x"), json.getDouble("y"));
                        System.out.println("location: " + l[0] + ", " + l[1]);
                        VCross cr = new VCross(l[0], l[1], 100, 20, 20, Color.YELLOW, Color.WHITE, .8f);
                        cr.setStroke(AstroObject.AO_STROKE);
                        VText lb = new VText(l[0]+10, l[1]+10, 101, Color.YELLOW, id, VText.TEXT_ANCHOR_START);
                        lb.setBorderColor(Color.BLACK);
                        lb.setTranslucencyValue(.6f);
                        mSpace.addGlyph(cr);
                        mSpace.addGlyph(lb);
                        VCircle circle = new VCircle(l[0], l[1], 100, 20, Color.YELLOW, Color.WHITE, .8f);
                        mSpace.addGlyph(circle);
                        //cr.setOwner(this.obj);
                        //lb.setOwner(this.obj);
                        cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                        lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);

                        /*
                        JSONArray arr = json.getJSONArray();

                        for(int i = 0; arr.length(); i++ ){

                            JSONObject iter = arr.getJSONObject(i);
                            String id = iter.getString("id");
                            double x = (iter.getDouble("x") + fitsImageDescRef.getX()) - fitsImageDescRef.getWidth()/2 ;
                            double y = (iter.getDouble("y") + fitsImageDescRef.getY()) - fitsImageDescRef.getHeight()/2 ;

                            VCross cr = new VCross(x, y, 100, 10, 10, Color.RED, Color.WHITE, .8f);
                            cr.setStroke(AstroObject.AO_STROKE);
                            VText lb = new VText(x+10, y+10, 101, Color.RED, id, VText.TEXT_ANCHOR_START);
                            lb.setBorderColor(Color.BLACK);
                            lb.setTranslucencyValue(.6f);
                            mSpace.addGlyph(cr);
                            mSpace.addGlyph(lb);
                            //cr.setOwner(this.obj);
                            //lb.setOwner(this.obj);
                            cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                            lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);
                        }
                        */
                        

                        //pythonWCS.deleteObserver(this);

                    }
                }catch(JSONException e){
                    System.out.println(e);
                }
            }
        }
    }

    void drawSymbols(List<AstroObject> objs){

        System.out.println("drawSymbols: size:" + objs.size());

        /*

        double[] ras = new double[objs.size()];
        double[] decs = new double[objs.size()];
        String[] ids = new String[objs.size()];

        AstroObject ao;
        for(int i = 0; i <  objs.size(); i++){
            ao = objs.get(i);
            ras[i] = ao.getRa();
            decs[i] = ao.getDec();
            ids[i] = ao.getIdentifier();
        }

        new DrawSymbol(ras, decs, ids);

        */
        
        for(AstroObject obj: objs){

            System.out.print("AstroObject: ");
            System.out.println(obj);
            
            draw.drawSymbol(obj.getRa(), obj.getDec(), obj.getIdentifier());

            // XXX
            /*
            Point2D.Double p = new Point2D.Double();//img.wcs2vs(obj.getRa(), obj.getDec());
            VCross cr = new VCross(p.x, p.y, 100, 10, 10, Color.RED, Color.WHITE, .8f);
            cr.setStroke(AstroObject.AO_STROKE);
            VText lb = new VText(p.x+10, p.y+10, 101, Color.RED, obj.getIdentifier(), VText.TEXT_ANCHOR_START);
            lb.setBorderColor(Color.BLACK);
            lb.setTranslucencyValue(.6f);
            mSpace.addGlyph(cr);
            mSpace.addGlyph(lb);
            cr.setOwner(obj);
            lb.setOwner(obj);
            cr.setType(JSkyFitsMenu.T_ASTRO_OBJ);
            lb.setType(JSkyFitsMenu.T_ASTRO_OBJ);
            */
        }
        
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


class RegionPickerListener implements RegionListener{


    JSkyFitsViewer app;

    public RegionPickerListener(JSkyFitsViewer app){
        this.app = app;
    }

    @Override
    public void enteredRegion(Region r){

        ObjectDescription[] objects = r.getObjectsInRegion();
        for( ObjectDescription desc : objects){
            if(desc instanceof JSkyFitsImageDescription){
                JSkyFitsImageDescription obj = (JSkyFitsImageDescription)desc;
                if( obj.isReference() ){
                    if( !obj.equals(app.fitsImageDescRef) ){
                        app.fitsImageDescRef = obj;
                        if(app.pythonWCS != null){
                            System.out.println("enteredRegion: " + r.getID());
                            System.out.println("pythonWCS.setReference("+app.fitsImageDescRef.getSrc().getPath()+")");
                            app.pythonWCS.setReference(app.fitsImageDescRef.getSrc().getPath());
                        } else {
                            System.out.println("pythonWCS == null. setReference() failed");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exitedRegion(Region r){
        //System.out.println("exitedRegion");
    }
}
