/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.Translucent;

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
import fr.inria.zuist.engine.RegionPicker;
import fr.inria.zuist.event.RegionListener;
import fr.inria.zuist.event.LevelListener;
import fr.inria.zuist.event.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.FitsImageDescription;

import fr.inria.zvtm.engine.ViewPanel;
import java.awt.event.MouseEvent;

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

import java.util.Observer;
import java.util.Observable;

import fr.inria.zuist.engine.JSkyFitsImageDescription;

import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;


/**
 * @author Emmanuel Pietriga, Fernando del Campo
 */

public class FitsViewer implements Java2DPainter, RegionListener, LevelListener {
    
    File SCENE_FILE, SCENE_FILE_DIR;
        
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

    public static double SCENE_W = 12000;//11520;//12000;
    public static double SCENE_H = 4500;//4320;//4500;
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    
    static final String mSpaceKsName = "SceneKsSpace";
    static final String mSpaceHName = "SceneHSpace";
    static final String mSpaceJName = "SceneJSpace";
    static final String mSpaceName = "SceneSpace";
    static final String pMnSpaceName = "PieMenuSpace";
    static final String mnSpaceName = "MenuSpace";
    static final String cursorSpaceName = "CursorSpace";
    static final String ovSpaceName = "OverlaySpace";

    
    static final int LAYER_SCENE_KS = 0;
    static final int LAYER_SCENE_H = 1;
    static final int LAYER_SCENE_J = 2;
    static final int LAYER_SCENE = 3;
    static final int LAYER_PIEMENU = 4;
    static final int LAYER_MENU = 5;
    static final int LAYER_CURSOR = 6;
    static final int LAYER_OVERLAY = 7;

    int layerScene = LAYER_SCENE;
    static final String[] layerSceneName = {"Ks", "H", "J", "all"};

    public static final Font FONT_LAYER_SCENE = new Font("Bold", Font.PLAIN, 46);

    //static final int LAYER_CURSOR = 7;

    public VirtualSpace mSpace;
    VirtualSpace mSpaceKs, mSpaceH, mSpaceJ;
    VirtualSpace pMnSpace; //menuSpace;
    public VirtualSpace mnSpace;
    public VirtualSpace cursorSpace;
    VirtualSpace ovSpace;
    
    public Camera mCamera;
    Camera mCameraKs, mCameraH, mCameraJ;
    Camera mnCamera; // menuCamera;
    public Camera cursorCamera;

    String mCameraAltStr = Messages.ALTITUDE + "0";
    String levelStr = Messages.LEVEL + "0";
    static final String mViewName = "ZUIST Viewer";
    View mView;
    ClusteredView clusteredView;
    FitsViewerEventHandler eh;

    SceneManager sm;

	FitsOverlayManager ovm;
	//VWGlassPane gp;
	PieMenu mainPieMenu;

    FitsImage.ColorFilter cfilter = FitsImage.ColorFilter.RAINBOW;
    FitsImage.ScaleMethod scaleMethod = FitsImage.ScaleMethod.LINEAR;

    public FitsMenu menu;
    double[] globalScaleParams = {Double.MAX_VALUE, Double.MIN_VALUE};
    
    SmartiesManager smartiesMngr;
    TuioEventHandler teh;

    public RegionPicker rPicker;

    public FitsImageDescription fitsImageDescRef;
    //public Object fitsImageDescRef;
    //NomWcsKeywordProvider wcsKeyProviderRef;
    //WCSTransform wcsTransformRef;

    VText wcsLabel;
    VText layerSceneLabel;

    String reference;

    public PythonWCS pythonWCS;

    AnimationManager am = VirtualSpaceManager.INSTANCE.getAnimationManager();
    //boolean galacticalSystem = false;
    
    //JSONArray readed;
    //Vector<SavedPosition> savedPositions;

    static HashMap<String,String> parseSceneOptions(Options options){
        HashMap<String,String> res = new HashMap(2,1);
        if (options.httpUser != null){
            res.put(SceneManager.HTTP_AUTH_USER, options.httpUser);
        }
        if (options.httpPassword != null){
            res.put(SceneManager.HTTP_AUTH_PASSWORD, options.httpPassword);
        }
        return res;
    }

    //public FitsViewer(boolean fullscreen, boolean opengl, boolean antialiased, File xmlSceneFile){
    public FitsViewer(Options options){
		ovm = new FitsOverlayManager(this);
		//initGUI(fullscreen, opengl, antialiased);
		initGUI(options);
        VirtualSpace[]  sceneSpaces = {mSpace, mSpaceKs, mSpaceH, mSpaceJ};
        Camera[] sceneCameras = {mCamera, mCameraKs, mCameraH, mCameraJ};

        sm = new SceneManager(sceneSpaces, sceneCameras, parseSceneOptions(options));
        sm.setRegionListener(this);
        sm.setLevelListener(this);

        pythonWCS = new PythonWCS();

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
        //smartiesMngr = new SmartiesManager(this);
        teh = new TuioEventHandler(this);

        layerSceneLabel = new VText(0d, 0d, 1, Color.RED, "");
        layerSceneLabel.setFont(FONT_LAYER_SCENE);
        layerSceneLabel.setTranslucencyValue(0.9f);

        //reference = options.reference;

        
        //pythonWCS.sendCoordinate(0,0, null);

    }
    //void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
    void initGUI(Options options){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("FitsViewer");

        
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

        eh = new FitsViewerEventHandler(this);

        menu = new FitsMenu(this);
        
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

        mView.setActiveLayer(LAYER_SCENE);
		
    }



    public void setColorFilter(ImageFilter filter){
        //hi.setColorFilter(filter);
    }

    public int getDisplayWidth(){
        return SCREEN_WIDTH;
    }

    public int getDisplayHeight(){
        return SCREEN_HEIGHT;
    }

    JMenuItem infoMI, consoleMI;

	public JMenuBar initMenu(){
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem reloadMI = new JMenuItem("Reload");
		reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		infoMI = new JMenuItem(Messages.INFO_SHOW);
		infoMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		consoleMI = new JMenuItem(Messages.CONSOLE_HIDE);
		consoleMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		final JMenuItem gcMI = new JMenuItem("Run Garbage Collector");
		gcMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
		final JMenuItem aboutMI = new JMenuItem("About...");
        final JMenuItem shortcutMI = new JMenuItem("Shortcut");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==openMI){openFile();}
				else if (e.getSource()==reloadMI){reload();}
				else if (e.getSource()==exitMI){exit();}
				else if (e.getSource()==infoMI){toggleMiscInfoDisplay();}
				else if (e.getSource()==gcMI){gc();}
				else if (e.getSource()==consoleMI){ovm.toggleConsole();}
				else if (e.getSource()==aboutMI){about();}
                else if (e.getSource()==shortcutMI){shortCut();}
			}
		};
		JMenuBar jmb = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenu viewM = new JMenu("View");
		JMenu helpM = new JMenu("Help");
		fileM.add(openMI);
		fileM.add(reloadMI);
		fileM.addSeparator();
		fileM.add(exitMI);
		viewM.add(infoMI);
		viewM.add(gcMI);
		viewM.add(consoleMI);
		helpM.add(aboutMI);
        helpM.add(shortcutMI);
		jmb.add(fileM);
		jmb.add(viewM);
		jmb.add(helpM);
		openMI.addActionListener(a0);
		reloadMI.addActionListener(a0);
		exitMI.addActionListener(a0);
		infoMI.addActionListener(a0);
		consoleMI.addActionListener(a0);
		gcMI.addActionListener(a0);
		aboutMI.addActionListener(a0);
        shortcutMI.addActionListener(a0);
		return jmb;
	}

    public int getLayerScene(){
        return layerScene;
    }

    public void setLayerScene(int l){
        layerScene = l;
        layerSceneLabel.setText(layerSceneName[layerScene]);
        //cursorSpace.addGlyph(layerSceneLabel);
        
        cursorSpace.addGlyph(layerSceneLabel);
        //layerSceneLabel.setTranslucencyValue(0f);

        Animation a = am.getAnimationFactory().createTranslucencyAnim(800,
            layerSceneLabel, 0f, false, IdentityInterpolator.getInstance(),
            new EndAction(){
                public void execute(Object subject, Animation.Dimension dimension){

                    cursorSpace.removeGlyph(layerSceneLabel);
                    layerSceneLabel.setTranslucencyValue(0.9f);
                }
            });
        am.startAnimation(a, true);
    }
	
    void toggleColorFilter(){
        FitsImage.ColorFilter next = 
            cfilter.ordinal() == (FitsImage.ColorFilter.values().length - 1) ? FitsImage.ColorFilter.values()[0] : FitsImage.ColorFilter.values()[cfilter.ordinal() + 1];
        cfilter = next;
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((FitsImageDescription)desc).setColorFilter(next);
            }
        } 
    }

    void toggleTransferFun(){
        FitsImage.ScaleMethod next = 
            scaleMethod.ordinal() == (FitsImage.ScaleMethod.values().length - 1) ? FitsImage.ScaleMethod.values()[0] : FitsImage.ScaleMethod.values()[scaleMethod.ordinal() + 1];
        scaleMethod = next;
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((FitsImageDescription)desc).setScaleMethod(next);
            }
        } 
    }

    public void setColorFilter(FitsImage.ColorFilter filter){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((FitsImageDescription)desc).setColorFilter(filter);
            }
        } 
    }

    public void setScaleMethod(FitsImage.ScaleMethod method){
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                ((FitsImageDescription)desc).setScaleMethod(method);
            }
        } 
    }

    public void rescale(double min, double max, double sigma){
    	//System.out.println("rescale");
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE) ){
                ((FitsImageDescription)desc).rescale(min, max, sigma);
            }
        }
    }

    public void rescaleGlobal(boolean global){
        //System.out.println("rescaleGlobal("+global+")");

        boolean globalData = false;
        if(!globalData)
    	for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){
                if(((FitsImageDescription)desc).isCreatedWithGlobalData()){
                    globalData = true;
                    //break;
                }
                double[] localScaleParams = ((FitsImageDescription)desc).getLocalScaleParams();
                if(localScaleParams[0] < globalScaleParams[0]) globalScaleParams[0] = localScaleParams[0];
                if(localScaleParams[1] > globalScaleParams[1]) globalScaleParams[1] = localScaleParams[1];
            }
        }
        if(globalData)
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && (getLayerScene() == ((FitsImageDescription)desc).getLayerIndex() || getLayerScene() == LAYER_SCENE)){ 
                ((FitsImageDescription)desc).setRescaleGlobal(globalScaleParams[0], globalScaleParams[1]);
                ((FitsImageDescription)desc).setRescaleGlobal(global);
                if(global) ((FitsImageDescription)desc).rescaleGlobal();
                else ((FitsImageDescription)desc).rescaleLocal();
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

    public void hideLayer(int layerIndex){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
               // System.out.println("getLayerIndex: " + ((FitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
                if( ((FitsImageDescription)desc).getLayerIndex() == layerIndex){
                    ((FitsImageDescription)desc).setVisible(false);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.hide(g);
        }*/
    }

    public void showLayer(int layerIndex, float alpha){
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
                //System.out.println("getLayerIndex: " + ((FitsImageDescription)desc).getLayerIndex() + " layerIndex: " + layerIndex);
                if( ((FitsImageDescription)desc).getLayerIndex() == layerIndex){
                    ((FitsImageDescription)desc).setVisible(true);
                    ((FitsImageDescription)desc).setTranslucencyValue(alpha);
                }
            }
        }
        /*for(Glyph g:vs.getAllGlyphs()){
            vs.show(g);
            g.setTranslucencyValue(alpha);
        }
        */
    }

    
    public void orientTo(double angle){
        System.out.println("angle: "+angle);
        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
                ((FitsImageDescription)desc).orientTo(angle);
            }
        }
    }

    public void loadFitsReference(){
        if(reference == null){
            
            for(ObjectDescription desc: sm.getObjectDescriptions()){
                if(desc instanceof FitsImageDescription){
                    if( ((FitsImageDescription)desc).isReference()){
                        System.out.println("Reference");
                        System.out.println(desc);

                        fitsImageDescRef = (FitsImageDescription)desc;
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
        
        
        System.out.println("loaded IVO Fits Reference");
    }

    

    /*
    public void moveToCoordinatesWCS(){


        Point2D.Double origin = null;
        Point2D.Double cdelt = null;

        String idTile = "I00358";//"I00370";

        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription){
                //System.out.println("index: "+((FitsImageDescription)desc).getID());
                if(((FitsImageDescription)desc).getID().equals(idTile+"-1-4-1-4-1-4")) {
                //if(((FitsImageDescription)desc).getID().equals(idTile+"-1-0-0-0-0-0")) {

                    try{
                        FITSImage fitsImage = new FITSImage(((FitsImageDescription)desc).getSrc());
                        System.out.println("src: "+((FitsImageDescription)desc).getSrc());
                        wcsKeyProvider = new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader());
                        wcsTransform = new WCSTransform(wcsKeyProvider);
                        Point2D.Double pix = wcsTransform.wcs2pix(wcsKeyProvider.getDoubleValue("CRVAL1"), wcsKeyProvider.getDoubleValue("CRVAL2"));
                        cdelt = new Point2D.Double(wcsKeyProvider.getDoubleValue("CDELT1"), wcsKeyProvider.getDoubleValue("CDELT2"));
                        //System.out.print("origin: (;" + origin.getX() + ";, ;" + origin.getY() + ";) --> (;");
                        ((FitsImageDescription)desc).moveTo(pix.getX(), pix.getY());
                        origin = new Point2D.Double(pix.getX(), pix.getY());
                        System.out.println("origin: (" + origin.getX() + ", " + origin.getY() + "): RA: " + wcsKeyProvider.getDoubleValue("CRVAL1") + " -- DEC: " + wcsKeyProvider.getDoubleValue("CRVAL2") );
                        double h = ((FitsImageDescription)desc).getHeight();
                        double w = ((FitsImageDescription)desc).getWidth();
                        double x = ((FitsImageDescription)desc).getX();
                        double y = ((FitsImageDescription)desc).getY();
                        System.out.println("h: "+ h + " w: " + w + " x: " + x + " y: " + y);
                        System.out.print(wcsTransform.wcs2pix(273.0544, -23.673911) );
                        System.out.print(" -- ");
                        System.out.println(wcsTransform.wcs2pix(274.21448, -24.24497) );
                        System.out.print(wcsTransform.wcs2pix(272.27939, -24.967136) );
                        System.out.print(" -- ");
                        System.out.println(wcsTransform.wcs2pix(273.45378, -25.550794) );

                        break;
                    } catch(IOException ioe){
                        wcsTransform = null;
                    } catch (FitsException fe){
                        wcsTransform = null;
                    } catch(FITSImage.NoImageDataFoundException nidfe){
                        wcsTransform = null;
                    } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                        wcsTransform = null;
                    }
                }
            }
        }

        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription ){ //&& !((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-1")){
                /*
                if(origin == null){
                    origin = desc;
                    try{
                        FITSImage fitsImage = new FITSImage(((FitsImageDescription)origin).getSrc());
                        System.out.println("src: "+((FitsImageDescription)origin).getSrc());
                        wcsKeyProvider = new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader());
                        wcsTransform = new WCSTransform(wcsKeyProvider);
                        Point2D.Double pix = wcsTransform.wcs2pix(wcsKeyProvider.getDoubleValue("CRVAL1"), wcsKeyProvider.getDoubleValue("CRVAL2"));
                        //System.out.print("origin: (;" + origin.getX() + ";, ;" + origin.getY() + ";) --> (;");
                        //origin.moveTo(pix.getX(), pix.getY());
                        //System.out.println(origin.getX() + ";, ;" + origin.getY() + ";);");
                    } catch(IOException ioe){
                        wcsTransform = null;
                    } catch (FitsException fe){
                        wcsTransform = null;
                    } catch(FITSImage.NoImageDataFoundException nidfe){
                        wcsTransform = null;
                    } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                        wcsTransform = null;
                    }
                } else {
                *
                    try{
                        FITSImage fitsImage = new FITSImage(((FitsImageDescription)desc).getSrc());
                        NomWcsKeywordProvider wcsKeyProvider2 = new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader());
                        WCSTransform wcsTransform2 = new WCSTransform(wcsKeyProvider2);

                        Point2D.Double pix = wcsTransform.wcs2pix(wcsKeyProvider2.getDoubleValue("CRVAL1"), wcsKeyProvider2.getDoubleValue("CRVAL2"));
                        //Point2D.Double pix = wcsTransform2.wcs2pix(wcsKeyProvider.getDoubleValue("CRVAL1"), wcsKeyProvider.getDoubleValue("CRVAL2"));
                        Point2D.Double pc1 = new Point2D.Double(wcsKeyProvider2.getDoubleValue("PC1_1"), wcsKeyProvider2.getDoubleValue("PC1_2"));
                        Point2D.Double pc2 = new Point2D.Double(wcsKeyProvider2.getDoubleValue("PC2_1"), wcsKeyProvider2.getDoubleValue("PC2_2"));

                        // MATRIX PROJECTION
                        /**
                        * alpha * | cos(teta)  -sin(teta) |
                        *         | sen(teta)   cos(teta) |
                        * alpha = sqrt( cos(teta)^2 + sin(teta)^2)
                        **

                        double alpha = Math.sqrt(pc1.getX()*pc1.getX() + pc2.getX()*pc2.getX());
                        double teta = Math.acos( pc1.getX()/(alpha) );

                        Point2D.Double pixo = wcsTransform2.wcs2pix(wcsKeyProvider2.getDoubleValue("CRVAL1"), wcsKeyProvider2.getDoubleValue("CRVAL2"));
                        //System.out.print("desc: (;" + desc.getX() + ";, ;" + desc.getY() + ";) --> (;");
                        
                        //if( ((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-0") || ((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-1") || ((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-2") || ((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-3") || ((FitsImageDescription)desc).getID().equals("I00358-1-1-1-1-1-4") ){
                        //    System.out.println(origin.getX() + ", " + origin.getY() + " -- " + pix.getX() + ", " + pix.getY() + " -- " + pixo.getX() + ", " + pixo.getY() + " -- " + ((FitsImageDescription)desc).getID() + " -- RA: " + wcsKeyProvider2.getDoubleValue("CRVAL1") + " DEC: " + wcsKeyProvider2.getDoubleValue("CRVAL2") );
                        //}


                        if( ((FitsImageDescription)desc).getID().equals(idTile+"-1-1-0-0-0-0") || ((FitsImageDescription)desc).getID().equals(idTile+"-1-2-0-0-0-0") || ((FitsImageDescription)desc).getID().equals(idTile+"-1-3-0-0-0-0") || ((FitsImageDescription)desc).getID().equals(idTile+"-1-4-0-0-0-0") || ((FitsImageDescription)desc).getID().equals(idTile+"-1-0-0-0-0-0") ){
                            System.out.println(origin.getX() + ", " + origin.getY() + " -- " + pix.getX() + ", " + pix.getY() + " -- " + pixo.getX() + ", " + pixo.getY() + " -- " + ((FitsImageDescription)desc).getID() + " -- RA: " + wcsKeyProvider2.getDoubleValue("CRVAL1") + " DEC: " + wcsKeyProvider2.getDoubleValue("CRVAL2") );
                            double h = ((FitsImageDescription)desc).getHeight();
                            double w = ((FitsImageDescription)desc).getWidth();
                            double x = ((FitsImageDescription)desc).getX();
                            double y = ((FitsImageDescription)desc).getY();
                            System.out.println("h: "+ h + " w: " + w + " x: " + x + " y: " + y);
                            System.out.println("PC1: ");
                            System.out.println(pc1);
                            System.out.println("PC2: ");
                            System.out.println(pc2);
                            System.out.println("CDELT: ");
                            System.out.println(cdelt);
                            
                            System.out.println("alpha: " + alpha);
                            System.out.println("teta: " + teta );

                            System.out.println("cdelt: " + cdelt.getX());
                            System.out.println("pix*cdelt: " + (pix.getX()*cdelt.getX()));
                            System.out.println( "pix/1.1011: "+(pix.getX()/1.1011));
                            System.out.println("1/cdelt:: " + (1/cdelt.getX()));
                        }
                        
                        //desc.moveTo( pix.getX()*(cdelt.getX()), pix.getY()*(cdelt.getY()) );
                        //desc.moveTo( pix.getX(), pix.getY() );
                        desc.moveTo( pix.getX()/1.10101, pix.getY()/1.005 );
                        //desc.moveTo( pix.getX()/(alpha*1.125), pix.getY()/(alpha) );
                        ((FitsImageDescription)desc).orientTo(-teta);

                        //desc.moveTo(origin.getX() + pix.getX() + pixo.getX(), origin.getY() + pix.getY() - pixo.getY() );
                        //System.out.println(desc.getX() + ";, ;" + desc.getY() + ";);");
                        //System.out.println("("+pix.getX()+", "+pix.getY()+")");


                    } catch(IOException ioe){
                        //wcsTransform2 = null;
                    } catch (FitsException fe){
                        //wcsTransform2 = null;
                    } catch(FITSImage.NoImageDataFoundException nidfe){
                        //wcsTransform2 = null;
                    } catch(FITSImage.DataTypeNotSupportedException dtnse) {
                        //wcsTransform2 = null;
                    }
                    
                //}
            }
        }

    }


    ObjectDescription beforeFits;

    //public Vector<ObjectDescription> getGlyphOnPoint(double jpx, double jpy){
    public ObjectDescription getGlyphOnPoint(double jpx, double jpy){
        //System.out.println("getGlyphOnPoint("+jpx+", "+jpy+")");
        //Vector<ObjectDescription> result = new Vector<ObjectDescription>();
        //double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;
        //System.out.println("a: "+a);
        //a = mCamera.focal / (mCamera.focal+ mCamera.getAltitude());
        //System.out.println("a: "+a);
        double h, w, x, y;

        if(beforeFits != null && beforeFits.getGlyph() != null && ((FitsImageDescription)beforeFits).isVisible()){
            h = ((FitsImageDescription)beforeFits).getHeight();
            w = ((FitsImageDescription)beforeFits).getWidth();
            x = ((FitsImageDescription)beforeFits).getX();
            y = ((FitsImageDescription)beforeFits).getY();
            if(x-w/2 < jpx && x+w/2 > jpx && y-h/2 < jpy && y+h/2 > jpy){
                return beforeFits;
            }

            /*
            int i = 0;
            for(ObjectDescription desc: ((FitsImageDescription)beforeFits).getParentRegion().getObjectsInRegion() ){
                System.out.println(i++);
                h = ((FitsImageDescription)desc).getHeight();
                w = ((FitsImageDescription)desc).getWidth();
                x = ((FitsImageDescription)desc).getX();
                y = ((FitsImageDescription)desc).getY();
                if(x-w/2 < jpx && x+w/2 > jpx && y-h/2 < jpy && y+h/2 > jpy){
                    //result.add((FitsImageDescription)desc);
                    //return result;
                    beforeFits = desc;
                    return desc;
                }
            }
            /

        }
 

        for(ObjectDescription desc: sm.getObjectDescriptions()){
            if(desc instanceof FitsImageDescription && ((FitsImageDescription)desc).isVisible() && ((FitsImageDescription)desc).getGlyph() != null){
                h = ((FitsImageDescription)desc).getHeight();
                w = ((FitsImageDescription)desc).getWidth();
                x = ((FitsImageDescription)desc).getX();
                y = ((FitsImageDescription)desc).getY();
                if(x-w/2 < jpx && x+w/2 > jpx && y-h/2 < jpy && y+h/2 > jpy){
                    //result.add((FitsImageDescription)desc);
                    //return result;
                    beforeFits = desc;
                    return desc;
                }
            }
            /*
            if(desc instanceof FitsImageDescription && ((FitsImageDescription)desc).getGlyph() != null){
                Glyph g = ((FitsImageDescription)desc).getGlyph();
                //System.out.println("x: " + ((FitsImageDescription)desc).getX() + " - y: " + ((FitsImageDescription)desc).getY() );
                double[] border = g.getBounds();
                //System.out.println(border[0]+" < "+jpx+" && "+border[1]+" > "+jpy+" && "+border[2]+" > "+jpx+" && "+border[3]+" < "+jpy );
                if(border[0] < jpx && border[1] > jpy && border[2] > jpx && border[3] < jpy){
                    //System.out.println(((FitsImageDescription)desc).getX() + " - " + ((FitsImageDescription)desc).getY());
                    result.add(g);
                }
            }
            /
        }
        //return result;
        return null;
    }
    */


    /*
    public void changeCoordinateSystem(Point2D.Double xy, MyCursor mc){

        if(galacticalSystem)
            galacticalSystem = false;
        else
            galacticalSystem = true;
        coordinateWCS(xy, mc);
    }
    */

    
    
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

           */

            double x = (coord[0] - fitsImageDescRef.getX())/fitsImageDescRef.getFactor() + fitsImageDescRef.getWidth()/fitsImageDescRef.getFactor()/2 ;
            double y = (coord[1] - fitsImageDescRef.getY())/fitsImageDescRef.getFactor() + fitsImageDescRef.getHeight()/fitsImageDescRef.getFactor()/2 ;


            System.out.println( "(" + x + ", " + y + ")");

            System.out.println("size: " + fitsImageDescRef.getWidthWithFactor() + ", " +  fitsImageDescRef.getHeightWithFactor());

            //pythonWCS.changeCoordinateSystem(galacticalSystem);
            //pythonWCS.sendCoordinate(x, y, mc);
            pythonWCS.sendPix2World(x, y, id);

        } catch (NullPointerException e){
            e.printStackTrace(System.out);
        }

        
    }
    

    /*
    public String getGalactic(){
        return "Galactic: " + pythonWCS.getGalactic();
    }

    public String getRaDec(){
        Point2D.Double coord = pythonWCS.getCoordinate();
        return "Ra: " + coord.getX() + " - Dec: " + coord.getY();
    }
    */

    

/*
    public String getObjectName(Point2D.Double xy){
        double[] coord = windowToViewCoordinate(xy.getX(), xy.getY());
        ObjectDescription g = getGlyphOnPoint(coord[0], coord[1] );
        FitsImageDescription fi;
        if(g != null){
            fi = (FitsImageDescription)(g);
        } else {
            return "";
        }
        return fi.getObjectName();
    }
*/

/*
    void updateWCS(Point2D.Double xy){

        //Camera c = mCamera;
        //double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        //double vx = c.vx;
        //double vy = c.vy;
        //System.out.println("VirtualSpace: Camera("+vx+","+vy+") (" + jpx + ", " + jpy + ")");
        //System.out.println("(" + (vx + a*(jpx-application.VIEW_W/2) ) + ", " + (vy + a*(jpy-application.VIEW_H/2) ) + ")");
        //System.out.println("VIEW_W: " + application.VIEW_W + " - VIEW_H: " + application.VIEW_H);
        
        System.out.println("updateWCS xy: " + xy.getX() + " " + xy.getY());



        double[] pos = windowToViewCoordinate(xy.getX(), xy.getY());
        Point2D.Double cur = new Point2D.Double(pos[0], pos[1]);
        
        System.out.println(pos[0] + " " + pos[1]);

        System.out.println(windowToViewCoordinate(0, 0)[0] + " " + windowToViewCoordinate(0, 0)[1]);

        Vector<Glyph> g = getGlyphOnPoint(cur.getX(), cur.getY() );
        FitsImage fi;
        if(g.size() > 0){
            fi = (FitsImage)g.firstElement();
        } else {
            return;
        }

        double x = (cur.getX()-fi.getLocation().getX());
        double y = (cur.getY()-fi.getLocation().getY());

        System.out.println("cursor-fits:");
        System.out.println(x + " " + y);


        Point2D.Double wcs = fi.pix2wcs(x, y);

        System.out.println(wcs);
        if(wcsLabel == null)
            wcsLabel = new VText(xy.getX(), xy.getY(), 100, Color.RED, "POSICION WCS: " + wcs.toString() );
        else
            wcsLabel.setText("POSICION WCS: " + wcs.toString());
        VCircle test = new VCircle(xy.getX(), xy.getY(), 100, 100, Color.RED);
    }

*/

	void displayMainPieMenu(boolean b){
		if (b){
			PieMenuFactory.setItemFillColor(ConfigManager.PIEMENU_FILL_COLOR);
			PieMenuFactory.setItemBorderColor(ConfigManager.PIEMENU_BORDER_COLOR);
			PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
			PieMenuFactory.setSelectedItemBorderColor(null);
			PieMenuFactory.setLabelColor(ConfigManager.PIEMENU_BORDER_COLOR);
			PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
			PieMenuFactory.setTranslucency(0.7f);
			PieMenuFactory.setSensitivityRadius(0.5);
			PieMenuFactory.setAngle(-Math.PI/2.0);
			PieMenuFactory.setRadius(150);
			mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, mView);
			Glyph[] items = mainPieMenu.getItems();
			items[0].setType(Messages.PM_ENTRY);
			items[1].setType(Messages.PM_ENTRY);
			items[2].setType(Messages.PM_ENTRY);
			items[3].setType(Messages.PM_ENTRY);
		}
		else {
			mainPieMenu.destroy(0);
			mainPieMenu = null;
		}
	}

	void pieMenuEvent(Glyph menuItem){
		int index = mainPieMenu.getItemIndex(menuItem);
		if (index != -1){
			String label = mainPieMenu.getLabels()[index].getText();
			if (label == Messages.PM_BACK){moveBack();}
			else if (label == Messages.PM_GLOBALVIEW){getGlobalView(null);}
			else if (label == Messages.PM_OPEN){openFile();}
			else if (label == Messages.PM_RELOAD){reload();}
		}
	}

    void windowLayout(){	
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        } else{
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

	/*-------------  Scene management    -------------*/
	
	void reset(){
		sm.reset();
		mSpace.removeAllGlyphs();
	}
	
	void openFile(){
		final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Find ZUIST Scene File");
		int returnVal= fc.showOpenDialog(mView.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION){
		    final SwingWorker worker = new SwingWorker(){
			    public Object construct(){
					/*
                    reset();
					sm.setUpdateLevel(false);
					sm.enableRegionUpdater(false);
					loadScene(fc.getSelectedFile());
					EndAction ea  = new EndAction(){
                           public void execute(Object subject, Animation.Dimension dimension){
                               sm.setUpdateLevel(true);
                               sm.enableRegionUpdater(true);
                               
                           }
                       };
					getGlobalView(ea);
                    */
                    openScene(fc.getSelectedFile());
					return null; 
			    }
			};
		    worker.start();
		}
	}

	void reload(){
		if (SCENE_FILE==null){return;}
		final SwingWorker worker = new SwingWorker(){
		    public Object construct(){
				reset();
				loadScene(SCENE_FILE);
                return null; 
		    }
		};
	    worker.start();
	}

    public void openScene(File xmlSceneFile) {
        reset();
        sm.setUpdateLevel(false);
        sm.enableRegionUpdater(false);
        loadScene(xmlSceneFile);
        EndAction ea  = new EndAction(){
               public void execute(Object subject, Animation.Dimension dimension){
                   sm.setUpdateLevel(true);
                   sm.enableRegionUpdater(true);
               }
           };
        getGlobalView(ea);
    }

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
    
    /*-------------     Navigation       -------------*/

    public void getGlobalView(EndAction ea){
		int l = 0;
		while (sm.getRegionsAtLevel(l) == null){
			l++;
			if (l > sm.getLevelCount()){
				l = -1;
				break;
			}
		}
		if (l > -1){
			rememberLocation(mCamera.getLocation());
			//long[] wnes = sm.getLevel(l).getBounds();
			double[] wnes = sm.getLevel(l).getBounds();
	        mCamera.getOwningView().centerOnRegion(mCamera, FitsViewer.ANIM_MOVE_LENGTH, wnes[0], wnes[1], wnes[2], wnes[3], ea);

		}
    }

    /* Higher view */
    public void getHigherView(){
		rememberLocation(mCamera.getLocation());
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
//        vsm.animator.createCameraAnimation(FitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(FitsViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    public void getLowerView(){
		rememberLocation(mCamera.getLocation());
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
//        vsm.animator.createCameraAnimation(FitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(FitsViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of FitsViewer.MOVE_* */
    public void translateView(short direction){
        //LongPoint trans;
        Point2D.Double trans;
        //long[] rb = mView.getVisibleRegion(mCamera);
        double[] rb = mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            //long qt = Math.round((rb[1]-rb[3])/4.0);
            double qt = (rb[1]-rb[3])/4.0;
            //trans = new LongPoint(0,qt);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            //trans = new LongPoint(0,qt);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            //trans = new LongPoint(qt,0);
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            //trans = new LongPoint(qt,0);
            trans = new Point2D.Double(qt,0);
        }
//        vsm.animator.createCameraAnimation(FitsViewer.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(FitsViewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
        
    }

    /* x,y in (X Window) screen coordinate */
    public void directTranslate(double x, double y){
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        Location l = mCamera.getLocation();
        double newx = l.getX() + a*x;
        double newy = l.getY() + a*y;
        mCamera.setLocation(new Location(newx, newy, l.getAltitude()));
    }

    public void zoomAnimated(double f, EndAction ea){
        Float alt = new Float(f);
        //f.out.println(" f: " + f);
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
            alt, true, SlowInSlowOutInterpolator.getInstance(), ea);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    public void centeredZoom(double f, double x, double y){
        Location l = mCamera.getLocation();
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        double newz = mCamera.focal * a * f - mCamera.focal;
        if (newz < 0){
            newz = 0;
            f = mCamera.focal / (a*mCamera.focal);
        }

        System.out.println("x: " + x + " - y: " + y);

        double xx = (long)((double)x - (SCENE_W/2.0))*a + l.getX();
        double yy = (long)(-(double)y + (SCENE_H/2.0))*a + l.getY();

        System.out.println("xx: " + xx + " - yy: " + yy);

        double dx = l.getX() - xx;
        double dy = l.getY() - yy;

        System.out.println("dx: " + dx + " - dy: " + dy);

        double newx = l.getX() + (f*dx - dx); // *a/(mCamera.altitude+ mCamera.focal));
        double newy = l.getY() + (f*dy - dy);

        System.out.println("newx: " + newx + " - newy: " + newy + " - newz: " + newz);

        mCamera.setLocation(new Location(newx, newy, newz));
    }

    public void traslateAnimated(double x, double y, EndAction ea){
        Location l = mCamera.getLocation();
        double a = (mCamera.focal+Math.abs(mCamera.altitude)) / mCamera.focal;
        double[] r = windowToViewCoordinate(x, y);
        double dx = l.getX() - r[0];
        double dy = l.getY() - r[1];

        Point2D.Double trans = new Point2D.Double(-dx,-dy);

        Animation ani = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Viewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), ea);
        vsm.getAnimationManager().startAnimation(ani, false);
    }

    public double[] windowToViewCoordinate(double x, double y){
        Location l = mCamera.getLocation();
        //System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        Location lc = cursorCamera.getLocation();
        //System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());

        //
        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        double xx = (long)((double)x - ((double)SCENE_W/2.0));
        double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        xx = l.getX()+ a*xx;
        yy = l.getY()+ a*yy;

        //double xx = l.getX()+ a*x;
        //double yy = l.getY()+ a*y;

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
    }

    public double[] windowToViewCoordinateFromSmarties(double x, double y){
        Location l = mCamera.getLocation();
        //System.out.println("mCamera.getLocation(): " + l.getX() + " " + l.getY());
        double a = (mCamera.focal + mCamera.getAltitude()) / mCamera.focal;

        Location lc = cursorCamera.getLocation();
        //System.out.println("cursorCamera.getLocation(): " + lc.getX() + " " + lc.getY());

        //double xx = (long)((double)x - ((double)getDisplayWidth()/2.0));
        //double yy = (long)(-(double)y + ((double)getDisplayHeight()/2.0));
        //double xx = (long)((double)x - ((double)SCENE_W/2.0));
        //double yy = (long)(-(double)y + ((double)SCENE_H/2.0));
        
        //xx = l.getX()+ a*xx;
        //yy = l.getY()+ a*yy;

        double xx = l.getX()+ lc.getX() + a*x;//x/a;//a*x;
        double yy = l.getY()+ lc.getY() + a*y;//y/a;//a*y;

        //System.out.println("xx: "+ xx + " - yy: " + yy);

        double[] r = new double[2];
        r[0] = xx;
        r[1] = yy;
        return r;
    }

    


	public void centerOnObject(String id){
		ovm.sayInConsole("Centering on object "+id+"\n");
		ObjectDescription od = sm.getObject(id);
		if (od != null){
			Glyph g = od.getGlyph();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				mCamera.getOwningView().centerOnGlyph(g, mCamera, FitsViewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}
	}

	public void centerOnRegion(String id){
		ovm.sayInConsole("Centering on region "+id+"\n");
		Region r = sm.getRegion(id);
		if (r != null){
			Glyph g = r.getBounds();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				mCamera.getOwningView().centerOnGlyph(g, mCamera, FitsViewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}		
	}

	Vector previousLocations;
	static final int MAX_PREV_LOC = 100;
	
	public void rememberLocation(){
	    rememberLocation(mCamera.getLocation());
    }
    
	public void rememberLocation(Location l){
		if (previousLocations.size() >= MAX_PREV_LOC){
			// as a result of release/click being undifferentiated)
			previousLocations.removeElementAt(0);
		}
		if (previousLocations.size()>0){
			if (!Location.equals((Location)previousLocations.lastElement(),l)){
                previousLocations.add(l);
            }
		}
		else {previousLocations.add(l);}
	}
	
	public void moveBack(){
		if (previousLocations.size()>0){
			Vector animParams = Location.getDifference(mSpace.getCamera(0).getLocation(), (Location)previousLocations.lastElement());
			sm.setUpdateLevel(false);
            class LevelUpdater implements EndAction {
                public void execute(Object subject, Animation.Dimension dimension){
                    sm.setUpdateLevel(true);
                }
            }
            Animation at = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(FitsViewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Point2D.Double)animParams.elementAt(1), true, SlowInSlowOutInterpolator.getInstance(), null);
            Animation aa = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(FitsViewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Double)animParams.elementAt(0), true, SlowInSlowOutInterpolator.getInstance(), new LevelUpdater());
            vsm.getAnimationManager().startAnimation(at, false);
            vsm.getAnimationManager().startAnimation(aa, false);
			previousLocations.removeElementAt(previousLocations.size()-1);
		}
	}
	
    void altitudeChanged(){
        mCameraAltStr = Messages.ALTITUDE + String.valueOf(mCamera.altitude);
    }
    
    void updatePanelSize(){
        //Dimension d = mView.getPanel().getSize();
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
		if (ovm.console != null){
			ovm.updateConsoleBounds();
		}
	}

	/* ---- Debug information ----*/
	
	public void enteredRegion(Region r){
	    ovm.sayInConsole("Entered region "+r.getID()+"\n");
	}

	public void exitedRegion(Region r){
	    ovm.sayInConsole("Exited region "+r.getID()+"\n");
	}

	public void enteredLevel(int depth){
	    ovm.sayInConsole("Entered level "+depth+"\n");
	    levelStr = Messages.LEVEL + String.valueOf(depth);
	}

	public void exitedLevel(int depth){
	    ovm.sayInConsole("Exited level "+depth+"\n");
	}
	
    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;	
    boolean SHOW_MISC_INFO = true;

    void toggleMiscInfoDisplay(){
        SHOW_MISC_INFO = !SHOW_MISC_INFO;
        if (SHOW_MISC_INFO){
            infoMI.setText(Messages.INFO_HIDE);
        }
        else {
            infoMI.setText(Messages.INFO_SHOW);
        }
        //vsm.repaintNow();
        vsm.repaint();
    }

	static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	static final Color MID_DARK_GRAY = new Color(64,64,64);

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
        totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
        usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(20,
            3,
            200,
            13);
        g2d.setColor(FitsViewer.MID_DARK_GRAY);
        g2d.fillRect(20,
            3,
            totalMemRatio * 2,
            13);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(20,
            3,
            usedMemRatio * 2,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(20,
            3,
            200,
            13);
        g2d.drawString(usedMemRatio + "%", 50, 14);
        g2d.drawString(totalMemRatio + "%", 100, 14);
        g2d.drawString(maxMem/1048576 + " Mb", 160, 14);	
    }

    // consider 1000 as the maximum number of requests that can be in the queue at any given time
    // 1000 is the default value ; adapt for each scene depending on the number of objects
    // as this could vary dramatically from one scene to another - see loadScene()
    float MAX_NB_REQUESTS = 1000;
    static final int REQ_QUEUE_BAR_WIDTH = 100;
    static final int REQ_QUEUE_BAR_HEIGHT = 6;
    
    void showReqQueueStatus(Graphics2D g2d, int viewWidth, int viewHeight){
        float ratio = sm.getPendingRequestQueueSize()/(MAX_NB_REQUESTS);
        if (ratio > 1.0f){
            // do not go over gauge boundary, even if actual number of requests goes beyond MAX_NB_REQUESTS
            ratio = 1.0f;
        }
        g2d.setColor(Color.GRAY);
        g2d.fillRect(viewWidth-Math.round(REQ_QUEUE_BAR_WIDTH * ratio)-10, 7, Math.round(REQ_QUEUE_BAR_WIDTH * ratio), REQ_QUEUE_BAR_HEIGHT);
        g2d.drawRect(viewWidth-REQ_QUEUE_BAR_WIDTH-10, 7, REQ_QUEUE_BAR_WIDTH, REQ_QUEUE_BAR_HEIGHT);
    }
    
    void showAltitude(Graphics2D g2d, int viewWidth, int viewHeight){        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(240,
            3,
            190,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(240,
            3,
            190,
            13);
        g2d.drawString(levelStr, 250, 14);
        g2d.drawString(mCameraAltStr, 310, 14);
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!SHOW_MISC_INFO){return;}
		g2d.setComposite(acST);
		showMemoryUsage(g2d, viewWidth, viewHeight);
		showReqQueueStatus(g2d, viewWidth, viewHeight);
		showAltitude(g2d, viewWidth, viewHeight);
		g2d.setComposite(Translucent.acO);
    }

    /* ----- Misc  ------*/
    
    void about(){
        ovm.showAbout();
    }

    void shortCut(){
        ovm.showShortcut();
    }

	void gc(){
		System.gc();
		if (SHOW_MISC_INFO){
			//vsm.repaintNow();
			vsm.repaint();
		}
    }
    
    void exit(){
        System.exit(0);
    }



    /*
    public static void main(String[] args){
        File xmlSceneFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("smooth")){Region.setDefaultTransitions(Region.FADE_IN, Region.FADE_OUT);}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){FitsViewer.printCmdLineHelp();System.exit(0);}
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){
                    if (f.isDirectory()){
                        // if arg is a directory, take first xml file we find in that directory
                        String[] xmlFiles = f.list(new FilenameFilter(){
                                                public boolean accept(File dir, String name){return name.endsWith(".xml");}
                                            });
                        if (xmlFiles.length > 0){
                            xmlSceneFile = new File(f, xmlFiles[0]);
                        }
                    }
                    else {
                        xmlSceneFile = f;                        
                    }
                }
            }
		}
		
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        
        System.out.println("--help for command line options");
        new FitsViewer(fs, ogl, aa, xmlSceneFile);
    }
    */
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
        new FitsViewer(options);

    }

    /*
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -cp target/timingframework-1.0.jar:zuist-engine-0.2.0-SNAPSHOT.jar:target/zvtm-0.10.0-SNAPSHOT.jar <path_to_scene_dir> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
        System.out.println("\t-smooth: default to smooth transitions between levels when none specified");
    }
    */
    
}

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



