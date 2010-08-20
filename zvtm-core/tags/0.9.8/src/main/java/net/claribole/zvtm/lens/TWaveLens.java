/*   FILE: TWaveLens.java
 *   DATE OF CREATION:  Fri Oct 06 08:41:04 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TWaveLens.java 664 2007-06-07 07:44:44Z epietrig $
 */ 


package net.claribole.zvtm.lens;

/**Translucent lens. Profile: sin(x)/x - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class TWaveLens extends TLens {

    float N = 5.0f;

    /* gain function parameters (transition in translucence space) */
    protected double aT = 0;
    protected double bT = 0;

    /**
        * create a lens with a maximum magnification factor of 2.0
        */
    public TWaveLens(){
        this.MM = 2.0f;
        updateMagBufferWorkingDimensions();
        computeDropoffFactors();
    }

    /**
        * create a lens with a given maximum magnification factor
        *
        *@param mm magnification factor, mm in [0,+inf[
        */
    public TWaveLens(float mm, float n){
        this.MM = mm;
        this.N = n;
        updateMagBufferWorkingDimensions();
        computeDropoffFactors();
    }

    /**
        * create a lens with a given maximum magnification factor, inner and outer radii
        *
        *@param mm magnification factor, mm in [0,+inf[
        *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
        *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        */
    public TWaveLens(float mm, float tc, float tf, int outerRadius, int innerRadius, float n){
        this.MM = mm;
        this.LR1 = outerRadius;
        this.LR2 = innerRadius;
        this.N = n;
        this.MMTc = tc;
        this.MMTf = tf;
        updateMagBufferWorkingDimensions();
        computeDropoffFactors();
    }

    /**
        * create a lens with a given maximum magnification factor, inner and outer radii
        *
        *@param mm magnification factor, mm in [0,+inf[
        *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
        *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
        *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
        */
    public TWaveLens(float mm, float tc, float tf, int outerRadius, int innerRadius, float n, int x, int y){
        this.MM = mm;
        this.LR1 = outerRadius;
        this.LR2 = innerRadius;
        this.N = n;
        this.MMTc = tc;
        this.MMTf = tf;
        updateMagBufferWorkingDimensions();
        computeDropoffFactors();
        lx = x;
        ly = y;
    }

    /**
        * set the lens' inner radius (beyond which maximum magnification is applied - inward)
        *
        *@param r radius in pixels
        */
    public void setInnerRadius(int r){
        super.setInnerRadius(r);
        bT = LR2 / N;
    }

    /**
        * set the lens' radii
        *
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        */
    public void setRadii(int outerRadius, int innerRadius, boolean forceRaster){
        super.setRadii(outerRadius, innerRadius, forceRaster);
        bT = LR2 / N;
    }


    /**
        * set the lens' radii and maximum magnification
        *
        *@param mm maximum magnification factor, mm in [0,+inf[
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius){
        this.setMMandRadii(mm, outerRadius, innerRadius, true);
        bT = LR2 / N;
    }

    /**
        * set the lens' radii and maximum magnification
        *
        *@param mm maximum magnification factor, mm in [0,+inf[
        *@param outerRadius outer radius (beyond which no magnification is applied - outward)
        *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
        */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius, boolean forceRaster){
        super.setMMandRadii(mm, outerRadius, innerRadius, forceRaster);
        bT = LR2 / N;
    }

    void computeDropoffFactors(){
        aT = MMTf - MMTc;
        bT = LR2 / N;
    }
    
    public void setN(float n){
        this.N = n;
        bT = LR2 / N;
    }
    
    public float getN(){
        return N;
    }

    public void gfT(float x, float y, float[] g){
        d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
        if (d <= LR2)
            g[0] = MMTf;
        else if (d <= LR1){
            // g[0] = (float)((MMTf-MMTc)*Math.sin(d/N-LR2/N)/(d/N-LR2/N)+MMTc);
            g[0] = (float)(aT*Math.sin(d/N-bT)/(d/N-bT)+MMTc);
            if (g[0] < MMTc){
                g[0] = MMTc;
            }
        }
        else
            g[0] = 0;
    }

}
