/*   FILE: FractalMandelbrot.java
 *   DATE OF CREATION:  Thu Dec 30 12:53:03 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VPoint;
import com.xerox.VTM.glyphs.VSegment;

public class FractalMandelbrot extends FractalDemo {

    static final int left = 20;
    static final int w = 300;
    static final double r = 2;
    static final double s = 2*r/w;
    static final double recen = 0;
    static final double imcen = 0;
    
    static final int kstart = 0;
    static final int kend = -99;
    static final int kstep = -3;
    
    ProgFrame pf;

    FractalMandelbrot(int n){
// 	if (n > 0){
// 	    N = n;
// 	}
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vsm.addVirtualSpace(mainSpaceName);
	buildMandelbrot();
	vsm.addCamera(mainSpaceName);
	vsm.getVirtualSpace(mainSpaceName).getCamera(0).setZoomFloor(-90);
	Vector cameras=new Vector();
	cameras.add(vsm.getVirtualSpace(mainSpaceName).getCamera(0));
	vsm.addExternalView(cameras, mainViewName, View.STD_VIEW, 800, 600, false, true);
	View v = vsm.getView(mainViewName);
	v.setEventHandler(eh);
	v.setBackgroundColor(Color.black);
	v.mouse.setColor(Color.white);
	vsm.getGlobalView(vsm.getVirtualSpace(mainSpaceName).getCamera(0), 500);
    }

    void reset(){
	vsm.destroyGlyphsInSpace(mainSpaceName);
    }

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c14_mandelbrot.html
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    void buildMandelbrot(){
	reset();
	pf = new ProgFrame("Computing points...", "Mandelbrot Fractal Demo");
	vsm.addGlyph(new VSegment((int)(-1/s+left+w/2), left+w/2, 0, Color.gray, (int)(1/s+left+w/2), left+w/2), mainSpaceName);
	vsm.addGlyph(new VSegment(left+w/2, (int)(-1/s+left+w/2), 0, Color.gray, left+w/2, (int)(1/s+left+w/2)), mainSpaceName);
	int x[] = new int[3];
	int y[] = new int[3];
	for (int k = kstart; k >= kend; k += kstep) {
	    boolean ok = false;
	    int i;
	    for (i = 0; i < 10000; i++) {
		if (outside(k, s*i + recen, imcen)) {
		    ok = true;
		    break;
		}
	    }
	    if (!ok) {
		break;
	    }
	    int vin = 0;
	    int vout = 1;
	    int vnew = 2;
	    x[vin] = i-1;
	    x[vout] = i;
	    x[vnew] = i;
	    y[vin] = 0;
	    y[vout] = 0;
	    y[vnew] = 1;
	    int xin = x[vin];
	    int yin = y[vin];
	    int xout = x[vout];
	    int yout = y[vout];
	    //vsm.addGlyph(new VPoint(x[vin]+left+w/2, -y[vin]+left+w/2, Color.white), mainSpaceName);
	    do {
		int vref;
		if (!outside(k, s*x[vnew]+recen, s*y[vnew]+imcen)) {
		    //vsm.addGlyph(new VSegment(x[vin]+left+w/2, -y[vin]+left+w/2, 0, Color.white, x[vnew]+left+w/2, -y[vnew]+left+w/2), mainSpaceName);

		    vsm.addGlyph(new VPoint(x[vin]+left+w/2, -y[vin]+left+w/2, Color.white), mainSpaceName);
		    vref = vin;
		    vin = vnew;
		    vnew = vref;
		} else {
		    vref = vout;
		    vout = vnew;
		    vnew = vref;
		}
		x[vnew] = x[vin] + x[vout] - x[vref];
		y[vnew] = y[vin] + y[vout] - y[vref];
	    } while (x[vin] != xin || y[vin] != yin || x[vout] != xout || y[vout] != yout);
	    pf.setPBValue(k * 100 / (kend - kstart));
	}
	pf.destroy();
    }

    boolean outside(int k, double rec, double imc){
	double re = rec;
	double im = imc;
	for (int j = 0; j < 2-k; j++) {
	    double re2 = re*re;
	    double im2 = im*im;
	    if (re2 + im2 > 256) {
		return true;
	    }
	    im = 2*re*im + imc;
	    re = re2 - im2 + rec;
	}
	return false;
    }

    public static void main(String[] args){
	int n = 0;
	for (int i=0;i<args.length;i++){
	    if (args[i].startsWith("-")){
		if (args[i].startsWith("-N") && args[i].length() > 2){
		    try {
			n = Integer.parseInt(args[i].substring(2));
		    }
		    catch (NumberFormatException ex){System.err.println("Invalid value for -N");}
		}
		else if (args[i].equals("-h") || args[i].indexOf("-help") != -1){
		    System.out.println("java [classpath_options] net.claribole.zvtm.demo.FractalMandelbrot [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 10000) (Mandelbrot algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalMandelbrot(n);
    }

}
