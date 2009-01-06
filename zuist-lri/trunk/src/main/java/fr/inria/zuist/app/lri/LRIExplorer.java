/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.lri;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.AlphaComposite;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleST;
import net.claribole.zvtm.engine.PostAnimationAdapter;
import net.claribole.zvtm.engine.TransitionManager;
import net.claribole.zvtm.engine.Java2DPainter;   
import net.claribole.zvtm.engine.RepaintAdapter;
import net.claribole.zvtm.engine.RepaintListener;
import net.claribole.zvtm.engine.GlyphKillAction;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.glyphs.VTextST;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.widgets.TranslucentButton;
import net.claribole.zvtm.widgets.TranslucentTextField;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.RegionListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LRIExplorer implements Java2DPainter, ProgressListener, LevelListener, RegionListener, ActionListener {

    String PATH_TO_HIERARCHY = "data/zbib/output/lri";
    String PATH_TO_SCENE_DIR = PATH_TO_HIERARCHY + "/lri4z";
    String PATH_TO_SCENE = PATH_TO_SCENE_DIR + "/scene.xml";
    String PATH_TO_METADATA = PATH_TO_HIERARCHY + "/bibdata.xml";
    File SCENE_FILE = new File(PATH_TO_SCENE);
    File METADATA_FILE = new File(PATH_TO_METADATA);

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;
    static int VIEW_MAX_H = 768;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "PDF Space";
    static final String mnSpaceName = "Menu Space";
    static final String ovSpaceName = "Overlay Space";
    VirtualSpace mSpace, mnSpace, ovSpace;
    Camera mCamera, mnCamera, ovCamera;
    String mCameraAltStr = "0";
    static final String mViewName = "ZUIST-LRI";
    View mView;
    ExplorerEventHandler eh;
    MenuEventHandler mneh;
    OverlayEventHandler oveh;

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 180;
    static int LENS_R2 = 90;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 2.0;
    static final float MAX_MAG_FACTOR = 8.0f;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    
    /* lens distance and drop-off functions */
    static final short L2_Gaussian = 0;
    static final short L2_Linear = 1;
    static final short LInf_Linear = 2;
    static final short LInf_Manhattan = 5;
    short lensFamily = L2_Gaussian;

    static final float FLOOR_ALTITUDE = 0.0f;

	static final int ANIM_MOV_LENGTH = 300;

    static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 10);
    static final Font ZVTM_LOGO_FONT = new Font("Century Gothic", Font.PLAIN, 40);
    static final Font MONOSPACE_FONT = new Font("Courier", Font.PLAIN, 10);
    static final Font ACTION_FONT = new Font("Arial", Font.ITALIC, 10);

    static final String ZVTM_LOGO_TEXT = "zvtm.sf.net";    
    
    static final Color BLANK_COLOR = Color.BLACK;

    boolean SHOW_MEMORY_USAGE = false;
    boolean SHOW_LOAD_PROGRESS = false;
    boolean SHOW_REQ_QUEUE_STATUS = true;

    /* Proceedings managenement (covers, papers, etc.) */
    SceneManager sm;

    static final short STD_VIEW = 0;
    static final short OGL_VIEW = 1;
    
    static final String NAV_MODE_DEFAULT_STR = "Click & Go";
    static final String NAV_MODE_FISHEYE_STR = "Fisheye Lens";
    static final String NAV_MODE_SS_STR = "Screen Saver";
    static final String[] NAV_MODES_STR = {NAV_MODE_DEFAULT_STR, NAV_MODE_FISHEYE_STR,
                                           NAV_MODE_SS_STR};
    static final short NAV_MODE_DEFAULT = 0;  // must be 0 (or change toggleNavigationMode())
    static final short NAV_MODE_FISHEYE = 1;
    static final short NAV_MODE_SS = 2;       // must be highest value (or change toggleNavigationMode())
    short NAV_MODE = NAV_MODE_DEFAULT;

    MenuManager mm;
    OverlayManager ovm;
    ScreenSaver ssm;

	Vector previousLocations;
	static final int MAX_PREV_LOC = 100;

    TranslucentButton prevPageBt, nextPageBt, firstPageBt, lastPageBt;
    TranslucentTextField pageInfoTf;
    
    static final boolean SHOW_STATUS_BAR = false;
    
    public LRIExplorer(short viewtype, boolean fullscreen, String pth){
        if (pth != null){
            PATH_TO_HIERARCHY = pth;
            PATH_TO_SCENE_DIR = PATH_TO_HIERARCHY + "/lri4z";
            PATH_TO_SCENE = PATH_TO_SCENE_DIR + "/scene.xml";
            PATH_TO_METADATA = PATH_TO_HIERARCHY + "/bibdata.xml";
            SCENE_FILE = new File(PATH_TO_SCENE);
            METADATA_FILE = new File(PATH_TO_METADATA);            
        }
        System.out.println("Loading scene from "+SCENE_FILE.getAbsolutePath());
        initGUI(viewtype, fullscreen);
        RepaintListener rl = new RepaintAdapter(){
            public void viewRepainted(View v){
                setShowLoadProgress(true);
            }
        };
        vsm.repaintNow(mView, rl);
        VirtualSpace[] sceneSpace = {mSpace};
        Camera[] sceneCamera = {mCamera};
        sm = new SceneManager(vsm, sceneSpace, sceneCamera);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        sm.setLevelListener(this);
        sm.setRegionListener(this);
        mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
        sm.loadScene(parseXML(SCENE_FILE), new File(PATH_TO_SCENE_DIR), this);

        Location l = vsm.getGlobalView(mCamera);
        mCamera.setLocation(l);
        sm.updateLevel(l.alt);

//        postProcessLabels();
//        loadMetadata();
        setShowLoadProgress(false);
        TransitionManager.fadeIn(mView, 500, vsm);
        System.gc();
        vsm.centerOnGlyph(sm.getRegion("root").getBounds(), mCamera, 500);
        ssm = new ScreenSaver(this);
    }

    void initGUI(short viewtype, boolean fullscreen){
        windowLayout();
        vsm = new VirtualSpaceManager();
        vsm.setMainFont(MAIN_FONT);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        mnSpace = vsm.addVirtualSpace(mnSpaceName);
        mnCamera = vsm.addCamera(mnSpace);
        ovSpace = vsm.addVirtualSpace(ovSpaceName);
        ovCamera = vsm.addCamera(ovSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(mnCamera);
        cameras.add(ovCamera);
        switch(viewtype){
            case STD_VIEW:{mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, SHOW_STATUS_BAR, false, false, null);break;}
            case OGL_VIEW:{mView = vsm.addExternalView(cameras, mViewName, View.OPENGL_VIEW, VIEW_W, VIEW_H, SHOW_STATUS_BAR, false, false, null);break;}
            default:{mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, SHOW_STATUS_BAR, false, false, null);break;}
        }
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setBlank(BLANK_COLOR);
        eh = new ExplorerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mneh = new MenuEventHandler(this);
        mView.setEventHandler(mneh, 1);
        oveh = new OverlayEventHandler(this);
        mView.setEventHandler(oveh, 2);
        mView.setBackgroundColor(Color.WHITE);
        mView.setNotifyMouseMoved(true);
        vsm.animator.setAnimationListener(eh);
        updatePanelSize();
        mm = new MenuManager(this);
        ovm = new OverlayManager(this);
        mnCamera.setAltitude(0);
        ovCamera.setAltitude(0);
	    previousLocations = new Vector();
	    JFrame f = (JFrame)mView.getFrame();
        JLayeredPane lp = f.getRootPane().getLayeredPane();
        firstPageBt = new TranslucentButton("|<");
        firstPageBt.setFont(MAIN_FONT);
        lp.add(firstPageBt, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        firstPageBt.setBounds(panelWidth/2-200, panelHeight-30, 60, 20);
        prevPageBt = new TranslucentButton("<");
        prevPageBt.setFont(MAIN_FONT);
        lp.add(prevPageBt, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        prevPageBt.setBounds(panelWidth/2-140, panelHeight-30, 60, 20);
        pageInfoTf = new TranslucentTextField(NO_PAGE);
        pageInfoTf.setFont(MAIN_FONT);
        pageInfoTf.setBorder(null);
        pageInfoTf.setEditable(false);
        pageInfoTf.setHorizontalAlignment(TranslucentTextField.CENTER);
        lp.add(pageInfoTf, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        pageInfoTf.setBounds(panelWidth/2-80, panelHeight-30, 140, 20);
        nextPageBt = new TranslucentButton(">");
        lp.add(nextPageBt, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        nextPageBt.setFont(MAIN_FONT);
        nextPageBt.setBounds(panelWidth/2+60, panelHeight-30, 60, 20);
        lastPageBt = new TranslucentButton(">|");
        lp.add(lastPageBt, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        lastPageBt.setFont(MAIN_FONT);
        lastPageBt.setBounds(panelWidth/2+120, panelHeight-30, 60, 20);
        firstPageBt.setVisible(false);
        prevPageBt.setVisible(false);
        pageInfoTf.setVisible(false);
        nextPageBt.setVisible(false);
        lastPageBt.setVisible(false);
        firstPageBt.addActionListener(this);
        prevPageBt.addActionListener(this);
        nextPageBt.addActionListener(this);
        lastPageBt.addActionListener(this);
        MouseListener m0 = new MouseAdapter(){
            public void mouseExited(MouseEvent e){mView.getFrame().requestFocus();}
        };
        firstPageBt.addMouseListener(m0);
        prevPageBt.addMouseListener(m0);
        nextPageBt.addMouseListener(m0);
        lastPageBt.addMouseListener(m0);
	    System.out.println("View size: "+panelWidth+"x"+panelHeight);
        //  	vsm.addGlyph(new com.xerox.VTM.glyphs.VSegment(-10000000L, 0, 0, Color.BLACK, 10000000L, 0), mSpace);
        //  	vsm.addGlyph(new com.xerox.VTM.glyphs.VSegment(0, -10000000L, 0, Color.BLACK, 0, 10000000L), mSpace);
    }

    void windowLayout(){
        if (Utilities.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utilities.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }
    
//    /* make some objects not sensitive to cursor events */
//    void disableObjects(){
//        ObjectDescription od;
//        for (Enumeration e=sm.getObjectIDs();e.hasMoreElements();){
//            od = sm.getObject((String)e.nextElement());
//            od.setSensitive(false);
//        }
//    }
//
//    void disableRegions(){
//        Region r;
//        String id;
//        for (Enumeration e=sm.getRegionIDs();e.hasMoreElements();){
//            sm.getRegion((String)e.nextElement()).setSensitive(false);
//        }
//    }

//    void postProcessLabels(){
//        boolean first = true;
//        for (Enumeration e=sm.getObjectIDs();e.hasMoreElements();){
//            String id = (String)e.nextElement();
//            if (id.startsWith("atomLb-")){
//                // keyword atoms in monospace
//                TextDescription td = (TextDescription)sm.getObject(id);
//                td.setFont(KEYWORD_ATOM_FONT);
//            }
//            else if (id.contains("Fig") && (id.contains("AUA") || id.contains("KWA")) || id.startsWith("authorCAA")){
//                // actions in italic
//                TextDescription td = (TextDescription)sm.getObject(id);
//                td.setFont(ACTION_FONT);
//            }
//        }
//    }
        
    static final int SWITCHING_MODE_MSG_DURATION = 800;
    
    void toggleNavigationMode(){
        if (NAV_MODE >= NAV_MODE_SS){
            NAV_MODE = NAV_MODE_DEFAULT;
        }
        else {
            NAV_MODE++;
            if (NAV_MODE == NAV_MODE_FISHEYE){lensFamily = L2_Gaussian;}
        }
        ovm.say("Navigation Mode: "+NAV_MODES_STR[NAV_MODE], SWITCHING_MODE_MSG_DURATION);
    }
    
    void setNavigationMode(String mode){
        if (mode == null){return;}
        if (mode.equals(NAV_MODE_FISHEYE_STR)){
            NAV_MODE = NAV_MODE_FISHEYE;
	    lensFamily = L2_Gaussian;
        }
        else if (mode.equals(NAV_MODE_SS_STR)){
             NAV_MODE = NAV_MODE_SS;
        }
        else { // NAV_MODE_DEFAULT_STR
            NAV_MODE = NAV_MODE_DEFAULT;
        }
    }
    
    short getNavigationMode(){
        return NAV_MODE;
    }

    void setLens(int t){
        eh.lensType = t;
    }

    Lens getLensDefinition(int x, int y){
        Lens res = null;
        switch (lensFamily){
            case L2_Gaussian:{
                res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                break;
            }
            case L2_Linear:{
                res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                break;
            }
        }
        return res;
    }

    void moveLens(int x, int y){
        lens.setAbsolutePosition(x, y);
        vsm.repaintNow();
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
    }

    void activateLens(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
            lens.getID(), null);
        setLens(ExplorerEventHandler.ZOOMIN_LENS);
    }

    void flattenLens(){
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            lens.getID(), new ZIP2LensAction(this));
    }
    
    synchronized void magnifyFocus(double magOffset){
        synchronized(lens){
            double nmf = MAG_FACTOR + magOffset;
            if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
                setMagFactor(nmf);
                vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
                    lens.getID(), null);
            }
        }
    }

    void disposeOfLens(){
        mView.setLens(null);
	    lens.dispose();
	    setMagFactor(LRIExplorer.DEFAULT_MAG_FACTOR);
	    lens = null;
	    setLens(ExplorerEventHandler.NO_LENS);
    }
    
    void goToRegion(String regionID){
        goToRegion(regionID, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS);
    }

    void goToRegion(String regionID, float focusScaleFactor){
        System.out.println("Going to region "+regionID);
        Region r = sm.getRegion(regionID);
        if (r!=null && r.getBounds()!=null){
			sm.setUpdateLevel(false);
            vsm.centerOnGlyph(r.getBounds(), mCamera, ANIM_MOV_LENGTH, true, focusScaleFactor, new PostAnimationAdapter(){
                public void animationEnded(Object target, short type, String dimension){
                    sm.setUpdateLevel(true);
                    sm.updateLevel(mCamera.altitude);
                }});
        }
    }

    void goToObject(String objectID, boolean preload, Float atAltitude){
        ObjectDescription od = (ObjectDescription)sm.getObject(objectID);
        if (od != null){
            goToObject(od, preload, atAltitude);
        }
    }

    void goToObject(ObjectDescription od, boolean preload, Float atAltitude){
        String id = od.getID();
        System.out.println("Going to object "+id);
        final Glyph g = od.getGlyph();
        // g might be null if in a very different location than the region observed right now
        if (g != null){
            sm.setUpdateLevel(false);
            if (atAltitude != null){
                Vector data = new Vector();
                data.add(new Float(atAltitude-mCamera.getAltitude()));
                data.add(new LongPoint(g.vx-mCamera.posx, g.vy-mCamera.posy));
                vsm.animator.createCameraAnimation(ANIM_MOV_LENGTH, AnimManager.CA_ALT_TRANS_SIG, data, mCamera.getID(), new PostAnimationAdapter(){
                    public void animationEnded(Object target, short type, String dimension){
                        sm.setUpdateLevel(true);
                        sm.updateLevel(mCamera.altitude);
                    }});
            }
            else {
                vsm.centerOnGlyph(g, mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS, new PostAnimationAdapter(){
                    public void animationEnded(Object target, short type, String dimension){
                        vsm.repaintNow(mView, new RepaintAdapter(){
                            public void viewRepainted(View v){
                                adjustAltitude(g);
                            }
                        });}});
            }
            justCenteredOnObject = od;
            updateCurrentPageID(id);
        }
        // in which case we attempt to preload the object
        else if (preload){
            // called from main thread, no need to synchronize with ZUIST engine's GlyphLoader
            od.createObject(mSpace, vsm, false);
            goToObject(od, false, atAltitude);
        }
    }
    
    void adjustAltitude(Glyph g){
        mView.removeRepaintListener();
        vsm.centerOnGlyph(g, mCamera, 100, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS, new PostAnimationAdapter(){
                    public void animationEnded(Object target, short type, String dimension){
                        sm.setUpdateLevel(true);
                        sm.updateLevel(mCamera.altitude);
                    }
        });
    }

	void goHome(){
		rememberLocation(mCamera.getLocation());
		sm.setUpdateLevel(false);
		updateCurrentPageID("");
		vsm.centerOnGlyph(sm.getRegion("root").getBounds(), mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS, new PostAnimationAdapter(){
            public void animationEnded(Object target, short type, String dimension){
                sm.setUpdateLevel(true);
                sm.updateLevel(mCamera.altitude);
            }
            });
	}

	void goUp(){
		rememberLocation(mCamera.getLocation());
		Region r = sm.getClosestRegionAtCurrentLevel(new LongPoint(mCamera.posx, mCamera.posy));
		if (r != null && r.getContainingRegion() != null){
			sm.setUpdateLevel(false);
			updateCurrentPageID("");
			vsm.centerOnGlyph(r.getContainingRegion().getBounds(), mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS, new PostAnimationAdapter(){
                public void animationEnded(Object target, short type, String dimension){
                    sm.setUpdateLevel(true);
                    sm.updateLevel(mCamera.altitude);
                }
                });
		}
	}

    Region justCenteredOnRegion = null;

	void clickedOnRegion(Region r, Glyph g, boolean updateVisibilityWhileMoving){
		System.err.println("Clicked region "+r.getID()+ " level= "+sm.getCurrentLevel());
//		if (sm.getCurrentLevel() == 0 || r.getID().startsWith("ABLr")){
//			rememberLocation(mCamera.getLocation());
//			sm.setUpdateLevel(false);
//			vsm.centerOnGlyph(g, mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS, new PostAnimationAdapter(){
//				public void animationEnded(Object target, short type, String dimension){
//					sm.setUpdateLevel(true);
//					sm.updateLevel(mCamera.altitude);
//				}
//				});
//		}
		if (sm.getCurrentLevel() <= 1){
		    if (sm.getCurrentLevel() == 0 && r.getID().equals("teams") ||
		        sm.getCurrentLevel() == 0 && r.getID().equals("authors")){
		        goToRegion(r.getID());
		    }
		    else {
    		    goHome();
		    }
		}
		else {
			goUp();
		}
	}

    ObjectDescription justCenteredOnObject = null;

    void clickedOnObject(ObjectDescription od, Glyph g, boolean updateVisibilityWhileMoving){
        String id = od.getID();
        System.err.println("Clicked object "+id);
        if (od == justCenteredOnObject){
            // clicked twice on an object, go to the region where it "takesTo"
            String ttID = od.takesTo();
            short ttT = od.takesToType();
            if (ttID != null && ttT == SceneManager.TAKES_TO_REGION){
				rememberLocation(mCamera.getLocation());
                sm.setUpdateLevel(false);
                vsm.centerOnGlyph(sm.getRegion(ttID).getBounds(), mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS,
                new PostAnimationAdapter(){
                    public void animationEnded(Object target, short type, String dimension){
                        sm.setUpdateLevel(true);
                        sm.updateLevel(mCamera.altitude);
                    }
                    });
            }
			justCenteredOnObject = null;
		}
		else {
			rememberLocation(mCamera.getLocation());
			vsm.centerOnGlyph(g, mCamera, ANIM_MOV_LENGTH, true, getFocusScaleFactorForObjectType(od.getID()));
			justCenteredOnObject = od;
			updateCurrentPageID(id);
		}
    }
    
    float DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS = 1.1f;
    float DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS = 1.05f;
    
    float getFocusScaleFactorForObjectType(String type){
//        if (type.startsWith("procFig") ||
//            type.startsWith("au") && type.contains("Fig") ||
//            type.startsWith("atom") && type.contains("-Fig")){
//            // figure objects
//            return 1.5f;
//        }
//        else if (type.startsWith("procPage") ||
//            type.startsWith("au") && type.contains("Page") ||
//            type.startsWith("atom") && type.contains("-Page")){
//            // page objects                
//            return 1.0f;
//        }
//        else {
            return DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS;
//        }
    }
    
    static final float TEAM_CAMERA_ALTITUDE = 40000000.0f;

    static final String TEAM_LABEL_ID_PREFIX = "teamLb";
    static final String CATEGORY_LABEL_ID_PREFIX = "catLb";
    static final String YEAR_LABEL_ID_PREFIX = "yearLb";
    static final String TITLE_LABEL_ID_PREFIX = "titleLb";

    void clickedText(TextDescription td){
        System.err.println("Clicked text "+td.getID());
        String ID = td.getID();
        if (ID.startsWith(CATEGORY_LABEL_ID_PREFIX) || ID.startsWith(YEAR_LABEL_ID_PREFIX)
            || ID.startsWith(TITLE_LABEL_ID_PREFIX) || td == justCenteredOnObject){
            // objects for which we go directly to "takesTo" region, or when
			// clicked twice on the object, go to the region where it "takesTo"
			String ttID = td.takesTo();
			short ttT = td.takesToType();
			if (ttT == SceneManager.TAKES_TO_REGION){
				rememberLocation(mCamera.getLocation());
				goToRegion(ttID);
			}
			else if (ttT == SceneManager.TAKES_TO_OBJECT){
				rememberLocation(mCamera.getLocation());
				goToObject(ttID, true, null);
			}
			justCenteredOnObject = null;
		}
		else {
			rememberLocation(mCamera.getLocation());
            if (ID.startsWith(TEAM_LABEL_ID_PREFIX)){
    			goToObject(td, false, TEAM_CAMERA_ALTITUDE);
            }
            else {
    			goToObject(td, false, null);
            }
			justCenteredOnObject = td;
		}
    }

	void rememberLocation(){
	    rememberLocation(mCamera.getLocation());
    }
    
	void rememberLocation(Location l){
		if (previousLocations.size()>=MAX_PREV_LOC){
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

	void moveBack(){
		if (previousLocations.size()>0){
			Location newlc = (Location)previousLocations.lastElement();
			Location currentlc = mSpace.getCamera(0).getLocation();
			Vector animParams = Location.getDifference(currentlc,newlc);
			sm.setUpdateLevel(false);
			vsm.animator.createCameraAnimation(ANIM_MOV_LENGTH, AnimManager.CA_ALT_TRANS_SIG,
				animParams, mSpace.getCamera(0).getID(),
				new PostAnimationAdapter(){
                    public void animationEnded(Object target, short type, String dimension){
                        sm.setUpdateLevel(true);
                        sm.updateLevel(mCamera.altitude);
                    }
                    });
			previousLocations.removeElementAt(previousLocations.size()-1);
		}
	}

    void altitudeChanged(){
        mCameraAltStr = String.valueOf(mCamera.getAltitude());
	    sm.updateLevel(mCamera.altitude);
    }
    
    public void enteredRegion(Region r){
        updateBreadcrumbs();
    }

    public void exitedRegion(Region r){
        updateBreadcrumbs();
    }
    
    public void enteredLevel(int depth){
        updateBreadcrumbs();
        if (depth == 6){
            showPageNavigation();
        }
    }
    
    /* exiting a level means we have entered another one, taking care of breadcrumb update in there*/
    public void exitedLevel(int depth){
        if (depth == 6){
            hidePageNavigation();
        }
    }
    
    /* --------- Page navigation -------- */
    
    static final String NO_PAGE = "No page to display";
    String currentPageID = null;
    
    void showPageNavigation(){
        firstPageBt.setVisible(true);
        prevPageBt.setVisible(true);
        pageInfoTf.setVisible(true);
        nextPageBt.setVisible(true);
        lastPageBt.setVisible(true);
    }
    
    void hidePageNavigation(){
        firstPageBt.setVisible(false);
        prevPageBt.setVisible(false);
        pageInfoTf.setVisible(false);
        nextPageBt.setVisible(false);
        lastPageBt.setVisible(false);
        mView.getFrame().requestFocus();
    }
    
    public void actionPerformed(ActionEvent e){
        if (e.getSource() == prevPageBt){
            goToPreviousPage();
        }
        else if (e.getSource() == nextPageBt){
            goToNextPage();
        }
        else if (e.getSource() == lastPageBt){
            goToLastPage();
        }
        else if (e.getSource() == firstPageBt){
            goToFirstPage();
        }
    }
    
    int getPage(String pageID){
        return Integer.parseInt(pageID.substring(pageID.indexOf("pages_p")+7, pageID.indexOf("_", pageID.indexOf("pages_p")+7)));
    }
    
    int getPageCount(String pageID){
        return Integer.parseInt(pageID.substring(pageID.lastIndexOf("_")+1));
    }
    
    String getIdWithoutPage(String pageID){
        return pageID.substring(0, pageID.indexOf("pages_p")+7);
    }
    
    void goToPreviousPage(){
        if (currentPageID != null){
            int pp = getPage(currentPageID);
            int pc = getPageCount(currentPageID);
            if (pp > 1){
                goToObject(getIdWithoutPage(currentPageID)+(pp-1)+"_"+(pc), true, null);
            }
        }
    }
    
    void goToNextPage(){
        if (currentPageID != null){
            int pp = getPage(currentPageID);
            int pc = getPageCount(currentPageID);
            // check that object exists is done in goToObject()
            // (page might not exist, we don't know how many pages the document contains)
            goToObject(getIdWithoutPage(currentPageID)+(pp+1)+"_"+(pc), true, null);
        }        
    }

    void goToFirstPage(){
        if (currentPageID != null){
            int pc = getPageCount(currentPageID);
            goToObject(getIdWithoutPage(currentPageID)+"1_"+(pc), true, null);
        }
    }

    void goToLastPage(){
        if (currentPageID != null){
            int pc = getPageCount(currentPageID);
            goToObject(getIdWithoutPage(currentPageID)+(pc)+"_"+(pc), true, null);
        }
    }
        
    void updateCurrentPageID(String id){
        currentPageID = (id.contains("pages_p")) ? id : null;
        if (currentPageID != null){
            pageInfoTf.setText("Page "+getPage(currentPageID)+" of "+getPageCount(currentPageID)+" ");
        }
        else {
            pageInfoTf.setText(NO_PAGE);
        }
    }
    
    /* ---------- Breadcrumbs ------------ */
    
    String breadcrumbs = EMPTY_STRING;
    static final String BREADCRUMBS_SEPARATOR = " > ";
    static final String EMPTY_STRING = "";
    static final Color BREADCRUMB_COLOR = Color.WHITE;
    
    void updateBreadcrumbs(){
//        Region r = sm.getClosestRegionAtCurrentLevel(new LongPoint(mCamera.posx, mCamera.posy));
//        if (r != null){
//            Vector containingRegions = r.getContainingRegions(new Vector());
//            breadcrumbs = EMPTY_STRING;
//            String s;
//            for (int i=0;i<containingRegions.size();i++){
//                s = ((Region)containingRegions.elementAt(i)).getTitle();
//                if (s != null){
//                    breadcrumbs = breadcrumbs.concat(BREADCRUMBS_SEPARATOR + s);
//                }
//            }
//        }
//        else {
//            breadcrumbs = EMPTY_STRING;
//        }
    }
    
    void showBreadcrumbs(Graphics2D g2d, int viewWidth, int viewHeight){
        g2d.setColor(BREADCRUMB_COLOR);
        g2d.drawString(breadcrumbs, MenuManager.MENU_ZONE_WIDTH+MenuManager.MENU_TITLE_HOFFSET, 14);
    }

    /* ---------- Graphics ------------ */

    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        try {
            mm.layoutMenuItems();
        }
        catch (NullPointerException ex){}
    }

    void toggleMemoryUsageDisplay(){
	    SHOW_MEMORY_USAGE = !SHOW_MEMORY_USAGE;
	    vsm.repaintNow();
    }
    
    boolean antialiasing = false;
    
    void toggleAntialiasing(){
	    antialiasing = !antialiasing;
	    mView.setAntialiasing(antialiasing);
	    vsm.repaintNow();
    }

    void gc(){
        System.gc();
        if (SHOW_MEMORY_USAGE){
            vsm.repaintNow();
        }
    }
    
    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;

    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (SHOW_MEMORY_USAGE){showMemoryUsage(g2d, viewWidth, viewHeight);}
        if (SHOW_LOAD_PROGRESS){showLoadProgress(g2d, viewWidth, viewHeight);}
        if (breadcrumbs.length() > 0){showBreadcrumbs(g2d, viewWidth, viewHeight);}
        if (SHOW_REQ_QUEUE_STATUS){showReqQueueStatus(g2d, viewWidth, viewHeight);}
        g2d.setFont(ZVTM_LOGO_FONT);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setComposite(acST);
        g2d.drawString(ZVTM_LOGO_TEXT, panelWidth-200, panelHeight+3);
        g2d.setComposite(Translucent.acO);
        g2d.setFont(MAIN_FONT);
    }

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
        totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
        usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
        g2d.setColor(Color.green);
        g2d.fillRect(20,
            viewHeight - 40,
            200,
            15);
        g2d.setColor(Color.orange);
        g2d.fillRect(20,
            viewHeight - 40,
            totalMemRatio * 2,
            15);
        g2d.setColor(Color.red);
        g2d.fillRect(20,
            viewHeight - 40,
            usedMemRatio * 2,
            15);
        g2d.setColor(Color.black);
        g2d.drawRect(20,
            viewHeight - 40,
            200,
            15);
        g2d.drawString(usedMemRatio + "%", 50, viewHeight - 28);
        g2d.drawString(totalMemRatio + "%", 100, viewHeight - 28);
        g2d.drawString(maxMem/1048576 + " Mb", 170, viewHeight - 28);
        g2d.drawString(mCameraAltStr, 250, viewHeight - 28);
    }
    
    // consider 1000 as the maximum number of requests that can be in the queue at any given time
    static final float MAX_NB_REQUESTS = 2000;
    static final int REQ_QUEUE_BAR_WIDTH = 100;
    static final int REQ_QUEUE_BAR_HEIGHT = 6;
    
    void showReqQueueStatus(Graphics2D g2d, int viewWidth, int viewHeight){
        float ratio = sm.getPendingRequestQueueSize()/(MAX_NB_REQUESTS);
        if (ratio > 1.0f){
            // do not go over gauge boundary, even if actual number of requests goes beyond MAX_NB_REQUESTS
            ratio = 1.0f;
        }
        g2d.setColor(Color.WHITE);
        g2d.fillRect(viewWidth-Math.round(REQ_QUEUE_BAR_WIDTH * ratio)-10, 7, Math.round(REQ_QUEUE_BAR_WIDTH * ratio), REQ_QUEUE_BAR_HEIGHT);
        g2d.drawRect(viewWidth-REQ_QUEUE_BAR_WIDTH-10, 7, REQ_QUEUE_BAR_WIDTH, REQ_QUEUE_BAR_HEIGHT);
    }

    static final int PROGRESS_BAR_WIDTH = 400;
    static final int PROGRESS_BAR_HEIGHT = 10;
    
    void setShowLoadProgress(boolean b){
        SHOW_LOAD_PROGRESS = b;
        if (b){
            mView.removeRepaintListener();
        }
    }

    void showLoadProgress(Graphics2D g2d, int viewWidth, int viewHeight){
        g2d.setColor(Color.LIGHT_GRAY);
        if (progressValue > 0){
            g2d.fillRect(viewWidth/2-PROGRESS_BAR_WIDTH/2, viewHeight/2-PROGRESS_BAR_HEIGHT,
                Math.round(progressValue*PROGRESS_BAR_WIDTH/100.0f), PROGRESS_BAR_HEIGHT);
        }
        g2d.drawRect(viewWidth/2-PROGRESS_BAR_WIDTH/2, viewHeight/2-PROGRESS_BAR_HEIGHT,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        if (progressLabel.length() > 0){
            g2d.drawString(progressLabel, viewWidth/2-PROGRESS_BAR_WIDTH/2, viewHeight/2-20);
        }
        if (progressHistory.length() > 0){
            g2d.drawString(progressHistory, viewWidth/2-PROGRESS_BAR_WIDTH/2, viewHeight/2-35);
        }
    }

    String progressLabel = EMPTY_STRING;
    String progressHistory = EMPTY_STRING;
    int progressValue = 0;

    public void setLabel(String s){
        if (!progressLabel.equals(s)){
            progressLabel = s;
            vsm.repaintNow();
        }
    }

    public void setValue(int i){
        if (i != progressValue){
            progressValue = i;
            vsm.repaintNow();
        }
    }

    static Document parseXML(File f){ 
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document res = builder.parse(f);
            return res;
        }
        catch (FactoryConfigurationError e){e.printStackTrace();return null;}
        catch (ParserConfigurationException e){e.printStackTrace();return null;}
        catch (SAXException e){e.printStackTrace();return null;}
        catch (IOException e){e.printStackTrace();return null;}
    }
    
    void about(){
        ovm.showAbout();
    }
    
    void exit(){
        System.exit(0);
    }

	/* vt: view type 0 (standard) or 1 (opengl)
	   fs: true/false fullscreen
	 */
    public static void main(String[] args){
        final short vt = (args.length > 0) ? Short.parseShort(args[0]) : 0;
        final boolean fs = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;
        new LRIExplorer(vt, fs, (args.length > 2) ? args[2] : null);
    }
    
}

class AltitudeAdjuster implements RepaintListener {

    LRIExplorer application;

    AltitudeAdjuster(LRIExplorer app){
        this.application = app;
    }
    
    public void viewRepainted(View v){
        
    }
    
}
