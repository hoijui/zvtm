/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.engine;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.View;

import com.xerox.VTM.glyphs.*;

public class ScrollLayer implements ComponentListener {

    Camera controlledCamera;
    View controlledView;
    Camera slC;
    VirtualSpace slVS;
    String slVSname;
    /* vertical scrollbar */
    Glyph vgutter;
    Glyph vslider;
    Glyph upBt;
    Glyph downBt;
    RectangularShape vgutterRS;
    RectangularShape vsliderRS;
    RectangularShape upBtRS;
    RectangularShape downBtRS;
    /* Horizontal scrollbar */
    Glyph hgutter;
    Glyph hslider;
    Glyph leftBt;
    Glyph rightBt;
    RectangularShape hgutterRS;
    RectangularShape hsliderRS;
    RectangularShape leftBtRS;
    RectangularShape rightBtRS;

    /** Bounds of region observed through controlled camera. */
    long[] observedRegionBounds = new long[4];
    /** Bounds of smallest region of virtual space containing all glyphs. */
    long[] populatedRegionBounds = new long[4];

    int panelWidth;
    int panelHeight;

    public ScrollLayer(VirtualSpaceManager vsm, Camera cc){
	controlledCamera = cc;
	slVSname = "scrollspace" + controlledCamera.getID();
	slVS = vsm.addVirtualSpace(slVSname);
	slC = vsm.addCamera(slVS);
	vgutter = new VRectangle(0,0,0,10,10,Color.RED);
	vslider = new VRectangle(0,0,0,10,10,Color.BLUE);
	upBt = new VRectangle(0,0,0,10,10,Color.GREEN);
	downBt = new VRectangle(0,0,0,10,10,Color.ORANGE);
	hgutter = new VRectangle(0,0,0,10,10,Color.RED);
	hslider = new VRectangle(0,0,0,10,10,Color.BLUE);
	leftBt = new VRectangle(0,0,0,10,10,Color.GREEN);
	rightBt = new VRectangle(0,0,0,10,10,Color.ORANGE);
	vgutterRS = (RectangularShape)vgutter;
	vsliderRS = (RectangularShape)vslider;
	upBtRS = (RectangularShape)upBt;
	downBtRS = (RectangularShape)downBt;
	hgutterRS = (RectangularShape)hgutter;
	hsliderRS = (RectangularShape)hslider;
	leftBtRS = (RectangularShape)leftBt;
	rightBtRS = (RectangularShape)rightBt;
	vsm.addGlyph(vgutter, slVS);
	vsm.addGlyph(vslider, slVS);
	vsm.addGlyph(upBt, slVS);
	vsm.addGlyph(downBt, slVS);
	vsm.addGlyph(hgutter, slVS);
	vsm.addGlyph(hslider, slVS);
	vsm.addGlyph(leftBt, slVS);
	vsm.addGlyph(rightBt, slVS);
    }

    public ScrollLayer(VirtualSpaceManager vsm, Camera cc, Glyph[] widgets){
	controlledCamera = cc;
	slVSname = "scrollspace" + controlledCamera.getID();
	slVS = vsm.addVirtualSpace(slVSname);
	slC = vsm.addCamera(slVS);
	vgutter = widgets[0];
	vslider = widgets[1];
	upBt = widgets[2];
	downBt = widgets[3];
	hgutter = widgets[4];
	hslider = widgets[5];
	leftBt = widgets[6];
	rightBt = widgets[7];
	vgutterRS = (RectangularShape)vgutter;
	vsliderRS = (RectangularShape)vslider;
	upBtRS = (RectangularShape)upBt;
	downBtRS = (RectangularShape)downBt;
	hgutterRS = (RectangularShape)hgutter;
	hsliderRS = (RectangularShape)hslider;
	leftBtRS = (RectangularShape)leftBt;
	rightBtRS = (RectangularShape)rightBt;
	vsm.addGlyph(vgutter, slVS);
	vsm.addGlyph(vslider, slVS);
	vsm.addGlyph(upBt, slVS);
	vsm.addGlyph(downBt, slVS);
	vsm.addGlyph(hgutter, slVS);
	vsm.addGlyph(hslider, slVS);
	vsm.addGlyph(leftBt, slVS);
	vsm.addGlyph(rightBt, slVS);
    }
    
    
    /** Set the view the controlled camera belongs to.
     * 
     */
    public void setView(View v){
	controlledView = v;
	controlledView.getPanel().addComponentListener(this);
	updateWidgetInvariants();
    }

    public void virtualSpaceUpdated(){
	controlledCamera.getOwningSpace().findFarmostGlyphCoords(populatedRegionBounds);
	updateScrollBars();
    }

    public void cameraUpdated(){
	controlledView.getVisibleRegion(controlledCamera, observedRegionBounds);
	updateScrollBars();
    }

    public void setVerticalScrollbarWidth(int w){
	vgutterRS.setWidth(w);
    }

    public void setHorizontalScrollbarHeight(int h){
	hgutterRS.setHeight(h);
    }

    void updateWidgetInvariants(){
	upBt.vx = downBt.vx = vgutter.vx = vslider.vx = panelWidth / 2 - vgutterRS.getWidth();
	upBt.vy = panelHeight / 2 - upBtRS.getHeight();
	downBt.vy = -panelHeight / 2 + downBtRS.getHeight() + 2 * hgutterRS.getHeight();
	vgutter.vy = (upBt.vy+downBt.vy) / 2;
	vgutterRS.setHeight(panelHeight/2 - hgutterRS.getHeight());

	//XXX
	vsliderRS.setHeight(vgutterRS.getHeight()/2);
	
	leftBt.vy = rightBt.vy = hgutter.vy = hslider.vy = - panelHeight / 2 + hgutterRS.getHeight();
	leftBt.vx = -panelWidth / 2 + leftBtRS.getWidth();
	rightBt.vx = panelWidth / 2 - rightBtRS.getWidth() - 2 * vgutterRS.getWidth();
	hgutter.vx = (leftBt.vx+rightBt.vx)/2;
	hgutterRS.setWidth(panelWidth/2 - vgutterRS.getWidth());

	//XXX
	hsliderRS.setWidth(hgutterRS.getWidth()/2);
    }

    public void updateScrollBars(){
	updateVerticalScrollBar();
	updateHorizontalScrollBar();
    }

    int MIN_SLIDER_SIZE = 5;

    public void updateVerticalScrollBar(){
	long observedHeight = observedRegionBounds[1] - observedRegionBounds[3];
	long totalHeight = populatedRegionBounds[1] - populatedRegionBounds[3];
	if (totalHeight < observedHeight){observedHeight = totalHeight;}
	double ratio = observedHeight / ((double)totalHeight);
	long sliderSize = Math.round(ratio * vgutterRS.getHeight());
	if (sliderSize < MIN_SLIDER_SIZE){
	    sliderSize = MIN_SLIDER_SIZE;
	}
	vsliderRS.setHeight(sliderSize);
	long y = Math.round(2*(controlledCamera.posy-(populatedRegionBounds[1]+populatedRegionBounds[3])/2.0)/(populatedRegionBounds[1] - populatedRegionBounds[3]) * vgutterRS.getHeight());
	if (y > upBt.vy-sliderSize){
	    y = upBt.vy-sliderSize;
	}
	else if (y < downBt.vy+sliderSize){
	    y = downBt.vy+sliderSize;
	}
	vslider.vy = y;
    }

    public void updateHorizontalScrollBar(){
	long observedWidth = observedRegionBounds[2] - observedRegionBounds[0];
	long totalWidth = populatedRegionBounds[2] - populatedRegionBounds[0];
	if (totalWidth < observedWidth){observedWidth = totalWidth;}
	double ratio = observedWidth / ((double)totalWidth);
	long sliderSize = Math.round(ratio * hgutterRS.getWidth());
	if (sliderSize < MIN_SLIDER_SIZE){
	    sliderSize = MIN_SLIDER_SIZE;
	}
	hsliderRS.setWidth(sliderSize);
	long x = Math.round(2*(controlledCamera.posx-(populatedRegionBounds[2]+populatedRegionBounds[0])/2.0)/(populatedRegionBounds[2] - populatedRegionBounds[0]) * hgutterRS.getWidth());
	if (x > rightBt.vx-sliderSize){
	    x = rightBt.vx-sliderSize;
	}
	else if (x < leftBt.vx+sliderSize){
	    x = leftBt.vx+sliderSize;
	}
	hslider.vx = x;
    }

    public Camera getWidgetCamera(){
	return slC;
    }

    public Camera getControlledCamera(){
	return controlledCamera;
    }

    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){updateViewSize(e.getComponent());}
    public void componentShown(ComponentEvent e){}

    void updateViewSize(Component c){
	Dimension d = c.getSize();
	panelWidth = d.width;
	panelHeight = d.height;
	updateWidgetInvariants();
	cameraUpdated();
    }
    
    
}
