/*   FILE: VImage.java
 *   DATE OF CREATION:  Wed Mar 21 17:27:03 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.glyphs;

import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.Translucent;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Translucent Bitmap Image. This version is less efficient than VImage, but it can be made translucent. It cannot be reoriented (see VImageOr*).<br>
 * If the image features its own alpha channel, the rendering will combine both the embedded image's channel and the translucency settings defined through the Translucent interface.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VImage
 *@see com.xerox.VTM.glyphs.VImageOr
 *@see net.claribole.zvtm.glyphs.VImageOrST
 */

public class VImageST extends VImage implements Translucent {

    AlphaComposite acST;
    float alpha=0.5f;

    /**
     *@param img image to be displayed
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VImageST(Image img, float a){
	super(img);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VImageST(long x,long y,float z,Image img, float a){
	super(x, y, z, img);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
     *@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public VImageST(long x, long y, float z, Image img, double scale, float a){
	super(x, y, z, img, scale);
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
    }

    public void setTranslucencyValue(float a){
	alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);  //translucency set to alpha
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    public float getTranslucencyValue(){
	return alpha;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)){
	    if (zoomSensitive){
		trueCoef = scaleFactor*coef;
	    }
	    else{
		trueCoef = scaleFactor;
	    }
	    //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
	    if (trueCoef!=1.0f){
		// translate
		at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
		g.setTransform(at);
		// rescale and draw
		if (alpha < 1.0f){// translucent
		    g.setComposite(acST);
		    g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		    g.setTransform(stdT);
		    if (drawBorder==1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
			}
		    }
		    else if (drawBorder==2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		    g.setComposite(acO);
		}
		else {// opaque
		    g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		    g.setTransform(stdT);
		    if (drawBorder==1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
			}
		    }
		    else if (drawBorder==2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		}
	    }
	    else {
		if (alpha < 1.0f){// translucent
		    g.setComposite(acST);
		    g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
		    if (drawBorder == 1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
			}
		    }
		    else if (drawBorder == 2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		    g.setComposite(acO);
		}
		else {// opaque
		    g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
		    if (drawBorder == 1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
			}
		    }
		    else if (drawBorder == 2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
		    }
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw > 1) && (pc[i].lch > 1)){
	    if (zoomSensitive){trueCoef=scaleFactor*coef;}
	    else {trueCoef=scaleFactor;}
	    if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
	    if (trueCoef!=1.0f){
		g.setTransform(AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch));
		if (alpha < 1.0f){// translucent
		    g.setComposite(acST);
		    g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		    g.setTransform(stdT);
		    if (drawBorder==1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
			}
		    }
		    else if (drawBorder==2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		    g.setComposite(acO);
		}
		else {// opaque
		    g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
		    g.setTransform(stdT);
		    if (drawBorder==1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
			}
		    }
		    else if (drawBorder==2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		}
	    }
	    else {
		if (alpha < 1.0f){// translucent
		    g.setComposite(acST);
		    g.drawImage(image, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
		    if (drawBorder == 1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
			}
		    }
		    else if (drawBorder == 2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		    g.setComposite(acO);
		}
		else {// opaque
		    g.drawImage(image, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
		    if (drawBorder == 1){
			if (pc[i].prevMouseIn){
			    g.setColor(borderColor);
			    g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
			}
		    }
		    else if (drawBorder == 2){
			g.setColor(borderColor);
			g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
		    }
		}
	    }
	}
	else {
	    g.setColor(this.borderColor);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    public Object clone(){
	VImageST res = new VImageST(vx, vy, 0, image, alpha);
	res.setWidth(vw);
	res.setHeight(vh);
	res.borderColor=this.borderColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	res.setDrawBorderPolicy(drawBorder);
	res.setZoomSensitive(zoomSensitive);
	return res;
    }

}





