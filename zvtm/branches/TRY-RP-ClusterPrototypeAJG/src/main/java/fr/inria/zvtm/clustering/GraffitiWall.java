
package fr.inria.zvtm.clustering;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.util.Vector;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.clustering.ObjIdFactory;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class GWOptions {
	@Option(name = "-w", aliases = {"--width"}, usage = "screen width (hint)")
	int width = 800;

	@Option(name = "-h", aliases = {"--height"}, usage = "screen height (hint)")
	int height = 600;

	@Option(name = "-f", aliases = {"--file"}, usage = "filename (state save/restore)")
	String filename = "";
}

//java -cp target/zvtm-0.10.0-SNAPSHOT.jar:targetimingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar fr.inria.zvtm.clustering.GraffitiWall
/**
 * Graffiti wall.
 * Implemented features:
 * - strokes
 *
 * Desirable features:
 * - images (scaled) [VImage support needs to be added to zvtm-cluster]
 * - text (scaled) [supported]
 * - easy way to add/update those and select parameters (e.g. color) [email, cli]
 * - persistence (save/load)
 */
public class GraffitiWall {
	private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	private VirtualSpace vs;
	private Cursor cursor;
	private final GWOptions options;

	private class Cursor {
		private final VSegment segH;
		private final VSegment segV;

		Cursor(VirtualSpace vs){
			segH = new VSegment(0,0,0,100,0,Color.RED);
			segV = new VSegment(0,0,0,0,100,Color.RED);
			vs.addGlyph(segH, false);
			vs.addGlyph(segV, false);
			segH.setStrokeWidth(15f);
			segV.setStrokeWidth(15f);
		}

		void moveTo(LongPoint newLoc){
			segH.moveTo(newLoc.x, newLoc.y);
			segV.moveTo(newLoc.x, newLoc.y);
		}
	}

	//silly hack to make sure this particular SU
	//*is* instrumented (transmits messages)
	private class MySlaveUpdater extends SlaveUpdater{
		MySlaveUpdater(VirtualSpace vs) throws Exception {super(vs);}
	}

	GraffitiWall(GWOptions options){
		this.options = options;
		vs = vsm.addVirtualSpace("testSpace");
		cursor = new Cursor(vs);

		Camera c = vsm.addCamera(vs);
		Vector<Camera> vcam = new Vector<Camera>();
		vcam.add(c);
		View view = vsm.addExternalView(vcam, "masterView", View.STD_VIEW,
				800, 600, false, true, true, null);
		view.setBackgroundColor(Color.LIGHT_GRAY);
		view.setEventHandler(new GraffitiWallEventHandler());
		view.setNotifyMouseMoved(true);

		VText text = new VText(0L, 0L, 0, //XXX set sensible coords 
				Color.YELLOW,
				"scribble here",
				VText.TEXT_ANCHOR_MIDDLE,
				3f,
				1f); 	
		vs.addGlyph(text, false);
		vs.getCameraGroup().setLocation(new Location(0,0,0));
		load();
	}

	//rudimentary binary save
	void save(){
		if(options.filename.equals("")){
			return;
		}
		try{
			ArrayList<Delta> createDeltas = new ArrayList<Delta>();
			Vector glyphs = vs.getAllGlyphs();
			for(Object obj: glyphs){
				Glyph g = (Glyph)obj;
				createDeltas.add(g.getCreateDelta());
			}

			OutputStream os = new FileOutputStream(options.filename);
			ObjectOutput oo = new ObjectOutputStream(os);
			oo.writeObject(createDeltas);
			oo.close();
		} catch (Exception e){
			//just fail semi-silently (throwaway code)
			e.printStackTrace();
		}
	}

	void load(){
		if(options.filename.equals("")){
			return;
		}
		try{
			InputStream is = new FileInputStream(options.filename);
			ObjectInput oi = new ObjectInputStream(is);
			ArrayList<Delta> createDeltas = (ArrayList<Delta>)oi.readObject();
			oi.close();
			MySlaveUpdater su = new MySlaveUpdater(vs);
			for(Delta delta: createDeltas){
				delta.apply(su);
			}
		} catch (Exception e){
			//just fail semi-silently (throwaway code)
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws CmdLineException{
		GWOptions options = new GWOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.parseArgument(args);

		new GraffitiWall(options);
	}

	class GraffitiWallEventHandler implements ViewEventHandler{
		private int lastJPX;
		private int lastJPY;
		private boolean painting = false;
		private Robot robot;
		private final Location CENTER = new Location(200,200,0);
		private static final long XYFACT = 5;
		private long precX = 0;
		private long precY = 0;
		private long wallX = 0;
		private long wallY = 0;

		GraffitiWallEventHandler(){
			try{
				robot = new Robot(); 
			} catch (AWTException ex) {
				throw new Error("could not create robot");
			}
			robot.mouseMove((int)CENTER.vx, (int)CENTER.vy);
		} 

		public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){ painting = true; }

		public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){ painting = false; }

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

		public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
			System.out.println("mm " + jpx + " " + jpy + ", " + e.getX() 
+ " " + e.getY());
			if((jpx != 195) && (jpy != 175)){
				//compute wall coords
				precX = wallX;
				precY = wallY;
				wallX += ((jpx - 195) * XYFACT);
				wallY -= ((jpy - 175) * XYFACT);
				//move cursor
				cursor.moveTo(new LongPoint(wallX, wallY));
				robot.mouseMove((int)CENTER.vx, (int)CENTER.vy);
			}
		}

		public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
			if(buttonNumber == 1){
				if((jpx != 195) && (jpy != 175)){
					//compute wall coords
					precX = wallX;
					precY = wallY;
					wallX += ((jpx - 195) * XYFACT);
					wallY -= ((jpy - 175) * XYFACT);
					//move cursor
					cursor.moveTo(new LongPoint(wallX, wallY));
					robot.mouseMove((int)CENTER.vx, (int)CENTER.vy);
				}
				VSegment glyph = new VSegment(precX, precY, 0, Color.GREEN, wallX, wallY);
				vs.addGlyph(glyph, false);
				glyph.setStrokeWidth(20f);
			}

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

		public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
			if(c == 's'){
				System.out.println("saving state");
				save();
			}
		}

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

