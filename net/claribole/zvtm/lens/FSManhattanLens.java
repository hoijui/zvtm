/*   FILE: FSManhattanLens.java
 *   DATE OF CREATION:  Wed Nov 03 11:51:04 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FSManhattanLens.java,v 1.5 2005/12/05 15:29:08 epietrig Exp $
 */ 


package net.claribole.zvtm.lens;

/**Profile: manhattan - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class FSManhattanLens extends FixedSizeLens {

    double d = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public FSManhattanLens(){
	this.MM = 2.0f;
	updateMagBufferWorkingDimensions();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public FSManhattanLens(float mm){
	this.MM = mm;
	updateMagBufferWorkingDimensions();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public FSManhattanLens(float mm, int innerRadius){
	this.MM = mm;
	this.LR2 = innerRadius;
	updateMagBufferWorkingDimensions();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public FSManhattanLens(float mm, int innerRadius, int x, int y){
	this.MM = mm;
	this.LR2 = innerRadius;
	updateMagBufferWorkingDimensions();
	lx = x;
	ly = y;
    }

    public void gf(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = g[1] = MM;
	else
	    g[0] = g[1] = 1;
    }

    public int getRadius(){
	return LR2;
    }

}