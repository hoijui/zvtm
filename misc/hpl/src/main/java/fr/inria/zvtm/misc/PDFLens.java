/* $Id:$ */

package fr.inria.zvtm.misc;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.glyphs.ZPDFPageImg;
import fr.inria.zvtm.glyphs.ZPDFPageG2D;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFLens {
	
	int VIEW_W = 1024;
	int VIEW_H = 768;
	
	static final int NAV_ANIM_DURATION = 300;
	
	static final short DIRECT_G2D_RENDERING = 0;
	static final short OFFSCREEN_IMAGE_RENDERING = 1;
	short rendering_technique = OFFSCREEN_IMAGE_RENDERING;

	VirtualSpace vs;
	static final String spaceName = "pdfSpace";
	ViewEventHandler eh;
	Camera mCamera;

	View pdfView;

	PDFLens(String pdfFilePath, float df){
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(new File(pdfFilePath), df);
	}

	public void initGUI(){
		vs = VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
		mCamera = VirtualSpaceManager.INSTANCE.addCamera(spaceName);
		Vector cameras = new Vector();
		cameras.add(mCamera);
		pdfView = VirtualSpaceManager.INSTANCE.addExternalView(cameras, "High precision lenses on PDF", View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
		pdfView.setBackgroundColor(Color.WHITE);
		eh = new PDFLensEventHandler(this);
		pdfView.setEventHandler(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(0);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	void load(File f, float detailFactor){
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdfFile = new PDFFile(buf);
			int page_width = (int)pdfFile.getPage(0).getBBox().getWidth();
			for (int i=0;i<pdfFile.getNumPages();i++){
				try {
				    if (rendering_technique == DIRECT_G2D_RENDERING){
				        vs.addGlyph(new ZPDFPageG2D(i*Math.round(page_width*1.1f), i*Math.round(page_width*1.1f), 0, pdfFile.getPage(i+1), detailFactor));
				    }
				    else {
				        // OFFSCREEN_IMAGE_RENDERING
				        vs.addGlyph(new ZPDFPageImg(i*Math.round(page_width*1.1f*detailFactor), i*Math.round(page_width*1.1f*detailFactor), 0, pdfFile.getPage(i+1), detailFactor, 1));
				    }
				}
				catch ( Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch ( Exception e) { 
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		System.out.println("-----------------");
		System.out.println("General information");
		System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
		System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
		System.out.println("-----------------");
		System.out.println("Directory information");
		System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
		System.out.println("Java directory: "+System.getProperty("java.home"));
		System.out.println("Launching from: "+System.getProperty("user.dir"));
		System.out.println("-----------------");
		System.out.println("User informations");
		System.out.println("User name: "+System.getProperty("user.name"));
		System.out.println("User home directory: "+System.getProperty("user.home"));
		System.out.println("-----------------");
		new PDFLens((args.length > 0) ? args[0] : null, (args.length > 1) ? Float.parseFloat(args[1]) : 1);
	}

}

class PDFLensEventHandler implements ViewEventHandler {

	PDFLens application;

	long lastJPX,lastJPY;

	PDFLensEventHandler(PDFLens appli){
		application = appli;
	}

	boolean dragging = false;

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		lastJPX=jpx;
		lastJPY=jpy;
		v.setDrawDrag(true);
		VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(false);
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
		v.setDrawDrag(false);
		VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(true);
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			VirtualSpaceManager.INSTANCE.centerOnGlyph(g, application.mCamera, PDFLens.NAV_ANIM_DURATION);
		}
		else {
			VirtualSpaceManager.INSTANCE.getGlobalView(application.mCamera, PDFLens.NAV_ANIM_DURATION);
		}
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

	}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
		if (buttonNumber == 1){
			Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
			float a = (c.focal+Math.abs(c.altitude))/c.focal;
			if (mod == SHIFT_MOD) {
			    VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
				//50 is just a speed factor (too fast otherwise)
			}
			else {
			    VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
			}
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
		float a = (c.focal+Math.abs(c.altitude))/c.focal;
		if (wheelDirection == WHEEL_UP){
			c.altitudeOffset(-a*5);
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
		else {
			//wheelDirection == WHEEL_DOWN
			c.altitudeOffset(a*5);
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
	}

	public void enterGlyph(Glyph g){
	}

	public void exitGlyph(Glyph g){
	}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){System.exit(0);}

}
