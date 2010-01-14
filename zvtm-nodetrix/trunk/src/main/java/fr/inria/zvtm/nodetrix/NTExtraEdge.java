/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.GPath;

public class NTExtraEdge extends NTEdge {
    
    GPath edgePath;
    // start and end point offsets w.r.t respective matrices
    LongPoint[] offsets;

    public NTExtraEdge(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }

    void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs){
        long dx = head.getMatrix().bkg.vx - tail.getMatrix().bkg.vx;
        long dy = head.getMatrix().bkg.vy - tail.getMatrix().bkg.vy;
        if (dx < 0){
            long wo = (tail.getMatrix().nodes.length > 1) ? -NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.length/2-2*tail.getMatrix().label_bkg[0].getWidth() : -tail.getMatrix().bkg.getWidth();
            if (dy < 0){
                // south west of start point
                long no = (head.getMatrix().nodes.length > 1) ? 2*head.getMatrix().label_bkg[1].getHeight() : 0;
                x1 = wo;
                y1 = tail.wdy;
                x2 = head.ndx;
                y2 = NodeTrixViz.CELL_SIZE*head.getMatrix().getSize()/2+no;
            }
            else {
                // north west of start point
                x1 = wo;
                y1 = tail.wdy;
                x2 = head.ndx;
                y2 = -NodeTrixViz.CELL_SIZE*head.getMatrix().getSize()/2;                
            }
        }
        else {
            long wo = (tail.getMatrix().nodes.length > 1) ? NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.length/2 : tail.getMatrix().bkg.getWidth();
            if (dy < 0){
                // south east of start point
                long no = (head.getMatrix().nodes.length > 1) ? 2*head.getMatrix().label_bkg[1].getHeight() : 0;
                x1 = wo;
                y1 = tail.wdy;
                x2 = head.ndx;
                y2 = NodeTrixViz.CELL_SIZE*head.getMatrix().getSize()/2+no;
            }
            else {
                // north east of start point
                x1 = wo;
                y1 = tail.wdy;
                x2 = head.ndx;
                y2 = -NodeTrixViz.CELL_SIZE*head.getMatrix().getSize()/2;                
            }
        }
        offsets = new LongPoint[2];
        offsets[0] = new LongPoint(x1, y1);
        offsets[1] = new LongPoint(x2, y2);
        LongPoint tmp = this.getTail().getMatrix().getPosition();
        LongPoint hmp = this.getHead().getMatrix().getPosition();
        edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, NodeTrixViz.EXTRA_LINK_COLOR);
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
