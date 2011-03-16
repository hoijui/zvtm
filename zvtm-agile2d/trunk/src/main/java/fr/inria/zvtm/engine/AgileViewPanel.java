/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import java.util.Vector;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.event.ViewListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;

import agile2d.AgileGraphics2D;
import agile2d.AgileState;

/**
 * JPanel used to paint the content of a view (all camera layers).
 * Uses OpenGL acceletation provided by the Agile2D rendering pipeline (itself based upon JOGL 2.0).<br>
 * <a href="http://agile2d.sourceforge.net/">Agile2D homepage</a><br>
 * <a href="http://download.java.net/media/jogl/jogl-2.x-docs/">JOGL 2 javadoc</a><br>
 * @author Emmanuel Pietriga, Rodrigo A. B. de Almeida
 */

public abstract class AgileViewPanel extends ViewPanel implements GLEventListener {
    
    private AgileGraphics2D jgraphics;
    private Component       root;
    
	Timer edtTimer;

    void stop(){
        edtTimer.stop();
    }
    
    public void init(GLAutoDrawable drawable){
        // Called by the drawable immediately after the OpenGL context is initialized.
        jgraphics = new AgileGraphics2D(drawable);
        GL2 gl = drawable.getGL().getGL2();
        if (VirtualSpaceManager.INSTANCE.debugModeON()){
            System.out.println("Agile2D:: INIT GL IS: " + gl.getClass().getName());
        }
        gl.setSwapInterval(1);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
        // Called by the drawable during the first repaint after the component has been resized.
        size = new Dimension(width, height);
    }

    public void dispose(GLAutoDrawable drawable){
        // Notifies the listener to perform the release of all OpenGL resources per GLContext, such as memory buffers and GLSL programs.

    }

    public void display(GLAutoDrawable drawable){
        // Called by the drawable to initiate OpenGL rendering by the client.
        GL2 gl = drawable.getGL().getGL2();
        // Restore all the Java2D Graphics defaults
        jgraphics.resetAll(drawable);        
    	this.paint(jgraphics, gl);
    }

    void paint(Graphics g, GL2 gl) {
        // stableRefToBackBufferGraphics is used here not as a Graphics from a back buffer image, but directly as the OpenGL graphics context
        // (simply reusing an already declared var instead of creating a new one for nothing)
        stableRefToBackBufferGraphics = (Graphics2D)g;
        try {
            standardStroke = stableRefToBackBufferGraphics.getStroke();
            standardTransform = stableRefToBackBufferGraphics.getTransform();
            if (notBlank){
                if (repaintASAP || updateCursorOnly){
                    repaintASAP=false; //do this first as the thread can be interrupted inside
    				//this branch and we want to catch new requests for repaint
    				updateCursorOnly=false;
                    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                    //stableRefToBackBufferGraphics.setPaintMode();
                    stableRefToBackBufferGraphics.setBackground(backColor);
                    stableRefToBackBufferGraphics.clearRect(0, 0, size.width, size.height);
                    backgroundHook();
                    //begin actual drawing here
                    for (int nbcam=0;nbcam<cams.length;nbcam++){
                        if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
                            camIndex = cams[nbcam].getIndex();
                            drawnGlyphs = cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
                            drawnGlyphs.removeAllElements();
                            double uncoef = (cams[nbcam].focal+cams[nbcam].altitude) / cams[nbcam].focal;
                            //compute region seen from this view through camera
                            double viewW = size.width;
                            double viewH = size.height;
                            double viewWC = cams[nbcam].vx - (viewW/2-visibilityPadding[0]) * uncoef;
                            double viewNC = cams[nbcam].vy + (viewH/2-visibilityPadding[1]) * uncoef;
                            double viewEC = cams[nbcam].vx + (viewW/2-visibilityPadding[2]) * uncoef;
                            double viewSC = cams[nbcam].vy - (viewH/2-visibilityPadding[3]) * uncoef;
                            gll = cams[nbcam].parentSpace.getDrawingList();
                            for (int i=0;i<gll.length;i++){
                                if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cams[nbcam])){
                                    //if glyph is at least partially visible in the reg. seen from this view, display
                                    gll[i].project(cams[nbcam], size);
                                    if (gll[i].isVisible()){
                                        gll[i].draw(stableRefToBackBufferGraphics, size.width, size.height, cams[nbcam].getIndex(), standardStroke, standardTransform, 0, 0);
                                    }
                                    // notifying outside if branch because glyph sensitivity is not
                                    // affected by glyph visibility when managed through Glyph.setVisible()
                                    cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
                                }
                            }
                        }
                    }
                    foregroundHook();
                    afterLensHook();
                    drawPortals();
                    portalsHook();
                    if (cursor_inside){
                        //deal with mouse glyph only if mouse cursor is inside this window
                        try {
                            //we project the mouse cursor wrt the appropriate coord sys
                            parent.mouse.unProject(cams[activeLayer], this);
                            if (parent.mouse.isSensitive()){
                                parent.mouse.getPicker().computePickedGlyphList(evHs[activeLayer], cams[activeLayer]);
                            }
                        }
                        catch (NullPointerException ex) {if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.drawdrag "+ex);}}
                        stableRefToBackBufferGraphics.setColor(parent.mouse.hcolor);
                        if (drawDrag){stableRefToBackBufferGraphics.drawLine(origDragx,origDragy,parent.mouse.jpx,parent.mouse.jpy);}
                        if (drawRect){stableRefToBackBufferGraphics.drawRect(Math.min(origDragx,parent.mouse.jpx),Math.min(origDragy,parent.mouse.jpy),Math.max(origDragx,parent.mouse.jpx)-Math.min(origDragx,parent.mouse.jpx),Math.max(origDragy,parent.mouse.jpy)-Math.min(origDragy,parent.mouse.jpy));}
                        if (drawOval){
                            if (circleOnly){
                                stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.jpx),origDragy-Math.abs(origDragx-parent.mouse.jpx),2*Math.abs(origDragx-parent.mouse.jpx),2*Math.abs(origDragx-parent.mouse.jpx));
                            }
                            else {
                                stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.jpx),origDragy-Math.abs(origDragy-parent.mouse.jpy),2*Math.abs(origDragx-parent.mouse.jpx),2*Math.abs(origDragy-parent.mouse.jpy));
                            }
                        }
                        if (drawVTMcursor){
                            parent.mouse.draw(stableRefToBackBufferGraphics);
                            oldX=parent.mouse.jpx;
                            oldY=parent.mouse.jpy;
                        }
                    }
                    //end drawing here                    
                }
            }
            else {
                stableRefToBackBufferGraphics.setColor(blankColor);
                stableRefToBackBufferGraphics.fillRect(0, 0, size.width, size.height);
                portalsHook();
            }
        }
        catch (NullPointerException ex0){if (VirtualSpaceManager.debugModeON()){System.err.println("AgileViewPanel.paint "+ex0);}}
        if (repaintListener != null){repaintListener.viewRepainted(this.parent);}
    }

    @Override
    public void setRefreshRate(int rr){
        edtTimer.setDelay(rr);
    }

    @Override
    public int getRefreshRate(){
        return edtTimer.getDelay();
    }

    /** Not implemented yet. */
    @Override
    public BufferedImage getImage(){
	    return null;
    }

}
