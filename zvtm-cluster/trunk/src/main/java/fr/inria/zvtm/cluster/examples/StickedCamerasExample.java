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
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
/**
 * Sample master application.
 */
public class StickedCamerasExample {
	//shortcut
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 

    /* Navigation constants */
    static final int ANIM_MOVE_DURATION = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;

    Camera cam1;
    Camera cam2;
    
    View view;
    
    StickedCamerasExample(SCamOptions options){
        vsm.setMaster("StickedCamerasExample");
        VirtualSpace vs1 = vsm.addVirtualSpace("testSpace1");
        VirtualSpace vs2 = vsm.addVirtualSpace("testSpace2");
        Vector<Camera> cameras = new Vector<Camera>();
        cam1 = vs1.addCamera();
        cam2 = vs2.addCamera();
        cameras.add(cam1);	
        cameras.add(cam2);	
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
        View view = vsm.addFrameView(cameras, "Master View",
                View.STD_VIEW, 800, 600, false, true, true, null);
        view.getCursor().setColor(Color.WHITE);
        PanZoomEventHandler eh = new PanZoomEventHandler();
		view.setListener(eh);
        cam1.addListener(eh);
		VCircle c1 = new VCircle(0, 0, 0, 20, Color.WHITE);
		vs1.addGlyph(c1, false);
		VCircle c2 = new VCircle(20, 0, 0, 20, Color.WHITE);
		vs2.addGlyph(c2, false);
	}
	
	/* Higher view */
    void getHigherView(){
        Float alt = new Float(cam1.getAltitude() + cam1.getFocal());
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(ANIM_MOVE_DURATION, cam1,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(cam1.getAltitude() + cam1.getFocal())/2.0f);
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(ANIM_MOVE_DURATION, cam1,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of WorldExplorer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = view.getVisibleRegion(cam1);
        if (direction==MOVE_UP){
            double qt = (rb[1]-rb[3])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(ANIM_MOVE_DURATION, cam1,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

	public static void main(String[] args){
		SCamOptions options = new SCamOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		new StickedCamerasExample(options);
	}

	private class PanZoomEventHandler implements ViewListener, CameraListener {
	    
	    final float WHEEL_ZOOMIN_FACTOR = 21.0f;
        final float WHEEL_ZOOMOUT_FACTOR = 22.0f;
        float WHEEL_MM_STEP = 1.0f;
        
		private int lastJPX;
		private int lastJPY;
		boolean panning = false;

		public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		    lastJPX = jpx;
            lastJPY = jpy;
            panning = true;
		}

		public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		    panning = false;
		}

		public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

		public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

		public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

		public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
			if (panning){
                double a = (cam1.focal+Math.abs(cam1.altitude)) / cam1.focal;
                synchronized(cam1){
                    cam1.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
                    lastJPX = jpx;
                    lastJPY = jpy;
    				cameraMoved(cam1, null, 0);
                }
            }
		}

		public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		    double a = (cam1.focal+Math.abs(cam1.altitude)) / cam1.focal;
            if (wheelDirection == WHEEL_UP){
                // zooming in
                cam1.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
            }
            else {
                //wheelDirection == WHEEL_DOWN, zooming out
                cam1.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
            }
		}

        public void cameraMoved(Camera cam, Point2D.Double coord, double a){
            cam2.setAltitude(cam1.getAltitude());
            cam2.moveTo(cam1.vx, cam1.vy);
        }
        
		public void enterGlyph(Glyph g){
		}

		public void exitGlyph(Glyph g){
		}

		public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

		public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		    if (code==KeyEvent.VK_PAGE_UP){getHigherView();}
        	else if (code==KeyEvent.VK_PAGE_DOWN){getLowerView();}
        	else if (code==KeyEvent.VK_UP){translateView(MOVE_UP);}
        	else if (code==KeyEvent.VK_DOWN){translateView(MOVE_DOWN);}
        	else if (code==KeyEvent.VK_LEFT){translateView(MOVE_LEFT);}
        	else if (code==KeyEvent.VK_RIGHT){translateView(MOVE_RIGHT);}
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

class SCamOptions {
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

