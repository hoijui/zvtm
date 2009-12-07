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
    
    fr.inria.zvtm.glyphs.VSegment edgePath;
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
        edgePath = new fr.inria.zvtm.glyphs.VSegment(tmp.x+offsets[0].x, tmp.y+offsets[0].y,
                                                     0, NodeTrixViz.EXTRA_LINK_COLOR,
                                                     hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        vs.addGlyph(edgePath);
    }
    
    void moveTo(long x, long y){
        
    }
    
}
