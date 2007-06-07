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
import com.xerox.VTM.engine.AnimManager;
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
	vgutter = new VRectangleST(0,0,0,10,10,Color.RED);
	vslider = new VRectangleST(0,0,0,10,10,Color.BLUE);
	upBt = new VRectangleST(0,0,0,10,10,Color.GREEN);
	downBt = new VRectangle(0,0,0,10,10,Color.ORANGE);
	hgutter = new VRectangleST(0,0,0,10,10,Color.RED);
	hslider = new VRectangleST(0,0,0,10,10,Color.BLUE);
	leftBt = new VRectangleST(0,0,0,10,10,Color.GREEN);
	rightBt = new VRectangleST(0,0,0,10,10,Color.ORANGE);
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
	vgutter.setSensitivity(false);
	hgutter.setSensitivity(false);
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
	updateViewSize(v.getPanel());
	updateScrollBars();
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
	upBt.vx = downBt.vx = vgutter.vx = vslider.vx = Math.round(Math.ceil(panelWidth / 2.0 - vgutterRS.getWidth()));
	upBt.vy = Math.round(Math.ceil(panelHeight / 2.0 - upBtRS.getHeight()));
	downBt.vy = Math.round(Math.ceil(-panelHeight / 2.0 + downBtRS.getHeight() + 2.0 * hgutterRS.getHeight()));
	vgutter.vy = Math.round(Math.ceil((upBt.vy+downBt.vy)/2.0));
	vgutterRS.setHeight(Math.round(Math.ceil(panelHeight/2.0 - hgutterRS.getHeight() - upBtRS.getHeight() - downBtRS.getHeight())));
	leftBt.vy = rightBt.vy = hgutter.vy = hslider.vy = Math.round(Math.ceil(-panelHeight / 2.0 + hgutterRS.getHeight()));
	leftBt.vx = Math.round(Math.ceil(-panelWidth / 2.0 + leftBtRS.getWidth()));
	rightBt.vx = Math.round(Math.ceil(panelWidth / 2.0 - rightBtRS.getWidth() - 2.0 * vgutterRS.getWidth()));
	hgutter.vx = Math.round(Math.ceil((leftBt.vx+rightBt.vx)/2.0));
	hgutterRS.setWidth(Math.round(Math.ceil(panelWidth/2.0 - vgutterRS.getWidth() - leftBtRS.getWidth() - rightBtRS.getWidth())));
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
	if (y > upBt.vy-upBtRS.getHeight()-sliderSize){
	    y = upBt.vy-upBtRS.getHeight()-sliderSize;
	}
	else if (y < downBt.vy+downBtRS.getHeight()+sliderSize){
	    y = downBt.vy+downBtRS.getHeight()+sliderSize;
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
	if (x > rightBt.vx-rightBtRS.getWidth()-sliderSize){
	    x = rightBt.vx-rightBtRS.getWidth()-sliderSize;
	}
	else if (x < leftBt.vx+leftBtRS.getWidth()+sliderSize){
	    x = leftBt.vx+leftBtRS.getWidth()+sliderSize;
	}
	hslider.vx = x;
    }

    public void updateCameraVerticalPosition(){
	controlledCamera.moveTo(0, Math.round(vslider.vy * (populatedRegionBounds[1]-populatedRegionBounds[3]) / (2.0*vgutterRS.getHeight()) + (populatedRegionBounds[1] + populatedRegionBounds[3])/2.0));
    }
    
    public void updateCameraHorizontalPosition(){
	controlledCamera.moveTo(Math.round(hslider.vx * (populatedRegionBounds[2]-populatedRegionBounds[0]) / (2.0*hgutterRS.getWidth()) + (populatedRegionBounds[2] + populatedRegionBounds[0])/2.0), 0);
    }

    /** Tells whether the given point is inside the area containing the scroll bars or not. 
     *@param x provide projected JPanel coordinates of the associated view, not virtual space coordinates
     *@param y provide projected JPanel coordinates of the associated view, not virtual space coordinates
     */
    public boolean cursorInside(int cx, int cy){
	double coef = (((double)slC.focal+(double)slC.altitude) / (double)slC.focal);
	long vx = Math.round(((cx - (panelWidth/2)) * coef) + slC.posx);
	long vy = Math.round((((panelHeight/2) - cy) * coef) + slC.posy);
	return (vx > vgutter.vx-vgutterRS.getWidth()) || (vy < hgutter.vy+hgutterRS.getHeight());
    }

    public Camera getWidgetCamera(){
	return slC;
    }

    public Camera getControlledCamera(){
	return controlledCamera;
    }

    public void draggingHorizontalSlider(int dx){
	if (hslider.vx + dx + hsliderRS.getWidth() < rightBt.vx - rightBtRS.getWidth() &&
	    hslider.vx + dx - hsliderRS.getWidth() > leftBt.vx + leftBtRS.getWidth()){
	    hslider.move(dx, 0);
	    updateCameraHorizontalPosition();
	}
    }

    public void draggingVerticalSlider(int dy){
	if (vslider.vy + dy + vsliderRS.getHeight() < upBt.vy - upBtRS.getHeight() &&
	    vslider.vy + dy - vsliderRS.getHeight() > downBt.vy + downBtRS.getHeight()){
	    vslider.move(0, dy);
	    updateCameraVerticalPosition();
	}
    }

    public void moveUp(){
	long dy = vsliderRS.getHeight();
	if (vslider.vy + dy + vsliderRS.getHeight() < upBt.vy - upBtRS.getHeight() &&
	    vslider.vy + dy - vsliderRS.getHeight() > downBt.vy + downBtRS.getHeight()){
	    vslider.move(0, dy);
	    updateCameraVerticalPosition();
	}
	else {
	    vslider.moveTo(vslider.vx, upBt.vy - upBtRS.getHeight() - vsliderRS.getHeight());
	    updateCameraVerticalPosition();
	}
    }

    public void moveDown(){
	long dy = -vsliderRS.getHeight();
	if (vslider.vy + dy + vsliderRS.getHeight() < upBt.vy - upBtRS.getHeight() &&
	    vslider.vy + dy - vsliderRS.getHeight() > downBt.vy + downBtRS.getHeight()){
	    vslider.move(0, dy);
	    updateCameraVerticalPosition();
	}
	else {
	    vslider.moveTo(vslider.vx, downBt.vy + downBtRS.getHeight() + vsliderRS.getHeight());
	    updateCameraVerticalPosition();
	}
    }

    public void moveLeft(){
	long dx = -hsliderRS.getWidth();
	if (hslider.vx + dx + hsliderRS.getWidth() < rightBt.vx - rightBtRS.getWidth() &&
	    hslider.vx + dx - hsliderRS.getWidth() > leftBt.vx + leftBtRS.getWidth()){
	    hslider.move(dx, 0);
	    updateCameraHorizontalPosition();
	}
	else {
	    hslider.moveTo(leftBt.vx + leftBtRS.getWidth() + hsliderRS.getWidth(), hslider.vy);
	    updateCameraHorizontalPosition();
	}
    }

    public void moveRight(){
	long dx = hsliderRS.getWidth();
	if (hslider.vx + dx + hsliderRS.getWidth() < rightBt.vx - rightBtRS.getWidth() &&
	    hslider.vx + dx - hsliderRS.getWidth() > leftBt.vx + leftBtRS.getWidth()){
	    hslider.move(dx, 0);
	    updateCameraHorizontalPosition();
	}
	else {
	    hslider.moveTo(rightBt.vx - rightBtRS.getWidth() - hsliderRS.getWidth(), hslider.vy);
	    updateCameraHorizontalPosition();
	}
    }

    public Glyph getVerticalSlider(){return vslider;}
    public Glyph getHorizontalSlider(){return hslider;}
    public Glyph getUpButton(){return upBt;}
    public Glyph getDownButton(){return downBt;}
    public Glyph getLeftButton(){return leftBt;}
    public Glyph getRightButton(){return rightBt;}

    /** Make scroll bars fade in (gradually appear).
     * Make sure glyphs used to represent scrollbar widgets implement the Translucent interface.
     */
    public void fade(AnimManager am, int duration, float alphaOffset) throws ClassCastException {
	float[] FADE_IN_DATA = {0, 0, 0, 0, 0, 0, alphaOffset};
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, vgutter.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, vslider.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, upBt.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, downBt.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, hgutter.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, hslider.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, rightBt.getID());
	am.createGlyphAnimation(duration, AnimManager.GL_COLOR_LIN, FADE_IN_DATA, leftBt.getID());
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
