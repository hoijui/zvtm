/*   FILE: GLViewPanel.java
 *   DATE OF CREATION:   Tue Oct 12 09:10:47 2004
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: GLViewPanel.java,v 1.11 2006/06/01 07:11:56 epietrig Exp $
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

import net.claribole.zvtm.engine.Java2DPainter;

/**
 * Each view runs in its own thread - uses OpenGL acceletation provided by J2SE 5.0<br>
 * The use of GLViewPanel requires the following Java property: -Dsun.java2d.opengl=true
 * @author Emmanuel Pietriga
 */

public class GLViewPanel extends ViewPanel implements Runnable {
    
    //XXX:not yet implemented
    public BufferedImage getImage(){
	return null;
    }

    Dimension oldSize;
    Graphics2D g2d;

    public GLViewPanel(Vector cameras,View v) {
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
	oldSize=getSize();
	while (runView==me) {
	    if (active){
		if (repaintNow || updateMouseOnly){
		    repaint();
		    if (repaintListener != null){repaintListener.viewRepainted(this.parent);}
		    // time to sleep = wanted refresh rate minus time needed to do the actual repaint operations
		    timeToSleep = frameTime - loopTotalTime;
		    try {// sleep at least minimumSleepTime ms so that other
			//  threads get a chance to run (thread policy varies with each OS)
			runView.sleep((timeToSleep > minimumSleepTime) ? timeToSleep : minimumSleepTime);
		    }
		    catch (InterruptedException e) {
			if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep3 "+e);}
			return;
		    }
		}
		else {
		    try {
			runView.sleep(frameTime + noRepaintAdditionalTime);
		    }
		    catch (InterruptedException e) {
			if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep4 "+e);}
			return;
		    }
		}
	    }
	    else {
		try {
		    runView.sleep(deactiveTime);
		} 
		catch (InterruptedException e) {
		    if (parent.parent.debug){System.err.println("viewpanel.run.runview.sleep5 "+e);}
		    return;
		}
	    }
	}
    }

    public void paint(Graphics g) {
	loopStartTime = System.currentTimeMillis();
	super.paint(g);
	g2d = (Graphics2D)g;
	try {
	    repaintNow=false;//do this first as the thread can be interrupted inside this branch and we want to catch new requests for repaint
	    updateMouseOnly = false;
	    size = this.getSize();
	    viewW = size.width;//compute region's width and height
	    viewH = size.height;
	    if (size.width != oldSize.width || size.height != oldSize.height) {
		if (parent.parent.debug){System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");}
		oldSize=size;
		updateAntialias=true;
		updateFont=true;
	    }
	    if (updateFont){g2d.setFont(VirtualSpaceManager.mainFont);updateFont=false;}
	    if (updateAntialias){if (antialias){g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);} else {g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);}updateAntialias=false;}
	    standardStroke=g2d.getStroke();
	    standardTransform=g2d.getTransform();
	    //synchronized(this){
		g2d.setPaintMode();
		g2d.setBackground(backColor);
		g2d.clearRect(0,0,getWidth(),getHeight());
		// call to background java2d painting hook
		if (parent.painters[Java2DPainter.BACKGROUND] != null){
		    parent.painters[Java2DPainter.BACKGROUND].paint(g2d, size.width, size.height);
		}
		//begin actual drawing here
		for (int nbcam=0;nbcam<cams.length;nbcam++){
		    if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
			camIndex=cams[nbcam].getIndex();
			drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
			//synchronized(cams[nbcam].parentSpace.getDrawnGlyphs(camIndex)){
			synchronized(drawnGlyphs){
			    //cams[nbcam].parentSpace.getDrawnGlyphs(camIndex).removeAllElements();
			    drawnGlyphs.removeAllElements();
			    uncoef=(float)((cams[nbcam].focal+cams[nbcam].altitude)/cams[nbcam].focal);
			    //compute region seen from this view through camera
			    viewWC = (long)(cams[nbcam].posx-(viewW/2-visibilityPadding[0])*uncoef);
			    viewNC = (long)(cams[nbcam].posy+(viewH/2-visibilityPadding[1])*uncoef);
			    viewEC = (long)(cams[nbcam].posx+(viewW/2-visibilityPadding[2])*uncoef);
			    viewSC = (long)(cams[nbcam].posy-(viewH/2-visibilityPadding[3])*uncoef);
			    gll = cams[nbcam].parentSpace.getVisibleGlyphList();
			    for (int i=0;i<gll.length;i++){
				if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
				    //if glyph is at least partially visible in the reg. seen from this view, display
				    synchronized(gll[i]){
					gll[i].project(cams[nbcam], size);
					if (gll[i].isVisible()){
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
		if (inside){//deal with mouse glyph only if mouse cursor is inside this window
		    try {
			parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
			if (computeListAtEachRepaint && parent.mouse.isSensitive()){
			    parent.mouse.computeMouseOverList(evH,cams[activeLayer]);
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
			    //g2d.setXORMode(Color.white);
			    parent.mouse.draw(g2d);
			    oldX=parent.mouse.mx;
			    oldY=parent.mouse.my;
			}
		    }
		}
		//end drawing here
		//}
	}
	catch (NullPointerException ex0){if (parent.parent.debug){System.err.println("GLViewPanel.paint "+ex0);}}
	loopTotalTime = System.currentTimeMillis() - loopStartTime;
    }

}
