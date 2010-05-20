/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.glyphs.IcePDFPageImg;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.util.GraphicsRenderingHints;

public class PDFViewer {
	
	static final int NAV_ANIM_DURATION = 300;
	
	static final short DIRECT_G2D_RENDERING = 0;
	static final short OFFSCREEN_IMAGE_RENDERING = 1;
	short rendering_technique = OFFSCREEN_IMAGE_RENDERING;

	VirtualSpace vs;
	static final String spaceName = "pdfSpace";
	ViewEventHandler eh;
	Camera mCamera;

	View pdfView;

	PDFViewer(String pdfFilePath, float df){
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(new File(pdfFilePath), df);
	}

	public void initGUI(){
		vs = VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
		mCamera = vs.addCamera();
		Vector cameras = new Vector();
		cameras.add(mCamera);
		pdfView = VirtualSpaceManager.INSTANCE.addFrameView(cameras, "ZVTM PDF Viewer", View.STD_VIEW, 1024, 768, false, true, true, null);
		pdfView.setBackgroundColor(Color.WHITE);
		eh = new PDFViewerEventHandler(this);
		pdfView.setEventHandler(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(0);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

    void load(File f, float detailFactor){
        Document document = new Document();
        try {
            document.setFile(f.getAbsolutePath());
        } catch (PDFException ex) {
            System.out.println("Error parsing PDF document " + ex);
        } catch (PDFSecurityException ex) {
            System.out.println("Error encryption not supported " + ex);
        } catch (FileNotFoundException ex) {
            System.out.println("Error file not found " + ex);
        } catch (IOException ex) {
            System.out.println("Error handling PDF document " + ex);
        }
        int page_width = (int)document.getPageDimension(0, 0).getWidth();
        // Paint each pages content to an image and write the image to file
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            IcePDFPageImg g = new IcePDFPageImg(i*Math.round(page_width*1.1f*detailFactor), 0, 0, document, i, detailFactor, 1.0f);
            vs.addGlyph(g);
        }
        // clean up resources
        document.dispose();
    }

	public static void main(String[] args){
	    System.getProperties().put("org.icepdf.core.screen.alphaInterpolation", RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.antiAliasing", RenderingHints.VALUE_ANTIALIAS_ON);
        System.getProperties().put("org.icepdf.core.screen.textAntiAliasing", RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        System.getProperties().put("org.icepdf.core.screen.colorRender", RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.dither", RenderingHints.VALUE_DITHER_ENABLE);
        System.getProperties().put("org.icepdf.core.screen.fractionalmetrics", RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        System.getProperties().put("org.icepdf.core.screen.interpolation", RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        System.getProperties().put("org.icepdf.core.screen.render", RenderingHints.VALUE_RENDER_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.stroke", RenderingHints.VALUE_STROKE_PURE);
	    
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

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			application.pdfView.centerOnGlyph(g, application.mCamera, PDFViewer.NAV_ANIM_DURATION);
		}
		else {
			application.pdfView.getGlobalView(application.mCamera, PDFViewer.NAV_ANIM_DURATION);
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
