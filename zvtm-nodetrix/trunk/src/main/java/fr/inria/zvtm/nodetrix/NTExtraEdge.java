/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.DPath;

public class NTExtraEdge extends NTEdge {
    
    DPath edgePath;
    // start and end point offsets w.r.t respective matrices
    LongPoint[] offsets;

    public NTExtraEdge(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }

    void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs){
        offsets = new LongPoint[2];
        offsets[0] = new LongPoint(x1, y1);
        offsets[1] = new LongPoint(x2, y2);
        LongPoint tmp = this.getTail().getMatrix().getPosition();
        LongPoint hmp = this.getHead().getMatrix().getPosition();
        edgePath = new DPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, NodeTrixViz.EXTRA_LINK_COLOR);
        long tm_sz = NodeTrixViz.CELL_SIZE * getTail().getMatrix().getSize()*2;
        long hm_sz = NodeTrixViz.CELL_SIZE * getHead().getMatrix().getSize()*2;
        if (x1 < 0){tm_sz = -tm_sz;}
        if (y2 < 0){hm_sz = -hm_sz;}
        edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                            tmp.x+offsets[0].x+tm_sz, tmp.y+offsets[0].y,
                            hmp.x+offsets[1].x, hmp.y+offsets[1].y+hm_sz, true);        
        vs.addGlyph(edgePath);
        edgePath.setOwner(this);
    }
    
    void moveTo(long x, long y){
        // does not make sense, moving either head or tail
        // see moveHeadTo() and moveTailTo()
    }
    
    void move(long x, long y){
        // x & y are actually ignored, computing new path geometry from matrix position
        LongPoint tmp = this.getTail().getMatrix().getPosition();
        LongPoint hmp = this.getHead().getMatrix().getPosition();
        long tm_sz = NodeTrixViz.CELL_SIZE * getTail().getMatrix().getSize()*2;
        long hm_sz = NodeTrixViz.CELL_SIZE * getHead().getMatrix().getSize()*2;
        if (offsets[0].x < 0){tm_sz = -tm_sz;}
        if (offsets[1].y < 0){hm_sz = -hm_sz;}
        LongPoint[] npos = {new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y),
            new LongPoint(tmp.x+offsets[0].x+tm_sz, tmp.y+offsets[0].y),
            new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y+hm_sz),
            new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y)};
        edgePath.edit(npos, true);
    }
    
    void moveHeadTo(long x, long y){
        // TBW
    }

    void moveTailTo(long x, long y){
        // TBW
    }
    
}
