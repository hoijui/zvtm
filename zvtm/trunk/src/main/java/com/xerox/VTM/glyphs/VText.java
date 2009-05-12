/*   FILE: VText.java
 *   DATE OF CREATION:   Nov 23 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import net.claribole.zvtm.glyphs.projection.ProjText;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;

/**
 * Standalone Text.  This version is the most efficient, but it cannot be reoriented (see VTextOr*).<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).<br>
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VTextOr
 *@see com.xerox.VTM.glyphs.LText
 *@see com.xerox.VTM.glyphs.LBText
 *@see net.claribole.zvtm.glyphs.VTextST
 *@see net.claribole.zvtm.glyphs.VTextOrST
 */

public class VText extends Glyph {

    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with start of String). */
    public static final short TEXT_ANCHOR_START=0;
    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with middle of String). */
    public static final short TEXT_ANCHOR_MIDDLE=1;
    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with end of String). */
    public static final short TEXT_ANCHOR_END=2;

    /** Text alignment (read-only). Use access methods to change. One of TEXT_ANCHOR_*. */
    public short text_anchor = TEXT_ANCHOR_START;

    /** Affine Transform used when drawing text. For internal use. */
    public AffineTransform at;

    /** For internal use. */
    public ProjText[] pc;

    /** (read-only), use access methods to change.
     *@see #isZoomSensitive()
     *@see #setZoomSensitive(boolean b)
     */
    public boolean zoomSensitive=true;

    /** Font size in pixels (read-only). */
    public static float fontSize=VirtualSpaceManager.getMainFont().getSize2D();

    /** Special font used in this object only. Null if default font. (read-only), use access methods to change.
     *@see #setSpecialFont(Font f)
     *@see #getFont()
     */
    public Font font;

    /** For internal use. */
    public Rectangle2D bounds;

    /** Text that should be painted (read-only), use access methods to change.
     *@see #setText(String t)
     *@see #getText()
     */
    public String text;

    /** For internal use. Made public for easier outside package subclassing. */
    public float scaleFactor = 1.0f;
    
    /** For internal use. Made public for easier outside package subclassing. */
    public float trueCoef = 1.0f;

    public VText(String t){
	vx=0;
	vy=0;
	vz=0;
	text=t;
	setColor(Color.white);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     */
    public VText(long x,long y, int z,Color c,String t){
	vx=x;
	vy=y;
	vz=z;
	text=t;
	setColor(c);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public VText(long x,long y, int z,Color c,String t,short ta){
	vx=x;
	vy=y;
	vz=z;
	text=t;
	setColor(c);
	text_anchor=ta;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
     */
    public VText(long x, long y, int z, Color c, String t, short ta, float scale){
	vx=x;
	vy=y;
	vz=z;
	text=t;
	setColor(c);
	text_anchor=ta;
	scaleFactor = scale;
    }

    public void initCams(int nbCam){
	pc=new ProjText[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjText();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjText[] ta=pc;
		pc=new ProjText[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjText();
	    }
	    else {System.err.println("VText:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjText[1];
		pc[0]=new ProjText();
	    }
	    else {System.err.println("VText:Error while adding camera "+verifIndex);}
	}
    }

    public void removeCamera(int index){
	pc[index]=null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
    }

    /** Cannot be resized. */
    public void sizeTo(float factor){}

    /** Cannot be resized. */
    public void reSize(float factor){}

    /** Cannot be reoriented. */
    public void orientTo(float angle){}

    /** Always returns 1. */
    public float getSize(){return 1.0f;}

    public float getOrient(){return orient;}

    /** Set to false if the text should not be scaled according to camera's altitude. Its apparent size will always be the same, no matter the camera's altitude.
     *@see #isZoomSensitive()
     */
    public void setZoomSensitive(boolean b){
	if (zoomSensitive!=b){
	    zoomSensitive=b;
	    VirtualSpaceManager.INSTANCE.repaintNow();
	}
    }

    /** Indicates whether the text is scaled according to camera's altitude.
     *@see #setZoomSensitive(boolean b)
     */
    public boolean isZoomSensitive(){
	return zoomSensitive;
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
        if (!validBounds(i)){return true;}
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            //if glyph hotspot is in the region, it is obviously visible
            return true;
        }
        else {
            // cw and ch actually hold width and height of text *in virtual space*
            if (text_anchor==TEXT_ANCHOR_START){
                if ((vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                }
                //otherwise the glyph is not visible
                else return false;
            }
            else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                if ((vx-pc[i].cw/2<=eb) && ((vx+pc[i].cw/2)>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                }
                //otherwise the glyph is not visible
                else return false;
            }
            else {
                //TEXT_ANCHOR_END
                if ((vx-pc[i].cw<=eb) && (vx>=wb) && (vy<=nb) && ((vy+pc[i].ch)>=sb)){
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                }
                //otherwise the glyph is not visible
                else return false;
            }
        }
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region.
	       There is a good chance the glyph is contained in the region, but this is not sufficient. */
	    // cw and ch actually hold width and height of text *in virtual space*
	    if (text_anchor==TEXT_ANCHOR_START){
		if ((vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	    else if (text_anchor==TEXT_ANCHOR_MIDDLE){
		if ((vx+pc[i].cw/2<=eb) && ((vx-pc[i].cw/2)>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	    else {//TEXT_ANCHOR_END
		if ((vx+pc[i].cw<=eb) && (vx>=wb) && (vy<=nb) && ((vy-pc[i].ch)>=sb)){
		    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
		    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
		}
	    }
	}
	return false;
    }

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        boolean res=false;
        switch (text_anchor){
            case VText.TEXT_ANCHOR_START:{
                if ((cvx>=vx) && (cvy>=vy) && (cvx<=(vx+pc[camIndex].cw)) && (cvy<=(vy+pc[camIndex].ch))){res=true;}
                break;
            }
            case VText.TEXT_ANCHOR_MIDDLE:{
                if ((cvx>=vx-pc[camIndex].cw/2) && (cvy>=vy) && (cvx<=(vx+pc[camIndex].cw/2)) && (cvy<=(vy+pc[camIndex].ch))){res=true;}
                break;
            }
            default:{
                if ((cvx<=vx) && (cvy>=vy) && (cvx>=(vx-pc[camIndex].cw)) && (cvy<=(vy+pc[camIndex].ch))){res=true;}
            }
        }
        return res;
    }

	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
	    if (text_anchor==TEXT_ANCHOR_START){
    		return dvs.intersects(vx, vy, pc[camIndex].cw, pc[camIndex].ch);
        }
        else if (text_anchor==TEXT_ANCHOR_MIDDLE){
    		return dvs.intersects(vx-pc[camIndex].cw/2, vy, pc[camIndex].cw, pc[camIndex].ch);
        }
        else {
            //TEXT_ANCHOR_END
    		return dvs.intersects(vx-pc[camIndex].cw, vy, pc[camIndex].cw, pc[camIndex].ch);
        }	    
	}

    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
	    if (coordInside(jpx, jpy, camIndex, cvx, cvy)){
             //if the mouse is inside the glyph
             if (!pc[camIndex].prevMouseIn){
                 //if it was not inside it last time, mouse has entered the glyph
                 pc[camIndex].prevMouseIn=true;
                 return Glyph.ENTERED_GLYPH;
             }
             //if it was inside last time, nothing has changed
             else {return Glyph.NO_CURSOR_EVENT;}  
         }
         else{
             //if the mouse is not inside the glyph
             if (pc[camIndex].prevMouseIn){
                 //if it was inside it last time, mouse has exited the glyph
                 pc[camIndex].prevMouseIn=false;
                 return Glyph.EXITED_GLYPH;
             }//if it was not inside last time, nothing has changed
             else {return Glyph.NO_CURSOR_EVENT;}
         }
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
    }

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		g.setColor(this.color);
		trueCoef = scaleFactor * coef;
		if (trueCoef*fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive){
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (!pc[i].valid){
				bounds = g.getFontMetrics().getStringBounds(text,g);
				// cw and ch actually hold width and height of text *in virtual space*
				pc[i].cw = (int)Math.round(bounds.getWidth() * scaleFactor);
				pc[i].ch = (int)Math.round(bounds.getHeight() * scaleFactor);
				pc[i].valid=true;
			}
			if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
			else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
			else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
			if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
			g.setTransform(at);
			g.drawString(text, 0.0f, 0.0f);
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		}
	}

	public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		g.setColor(this.color);
		trueCoef = scaleFactor * coef;
		if (trueCoef*fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive){
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (!pc[i].lvalid){
				bounds = g.getFontMetrics().getStringBounds(text,g);
				// lcw and lch actually hold width and height of text *in virtual space*
				pc[i].lcw = (int)Math.round(bounds.getWidth() * scaleFactor);
				pc[i].lch = (int)Math.round(bounds.getHeight() * scaleFactor);
				pc[i].lvalid=true;
			}
			if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx,dy+pc[i].lcy);}
			else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef/2.0f,dy+pc[i].lcy);}
			else {at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw*coef,dy+pc[i].lcy);}
			if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
			g.setTransform(at);
			g.drawString(text, 0.0f, 0.0f);
			g.setTransform(stdT);
		}
		else {
			g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		}
	}

    /** Set text that should be painted. */
    public void setText(String t){
	text=t;
	VirtualSpaceManager.INSTANCE.repaintNow();
	invalidate();
    }

    /** Set the scale factor for this text.
     * The actual size of the text will be that defined by the font size multiplied by this scale factor.
     *@param s scale factor
     */
    public void setScale(float s){
	scaleFactor = s;
	invalidate();
    }
    
    /** Get the scale factor for this text.
     * The actual size of the text is that defined by the font size multiplied by the scale factor returned by this method.
     */
    public float getScale(){
	return scaleFactor;
    }

    /** Force computation of text's bounding box at next call to draw(). */
    public void invalidate(){
	try {
	    for (int i=0;i<pc.length;i++){
		pc[i].valid=false;
		pc[i].lvalid=false;
	    }
	}
	catch (NullPointerException ex){}
    }

    /** Get the width and height of the bounding box in virtual space.
     *@param i index of camera (Camera.getIndex())
     *@return the width and height of the text's bounding box, as a LongPoint
     */
    public LongPoint getBounds(int i){
	return new LongPoint(pc[i].cw,pc[i].ch);
    }

    /** Indicates whether the bounds of the text are valid at this time or not.
     * The bounds can be invalid if the thread in charge of painting has not dealt with this glyph since invalidate() was last called on it.
     * It is advisable to test this before calling getBounds()
     */
    public boolean validBounds(int i){
	return pc[i].valid;
    }

    /** Change the Font used to display this specific text object.
     *@param f set to null to use the default font
     */
    public void setSpecialFont(Font f){
	if (f!=null){font=f;fontSize=font.getSize2D();}else{font=null;fontSize=VirtualSpaceManager.getMainFont().getSize2D();}
	VirtualSpaceManager.INSTANCE.repaintNow();
	invalidate();
    }

    /** Get the Font used to display this specific text object.
     *@return the main ZVTM font if no specific Font is used in this object
     */
    public Font getFont(){
	if (font!=null){return font;}
	else return VirtualSpaceManager.getMainFont();
    }

    /** Indicates whether this glyph is using a special font or not.
     * Using a special font does not necessarily mean that this font is different from the default font.
     */
    public boolean usesSpecialFont(){
	if (font==null){return false;}
	else {return true;}
    }

    /** Get text painted by this glyph. */
    public String getText(){return text;}

    /** Set the text anchor
     *@param ta one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END
     */
    public void setTextAnchor(short ta){
	text_anchor=ta;
    }

    /** Get text anchor.
     *@return one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END
     */
    public short getTextAnchor(){
	return text_anchor;
    }

    public Object clone(){
	VText res=new VText(vx,vy,0,color,(new StringBuffer(text)).toString(),text_anchor);
	res.mouseInsideColor=this.mouseInsideColor;
	return res;
    }

    /** Highlight this glyph to give visual feedback when the cursor is inside it. */
    public void highlight(boolean b, Color selectedColor){
        boolean update = false;
        if (b){
            if (mouseInsideColor != null){color = mouseInsideColor;update = true;}
        }
        else {
            if (isSelected() && selectedColor != null){
                color = selectedColor;
                update = true;
            }
            else {
                if (mouseInsideColor != null){color = fColor;update = true;}
            }
        }
        if (update){
		VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }

}
