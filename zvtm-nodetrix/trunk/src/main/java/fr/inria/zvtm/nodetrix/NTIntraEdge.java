/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VTriangleOr;

public class NTIntraEdge extends NTEdge {
    
//    VRectangle edgeRect;
    Glyph edgeRect;
	LongPoint offset;
    boolean directedInverse; //true if this edge is the inverse of a directed one.

    public NTIntraEdge(NTNode t, NTNode h, Color c, boolean directedInverse){
        this.tail = t;
        this.head = h;
        this.edgeColor = c;
        this.directedInverse = directedInverse;
    }
    
    void createGraphics(long height, long y, long x, long noMeaning, VirtualSpace vs){
        this.offset = new LongPoint(x, y);
        LongPoint mp = tail.getMatrix().getPosition();
        float alpha = 1;
//        LongPoint[] p = new LongPoint[3];
//        long cs = NodeTrixViz.CELL_SIZE/2;
//        
//        p[0] = new LongPoint(mp.x +offset.x - cs, mp.y +offset.y + cs);
//        p[2] = new LongPoint(mp.x +offset.x + cs, mp.y +offset.y - cs);
        if(this.directedInverse)
        {
        	alpha = 0.4f;
//        	p[1] = new LongPoint(mp.x +offset.x - cs, mp.y + offset.y - cs);
//    	}else{
//        	p[1] = new LongPoint(mp.x +offset.x + cs, mp.y + offset.y + cs);
    	}
//        
//        this.edgeRect = new VPolygon(p, 0, this.edgeColor, Color.white, alpha);
//        vs.addGlyph(edgeRect);
//        edgeRect.setOwner(this);
    	
      this.edgeRect = new VRectangle(mp.x+offset.x, mp.y+offset.y, 0,
      NodeTrixViz.CELL_SIZE/2, height/2,
      this.edgeColor, Color.white, alpha);
      vs.addGlyph(edgeRect);
      edgeRect.setOwner(this);
    }

    void moveTo(long x, long y){
        LongPoint mp = tail.getMatrix().getPosition();
        edgeRect.moveTo(mp.x+offset.x, mp.y+offset.y);
    }
    
    void move(long x, long y){
        edgeRect.move(x, y);
    }

}
