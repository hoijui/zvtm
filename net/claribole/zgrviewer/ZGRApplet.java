/*   FILE: ZGRApplet.java
 *   DATE OF CREATION:   Fri May 09 09:52:34 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zgrviewer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;

import java.util.Vector;

import java.net.URL;
import java.net.MalformedURLException;

import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.RepaintListener;

import org.w3c.dom.Document;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.svg.SVGReader;


public class ZGRApplet extends JApplet implements MouseListener, KeyListener, ZGRApplication, RepaintListener {

    static final int DEFAULT_VIEW_WIDTH = 640;
    static final int DEFAULT_VIEW_HEIGHT = 480;
    static final String APPLET_WIDTH_PARAM = "width";
    static final String APPLET_HEIGHT_PARAM = "height";
    static final String SVG_FILE_URL_PARAM = "svgURL";
    static final String SHOW_NAVIGATION_CONTROLS_PARAM = "showNavControls";
    static final String APPLET_TITLE_PARAM = "title";
    static final String APPLET_BKG_COLOR_PARAM = "appletBackgroundColor";
    static final String GRAPH_BKG_COLOR_PARAM = "graphBackgroundColor";
    static final String CURSOR_COLOR_PARAM = "cursorColor";
    static final String CENTER_ON_LABEL_PARAM = "centerOnLabel";


    String APPLET_TITLE = "ZGRViewer - Applet";

    static ConfigManager cfgMngr;

    public GVLoader gvLdr;
    public GraphicsManager grMngr;

    ZgrAppletEvtHdlr meh;

    JPanel viewPanel;
    NavPanel navPanel;
    JLabel statusBar;

    int appletWindowWidth = DEFAULT_VIEW_WIDTH;
    int appletWindowHeight = DEFAULT_VIEW_HEIGHT;

    public ZGRApplet(){
	getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
    }

    public void init(){
	initConfig();
	initGUI();
    }

    void initConfig(){
	grMngr = new GraphicsManager(this);
	cfgMngr = new ConfigManager(grMngr, true);
	grMngr.setConfigManager(cfgMngr);
	gvLdr = new GVLoader(this, grMngr, cfgMngr, null);
    }

    void initGUI(){
	this.addKeyListener(this);
	this.addMouseListener(this);
	// get width and height of applet panel
	try {appletWindowWidth = Integer.parseInt(getParameter(APPLET_WIDTH_PARAM));}
	catch(NumberFormatException ex){appletWindowWidth = DEFAULT_VIEW_WIDTH;}
	try {appletWindowHeight = Integer.parseInt(getParameter(APPLET_HEIGHT_PARAM));}
	catch(NumberFormatException ex){appletWindowHeight = DEFAULT_VIEW_HEIGHT;}
	// should the navigation control panel be displayed or not
	boolean showNavControl = false;
	try {
	    showNavControl = (new Boolean(getParameter(SHOW_NAVIGATION_CONTROLS_PARAM))).booleanValue();
	}
	catch(Exception ex){}
	try {
	    APPLET_TITLE = getParameter(APPLET_TITLE_PARAM);
	}
	catch(Exception ex){APPLET_TITLE = "ZGRViewer - Applet";}
	Color APPLET_BKG_COLOR = null;
	try {
	    APPLET_BKG_COLOR = SVGReader.getColor(getParameter(APPLET_BKG_COLOR_PARAM));
	}
	catch(Exception ex){}
	boolean graphBkgColorSpecified = false;
	try {
	    ConfigManager.backgroundColor = SVGReader.getColor(getParameter(GRAPH_BKG_COLOR_PARAM));
	    graphBkgColorSpecified = true;
	}
	catch(Exception ex){
	    ConfigManager.backgroundColor = Color.WHITE;
	    graphBkgColorSpecified = false;
	}
	final boolean graphBkgColorSpecifiedF = graphBkgColorSpecified;
	String centerOnLabel = null;
	try {
	    centerOnLabel = getParameter(CENTER_ON_LABEL_PARAM);
	}
	catch (Exception ex){
	    centerOnLabel = null;
	}
	final String centerOnLabelF = centerOnLabel;
	AppletUtils.initLookAndFeel();
	Container cpane = getContentPane();
	this.setSize(appletWindowWidth-10, appletWindowHeight-10);
	cpane.setSize(appletWindowWidth, appletWindowHeight);
	cpane.setBackground(APPLET_BKG_COLOR);
	viewPanel = grMngr.createPanelView(grMngr.createZVTMelements(true), appletWindowWidth, appletWindowHeight-40);
	meh = new ZgrAppletEvtHdlr(this, this.grMngr);
	grMngr.parameterizeView(meh);
 	viewPanel.setPreferredSize(new Dimension(appletWindowWidth-10, appletWindowHeight-40));
	statusBar = new JLabel(Messages.LOADING_SVG);
	JPanel borderPanel = new JPanel();
	borderPanel.setLayout(new BorderLayout());
	borderPanel.add(viewPanel, BorderLayout.CENTER);
	borderPanel.add(statusBar, BorderLayout.SOUTH);
	borderPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black,2), APPLET_TITLE));
	borderPanel.setOpaque(false);
	if (showNavControl){
	    GridBagLayout gridBag = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.CENTER;
	    cpane.setLayout(gridBag);
	    buildConstraints(constraints,0,0,1,1,90,100);
	    gridBag.setConstraints(borderPanel, constraints);
	    cpane.add(borderPanel);
	    navPanel = new NavPanel(grMngr);
	    buildConstraints(constraints,1,0,1,1,10,0);
	    gridBag.setConstraints(navPanel, constraints);
	    cpane.add(navPanel);
	}
	else {
	    cpane.add(borderPanel);
	}
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    sleep(1000);
		    requestFocus();
		    grMngr.vsm.repaintNow();
		    gvLdr.loadSVG(getParameter(SVG_FILE_URL_PARAM));
		    // override SVG's background color if background color is specified in applet params
		    if (graphBkgColorSpecifiedF){grMngr.mainView.setBackgroundColor(ConfigManager.backgroundColor);}
		    setStatusBarText(Messages.EMPTY_STRING);
		    grMngr.tp.updateHiddenPosition();
		    grMngr.vsm.repaintNow(grMngr.mainView, ZGRApplet.this);
		    while (!ZGRApplet.this.paintedAtLeastOnce){
			sleep(500);
		    }
		    if (centerOnLabelF != null){
			grMngr.search(centerOnLabelF, 1);
		    }
		    grMngr.vsm.repaintNow();
		    return null; 
		}
	    };
	worker.start();
    }

    boolean paintedAtLeastOnce = false;

    public void viewRepainted(View v){
	paintedAtLeastOnce = true;
	v.removeRepaintListener();
    }

    public void setStatusBarText(String s){
	statusBar.setText(s);
    }

    //open up the default or user-specified browser (netscape, ie,...) and try to display the content uri
    void displayURLinBrowser(String uri){
	try {
	    getAppletContext().showDocument(new URL(uri), ConfigManager._BLANK);
	}
	catch(MalformedURLException ex){System.out.println("Error: could not load "+uri);}
    }

    public void about(){
	JOptionPane.showMessageDialog(this, Messages.about);
    }

    /* Key listener (keyboard events are not sent to ViewEventHandler when View is a JPanel...) */
    
    public void keyPressed(KeyEvent e){
	int code = e.getKeyCode();
	char c = e.getKeyChar();
	if(code == KeyEvent.VK_PAGE_UP){grMngr.getHigherView();}
	else if (code == KeyEvent.VK_PAGE_DOWN){grMngr.getLowerView();}
	else if (code == KeyEvent.VK_HOME){grMngr.getGlobalView();}
	else if (code == KeyEvent.VK_UP){grMngr.translateView(GraphicsManager.MOVE_UP);}
	else if (code == KeyEvent.VK_DOWN){grMngr.translateView(GraphicsManager.MOVE_DOWN);}
	else if (code == KeyEvent.VK_LEFT){grMngr.translateView(GraphicsManager.MOVE_LEFT);}
	else if (code == KeyEvent.VK_RIGHT){grMngr.translateView(GraphicsManager.MOVE_RIGHT);}
	else if (c == '+'){
	    if (grMngr.lensType != GraphicsManager.NO_LENS && grMngr.lens != null){
		grMngr.magnifyFocus(GraphicsManager.WHEEL_MM_STEP, grMngr.lensType, grMngr.mainCamera);

	    }
	    else if (meh.inZoomWindow){
		meh.tfactor = (grMngr.dmCamera.focal+Math.abs(grMngr.dmCamera.altitude))/grMngr.dmCamera.focal;
		grMngr.dmCamera.altitudeOffset(-meh.tfactor*BaseEventHandler.WHEEL_ZOOMIN_FACTOR);
		grMngr.updateMagWindow();
		grMngr.vsm.repaintNow();
	    }
	    else {
		meh.tfactor = (grMngr.mainCamera.focal+Math.abs(grMngr.mainCamera.altitude))/grMngr.mainCamera.focal;
		grMngr.mainCamera.altitudeOffset(-meh.tfactor*BaseEventHandler.WHEEL_ZOOMIN_FACTOR);
		grMngr.cameraMoved();
	    }
	}
	else if (c == '-'){
	    if (grMngr.lensType != GraphicsManager.NO_LENS && grMngr.lens != null){
		grMngr.magnifyFocus(-GraphicsManager.WHEEL_MM_STEP, grMngr.lensType, grMngr.mainCamera);
	    }
	    else if (meh.inZoomWindow){
		meh.tfactor = (grMngr.dmCamera.focal+Math.abs(grMngr.dmCamera.altitude))/grMngr.dmCamera.focal;
		grMngr.dmCamera.altitudeOffset(meh.tfactor*BaseEventHandler.WHEEL_ZOOMOUT_FACTOR);
		grMngr.updateMagWindow();
		grMngr.vsm.repaintNow();
	    }
	    else {
		meh.tfactor = (grMngr.mainCamera.focal+Math.abs(grMngr.mainCamera.altitude))/grMngr.mainCamera.focal;
		grMngr.mainCamera.altitudeOffset(meh.tfactor*BaseEventHandler.WHEEL_ZOOMOUT_FACTOR);
		grMngr.cameraMoved();
	    }
	}
    }

    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){}

    public void mouseClicked(MouseEvent e){}

    public void mouseEntered(MouseEvent e){requestFocus();}

    public void mouseExited(MouseEvent e){}

    public void mousePressed(MouseEvent e){}

    public void mouseReleased(MouseEvent e){}

    static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}
