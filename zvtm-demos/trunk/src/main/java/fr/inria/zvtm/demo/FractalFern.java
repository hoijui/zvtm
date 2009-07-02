/*   FILE: FractalFern.java
 *   DATE OF CREATION:  Thu Dec 30 12:53:03 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VPoint;

public class FractalFern extends FractalDemo {

    static int imax = 10000;
    static final int left = 3000;
    static final int w = 30000;
    static final int wl = w + left;
    
    static final double e1 = 0.5 * w;
    static final double e2 = 0.57 * w;
    static final double e3 = 0.408 * w;
    static final double e4 = 0.1075 * w;
    static final double f1 = 0 * w;
    static final double f2 = -0.036 * w;
    static final double f3 = 0.0893 * w;
    static final double f4 = 0.27 * w;

    ProgFrame pf;

    FractalFern(int n){
	if (n > 0){
	    imax = n;
	}
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vs = vsm.addVirtualSpace(mainSpaceName);    
	vs.getCamera(0).setZoomFloor(-90);
	buildFern();
	vsm.addCamera(mainSpaceName);
	Vector cameras=new Vector();
	cameras.add(vs.getCamera(0));
	vsm.addExternalView(cameras, mainViewName, View.STD_VIEW, 800, 600, false, true);
	View v = vsm.getView(mainViewName);
	v.setEventHandler(eh);
	v.setBackgroundColor(Color.black);
	v.mouse.setColor(Color.white);
	vsm.getGlobalView(vs.getCamera(0), 500);
    }

    void reset(){
	vs.removeAllGlyphs();
    }

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c06_fern.java
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    void buildFern(){
	reset();
	pf = new ProgFrame("Computing "+imax+" points...", "Fern Fractal Demo");
	double x = e1;
	double y = 0;
	for (int i = 0; i < imax; i++) {
	    double r = Math.random();
	    double xn, yn;
	    if (r < 0.02) {
		xn = 0 * x + 0 * y + e1;
		yn = 0 * x + 0.27 * y + f1;
	    } else if (r < 0.17) {
		xn = -0.139 * x + 0.263 * y + e2;
		yn = 0.246 * x + 0.224 * y + f2;
	    } else if (r < 0.3) {
		xn = 0.17 * x - 0.215 * y + e3;
		yn = 0.222 * x + 0.176 * y + f3;
	    } else {
		xn = 0.781 * x + 0.034 * y + e4;
		yn = -0.032 * x + 0.739 * y + f4;
	    }
	    vs.addGlyph(new VPoint(left+(int)xn, (int)yn-wl, Color.white));
	    x = xn;
	    y = yn;
	    pf.setPBValue(i*100/imax);
	}
	pf.destroy();
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
		    System.out.println("java [classpath_options] fr.inria.zvtm.demo.FractalFern [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 10000) (Fern algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalFern(n);
    }

}
