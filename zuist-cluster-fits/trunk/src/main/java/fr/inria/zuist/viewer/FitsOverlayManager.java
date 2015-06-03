/*   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.OverlayLayout;
import javax.swing.JLayeredPane;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Insets;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.util.Scanner;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.RImage;

//import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.event.ViewListener;

import fr.inria.zvtm.widgets.TranslucentTextArea;

//class FitsOverlayManager implements ViewEventHandler {
class FitsOverlayManager implements ViewListener {

	static String VERSION;

	static {
	    Scanner sc = new Scanner(Messages.class.getResourceAsStream("/p/v_properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                VERSION = sc.next();
            }
        }
	}

    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;

    FitsViewer application;

	TranslucentTextArea console;

	// west, east and south margins of console (north bound depends on frame's height)
	int[] consoleMarginsWES = {10, 10, 10};

	int[] consolePaddingWNES = {5, 5, 5, 5};

    FitsOverlayManager(FitsViewer app){
        this.application = app;
    }

	void initConsole(){
		JFrame f = (JFrame)application.mView.getFrame();
		JLayeredPane lp = f.getRootPane().getLayeredPane();
		console = new TranslucentTextArea("");
		console.setEditable(false);
		console.setMargin(new Insets(consolePaddingWNES[1], consolePaddingWNES[0], consolePaddingWNES[3], consolePaddingWNES[2]));
		lp.add(console, (Integer)(JLayeredPane.DEFAULT_LAYER+33));
		updateConsoleBounds();
	}

	void updateConsoleBounds(){
		console.setBounds(consoleMarginsWES[0], Math.round(application.panelHeight*.8f),
		                  application.panelWidth-consoleMarginsWES[1]-consoleMarginsWES[0], Math.round(application.panelHeight*.2f-consoleMarginsWES[2]));
	}

	void toggleConsole(){
		console.setVisible(!console.isVisible());
		if (console.isVisible()){
		    application.consoleMI.setText(Messages.CONSOLE_HIDE);
		}
		else {
		    application.consoleMI.setText(Messages.CONSOLE_SHOW);
		}
	}

	void sayInConsole(String text){
		console.setText(console.getText()+text);
	}

    boolean showingAbout = false;
    boolean showingShortcut = false;
    VRectangle fadeAbout;
    VImage insituLogo, inriaLogo;
    VText[] aboutLines;

    void showAbout(){
        if (!showingAbout){
            fadeAbout = new VRectangle(0, 0, 0, 600, 400,
                FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[5];
            aboutLines[0] = new VText(0, 100, 0, Color.WHITE, "ZUIST FITS Image Viewer", VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 40, 0, Color.WHITE, "v"+VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 0, 0, Color.WHITE, "Romain Primet, Fernando del Campo, Emmanuel Pietriga", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[3] = new VText(0, -120, 0, Color.WHITE, "Based on the ZVTM toolkit", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[4] = new VText(0, -160, 0, Color.WHITE, "http://zvtm.sf.net", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            application.ovSpace.addGlyph(fadeAbout);
			for (int i=0;i<aboutLines.length;i++){
	            application.ovSpace.addGlyph(aboutLines[i]);
			}
            showingAbout = true;
        }
		application.mView.setActiveLayer(application.LAYER_OVERLAY);
    }

    void hideAbout(){
        if (showingAbout){
            showingAbout = false;
            if (insituLogo != null){
                application.ovSpace.removeGlyph(insituLogo);
                insituLogo = null;
            }
            if (inriaLogo != null){
                application.ovSpace.removeGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null){
                application.ovSpace.removeGlyph(fadeAbout);
                fadeAbout = null;
            }
			for (int i=0;i<aboutLines.length;i++){
	            if (aboutLines[i] != null){
	                application.ovSpace.removeGlyph(aboutLines[i]);
	                aboutLines[i] = null;
	            }
			}
		}
		application.mView.setActiveLayer(application.LAYER_SCENE);
	}

	void showShortcut(){
        if (!showingShortcut){
            fadeAbout = new VRectangle(0, 0, 0, 600, 400,
                FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[27];
			aboutLines[0] = new VText(-290, 175, 0, Color.WHITE, "ZUIST FITS Image Viewer", VText.TEXT_ANCHOR_START, 2.0f);
            aboutLines[1] = new VText(-285+50, 140, 0, Color.WHITE, "Key", VText.TEXT_ANCHOR_START, 1.5f);
            aboutLines[2] = new VText(-185+50, 140, 0, Color.WHITE, "Description", VText.TEXT_ANCHOR_START, 1.5f);
            aboutLines[3] = new VText(-275+50, 120, 0, Color.WHITE, "F1", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[4] = new VText(-175+50, 120, 0, Color.WHITE, "Set previous color mapping", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[5] = new VText(-275+50, 100, 0, Color.WHITE, "F2", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[6] = new VText(-175+50, 100, 0, Color.WHITE, "Set next color mapping", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[7] = new VText(-275+50, 80, 0, Color.WHITE, "F3", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[8] = new VText(-175+50, 80, 0, Color.WHITE, "Set previous scale method", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[9] = new VText(-275+50, 60, 0, Color.WHITE, "F4", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[10] = new VText(-175+50, 60, 0, Color.WHITE, "Set next scale method", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[11] = new VText(-275+50, 40, 0, Color.WHITE, "L", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[12] = new VText(-175+50, 40, 0, Color.WHITE, "Set scale method with local values", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[13] = new VText(-275+50, 20, 0, Color.WHITE, "G", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[14] = new VText(-175+50, 20, 0, Color.WHITE, "Set scale method with global values", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[15] = new VText(-275+50, 0, 0, Color.WHITE, "1", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[16] = new VText(-175+50, 0, 0, Color.WHITE, "Set Ks layer", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[17] = new VText(-275+50, -20, 0, Color.WHITE, "2", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[18] = new VText(-175+50, -20, 0, Color.WHITE, "Set Ks/H layer", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[19] = new VText(-275+50, -40, 0, Color.WHITE, "3", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[20] = new VText(-175+50, -40, 0, Color.WHITE, "Set H layer", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[21] = new VText(-275+50, -60, 0, Color.WHITE, "4", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[22] = new VText(-175+50, -60, 0, Color.WHITE, "Set H/J layer", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[23] = new VText(-275+50, -80, 0, Color.WHITE, "5", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[24] = new VText(-175+50, -80, 0, Color.WHITE, "Set J layer", VText.TEXT_ANCHOR_START, 1.3f);
             aboutLines[25] = new VText(-275+50, -100, 0, Color.WHITE, "6", VText.TEXT_ANCHOR_START, 1.3f);
            aboutLines[26] = new VText(-175+50, -100, 0, Color.WHITE, "Set Ks/H/J layer", VText.TEXT_ANCHOR_START, 1.3f);
            application.ovSpace.addGlyph(fadeAbout);
			for (int i=0;i<aboutLines.length;i++){
	            application.ovSpace.addGlyph(aboutLines[i]);
			}
            showingShortcut = true;
        }
		application.mView.setActiveLayer(application.LAYER_OVERLAY);
    }

    void hideShortcut(){
        if (showingShortcut){
            showingShortcut = false;
            if (insituLogo != null){
                application.ovSpace.removeGlyph(insituLogo);
                insituLogo = null;
            }
            if (inriaLogo != null){
                application.ovSpace.removeGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null){
                application.ovSpace.removeGlyph(fadeAbout);
                fadeAbout = null;
            }
			for (int i=0;i<aboutLines.length;i++){
	            if (aboutLines[i] != null){
	                application.ovSpace.removeGlyph(aboutLines[i]);
	                aboutLines[i] = null;
	            }
			}
		}
		application.mView.setActiveLayer(application.LAYER_SCENE);
	}

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		hideAbout();
		hideShortcut();
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

	public void enterGlyph(Glyph g){}

	public void exitGlyph(Glyph g){}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		hideAbout();
		hideShortcut();
	}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){
		application.exit();
	}
}
