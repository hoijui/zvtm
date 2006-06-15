/*   FILE: FixedSizeLens.java
 *   DATE OF CREATION:  Tue Nov 09 11:51:28 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FixedSizeLens.java,v 1.22 2006/05/31 12:05:16 epietrig Exp $
 */ 

package net.claribole.zvtm.lens;

import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.Dimension;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.ViewPanel;

/**Parent class of all lenses which have a fixed size (i.e. whose radius does not depend on the view's size)*/

public abstract class FixedSizeLens extends Lens {

    //lens radii
    protected int LR1 = 100;
    protected int LR2 = 50;

    /**
     * set the lens' outer radius (beyond which no magnification is applied - outward)
     *
     *@param r radius in pixels
     */
    public void setOuterRadius(int r){
	LR1 = r;
	updateMagBufferWorkingDimensions();
    }
    
    /**
     * set the lens' inner radius (beyond which maximum magnification is applied - inward)
     *
     *@param r radius in pixels
     */
    public void setInnerRadius(int r){
	LR2 = r;
	updateMagBufferWorkingDimensions();
    }

    /**
     * set the lens' radii
     *
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setRadii(int outerRadius, int innerRadius){
	setRadii(outerRadius, innerRadius, true);
    }

    /**
     * set the lens' radii
     *
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param forceRaster true if the magnification raster size should be updated according to the new maximum magnification factor (default is true)
     */
    public void setRadii(int outerRadius, int innerRadius, boolean forceRaster){
	LR1 = outerRadius;
	LR2 = innerRadius;
	updateMagBufferWorkingDimensions();
	if (forceRaster){
	    setMagRasterDimensions(mbw, mbh);
	}
    }

    /**
     * set the lens' radii and maximum magnification
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius){
	setMMandRadii(mm, outerRadius, innerRadius, true);
    }

    /**
     * set the lens' radii and maximum magnification
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param forceRaster true if the magnification raster size should be updated according to the new maximum magnification factor (default is true)
     */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius, boolean forceRaster){
	MM = mm;
	LR1 = outerRadius;
	LR2 = innerRadius;
	updateMagBufferWorkingDimensions();
	if (forceRaster){
	    setMagRasterDimensions(mbw, mbh);
	}
    }

    /**
     * get the lens' outer radius (beyond which no magnification is applied - outward)
     */
    public int getOuterRadius(){
	return LR1;
    }

    public int getRadius(){
	return LR1;
    }

    /**
     * get the lens' inner radius (beyond which maximum magnification is applied - inward)
     */
    public int getInnerRadius(){
	return LR2;
    }

    /**Should not be called directly ; used to update buffer image caracteristics when lens outer radius changes*/
    public synchronized void setLensBuffer(ViewPanel p){
	owningView = p;
	Dimension s = p.getSize();
	w = s.width;
	h = s.height;
	sw = w / 2;
	sh = h / 2;
	lurd[0] = lx + sw - LR1;
	lurd[1] = ly + sh - LR1;
	lurd[2] = lx + sw + LR1;
	lurd[3] = ly + sh + LR1;
	if (lurd[0] < 0){lurd[0] = 0;}
	if (lurd[1] < 0){lurd[1] = 0;}
	if (lurd[2] > w){lurd[2] = w;}
	if (lurd[3] > h){lurd[3] = h;}
	lensWidth = lurd[2] - lurd[0];
	lensHeight = lurd[3] - lurd[1];
	BufferedImage tbi = (BufferedImage)p.createImage(1,1);
	imageType = tbi.getType();
	transferType = tbi.getRaster().getTransferType();
	initBuffers((lensWidth)*(lensHeight), (mbw)*(mbh));
    }

    synchronized void transformI(WritableRaster iwr, WritableRaster ewr){
	synchronized(this){
	    // get source pixels in an array
	    iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsI);
	    // get magnified source pixels in a second array
	    ewr.getDataElements(0, 0, mbw, mbh, mPixelsI);
	    // transfer them to the target array taking the gain function into account
	    for (int x=lurd[0];x<lurd[2];x++){
		for (int y=lurd[1];y<lurd[3];y++){
		    /* gain is computed w.r.t main buffer pixels
		       (we do not want to compute the gain for pixels that won't be in the output) */
		    this.gf(x,y,gain);
		    if (gain[0] > mSwitchThreshold || gain[1] > mSwitchThreshold){
			/* following 3 commented lines left here for documentation of what the actual
			   single instruction means, x0 and y0 being mere int variables */
			//x0 = Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f);
			//y0 = Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f);
			//tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = mPixelsI[Math.round(y0*mbw+x0];
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    mPixelsI[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f)];
		    }
		    else {
			//x0 = Math.round((((float)x-sw-lx)/gain[0])+sw+lx);
			//y0 = Math.round((((float)y-sh-ly)/gain[1])+sh+ly);
			//tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = oPixelsI[(y0-lurd[1])*(lensWidth)+(x0-lurd[0])];
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    oPixelsI[(Math.round((((float)y-sh-ly)/gain[1])+sh+ly)-lurd[1])*(lensWidth)+(Math.round((((float)x-sw-lx)/gain[0])+sw+lx)-lurd[0])];
  		    }
		}
	    }
	    // transfer pixels in the target array back to the raster
	    iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsI);
	}
    }

    synchronized void transformS(WritableRaster iwr, WritableRaster ewr){
	// get source pixels in an array
 	iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsS);
	// get magnified source pixels in a second array
	ewr.getDataElements(0, 0, mbw, mbh, mPixelsS);
	// transfer them to the target array taking the gain function into account
	for (int x=lurd[0];x<lurd[2];x++){
	    for (int y=lurd[1];y<lurd[3];y++){
		/* gain is computed w.r.t main buffer pixels
		   (we do not want to compute the gain for pixels that won't be in the output) */
		this.gf(x,y,gain);
 		if (gain[0] > mSwitchThreshold || gain[1] > mSwitchThreshold){
		    /* following 3 commented lines left here for documentation of what the actual
		       single instruction means, x0 and y0 being mere int variables */
		    //x0 = Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f);
		    //y0 = Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f);
		    //tPixelsS[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = mPixelsS[Math.round(y0*mbw+x0];
		    tPixelsS[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			mPixelsS[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f)];
		}
		else {
		    //x0 = Math.round((((float)x-sw-lx)/gain[0])+sw+lx);
		    //y0 = Math.round((((float)y-sh-ly)/gain[1])+sh+ly);
		    //tPixelsS[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = oPixelsS[(y0-lurd[1])*(lensWidth)+(x0-lurd[0])];
		    tPixelsS[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			oPixelsS[(Math.round((((float)y-sh-ly)/gain[1])+sh+ly)-lurd[1])*(lensWidth)+(Math.round((((float)x-sw-lx)/gain[0])+sw+lx)-lurd[0])];
		}
	    }
	}
	// transfer pixels in the target array back to the raster
	iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsS);
    }

    synchronized void transformB(WritableRaster iwr, WritableRaster ewr){
	// get source pixels in an array
 	iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsB);
	// get magnified source pixels in a second array
	ewr.getDataElements(0, 0, mbw, mbh, mPixelsB);
	// transfer them to the target array taking the gain function into account
	for (int x=lurd[0];x<lurd[2];x++){
	    for (int y=lurd[1];y<lurd[3];y++){
		/* gain is computed w.r.t main buffer pixels
		   (we do not want to compute the gain for pixels that won't be in the output) */
		this.gf(x,y,gain);
 		if (gain[0] > mSwitchThreshold || gain[1] > mSwitchThreshold){
		    /* following 3 commented lines left here for documentation of what the actual
		       single instruction means, x0 and y0 being mere int variables */
		    //x0 = Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f);
		    //y0 = Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f);
		    //tPixelsB[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = mPixelsB[Math.round(y0*mbw+x0];
		    tPixelsB[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			mPixelsB[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f)];
		}
		else {
		    //x0 = Math.round((((float)x-sw-lx)/gain[0])+sw+lx);
		    //y0 = Math.round((((float)y-sh-ly)/gain[1])+sh+ly);
		    //tPixelsB[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = oPixelsB[(y0-lurd[1])*(lensWidth)+(x0-lurd[0])];
		    tPixelsB[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			oPixelsB[(Math.round((((float)y-sh-ly)/gain[1])+sh+ly)-lurd[1])*(lensWidth)+(Math.round((((float)x-sw-lx)/gain[0])+sw+lx)-lurd[0])];
		}
	    }
	}
	// transfer pixels in the target array back to the raster
	iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsB);
    }

    /**set the position of the lens inside the view
     *@param ax lens's center horizontal coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param ay lens's center vertical coordinate expressed as an absolute position within the view (JPanel coordinate system)
     */
    public synchronized void setAbsolutePosition(int ax, int ay){
	lx = ax - sw;
	ly = ay - sh;
	lurd[0] = lx + sw - LR1;
	lurd[1] = ly + sh - LR1;
	lurd[2] = lx + sw + LR1;
	lurd[3] = ly + sh + LR1;
	if (lurd[0] < 0){lurd[0] = 0;}
	if (lurd[1] < 0){lurd[1] = 0;}
	if (lurd[2] > w){lurd[2] = w;}
	if (lurd[3] > h){lurd[3] = h;}
	lensWidth = lurd[2] - lurd[0];
	lensHeight = lurd[3] - lurd[1];
    }

}

