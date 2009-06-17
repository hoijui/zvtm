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
import com.xerox.VTM.engine.LongPoint;
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

class CRAOptions {
	@Option(name = "-x", aliases = {"--xnum"}, usage = "number of subdivisions along x axis")
	int xNum = 50;

	@Option(name = "-y", aliases = {"--ynum"}, usage = "number of subdivisions along y axis")
	int yNum = 20;

	@Option(name = "-w", aliases = {"--width"}, usage = "color rect width")
	int width = 800;

	@Option(name = "-h", aliases = {"--height"}, usage = "color rect height")
	int height = 600;
}

//master application skeleton
public class ColorRectAnim {
	VirtualSpaceManager vsm;
	CRAOptions options;

	public ColorRectAnim(CRAOptions options){
		this.options = options;

		//create virtualspace "protoSpace"
		vsm = VirtualSpaceManager.getInstance();
		VirtualSpace vs = vsm.addVirtualSpace("protoSpace");
		//create metacamera
		MetaCamera metacam = new MetaCamera(2,2,600,400,vs);
		vs.setMetaCamera(metacam);
		long xOffset = -options.width/2;
		long yOffset = -options.height/2;
		long rectWidth = options.width/options.xNum;
		long rectHeight = options.height/options.yNum;
		final java.util.Random rnd = new java.util.Random(); //animation direction
		for(int i=0; i<options.xNum; ++i){
			for(int j=0; j<options.yNum; ++j){
				final VRectangle rect = new VRectangle(xOffset+i*rectWidth,
						yOffset+j*rectHeight,
						0,
						(long)(0.5*(rectWidth/2)), (long)(0.5*(rectHeight/2)),
						Color.getHSBColor((float)(i*j/(float)(options.xNum*options.yNum)), 1f, 1f));
				rect.setDrawBorder(false);
				vsm.addGlyph(rect, vs, false);
				//animate rectangle: infinite circular motion
				Animation rectAnim = vsm.getAnimationManager()
					.getAnimationFactory().createAnimation(3000, org.jdesktop.animation.timing.Animator.INFINITE, org.jdesktop.animation.timing.Animator.RepeatBehavior.LOOP, rect,
							Animation.Dimension.POSITION,
	new DefaultTimingHandler(){
	private final LongPoint center = rect.getLocation();
	private final float radius = rect.getSize();
	private final int direction = rnd.nextBoolean()? 1 : -1; 
	@Override
	public void timingEvent(float fraction, Object subject, Animation.Dimension dim){
		((Glyph)subject).moveTo((long)(center.x + (radius*Math.cos(2*Math.PI*fraction))),
	(long)(center.y + (direction*radius*Math.sin(2*Math.PI*fraction))));
	}
	});
	vsm.getAnimationManager().startAnimation(rectAnim, true);

			}
		}

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
		//set refresh rate to 25 fps (40ms)
		view.setRefreshRate(40);
		//add a panning evt handler
		view.setEventHandler(new CREventHandler());
	}

	public static void main(String[] args) throws CmdLineException{
		CRAOptions options = new CRAOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);

		new ColorRectAnim(options);
	}

	class CREventHandler implements ViewEventHandler{
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
					vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/4.0f)) : (long)((jpx-lastJPX)/(a*4)));
					vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/4.0f)) : (long)((lastJPY-jpy)/(a*4)));
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

