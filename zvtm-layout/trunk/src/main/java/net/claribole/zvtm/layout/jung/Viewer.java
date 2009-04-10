/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
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
import javax.swing.JFrame;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.Enumeration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.svg.SVGWriter;
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

import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Viewer extends JFrame {
	
	static int NODE_SIZE = 100;

	short EDGE_SHAPE = EdgeTransformer.EDGE_QUAD_CURVE;

	static final String LAYOUT_TYPE_PARAM = "layoutType";
	static final short LAYOUT_CIRCLE = 0;
	static final short LAYOUT_KK = 1;
	static final short LAYOUT_SPRING = 2;
	static final short LAYOUT_ISOM = 3;
	static final short LAYOUT_FR = 4;
	static final short LAYOUT_STATIC = 5;
	short LAYOUT_TYPE = LAYOUT_SPRING;
	
   	static final int DEFAULT_VIEW_WIDTH = 1600;
    static final int DEFAULT_VIEW_HEIGHT = 1200;
	int viewWidth = DEFAULT_VIEW_WIDTH;
	int viewHeight = DEFAULT_VIEW_HEIGHT;

	VirtualSpaceManager vsm;
	static final String mSpaceName = "graph space";
	static final String mViewName = "Jung in ZVTM Viewer";
	View mView;
	VirtualSpace mSpace;
	Camera mCamera;
	ViewerEventHandler eh;

	Graph graph;
	AbstractLayout layout;
	
	static int NB_TARGETS_PER_TRIAL = 24;
	static final float CAM_ALT = 900.0f;  // so as to get a proj coef of 0.1 (focal is 100.0f)
	static double D = 800;
	static final long TARGET_R_POS = Math.round(D * (Camera.DEFAULT_FOCAL+CAM_ALT)/Camera.DEFAULT_FOCAL / 2.0);
		
	public Viewer(String gmlFilePath, short es, short lt){
		super(mViewName);
		EDGE_SHAPE = es;
		LAYOUT_TYPE = lt;
		initGUI();
		loadGraph(new File(gmlFilePath));
		layoutGraph(getLayout(LAYOUT_TYPE));
    }

	void initGUI(){
		vsm = new VirtualSpaceManager(false);
		eh = new ViewerEventHandler(this);
		mSpace = vsm.addVirtualSpace(mSpaceName);
		mCamera = vsm.addCamera(mSpaceName);
		Vector cameras = new Vector();
		cameras.add(mCamera);
		mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, viewWidth, viewHeight, false, true);
        mView.setBackgroundColor(Color.WHITE);
        mView.setEventHandler(eh);
		mView.setAntialiasing(true);
		mCamera.moveTo(0, 0);
		mCamera.setAltitude(CAM_ALT);
		
//		double angle = 0;
//		for (int i=0;i<NB_TARGETS_PER_TRIAL;i++){
//			long x = (long) (TARGET_R_POS * Math.cos(angle));
//			long y = (long) (TARGET_R_POS * Math.sin(angle));
//			Glyph g = new VCircle(x, y, 0, Math.round(NODE_SIZE*1.1), Color.RED);
//			vsm.addGlyph(g, mSpaceName);
//			if (i % 2 == 0){angle += Math.PI;}
//			else {angle += 2 * Math.PI / ((double)NB_TARGETS_PER_TRIAL) - Math.PI;}
//			g.setType("del");
//		}
		
		
		vsm.repaintNow();
	}
	
	void loadGraph(File graphFile){
		GraphMLFile f = new GraphMLFile();
		try {
			graph = f.load(new FileReader(graphFile));
			// code below removes duplicate edges
			Set edges = graph.getEdges();
			Iterator i = edges.iterator();
			Vector edgesToKeep = new Vector();
			Vector edgesToRemove = new Vector();
			while (i.hasNext()){
				boolean ok = true;
				Edge e = (Edge)i.next();
				Pair p1 = e.getEndpoints();
				for (int j=0;j<edgesToKeep.size();j++){
					Pair p2 = ((Edge)edgesToKeep.elementAt(j)).getEndpoints();
					if (p1.getFirst() == p2.getFirst() && p1.getSecond() == p2.getSecond()
					    || p1.getFirst() == p2.getSecond() && p1.getSecond() == p2.getFirst()){
						edgesToRemove.add(e);
						ok = false;
						break;
					}
				}
				if (ok){
					edgesToKeep.add(e);
				}
			}
			for (int j=0;j<edgesToRemove.size();j++){
				graph.removeEdge((Edge)edgesToRemove.elementAt(j));
			}
			System.out.println("Found "+graph.numVertices()+" vertices and "+graph.numEdges()+" edges");
			
		}
		catch (IOException ex){ex.printStackTrace();}
	}

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
		layout.initialize(new java.awt.Dimension(numVertices * 170, numVertices * 120));
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
			VCircle cl = new VCircle((int)c.getX(), (int)c.getY(), 0, NODE_SIZE, Color.WHITE);
			vsm.addGlyph(cl, mSpaceName);
			cl.setMouseInsideHighlightColor(Color.RED);
			vertex2glyph.put(v, cl);
			cl.setOwner(v);
		}
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
	
	void updateEdges(Glyph n){
		Vertex v = (Vertex)n.getOwner();
		Vertex oe;
		Edge e;
		Glyph oen;
		Iterator i = v.getIncidentEdges().iterator();
		switch (EDGE_SHAPE){
			case EdgeTransformer.EDGE_LINE:{
				while (i.hasNext()){
					e = (Edge)i.next();
					oe = e.getOpposite(v);
					oen = (Glyph)vertex2glyph.get(oe);
					DPath p = (DPath)edge2glyph.get(e);
					LongPoint[] coords = {new LongPoint(Math.round(n.vx), Math.round(n.vy)),
						                  new LongPoint(Math.round(oen.vx), Math.round(oen.vy))};
					p.edit(coords, true);					
				}
				break;
			}
		}
	}
	
	void translateGraph(long x, long y){
		for (Enumeration e=vertex2glyph.elements();e.hasMoreElements();){
			((Glyph)e.nextElement()).move(x, y);
		}
		for (Enumeration e=edge2glyph.elements();e.hasMoreElements();){
			((Glyph)e.nextElement()).move(x, y);
		}
	}
	
    static final String SVG_OUTPUT_ENCODING = "UTF-8";
	
	void exportSVG(){
		File f = new File("eval.svg");
		Document d = (new SVGWriter()).exportVirtualSpace(mSpace, new DOMImplementationImpl(), f);
		OutputFormat format=new OutputFormat(d, SVG_OUTPUT_ENCODING, true);
	 	format.setLineSeparator(LineSeparator.Web);
		try {
		    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), SVG_OUTPUT_ENCODING);
		    DOMSerializer serializer = (new XMLSerializer(osw, format)).asDOMSerializer();
		    serializer.serialize(d);
		}
		catch (IOException e){e.printStackTrace();}
	}
	
	public static void main(String[] args){
		new Viewer(args[0], Short.parseShort(args[1]), Short.parseShort(args[2]));
	}
	
}

class ViewerEventHandler implements ViewEventHandler {
	
	int bob = 0;

	Viewer application;
	
	int lastJPX, lastJPY;

	Glyph draggedNode = null;
	
	ViewerEventHandler(Viewer app){
		this.application = app;
	}

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			draggedNode = g;
			application.vsm.stickToMouse(draggedNode);
		}
	}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		application.vsm.unstickFromMouse();
		draggedNode = null;
	}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
//		Vector v1 = v.getMouse().getIntersectingPaths(application.mCamera, 10);
//		if (v1 != null && !v1.isEmpty()){
//			Glyph g = (Glyph)v1.firstElement();
//			System.out.println(g);
//			application.mSpace.destroyGlyph(g);
//		}
	}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){	
//		Glyph g = v.lastGlyphEntered();
//		if (g != null){
//			g.setColor(Color.YELLOW);
//			g.setType("target_"+bob);
//			bob++;
//		}
//	
	}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        application.vsm.activeView.mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.getAnimationManager().setXspeed(0);
        application.vsm.getAnimationManager().setYspeed(0);
        application.vsm.getAnimationManager().setZspeed(0);
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
                application.vsm.getAnimationManager().setXspeed(0);
                application.vsm.getAnimationManager().setYspeed(0);
                application.vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                application.vsm.getAnimationManager().setZspeed(0);
            }
        }
		else if (draggedNode != null){
			application.updateEdges(draggedNode);
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
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if (code == KeyEvent.VK_SPACE){application.updateLayout();}
		else if (code == KeyEvent.VK_LEFT){application.translateGraph(-Viewer.TARGET_R_POS/16, 0);}
		else if (code == KeyEvent.VK_RIGHT){application.translateGraph(Viewer.TARGET_R_POS/16, 0);}
		else if (code == KeyEvent.VK_UP){application.translateGraph(0, Viewer.TARGET_R_POS/16);}
		else if (code == KeyEvent.VK_DOWN){application.translateGraph(0, -Viewer.TARGET_R_POS/16);}
		else if (code == KeyEvent.VK_DELETE){
			Glyph g = v.lastGlyphEntered();
			if (g != null){
				application.mSpace.destroyGlyph(g);
			}
		}
		else if (code == KeyEvent.VK_E){application.exportSVG();}
	}
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
