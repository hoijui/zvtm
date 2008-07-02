/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: OverlayManager.java,v 1.5 2007/10/07 13:36:24 pietriga Exp $
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

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VRectangleST;
import net.claribole.zvtm.glyphs.VTextST;
import net.claribole.zvtm.glyphs.RImage;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.widgets.TranslucentTextArea;

class OverlayManager implements ViewEventHandler {
    
    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;

    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";

    Viewer application;

	JPanel consolePane;
	TranslucentTextArea console;

    OverlayManager(Viewer app){
        this.application = app;
    }

	void initConsole(){
		JFrame f = (JFrame)application.mView.getFrame();
		JLayeredPane lp = f.getRootPane().getLayeredPane();
		lp.setLayout(new OverlayLayout(lp));
		consolePane = new JPanel();
		consolePane.setOpaque(false);
		consolePane.setLayout(new BorderLayout());
		console = new TranslucentTextArea("Console");
		console.setPreferredSize(new Dimension((int)Math.round(application.panelWidth*0.9), (int)Math.round(application.panelHeight*0.2)));
		consolePane.add(console, BorderLayout.SOUTH);
		lp.add(consolePane, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
		consolePane.setVisible(false);
	}
	
	void toggleConsole(){
		consolePane.setVisible(!consolePane.isVisible());
	}
	
	void sayInConsole(String text){
		console.append(text);
	}
    
    boolean showingAbout = false;
    VRectangleST fadeAbout;
    VImage insituLogo, inriaLogo;
    VText[] aboutLines;
    
    void showAbout(){
        if (!showingAbout){
            fadeAbout = new VRectangleST(0, 0, 0, Math.round(application.panelWidth/2.1), Math.round(application.panelHeight/3),
                FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[5];
			aboutLines[0] = new VText(0, 150, 0, Color.WHITE, "ZUIST Viewer", VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 110, 0, Color.WHITE, "v"+Messages.VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 0, 0, Color.WHITE, "By Emmanuel Pietriga (INRIA) & Michel Beaudouin-Lafon (Universit\u00E9 Paris-Sud)", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            RImage.setReflectionHeight(0.7f);
            inriaLogo = new RImage(-150, -70, 0, (new ImageIcon(this.getClass().getResource(INRIA_LOGO_PATH))).getImage(), 1.0f);
            insituLogo = new RImage(200, -70, 0, (new ImageIcon(this.getClass().getResource(INSITU_LOGO_PATH))).getImage(), 1.0f);
            aboutLines[3] = new VText(0, -170, 0, Color.WHITE, "Based on the ZVTM toolkit", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[4] = new VText(0, -200, 0, Color.WHITE, "http://zvtm.sf.net", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            application.vsm.addGlyph(fadeAbout, application.ovSpace);
            application.vsm.addGlyph(inriaLogo, application.ovSpace);
            application.vsm.addGlyph(insituLogo, application.ovSpace);
			for (int i=0;i<aboutLines.length;i++){
	            application.vsm.addGlyph(aboutLines[i], application.ovSpace);				
			}
            showingAbout = true;
        }
		application.mView.setActiveLayer(2);
    }

    void hideAbout(){
        if (showingAbout){
            showingAbout = false;
            if (insituLogo != null){
                application.ovSpace.destroyGlyph(insituLogo);
                insituLogo = null;
            }
            if (inriaLogo != null){
                application.ovSpace.destroyGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null){
                application.ovSpace.destroyGlyph(fadeAbout);
                fadeAbout = null;
            }
			for (int i=0;i<aboutLines.length;i++){
	            if (aboutLines[i] != null){
	                application.ovSpace.destroyGlyph(aboutLines[i]);
	                aboutLines[i] = null;
	            }				
			}
		}
		application.mView.setActiveLayer(0);
	}

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		hideAbout();
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
