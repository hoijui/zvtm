/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;

public abstract class NTEdge {

    NTNode tail, head;
    Color edgeColor;
    
    Object owner;
    
    public void setNodes(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }

    abstract void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs);
    
    abstract void moveTo(long x, long y);
    
    abstract void move(long x, long y);
    
    public NTNode getTail(){
        return tail;
    }
    
    public NTNode getHead(){
        return head;
    }
    
    public void setOwner(Object o){
        this.owner = o;
    }
    
    public Object getOwner(){
        return owner;
    }

}
