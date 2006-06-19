/*   FILE: AbstractWorldManager.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.eval;

import java.awt.Color;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRoundRect;

class AbstractWorldManager {
    
    static int TREE_DEPTH = 6; // we want subjects to navigate through 5 levels, includes root
    static final long SMALLEST_ELEMENT_WIDTH = 50;
    static final long MUL_FACTOR = 50;

    static long WORLD_WIDTH;
    static long WORLD_HEIGHT;
    static long HALF_WORLD_WIDTH;
    static long HALF_WORLD_HEIGHT;

    static long[] widthByLevel;
    static Color[] colorByLevel;
    static {
	// compute size of all levels
	widthByLevel = new long[TREE_DEPTH];
	colorByLevel = new Color[TREE_DEPTH];
	for (int i=0;i<TREE_DEPTH;i++){
	    widthByLevel[i] = SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(MUL_FACTOR, (TREE_DEPTH-i-1)));
	    colorByLevel[i] = Color.getHSBColor(0, 0, (i)/((float)TREE_DEPTH));
	}
	WORLD_WIDTH = widthByLevel[0] * 2;
	WORLD_HEIGHT = WORLD_WIDTH / 2;
	HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
	HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
    }

    int NB_CHILDREN = 10;  //{5,10,20}

    static final int ROUND_CORNER_RATIO = 5;

    AbstractRegion root; // region at level 0

    ZLAbstractTask application;

    AbstractWorldManager(ZLAbstractTask app){
	this.application = app;
    }

    void generateWorld(){
	// init highest region
	root = new AbstractRegion(0);
	root.setTarget(new VRoundRect(0, 0, 0, widthByLevel[0], widthByLevel[0]/2, colorByLevel[0], (int)widthByLevel[0]/ROUND_CORNER_RATIO, (int)widthByLevel[0]/ROUND_CORNER_RATIO));
	root.target.setPaintBorder(false);
	application.vsm.addGlyph(root.target, application.mainVS);
	// then populate each lower level
	populateRegion(root);
    }

    void populateRegion(AbstractRegion ar){
	int depth = ar.level + 1;
	// generate NB_CHILDREN-1 false targets
	VRectangle[] ds = new VRectangle[NB_CHILDREN-1];
	long vx,vy;
	for (int i=0;i<NB_CHILDREN-1;i++){
	    vx = Math.round(ar.target.vx + Math.random()*(ar.target.getWidth()-widthByLevel[depth])*2-(ar.target.getWidth()-widthByLevel[depth]));
	    vy = Math.round(ar.target.vy + Math.random()*(ar.target.getHeight()-widthByLevel[depth])*2-(ar.target.getHeight()-widthByLevel[depth]));
	    ds[i] = new VRectangle(vx, vy, 0, widthByLevel[depth], widthByLevel[depth], colorByLevel[depth]);
	    ds[i].setPaintBorder(false);
	    application.vsm.addGlyph(ds[i], application.mainVS);
	}
	ar.setDistractors(ds);
	// generate 1 target
	vx = Math.round(ar.target.vx + Math.random()*(ar.target.getWidth()-widthByLevel[depth])*2-(ar.target.getWidth()-widthByLevel[depth]));
	vy = Math.round(ar.target.vy + Math.random()*(ar.target.getHeight()-widthByLevel[depth])*2-(ar.target.getHeight()-widthByLevel[depth]));
 	VRoundRect tr = new VRoundRect(vx, vy, 0, widthByLevel[depth], widthByLevel[depth], colorByLevel[depth], (int)widthByLevel[depth]/ROUND_CORNER_RATIO, (int)widthByLevel[depth]/ROUND_CORNER_RATIO);
	tr.setPaintBorder(false);
	application.vsm.addGlyph(tr, application.mainVS);
	AbstractRegion car = new AbstractRegion(depth);
	car.setTarget(tr);
	// make this target a region and populate it
	// (unless we have reached TREE_DEPTH)
	if (depth < TREE_DEPTH-1){
	    populateRegion(car);
	}
    }
    
    
}
