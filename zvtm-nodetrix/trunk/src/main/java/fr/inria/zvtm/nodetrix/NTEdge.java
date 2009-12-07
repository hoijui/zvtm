/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import fr.inria.zvtm.engine.VirtualSpace;

public abstract class NTEdge {

    NTNode tail, head;
    
    public void setNodes(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }

    abstract void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs);
    
    abstract void moveTo(long x, long y);
    
    public NTNode getTail(){
        return tail;
    }
    
    public NTNode getHead(){
        return head;
    }

}
