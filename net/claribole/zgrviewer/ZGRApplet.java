/*   FILE: ZGRApplet.java
 *   DATE OF CREATION:   Fri May 09 09:52:34 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zgrviewer;

import java.awt.*;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JPanel;

import net.claribole.zvtm.engine.Location;

import org.w3c.dom.Document;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.svg.SVGReader;


public class ZGRApplet extends JApplet {

    static final int DEFAULT_VIEW_WIDTH = 640;
    static final int DEFAULT_VIEW_HEIGHT = 480;
    static final String WIDTH_APPLET_PARAM = "width";
    static final String HEIGHT_APPLET_PARAM = "height";
    static final String SVG_FILE_URL_PARAM = "svgURL";

    static ConfigManager cfgMngr;

    public GVLoader gvLdr;
    public GraphicsManager grMngr;

    ZgrAppletEvtHdlr meh;

    JPanel viewPanel;

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
	grMngr = new GraphicsManager();
	cfgMngr = new ConfigManager(grMngr, true);
	grMngr.setConfigManager(cfgMngr);
	gvLdr = new GVLoader(this, grMngr, cfgMngr, null);
    }

    void initGUI(){
	try {appletWindowWidth = Integer.parseInt(getParameter(WIDTH_APPLET_PARAM));}
	catch(NumberFormatException ex){appletWindowWidth = DEFAULT_VIEW_WIDTH;}
	try {appletWindowHeight = Integer.parseInt(getParameter(HEIGHT_APPLET_PARAM));}
	catch(NumberFormatException ex){appletWindowHeight = DEFAULT_VIEW_HEIGHT;}
	Container cpane = getContentPane();
	cpane.setBackground(Color.BLUE);
	cpane.setLayout(new FlowLayout());
	AppletUtils.initLookAndFeel();
	viewPanel = grMngr.createPanelView(grMngr.createZVTMelements(true), appletWindowWidth, appletWindowHeight);
	grMngr.parameterizeView(new ZgrAppletEvtHdlr(this, this.grMngr));
	this.setSize(appletWindowWidth-10, appletWindowHeight-10);
	cpane.setSize(appletWindowWidth, appletWindowHeight);
	viewPanel.setPreferredSize(new Dimension(appletWindowWidth-10, appletWindowHeight-60));
	cpane.add(viewPanel);
	setVisible(true);
	validate();
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    gvLdr.loadSVG(getParameter(SVG_FILE_URL_PARAM));
		    return null; 
		}
	    };
	worker.start();
    }

}
