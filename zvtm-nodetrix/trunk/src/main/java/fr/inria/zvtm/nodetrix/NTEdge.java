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

    public abstract void createGraphics(long dx, long dy, VirtualSpace vs);
    
    public NTNode getTail(){
        return tail;
    }
    
    public NTNode getHead(){
        return head;
    }

}
