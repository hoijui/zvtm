package net.claribole.zvtm.cluster;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.DefaultTimingHandler;
import org.jdesktop.animation.timing.Animator;

import net.claribole.zvtm.cluster.MetaCamera;

import net.claribole.zvtm.glyphs.DPath;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.Vector;

//master application skeleton
public class MasterSkel {
	public MasterSkel(){
		//create virtualspace "protoSpace"
		VirtualSpaceManager vsm = VirtualSpaceManager.getInstance();
		VirtualSpace vs = vsm.addVirtualSpace("protoSpace");
		//create metacamera
		MetaCamera metacam = new MetaCamera(3,2,400,240,vs);
		vs.setMetaCamera(metacam);
		//signal that the space and metacamera are ready
		//to unfreeze the slaves
		vsm.signalMasterReady();

		//add a few glyphs to the virtual space
		Glyph rect = new VRectangle(0,0,0,60,40,Color.GREEN);
		Glyph anotherRect = new VRectangle(30,50,0,60,70,Color.RED);
		VText text = new VText(20,20,0,Color.YELLOW,"No Loitering");
		text.setScale(4f);
		DPath path = new DPath(40,0,0,Color.ORANGE);
		path.addQdCurve(60,60,0,60,false);
		path.addQdCurve(60,60,60,0,false);
		path.setStrokeWidth(5f);
		Glyph image = new VImage(100, 200, 0, "/Users/romain/Documents/inria/checkouts/zvtm-terra-proto/NoLoitering.gif");

		vsm.addGlyph(rect, vs);
		vsm.addGlyph(anotherRect, vs);
		vsm.addGlyph(text, vs);
		vsm.addGlyph(path, vs);
		vsm.addGlyph(image, vs);

		Camera cam = vsm.addCamera(vs);
		cam.setAltitude(0f);
		cam.moveTo(vs.getMetaCamera().retrieveCamera(0).getLocation().vx,
				vs.getMetaCamera().retrieveCamera(0).getLocation().vy);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(cam);

		//add an external view for interaction (scrolling) purposes
		View view = vsm.addExternalView(vcam, "masterView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);

		//animate metacamera (circular, repeating animation)
		Animation anim = vsm.getAnimationManager().getAnimationFactory()
			.createAnimation(5000, 10, Animator.RepeatBehavior.LOOP,
					metacam, Animation.Dimension.POSITION,
					new DefaultTimingHandler(){
						public void timingEvent(float fraction,
							Object subject, Animation.Dimension dim){
							double theta = 2 * Math.PI * fraction;
							((MetaCamera)subject).moveTo((long)(150*Math.cos(theta)),
								(long)(150*Math.sin(theta)));
						}
			}
			);
		vsm.getAnimationManager().startAnimation(anim, false);
	}

	public static void main(String[] args){
		new MasterSkel();
	}
}

