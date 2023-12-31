/*   FILE: StdViewPanel.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2014. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.AlphaComposite;

import javax.swing.Timer;
import javax.swing.JPanel;

import java.util.Arrays;
import java.util.Vector;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Translucent;

import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;

/**
 * JPanel used to paint the content of a view (all camera layers).
 * Manual double buffering, to allow modifications to the rendering before it gets painted on screen (e.g., applying magnification lenses).
 * @author Emmanuel Pietriga
 */

public class StdViewPanel extends ViewPanel {

    protected JPanel panel;

    /**Get the underlying Swing component.
     *@return the underlying JPanel
     */
    public Component getComponent(){
        return panel;
    }

    /** Double Buffering uses a BufferedImage as the back buffer. */
    BufferedImage backBuffer;
    int backBufferW = 0;
    int backBufferH = 0;

    private Graphics2D backBufferGraphics = null;
    Dimension oldSize;
    Graphics2D lensG2D = null;

    /** Double Buffering uses a BufferedImage as the overlay buffer. */
    BufferedImage overlayBuffer;
    private Graphics2D overlayBufferGraphics = null;

    private Timer edtTimer;

    StdViewPanel(Vector<Camera> cameras,View v, boolean arfome) {
        panel = new JPanel(){
            @Override
            public void paint(Graphics g) {
                if (backBuffer != null){
                    g.drawImage(backBuffer, 0, 0, panel);
                }
                for (int i=0;i<parent.portals.length;i++){
                    Portal p = parent.portals[i];
                    BufferedImage bi = p.getBufferImage();
                    if (bi != null){
                        AlphaComposite ac = p.getAlphaComposite();
                        if (ac != null){
                            ((Graphics2D)g).setComposite(ac);
                        }
                        g.drawImage(bi, p.getBufferX(), p.getBufferY(), panel);
                        if (ac != null){
                            ((Graphics2D)g).setComposite(Translucent.acO);
                        }
                    }     
                }
                if (overlayBuffer != null){
                     g.drawImage(overlayBuffer, 0, 0, panel);
                }
            }
        };

        ActionListener taskPerformer = new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                drawOffscreen();
            }
        };
        edtTimer = new Timer(DEFAULT_DELAY, taskPerformer);

        panel.addHierarchyListener(
                new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                        if (panel.isShowing()) {
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
            cams[nbcam] = cameras.get(nbcam);
        }
        visibilityPadding = new int[cams.length][4];
        for (int i=0;i<visibilityPadding.length;i++){
            Arrays.fill(visibilityPadding[i], 0);
        }
        drawInLens = new boolean[cams.length];
        drawInContext = new boolean[cams.length];
        Arrays.fill(drawInLens, true);
        Arrays.fill(drawInContext, true);
        //init other stuff
        panel.setBackground(backColor);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
        panel.addComponentListener(this);
        panel.setDoubleBuffered(false);
        setAutoRequestFocusOnMouseEnter(arfome);
        setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
        this.size = panel.getSize();
        if (VirtualSpaceManager.debugModeON()){System.out.println("View refresh time set to "+getRefreshRate()+"ms");}
    }

    private void start(){
        backBufferGraphics = null;
        overlayBufferGraphics = null;
        edtTimer.start();
    }

    void stop(){
        edtTimer.stop();
        if (stableRefToBackBufferGraphics != null) {
            stableRefToBackBufferGraphics.dispose();
        }
        if (stableRefToOverlayBufferGraphics != null) {
            stableRefToOverlayBufferGraphics.dispose();
        }
    }

    private void updateOffscreenBuffer(){
        size = panel.getSize();
        if (size.width != oldSize.width || size.height != oldSize.height || backBufferW != size.width || backBufferH != size.height) {
            //each time the parent window is resized, adapt the buffer image size
            backBuffer = null;
            if (backBufferGraphics != null) {
                backBufferGraphics.dispose();
                backBufferGraphics = null;
            }
            if (lens != null){
                lens.resetMagnificationBuffer();
                if (lensG2D != null) {
                    lensG2D.dispose();
                    lensG2D = null;
                }
            }
            overlayBuffer = null;
            if (overlayBufferGraphics != null) {
                overlayBufferGraphics.dispose();
                overlayBufferGraphics = null;
            }
            if (VirtualSpaceManager.debugModeON()){
                System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");
            }
            oldSize=size;
            updateAntialias=true;
            updateFont=true;
        }
        if (backBuffer == null){
            gconf = panel.getGraphicsConfiguration();
            // assign minimal size of 1
            backBuffer = gconf.createCompatibleImage((size.width > 0) ? size.width : 1, (size.height > 0) ? size.height : 1);
            backBufferW = backBuffer.getWidth();
            backBufferH = backBuffer.getHeight();
            if (backBufferGraphics != null){
                backBufferGraphics.dispose();
                backBufferGraphics = null;
            }
        }
        if (parent.overlayCamera != null && overlayBuffer == null){
            gconf = panel.getGraphicsConfiguration();
            // assign minimal size of 1
            overlayBuffer = gconf.createCompatibleImage((size.width > 0) ? size.width : 1, (size.height > 0) ? size.height : 1, BufferedImage.TYPE_INT_ARGB);
            if (overlayBufferGraphics != null){
                overlayBufferGraphics.dispose();
                overlayBufferGraphics = null;
            }
        }
        if (backBufferGraphics == null) {
            backBufferGraphics = backBuffer.createGraphics();
            updateAntialias=true;
            updateFont=true;
        }
        if (parent.overlayCamera != null && overlayBufferGraphics == null) {
            overlayBufferGraphics = overlayBuffer.createGraphics();
            updateAntialias=true;
            updateFont=true;
        }
        if (lens != null){
            lensG2D = lens.getMagnificationGraphics();
            lensG2D.setFont(VText.getMainFont());
            if (antialias){
                lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            else {
                lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }
        if (updateFont){
            backBufferGraphics.setFont(VText.getMainFont());
            if (overlayBufferGraphics != null){
                overlayBufferGraphics.setFont(VText.getMainFont());
            }
            if (lensG2D != null){
                lensG2D.setFont(VText.getMainFont());
            }
            updateFont = false;
        }
        if (updateAntialias){
            if (antialias){
                backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (overlayBufferGraphics != null){
                    overlayBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                if (lensG2D != null){
                    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
            }
            else {
                backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                if (overlayBufferGraphics != null){
                    overlayBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                if (lensG2D != null){
                    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
            }
            updateAntialias = false;
        }
        stableRefToBackBufferGraphics = backBufferGraphics;
        stableRefToOverlayBufferGraphics = overlayBufferGraphics;
        standardStroke=stableRefToBackBufferGraphics.getStroke();
        standardTransform=stableRefToBackBufferGraphics.getTransform();
    }

    private void drawScene(boolean drawLens){
        if(drawLens){
            if (lensG2D == null){
                updateOffscreenBuffer();
            }
            lensG2D.setPaintMode(); // to the lens from LAnimation.animate() methods and this thread
            lensG2D.setBackground(backColor);
            lensG2D.clearRect(0, 0, lens.mbw, lens.mbh);
        }
        for (int nbcam=0;nbcam<cams.length;nbcam++){
            if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
                camIndex=cams[nbcam].getIndex();
                drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
                drawnGlyphs.removeAllElements();
                double uncoef = (cams[nbcam].focal+cams[nbcam].altitude) / cams[nbcam].focal;
                //compute region seen from this view through camera
                double viewW = size.width;
                double viewH = size.height;
                double viewWC = cams[nbcam].vx - (viewW/2-visibilityPadding[nbcam][0]) * uncoef;
                double viewNC = cams[nbcam].vy + (viewH/2-visibilityPadding[nbcam][1]) * uncoef;
                double viewEC = cams[nbcam].vx + (viewW/2-visibilityPadding[nbcam][2]) * uncoef;
                double viewSC = cams[nbcam].vy - (viewH/2-visibilityPadding[nbcam][3]) * uncoef;
                double lviewWC = 0;
                double lviewNC = 0;
                double lviewEC = 0;
                double lviewSC = 0;
                double lensVx = 0;
                double lensVy = 0;
                if (drawLens && drawInLens[nbcam]){
                    lviewWC = cams[nbcam].vx + (lens.lx-lens.lensWidth/2) * uncoef;
                    lviewNC = cams[nbcam].vy + (-lens.ly+lens.lensHeight/2) * uncoef;
                    lviewEC = cams[nbcam].vx + (lens.lx+lens.lensWidth/2) * uncoef;
                    lviewSC = cams[nbcam].vy + (-lens.ly-lens.lensHeight/2) * uncoef;
                    lensVx = (lviewWC+lviewEC) / 2d;
                    lensVy = (lviewSC+lviewNC) / 2d;
                }
                gll = cams[nbcam].parentSpace.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cams[nbcam])){
                            /* if glyph is at least partially visible in the reg. seen from this view,
                               compute in which buffer it should be rendered: */
                            /* always draw in the main buffer */
                            if (drawInContext[nbcam]){
                                gll[i].project(cams[nbcam], size);
                                if (gll[i].isVisible()){
                                    gll[i].draw(stableRefToBackBufferGraphics, size.width, size.height, cams[nbcam].getIndex(),
                                            standardStroke, standardTransform, 0, 0);
                                }
                            }
                            if (drawLens && drawInLens[nbcam]){
                                if (gll[i].visibleInViewport(lviewWC, lviewNC, lviewEC, lviewSC, cams[nbcam])){
                                    /* partially within the region seen through the lens
                                       draw it in both buffers */
                                    gll[i].projectForLens(cams[nbcam], lens.mbw, lens.mbh, lens.getMaximumMagnification(), lensVx, lensVy);
                                    if (gll[i].isVisibleThroughLens()){
                                        gll[i].drawForLens(lensG2D, lens.mbw, lens.mbh, cams[nbcam].getIndex(),
                                                standardStroke, standardTransform, 0, 0);
                                    }
                                }
                            }
                            /* notifying outside of above test because glyph sensitivity is not
                               affected by glyph visibility when managed through Glyph.setVisible() */
                            cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
                        }
                    }
                }
            }
        }
        foregroundHook();
        if(drawLens){
            try {
                lens.transform(backBuffer);
                lens.drawBoundary(stableRefToBackBufferGraphics);
            }
            catch (ArrayIndexOutOfBoundsException ex){
                if (VirtualSpaceManager.debugModeON()){ex.printStackTrace();}
            }
            catch (NullPointerException ex2){
                // this sometimes happens when the lens is unset after entering this branch but before doing the actual transform
                if (VirtualSpaceManager.debugModeON()){ex2.printStackTrace();}
            }
        }
    }

    private void drawOverlayCam(){
        Camera overlayCamera = parent.overlayCamera;
        if ((overlayCamera!=null) && (overlayCamera.enabled) && ((overlayCamera.eager) || (overlayCamera.shouldRepaint()))){
            stableRefToOverlayBufferGraphics.setComposite(
                AlphaComposite.getInstance(AlphaComposite.CLEAR));
            stableRefToOverlayBufferGraphics.fillRect(0, 0, size.width,size.height);
            stableRefToOverlayBufferGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            camIndex=overlayCamera.getIndex();
            drawnGlyphs=overlayCamera.parentSpace.getDrawnGlyphs(camIndex);
            drawnGlyphs.removeAllElements();
            double uncoef = (overlayCamera.focal+overlayCamera.altitude) / overlayCamera.focal;
            //compute region seen from this view through camera
            double viewW = size.width;
            double viewH = size.height;
            double viewWC = overlayCamera.vx - (viewW/2) * uncoef;
            double viewNC = overlayCamera.vy + (viewH/2) * uncoef;
            double viewEC = overlayCamera.vx + (viewW/2) * uncoef;
            double viewSC = overlayCamera.vy - (viewH/2) * uncoef;
            gll = overlayCamera.parentSpace.getDrawingList();
            for (int i=0;i<gll.length;i++){
                if (gll[i] != null){
                    if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, overlayCamera)){
                        /* if glyph is at least partially visible in the reg. seen from this view,
                           compute in which buffer it should be rendered: */
                        /* always draw in the main buffer */
                            gll[i].project(overlayCamera, size);
                            if (gll[i].isVisible()){
                                gll[i].draw(stableRefToOverlayBufferGraphics, size.width, size.height, overlayCamera.getIndex(),
                                        standardStroke, standardTransform, 0, 0);
                            }
                        /* notifying outside of above test because glyph sensitivity is not
                           affected by glyph visibility when managed through Glyph.setVisible() */
                        overlayCamera.parentSpace.drewGlyph(gll[i], camIndex);
                    }
                }
            }
        }
    }

    private void drawCursor(boolean erase){
        Graphics2D stableRef = stableRefToBackBufferGraphics;
        
        stableRef.setColor(parent.mouse.hcolor);
        if (drawDrag){stableRef.drawLine(origDragx,origDragy,parent.mouse.jpx,parent.mouse.jpy);}
        if (drawRect){
            stableRef.drawRect(Math.min(origDragx,parent.mouse.jpx),
                    Math.min(origDragy,parent.mouse.jpy),
                    Math.max(origDragx,parent.mouse.jpx)-Math.min(origDragx,parent.mouse.jpx),
                    Math.max(origDragy,parent.mouse.jpy)-Math.min(origDragy,parent.mouse.jpy));}
        if (drawOval){
            if (circleOnly){
                stableRef.drawOval(origDragx-Math.abs(origDragx-parent.mouse.jpx),
                        origDragy-Math.abs(origDragx-parent.mouse.jpx),
                        2*Math.abs(origDragx-parent.mouse.jpx),
                        2*Math.abs(origDragx-parent.mouse.jpx));
            }
            else {
                stableRef.drawOval(origDragx-Math.abs(origDragx-parent.mouse.jpx),
                        origDragy-Math.abs(origDragy-parent.mouse.jpy),
                        2*Math.abs(origDragx-parent.mouse.jpx),
                        2*Math.abs(origDragy-parent.mouse.jpy));
            }
        }
        if (sfopw){
            stableRef.drawImage(FIRST_ORDER_PAN_WIDGET, fopw_x, fopw_y, null);
        }
        if (drawVTMcursor){
            stableRef.setXORMode(backColor);
            stableRef.setColor(parent.mouse.color);
            if (erase){
                stableRef.drawLine(oldX-parent.mouse.size,oldY,oldX+parent.mouse.size,oldY);
                stableRef.drawLine(oldX,oldY-parent.mouse.size,oldX,oldY+parent.mouse.size);
            }
            stableRef.drawLine(parent.mouse.jpx-parent.mouse.size,parent.mouse.jpy,parent.mouse.jpx+parent.mouse.size,parent.mouse.jpy);
            stableRef.drawLine(parent.mouse.jpx,parent.mouse.jpy-parent.mouse.size,parent.mouse.jpx,parent.mouse.jpy+parent.mouse.size);
            oldX = parent.mouse.jpx;
            oldY = parent.mouse.jpy;
        }

    }
    
    void eraseCursor(){
        Graphics2D stableRef =  stableRefToBackBufferGraphics;
        
        try {
            if (drawVTMcursor){
                stableRef.setXORMode(backColor);
                stableRef.setColor(parent.mouse.color);
                stableRef.drawLine(parent.mouse.jpx-parent.mouse.size,parent.mouse.jpy,parent.mouse.jpx+parent.mouse.size,parent.mouse.jpy);
                stableRef.drawLine(parent.mouse.jpx,parent.mouse.jpy-parent.mouse.size,parent.mouse.jpx,parent.mouse.jpy+parent.mouse.size);
                panel.paintImmediately(0,0,size.width,size.height);
            }
        }
        catch (NullPointerException ex47){if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.runview.drawVTMcursor "+ex47);}}
    }

    private void doCursorPicking(){
        try {
            //we project the mouse cursor wrt the appropriate coord sys
            parent.mouse.unProject(cams[activeLayer],this);
            if (parent.mouse.isSensitive()){
                parent.mouse.getPicker().computePickedGlyphList(cams[activeLayer], this);}
        }
        catch (NullPointerException ex) {if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.drawdrag "+ex);}}
    }

    //draw ONCE (no more infinite thread loop; will be driven
    //from an EDT timer)
    public void drawOffscreen() {
        oldSize = panel.getSize();
        if (notBlank){
            if (repaintable && !paintLocked){
                if (repaintASAP){
                    try {
                        //long ct = System.currentTimeMillis();
                        repaintASAP=false; //do this first as the thread can be interrupted inside
                        //this branch and we want to catch new requests for repaint
                        boolean rb = repaintBack;
                        repaintBack = false;
                        boolean rps = repaintPortals;
                        repaintPortals = false;
                        boolean ro = repaintOverlay;
                        repaintOverlay = false;
                        boolean uco = updateCursorOnly;
                        updateCursorOnly=false;
                        boolean printTime= false;
                        long ct=0,ctt=0,ctt2=0;
                        if (printTime){
                            ct = System.nanoTime();
                            ctt=ct; ctt2=ct;
                        }
                        if(rb){
                            updateOffscreenBuffer();
                            stableRefToBackBufferGraphics.setPaintMode();
                            stableRefToBackBufferGraphics.setBackground(backColor);
                            stableRefToBackBufferGraphics.clearRect(0, 0, size.width, size.height);
                            if (printTime){
                                ctt2 = System.nanoTime();
                                System.out.println("Drawin Delay CLR "+ (ctt2-ctt)/1000000);
                                ctt = ctt2;
                            }
                            backgroundHook();
                            //begin actual drawing here
                            if(lens != null) {
                                drawScene(true);
                            } else {
                                drawScene(false);
                            }
                            if (printTime){
                                ctt2 = System.nanoTime();
                                System.out.println("Drawin Delay DSC "+ (ctt2-ctt)/1000000);
                                ctt = ctt2;
                            }
                            afterLensHook();
                        }
                        if (rps){
                            drawPortals();
                            portalsHook();
                            if (printTime){
                                ctt2 = System.nanoTime();
                                System.out.println("Drawin Delay DPT "+ (ctt2-ctt)/1000000);
                                ctt = ctt2;
                            }
                        }
                        if (ro){
                            drawOverlayCam();
                            if (printTime){
                                ctt2 = System.nanoTime();
                                System.out.println("Drawin Delay DOV "+ (ctt2-ctt)/1000000);
                                ctt = ctt2;
                            }
                        }
                        if (cursor_inside && (rb || uco)){
                            //deal with mouse glyph only if mouse cursor is inside this window
                            if (uco) doCursorPicking();
                            //System.out.println("Drawing Cursor "+uco);
                            drawCursor(!rb);
                        }
                        if (printTime){
                            ctt2 = System.nanoTime();
                            System.out.println("Drawin Delay "+ (ctt2-ct)/1000000);
                        }
                        //end drawing here
                        if (stableRefToBackBufferGraphics == backBufferGraphics) {
                            if (syncPaintImmediately != null){
                               syncPaintImmediately.paint();
                            }
                            else{
                                panel.paintImmediately(0,0,size.width,size.height);
                            }
                            if (repaintListener != null){repaintListener.viewRepainted(StdViewPanel.this.parent);}
                            synchronized(this){
                                lastButOneRepaint = lastRepaint;
                                lastRepaint = System.currentTimeMillis();
                                delay = lastRepaint - lastButOneRepaint;
                            }
                        }
                    }
                    catch (NullPointerException ex0){
                        if (VirtualSpaceManager.debugModeON()){
                            System.err.println("viewpanel.run (probably due to backBuffer.createGraphics()) "+ex0);
                            ex0.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            // blank screen 
            updateOffscreenBuffer();
            stableRefToBackBufferGraphics.setPaintMode();
            stableRefToBackBufferGraphics.setColor(blankColor);
            stableRefToBackBufferGraphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
            // portalsHook();
            // FIXME multi buffer rendering should blanks portals in buffer mode...
            if (stableRefToOverlayBufferGraphics != null){
                stableRefToOverlayBufferGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                stableRefToOverlayBufferGraphics.fillRect(0, 0, size.width,size.height);
            }
            panel.paintImmediately(0,0,size.width,size.height);
            if (repaintListener != null){repaintListener.viewRepainted(StdViewPanel.this.parent);}
        }
    }

    @Override
    public BufferedImage getImage(){
        return this.backBuffer;
    }

    @Override
    public void setRefreshRate(int rr){
        edtTimer.setDelay(rr);
    }

    @Override
    public int getRefreshRate(){
        return edtTimer.getDelay();
    }

}
