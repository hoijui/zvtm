/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.lens;

import java.awt.image.WritableRaster;

/** Parent class of all lenses which have a parameterable focus offset */

public abstract class FSOLens extends FixedSizeLens {

    int dx = 0;
    int dy = 0;

    public void setXfocusOffset(int x){
        dx = x;
    }

    public void setYfocusOffset(int y){
        dy = y;
    }
    
    public int getXfocusOffset(){
        return dx;
    }
    
    public int getYfocusOffset(){
        return dy;
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
                        //tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = mPixelsI[Math.round(y0*mbw+x0)];
                        tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
                            mPixelsI[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f+dy)*mbw + Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f+dx)];
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
                        mPixelsS[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f+dy)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f+dx)];
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
                        mPixelsB[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / gain[1] + mbh/2.0f+dy)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / gain[0] + mbw/2.0f+dx)];
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

}
