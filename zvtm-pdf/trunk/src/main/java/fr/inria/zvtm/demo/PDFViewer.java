/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.Vector;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.glyphs.ZPDFPage;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFViewer {
	
	static final int NAV_ANIM_DURATION = 300;

	VirtualSpaceManager vsm;
	VirtualSpace vs;
	static final String spaceName = "pdfSpace";
	ViewEventHandler eh;
	Camera mCamera;

	View pdfView;

	PDFViewer(String pdfFilePath){
		vsm = new VirtualSpaceManager();
		vsm.setDebug(true);
		initGUI();
		load(new File(pdfFilePath));
	}

	public void initGUI(){
		vs = vsm.addVirtualSpace(spaceName);
		vsm.setZoomLimit(-90);
		mCamera = vsm.addCamera(spaceName);
		Vector cameras = new Vector();
		cameras.add(mCamera);
		pdfView = vsm.addExternalView(cameras, "ZVTM PDF Viewer", View.STD_VIEW, 800, 600, false, true, true, null);
		pdfView.setBackgroundColor(Color.WHITE);
		eh = new PDFViewerEventHandler(this);
		pdfView.setEventHandler(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(0);
		vsm.repaintNow();
	}

	void load(File f){
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdfFile = new PDFFile(buf);
			for (int i=0;i<pdfFile.getNumPages();i++){
				try {
					vsm.addGlyph(new ZPDFPage(i*700, 0, 0, pdfFile.getPage(i+1), 1, 1), vs);				
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
		new PDFViewer((args.length > 0) ? args[0] : null);
	}

}

class PDFViewerEventHandler implements ViewEventHandler {

	PDFViewer application;

	long lastJPX,lastJPY;

	PDFViewerEventHandler(PDFViewer appli){
		application = appli;
	}

	boolean dragging = false;

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		lastJPX=jpx;
		lastJPY=jpy;
		v.setDrawDrag(true);
		application.vsm.activeView.mouse.setSensitivity(false);
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		application.vsm.getAnimationManager().setXspeed(0);
        application.vsm.getAnimationManager().setYspeed(0);
        application.vsm.getAnimationManager().setZspeed(0);
		v.setDrawDrag(false);
		application.vsm.activeView.mouse.setSensitivity(true);
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			application.vsm.centerOnGlyph(g, application.mCamera, PDFViewer.NAV_ANIM_DURATION);
		}
		else {
			application.vsm.getGlobalView(application.mCamera, PDFViewer.NAV_ANIM_DURATION);
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
			Camera c = application.vsm.getActiveCamera();
			float a = (c.focal+Math.abs(c.altitude))/c.focal;
			if (mod == SHIFT_MOD) {
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
