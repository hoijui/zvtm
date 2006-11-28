/*   FILE: AppletViewPanel.java
 *   DATE OF CREATION:   Jul 04 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
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
 * $Id: AppletViewPanel.java,v 1.15 2006/06/01 07:11:56 epietrig Exp $
 */

package com.xerox.VTM.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.lens.Lens;

/**
 * Each view runs in its own thread - uses double buffering - for use in JApplet only
 * @author Emmanuel Pietriga
 */
public class AppletViewPanel extends ViewPanel implements Runnable {

    /*coordinates of lens center in virtual space for each camera*/
    long lensVx, lensVy;
    long lviewWC, lviewNC, lviewEC, lviewSC;

    /**for Double Buffering*/
    BufferedImage buffImg;

    //get the BufferedImage or VolatileImage for this view
    public BufferedImage getImage(){
	return this.buffImg;
    }

    public AppletViewPanel(Vector cameras,View v) {
	addHierarchyListener(
	    new HierarchyListener() {
	       public void hierarchyChanged(HierarchyEvent e) {
		   if (isShowing()) {
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
	for (int nbcam=0;nbcam<cameras.size();nbcam++){
	    cams[nbcam]=(Camera)(cameras.get(nbcam));
	}
	//init other stuff
	setBackground(Color.lightGray);
	this.addMouseListener(this);
	this.addMouseMotionListener(this);
	this.setDoubleBuffered(false);
	start();
	setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
	if (parent.parent.debug){System.out.println("View refresh time set to "+frameTime+"ms");}
    }

    public void start(){
	size = getSize();
	runView = new Thread(this);
	runView.setPriority(Thread.NORM_PRIORITY);
	runView.start();
    }

    public synchronized void stop() {
	runView = null;
	notify();
    }

    public void run() {
	Thread me = Thread.currentThread();
	while (getSize().width <= 0) {  //Wait until the window actually exists
	    try {
		runView.sleep(deactiveTime);
	    } 
	    catch (InterruptedException e) {
		if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep "+e);}
		return;
	    }
        }
	Graphics2D g2d = null;
	Graphics2D BufferG2D = null;
	Graphics2D lensG2D = null;
	Dimension oldSize=getSize();
	//clipRect=new Rectangle(0,0,oldSize.width,oldSize.height);
	while (runView==me) {
	    loopStartTime=System.currentTimeMillis();
 	    if (notBlank){
		if (active){
		    if (repaintNow){
			try {
			    repaintNow=false;//do this first as the thread can be interrupted inside this branch and we want to catch new requests for repaint
			    updateMouseOnly=false;
			    size = this.getSize();
			    viewW = size.width;//compute region's width and height
			    viewH = size.height;
			    if (size.width != oldSize.width || size.height != oldSize.height) {
				//each time the parent window is resized, adapt the buffer image size
				buffImg=null;
				if (BufferG2D!=null) {
				    BufferG2D.dispose();
				    BufferG2D=null;
				}
				if (lens != null){
				    lens.resetMagnificationBuffer();
				    if (lensG2D != null) {
					lensG2D.dispose();
					lensG2D = null;
				    }
				}
				//clipRect=new Rectangle(0,0,size.width,size.height);
				if (parent.parent.debug){
				    System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");
				}
				oldSize=size;
				updateAntialias=true;
				updateFont=true;
			    }
			    if (buffImg==null){
				buffImg=(BufferedImage)createImage(size.width, size.height);
				updateAntialias=true;
				updateFont=true;
			    }
			    if (BufferG2D == null) {
				BufferG2D = buffImg.createGraphics();
				updateAntialias=true;
				updateFont=true;
			    }
			    if (lens != null){
				lensG2D = lens.getMagnificationGraphics();
				lensG2D.setFont(VirtualSpaceManager.mainFont);
				if (antialias){
				    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
				else {
				    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				}
			    }
			    if (updateFont){
				BufferG2D.setFont(VirtualSpaceManager.mainFont);
				if (lensG2D != null){
				    lensG2D.setFont(VirtualSpaceManager.mainFont);
				}
				updateFont=false;
			    }
			    if (updateAntialias){
				if (antialias){
				    BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				    if (lensG2D != null){
					lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				    }
				}
				else {
				    BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				    if (lensG2D != null){
					lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				    }
				}
				updateAntialias = false;
			    }
			    g2d = BufferG2D;
			    standardStroke=g2d.getStroke();
			    standardTransform=g2d.getTransform();
			    synchronized(this){
				g2d.setPaintMode();
				g2d.setBackground(backColor);
				g2d.clearRect(0,0,getWidth(),getHeight());
				// call to background java2d painting hook
				if (parent.painters[Java2DPainter.BACKGROUND] != null){
				    parent.painters[Java2DPainter.BACKGROUND].paint(g2d, size.width, size.height);
				}
				//begin actual drawing here
				if (lens != null){// drawing with a lens
 				    synchronized(lens){// prevents flickering when the lens parameters are being animated (caused by concurent access)
					lensG2D.setPaintMode(); // to the lens from LAnimation.animate() methods and this thread
					lensG2D.setBackground(backColor);
					lensG2D.clearRect(0, 0, lens.mbw, lens.mbh);
					for (int nbcam=0;nbcam<cams.length;nbcam++){
					    if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
						camIndex=cams[nbcam].getIndex();
						drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
						synchronized(drawnGlyphs){
						    drawnGlyphs.removeAllElements();
						    uncoef=(float)((cams[nbcam].focal+cams[nbcam].altitude)/cams[nbcam].focal);
						    //compute region seen from this view through camera
						    viewWC = (long)(cams[nbcam].posx-(viewW/2-visibilityPadding[0])*uncoef);
						    viewNC = (long)(cams[nbcam].posy+(viewH/2-visibilityPadding[1])*uncoef);
						    viewEC = (long)(cams[nbcam].posx+(viewW/2-visibilityPadding[2])*uncoef);
						    viewSC = (long)(cams[nbcam].posy-(viewH/2-visibilityPadding[3])*uncoef);
						    lviewWC = (long)(cams[nbcam].posx + (lens.lx-lens.lensWidth/2)*uncoef);
						    lviewNC = (long)(cams[nbcam].posy + (-lens.ly+lens.lensHeight/2)*uncoef);
						    lviewEC = (long)(cams[nbcam].posx + (lens.lx+lens.lensWidth/2)*uncoef);
						    lviewSC = (long)(cams[nbcam].posy + (-lens.ly-lens.lensHeight/2)*uncoef);
						    lensVx = (lviewWC+lviewEC)/2;
						    lensVy = (lviewSC+lviewNC)/2;
						    gll = cams[nbcam].parentSpace.getVisibleGlyphList();
						    if (parent.detectMultipleFullFills){// if detect multiple fills option is ON
							for (int i=0;i<gll.length;i++){ // (usually not the case)
							    if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
								cams[nbcam].parentSpace.drewGlyph(gll[i],camIndex);
								gll[i].project(cams[nbcam], size);
							    }
							}
							// XXX: looks like this part of the code has not been updated for some time
							//drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
							beginAt=0;
							for (int j=drawnGlyphs.size()-1;j>=0;j--){//glyphs must have been projected because fillsView uses
							    if (((Glyph)drawnGlyphs.elementAt(j)).fillsView(viewW,viewH,cams[nbcam].getIndex())){//projected coords
								beginAt=j;
								break;
							    }
							}
							for (int j=beginAt;j<drawnGlyphs.size();j++){
							    gl=(Glyph)drawnGlyphs.elementAt(j);
							    if (gl.isVisible()){
								gl.draw(g2d,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
							    }
							    cams[nbcam].parentSpace.drewGlyph(gl, camIndex);
							}
							// EOXXX
						    }
						    else {//if detect multiple fills option is OFF
							for (int i=0;i<gll.length;i++){
							    synchronized(gll[i]){
								if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
								    /* if glyph is at least partially visible in the reg. seen from this view,
								       compute in which buffer it should be rendered: */
								    /* always draw in the main buffer */
								    gll[i].project(cams[nbcam], size);
								    if (gll[i].isVisible()){
									gll[i].draw(g2d, size.width, size.height, cams[nbcam].getIndex(),
										    standardStroke, standardTransform, 0, 0);
								    }
								    if (gll[i].visibleInRegion(lviewWC, lviewNC, lviewEC, lviewSC, camIndex)){
									/* partially within the region seen through the lens
									   draw it in both buffers */
									gll[i].projectForLens(cams[nbcam], lens.mbw, lens.mbh, lens.getMaximumMagnification(), lensVx, lensVy);
									if (gll[i].isVisible()){
									    gll[i].drawForLens(lensG2D, lens.mbw, lens.mbh, cams[nbcam].getIndex(),
											       standardStroke, standardTransform, 0 , 0);
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
					    }
					}
					// call to foreground java2d painting hook
					if (parent.painters[Java2DPainter.FOREGROUND] != null){
					    parent.painters[Java2DPainter.FOREGROUND].paint(g2d, size.width, size.height);
					}
					try {
					    lens.transform(buffImg);
					    lens.drawBoundary(g2d);
					}
					catch (ArrayIndexOutOfBoundsException ex){
					    if (VirtualSpaceManager.debugModeON()){ex.printStackTrace();}
					}
					catch (NullPointerException ex2){
					    // this sometimes happens when the lens is unset after entering this branch but before doing the actual transform
					    if (VirtualSpaceManager.debugModeON()){ex2.printStackTrace();}
					}
					if (parent.painters[Java2DPainter.AFTER_DISTORTION] != null){
					    parent.painters[Java2DPainter.AFTER_DISTORTION].paint(g2d, size.width, size.height);
					}
					// paint portals associated with this view
					for (int i=0;i<parent.portals.length;i++){
					    parent.portals[i].paint(g2d, size.width, size.height);
					}
					// call to after-portals java2d painting hook
					if (parent.painters[Java2DPainter.AFTER_PORTALS] != null){
					    parent.painters[Java2DPainter.AFTER_PORTALS].paint(g2d, size.width, size.height);
					}
				    }
				}
				else {// standard drawing, with no lens
				    for (int nbcam=0;nbcam<cams.length;nbcam++){
					if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
					    camIndex=cams[nbcam].getIndex();
					    drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
					    synchronized(drawnGlyphs){
						drawnGlyphs.removeAllElements();
						uncoef=(float)((cams[nbcam].focal+cams[nbcam].altitude)/cams[nbcam].focal);
						//compute region seen from this view through camera
						viewWC = (long)(cams[nbcam].posx-(viewW/2-visibilityPadding[0])*uncoef);
						viewNC = (long)(cams[nbcam].posy+(viewH/2-visibilityPadding[1])*uncoef);
						viewEC = (long)(cams[nbcam].posx+(viewW/2-visibilityPadding[2])*uncoef);
						viewSC = (long)(cams[nbcam].posy-(viewH/2-visibilityPadding[3])*uncoef);
						gll = cams[nbcam].parentSpace.getVisibleGlyphList();
						if (parent.detectMultipleFullFills){// if detect multiple fills option is ON
						    for (int i=0;i<gll.length;i++){ // (usually not the case)
							if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
							    cams[nbcam].parentSpace.drewGlyph(gll[i],camIndex);
							    gll[i].project(cams[nbcam], size);
							}
						    }
						    // XXX: looks like this part of the code has not been updated for some time
						    //drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
						    beginAt=0;
						    for (int j=drawnGlyphs.size()-1;j>=0;j--){//glyphs must have been projected because fillsView uses
							if (((Glyph)drawnGlyphs.elementAt(j)).fillsView(viewW,viewH,cams[nbcam].getIndex())){//projected coords
							    beginAt=j;
							    break;
							}
						    }
						    for (int j=beginAt;j<drawnGlyphs.size();j++){
							gl=(Glyph)drawnGlyphs.elementAt(j);
							if (gl.isVisible()){
							    gl.draw(g2d,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
							}
							cams[nbcam].parentSpace.drewGlyph(gl, camIndex);
						    }
						    // EOXXX
						}
						else {//if detect multiple fills option is OFF
						    for (int i=0;i<gll.length;i++){
							if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
							    //if glyph is at least partially visible in the reg. seen from this view, display
							    synchronized(gll[i]){
								gll[i].project(cams[nbcam], size); // an invisible glyph should still be projected
								if (gll[i].isVisible()){          // as it can be sensitive
								    gll[i].draw(g2d,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
								}
								// notifying outside if branch because glyph sensitivity is not
								// affected by glyph visibility when managed through Glyph.setVisible()
								cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
							    }
							}
						    }
						}
					    }
					}
				    }
				    // call to foreground java2d painting hook
				    if (parent.painters[Java2DPainter.FOREGROUND] != null){
					parent.painters[Java2DPainter.FOREGROUND].paint(g2d, size.width, size.height);
				    }
				    // call to after-distortion java2d painting hook
				    if (parent.painters[Java2DPainter.AFTER_DISTORTION] != null){
					parent.painters[Java2DPainter.AFTER_DISTORTION].paint(g2d, size.width, size.height);
				    }
				    // paint portals associated with this view
				    for (int i=0;i<parent.portals.length;i++){
					parent.portals[i].paint(g2d, size.width, size.height);
				    }
				    // call to after-portals java2d painting hook
				    if (parent.painters[Java2DPainter.AFTER_PORTALS] != null){
					parent.painters[Java2DPainter.AFTER_PORTALS].paint(g2d, size.width, size.height);
				    }
				}
				if (inside){//deal with mouse glyph only if mouse cursor is inside this window
				    try {
					parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
					if (computeListAtEachRepaint && parent.mouse.isSensitive()){
					    parent.mouse.computeMouseOverList(evH,cams[activeLayer],this.lens);
					}
				    }
				    catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.run.drawdrag "+ex);}}
				    g2d.setColor(parent.mouse.hcolor);
				    if (drawDrag){g2d.drawLine(origDragx,origDragy,parent.mouse.mx,parent.mouse.my);}
				    if (drawRect){g2d.drawRect(Math.min(origDragx,parent.mouse.mx),Math.min(origDragy,parent.mouse.my),Math.max(origDragx,parent.mouse.mx)-Math.min(origDragx,parent.mouse.mx),Math.max(origDragy,parent.mouse.my)-Math.min(origDragy,parent.mouse.my));}
				    if (drawOval){
					if (circleOnly){
					    g2d.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx));
					}
					else {
					    g2d.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragy-parent.mouse.my),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragy-parent.mouse.my));
					}
				    }
				    if (drawVTMcursor){
					synchronized(this){
					    g2d.setXORMode(backColor);
					    parent.mouse.draw(g2d);
					    oldX=parent.mouse.mx;
					    oldY=parent.mouse.my;
					}
				    }
				}
				//end drawing here
				if (g2d == BufferG2D) {
				    repaint();
				}
				loopTotalTime = System.currentTimeMillis() - loopStartTime;
				// time to sleep = wanted refresh rate minus time needed to do the actual repaint operations
				timeToSleep = frameTime - loopTotalTime;
			    }
			}
			catch (NullPointerException ex0){
			    if (parent.parent.debug){
				System.err.println("viewpanel.run (probably due to buffImg.createGraphics()) "+ex0);
			    }
			}
			//either we do - BETTER UNDER Win32
			try {
			    runView.sleep((timeToSleep > minimumSleepTime) ? timeToSleep : minimumSleepTime);
			} 
			catch (InterruptedException e) {
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep2 "+e);}
			    return;
			}
			//or this   both seem to work well (have to test on several config) - BETTER UNDER SOLARIS
			//Thread.yield();
		    }
		    else if (updateMouseOnly){
			updateMouseOnly=false;//do this first as the thread can be interrupted inside this branch and we want to catch new requests for repaint
			try {
			    parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
			    if (computeListAtEachRepaint && parent.mouse.isSensitive()){parent.mouse.computeMouseOverList(evH,cams[activeLayer],this.lens);}
			}
			catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.run.drawdrag "+ex);}}
			if (drawVTMcursor){
			    synchronized(this){
				try {
				    g2d.setXORMode(backColor);
				    g2d.setColor(parent.mouse.color);
				    g2d.drawLine(oldX-10,oldY,oldX+10,oldY);
				    g2d.drawLine(oldX,oldY-10,oldX,oldY+10);
				    g2d.drawLine(parent.mouse.mx-10,parent.mouse.my,parent.mouse.mx+10,parent.mouse.my);
				    g2d.drawLine(parent.mouse.mx,parent.mouse.my-10,parent.mouse.mx,parent.mouse.my+10);
				    oldX=parent.mouse.mx;
				    oldY=parent.mouse.my;
				}//a nullpointerex on g2d seems to occur from time to time when going in or exiting from blank mode - just catch it and wait for next loop until we find out what's causing this
				catch (NullPointerException ex47){if (parent.parent.debug){System.err.println("viewpanel.run.runview.drawVTMcursor "+ex47);}} 
			    }
			}
			repaint();
			loopTotalTime = System.currentTimeMillis() - loopStartTime;
			// time to sleep = wanted refresh rate minus time needed to do the actual repaint operations
			timeToSleep = frameTime - loopTotalTime;
			try {
			    runView.sleep((timeToSleep > minimumSleepTime) ? timeToSleep : minimumSleepTime);
			}
			catch (InterruptedException e) {
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep3 "+e);}
			    return;
			}
		    }
		    else {
			try {
			    runView.sleep(frameTime + noRepaintAdditionalTime);   //sleep ... ms  
			}
			catch (InterruptedException e) {
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep4 "+e);}
			    return;
			}
		    }
		}
		else {
		    try {
			runView.sleep(deactiveTime);   //sleep ... ms  
		    } 
		    catch (InterruptedException e) {
			if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep5 "+e);}
			return;
		    }
		}
	    }
	    else {
		size=getSize();
		if (size.width != oldSize.width || size.height != oldSize.height) {
		    //each time the parent window is resized, adapt the buffer image size
		    buffImg=null;
		    if (BufferG2D!=null) {
			BufferG2D.dispose();
			BufferG2D=null;
		    }
		    if (parent.parent.debug){System.out.println("Resizing JPanel in blank mode: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");}
		    oldSize=size;
		    updateAntialias=true;
		    updateFont=true;
		}
		if (buffImg==null) {
		    buffImg=(BufferedImage) createImage(size.width,size.height);
		    updateAntialias=true;
		    updateFont=true;
		}
		if (BufferG2D == null) {
		    BufferG2D = buffImg.createGraphics();
		    updateAntialias=true;
		    updateFont=true;
		}
		if (updateFont){
		    BufferG2D.setFont(VirtualSpaceManager.mainFont);
		    if (lensG2D != null){
			lensG2D.setFont(VirtualSpaceManager.mainFont);
		    }
		    updateFont=false;
		}
		if (updateAntialias){
		    if (antialias){
			BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			if (lensG2D != null){
			    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			}
		    }
		    else {
			BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			if (lensG2D != null){
			    lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		    }
		    updateAntialias=false;
		}
		g2d = BufferG2D;
		standardStroke=g2d.getStroke();
		standardTransform=g2d.getTransform();
		g2d.setPaintMode();
		g2d.setColor(blankColor);
		g2d.fillRect(0,0,getWidth(),getHeight());
		repaint();
		try {
		    runView.sleep(deactiveTime);   //sleep ... ms  
		} 
		catch (InterruptedException e) {
		    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep5 "+e);}
		    return;
		}
	    }
	}
	if (g2d != null) {
	    g2d.dispose();
	}
    }

    public void paint(Graphics g) {
	synchronized (this) {
	    g2 = (Graphics2D) g;
	    if (buffImg != null){
		g2.drawImage(buffImg, null, 0, 0);
	    }
        }
    }

}
