/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster.examples;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.net.MalformedURLException;
import java.net.URL;
/**
 * Sample master application.
 */
public class DPathExample {
	//shortcut
	private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 

	DPathExample(PathOptions options){
		vsm.setMaster("DPathExample");
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		Camera cam = vs.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(cam);	
        ClusterGeometry clGeom = new ClusterGeometry(
                options.blockWidth,
                options.blockHeight,
                options.numCols,
                options.numRows);
		ClusteredView cv = 
            new ClusteredView(
                    clGeom,
                    options.numRows-1, //origin (block number)
                    options.numCols, //use complete
                    options.numRows, //cluster surface
                    cameras);
        cv.setBackgroundColor(Color.GRAY);
        vsm.addClusteredView(cv);

        //the view below is just a standard, non-clustered view
        //that lets an user navigate the scene
        View view = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	
        view.setBackgroundColor(Color.GRAY);
        view.setEventHandler(new PanZoomEventHandler());

        DPath path = new DPath(30,50,0,Color.GREEN);	
        path.addSegment(100,200,true);
        path.addSegment(200,0,true);
        path.addSegment(0,0,true);
        vs.addGlyph(path, false);	
        path.moveTo(100,200);
        System.out.println("path position: (" + path.vx + ", " + path.vy + ")");
        path.moveTo(100,-200);
        System.out.println("path position: (" + path.vx + ", " + path.vy + ")");

        VRectangle rect = new VRectangle(0,0,0,100,200,Color.YELLOW);
        vs.addGlyph(rect);

        DPath pathEdit = new DPath(0,0,0,Color.RED);
        pathEdit.addSegment(0,30,true);
        pathEdit.addSegment(30,30,true);
        pathEdit.addSegment(30,0,true);
        pathEdit.addSegment(0,0,true);
        //turn the square into a triangle
        pathEdit.edit(new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0,0),
        new Point2D.Double(0,0), new Point2D.Double(-30,0), new Point2D.Double(0,0)},
                false);
        vs.addGlyph(pathEdit);
    }

	public static void main(String[] args){
		PathOptions options = new PathOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		new DPathExample(options);
	}

	private class PanZoomEventHandler implements ViewEventHandler{
		private int lastJPX;
		private int lastJPY;

		public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			lastJPX=jpx;
			lastJPY=jpy;
			v.setDrawDrag(true);
			vsm.activeView.mouse.setSensitivity(false);
			//because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
		}

		public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			vsm.getAnimationManager().setXspeed(0);
			vsm.getAnimationManager().setYspeed(0);
			vsm.getAnimationManager().setZspeed(0);
			v.setDrawDrag(false);
			vsm.activeView.mouse.setSensitivity(true);
		}

		public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

		public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
			if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
				Camera c=vsm.getActiveCamera();
				double a=(c.focal+Math.abs(c.altitude))/c.focal;
				if (mod == META_SHIFT_MOD) {
					vsm.getAnimationManager().setXspeed(0);
					vsm.getAnimationManager().setYspeed(0);
					vsm.getAnimationManager().setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));

				}
				else {
					vsm.getAnimationManager().setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/4.0) : (jpx-lastJPX)/(a*4));
					vsm.getAnimationManager().setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));
				}
			}
		}

		public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

		public void enterGlyph(Glyph g){
		}

		public void exitGlyph(Glyph g){
		}

		public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

		public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		}

		public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

		public void viewActivated(View v){}

		public void viewDeactivated(View v){}

		public void viewIconified(View v){}

		public void viewDeiconified(View v){}

		public void viewClosing(View v){
            vsm.stop();
            System.exit(0);
        }

	}
}

class PathOptions {
	@Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
	int blockWidth = 400;

	@Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
	int blockHeight = 300;

	@Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
	int numRows = 2;

	@Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
	int numCols = 3;
 }

