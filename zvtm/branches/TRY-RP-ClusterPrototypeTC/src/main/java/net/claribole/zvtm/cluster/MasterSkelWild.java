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
public class MasterSkelWild {
	public MasterSkelWild(){
		//create virtualspace "protoSpace"
		VirtualSpaceManager vsm = VirtualSpaceManager.getInstance();
		VirtualSpace vs = vsm.addVirtualSpace("protoSpace");
		//create metacamera
		MetaCamera metacam = new MetaCamera(2,3,2560,1600,vs);
		vs.setMetaCamera(metacam);
		//signal that the space and metacamera are ready
		//to unfreeze the slaves
		vsm.signalMasterReady();

		for(int i=0; i<10; ++i){
			//add a few glyphs to the virtual space
			Glyph rect = new VRectangle(-2000+400*i,-2000+400*i,0,600,400,Color.GREEN);
			Glyph anotherRect = new VRectangle(-1700+400*i,-1700+400*i,0,600,700,Color.RED);
			Glyph image = new VImage(-1750+450*i, -1750+450*i, 0, "/home/wild/romain/zvtm_tc/NoLoitering.gif");

			vsm.addGlyph(rect, vs);
			vsm.addGlyph(anotherRect, vs);
			vsm.addGlyph(image, vs);
		}

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
							((MetaCamera)subject).moveTo((long)(2300*Math.cos(theta)),
								(long)(2300*Math.sin(theta)));
						}
			}
			);
		vsm.getAnimationManager().startAnimation(anim, false);
	}

	public static void main(String[] args){
		new MasterSkelWild();
	}
}

