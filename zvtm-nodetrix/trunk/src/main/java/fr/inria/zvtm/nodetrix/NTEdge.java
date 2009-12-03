/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

public abstract class NTEdge {

    NTNode tail, head;
    
    public void setNodes(NTNode t, NTNode h){
        this.tail = t;
        this.head = h;
    }

}
