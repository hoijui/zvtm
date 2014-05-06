/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
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
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.ZScale;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.FitsImage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Vector;
import java.io.IOException;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.net.MalformedURLException;
import java.net.URL;
/*
 * A simple clustered FITS viewer example.
 * No support for complex scenes, this will 
 * come as an extension to ZUIST.
 * At the moment, it simply loads and displays a single image.
 */
public class FitsViewer {
	//shortcut
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 
    private FitsImage image;
    private RangeSelection rsel;
    private double[] scaleBounds;
    private boolean dragLeft = false, dragRight = false;
    private View view;

    FitsViewer(ImOptions options) throws IOException{
        vsm.setMaster("FitsViewer");
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
        cv.setBackgroundColor(Color.LIGHT_GRAY);
        vsm.addClusteredView(cv);

        //the view below is just a standard, non-clustered view
        //that lets an user navigate the scene
        view = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);	
		view.setListener(new PanZoomEventHandler());
        view.getCursor().setColor(Color.GREEN);

		URL imgURL = null;
		try {
			if(options.source.equals("")){
				imgURL = new URL("http://fits.gsfc.nasa.gov/samples/FOCx38i0101t_c0f.fits");
			} else {
				imgURL = new URL(options.source);
			}
		} catch (MalformedURLException e){
			throw new Error("Malformed URL");
		}
		image = new FitsImage(0,0,0,imgURL);
		vs.addGlyph(image);
        image.setScaleMethod(FitsImage.ScaleMethod.LINEAR);
        scaleBounds = ZScale.computeScale(image.getUnderlyingImage());
        image.rescale(scaleBounds[0], scaleBounds[1], 1);
        rsel = new RangeSelection();
        double min = image.getUnderlyingImage().getHistogram().getMin();
        double max = image.getUnderlyingImage().getHistogram().getMax();
        rsel.setTicksVal((scaleBounds[0]-min)/(max-min), (scaleBounds[1]-min)/(max-min));
        vs.addGlyph(rsel);
	}

	public static void main(String[] args) throws Exception{
		ImOptions options = new ImOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		new FitsViewer(options);
	}

    private Point2D.Double viewToSpace(Camera cam, int jpx, int jpy){
        Location camLoc = cam.getLocation();
        double focal = cam.getFocal();
        double altCoef = (focal + camLoc.alt) / focal;
        Dimension viewSize = view.getPanelSize();

        //find coords of view origin in the virtual space
        double viewOrigX = camLoc.vx - 0.5*viewSize.width*altCoef;
        double viewOrigY = camLoc.vy + 0.5*viewSize.height*altCoef;

        return new Point2D.Double(
                viewOrigX + altCoef*jpx,
                viewOrigY - altCoef*jpy);
    }

	private class PanZoomEventHandler implements ViewListener{
		private int lastJPX;
		private int lastJPY;

        public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            Point2D.Double cursorPos = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
            if(rsel.overLeftTick(cursorPos.x, cursorPos.y)){
                dragLeft = true;
            } else if(rsel.overRightTick(cursorPos.x, cursorPos.y)){
                dragRight = true;
            }
        }

        public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            dragLeft = false;
            dragRight = false;
            double min = image.getUnderlyingImage().getHistogram().getMin();
            double max = image.getUnderlyingImage().getHistogram().getMax();
            image.rescale(min + rsel.getLeftValue()*(max - min),
                    min + rsel.getRightValue()*(max - min),
                    1);
        }

		public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
			lastJPX=jpx;
			lastJPY=jpy;
			v.setDrawDrag(true);
			vsm.getActiveView().mouse.setSensitivity(false);
			//because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
		}

		public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		    Camera c=vsm.getActiveCamera();
			c.setXspeed(0);
			c.setYspeed(0);
			c.setZspeed(0);
			v.setDrawDrag(false);
			vsm.getActiveView().mouse.setSensitivity(true);
		}

		public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

        public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            if(buttonNumber == 1){
                if(dragLeft) {
                    rsel.setLeftTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                } else if(dragRight){
                    rsel.setRightTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
                }
            }

            if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
                Camera c=vsm.getActiveCamera();
                double a=(c.focal+Math.abs(c.altitude))/c.focal;
				if (mod == META_SHIFT_MOD) {
					c.setXspeed(0);
					c.setYspeed(0);
					c.setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));

				}
				else {
					c.setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/4.0) : (jpx-lastJPX)/(a*4));
					c.setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));
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

		public void viewClosing(View v){System.exit(0);}

	}
}

class ImOptions {
	@Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
	int blockWidth = 400;

	@Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
	int blockHeight = 300;

	@Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
	int numRows = 2;

	@Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
	int numCols = 3;

    @Option(name = "-s", aliases = {"--source"}, usage = "image source")
	String source = "";
}

