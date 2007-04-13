/*   FILE: AccViewPanel.java
 *   DATE OF CREATION:   Jun 08 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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

package com.xerox.VTM.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Vector;

import net.claribole.zvtm.engine.Java2DPainter;

import com.xerox.VTM.glyphs.Glyph;

/**
 * Each view runs in its own thread - uses double buffering - Hhardware accelerated using the VolatileImage API.
 * @author Emmanuel Pietriga
 */
public class AccViewPanel extends ViewPanel implements Runnable {

    /** Double Buffering uses a VolatileImage as the back buffer. */
    VolatileImage backBuffer;

    /** Used for VolatileImage validation. */
    int valCode;

    public AccViewPanel(Vector cameras,View v) {
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
	this.addMouseWheelListener(this);
	this.setDoubleBuffered(false);
	start();
	setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
	if (parent.parent.debug){System.out.println("View refresh time set to "+frameTime+"ms");}
    }

    public void start() {
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
		runView.sleep(inactiveSleepTime);
	    } 
	    catch (InterruptedException e) {
		if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep "+e);}
		return;
	    }
        }
	backBufferGraphics = null;
	Dimension oldSize=getSize();
	while (runView == me) {
	    loopStartTime=System.currentTimeMillis();
	    if (notBlank){
		if (active){
		    if (repaintNow) {
			repaintNow=false;//do this first as the thread can be interrupted inside this branch and we want to catch new requests for repaint
			updateMouseOnly=false;
			size = this.getSize();
			viewW = size.width;//compute region's width and height
			viewH = size.height;
			if (size.width != oldSize.width || size.height != oldSize.height) {
			    //each time the parent window is resized, adapt the VolatileImage that serves as a back buffer
			    if (backBuffer != null){
				backBuffer.flush();
				backBuffer = null;
			    }
			    if (backBufferGraphics != null){
				backBufferGraphics.dispose();
				backBufferGraphics = null;
			    }
			    //clipRect=new Rectangle(0,0,size.width,size.height);
			    if (parent.parent.debug){System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");}
			    oldSize=size;
			    updateAntialias=true;
			    updateFont=true;
			}
			if (backBuffer == null) {
			    gconf = getGraphicsConfiguration();
			    backBuffer = gconf.createCompatibleVolatileImage(size.width,size.height);
			    if (backBufferGraphics != null){
				backBufferGraphics.dispose();
				backBufferGraphics = null;
			    }
			}
			if (backBufferGraphics == null) {
			    backBufferGraphics = backBuffer.createGraphics();
			    updateAntialias = true;
			    updateFont = true;
			}
			if (updateFont){
			    backBufferGraphics.setFont(VirtualSpaceManager.mainFont);
			    updateFont = false;
			}
			if (updateAntialias){
			    if (antialias){
				backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			    }
			    else {
				backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			    }
			    updateAntialias=false;
			}
			stableRefToBackBufferGraphics = backBufferGraphics;
			gconf = this.getGraphicsConfiguration();
			valCode = backBuffer.validate(gconf);
			if (valCode == VolatileImage.IMAGE_INCOMPATIBLE){
			    backBuffer = gconf.createCompatibleVolatileImage(size.width, size.height);
			}
			// case of IMAGE_RESTORED is not handled since we are
			// only drawing to the image not copying from it
			standardStroke = stableRefToBackBufferGraphics.getStroke();
			standardTransform = stableRefToBackBufferGraphics.getTransform();
			synchronized(this){
			    do {
				stableRefToBackBufferGraphics.setPaintMode();
				stableRefToBackBufferGraphics.setBackground(backColor);
				stableRefToBackBufferGraphics.clearRect(0, 0, getWidth(), getHeight());
				// call to background java2d painting hook
				if (parent.painters[Java2DPainter.BACKGROUND] != null){
				    parent.painters[Java2DPainter.BACKGROUND].paint(stableRefToBackBufferGraphics, size.width, size.height);
				}
				//begin actual drawing here
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
					    if (parent.detectMultipleFullFills){//if detect multiple fills option is ON
						for (int i=0;i<gll.length;i++){
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
							gl.draw(stableRefToBackBufferGraphics,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
						    }
						    cams[nbcam].parentSpace.drewGlyph(gl, camIndex);
						}
						// EOXXX
					    }
					    else {//if detect multiple fills option is OFF
						for (int i=0;i<gll.length;i++){
						    if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
							//if glyph is al least partially visible in the reg. seen from this view, display
							synchronized(gll[i]){
							    gll[i].project(cams[nbcam], size);
							    if (gll[i].isVisible()){
								gll[i].draw(stableRefToBackBufferGraphics,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
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
				    parent.painters[Java2DPainter.FOREGROUND].paint(stableRefToBackBufferGraphics, size.width, size.height);
				}
				// call to after-distortion java2d painting hook
				if (parent.painters[Java2DPainter.AFTER_DISTORTION] != null){
				    parent.painters[Java2DPainter.AFTER_DISTORTION].paint(stableRefToBackBufferGraphics, size.width, size.height);
				}
				// paint portals associated with this view
				for (int i=0;i<parent.portals.length;i++){
				    parent.portals[i].paint(stableRefToBackBufferGraphics, size.width, size.height);
				}
				// call to after-portals java2d painting hook
				if (parent.painters[Java2DPainter.AFTER_PORTALS] != null){
				    parent.painters[Java2DPainter.AFTER_PORTALS].paint(stableRefToBackBufferGraphics, size.width, size.height);
				}
				if (inside){//deal with mouse glyph only if mouse cursor is inside this window
				    try {
					parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
					if (computeListAtEachRepaint && parent.mouse.isSensitive()){
					    parent.mouse.computeMouseOverList(evH,cams[activeLayer]);
					}
				    }
				    catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.run.drawdrag "+ex);}}
				    stableRefToBackBufferGraphics.setColor(parent.mouse.hcolor);
				    if (drawDrag){stableRefToBackBufferGraphics.drawLine(origDragx,origDragy,parent.mouse.mx,parent.mouse.my);}
				    if (drawRect){stableRefToBackBufferGraphics.drawRect(Math.min(origDragx,parent.mouse.mx),Math.min(origDragy,parent.mouse.my),Math.max(origDragx,parent.mouse.mx)-Math.min(origDragx,parent.mouse.mx),Math.max(origDragy,parent.mouse.my)-Math.min(origDragy,parent.mouse.my));}
				    if (drawOval){
					if (circleOnly){
					    stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx));
					}
					else {
					    stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragy-parent.mouse.my),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragy-parent.mouse.my));
					}
				    }
				    if (drawVTMcursor){
					synchronized(this){
					    stableRefToBackBufferGraphics.setXORMode(backColor);
					    parent.mouse.draw(stableRefToBackBufferGraphics);
					    oldX=parent.mouse.mx;
					    oldY=parent.mouse.my;
					}
				    }
				}
			    }
			    while (backBuffer.contentsLost());
			    //end drawing here
			    if (stableRefToBackBufferGraphics == backBufferGraphics) {
				repaint();
			    }
			    loopTotalTime = System.currentTimeMillis() - loopStartTime;
			    // time to sleep = wanted refresh rate minus time needed to do the actual repaint operations
			    timeToSleep = frameTime - loopTotalTime;
			}
			try {
			    runView.sleep((timeToSleep > minimumSleepTime) ? timeToSleep : minimumSleepTime);
			} 
			catch (InterruptedException e) {
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep2 "+e);}
			    return;
			}
		    }
		    else if (updateMouseOnly){
			updateMouseOnly=false;
			try {
			    parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
			    if (computeListAtEachRepaint && parent.mouse.isSensitive()){parent.mouse.computeMouseOverList(evH,cams[activeLayer]);}
			}
			catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.run.drawdrag "+ex);}}
			if (drawVTMcursor){
			    synchronized(this){
				try {
				    stableRefToBackBufferGraphics.setXORMode(backColor);
				    stableRefToBackBufferGraphics.setColor(parent.mouse.color);
				    stableRefToBackBufferGraphics.drawLine(oldX-parent.mouse.size,oldY,oldX+parent.mouse.size,oldY);
				    stableRefToBackBufferGraphics.drawLine(oldX,oldY-parent.mouse.size,oldX,oldY+parent.mouse.size);
				    stableRefToBackBufferGraphics.drawLine(parent.mouse.mx-parent.mouse.size,parent.mouse.my,parent.mouse.mx+parent.mouse.size,parent.mouse.my);
				    stableRefToBackBufferGraphics.drawLine(parent.mouse.mx,parent.mouse.my-parent.mouse.size,parent.mouse.mx,parent.mouse.my+parent.mouse.size);
				    oldX=parent.mouse.mx;
				    oldY=parent.mouse.my;
				}// a nullpointerex on stableRefToBackBufferGraphics seems to occur from time to time when going in or exiting from blank mode
				//  just catch it and wait for next loop until we find out what's causing this
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
			catch (InterruptedException e){
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep3 "+e);}
			    return;
			}
		    }
		    else {
			try {
			    runView.sleep(frameTime + noRepaintAdditionalTime);   //sleep ... ms  
			} 
			catch (InterruptedException e) {
			    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep3 "+e);}
			    return;
			}
		    }
		}
		else {
		    try {
			runView.sleep(inactiveSleepTime);   //sleep ... ms  
		    } 
		    catch (InterruptedException e) {
			if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep4 "+e);}
			return;
		    }
		}
	    }
	    else {
		size = this.getSize();
		viewW = size.width;//compute region's width and height
		viewH = size.height;
		if (size.width != oldSize.width || size.height != oldSize.height) {
		    //each time the parent window is resized, adapt the VolatileImage that serves as a back buffer
		    if (backBuffer != null){
			backBuffer.flush();
			backBuffer = null;
		    }
		    if (backBufferGraphics != null){
			backBufferGraphics.dispose();
			backBufferGraphics = null;
		    }
		    //clipRect=new Rectangle(0,0,size.width,size.height);
		    if (parent.parent.debug){System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");}
		    oldSize=size;
		    updateAntialias=true;
		    updateFont=true;
		}
		if (backBuffer == null) {
		    gconf = getGraphicsConfiguration();
		    backBuffer = gconf.createCompatibleVolatileImage(size.width,size.height);
		    if (backBufferGraphics != null){
			backBufferGraphics.dispose();
			backBufferGraphics = null;
		    }
		}
		if (backBufferGraphics == null) {
		    backBufferGraphics = backBuffer.createGraphics();
		    updateAntialias = true;
		    updateFont = true;
		}
		if (updateFont){
		    backBufferGraphics.setFont(VirtualSpaceManager.mainFont);
		    updateFont = false;
		}
		if (updateAntialias){
		    if (antialias){
			backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		    }
		    else {
			backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		    }
		    updateAntialias=false;
		}
		stableRefToBackBufferGraphics = backBufferGraphics;
		gconf = this.getGraphicsConfiguration();
		valCode = backBuffer.validate(gconf);
		if (valCode == VolatileImage.IMAGE_INCOMPATIBLE){
		    backBuffer = gconf.createCompatibleVolatileImage(size.width,size.height);
		}
		// case of IMAGE_RESTORED is not handled since we are
		// only drawing to the image not copying from it
		standardStroke=stableRefToBackBufferGraphics.getStroke();
		standardTransform=stableRefToBackBufferGraphics.getTransform();
		do {
		    stableRefToBackBufferGraphics.setPaintMode();
		    stableRefToBackBufferGraphics.setColor(blankColor);
		    stableRefToBackBufferGraphics.fillRect(0, 0, size.width, size.height);
		}
		while (backBuffer.contentsLost());
		repaint();
		try {
		    runView.sleep(blankSleepTime);   //sleep ... ms
		}
		catch (InterruptedException e){
		    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep5 "+e);}
		    return;
		}
	    }
	}
	if (stableRefToBackBufferGraphics != null) {
	    stableRefToBackBufferGraphics.dispose();
	}
    }
    
    public void paint(Graphics g) {
	synchronized (this) {
	    if (backBuffer != null) {
		g.drawImage(backBuffer, 0, 0, this);
		if (repaintListener != null){repaintListener.viewRepainted(this.parent);}
	    }
        }
    }

    /** Get a snapshot of this VolatileImage-based accelerated view.*/
    public BufferedImage getImage(){
	return this.backBuffer.getSnapshot();
    }

}
