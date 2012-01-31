/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *  $
 */

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import java.util.Vector;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.event.ViewListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.awt.NewtCanvasAWT;

import agile2d.AgileGraphics2D;
import agile2d.AgileState;

/**
 * NewtCanvas used to paint the content of a view (all camera layers).
 * Uses OpenGL acceletation provided by the Agile2D rendering pipeline (itself based upon JOGL 2.0).<br>
 * <a href="http://agile2d.sourceforge.net/">Agile2D homepage</a><br>
 * <a href="http://download.java.net/media/jogl/jogl-2.x-docs/">JOGL 2 javadoc</a><br>
 * <a href="http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc">JogAmp javadoc</a><br>
 * <a href="http://jogamp.org/jogl/doc/NEWT-Overview.html">NEWT</a><br>
 * Before instantiating an Agile2D ZVTM View, one must register the new view type:<br>
 * View.registerViewPanelType(AgileGLJPanelFactory.AGILE_NEWT_VIEW, new AgileNewtCanvasFactory());<br><br>
 * Then the view gets created as any other view:<br>
 * View v = VirtualSpaceManager.INSTANCE.addFrameView(cameras, View.ANONYMOUS, AgileNewtCanvasFactory.AGILE_NEWT_VIEW, 800, 600, true);
 * @author Emmanuel Pietriga, Rodrigo A. B. de Almeida
 */

public class AgileNewtViewPanel extends AgileViewPanel {
    
    protected NewtCanvasAWT panel;
    protected GLWindow window;
    
    public Component getComponent(){
        return panel;
    }
    
    AgileNewtViewPanel(Vector cameras, View v, boolean arfome) {
        GLProfile myGLProfile = GLProfile.get(GLProfile.GL2);
		GLCapabilities caps = new GLCapabilities(myGLProfile);
		//caps.setDoubleBuffered(true);

        window = GLWindow.create(caps); 
		panel = new NewtCanvasAWT(window);
        window.addGLEventListener(this);
        
        // made not focusable because otherwise key events are lost when the panel gets focus
        // only true for Agile2D-based ViewPanel types, not for the default one.
        panel.setFocusable(false);
        		
        ActionListener taskPerformer = new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                panel.repaint();
            }
        };
        edtTimer = new Timer(DEFAULT_DELAY, taskPerformer);
        panel.addHierarchyListener(
            new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if (panel.isShowing()){
                        start();
                    } else {
                        stop();
                    }
                }
            }
        );
        parent=v;
        //init of camera array
        cams=new Camera[cameras.size()];  //array of Camera
        evHs = new ViewListener[cams.length];
        for (int nbcam=0;nbcam<cameras.size();nbcam++){
            cams[nbcam]=(Camera)(cameras.get(nbcam));
        }
        //init other stuff
        /*
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
        panel.addComponentListener(this);
        */
        
        //init other stuff
	/*
        window.addMouseListener(this);
        window.addMouseMotionListener(this);
        window.addMouseWheelListener(this);
        window.addComponentListener(this);
        */
        
        setAutoRequestFocusOnMouseEnter(arfome);
        setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
        //this.size = this.getSize();
        if (VirtualSpaceManager.debugModeON()){System.out.println("View refresh time set to "+getRefreshRate()+"ms");}
        start();
    }

    private void start(){
        edtTimer.start();
    }

}
