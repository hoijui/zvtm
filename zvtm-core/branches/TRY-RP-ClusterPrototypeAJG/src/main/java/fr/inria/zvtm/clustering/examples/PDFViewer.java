/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: PDFViewer.java 2322 2009-09-08 10:08:29Z epietrig $
 */

package fr.inria.zvtm.clustering.examples;

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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFViewer {
	
	static final int NAV_ANIM_DURATION = 300;
	
	static final short DIRECT_G2D_RENDERING = 0;
	static final short OFFSCREEN_IMAGE_RENDERING = 1;
	short rendering_technique = OFFSCREEN_IMAGE_RENDERING;

	VirtualSpace vs;
	static final String spaceName = "testSpace";
	ViewEventHandler eh;
	Camera mCamera;

	View pdfView;

	PDFViewer(String pdfFilePath, float df){
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(pdfFilePath, df);
	}

	public void initGUI(){
		vs = VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
		mCamera = VirtualSpaceManager.INSTANCE.addCamera(vs);
		Vector cameras = new Vector();
		cameras.add(mCamera);
		pdfView = VirtualSpaceManager.INSTANCE.addExternalView(cameras, "ZVTM PDF Viewer", View.STD_VIEW, 800, 600, false, true, true, null);
		pdfView.setBackgroundColor(Color.WHITE);
		eh = new PDFViewerEventHandler(this);
		pdfView.setEventHandler(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(0);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	void load(String filePath, float detailFactor){
		try {
			File f = new File(filePath);
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdfFile = new PDFFile(buf);
			int page_width = (int)pdfFile.getPage(0).getBBox().getWidth();
			for (int i=0;i<pdfFile.getNumPages();i++){
				try {
					// OFFSCREEN_IMAGE_RENDERING
					vs.addGlyph(new ZPDFPageImg(i*Math.round(page_width*1.1f*detailFactor), i*Math.round(page_width*1.1f*detailFactor), 0, filePath, i+1));
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
		new PDFViewer((args.length > 0) ? args[0] : null, (args.length > 1) ? Float.parseFloat(args[1]) : 1);
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
		VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(false);
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
		v.setDrawDrag(false);
		VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(true);
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

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
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/45.0f)) : (long)((lastJPY-jpy)/(a*45)));
			}
			else {
			    VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/10.0f)) : (long)((jpx-lastJPX)/(a*10)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/10.0f)) : (long)((lastJPY-jpy)/(a*10)));
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
