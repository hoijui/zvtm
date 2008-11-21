/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.layout.jung;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.Container;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VPath;
import net.claribole.zvtm.glyphs.DPath;
import net.claribole.zvtm.engine.ViewEventHandler;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.StaticLayout;
import edu.uci.ics.jung.io.GraphMLFile;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.graph.decorators.EdgeShape;

import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShapeFunction;

public class GraphLayoutDemo extends JApplet {

	static final String EDGE_TYPE_PARAM = "edgeType";
	short EDGE_SHAPE = EdgeTransformer.EDGE_QUAD_CURVE;

	static final String LAYOUT_TYPE_PARAM = "layoutType";
	static final short LAYOUT_CIRCLE = 0;
	static final short LAYOUT_KK = 1;
	static final short LAYOUT_SPRING = 2;
	static final short LAYOUT_ISOM = 3;
	static final short LAYOUT_FR = 4;
	static final short LAYOUT_STATIC = 5;
	short LAYOUT_TYPE = LAYOUT_SPRING;
	
	static final String GRAPHML_FILE_URL_PARAM = "GraphMLFile";
	URL SVG_URL = null;

   	static final int DEFAULT_VIEW_WIDTH = 640;
    static final int DEFAULT_VIEW_HEIGHT = 480;
    static final String APPLET_WIDTH_PARAM = "width";
    static final String APPLET_HEIGHT_PARAM = "height";
	int appletWindowWidth = DEFAULT_VIEW_WIDTH;
	int appletWindowHeight = DEFAULT_VIEW_HEIGHT;

	VirtualSpaceManager vsm;
	static final String mSpaceName = "graph space";
	static final String mViewName = "Jung in ZVTM Demo";
	View mView;
	Camera mCamera;
	GraphLayoutDemoEventHandler eh;
	JPanel viewPanel;

	Graph graph;
	AbstractLayout layout;
		
	public GraphLayoutDemo(){
		getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
    }

    public void init(){
		getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
		initConfig();
		initGUI();
		loadGraph(SVG_URL);
		layoutGraph(getLayout(LAYOUT_TYPE));
    }

    void initConfig(){
		// get width and height of applet panel
		try {appletWindowWidth = Integer.parseInt(getParameter(APPLET_WIDTH_PARAM));}
		catch(NumberFormatException ex){appletWindowWidth = DEFAULT_VIEW_WIDTH;}
		try {appletWindowHeight = Integer.parseInt(getParameter(APPLET_HEIGHT_PARAM));}
		catch(NumberFormatException ex){appletWindowHeight = DEFAULT_VIEW_HEIGHT;}
        try {
            SVG_URL = new URL(getDocumentBase(), getParameter(GRAPHML_FILE_URL_PARAM));
        }
        catch (MalformedURLException ex){
            ex.printStackTrace();
        }
        try {
            EDGE_SHAPE = Short.parseShort(getParameter(EDGE_TYPE_PARAM));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

	void initGUI(){
		this.addKeyListener(new GraphLayoutDemoKeyEventHandler(this));
		Container cpane = getContentPane();
		this.setSize(appletWindowWidth-10, appletWindowHeight-10);
		cpane.setSize(appletWindowWidth, appletWindowHeight);
		cpane.setBackground(Color.WHITE);
		vsm = new VirtualSpaceManager(true);
		eh = new GraphLayoutDemoEventHandler(this);
		vsm.addVirtualSpace(mSpaceName);
		mCamera = vsm.addCamera(mSpaceName);
		Vector cameras = new Vector();
		cameras.add(mCamera);		
		viewPanel = vsm.addPanelView(cameras, mViewName, appletWindowWidth, appletWindowHeight);
		viewPanel.setPreferredSize(new Dimension(appletWindowWidth-10, appletWindowHeight-40));
		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.add(viewPanel, BorderLayout.CENTER);
		borderPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "Test"));
		borderPanel.setOpaque(false);
		mView = vsm.getView(mViewName);
		mView.setBackgroundColor(Color.WHITE);
		mView.setEventHandler(eh);
		mView.setNotifyMouseMoved(true);
		mView.setAntialiasing(true);
		mCamera.setAltitude(0);
		cpane.add(borderPanel);
	}

	void loadGraph(URL url){	
		try {
			URLConnection c = url.openConnection();
			InputStream is = new BufferedInputStream(c.getInputStream());
			GraphMLFile f = new GraphMLFile();
			graph = f.load(new InputStreamReader(is));
		}
		catch (Exception ex){ex.printStackTrace();}
	}
	
//	void loadGraph(File graphFile){
//		GraphMLFile f = new GraphMLFile();
//		try {
//			graph = f.load(new FileReader(graphFile));
//		}
//		catch (IOException ex){ex.printStackTrace();}
//	}

	Hashtable edge2glyph = new Hashtable();
	Hashtable vertex2glyph = new Hashtable();

	AbstractLayout getLayout(short layoutType){
		switch(layoutType){
			case LAYOUT_CIRCLE:{return new CircleLayout(graph);}
			case LAYOUT_KK:{return new KKLayout(graph);}
			case LAYOUT_SPRING:{return new SpringLayout(graph);}
			case LAYOUT_ISOM:{return new ISOMLayout(graph);}
			case LAYOUT_FR:{return new FRLayout(graph);}
			case LAYOUT_STATIC:{return new StaticLayout(graph);}
			default:{return null;}
		}
	}
	
	void layoutGraph(AbstractLayout l){
		if (l == null){return;}
		layout = l;
		int numVertices = graph.numVertices();
		layout.initialize(new java.awt.Dimension(numVertices * 10, numVertices * 10));
		Iterator i = layout.getVisibleEdges().iterator();
		while (i.hasNext()){
			Edge e = (Edge)i.next();
			Glyph g = EdgeTransformer.getDPath(e, l, EDGE_SHAPE, Color.BLACK, false);
			vsm.addGlyph(g, mSpaceName);
			edge2glyph.put(e, g);
			g.setOwner(e);
		}
		i = layout.getVisibleVertices().iterator();
		while (i.hasNext()){
			Vertex v = (Vertex)i.next();
			Coordinates c = layout.getCoordinates(v);
			VCircle cl = new VCircle((int)c.getX(), (int)c.getY(), 0, 10, Color.RED);
			vsm.addGlyph(cl, mSpaceName);
			vertex2glyph.put(v, cl);
			cl.setOwner(v);
		}
		//vsm.getGlobalView(mCamera, 300);
	}
	
	void updateLayout(){
		if (layout == null){return;}
		layout.advancePositions();
		Iterator i = layout.getVisibleEdges().iterator();
		while (i.hasNext()){
			Edge e = (Edge)i.next();
			DPath p = (DPath)edge2glyph.get(e);
			switch (EDGE_SHAPE){
				case EdgeTransformer.EDGE_LINE:{EdgeTransformer.updateLine(e, layout, p, 0, null);break;}
				case EdgeTransformer.EDGE_QUAD_CURVE:{EdgeTransformer.updateQuadCurve(e, layout, p, 0, null);break;}
				case EdgeTransformer.EDGE_CUBIC_CURVE:{EdgeTransformer.updateCubicCurve(e, layout, p, 0, null);break;}
			}
		}
		i = layout.getVisibleVertices().iterator();
		while (i.hasNext()){
			Vertex v = (Vertex)i.next();
			Coordinates c = layout.getCoordinates(v);
			VCircle cl = (VCircle)vertex2glyph.get(v);
			cl.moveTo((int)c.getX(), (int)c.getY());
		}
	}
	
//	public static void main(String[] args){
//		new GraphLayoutDemo(args[0], Short.parseShort(args[1]), Short.parseShort(args[2]));
//	}
	
}

class GraphLayoutDemoEventHandler implements ViewEventHandler {

	GraphLayoutDemo application;
	
	int lastJPX, lastJPY;
	
	GraphLayoutDemoEventHandler(GraphLayoutDemo app){
		this.application = app;
	}

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //application.vsm.setSync(false);
        lastJPX=jpx;
        lastJPY=jpy;
        //application.vsm.animator.setActiveCam(v.cams[0]);
        v.setDrawDrag(true);
        application.vsm.activeView.mouse.setSensitivity(false);
        //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.animator.Xspeed=0;
        application.vsm.animator.Yspeed=0;
        application.vsm.animator.Aspeed=0;
        v.setDrawDrag(false);
        application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=application.vsm.getActiveCamera();
            float a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                application.vsm.animator.Xspeed=0;
                application.vsm.animator.Yspeed=0;
                application.vsm.animator.Aspeed=(c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.vsm.animator.Xspeed=(c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50));
                application.vsm.animator.Yspeed=(c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));
                application.vsm.animator.Aspeed=0;
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c = application.vsm.getActiveCamera();
        float a = (c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            application.vsm.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            application.vsm.repaintNow();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
		System.out.println(g.getOwner());
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if (code == KeyEvent.VK_SPACE){application.updateLayout();}
	}
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}

class GraphLayoutDemoKeyEventHandler implements KeyListener {
	
	GraphLayoutDemo application;
	
	GraphLayoutDemoKeyEventHandler(GraphLayoutDemo app){
		this.application = app;
	}
	
	/* Key listener (keyboard events are not sent to ViewEventHandler when View is a JPanel...) */
    
	public void keyPressed(KeyEvent e){
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_SPACE){application.updateLayout();}
	}
	
	public void keyReleased(KeyEvent e){}
	
	public void keyTyped(KeyEvent e){}
	
}
