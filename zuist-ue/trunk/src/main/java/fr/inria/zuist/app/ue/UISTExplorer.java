/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.ue;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.AlphaComposite;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
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

public class UISTExplorer implements Java2DPainter, ProgressListener, LevelListener, RegionListener {

    static final String PATH_TO_HIERARCHY = "data/uist4z";
    static final String PATH_TO_SCENE = PATH_TO_HIERARCHY + "/scene.xml";
    static final String PATH_TO_METADATA = PATH_TO_HIERARCHY + "/UISTmetadata.xml";
    static final File PROCEEDINGS_DIR = new File(PATH_TO_HIERARCHY);
    static final File SCENE_FILE = new File(PATH_TO_SCENE);
    static final File METADATA_FILE = new File(PATH_TO_METADATA);

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Proceedings Space";
    static final String mnSpaceName = "Menu Space";
    static final String ovSpaceName = "Overlay Space";
    VirtualSpace mSpace, mnSpace, ovSpace;
    Camera mCamera, mnCamera, ovCamera;
    static final String mViewName = "ZUIST";
    View mView;
    ExplorerEventHandler eh;
    MenuEventHandler mneh;
    OverlayEventHandler oveh;

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 120;
    static int LENS_R2 = 60;
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
    static final short L2_HLinear = 6;
    short lensFamily = L2_HLinear;

    static final float FLOOR_ALTITUDE = 0.0f;

    static final Float AUTHOR_CAMERA_ALTITUDE = new Float(220000.0f);

	static final int ANIM_MOV_LENGTH = 300;

    static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 10);
    static final Font ZVTM_LOGO_FONT = new Font("Century Gothic", Font.PLAIN, 40);
    static final Font KEYWORD_ATOM_FONT = new Font("Courier", Font.PLAIN, 10);
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
    static final String NAV_MODE_FISHEYE2_STR = "Pyramid Lens";
    static final String NAV_MODE_SS_STR = "Screen Saver";
    static final String[] NAV_MODES_STR = {NAV_MODE_DEFAULT_STR, NAV_MODE_FISHEYE_STR,
         NAV_MODE_FISHEYE2_STR, NAV_MODE_SS_STR};
    static final short NAV_MODE_DEFAULT = 0;  // must be 0 (or change toggleNavigationMode())
    static final short NAV_MODE_FISHEYE = 1;
    static final short NAV_MODE_FISHEYE2 = 2;
    static final short NAV_MODE_SS = 3;       // must be highest value (or change toggleNavigationMode())
    short NAV_MODE = NAV_MODE_DEFAULT;

    MenuManager mm;
    OverlayManager ovm;
    ScreenSaver ssm;

	Vector previousLocations;
	static final int MAX_PREV_LOC = 100;
    
    static final boolean SHOW_STATUS_BAR = false;
    
    static final String LOG_FILE_EXT = ".csv";
    static final String OUTPUT_CSV_SEP = "\t";
    static final String LOG_DIR = "logs";
    static final String LOG_DIR_FULL = System.getProperty("user.dir") + File.separator + LOG_DIR;
    File logFile;
    BufferedWriter bwl;
    boolean loggingEnabled = true;

    public UISTExplorer(short viewtype, boolean fullscreen, boolean le){
        initGUI(viewtype, fullscreen);
        this.loggingEnabled = le;
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
        sm.loadScene(parseXML(SCENE_FILE), new File(PATH_TO_HIERARCHY), this);

        Location l = vsm.getGlobalView(mCamera);
        mCamera.setLocation(l);
        sm.updateLevel(l.alt);

        postProcessLabels();
        loadMetadata();
        setShowLoadProgress(false);
        TransitionManager.fadeIn(mView, 500, vsm);
        System.gc();
        vsm.centerOnGlyph(sm.getRegion("root").getBounds(), mCamera, 500);
        ssm = new ScreenSaver(this);
        if (loggingEnabled){
            initLog();
        }
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
    
    void initLog(){
        logFile = initLogFile("zuist_log", LOG_DIR);
    	try {
    	    bwl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
    	}
    	catch(IOException ex){ex.printStackTrace();}
    }

    /* make some objects not sensitive to cursor events */
    void disableObjects(){
        ObjectDescription od;
        for (Enumeration e=sm.getObjectIDs();e.hasMoreElements();){
            od = sm.getObject((String)e.nextElement());
            od.setSensitive(false);
        }
    }

    void disableRegions(){
        Region r;
        String id;
        for (Enumeration e=sm.getRegionIDs();e.hasMoreElements();){
            sm.getRegion((String)e.nextElement()).setSensitive(false);
        }
    }

    void postProcessLabels(){
        boolean first = true;
        for (Enumeration e=sm.getObjectIDs();e.hasMoreElements();){
            String id = (String)e.nextElement();
            if (id.startsWith("atomLb-")){
                // keyword atoms in monospace
                TextDescription td = (TextDescription)sm.getObject(id);
                td.setFont(KEYWORD_ATOM_FONT);
            }
            else if (id.contains("Fig") && (id.contains("AUA") || id.contains("KWA")) || id.startsWith("authorCAA")){
                // actions in italic
                TextDescription td = (TextDescription)sm.getObject(id);
                td.setFont(ACTION_FONT);
            }
        }
    }
    
    /* keyword ID -> keyword expression*/
    Hashtable id2keyword = new Hashtable();
    /* atom label -> String[] keyword IDs using that atom*/
    Hashtable atom2keywords = new Hashtable();
    /* author ID -> author name */
    Hashtable id2author = new Hashtable();
    /* author ID -> String[] coauthor IDs */
    /* authors without coauthors are not in the table */
    Hashtable author2coauthors = new Hashtable();
    /* paper ID (YEARp1PG) -> PaperInfo */
    Hashtable paper2paperinfo = new Hashtable();
    /* kwID -> one of possibly several regions associated with this keyword expression (region ID) */
    Hashtable keyword2regionid = new Hashtable();

    void loadMetadata(){
        setLabel("Loading metadata...");
        setValue(0);
        progressHistory = "Processed "+sm.getObjectCount()+" objects in "+sm.getRegionCount()+" regions on "+sm.getLevelCount()+" levels";
        Document d = parseXML(METADATA_FILE);
        Element root = d.getDocumentElement();
        NodeList nl = ((Element)((Element)root.getElementsByTagName("allKeywords").item(0)).getElementsByTagName("keywords").item(0)).getElementsByTagName("keyword");
        Node n;
        for (int i=0;i<nl.getLength();i++){
    	    n = nl.item(i);
            id2keyword.put(((Element)n).getAttribute("id"), n.getFirstChild().getNodeValue());
            if (i % 100 == 0){setValue(Math.round(20*i/((float)nl.getLength())));}
        }
    	nl = ((Element)((Element)root.getElementsByTagName("allKeywords").item(0)).getElementsByTagName("keywordAtoms").item(0)).getElementsByTagName("keywordAtom");
        for (int i=0;i<nl.getLength();i++){
    	    n = nl.item(i);
            atom2keywords.put(n.getFirstChild().getNodeValue(), ((Element)n).getAttribute("idrefs").split(","));
            if (i % 100 == 0){setValue(Math.round(20+20*i/((float)nl.getLength())));}
        }
        Element e1,e2;
        nl = ((Element)root.getElementsByTagName("allAuthors").item(0)).getElementsByTagName("author");
        NodeList nl2;
        String auID;
        for (int i=0;i<nl.getLength();i++){
    	    e1 = (Element)nl.item(i);
    	    auID = e1.getAttribute("id");
            id2author.put(auID, e1.getElementsByTagName("canonicalName").item(0).getFirstChild().getNodeValue());
            nl2 = e1.getElementsByTagName("coauthor");
            if (nl2.getLength() > 0){
                String[] coauthors = new String[nl2.getLength()];
                for (int j=0;j<nl2.getLength();j++){
                    coauthors[j] = ((Element)nl2.item(j)).getAttribute("idref");
                }
                author2coauthors.put(auID, coauthors);
            }
            if (i % 100 == 0){setValue(Math.round(40+20*i/((float)nl.getLength())));}
        }
        nl = ((Element)root.getElementsByTagName("allProceedings").item(0)).getElementsByTagName("proceedings");
        String pID;
        for (int i=0;i<nl.getLength();i++){
            e1 = (Element)nl.item(i);
            nl2 = e1.getElementsByTagName("article");
            for (int j=0;j<nl2.getLength();j++){
                e2 = (Element)nl2.item(j);
                pID = e2.getAttribute("id");
                paper2paperinfo.put(pID.substring(0,4) + "p" +pID.substring(6, pID.indexOf("-")), new PaperInfo(e2));
            }
        }
        // identify one of possibly several region that contain papers associated with a keyword expression
        // in the By keyword subtree, so that we can get there from the list of keywords associated with a paper
        // from another part of the hierarchy
        // not really metadata parsing, but close...
        String kwID, regionID;
        for (Enumeration e=sm.getRegionIDs();e.hasMoreElements();){
            regionID = (String)e.nextElement();
            if (regionID.startsWith("atom") && regionID.contains("kw")){
                kwID = regionID.substring(regionID.indexOf("kw"));
                if (!keyword2regionid.containsKey(kwID)){
                    keyword2regionid.put(kwID, regionID);
                }
            }
        }
    }
    
    static final int SWITCHING_MODE_MSG_DURATION = 800;
    
    void toggleNavigationMode(){
        if (NAV_MODE >= NAV_MODE_SS){
            NAV_MODE = NAV_MODE_DEFAULT;
        }
        else {
            NAV_MODE++;
            if (NAV_MODE == NAV_MODE_FISHEYE){lensFamily = L2_HLinear;}
            else if (NAV_MODE == NAV_MODE_FISHEYE2){lensFamily = LInf_Linear;}
        }
        ovm.say("Navigation Mode: "+NAV_MODES_STR[NAV_MODE], SWITCHING_MODE_MSG_DURATION);
    }
    
    void setNavigationMode(String mode){
        if (mode == null){return;}
        if (mode.equals(NAV_MODE_FISHEYE_STR)){
            NAV_MODE = NAV_MODE_FISHEYE;
	    lensFamily = L2_Linear;
        }
        else if (mode.equals(NAV_MODE_FISHEYE2_STR)){
            NAV_MODE = NAV_MODE_FISHEYE2;
	    lensFamily = LInf_Linear;
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
            case LInf_Linear:{
                res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                break;
            }
            case LInf_Manhattan:{
                res = new LInfFSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                break;
            }
            case L2_HLinear:{
                res = new HLinearLens(1.0f, 0.4f, 1.0f, 300, 150, x - panelWidth/2, y - panelHeight/2);
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
	    setMagFactor(UISTExplorer.DEFAULT_MAG_FACTOR);
	    lens = null;
	    setLens(ExplorerEventHandler.NO_LENS);
    }
    
    void goToRegion(String regionID){
        goToRegion(regionID, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS);
    }

    void goToRegion(String regionID, float focusScaleFactor){
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
        System.out.println("going to "+od.getID());
        
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

		if (sm.getCurrentLevel() == 0 || r.getID().startsWith("ABLr")){
			rememberLocation(mCamera.getLocation());
			sm.setUpdateLevel(false);
			vsm.centerOnGlyph(g, mCamera, ANIM_MOV_LENGTH, true, DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS, new PostAnimationAdapter(){
				public void animationEnded(Object target, short type, String dimension){
					sm.setUpdateLevel(true);
					sm.updateLevel(mCamera.altitude);
				}
				});
		}
		else if (sm.getCurrentLevel() == 1 && r.getID().equals("root")){
		    goHome();
		}
		else {
			goUp();
		}
	}

    ObjectDescription justCenteredOnObject = null;

    void clickedOnObject(ObjectDescription od, Glyph g, boolean updateVisibilityWhileMoving){
        System.err.println("Clicked object "+od.getID());
        if (od == justCenteredOnObject){
            // clicked twice on an object, go to the region where it "takesTo"
            String ttID = od.takesTo();
            short ttT = od.takesToType();
            System.out.println(ttID+"  [[]]  "+ttT);
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
		}
    }
    
    float DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS = 1.1f;
    float DEFAULT_FOCUS_SCALE_FACTOR_FOR_REGIONS = 1.05f;
    
    float getFocusScaleFactorForObjectType(String type){
        if (type.startsWith("procFig") ||
            type.startsWith("au") && type.contains("Fig") ||
            type.startsWith("atom") && type.contains("-Fig")){
            // figure objects
            return 1.5f;
        }
        else if (type.startsWith("procPage") ||
            type.startsWith("au") && type.contains("Page") ||
            type.startsWith("atom") && type.contains("-Page")){
            // page objects                
            return 1.0f;
        }
        else {
            return DEFAULT_FOCUS_SCALE_FACTOR_FOR_OBJECTS;
        }
    }

    void clickedText(TextDescription td){
        String ID = td.getID();
        System.out.println("Clicked text "+ID);
        if (ID.startsWith("atomLb-")){
            String[] kwIDs = (String[])atom2keywords.get(td.getText());
            String[] keywords = new String[kwIDs.length];
            String[] IDs = new String[kwIDs.length];
            // 7 = len(atomLb-)
            String atomID = ID.substring(7);
            for (int i=0;i<kwIDs.length;i++){
                keywords[i] = (String)id2keyword.get(kwIDs[i]);
                // append R in front to specify that we will be going to a region
                IDs[i] = "Ratom" + atomID + kwIDs[i];
            }
            ovm.displayLinksP1(keywords, IDs, td.getText());
        }
		else if (ID.startsWith("authorLb")){
			if (td == justCenteredOnObject){
				// clicked twice on an object, go to the region where it "takesTo"
				String ttID = td.takesTo();
				short ttT = td.takesToType();
				System.out.println(ttID+"  [[]]  "+ttT);
				if (ttT == SceneManager.TAKES_TO_REGION){
					rememberLocation(mCamera.getLocation());
					goToRegion(ttID);					
				}
				justCenteredOnObject = null;
			}
			else {
				rememberLocation(mCamera.getLocation());
				goToObject(td, false, AUTHOR_CAMERA_ALTITUDE);
				justCenteredOnObject = td;
			}
		}
		else if (ID.startsWith("ABLo")){
			String ttID = td.takesTo();
			short ttT = td.takesToType();
			if (ttT == SceneManager.TAKES_TO_REGION){
				rememberLocation(mCamera.getLocation());
                goToRegion(ttID);
			}
		}
		else if (ID.startsWith("authorCAA")){
            // 10 = len(authorCAA-)
            String authorID = ID.substring(10);
            String[] auIDs = (String[])author2coauthors.get(authorID);
            if (auIDs != null){
                String[] coauthors = new String[auIDs.length];
                String[] IDs = new String[auIDs.length];
                for (int i=0;i<auIDs.length;i++){
                    coauthors[i] = (String)id2author.get(auIDs[i]);
                    // append O in front to specify that we will be going to an object
                    IDs[i] = "OauthorLb-"+auIDs[i];
                }
                ovm.displayLinksP1(coauthors, IDs, "Coauthors of " + (String)id2author.get(authorID));
            }
        }
		else if (ID.contains("Fig") && ID.contains("AUA")){
		    int fi = ID.indexOf("Fig");
            String year = ID.substring(fi+3,fi+7);
            String firstPage = ID.substring(fi+8, ID.indexOf("AUA"));
            String[] auIDs = ((PaperInfo)paper2paperinfo.get(year+"p"+firstPage)).authorIDs;
            String[] authors = new String[auIDs.length];
            String[] IDs = new String[auIDs.length];
            for (int i=0;i<auIDs.length;i++){
                authors[i] = (String)id2author.get(auIDs[i]);
                // append O in front to specify that we will be going to an object
                IDs[i] = "OauthorLb-"+auIDs[i];
                
            }
            ovm.displayLinksP1(authors, IDs, "Authors");
        }
		else if (ID.contains("Fig") && ID.contains("KWA")){
		    int fi = ID.indexOf("Fig");
            String year = ID.substring(fi+3,fi+7);
            String firstPage = ID.substring(fi+8, ID.indexOf("KWA"));
            String[] kwIDs = ((PaperInfo)paper2paperinfo.get(year+"p"+firstPage)).keywordIDs;
            if (kwIDs != null){
                String[] keywords = new String[kwIDs.length];
                String[] IDs = new String[kwIDs.length];
                String rID;
                for (int i=0;i<kwIDs.length;i++){
                    keywords[i] = (String)id2keyword.get(kwIDs[i]);
                    // append R in front to specify that we will be going to a region
                    rID = (String)keyword2regionid.get(kwIDs[i]);
                    IDs[i] = (rID != null) ? "R"+rID : null;
                }
                ovm.displayLinksP1(keywords, IDs, "Keywords");
            }
        }
		else if (ID.startsWith("yearLb-")){
			String ttID = td.takesTo();
			short ttT = td.takesToType();
			if (ttT == SceneManager.TAKES_TO_REGION){
				rememberLocation(mCamera.getLocation());
                goToRegion(ttID);
			}
		}
		else if (ID.equals("aboutLb")){
            mView.setActiveLayer(2);
			about();
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
                if (loggingEnabled){
                    logLocation(l);
                }
            }
		}
		else {previousLocations.add(l);}
	}
	
    void logLocation(Location l){
        try {
            bwl.write(l.vx + OUTPUT_CSV_SEP + l.vy + OUTPUT_CSV_SEP + l.alt);
            bwl.newLine();
            bwl.flush();
        }
        catch (Exception ex){ex.printStackTrace();}
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

    float getAltitudeDelta(Region r){
	if (r.getID().startsWith("paper-")){
	    return -0.5f;
	}
	else {
	    return -0.25f;
	}
    }

    void altitudeChanged(){
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
    }
    
    /* exiting a level means we have entered another one, taking care of breadcrumb update in there*/
    public void exitedLevel(int depth){}
    
    String breadcrumbs = EMPTY_STRING;
    static final String BREADCRUMBS_SEPARATOR = " > ";
    static final String EMPTY_STRING = "";
    static final Color BREADCRUMB_COLOR = Color.WHITE;
    
    void updateBreadcrumbs(){
        Region r = sm.getClosestRegionAtCurrentLevel(new LongPoint(mCamera.posx, mCamera.posy));
        if (r != null){
            Vector containingRegions = r.getContainingRegions(new Vector());
            breadcrumbs = EMPTY_STRING;
            String s;
            for (int i=0;i<containingRegions.size();i++){
                s = ((Region)containingRegions.elementAt(i)).getTitle();
                if (s != null){
                    breadcrumbs = breadcrumbs.concat(BREADCRUMBS_SEPARATOR + s);
                }
            }
        }
        else {
            breadcrumbs = EMPTY_STRING;
        }
    }
    
    void showBreadcrumbs(Graphics2D g2d, int viewWidth, int viewHeight){
        g2d.setColor(BREADCRUMB_COLOR);
        g2d.drawString(breadcrumbs, MenuManager.MENU_ZONE_WIDTH+MenuManager.MENU_TITLE_HOFFSET, 14);
    }

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

    static File initLogFile(String fileName, String dirName){
        String outputFile = dirName + File.separator + fileName + LOG_FILE_EXT;
        File file = new File(outputFile);
        int i = 0;
        while (file.exists()){
            i++;
            file = new File(outputFile.substring(0,outputFile.length()-4) + "-" + i + LOG_FILE_EXT);
        }
        return file;
    }
    
    void about(){
        ovm.showAbout();
    }
    
    void exit(){
        if (bwl != null){
            try{
                bwl.flush();
                bwl.close();   
            }
            catch(Exception ex){ex.printStackTrace();}
        }
        System.exit(0);
    }

	/* vt: view type 0 (standard) or 1 (opengl)
	   fs: true/false fullscreen
	   le: true/false logging
	 */
    public static void main(String[] args){
        final short vt = (args.length > 0) ? Short.parseShort(args[0]) : 0;
        final boolean fs = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;
        final boolean le = (args.length > 2) ? Boolean.parseBoolean(args[2]) : false;
        new UISTExplorer(vt, fs, le);
    }
    
}

class AltitudeAdjuster implements RepaintListener {

    UISTExplorer application;

    AltitudeAdjuster(UISTExplorer app){
        this.application = app;
    }
    
    public void viewRepainted(View v){
        
    }
    
}
