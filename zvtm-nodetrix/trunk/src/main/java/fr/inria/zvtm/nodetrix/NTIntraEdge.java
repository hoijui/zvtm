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
import fr.inria.zvtm.glyphs.VRectangle;

public class NTIntraEdge extends NTEdge {
    
    VRectangle edgeRect;
    LongPoint offset;

    public NTIntraEdge(NTNode t, NTNode h, Color c){
        this.tail = t;
        this.head = h;
        this.edgeColor = c;
    }
    
    void createGraphics(long height, long y, long x, long noMeaning, VirtualSpace vs){
        this.offset = new LongPoint(x, y);
        LongPoint mp = tail.getMatrix().getPosition();
        this.edgeRect = new VRectangle(mp.x+offset.x, mp.y+offset.y, 0,
            NodeTrixViz.CELL_SIZE/2, height/2,
            this.edgeColor, Color.white);            
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
