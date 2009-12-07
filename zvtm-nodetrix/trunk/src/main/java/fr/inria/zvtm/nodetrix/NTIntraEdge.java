/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.VRectangle;

public class NTIntraEdge extends NTEdge {
    
    VRectangle edgeRect;
    LongPoint offset;

    public NTIntraEdge(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }
    
    public void createGraphics(long dx, long dy, VirtualSpace vs){
        this.offset = new LongPoint(dx, dy);
        LongPoint mp = this.getTail().getMatrix().getPosition();
        this.edgeRect = new VRectangle(mp.x+dx, mp.y+dy, 0,
                                       NodeTrixViz.CELL_SIZE/2, NodeTrixViz.CELL_SIZE/2,
                                       NodeTrixViz.INTRA_LINK_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
        vs.addGlyph(edgeRect);
    }

}
