/*   FILE: FractalLSystems.java
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
import java.util.Stack;
import java.util.Vector;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VSegment;

public class FractalLSystems extends FractalDemo {

    static final int left = 300;
    static final int h = 3000000;

    double x, y;
    double dx, dy;
    Stack stack = new Stack();
    
    int N = 8;
    int progressIndex = 0;
    long totalNumberOfGlyphs = 0;
    ProgFrame pf;

    FractalLSystems(int n){
	if (n > 0){
	    N = n;
	}
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vsm.addVirtualSpace(mainSpaceName);
	buildLSystems();
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
	totalNumberOfGlyphs = 0;
	progressIndex = 0;
    }

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c07_lsystems.html
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    void buildLSystems(){
	reset();
	totalNumberOfGlyphs = Math.round(Math.pow(3,N));
	pf = new ProgFrame("Computing 0 segments...", "LSystems Fractal Demo");
	for (int stage = N; stage <= N; stage++) {
	    x = left+h/2;
	    y = left+h;
	    dx = 0;
	    dy = -10*(stage+1);
	    forward(stage);
	}
	pf.destroy();
    }

    void forward(int stage){
	if (stage > 0) {
	    forward(stage-1);
	    save();
	    left();
	    forward(stage-1);
	    restore();
	    save();
	    right();
	    forward(stage-1);
	    restore();
	} 
	else {
	    vsm.addGlyph(new VSegment((int)x, (int)-y, 0, Color.white, (int)(x+dx), (int)-(y+dy)), mainSpaceName);
	    progressIndex+=1;
	    if (progressIndex % 100 == 0){//don't refresh too often: it is time consuming
		pf.setPBValue((int)(progressIndex * 100 / totalNumberOfGlyphs));
		pf.setLabel("Computing "+progressIndex+" segments...");
	    }
	    x += dx;
	    y += dy;
	}
    }
    
    void left(){
	double dx1 = dx;
	dx = 0.707 * dx1 - 0.707 * dy;
	dy = 0.707 * dx1 + 0.707 * dy;
    }
    
    void right(){
	double dx1 = dx;
	dx = 0.866 * dx1 + 0.500 * dy;
	dy = -0.500 * dx1 + 0.866 * dy;
    }
    
    void save(){
	stack.push(new State());
	dx *= 0.7;
	dy *= 0.7;
    }
    
    void restore(){
	State state = (State)stack.pop();
	state.restore();
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
		    System.out.println("java [classpath_options] fr.inria.zvtm.demo.FractalLSystems [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 8) (LSystems algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalLSystems(n);
    }

    class State {
	
	double x, y, dx, dy;
	
	State(){
	    this.x = FractalLSystems.this.x;
	    this.y = FractalLSystems.this.y;
	    this.dx = FractalLSystems.this.dx;
	    this.dy = FractalLSystems.this.dy;
	}
	
	void restore(){
	    FractalLSystems.this.x = this.x;
	    FractalLSystems.this.y = this.y;
	    FractalLSystems.this.dx = this.dx;
	    FractalLSystems.this.dy = this.dy;
	}
	
    }

}

