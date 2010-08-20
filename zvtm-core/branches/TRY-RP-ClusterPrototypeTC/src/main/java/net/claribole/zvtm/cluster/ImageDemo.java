package net.claribole.zvtm.cluster;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.DefaultTimingHandler;
import org.jdesktop.animation.timing.Animator;

import net.claribole.zvtm.engine.ViewEventHandler;

import net.claribole.zvtm.cluster.MetaCamera;

import net.claribole.zvtm.glyphs.DPath;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Color;
import java.util.Vector;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class IDOptions {
	@Option(name = "-p", aliases = {"--path"}, usage = "image path")
	String imagePath = "";
}

//master application skeleton
public class ImageDemo {
	VirtualSpaceManager vsm;
	IDOptions options;

	public ImageDemo(IDOptions options){
		this.options = options;

		//create virtualspace "protoSpace"
		vsm = VirtualSpaceManager.getInstance();
		VirtualSpace vs = vsm.addVirtualSpace("protoSpace");
		//create metacamera
		MetaCamera metacam = new MetaCamera(3,2,600,400,vs);
		vs.setMetaCamera(metacam);

		VImage img1 = new VImage(0, 0, 0, "/home/rprimet/Bureau/images_astro/hs-2004-17-b-full_jpg.jpg");
		vsm.addGlyph(img1, vs);

		//signal that the space and metacamera are ready
		//to unfreeze the slaves
		vsm.signalMasterReady();

		Camera cam = vsm.addCamera(vs);
		cam.setAltitude(0f);
		vsm.setZoomLimit(0);
		cam.moveTo(vs.getMetaCamera().retrieveCamera(0).getLocation().vx,
				vs.getMetaCamera().retrieveCamera(0).getLocation().vy);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(cam);

		//add an external view for interaction (scrolling) purposes
		View view = vsm.addExternalView(vcam, "masterView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
		//add a panning evt handler
		view.setEventHandler(new IDEventHandler());
	}

	public static void main(String[] args) throws CmdLineException{
		IDOptions options = new IDOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);

		new ImageDemo(options);
	}

	class IDEventHandler implements ViewEventHandler{
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
				float a=(c.focal+Math.abs(c.altitude))/c.focal;
				if (mod == META_SHIFT_MOD) {
					vsm.getAnimationManager().setXspeed(0);
					vsm.getAnimationManager().setYspeed(0);
					//50 is just a speed factor (too fast otherwise)
				}
				else {
					vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
					vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
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

